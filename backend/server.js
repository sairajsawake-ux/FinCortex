const express = require("express");
const cors = require("cors");

const app = express();
const PORT = 5000;

app.use(cors());
app.use(express.json());

// 🔹 ADD THIS
const authRoutes = require("./routes/auth");
app.use("/auth", authRoutes);

const expenseRoutes = require("./routes/expense");
app.use("/expense", expenseRoutes);

app.get("/", (req, res) => {
  res.send("FinCortex Backend is running");
});

app.listen(PORT, () => {
  console.log(`Server running on port ${PORT}`);
});
