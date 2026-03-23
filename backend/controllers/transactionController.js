/**
 * Transaction Controller
 * Handles transaction operations, SMS parsing, and CSV statement upload
 */

const admin = require('../firebase/firebaseAdmin');
const db = admin.firestore();
const { validateTransaction, sanitizeTransaction } = require('../utils/validator');
const { getCategory } = require('../utils/categorization');
const { parseSMS, isDuplicateTransaction } = require('../utils/smsParser');
const csv = require('csv-parser');
const fs = require('fs');

// Standard API response format
const apiResponse = (res, statusCode, success, message, data = null) => {
  return res.status(statusCode).json({
    success,
    message,
    ...(data && { data })
  });
};

/**
 * Add a new transaction
 * POST /addTransaction
 */
const addTransaction = async (req, res) => {
  try {
    const { amount, category, merchant, type, description, date, smsContent } = req.body;
    const userId = req.user.uid;

    // Handle SMS parsing if smsContent is provided
    if (smsContent) {
      const parsedSMS = parseSMS(smsContent);
      if (!parsedSMS) {
        return apiResponse(res, 400, false, 'Invalid or non-transaction SMS');
      }

      // Use parsed data, allowing override from request body
      Object.assign(req.body, {
        amount: parsedSMS.amount,
        type: parsedSMS.type,
        merchant: parsedSMS.merchant,
        description: parsedSMS.description,
        date: parsedSMS.date,
        isFromSMS: true
      });
    }

    // Validate transaction data
    const validation = validateTransaction(req.body);
    if (!validation.isValid) {
      return apiResponse(res, 400, false, validation.errors.join(', '));
    }

    // Sanitize transaction data
    const transactionData = sanitizeTransaction(req.body);

    // Assign category dynamically if not provided
    const finalCategory = transactionData.category === 'Others' 
      ? getCategory(transactionData.merchant || transactionData.description)
      : transactionData.category;

    // Check for duplicate transaction
    const existingSnapshot = await db.collection('transactions')
      .where('userId', '==', userId)
      .get();

    const existingTransactions = [];
    existingSnapshot.forEach(doc => {
      existingTransactions.push({ id: doc.id, ...doc.data() });
    });

    if (isDuplicateTransaction(transactionData, existingTransactions, userId)) {
      return apiResponse(res, 409, false, 'Duplicate transaction detected');
    }

    // Create transaction document
    const transactionDoc = {
      transactionID: `TXN_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`,
      userId,
      amount: transactionData.amount,
      category: finalCategory,
      merchant: transactionData.merchant,
      type: transactionData.type,
      description: transactionData.description,
      createdAt: transactionData.date,
      updatedAt: new Date().toISOString(),
      isFromSMS: transactionData.isFromSMS || false
    };

    // Store in Firestore
    const docRef = await db.collection('transactions').add(transactionDoc);

    // Auto-update budget if it's a debit transaction
    if (transactionData.type === 'debit') {
      await updateBudgetSpent(userId, transactionData.amount);
    }

    return apiResponse(res, 201, true, 'Transaction added successfully', {
      id: docRef.id,
      ...transactionDoc
    });
  } catch (error) {
    console.error('Error adding transaction:', error);
    return apiResponse(res, 500, false, `Server error: ${error.message}`);
  }
};

/**
 * Get all transactions for a user
 * GET /getTransactions
 */
const getTransactions = async (req, res) => {
  try {
    const userId = req.user.uid;
    const { category, startDate, endDate, type } = req.query;

    let query = db.collection('transactions').where('userId', '==', userId);

    // Apply filters
    if (category) {
      query = query.where('category', '==', category);
    }
    if (type) {
      query = query.where('type', '==', type);
    }

    const snapshot = await query.orderBy('createdAt', 'desc').get();

    let transactions = [];
    snapshot.forEach(doc => {
      transactions.push({ id: doc.id, ...doc.data() });
    });

    // Filter by date range if provided
    if (startDate || endDate) {
      transactions = transactions.filter(t => {
        const txnDate = new Date(t.createdAt);
        if (startDate && txnDate < new Date(startDate)) return false;
        if (endDate && txnDate > new Date(endDate)) return false;
        return true;
      });
    }

    return apiResponse(res, 200, true, 'Transactions retrieved successfully', {
      transactions,
      count: transactions.length
    });
  } catch (error) {
    console.error('Error getting transactions:', error);
    return apiResponse(res, 500, false, `Server error: ${error.message}`);
  }
};

/**
 * Upload bank statement (CSV)
 * POST /uploadStatement
 */
