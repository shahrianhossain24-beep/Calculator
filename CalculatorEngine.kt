package dev.example.calculator.logic

data class HistoryItem(val expression: String, val result: String)

data class CalcState(
    val expression: String = "",
    val preview: String = "",
    val error: String? = null,
    val justEvaluated: Boolean = false,
    val history: List<HistoryItem> = emptyList(),
    val degrees: Boolean = true,
    val scientific: Boolean = false,
    val historyOpen: Boolean = false,
)

/** Pure reducer functions with no Android dependencies, so they are plain-JUnit testable. */
object CalculatorEngine {
    const val MAX_HISTORY = 100
    private val OPS = charArrayOf('+', '−', '×', '÷', '^')
    private val NUMBER = Regex("^[−-]?[0-9.]+$")
    private val FUNC_TAIL = Regex("(sin|cos|tan|ln|log|√)\\($")
    private const val VALUE_ENDS = ")π!%e"

    private fun fresh(s: CalcState) = s.justEvaluated || s.error != null
    private fun base(s: CalcState) = if (fresh(s)) "" else s.expression
    private fun chainBase(s: CalcState) = if (s.justEvaluated) s.preview else s.expression
    private fun endsValue(c: Char?) = c != null && (c.isDigit() || c in VALUE_ENDS)

    private fun refresh(s: CalcState): CalcState {
        val expr = s.expression.trimEnd(*OPS)
        if (expr.isEmpty() || NUMBER.matches(expr)) return s.copy(preview = "")
        return try {
            s.copy(preview = Evaluator.format(Evaluator.evaluate(expr, s.degrees)))
        } catch (e: CalcException) {
            s.copy(preview = "")
        }
    }

    fun digit(s: CalcState, d: String): CalcState {
        val b = base(s)
        val last = b.lastOrNull()
        var prefix = if (last != null && last in VALUE_ENDS) "×" else ""
        if (d == ".") {
            val run = b.takeLastWhile { it.isDigit() || it == '.' }
            if ('.' in run) return s
            if (run.isEmpty()) prefix += "0" else prefix = ""
        }
        return refresh(s.copy(expression = b + prefix + d, justEvaluated = false, error = null))
    }

    fun operator(s: CalcState, op: Char): CalcState {
        if (s.error != null) return s
        val b = chainBase(s)
        if (b.isEmpty()) return if (op == '−') refresh(s.copy(expression = "−", justEvaluated = false)) else s
        if (b == "−") return s
        val last = b.last()
        val next = when {
            last in OPS -> b.dropLast(1) + op
            last == '(' -> if (op == '−') b + op else return s
            else -> b + op
        }
        return refresh(s.copy(expression = next, justEvaluated = false))
    }

    fun function(s: CalcState, name: String): CalcState =
        refresh(s.copy(expression = base(s) + name + "(", justEvaluated = false, error = null))

    fun constant(s: CalcState, c: String): CalcState =
        refresh(s.copy(expression = base(s) + c, justEvaluated = false, error = null))

    fun postfix(s: CalcState, c: Char): CalcState {
        if (s.error != null) return s
        val b = chainBase(s)
        if (!endsValue(b.lastOrNull())) return s
        return refresh(s.copy(expression = b + c, justEvaluated = false))
    }

    fun square(s: CalcState): CalcState {
        if (s.error != null) return s
        val b = chainBase(s)
        if (!endsValue(b.lastOrNull())) return s
        return refresh(s.copy(expression = "$b^2", justEvaluated = false))
    }

    fun parens(s: CalcState): CalcState {
        val b = base(s)
        val open = b.count { it == '(' }
        val close = b.count { it == ')' }
        val closeIt = open > close && endsValue(b.lastOrNull())
        return refresh(s.copy(expression = b + if (closeIt) ")" else "(", justEvaluated = false, error = null))
    }

    fun backspace(s: CalcState): CalcState {
        if (s.error != null) return clear(s)
        val e = s.expression
        if (e.isEmpty()) return s
        val next = FUNC_TAIL.find(e)?.let { e.removeRange(it.range) } ?: e.dropLast(1)
        return refresh(s.copy(expression = next, justEvaluated = false))
    }

    fun clear(s: CalcState) = s.copy(expression = "", preview = "", error = null, justEvaluated = false)

    fun equals(s: CalcState): CalcState {
        if (s.error != null || s.expression.isEmpty() || s.justEvaluated) return s
        val expr = s.expression.trimEnd(*OPS)
        if (expr.isEmpty()) return s
        if (NUMBER.matches(expr)) return s.copy(expression = expr, preview = expr, justEvaluated = true)
        return try {
            val r = Evaluator.format(Evaluator.evaluate(expr, s.degrees))
            s.copy(
                expression = expr, preview = r, justEvaluated = true, error = null,
                history = (listOf(HistoryItem(expr, r)) + s.history).take(MAX_HISTORY),
            )
        } catch (e: CalcException) {
            s.copy(error = e.message ?: "Error", preview = "")
        }
    }

    fun toggleDegrees(s: CalcState) = refresh(s.copy(degrees = !s.degrees))
    fun toggleScientific(s: CalcState) = s.copy(scientific = !s.scientific)
    fun toggleHistory(s: CalcState) = s.copy(historyOpen = !s.historyOpen)
    fun clearHistory(s: CalcState) = s.copy(history = emptyList())
    fun recall(s: CalcState, item: HistoryItem) =
        refresh(s.copy(expression = item.expression, justEvaluated = false, error = null))
}
