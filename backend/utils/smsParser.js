/**
 * SMS Parsing Engine
 * Extracts transaction details from SMS messages
 */

// OTP and non-transaction SMS patterns to ignore
const IGNORE_PATTERNS = [
  /otp/i,
  /one time password/i,
  /verification code/i,
  / login otp/i,
  /register otp/i,
  /transaction otp/i,
  /password/i,
  /reset password/i,
  /forgot password/i,
  /welcome/i,
  /thank you for registering/i,
  /account activated/i,
  /kyc/i,
  /upi set up/i,
  /new device/i,
  /login alert/i,
  /suspicious activity/i
];

// Debit keywords
const DEBIT_KEYWORDS = [
  'debited', 'debited from', 'paid', 'payment', 'purchase',
  'bought', 'spent', 'withdrawn', 'transfer debited',
  'sent', 'failed', 'declined', 'insufficient'
];

// Credit keywords
const CREDIT_KEYWORDS = [
  'credited', 'credited to', 'received', 'deposit', 'refund',
  'cashback', 'reward', 'reimbursement', 'salary', 'interest',
  'credited via', 'money received', 'transfer received'
];

// Amount patterns for different formats
const AMOUNT_PATTERNS = [
  /Rs\.?\s*(\d+(?:,\d{3})*(?:\.\d{2})?)/i,           // Rs.1000 or Rs.1,000.00
  /INR\s*(\d+(?:,\d{3})*(?:\.\d{2})?)/i,             // INR1000 or INR1,000.00
  /₹(\d+(?:,\d{3})*(?:\.\d{2})?)/i,                  // ₹1000 or ₹1,000.00
  /amount\s*:?\s*Rs?\.?\s*(\d+(?:,\d{3})*(?:\.\d{2})?)/i, // Amount: Rs.1000
  /rs\.?\s*(\d+(?:,\d{3})*(?:\.\d{2})?)/i,           // rs1000
  /(\d+(?:,\d{3})*(?:\.\d{2})?)\s*debited/i,          // 1000 debited
  /(\d+(?:,\d{3})*(?:\.\d{2})?)\s*credited/i          // 1000 credited
];

/**
 * Check if SMS should be ignored (OTP or non-transaction)
 * @param {string} sms - SMS message text
 * @returns {boolean} - True if should be ignored
 */
const shouldIgnoreSMS = (sms) => {
  if (!sms || typeof sms !== 'string') {
    return true;
  }

  for (const pattern of IGNORE_PATTERNS) {
    if (pattern.test(sms)) {
      return true;
    }
  }

  return false;
};

/**
 * Extract amount from SMS using regex patterns
 * @param {string} sms - SMS message text
 * @returns {number|null} - Extracted amount or null
 */
const extractAmount = (sms) => {
  if (!sms) return null;

  for (const pattern of AMOUNT_PATTERNS) {
    const match = sms.match(pattern);
    if (match && match[1]) {
      // Remove commas and convert to number
      const amount = parseFloat(match[1].replace(/,/g, ''));
      if (!isNaN(amount) && amount > 0) {
        return amount;
      }
    }
  }

  return null;
};

/**
 * Detect transaction type (credit or debit)
 * @param {string} sms - SMS message text
 * @returns {string} - 'credit' or 'debit'
 */
const detectTransactionType = (sms) => {
  if (!sms) return 'debit';

  const lowerSMS = sms.toLowerCase();

  // Check for credit keywords first
  for (const keyword of CREDIT_KEYWORDS) {
    if (lowerSMS.includes(keyword)) {
      return 'credit';
    }
  }

  // Check for debit keywords
  for (const keyword of DEBIT_KEYWORDS) {
    if (lowerSMS.includes(keyword)) {
      return 'debit';
    }
  }

  // Default to debit for most SMS transactions
  return 'debit';
};

/**
 * Extract merchant name from SMS
 * @param {string} sms - SMS message text
 * @returns {string} - Extracted merchant name
 */
