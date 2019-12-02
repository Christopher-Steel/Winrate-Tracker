package com.dontpunchmugs.winratetracker

import android.content.Context
import android.media.Image
import android.util.AttributeSet
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import org.w3c.dom.Attr
import java.lang.Exception
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

class WinLossBatchRenderer(context: Context, attrs: AttributeSet? = null, batchData: WinLossBatch? = null): LinearLayout(context) {
    private val nameText : TextView
    private val dateCreatedText : TextView
    private val winRateText : TextView
    private var batch: WinLossBatch = WinLossBatch()
    private val scoring : View
    private val editing : View
    private val editName : EditText
    private val editDate : EditText
    private val editWins : EditText
    private val editLosses : EditText
    private val dateFormatter : DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    constructor(context: Context, attrs: AttributeSet): this(context, attrs, null)
    constructor(context: Context, batchData: WinLossBatch): this(context, null, batchData)

    init {
        inflate(context, R.layout.win_loss_batch, this)
        nameText = findViewById(R.id.name)
        dateCreatedText = findViewById(R.id.dateCreated)
        winRateText = findViewById(R.id.winRate)
        scoring = findViewById(R.id.scoring)
        editing = findViewById(R.id.editing)
        editing.visibility = View.GONE
        scoring.visibility = View.VISIBLE

        if (batchData != null) {
            batch = batchData
        } else if (attrs != null) {
            extractBatchFromAttributes(attrs)
        }
        render()

        // set event handlers
        val addWinButton : ImageButton = findViewById(R.id.winButton)
        val addLossButton : ImageButton = findViewById(R.id.loseButton)
        addWinButton.setOnClickListener { batch.wins += 1 ; batch.save() ; render() }
        addLossButton.setOnClickListener { batch.losses += 1 ; batch.save() ; render() }
        winRateText.setOnLongClickListener { enterEditingMode() ; true }

        editName = findViewById(R.id.nameEdit)
        editDate = findViewById(R.id.dateCreatedEdit)
        editWins = findViewById(R.id.winNumberEdit)
        editLosses = findViewById(R.id.lossNumberEdit)

        val confirmEditsButton : ImageButton = findViewById(R.id.confirmEditsButton)
        confirmEditsButton.setOnClickListener { leaveEditingMode() ; render() }
    }

    private fun extractBatchFromAttributes(attrs:AttributeSet) {
        val attributes = context.obtainStyledAttributes(attrs, R.styleable.WinLossBatchRenderer)
        batch.name = attributes.getString(R.styleable.WinLossBatchRenderer_name).toString()
        val dateAttr = attributes.getString(R.styleable.WinLossBatchRenderer_dateCreated)
        if (dateAttr != null) {
            batch.dateCreated = LocalDate.parse(dateAttr, dateFormatter)
        }
        batch.wins = attributes.getInt(R.styleable.WinLossBatchRenderer_wins, 0)
        batch.losses = attributes.getInt(R.styleable.WinLossBatchRenderer_losses, 0)
        attributes.recycle()
    }

    private fun render() {
        val winRateString : String
        val winRate : Float
        nameText.text = batch.name
        dateCreatedText.text = batch.dateCreated.format(dateFormatter)
        if (batch.wins + batch.losses == 0) {
            winRateString = "No data"
        } else {
            winRate = batch.wins.toFloat() / (batch.wins + batch.losses).toFloat() * 100f
            winRateString = "%.2f%%".format(winRate)
        }
        winRateText.text = winRateString
    }

    fun enterEditingMode() {
        scoring.visibility = View.GONE
        editName.setText(batch.name)
        if (editName.text.isEmpty()) {
            editName.requestFocus()
            showKeyboard()
        }
        editDate.setText(batch.dateCreated.format(dateFormatter))
        editWins.setText(batch.wins.toString(10))
        editLosses.setText(batch.losses.toString(10))
        editing.visibility = View.VISIBLE
    }

    fun leaveEditingMode() {
        editing.visibility = View.GONE
        batch.name = editName.text.toString()
        try {
            batch.dateCreated = LocalDate.parse(editDate.text, dateFormatter)
        } catch (e: Exception) {}
        batch.wins = editWins.text.toString().toInt(10)
        batch.losses = editLosses.text.toString().toInt(10)
        batch.save()
        render()
        hideKeyboard()
        scoring.visibility = View.VISIBLE
    }

    private fun hideKeyboard() {
        getInput().hideSoftInputFromWindow(windowToken, 0)
    }

    private fun showKeyboard()  {
        getInput().showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun getInput(): InputMethodManager {
        return context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    }
}