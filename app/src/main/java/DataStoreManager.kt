import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("userSession")

class DataStoreManager(context: Context) {

    private val dataStore = context.dataStore

    companion object {
        val LOGGED_IN_KEY = booleanPreferencesKey("logged_in")
        val USER_ID_KEY = stringPreferencesKey("user_id")
    }

    val isLoggedIn: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[LOGGED_IN_KEY] ?: false
    }

    val userId: Flow<String?> = dataStore.data.map { preferences ->
        preferences[USER_ID_KEY]
    }

    suspend fun setLoggedIn(loggedIn: Boolean) {
        dataStore.edit { preferences ->
            preferences[LOGGED_IN_KEY] = loggedIn
        }
    }

    suspend fun setUserId(userId: String) {
        dataStore.edit { preferences ->
            preferences[USER_ID_KEY] = userId
        }
    }

    suspend fun clearUserData() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }

}

