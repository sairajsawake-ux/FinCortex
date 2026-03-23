const express = require("express");
const router = express.Router();
const { setBudget, getBudget, resetBudget } = require("../controllers/budgetController");
const authMiddleware = require("../middleware/authMiddleware");

// Apply auth middleware to all routes
router.use(authMiddleware);

// Budget Routes
router.post("/setBudget", setBudget);
router.get("/getBudget", getBudget);
router.post("/resetBudget", resetBudget);

module.exports = router;
