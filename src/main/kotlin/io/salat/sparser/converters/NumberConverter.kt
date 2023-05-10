package io.salat.sparser.converters

import io.salat.sparser.ArgumentConverter

/**
 * Converts string to number value.
 */
class NumberConverter : ArgumentConverter() {
    override fun convert(value: String, targetType: Class<*>): Number {
        return when (targetType) {
            Double::class.java -> value.toDouble()
            Float::class.java -> value.toFloat()
            Short::class.java -> value.toShort()
            Byte::class.java -> value.toByte()
            Long::class.java -> value.toLong()
            Int::class.java -> value.toInt()
            else -> throw IllegalStateException("Number cannot be converted to type ${targetType.canonicalName}")
        }
    }

    override fun canConvert(targetType: Class<*>): Boolean {
        return (targetType == Double::class.java
                || targetType == Float::class.java
                || targetType == Short::class.java
                || targetType == Byte::class.java
                || targetType == Long::class.java
                || targetType == Int::class.java)
    }
}