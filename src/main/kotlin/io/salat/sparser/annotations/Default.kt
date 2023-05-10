package io.salat.sparser.annotations

/**
 * Provides default string value to the [Argument] annotated field.
 */
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class Default(
    val value: String
)
