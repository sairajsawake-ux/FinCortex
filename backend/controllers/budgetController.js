/**
 * Budget Controller
 * Handles budget management and auto-update logic
 */

const admin = require('../firebase/firebaseAdmin');
const db = admin.firestore();
const { validateBudget, sanitizeBudget } = require('../utils/validator');

// Standard API response format
const apiResponse = (res, statusCode, success, message, data = null) => {
  return res.status(statusCode).json({
    success,
    message,
    ...(data && { data })
  });
};

/**
 * Set or update budget for a user
 * POST /setBudget
 */
const setBudget = async (req, res) => {
  try {
    const { monthlyLimit, spentAmount = 0 } = req.body;
    const userId = req.user.uid;

    // Validate budget data
    const validation = validateBudget({ monthlyLimit });
    if (!validation.isValid) {
      return apiResponse(res, 400, false, validation.errors.join(', '));
    }

    // Sanitize budget data
    const budgetData = sanitizeBudget({
      monthlyLimit,
      spentAmount,
      remainingAmount: monthlyLimit - spentAmount
    });

    // Check if budget already exists for user
    const existingSnapshot = await db.collection('budgets')
      .where('userId', '==', userId)
      .limit(1)
      .get();

    let budgetDoc;
    let isUpdate = false;

    if (!existingSnapshot.empty) {
      // Update existing budget
      budgetDoc = existingSnapshot.docs[0];
      isUpdate = true;
      
      // Recalculate based on new limit
      const currentSpent = budgetDoc.data().spentAmount || 0;
      budgetData.spentAmount = currentSpent;
      budgetData.remainingAmount = budgetData.monthlyLimit - currentSpent;
      budgetData.limitExceeded = currentSpent > budgetData.monthlyLimit;
    }

    // Prepare budget document
    const budgetDocData = {
      userId,
      monthlyLimit: budgetData.monthlyLimit,
      spentAmount: budgetData.spentAmount,
      remainingAmount: budgetData.remainingAmount,
      limitExceeded: budgetData.limitExceeded,
      updatedAt: new Date().toISOString(),
      ...(isUpdate ? {} : { createdAt: new Date().toISOString() })
    };

    // Store in Firestore
    if (isUpdate) {
      await db.collection('budgets').doc(budgetDoc.id).update(budgetDocData);
    } else {
      await db.collection('budgets').add(budgetDocData);
    }

    return apiResponse(res, isUpdate ? 200 : 201, true, 
      isUpdate ? 'Budget updated successfully' : 'Budget set successfully',
      budgetDocData
    );
  } catch (error) {
    console.error('Error setting budget:', error);
    return apiResponse(res, 500, false, `Server error: ${error.message}`);
  }
};

/**
 * Get budget for a user
 * GET /getBudget
 */
const getBudget = async (req, res) => {
  try {
    const userId = req.user.uid;

    const snapshot = await db.collection('budgets')
      .where('userId', '==', userId)
      .limit(1)
      .get();

    if (snapshot.empty) {
      return apiResponse(res, 200, true, 'No budget found', {
        monthlyLimit: 0,
        spentAmount: 0,
        remainingAmount: 0,
        limitExceeded: false
      });
    }

    const budgetData = snapshot.docs[0].data();

    return apiResponse(res, 200, true, 'Budget retrieved successfully', budgetData);
  } catch (error) {
    console.error('Error getting budget:', error);
    return apiResponse(res, 500, false, `Server error: ${error.message}`);
  }
};

/**
 * Reset budget spent amount (monthly)
 * POST /resetBudget
 */
const resetBudget = async (req, res) => {
  try {
    const userId = req.user.uid;

    const snapshot = await db.collection('budgets')
      .where('userId', '==', userId)
      .limit(1)
      .get();

    if (snapshot.empty) {
      return apiResponse(res, 404, false, 'No budget found to reset');
    }

    const budgetDoc = snapshot.docs[0];
    const budgetData = budgetDoc.data();

    await db.collection('budgets').doc(budgetDoc.id).update({
      spentAmount: 0,
      remainingAmount: budgetData.monthlyLimit,
      limitExceeded: false,
      updatedAt: new Date().toISOString(),
      lastResetAt: new Date().toISOString()
    });

    return apiResponse(res, 200, true, 'Budget reset successfully');
  } catch (error) {
    console.error('Error resetting budget:', error);
    return apiResponse(res, 500, false, `Server error: ${error.message}`);
  }
};

/**
 * Update spent amount (called internally by transaction controller)
 * PUT /updateSpent
 */
const updateSpent = async (req, res) => {
  try {
    const { amount, operation = 'add' } = req.body;
    const userId = req.user.uid;

    if (!amount || isNaN(parseFloat(amount))) {
      return apiResponse(res, 400, false, 'Valid amount is required');
    }

    const snapshot = await db.collection('budgets')
      .where('userId', '==', userId)
      .limit(1)
      .get();

    if (snapshot.empty) {
      return apiResponse(res, 404, false, 'No budget found');
    }

    const budgetDoc = snapshot.docs[0];
    const budgetData = budgetDoc.data();

    let newSpentAmount;
    if (operation === 'add') {
      newSpentAmount = (budgetData.spentAmount || 0) + parseFloat(amount);
    } else if (operation === 'subtract') {
      newSpentAmount = Math.max(0, (budgetData.spentAmount || 0) - parseFloat(amount));
    } else {
      newSpentAmount = parseFloat(amount);
    }

    const newRemainingAmount = (budgetData.monthlyLimit || 0) - newSpentAmount;
    const limitExceeded = newSpentAmount > (budgetData.monthlyLimit || 0);

    await db.collection('budgets').doc(budgetDoc.id).update({
      spentAmount: newSpentAmount,
      remainingAmount: newRemainingAmount,
      limitExceeded,
      updatedAt: new Date().toISOString()
    });

    return apiResponse(res, 200, true, 'Budget updated successfully', {
      spentAmount: newSpentAmount,
      remainingAmount: newRemainingAmount,
      limitExceeded
    });
  } catch (error) {
    console.error('Error updating spent:', error);
    return apiResponse(res, 500, false, `Server error: ${error.message}`);
  }
};

module.exports = {
  setBudget,
  getBudget,
  resetBudget,
  updateSpent
};
