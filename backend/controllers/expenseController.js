const expenses = []; // temporary storage

exports.addExpense = (req, res) => {
  const { title, amount, category } = req.body;

  if (!title || !amount || !category) {
    return res.status(400).json({ message: "All fields required" });
  }

  const expense = {
    id: expenses.length + 1,
    title,
    amount,
    category,
    date: new Date()
  };

  expenses.push(expense);
  res.json({ message: "Expense added", expense });
};

exports.getExpenses = (req, res) => {
  res.json(expenses);
};
