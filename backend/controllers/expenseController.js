const admin = require('../firebase/firebaseAdmin');
const db = admin.firestore();
const { getCategory } = require('../utils/categorization');

// Standard API response format
const apiResponse = (res, statusCode, success, message, data = null) => {
  return res.status(statusCode).json({
    success,
    message,
    ...(data && { data })
  });
};

const addExpense = async (req, res) => {
  try {
    const { amount, category, title, type = 'debit', description = '' } = req.body;
    const userId = req.user.uid;

    // Validate required fields
    if (!amount) {
      return apiResponse(res, 400, false, "Amount is required");
    }

    // Validate amount is numeric
    if (isNaN(parseFloat(amount)) || !isFinite(amount)) {
      return apiResponse(res, 400, false, "Amount must be a valid number");
    }

    // Assign category dynamically if not provided
    const finalCategory = category || getCategory(description || title || '');

    const riskTag = Number(amount) > 5000 ? "HIGH_SPENDING" : "NORMAL";

    const newExpense = {
      userId,
      amount: parseFloat(amount),
      category: finalCategory,
      type: type.toLowerCase(),
      title: title || '',
      description: description || '',
      riskTag,
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString()
    };

    await db.collection('expenses').add(newExpense);

    return apiResponse(res, 201, true, "Expense added successfully", {
      id: newExpense.id,
      ...newExpense,
      aiInsight: `Expense tagged as ${riskTag}`
    });
  } catch (error) {
    console.error('Error adding expense:', error);
    return apiResponse(res, 500, false, `Server error: ${error.message}`);
  }
};

const getAllExpenses = async (req, res) => {
  try {
    const userId = req.user.uid;
    const { category, type } = req.query;
    
    let query = db.collection('expenses').where('userId', '==', userId);

    if (category) {
      query = query.where('category', '==', category);
    }
    if (type) {
      query = query.where('type', '==', type);
    }

    const snapshot = await query.orderBy('createdAt', 'desc').get();

    const expenses = [];
    snapshot.forEach(doc => {
      expenses.push({ id: doc.id, ...doc.data() });
    });

    return apiResponse(res, 200, true, "Expenses retrieved successfully", {
      expenses,
      count: expenses.length
    });
  } catch (error) {
    console.error('Error getting expenses:', error);
    return apiResponse(res, 500, false, `Server error: ${error.message}`);
  }
};

module.exports = { addExpense, getAllExpenses };
