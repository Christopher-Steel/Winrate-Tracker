package com.dontpunchmugs.winratetracker

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.widget.LinearLayout
import com.google.gson.Gson
import com.google.gson.GsonBuilder

import kotlinx.android.synthetic.main.activity_main.*
import java.io.FileNotFoundException
import java.time.LocalDate
import java.util.*

class MainActivity : AppCompatActivity() {
    private val batchListPath = "batch_list.json"

    private val gson: Gson = GsonBuilder()
        .setDateFormat("dd/MM/yyyy")
        .registerTypeAdapter(LocalDate::class.java, LocalDateTypeAdapter().nullSafe())
        .create()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        saver = ::saveToFile
        loader = ::loadFromFile
        renderBatchList(loadBatchList())
    }

    private fun saveToFile(path: String, data: String) {
        openFileOutput(path, Context.MODE_PRIVATE).use {
            it.write(data.toByteArray())
        }
    }

    private fun loadFromFile(path: String): String {
        val fileData = openFileInput(path)
        return fileData.bufferedReader().readText()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_create_batch -> {
                createBatch()
                true
            }
            R.id.action_delete_everything -> {
                confirmDeleteEverything()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun loadBatchList(): List<String> {
        val fileText: String =
            try {
                loadFromFile(batchListPath)
            } catch (e: FileNotFoundException) {
                "[]"
            }
        return gson.fromJson(fileText, Array<String>::class.java).toList()
    }

    private fun renderBatchList(batchIds: List<String>) {
        batchIds.map {
            val batch = WinLossBatch(uuid=UUID.fromString(it))
            batch.load()
            addBatchToList(batch)
            batch
        }
    }

    private fun createBatch() {
        val batch = WinLossBatch()
        val renderer = addBatchToList(batch, atTop=true)
        if (batch.uuid != null) {
            addBatchToSaveFile(batch.uuid!!)
        }
        renderer.enterEditingMode()
    }

    private fun addBatchToSaveFile(uuid: UUID) {
        val batchIds = loadBatchList()
        if (uuid.toString() !in batchIds) {
            saveToFile(batchListPath, gson.toJson(listOf(uuid.toString()) + batchIds))
        }
    }

    private fun wipeSaveFileContents() {
        saveToFile(batchListPath, gson.toJson(emptyList<String>()))
    }

    private fun deleteAllSavedBatches() {
        val batchIds = loadBatchList()
        batchIds.forEach {
            val batch = WinLossBatch(uuid=UUID.fromString(it))
            deleteFile(batch.getFilePath())
        }
    }

    private fun addBatchToList(batch: WinLossBatch, atTop: Boolean = false): WinLossBatchRenderer {
        val batchListView: LinearLayout = findViewById(R.id.batchList)
        val batchRenderer = WinLossBatchRenderer(applicationContext, batch)
        if (atTop) {
            batchListView.addView(batchRenderer, 0)
        } else {
            batchListView.addView(batchRenderer)
        }
        return batchRenderer
    }

    private fun clearList() {
        val batchListView: LinearLayout = findViewById(R.id.batchList)
        batchListView.removeAllViews()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && requestCode == 0) {
            // delete everything
            clearList()
            deleteAllSavedBatches()
            wipeSaveFileContents()
        }
    }

    private fun confirmDeleteEverything() {
        val intent = Intent(this, PopUpWindow::class.java)
        startActivityForResult(intent, 0)
    }
}
