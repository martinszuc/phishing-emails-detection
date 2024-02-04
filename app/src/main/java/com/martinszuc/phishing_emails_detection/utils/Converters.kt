package com.martinszuc.phishing_emails_detection.utils

import androidx.room.TypeConverter
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import com.martinszuc.phishing_emails_detection.data.local.entity.email_full.Header
import com.martinszuc.phishing_emails_detection.data.local.entity.email_full.Part

/**
 * Authored by matoszuc@gmail.com
 */

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromHeaderList(headers: List<Header>): String {
        return gson.toJson(headers)
    }

    @TypeConverter
    fun toHeaderList(headersString: String): List<Header> {
        val type = object : TypeToken<List<Header>>() {}.type
        return gson.fromJson(headersString, type)
    }

    @TypeConverter
    fun fromPartList(parts: List<Part>): String {
        return gson.toJson(parts)
    }

    @TypeConverter
    fun toPartList(partsString: String): List<Part> {
        val type = object : TypeToken<List<Part>>() {}.type
        return gson.fromJson(partsString, type)
    }

    @TypeConverter
    fun fromStringList(strings: List<String>): String {
        return gson.toJson(strings)
    }

    @TypeConverter
    fun toStringList(stringsString: String): List<String> {
        val type = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(stringsString, type)
    }
}
