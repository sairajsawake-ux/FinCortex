/**
 * Category Mapping Logic
 * Dynamically assigns categories based on keywords
 */

// Category to keywords mapping
const categoryKeywords = {
  Food: [
    'zomato', 'swiggy', 'foodpanda', 'dominos', 'pizza hut', 'mcdonalds', 
    'kfc', 'burger king', 'restaurant', 'cafe', 'coffee', 'tea', 
    'hotel', 'kitchen', 'diner', 'eatery', 'bakery', 'sweet', 
    'ice cream', 'juice', 'lunch', 'dinner', 'breakfast', 'meal',
    'uber eats', 'grubhub', 'doordash'
  ],
  Travel: [
    'uber', 'ola', 'lyft', 'taxi', 'cab', 'auto', 'rickshaw',
    'airline', 'flight', 'air india', 'indigo', 'spicejet', 
    'railway', 'train', 'irctc', 'bus', 'metro', ' subway',
    'booking', 'makeMyTrip', 'goibibo', 'yatra', 'travel',
    'fuel', 'petrol', 'diesel', 'gas', 'shell', 'bp', 'hp',
    'parking', 'toll', 'highway'
  ],
  Shopping: [
    'amazon', 'flipkart', 'myntra', 'snapdeal', 'ebay',
    'shopping', 'store', 'market', 'mall', 'retail', 'cloth',
    'clothing', 'fashion', 'shoes', 'watch', 'jewelry', 'bag',
    'electronics', 'mobile', 'laptop', 'computer', 'tv',
    'furniture', 'home', 'decor', 'interior'
  ],
  Bills: [
    'electricity', 'bill', 'power', 'water', 'gas', 'phone',
    'recharge', 'broadband', 'wifi', 'internet', 'cable',
    'insurance', 'premium', 'rent', 'maintenance', 'society',
    'credit card', 'emi', 'loan', 'interest', 'bank charge',
    'subscription', 'netflix', 'spotify', 'amazon prime',
    'disney', 'hotstar', 'youtube', 'google'
  ],
  Medical: [
    'hospital', 'clinic', 'doctor', 'medicine', 'pharmacy',
    'medical', 'health', 'diagnostic', 'lab', 'test', 'scan',
    'treatment', 'surgery', 'consultation', 'prescription',
    'healthcare', 'medical store', 'apollo', 'fortis'
  ],
  Others: []
};

/**
 * Get category based on merchant name or description
 * @param {string} merchant - Merchant name or transaction description
 * @returns {string} - Category name
 */
const getCategory = (merchant = '') => {
  if (!merchant || typeof merchant !== 'string') {
    return 'Others';
  }

  const lowerMerchant = merchant.toLowerCase();

  // Check each category's keywords
  for (const [category, keywords] of Object.entries(categoryKeywords)) {
    if (category === 'Others') continue; // Skip default category
    
    for (const keyword of keywords) {
      if (lowerMerchant.includes(keyword.toLowerCase())) {
        return category;
      }
    }
  }

  return 'Others';
};

/**
 * Get all available categories
 * @returns {string[]} - Array of category names
 */
const getCategories = () => {
  return Object.keys(categoryKeywords);
};

/**
 * Add custom keyword to a category
 * @param {string} category - Category name
 * @param {string|string[]} keywords - Keywords to add
 */
const addKeywordToCategory = (category, keywords) => {
  if (!categoryKeywords[category]) {
    categoryKeywords[category] = [];
  }
  
  if (Array.isArray(keywords)) {
    categoryKeywords[category].push(...keywords);
  } else {
    categoryKeywords[category].push(keywords);
  }
};

module.exports = {
  getCategory,
  getCategories,
  addKeywordToCategory,
  categoryKeywords
};
