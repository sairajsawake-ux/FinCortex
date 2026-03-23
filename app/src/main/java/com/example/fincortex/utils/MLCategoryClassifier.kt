package com.example.fincortex.utils

/**
 * Scoring-based ML-style category classifier.
 *
 * Each keyword carries an explicit weight (1–10). For a given input text,
 * the score for every category is the sum of weights for all keywords that
 * appear in the text. The category with the highest total score wins.
 * Ties or zero-score inputs default to "Others".
 *
 * This intentionally extends [CategoryUtils] by adding an "Entertainment"
 * category and using weighted scoring instead of first-match logic.
 */
object MLCategoryClassifier {

    private data class Rule(val category: String, val keywords: List<Pair<String, Int>>)

    private val rules = listOf(
        Rule(
            "Food", listOf(
                "swiggy" to 10, "zomato" to 10, "foodpanda" to 10,
                "dominos" to 10, "pizza hut" to 10, "mcdonalds" to 10,
                "kfc" to 10, "burger king" to 10, "uber eats" to 10,
                "restaurant" to 7, "cafe" to 7, "bakery" to 7,
                "diner" to 7, "eatery" to 7, "ice cream" to 7,
                "coffee" to 6, "tea shop" to 6, "food" to 6,
                "kitchen" to 5, "hotel" to 5, "meal" to 5,
                "lunch" to 5, "dinner" to 5, "breakfast" to 5,
                "juice" to 5, "sweet" to 4
            )
        ),
        Rule(
            "Travel", listOf(
                "uber" to 10, "ola" to 10, "rapido" to 10,
                "irctc" to 10, "air india" to 10, "indigo" to 10,
                "spicejet" to 10, "makemytrip" to 10, "goibibo" to 10,
                "railway" to 9, "flight" to 9, "airline" to 9,
                "yatra" to 9, "petrol" to 8, "fuel" to 8,
                "diesel" to 8, "taxi" to 8, "cab" to 7,
                "metro" to 7, "parking" to 7, "toll" to 7,
                "bus" to 6, "highway" to 6, "travel" to 5,
                "auto" to 4, "rickshaw" to 4
            )
        ),
        Rule(
            "Shopping", listOf(
                "amazon" to 10, "flipkart" to 10, "myntra" to 10,
                "snapdeal" to 10, "ajio" to 10, "nykaa" to 10,
                "meesho" to 10, "ebay" to 9,
                "shopping" to 7, "clothing" to 7, "fashion" to 7,
                "apparel" to 7, "electronics" to 7, "laptop" to 7,
                "furniture" to 7, "shoes" to 6, "mobile" to 6,
                "mall" to 6, "retail" to 6, "store" to 5,
                "market" to 4, "watch" to 4
            )
        ),
        Rule(
            "Bills", listOf(
                "electricity" to 10, "bescom" to 10, "bses" to 10,
                "msedcl" to 10, "lpg" to 10, "indane" to 10,
                "water bill" to 10, "credit card" to 9, "emi" to 9,
                "recharge" to 9, "broadband" to 9, "insurance" to 9,
                "bsnl" to 8, "wifi" to 8, "gas" to 8,
                "cylinder" to 8, "loan" to 8, "rent" to 8,
                "internet" to 7, "premium" to 7, "maintenance" to 7,
                "tax" to 7, "airtel" to 6, "jio" to 6
            )
        ),
        Rule(
            "Entertainment", listOf(
                "netflix" to 10, "spotify" to 10, "hotstar" to 10,
                "disney" to 10, "zee5" to 10, "sonyliv" to 10,
                "amazon prime" to 10, "bookmyshow" to 10,
                "pvr" to 10, "inox" to 10, "youtube premium" to 10,
                "movie" to 9, "cinema" to 9, "concert" to 8,
                "gaming" to 8, "steam" to 8, "playstation" to 9,
                "xbox" to 9, "youtube" to 7, "event" to 6
            )
        ),
        Rule(
            "Medical", listOf(
                "hospital" to 10, "clinic" to 10, "pharmacy" to 10,
                "medplus" to 10, "1mg" to 10, "netmeds" to 10,
                "apollo" to 9, "fortis" to 9, "diagnostic" to 9,
                "pathology" to 9, "doctor" to 9, "medicine" to 9,
                "medical" to 8, "healthcare" to 7, "lab" to 6,
                "health" to 5, "treatment" to 5
            )
        ),
        Rule(
            "Investment", listOf(
                // Brokerage platforms
                "zerodha" to 10, "groww" to 10, "upstox" to 10,
                "kuvera" to 10, "paytm money" to 10, "angel broking" to 10,
                "icicidirect" to 10, "hdfc securities" to 10, "sbisec" to 10,
                "kotak securities" to 10, "5paisa" to 10, "motilal oswal" to 10,
                // Instruments
                "mutual fund" to 10, "sip" to 10, "elss" to 10,
                "equity" to 9, "stocks" to 9, "shares" to 9,
                "nse" to 9, "bse" to 9, "demat" to 9,
                "ipo" to 9, "nfo" to 9, "etf" to 9,
                "fixed deposit" to 9, "fd" to 8, "ppf" to 9,
                "nps" to 9, "epf" to 8, "provident fund" to 9,
                "bonds" to 8, "securities" to 8, "debenture" to 8,
                "gold bond" to 9, "sovereign" to 8, "sgb" to 9,
                "cryptocurrency" to 8, "crypto" to 8, "bitcoin" to 8,
                "dividend" to 7, "brokerage" to 8, "portfolio" to 7,
                "invest" to 5, "investment" to 5
            )
        )
    )

    /**
     * Returns the best-matching category for the given merchant name or SMS text.
     * Falls back to "Others" when no keyword matches.
     */
    fun predictCategory(text: String): String {
        if (text.isBlank()) return "Others"
        val lower = text.lowercase()

        var bestCategory = "Others"
        var bestScore = 0

        for (rule in rules) {
            var score = 0
            for ((keyword, weight) in rule.keywords) {
                if (lower.contains(keyword)) score += weight
            }
            if (score > bestScore) {
                bestScore = score
                bestCategory = rule.category
            }
        }

        return bestCategory
    }
}
