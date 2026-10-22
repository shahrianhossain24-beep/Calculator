package dev.example.calculator

import dev.example.calculator.logic.CalcState
import dev.example.calculator.logic.CalculatorEngine as E
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class EngineTest {
    private fun run(vararg steps: (CalcState) -> CalcState) = steps.fold(CalcState()) { acc, f -> f(acc) }
    private fun d(x: String): (CalcState) -> CalcState = { E.digit(it, x) }
    private fun op(c: Char): (CalcState) -> CalcState = { E.operator(it, c) }
    private val eq: (CalcState) -> CalcState = { E.equals(it) }

    @Test fun sevenTimesSix() {
        val s = run(d("7"), op('×'), d("6"), eq)
        assertEquals("42", s.preview)
        assertTrue(s.justEvaluated)
        assertEquals(1, s.history.size)
    }

    @Test fun livePreview() = assertEquals("5", run(d("2"), op('+'), d("3")).preview)
    @Test fun trailingOperatorHasNoPreview() = assertEquals("", run(d("5"), op('×')).preview)

    @Test fun secondDotIgnored() = assertEquals("1.5", run(d("1"), d("."), d("5"), d(".")).expression)
    @Test fun leadingDotGetsZero() = assertEquals("0.", run(d(".")).expression)
    @Test fun operatorReplaced() = assertEquals("5×", run(d("5"), op('+'), op('×')).expression)

    @Test fun operatorContinuesFromResult() {
        val s = run(d("2"), op('+'), d("3"), eq, op('×'), d("2"))
        assertEquals("5×2", s.expression)
        assertEquals("10", s.preview)
    }

    @Test fun digitAfterEqualsStartsFresh() {
        val s = run(d("2"), op('+'), d("3"), eq, d("9"))
        assertEquals("9", s.expression)
        assertFalse(s.justEvaluated)
    }

    @Test fun backspaceRemovesWholeFunctionToken() {
        val s = run({ E.function(it, "sin") }, { E.backspace(it) })
        assertEquals("", s.expression)
    }

    @Test fun smartParens() {
        val s = run({ E.parens(it) }, d("2"), { E.parens(it) })
        assertEquals("(2)", s.expression)
    }

    @Test fun divideByZeroShowsError() {
        val s = run(d("5"), op('÷'), d("0"), eq)
        assertEquals("Can't divide by 0", s.error)
    }

    @Test fun errorClearedByDigit() {
        val s = run(d("5"), op('÷'), d("0"), eq, d("3"))
        assertEquals(null, s.error)
        assertEquals("3", s.expression)
    }

    @Test fun historyCapped() {
        var s = CalcState()
        repeat(E.MAX_HISTORY + 5) { i ->
            s = E.clear(s)
            s = run2(s, d("1"), op('+'), d((i % 9 + 1).toString()), eq)
        }
        assertEquals(E.MAX_HISTORY, s.history.size)
    }

    @Test fun recallFromHistory() {
        val s0 = run(d("2"), op('+'), d("3"), eq)
        val s1 = E.recall(E.clear(s0), s0.history.first())
        assertEquals("2+3", s1.expression)
        assertEquals("5", s1.preview)
    }

    @Test fun degreesToggleRecomputesPreview() {
        val s = run({ E.function(it, "sin") }, d("3"), d("0"), { E.parens(it) })
        assertEquals("0.5", s.preview)
        assertNotNull(E.toggleDegrees(s).preview)
        assertFalse(E.toggleDegrees(s).degrees)
    }

    @Test fun togglesFlip() {
        assertTrue(E.toggleScientific(CalcState()).scientific)
        assertTrue(E.toggleHistory(CalcState()).historyOpen)
    }

    private fun run2(start: CalcState, vararg steps: (CalcState) -> CalcState) = steps.fold(start) { acc, f -> f(acc) }
}
