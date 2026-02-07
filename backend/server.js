const express = require("express");
const cors = require("cors");
require("dotenv").config();

const app = express();
const PORT = process.env.PORT || 5000;

// Middleware
app.use(cors());
app.use(express.json());

// Routes (ONLY files that actually exist)
const authRoutes = require("./routes/auth");
const expenseRoutes = require("./routes/expense");
const chatRoutes = require("./routes/chat.routes");
const aiRoutes = require("./routes/ai");

// Route mounting
app.use("/auth", authRoutes);
app.use("/expense", expenseRoutes);
app.use("/api", chatRoutes);
app.use("/ai", aiRoutes);

// Health check
app.get("/", (req, res) => {
  res.send("FinCortex Backend is running");
});

// Start server
app.listen(PORT, () => {
  console.log(`Server running on port ${PORT}`);
});
