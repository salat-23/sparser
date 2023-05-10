package io.salat.sparser.annotations

/**
 * Marks class as command class. Recommended to use with POJO classes only for now.
 * All fields must be annotated with [Argument]
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Command(
    val name: String,
    val description: String,
    vararg val identifiers: String,
)
