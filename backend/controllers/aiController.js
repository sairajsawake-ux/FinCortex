const { expenses } = require("./expenseController");


exports.chat = (req, res) => {
  const userId = req.userId;
  const userMessage = req.body.message;

  const userExpenses = expenses.filter(
    expense => expense.userId == userId
  );

  let totalSpent = 0;
  userExpenses.forEach(exp => {
    totalSpent += Number(exp.amount);
  });

  let reply = "";

  if (userExpenses.length === 0) {
    reply = "You have not added any expenses yet.";
  } else if (userMessage.toLowerCase().includes("spend")) {
    reply = `You have spent ₹${totalSpent} in total so far.`;
  } else if (userMessage.toLowerCase().includes("save")) {
    reply = "Try reducing unnecessary food and shopping expenses to save more money.";
  } else {
    reply = "I can help you analyze your expenses and savings.";
  }

  res.json({ reply });
};
