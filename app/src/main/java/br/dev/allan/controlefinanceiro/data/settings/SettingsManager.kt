package br.dev.allan.controlefinanceiro.data.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SettingsManager(private val context: Context) {
    private val Context.dataStore by preferencesDataStore(name = "settings")
    private val IS_BALANCE_VISIBLE = booleanPreferencesKey("is_balance_visible")

    val isBalanceVisible: Flow<Boolean> = context.dataStore.data
        .map { preferences -> preferences[IS_BALANCE_VISIBLE] ?: true }

    suspend fun setBalanceVisible(isVisible: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[IS_BALANCE_VISIBLE] = isVisible
        }
    }
}