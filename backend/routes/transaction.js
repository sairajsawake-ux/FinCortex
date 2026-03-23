const express = require("express");
const router = express.Router();
const multer = require('multer');
const path = require('path');
const { addTransaction, getTransactions, uploadStatement } = require("../controllers/transactionController");
const authMiddleware = require("../middleware/authMiddleware");

// Configure multer for file uploads
const storage = multer.diskStorage({
  destination: (req, file, cb) => {
    cb(null, 'uploads/');
  },
  filename: (req, file, cb) => {
    cb(null, `statement_${Date.now()}${path.extname(file.originalname)}`);
  }
});

// File filter to only accept CSV
const fileFilter = (req, file, cb) => {
  if (file.mimetype === 'text/csv' || 
      file.mimetype === 'application/vnd.ms-excel' ||
      file.originalname.endsWith('.csv')) {
    cb(null, true);
  } else {
    cb(new Error('Only CSV files are allowed'), false);
  }
};

const upload = multer({ 
  storage,
  fileFilter,
  limits: { fileSize: 10 * 1024 * 1024 } // 10MB limit
});

// Ensure uploads directory exists
const fs = require('fs');
if (!fs.existsSync('uploads')) {
  fs.mkdirSync('uploads');
}

// Apply auth middleware to all routes
router.use(authMiddleware);

// Transaction Routes
router.post("/addTransaction", addTransaction);
router.get("/getTransactions", getTransactions);

// Statement Upload Route
router.post("/uploadStatement", upload.single('file'), uploadStatement);

module.exports = router;
