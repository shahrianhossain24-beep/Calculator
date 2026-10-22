package dev.example.calculator.ui

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.example.calculator.logic.CalcState
import dev.example.calculator.logic.CalculatorEngine as E
import dev.example.calculator.logic.HistoryItem

enum class KeyStyle { Digit, Operator, Function, Clear, Equals }

data class KeyDef(
    val label: String,
    val style: KeyStyle,
    val tag: String = label,
    val action: (CalcState) -> CalcState,
)

private data class ClearEvent(val id: Int, val text: String)

private fun digit(d: String) = KeyDef(d, KeyStyle.Digit) { E.digit(it, d) }

private fun basicKeys(): List<KeyDef> = listOf(
    KeyDef("AC", KeyStyle.Clear, "AC") { E.clear(it) },
    KeyDef("( )", KeyStyle.Function, "paren") { E.parens(it) },
    KeyDef("%", KeyStyle.Function, "percent") { E.postfix(it, '%') },
    KeyDef("÷", KeyStyle.Operator, "div") { E.operator(it, '÷') },
    digit("7"), digit("8"), digit("9"),
    KeyDef("×", KeyStyle.Operator, "mul") { E.operator(it, '×') },
    digit("4"), digit("5"), digit("6"),
    KeyDef("−", KeyStyle.Operator, "sub") { E.operator(it, '−') },
    digit("1"), digit("2"), digit("3"),
    KeyDef("+", KeyStyle.Operator, "add") { E.operator(it, '+') },
    KeyDef("⌫", KeyStyle.Function, "backspace") { E.backspace(it) },
    digit("0"), digit("."),
    KeyDef("=", KeyStyle.Equals, "equals") { E.equals(it) },
)

private fun sciKeys(degrees: Boolean): List<KeyDef> = listOf(
    KeyDef(if (degrees) "Deg" else "Rad", KeyStyle.Function, "angle") { E.toggleDegrees(it) },
    KeyDef("sin", KeyStyle.Function) { E.function(it, "sin") },
    KeyDef("cos", KeyStyle.Function) { E.function(it, "cos") },
    KeyDef("tan", KeyStyle.Function) { E.function(it, "tan") },
    KeyDef("ln", KeyStyle.Function) { E.function(it, "ln") },
    KeyDef("log", KeyStyle.Function) { E.function(it, "log") },
    KeyDef("√", KeyStyle.Function, "sqrt") { E.function(it, "√") },
    KeyDef("^", KeyStyle.Function, "pow") { E.operator(it, '^') },
    KeyDef("!", KeyStyle.Function, "fact") { E.postfix(it, '!') },
    KeyDef("π", KeyStyle.Function, "pi") { E.constant(it, "π") },
    KeyDef("e", KeyStyle.Function, "euler") { E.constant(it, "e") },
    KeyDef("x²", KeyStyle.Function, "square") { E.square(it) },
)

@Composable
fun CalculatorScreen(vm: CalculatorViewModel = viewModel()) {
    val s = vm.state
    val landscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    var clearEvent by remember { mutableStateOf(ClearEvent(0, "")) }
    val onKey: (KeyDef) -> Unit = { key ->
        if (key.tag == "AC" && vm.state.expression.isNotEmpty()) {
            clearEvent = ClearEvent(clearEvent.id + 1, vm.state.expression)
        }
        vm.update(key.action)
    }

    Box(Modifier.fillMaxSize().background(Palette.Background).windowInsetsPadding(WindowInsets.systemBars)) {
        if (landscape) {
            Row(Modifier.fillMaxSize().padding(8.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(Modifier.weight(1f).fillMaxHeight()) {
                    DisplayPane(s, clearEvent, vm::update, Modifier.weight(1f))
                    HandleRow(s, vm::update)
                }
                Row(Modifier.weight(1.9f).fillMaxHeight(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    KeyGrid(sciKeys(s.degrees), 3, true, onKey, Modifier.weight(0.75f).fillMaxHeight())
                    KeyGrid(basicKeys(), 4, true, onKey, Modifier.weight(1f).fillMaxHeight())
                }
            }
        } else {
            Column(Modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 8.dp)) {
                DisplayPane(s, clearEvent, vm::update, Modifier.weight(1f))
                HandleRow(s, vm::update)
                AnimatedVisibility(
                    visible = s.scientific,
                    enter = expandVertically(tween(280, easing = FastOutSlowInEasing)) + fadeIn(tween(280)),
                    exit = shrinkVertically(tween(220, easing = FastOutSlowInEasing)) + fadeOut(tween(160)),
                ) {
                    Column {
                        KeyGrid(sciKeys(s.degrees), 4, false, onKey, Modifier)
                        androidx.compose.foundation.layout.Spacer(Modifier.padding(top = 8.dp))
                    }
                }
                KeyGrid(basicKeys(), 4, false, onKey, Modifier)
            }
        }
    }
}

@Composable
private fun HandleRow(s: CalcState, update: ((CalcState) -> CalcState) -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        TextButton(onClick = { update { E.toggleHistory(it) } }, modifier = Modifier.testTag("toggle_history")) {
            Text("History", color = if (s.historyOpen) Palette.LightBlue else Palette.Muted)
        }
        TextButton(onClick = { update { E.toggleScientific(it) } }, modifier = Modifier.testTag("toggle_scientific")) {
            Text("Scientific", color = if (s.scientific) Palette.LightBlue else Palette.Muted)
        }
    }
}