const extractMerchant = (sms) => {
  if (!sms) return '';

  let merchant = '';

  // Try to extract from common patterns
  // Pattern: "at/on MERCHANT" or "to MERCHANT"
  const atPatterns = [
    /at\s+([A-Za-z0-9\s]+?)(?:\s+on|\s+via|\s+used|\s+used|$)/i,
    /on\s+([A-Za-z0-9\s]+?)(?:\s+via|\s+used|\s+used|$)/i,
    /to\s+([A-Za-z0-9\s]+?)(?:\s+via|\s+used|$)/i,
    /from\s+([A-Za-z0-9\s]+?)(?:\s+via|\s+used|$)/i,
    /merchant\s*:\s*([A-Za-z0-9\s]+)/i,
    /merchant\s+([A-Za-z0-9\s]+)/i
  ];

  for (const pattern of atPatterns) {
    const match = sms.match(pattern);
    if (match && match[1]) {
      merchant = match[1].trim();
      // Clean up the merchant name
      merchant = merchant.replace(/\s+/g, ' ').trim();
      if (merchant.length >= 2 && merchant.length <= 50) {
        break;
      }
    }
  }

  // If no merchant found, try to get first few words as merchant
  if (!merchant) {
    const words = sms.split(/\s+/).slice(0, 4).join(' ');
    merchant = words.replace(/[^a-zA-Z0-9\s]/g, '').trim();
  }

  return merchant.substring(0, 50); // Limit length
};

/**
 * Extract date from SMS
 * @param {string} sms - SMS message text
 * @returns {string} - ISO date string
 */
const extractDate = (sms) => {
  // Try to find date patterns in SMS
  const datePatterns = [
    /(\d{1,2})-(\d{1,2})-(\d{4})/,  // DD-MM-YYYY
    /(\d{1,2})\/(\d{1,2})\/(\d{4})/, // DD/MM/YYYY
    /(\d{1,2})-(\d{1,2})-(\d{2})/,   // DD-MM-YY
    /on\s+(\d{1,2})\s+(jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec)[a-z]*\s+(\d{4})/i
  ];

  for (const pattern of datePatterns) {
    const match = sms.match(pattern);
    if (match) {
      try {
        const dateStr = match[0];
        const date = new Date(dateStr);
        if (!isNaN(date.getTime())) {
          return date.toISOString();
        }
      } catch (e) {
        // Continue to next pattern
      }
    }
  }

  // Default to current date if no date found
  return new Date().toISOString();
};

/**
 * Parse SMS and extract transaction details
 * @param {string} sms - SMS message text
 * @returns {Object|null} - Parsed transaction or null if invalid
 */
const parseSMS = (sms) => {
  // Check if SMS should be ignored
  if (shouldIgnoreSMS(sms)) {
    return null;
  }

  // Extract amount
  const amount = extractAmount(sms);
  if (!amount || amount <= 0) {
    return null;
  }

  // Detect transaction type
  const type = detectTransactionType(sms);

  // Extract merchant
  const merchant = extractMerchant(sms);

  // Extract date
  const date = extractDate(sms);

  return {
    amount,
    type,
    merchant,
    description: sms,
    date,
    isFromSMS: true
  };
};

/**
 * Check for potential duplicate transaction
 * @param {Object} parsedData - Parsed SMS data
 * @param {Object} existingTransactions - Array of existing transactions
 * @returns {boolean} - True if duplicate
 */
const isDuplicateTransaction = (parsedData, existingTransactions, userId) => {
  if (!parsedData || !existingTransactions || existingTransactions.length === 0) {
    return false;
  }

  const parsedDate = new Date(parsedData.date).toDateString();
  const parsedAmount = parseFloat(parsedData.amount);
  const parsedMerchant = (parsedData.merchant || '').toLowerCase();

  return existingTransactions.some(transaction => {
    // Check same user
    if (transaction.userId !== userId) return false;
    
    // Check same amount
    if (parseFloat(transaction.amount) !== parsedAmount) return false;

    // Check same merchant (partial match)
    const transactionMerchant = (transaction.merchant || '').toLowerCase();
    if (!transactionMerchant.includes(parsedMerchant) && 
        !parsedMerchant.includes(transactionMerchant)) {
      return false;
    }

    // Check same date
    const transactionDate = new Date(transaction.createdAt || transaction.date).toDateString();
    if (transactionDate !== parsedDate) return false;

    return true;
  });
};

module.exports = {
  parseSMS,
  extractAmount,
  detectTransactionType,
  extractMerchant,
  extractDate,
  shouldIgnoreSMS,
  isDuplicateTransaction,
  DEBIT_KEYWORDS,
  CREDIT_KEYWORDS
};
