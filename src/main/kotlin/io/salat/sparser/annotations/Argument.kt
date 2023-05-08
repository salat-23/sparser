package io.salat.sparser.annotations

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class Argument(
    val name: String,
    val description: String,
    val required: Boolean = false,
    val implicit: Boolean = false,
    vararg val identifiers: String
)