const uploadStatement = async (req, res) => {
  try {
    if (!req.file) {
      return apiResponse(res, 400, false, 'No file uploaded');
    }

    const userId = req.user.uid;
    const results = [];
    const errors = [];
    let addedCount = 0;
    let skippedCount = 0;

    // Read and parse CSV file
    await new Promise((resolve, reject) => {
      fs.createReadStream(req.file.path)
        .pipe(csv())
        .on('data', (row) => {
          results.push(row);
        })
        .on('end', resolve)
        .on('error', reject);
    });

    // Get existing transactions for duplicate check
    const existingSnapshot = await db.collection('transactions')
      .where('userId', '==', userId)
      .get();

    const existingTransactions = [];
    existingSnapshot.forEach(doc => {
      existingTransactions.push({ id: doc.id, ...doc.data() });
    });

    // Process each row
    for (const row of results) {
      try {
        // Extract data from CSV row (common column names)
        const date = row.date || row.Date || row.DATE || row['Transaction Date'] || new Date().toISOString();
        const description = row.description || row.Description || row.DESC || row.narration || row.Narration || '';
        const debit = row.debit || row.Debit || row.DEBIT || row.withdrawal || row.Withdrawal || '0';
        const credit = row.credit || row.Credit || row.CREDIT || row.deposit || row.Deposit || '0';
        const amount = row.amount || row.Amount || row.AMOUNT || 
          (debit && debit !== '0' ? debit : credit) || '0';

        // Skip empty rows
        if (!amount || amount === '0' || amount === 0) {
          errors.push({ row, error: 'No valid amount' });
          continue;
        }

        // Determine type and final amount
        const isDebit = debit && debit !== '0' && parseFloat(debit) > 0;
        const finalAmount = isDebit ? parseFloat(debit) : parseFloat(credit);
        const type = isDebit ? 'debit' : 'credit';

        // Get category
        const category = getCategory(description);

        // Check for duplicate
        const transactionData = {
          amount: finalAmount,
          type,
          merchant: description,
          description,
          date
        };

        if (isDuplicateTransaction(transactionData, existingTransactions, userId)) {
          skippedCount++;
          continue;
        }

        // Create transaction document
        const transactionDoc = {
          transactionID: `TXN_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`,
          userId,
          amount: finalAmount,
          category,
          merchant: description.substring(0, 100),
          type,
          description,
          createdAt: new Date(date).toISOString(),
          updatedAt: new Date().toISOString(),
          isFromSMS: false,
          isFromStatement: true
        };

        // Store in Firestore
        await db.collection('transactions').add(transactionDoc);
        existingTransactions.push(transactionDoc); // Add to check for subsequent duplicates
        addedCount++;

        // Auto-update budget if it's a debit transaction
        if (type === 'debit') {
          await updateBudgetSpent(userId, finalAmount);
        }
      } catch (rowError) {
        errors.push({ row, error: rowError.message });
      }
    }

    // Clean up uploaded file
    fs.unlinkSync(req.file.path);

    return apiResponse(res, 200, true, 'Statement processed', {
      added: addedCount,
      skipped: skippedCount,
      errors: errors.length > 0 ? errors : null
    });
  } catch (error) {
    console.error('Error uploading statement:', error);
    // Clean up file if exists
    if (req.file && req.file.path) {
      try {
        fs.unlinkSync(req.file.path);
      } catch (e) {}
    }
    return apiResponse(res, 500, false, `Server error: ${error.message}`);
  }
};

/**
 * Update budget spent amount
 * @param {string} userId - User ID
 * @param {number} amount - Amount to add to spent
 */
const updateBudgetSpent = async (userId, amount) => {
  try {
    const budgetSnapshot = await db.collection('budgets')
      .where('userId', '==', userId)
      .limit(1)
      .get();

    if (budgetSnapshot.empty) return;

    const budgetDoc = budgetSnapshot.docs[0];
    const budgetData = budgetDoc.data();

    const newSpentAmount = (budgetData.spentAmount || 0) + parseFloat(amount);
    const newRemainingAmount = (budgetData.monthlyLimit || 0) - newSpentAmount;
    const limitExceeded = newSpentAmount > (budgetData.monthlyLimit || 0);

    await db.collection('budgets').doc(budgetDoc.id).update({
      spentAmount: newSpentAmount,
      remainingAmount: newRemainingAmount,
      limitExceeded,
      updatedAt: new Date().toISOString()
    });
  } catch (error) {
    console.error('Error updating budget:', error);
  }
};

module.exports = {
  addTransaction,
  getTransactions,
  uploadStatement
};
