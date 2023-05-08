package io.salat.sparser

abstract class ArgumentConverter {

    abstract fun convert(value: String, targetType: Class<*>): Any
    abstract fun canConvert(targetType: Class<*>): Boolean
    open fun isAbleToBeValueless(): Boolean = false
    open fun getDefaultWhenValueless(): Any = throw IllegalStateException("No default value")
}