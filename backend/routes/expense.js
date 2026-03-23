const express = require("express");
const router = express.Router();
const { addExpense, getAllExpenses } = require("../controllers/expenseController");
const authMiddleware = require("../middleware/authMiddleware");

// Apply auth middleware to all expense routes
router.use(authMiddleware);

// Route: POST /expense/add
router.post("/add", addExpense);

// Route: GET /expense/
router.get("/", getAllExpenses);

module.exports = router;
