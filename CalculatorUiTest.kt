package dev.example.calculator

import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test

class CalculatorUiTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()

    private fun tap(tag: String) = rule.onNodeWithTag("key_$tag").performClick()

    @Test fun sevenTimesSixIsFortyTwo() {
        tap("7"); tap("mul"); tap("6"); tap("equals")
        rule.onNodeWithTag("result").assertTextEquals("42")
    }

    @Test fun acClearsExpression() {
        tap("9"); tap("AC")
        rule.onNodeWithTag("expression").assertTextEquals("")
    }

    @Test fun scientificPanelOpens() {
        rule.onNodeWithTag("toggle_scientific").performClick()
        rule.onNodeWithTag("key_sin").assertExists()
    }

    @Test fun historyRecordsCalculation() {
        tap("2"); tap("add"); tap("3"); tap("equals")
        rule.onNodeWithTag("toggle_history").performClick()
        rule.onNodeWithTag("history_panel").assertExists()
    }
}
