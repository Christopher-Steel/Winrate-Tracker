package com.dontpunchmugs.winratetracker

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.Image
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import java.lang.Exception
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

class EditBatch : AppCompatActivity() {

    private lateinit var editName : EditText
    private lateinit var editDate : EditText
    private lateinit var editWins : EditText
    private lateinit var editLosses : EditText
    private val dateFormatter : DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    private lateinit var uuidOfBatchBeingEdited : UUID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_batch)

        editName = findViewById(R.id.nameEdit)
        editDate = findViewById(R.id.dateCreatedEdit)
        editWins = findViewById(R.id.winNumberEdit)
        editLosses = findViewById(R.id.lossNumberEdit)

        // Extra data from intent
        val bundle = intent.extras
        if (bundle != null) {
            uuidOfBatchBeingEdited = UUID.fromString(bundle.getString("uuid"))
            val batch = WinLossBatch(uuid = uuidOfBatchBeingEdited)
            batch.load()
            editName.setText(batch.name)
            editDate.setText(batch.dateCreated.format(dateFormatter))
            editWins.setText(batch.wins.toString())
            editLosses.setText(batch.losses.toString())
        }

        val confirmEditsButton : Button = findViewById(R.id.confirmEditsButton)
        confirmEditsButton.setOnClickListener { saveChanges() }

        val deleteButton : ImageButton = findViewById(R.id.deleteButton)
        deleteButton.setOnClickListener { deleteBatch() }

        editName.requestFocus()
        showKeyboard()
    }

    fun saveChanges() {
        var dateCreated = LocalDate.now()
        var wins = 0
        var losses = 0

        try {
            dateCreated = LocalDate.parse(editDate.text, dateFormatter)
            wins = editWins.text.toString().toInt(10)
            losses = editLosses.text.toString().toInt(10)
        } catch (e: Exception) {}
        val batch = WinLossBatch(
            name = editName.text.toString(),
            dateCreated = dateCreated,
            wins = wins,
            losses = losses,
            uuid = uuidOfBatchBeingEdited
        )
        batch.save()
        hideKeyboard()
        val returnedIntent = Intent()
        returnedIntent.putExtra("uuid", uuidOfBatchBeingEdited.toString())
        setResult(Activity.RESULT_OK, returnedIntent)
        finish()
    }

    // We let the calling activity handle the deletion
    private fun deleteBatch() {
        val returnedIntent = Intent()
        returnedIntent.putExtra("uuid", uuidOfBatchBeingEdited.toString())
        setResult(42, returnedIntent)
        finish()
    }

    private fun hideKeyboard() {
        getInput().hideSoftInputFromWindow(window.decorView.windowToken, 0)
    }

    private fun showKeyboard()  {
        getInput().showSoftInput(window.decorView, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun getInput(): InputMethodManager {
        return getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    }
}
