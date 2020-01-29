package org.envfirst.recyclewiz

enum class WeightUnit {
    g, kg, ton, lb, ounce
}

class WeightConvert {
    val conversion = arrayOf(
        mapOf("source" to WeightUnit.g, "target" to WeightUnit.kg, "conversion" to 0.001f),
        mapOf("source" to WeightUnit.kg, "target" to WeightUnit.g, "conversion" to 1000f),
        mapOf("source" to WeightUnit.g, "target" to WeightUnit.lb, "conversion" to 0.00220462f),
        mapOf("source" to WeightUnit.lb, "target" to WeightUnit.g, "conversion" to 453.592f),
        mapOf("source" to WeightUnit.g, "target" to WeightUnit.lb, "conversion" to 0.00220462f),
        mapOf("source" to WeightUnit.lb, "target" to WeightUnit.g, "conversion" to 453.592f),
        mapOf("source" to WeightUnit.kg, "target" to WeightUnit.lb, "conversion" to 2.20462f),
        mapOf("source" to WeightUnit.lb, "target" to WeightUnit.kg, "conversion" to 0.453592f)
    )

    public fun convert(source: String, target: String, amount: String): String {
        var result = 0.0
        val source = WeightUnit.valueOf(source.toLowerCase())
        val target = WeightUnit.valueOf(target.toLowerCase())
        val convertionRate =
            conversion.filter({ l -> l.get("source") == source && l.get("target") == target })[0]
        var rate: Float = 1.0f
        var matchedRate = convertionRate.get("conversion")
        if (matchedRate is Float) {
            rate = matchedRate
        }
        return (amount.toFloat() * rate).toString()
    }
}