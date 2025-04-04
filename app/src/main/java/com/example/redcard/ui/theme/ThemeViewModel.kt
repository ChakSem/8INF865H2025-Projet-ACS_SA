package com.example.redcard.ui.theme

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.compose.runtime.State
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ThemeViewModel : ViewModel() {
    // Initialisation d'un StateFlow pour l'état du thème
    private val _theme = MutableStateFlow(AppTheme.SYSTEME) // Par exemple, thème par défaut
    val theme: StateFlow<AppTheme> = _theme

    // Méthode pour changer de thème
    fun setTheme(theme: AppTheme) {
        _theme.value = theme
    }
}


