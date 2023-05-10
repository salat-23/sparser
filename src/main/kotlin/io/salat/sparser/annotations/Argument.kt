package io.salat.sparser.annotations

/**
 * Marks [Command] class field as argument.
 * Set `required` to `true`, if there is no default argument that can be provided.
 * Otherwise, you must also use [Default] and specify default string value for the argument.
 *
 * Set `implicit` to `true` to be able to use this argument implicitly, by not specifying its
 * name and setting some value right after command identifier in the call string.
 * There can only be one implicit argument in the command.
 *
 *  Example:
 *
 *  Explicit - "`who -name John`"
 *
 *  Implicit - "`who John`"
 */
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class Argument(
    val name: String,
    val description: String,
    val required: Boolean = false,
    val implicit: Boolean = false,
    vararg val identifiers: String
)