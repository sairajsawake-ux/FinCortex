/**
 * AI/ML Simulation Module
 * This simulates intelligent decision making for user security.
 */

// AI Logic: Calculate User Risk Score based on heuristics
const calculateRiskScore = (password, email) => {
  let score = 0;

  // 1. Password Complexity Analysis (Simulating NLP/Pattern recognition)
  if (password.length < 8) score += 40; // Too short
  if (!/[A-Z]/.test(password)) score += 15; // No uppercase
  if (!/[0-9]/.test(password)) score += 15; // No numbers
  if (!/[!@#$%^&*]/.test(password)) score += 10; // No special chars

  // 2. Email Domain Reputation (Simulating Threat Intelligence)
  const suspiciousDomains = ['tempmail.com', '10minutemail.com'];
  const domain = email.split('@')[1];
  if (suspiciousDomains.includes(domain)) score += 50;

  // 3. AI Classification
  if (score >= 70) return "HIGH";
  if (score >= 30) return "MEDIUM";
  return "LOW";
};

// AI Logic: Generate Trust Score
const generateTrustScore = (riskLevel) => {
  if (riskLevel === "HIGH") return 20;
  if (riskLevel === "MEDIUM") return 65;
  return 95; // LOW risk means High trust
};

module.exports = { calculateRiskScore, generateTrustScore };