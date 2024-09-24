package util.session

import android.content.Context
import android.content.SharedPreferences

class StoreManager(private val _context: Context) {
    private val editor: SharedPreferences.Editor
    var pref: SharedPreferences
    private val PRIVATE_MODE = 0

    init {
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE)
        editor = pref.edit()
    }

    fun setToken(Token: String) {
        editor.putString("Token", Token)
        println("token saved $Token")
        editor.commit()
    }

    val token: String?
        get() = pref.getString("Token", "")

    companion object {
        private const val PREF_NAME = "_store"
    }
}