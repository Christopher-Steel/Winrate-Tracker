package com.dontpunchmugs.winratetracker

import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.time.LocalDate
import java.util.*

private data class RawData(
    val name: String = "",
    val dateCreated: LocalDate = LocalDate.now(),
    val wins: Int = 0,
    val losses: Int = 0
)

var saver: (String, String) -> Unit = {_, _ ->}
var loader: (String) -> String = {_ -> ""}

class WinLossBatch (
    var name: String = "",
    var dateCreated: LocalDate = LocalDate.now(),
    var wins: Int = 0,
    var losses: Int = 0,
    var uuid: UUID? = null
) {
    private val gson: Gson = GsonBuilder()
        .setDateFormat("dd/MM/yyy")
        .registerTypeAdapter(LocalDate::class.java, LocalDateTypeAdapter().nullSafe())
        .create()

    init {
        if (uuid == null) {
            uuid = UUID.randomUUID()
            save()
        }

    }

    private fun toRawData(): RawData {
        return RawData(name, dateCreated, wins, losses)
    }

    private fun loadRawData(data: RawData) {
        name = data.name
        dateCreated = data.dateCreated
        wins = data.wins
        losses = data.losses
    }

    private fun getFilePath(): String {
        return "${uuid.toString()}.json"
    }

    fun save() {
        val jsonString = gson.toJson(toRawData())
        Log.d("SAVING FILE", getFilePath() + jsonString)
        saver(getFilePath(), jsonString)
    }

    fun load() {
        val fileText = loader(getFilePath())
        if (fileText.isNotEmpty()) {
            val rawData = gson.fromJson(fileText, RawData::class.java)
            loadRawData(rawData)
        }
    }
}
