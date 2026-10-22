package dev.example.calculator.logic

import java.math.BigDecimal
import java.math.MathContext
import kotlin.math.*

class CalcException(message: String) : Exception(message)

/** Original recursive-descent evaluator, written from scratch. */
object Evaluator {
    fun evaluate(input: String, degrees: Boolean = true): Double {
        val v = Parser(input.replace(" ", ""), degrees).parseAll()
        if (v.isNaN() || v.isInfinite()) throw CalcException("Undefined")
        return v
    }

    fun format(v: Double): String {
        if (v == floor(v) && abs(v) < 1e15) return v.toLong().toString().replace('-', '−')
        val bd = BigDecimal(v).round(MathContext(12)).stripTrailingZeros()
        if (abs(v) >= 1e15 || abs(v) < 1e-9) {
            val exp = bd.precision() - bd.scale() - 1
            val mantissa = bd.movePointLeft(exp).stripTrailingZeros().toPlainString().replace('-', '−')
            return "$mantissa×10^$exp"
        }
        return bd.toPlainString().replace('-', '−')
    }

    private fun clean(v: Double) = if (abs(v) < 1e6) Math.round(v * 1e12) / 1e12 else v
    private val FUNCS = setOf("sin", "cos", "tan", "ln", "log")

    private class Parser(val s: String, val deg: Boolean) {
        var i = 0
        fun peek(): Char? = s.getOrNull(i)

        fun parseAll(): Double {
            val v = expr()
            if (i < s.length) throw CalcException("Invalid format")
            return v
        }

        fun expr(): Double {
            var v = term()
            while (true) when (peek()) {
                '+' -> { i++; v += term() }
                '−', '-' -> { i++; v -= term() }
                else -> return v
            }
        }

        fun term(): Double {
            var v = unary()
            while (true) {
                val c = peek()
                when {
                    c == '×' || c == '*' -> { i++; v *= unary() }
                    c == '÷' || c == '/' -> {
                        i++
                        val d = unary()
                        if (d == 0.0) throw CalcException("Can't divide by 0")
                        v /= d
                    }
                    c != null && (c == '(' || c == '√' || c == '.' || c.isDigit() || c.isLetter()) -> v *= unary()
                    else -> return v
                }
            }
        }

        fun unary(): Double = when (peek()) {
            '−', '-' -> { i++; -unary() }
            '+' -> { i++; unary() }
            else -> power()
        }

        fun power(): Double {
            val base = postfix()
            if (peek() == '^') { i++; return base.pow(unary()) }
            return base
        }

        fun postfix(): Double {
            var v = primary()
            while (true) when (peek()) {
                '%' -> { i++; v /= 100.0 }
                '!' -> { i++; v = factorial(v) }
                else -> return v
            }
        }

        fun factorial(x: Double): Double {
            if (x < 0 || x != floor(x) || x > 170) throw CalcException("Undefined")
            var r = 1.0
            for (k in 2..x.toInt()) r *= k
            return r
        }

        fun primary(): Double {
            val c = peek() ?: throw CalcException("Invalid format")
            return when {
                c.isDigit() || c == '.' -> number()
                c == '(' -> { i++; val v = expr(); if (peek() == ')') i++; v }
                c == '√' -> {
                    i++
                    val x = primary()
                    if (x < 0) throw CalcException("Undefined")
                    sqrt(x)
                }
                c.isLetter() -> named()
                else -> throw CalcException("Invalid format")
            }
        }

        fun number(): Double {
            val start = i
            while (peek()?.let { it.isDigit() || it == '.' } == true) i++
            return s.substring(start, i).toDoubleOrNull() ?: throw CalcException("Invalid format")
        }

        fun named(): Double {
            val start = i
            while (peek()?.isLetter() == true) i++
            val name = s.substring(start, i)
            if (name in FUNCS) {
                if (peek() != '(') throw CalcException("Invalid format")
                i++
                val x = expr()
                if (peek() == ')') i++
                return apply(name, x)
            }
            i = start + 1
            return when (s[start]) {
                'π' -> PI
                'e' -> E
                else -> throw CalcException("Invalid format")
            }
        }

        fun apply(name: String, x: Double): Double {
            val r = if (deg) Math.toRadians(x) else x
            return when (name) {
                "sin" -> clean(sin(r))
                "cos" -> clean(cos(r))
                "tan" -> {
                    if (deg && ((x - 90.0) % 180.0 == 0.0)) throw CalcException("Undefined")
                    clean(tan(r))
                }
                "ln" -> if (x <= 0) throw CalcException("Undefined") else ln(x)
                "log" -> if (x <= 0) throw CalcException("Undefined") else clean(log10(x))
                else -> throw CalcException("Invalid format")
            }
        }
    }
}
