package dev.example.calculator.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import dev.example.calculator.logic.CalcState

class CalculatorViewModel : ViewModel() {
    var state by mutableStateOf(CalcState())
        private set

    fun update(transform: (CalcState) -> CalcState) {
        state = transform(state)
    }
}
