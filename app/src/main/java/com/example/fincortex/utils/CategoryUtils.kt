package com.example.fincortex.utils

/**
 * Utility for auto-categorizing transactions based on merchant name keywords.
 * Mirrors the backend categorization.js logic.
 */
object CategoryUtils {

    private val categoryKeywords = mapOf(
        "Food" to listOf(
            "zomato", "swiggy", "foodpanda", "dominos", "pizza hut", "mcdonalds",
            "kfc", "burger king", "restaurant", "cafe", "coffee", "tea",
            "hotel", "kitchen", "diner", "eatery", "bakery", "sweet",
            "ice cream", "juice", "lunch", "dinner", "breakfast", "meal", "uber eats"
        ),
        "Travel" to listOf(
            "uber", "ola", "lyft", "taxi", "cab", "auto", "rickshaw",
            "airline", "flight", "air india", "indigo", "spicejet",
            "railway", "train", "irctc", "bus", "metro", "subway",
            "booking", "makemytrip", "goibibo", "yatra", "travel",
            "fuel", "petrol", "diesel", "shell", "parking", "toll", "highway"
        ),
        "Shopping" to listOf(
            "amazon", "flipkart", "myntra", "snapdeal", "ebay",
            "shopping", "store", "market", "mall", "retail", "cloth",
            "clothing", "fashion", "shoes", "watch", "jewelry", "bag",
            "electronics", "mobile", "laptop", "computer", "tv",
            "furniture", "home", "decor"
        ),
        "Bills" to listOf(
            "electricity", "bill", "power", "water", "phone",
            "recharge", "broadband", "wifi", "internet", "cable",
            "insurance", "premium", "rent", "maintenance",
            "credit card", "emi", "loan", "interest",
            "subscription", "netflix", "spotify", "amazon prime",
            "disney", "hotstar", "youtube", "google"
        ),
        "Medical" to listOf(
            "hospital", "clinic", "doctor", "medicine", "pharmacy",
            "medical", "health", "diagnostic", "lab", "test", "scan",
            "treatment", "surgery", "consultation", "prescription",
            "healthcare", "apollo", "fortis"
        )
    )

    /** Returns category for a given merchant name. Defaults to "Others". */
    fun getCategory(merchant: String): String {
        if (merchant.isBlank()) return "Others"
        val lowerMerchant = merchant.lowercase()
        for ((category, keywords) in categoryKeywords) {
            if (keywords.any { lowerMerchant.contains(it) }) return category
        }
        return "Others"
    }

    /** Returns all available category names. */
    fun getAllCategories(): List<String> = categoryKeywords.keys.toList() + "Others"
}
