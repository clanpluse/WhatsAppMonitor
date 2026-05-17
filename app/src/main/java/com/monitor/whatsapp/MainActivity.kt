package com.monitor.whatsapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    private lateinit var keywordsAdapter: KeywordsAdapter
    private val keywordsList = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupRecyclerView()
        loadKeywords()
        setupButtons()
        updatePermissionStatus()
    }

    override fun onResume() {
        super.onResume()
        updatePermissionStatus()
    }

    private fun setupRecyclerView() {
        keywordsAdapter = KeywordsAdapter(keywordsList) { keyword ->
            removeKeyword(keyword)
        }
        findViewById<RecyclerView>(R.id.recyclerKeywords).apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = keywordsAdapter
        }
    }

    private fun setupButtons() {
        val editText = findViewById<EditText>(R.id.editKeyword)
        val btnAdd = findViewById<Button>(R.id.btnAdd)
        val btnPermission = findViewById<Button>(R.id.btnPermission)

        btnAdd.setOnClickListener {
            val keyword = editText.text.toString().trim()
            if (keyword.isNotEmpty() && !keywordsList.contains(keyword)) {
                keywordsList.add(keyword)
                keywordsAdapter.notifyItemInserted(keywordsList.size - 1)
                saveKeywords()
                editText.text.clear()
            }
        }

        btnPermission.setOnClickListener {
            startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
        }
    }

    private fun updatePermissionStatus() {
        val tvStatus = findViewById<TextView>(R.id.tvStatus)
        val btnPermission = findViewById<Button>(R.id.btnPermission)

        if (isNotificationListenerEnabled()) {
            tvStatus.text = "التطبيق يعمل ويراقب الإشعارات"
            tvStatus.setTextColor(getColor(android.R.color.holo_green_dark))
            btnPermission.text = "الإذن مفعّل"
        } else {
            tvStatus.text = "يجب منح إذن قراءة الإشعارات"
            tvStatus.setTextColor(getColor(android.R.color.holo_red_dark))
            btnPermission.text = "منح الإذن"
        }
    }

    private fun isNotificationListenerEnabled(): Boolean {
        val cn = "$packageName/${NotificationMonitorService::class.java.name}"
        val flat = Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
        return flat?.contains(cn) == true
    }

    private fun loadKeywords() {
        val prefs = getSharedPreferences("keywords_prefs", Context.MODE_PRIVATE)
        val saved = prefs.getStringSet("keywords", emptySet()) ?: emptySet()
        keywordsList.clear()
        keywordsList.addAll(saved)
        keywordsAdapter.notifyDataSetChanged()
    }

    private fun saveKeywords() {
        val prefs = getSharedPreferences("keywords_prefs", Context.MODE_PRIVATE)
        prefs.edit().putStringSet("keywords", keywordsList.toSet()).apply()
    }

    private fun removeKeyword(keyword: String) {
        val index = keywordsList.indexOf(keyword)
        if (index != -1) {
            keywordsList.removeAt(index)
            keywordsAdapter.notifyItemRemoved(index)
            saveKeywords()
        }
    }
}
