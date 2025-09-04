package com.example.tactonprueba.utils

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Variabless Globales =============================================================================
// Almacenar la información del usuario
val Context.userDataStore by preferencesDataStore(name = "user_prefs")

// FIN Variables Globales ==========================================================================

// Modelo de Datos =================================================================================
// Objeto Preferencias Usuario =====================================================================
object UserPreferences {

    // Datos del usuario
    private val INDICATIVO = stringPreferencesKey("indicativo")
    private val CIA = stringPreferencesKey("cia")
    private val SCC = stringPreferencesKey("scc")
    private val PN = stringPreferencesKey("pn")
    private val EMPLEO = stringPreferencesKey("empleo")
    private val SERVIDOR = stringPreferencesKey("servidor")

    // Guarda todos los datos
    suspend fun saveUserData(
        context: Context,
        indicativo: String,
        cia: String,
        scc: String,
        pn: String,
        empleo: String,
        servidor: String = ""
    ) {
        context.userDataStore.edit { prefs ->
            prefs[INDICATIVO] = indicativo
            prefs[CIA] = cia
            prefs[SCC] = scc
            prefs[PN] = pn
            prefs[EMPLEO] = empleo
            prefs[SERVIDOR] = servidor
        }
    }

    // Obtener los datos
    fun getUserData(context: Context): Flow<Map<String, String>> {
        return context.userDataStore.data.map { prefs ->
            mapOf(
                "indicativo" to (prefs[INDICATIVO] ?: ""),
                "cia" to (prefs[CIA] ?: ""),
                "scc" to (prefs[SCC] ?: ""),
                "pn" to (prefs[PN] ?: ""),
                "empleo" to (prefs[EMPLEO] ?: ""),
                "servidor" to (prefs[SERVIDOR] ?: "")
            )
        }
    }
}
