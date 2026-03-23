const express = require("express");
const cors = require("cors");
const multer = require("multer");
require("dotenv").config();

const app = express();
const PORT = process.env.PORT || 5000;

// Middleware
app.use(cors());
app.use(express.json());
app.use(express.urlencoded({ extended: true }));

// ====================
// API Response Format
// ====================
const apiResponse = (res, statusCode, success, message, data = null) => {
  return res.status(statusCode).json({
    success,
    message,
    ...(data && { data })
  });
};

// ====================
// Route Groups
// ====================

// Auth Routes
const authRoutes = require("./routes/auth");

// Transaction Routes (SMS parsing, CSV upload)
const transactionRoutes = require("./routes/transaction");

// Budget Routes
const budgetRoutes = require("./routes/budget");

// Legacy Expense Routes (maintained for compatibility)
const expenseRoutes = require("./routes/expense");

// AI & Chat Routes
const chatRoutes = require("./routes/chat.routes");
const aiRoutes = require("./routes/ai");

// ====================
// Route Mounting
// ====================

// Auth Routes - /auth/*
app.use("/auth", authRoutes);

// Transaction Routes - /transaction/*
app.use("/transaction", transactionRoutes);

// Budget Routes - /budget/*
app.use("/budget", budgetRoutes);

// Legacy Expense Routes - /expense/* (maintained for compatibility)
app.use("/expense", expenseRoutes);

// AI & Chat Routes - /api/* and /ai/*
app.use("/api", chatRoutes);
app.use("/ai", aiRoutes);

// ====================
// Health Check
// ====================
app.get("/", (req, res) => {
  apiResponse(res, 200, true, "FinCortex Backend is running", {
    version: "1.0.0",
    endpoints: {
      auth: "/auth",
      transaction: "/transaction",
      budget: "/budget",
      expense: "/expense",
      ai: "/ai",
      chat: "/api"
    }
  });
});

// ====================
// 404 Handler
// ====================
app.use((req, res) => {
  apiResponse(res, 404, false, "Endpoint not found");
});

// ====================
// Error Handler
// ====================
app.use((err, req, res, next) => {
  console.error('Server Error:', err);
  
  // Handle multer errors
  if (err instanceof multer.MulterError) {
    if (err.code === 'LIMIT_FILE_SIZE') {
      return apiResponse(res, 400, false, 'File size exceeds 10MB limit');
    }
    return apiResponse(res, 400, false, `File upload error: ${err.message}`);
  }
  
  if (err.message === 'Only CSV files are allowed') {
    return apiResponse(res, 400, false, err.message);
  }

  apiResponse(res, 500, false, "Internal server error");
});

// Start server
app.listen(PORT, () => {
  console.log(`Server running on port ${PORT}`);
});
