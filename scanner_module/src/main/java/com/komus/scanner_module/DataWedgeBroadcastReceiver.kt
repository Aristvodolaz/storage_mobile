package com.komus.scanner_module

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class DataWedgeBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val barcodeData = intent.getStringExtra("com.symbol.datawedge.data_string")
        if (!barcodeData.isNullOrEmpty()) {
            Log.d("DataWedgeReceiver", "Received barcode: $barcodeData")
        }
    }
}
