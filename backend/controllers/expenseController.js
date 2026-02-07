const admin = require('../firebase/firebaseAdmin');
const db = admin.firestore();

const addExpense = async (req, res) => {
  try {
    const { amount, category, title } = req.body;
    const userId = req.user.uid;

    if (!amount || !category) {
      return res.status(400).json({ message: "Amount and category are required" });
    }

    const riskTag = Number(amount) > 5000 ? "HIGH_SPENDING" : "NORMAL";

    const newExpense = {
      userId,
      amount,
      category,
      title: title || '',
      riskTag,
      createdAt: new Date().toISOString()
    };

    await db.collection('expenses').add(newExpense);

    res.status(201).json({
      success: true,
      message: "Expense added successfully",
      aiInsight: `Expense tagged as ${riskTag}`
    });
  } catch (error) {
    res.status(500).json({ message: error.message });
  }
};

const getAllExpenses = async (req, res) => {
  try {
    const userId = req.user.uid;
    const snapshot = await db.collection('expenses').where('userId', '==', userId).get();

    const expenses = [];
    snapshot.forEach(doc => {
      expenses.push({ id: doc.id, ...doc.data() });
    });

    res.status(200).json(expenses);
  } catch (error) {
    res.status(500).json({ message: error.message });
  }
};

module.exports = { addExpense, getAllExpenses };
