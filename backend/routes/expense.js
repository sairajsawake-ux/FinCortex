const express = require("express");
const router = express.Router();
const { addExpense, getAllExpenses } = require("../controllers/expenseController");

// Handle potential import issues (named vs default export)
const authMiddlewareImport = require("../middleware/authMiddleware");
const authMiddleware = typeof authMiddlewareImport === 'function' 
  ? authMiddlewareImport 
  : (authMiddlewareImport.authMiddleware || ((req, res, next) => next()));

router.post("/add", authMiddleware, addExpense);
router.get("/", authMiddleware, getAllExpenses);

module.exports = router;
