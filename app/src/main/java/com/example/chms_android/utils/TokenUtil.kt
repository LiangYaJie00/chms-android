package com.example.chms_android.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

object TokenUtil {
    private const val TOKEN_PREFS = "TOKEN"
    private const val KEY_TOKEN = "token"

    private fun getSharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(TOKEN_PREFS, Context.MODE_PRIVATE)
    }

    fun saveToken(token: String, context: Context) {
        getSharedPreferences(context).edit()
            .putString(KEY_TOKEN, token)
            .apply()
        Log.i("TokenUtil", "saveToken: $token")
    }

    fun getToken(context: Context): String {
        val token = getSharedPreferences(context).getString(KEY_TOKEN, null)
        Log.i("TokenUtil", "getToken: $token")
        return "CHMS $token"
    }

    fun haveToken(context: Context): Boolean {
        val token = getSharedPreferences(context).getString(KEY_TOKEN, null)
        val hasToken = token != null
        Log.i("TokenUtil", "haveToken: $hasToken")
        return hasToken
    }
    
    /**
     * 清除保存的token
     */
    fun clearToken(context: Context) {
        getSharedPreferences(context).edit()
            .remove(KEY_TOKEN)
            .apply()
        Log.i("TokenUtil", "clearToken: token已清除")
    }
}