@Composable
private fun DisplayPane(
    s: CalcState,
    clearEvent: ClearEvent,
    update: ((CalcState) -> CalcState) -> Unit,
    modifier: Modifier,
) {
    val ease = FastOutSlowInEasing
    // Expression shrinks and result grows when "=" is pressed.
    val exprSize by animateFloatAsState(if (s.justEvaluated) 26f else 42f, tween(260, easing = ease), label = "exprSize")
    val resultSize by animateFloatAsState(if (s.justEvaluated) 54f else 28f, tween(260, easing = ease), label = "resultSize")
    val exprColor by animateColorAsState(if (s.justEvaluated) Palette.Muted else Color.White, tween(260), label = "exprColor")

    // AC: the old expression floats up and fades out.
    val ghost = remember { Animatable(1f) }
    var ghostText by remember { mutableStateOf("") }
    LaunchedEffect(clearEvent) {
        if (clearEvent.text.isNotEmpty()) {
            ghostText = clearEvent.text
            ghost.snapTo(0f)
            ghost.animateTo(1f, tween(240, easing = ease))
            ghostText = ""
        }
    }

    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.Bottom, horizontalAlignment = Alignment.End) {
        // History slides open above the display.
        AnimatedVisibility(
            visible = s.historyOpen,
            modifier = Modifier.weight(1f, fill = false),
            enter = expandVertically(tween(300, easing = ease)) + fadeIn(tween(300)),
            exit = shrinkVertically(tween(260, easing = ease)) + fadeOut(tween(160)),
        ) {
            HistoryPane(
                s.history,
                onRecall = { item -> update { E.recall(it, item) } },
                onClear = { update { E.clearHistory(it) } },
            )
        }

        Box(Modifier.fillMaxWidth().heightIn(min = 52.dp).padding(top = 8.dp), contentAlignment = Alignment.CenterEnd) {
            val scroll = rememberScrollState()
            LaunchedEffect(s.expression) { scroll.scrollTo(scroll.maxValue) }
            Text(
                s.expression, color = exprColor, fontSize = exprSize.sp, maxLines = 1, softWrap = false,
                modifier = Modifier.horizontalScroll(scroll).testTag("expression"),
            )
            if (ghostText.isNotEmpty()) {
                Text(
                    ghostText, color = Color.White, fontSize = 42.sp, maxLines = 1, softWrap = false,
                    modifier = Modifier.graphicsLayer {
                        alpha = 1f - ghost.value
                        translationY = -48.dp.toPx() * ghost.value
                    },
                )
            }
        }

        Box(Modifier.fillMaxWidth().heightIn(min = 64.dp), contentAlignment = Alignment.CenterEnd) {
            val text = s.error ?: s.preview
            val style = if (s.error != null) {
                TextStyle(color = Palette.Error, fontSize = 24.sp, fontWeight = FontWeight.Light)
            } else {
                TextStyle(brush = Palette.Gradient, fontSize = resultSize.sp, fontWeight = FontWeight.Light)
            }
            Text(text, style = style, maxLines = 1, softWrap = false, modifier = Modifier.testTag("result"))
        }
    }
}

@Composable
private fun HistoryPane(history: List<HistoryItem>, onRecall: (HistoryItem) -> Unit, onClear: () -> Unit) {
    Column(Modifier.fillMaxWidth().testTag("history_panel"), horizontalAlignment = Alignment.End) {
        if (history.isEmpty()) {
            Text("No history", color = Palette.Muted, modifier = Modifier.padding(vertical = 12.dp))
        } else {
            TextButton(onClick = onClear) { Text("Clear history", color = Palette.LightBlue) }
            LazyColumn(Modifier.weight(1f, fill = false), reverseLayout = true) {
                items(history) { h ->
                    Column(
                        Modifier.fillMaxWidth().clickable { onRecall(h) }.padding(vertical = 6.dp),
                        horizontalAlignment = Alignment.End,
                    ) {
                        Text(h.expression, color = Palette.Muted, fontSize = 16.sp)
                        Text("= ${h.result}", color = Color.White, fontSize = 22.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun KeyGrid(keys: List<KeyDef>, columns: Int, fill: Boolean, onKey: (KeyDef) -> Unit, modifier: Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        keys.chunked(columns).forEach { row ->
            Row(
                Modifier.fillMaxWidth().let { if (fill) it.weight(1f) else it },
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                row.forEach { k ->
                    KeyButton(
                        k,
                        Modifier.weight(1f).let { if (fill) it.fillMaxHeight() else it.aspectRatio(1.15f) },
                    ) { onKey(k) }
                }
            }
        }
    }
}

@Composable
private fun KeyButton(def: KeyDef, modifier: Modifier, onClick: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    // Press "recoil": quick squash and springy return.
    val scale by animateFloatAsState(
        if (pressed) 0.90f else 1f,
        spring(dampingRatio = 0.4f, stiffness = Spring.StiffnessMedium),
        label = "recoil",
    )
    val haptic = LocalHapticFeedback.current
    val shape = RoundedCornerShape(26.dp)
    val bg = when (def.style) {
        KeyStyle.Equals -> Modifier.background(Palette.Gradient, shape)
        KeyStyle.Operator, KeyStyle.Clear -> Modifier.background(Palette.KeyOperator, shape)
        else -> Modifier.background(Palette.Key, shape)
    }
    val fg = when (def.style) {
        KeyStyle.Equals -> Color.White
        KeyStyle.Operator, KeyStyle.Clear -> Palette.LightBlue
        KeyStyle.Function -> Color(0xFFB8C4D9)
        KeyStyle.Digit -> Color.White
    }
    Box(
        modifier
            .testTag("key_${def.tag}")
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(shape)
            .then(bg)
            .clickable(interactionSource = interaction, indication = null) {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick()
            },
        contentAlignment = Alignment.Center,
    ) {
        Text(def.label, color = fg, fontSize = if (def.style == KeyStyle.Function) 20.sp else 26.sp)
    }
}
