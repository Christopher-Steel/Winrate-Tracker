package com.dontpunchmugs.winratetracker

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.Image
import android.util.AttributeSet
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat.startActivity
import org.w3c.dom.Attr
import java.lang.Exception
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

var editModeEnabler: (WinLossBatchRenderer) -> Unit = { }

class WinLossBatchRenderer(context: Context, attrs: AttributeSet? = null, batchData: WinLossBatch? = null): LinearLayout(context) {
    private val nameText : TextView
    private val dateCreatedText : TextView
    private val winRateText : TextView
    var batch: WinLossBatch = WinLossBatch()
    val dateFormatter : DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    constructor(context: Context, attrs: AttributeSet): this(context, attrs, null)
    constructor(context: Context, batchData: WinLossBatch): this(context, null, batchData)

    init {
        inflate(context, R.layout.win_loss_batch, this)
        nameText = findViewById(R.id.name)
        dateCreatedText = findViewById(R.id.dateCreated)
        winRateText = findViewById(R.id.winRate)

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
        winRateText.setOnLongClickListener { editModeEnabler(this) ; true }
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

    fun render() {
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
}