package dev.example.calculator

import dev.example.calculator.logic.CalcException
import dev.example.calculator.logic.Evaluator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class EvaluatorTest {
    private fun ev(s: String, deg: Boolean = true) = Evaluator.evaluate(s, deg)

    @Test fun precedence() = assertEquals(14.0, ev("2+3×4"), 0.0)
    @Test fun parentheses() = assertEquals(20.0, ev("(2+3)×4"), 0.0)
    @Test fun unaryMinusBelowPower() = assertEquals(-4.0, ev("−2^2"), 0.0)
    @Test fun powerRightAssociative() = assertEquals(512.0, ev("2^3^2"), 0.0)
    @Test fun division() = assertEquals(2.5, ev("10÷4"), 0.0)
    @Test fun percent() = assertEquals(0.5, ev("50%"), 1e-12)
    @Test fun factorial() = assertEquals(120.0, ev("5!"), 0.0)
    @Test fun sinDegrees() = assertEquals(0.5, ev("sin(30)"), 1e-12)
    @Test fun cosDegrees() = assertEquals(0.0, ev("cos(90)"), 1e-12)
    @Test fun sinRadians() = assertEquals(1.0, ev("sin(π/2)", deg = false), 1e-12)
    @Test fun implicitMultiply() = assertEquals(14.0, ev("2(3+4)"), 0.0)
    @Test fun piConstant() = assertEquals(2 * Math.PI, ev("2π"), 1e-12)
    @Test fun sqrt() = assertEquals(4.0, ev("√16"), 0.0)
    @Test fun lnE() = assertEquals(1.0, ev("ln(e)"), 1e-12)
    @Test fun log10() = assertEquals(3.0, ev("log(1000)"), 1e-12)
    @Test fun lenientMissingParen() = assertEquals(5.0, ev("(2+3"), 0.0)

    @Test fun divideByZero() {
        assertThrows(CalcException::class.java) { ev("5÷0") }
    }
    @Test fun tan90Undefined() {
        assertThrows(CalcException::class.java) { ev("tan(90)") }
    }
    @Test fun sqrtNegativeUndefined() {
        assertThrows(CalcException::class.java) { ev("√(−1)") }
    }
    @Test fun danglingOperatorInvalid() {
        assertThrows(CalcException::class.java) { ev("3+") }
    }

    @Test fun formatInteger() = assertEquals("14", Evaluator.format(14.0))
    @Test fun formatFloatNoise() = assertEquals("0.3", Evaluator.format(0.1 + 0.2))
    @Test fun formatThird() = assertEquals("0.333333333333", Evaluator.format(1.0 / 3.0))
    @Test fun formatLarge() = assertEquals("1×10^15", Evaluator.format(1e15))
    @Test fun formatSmall() = assertEquals("1.5×10^-10", Evaluator.format(1.5e-10))
    @Test fun formatLargeReparses() = assertEquals(1e15, ev(Evaluator.format(1e15)), 1.0)
}
