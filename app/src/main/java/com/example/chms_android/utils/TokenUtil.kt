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
}