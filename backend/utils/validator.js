/**
 * Data Validation Utilities
 * Validates transaction and budget data before storing
 */

// Validate transaction data
const validateTransaction = (data) => {
  const errors = [];

  // Check required fields
  if (!data.amount && data.amount !== 0) {
    errors.push("Amount is required");
  }

  // Validate amount is numeric
  if (data.amount && (isNaN(parseFloat(data.amount)) || !isFinite(data.amount))) {
    errors.push("Amount must be a valid number");
  }

  // Check type (credit/debit)
  if (data.type && !['credit', 'debit'].includes(data.type.toLowerCase())) {
    errors.push("Type must be either 'credit' or 'debit'");
  }

  // Check category
  const validCategories = ['Food', 'Travel', 'Shopping', 'Bills', 'Medical', 'Others'];
  if (data.category && !validCategories.includes(data.category)) {
    errors.push(`Category must be one of: ${validCategories.join(', ')}`);
  }

  return {
    isValid: errors.length === 0,
    errors
  };
};

// Validate budget data
const validateBudget = (data) => {
  const errors = [];

  // Check required fields
  if (!data.monthlyLimit && data.monthlyLimit !== 0) {
    errors.push("Monthly limit is required");
  }

  // Validate monthlyLimit is numeric
  if (data.monthlyLimit && (isNaN(parseFloat(data.monthlyLimit)) || !isFinite(data.monthlyLimit))) {
    errors.push("Monthly limit must be a valid number");
  }

  // Validate monthlyLimit is positive
  if (data.monthlyLimit && parseFloat(data.monthlyLimit) <= 0) {
    errors.push("Monthly limit must be greater than 0");
  }

  return {
    isValid: errors.length === 0,
    errors
  };
};

// Sanitize transaction data
const sanitizeTransaction = (data) => {
  return {
    amount: parseFloat(data.amount) || 0,
    category: data.category || 'Others',
    merchant: data.merchant || data.description || '',
    type: data.type ? data.type.toLowerCase() : 'debit',
    description: data.description || data.merchant || '',
    date: data.date || data.createdAt || new Date().toISOString(),
    isFromSMS: data.isFromSMS || false
  };
};

// Sanitize budget data
const sanitizeBudget = (data) => {
  return {
    monthlyLimit: parseFloat(data.monthlyLimit) || 0,
    spentAmount: parseFloat(data.spentAmount) || 0,
    remainingAmount: parseFloat(data.remainingAmount) || parseFloat(data.monthlyLimit) || 0,
    limitExceeded: data.limitExceeded || false
  };
};

module.exports = {
  validateTransaction,
  validateBudget,
  sanitizeTransaction,
  sanitizeBudget
};
