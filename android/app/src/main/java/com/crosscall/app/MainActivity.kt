package com.crosscall.app

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.webkit.JavascriptInterface
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
  private lateinit var webView: WebView
  private val PERMISSIONS = arrayOf(
    Manifest.permission.CAMERA,
    Manifest.permission.RECORD_AUDIO,
    Manifest.permission.READ_CONTACTS
  )
  private val REQ = 100
  private val CONTACT_PICK = 200

  @SuppressLint("SetJavaScriptEnabled")
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    webView = WebView(this)
    setContentView(webView)

    webView.settings.apply {
      javaScriptEnabled = true
      domStorageEnabled = true
      mediaPlaybackRequiresUserGesture = false
      allowFileAccess = true
      mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
    }
    webView.addJavascriptInterface(AndroidBridge(), "Android")
    webView.webViewClient = WebViewClient()
    webView.webChromeClient = object : WebChromeClient() {
      override fun onPermissionRequest(request: PermissionRequest) {
        request.grant(request.resources)
      }
    }

    if (hasPermissions()) loadApp() else requestPermissions()
  }

  private fun hasPermissions() = PERMISSIONS.all {
    ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
  }

  private fun requestPermissions() {
    ActivityCompat.requestPermissions(this, PERMISSIONS, REQ)
  }

  override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    loadApp()
  }

  private fun loadApp() {
    webView.loadUrl("https://nickatkinsbgky.github.io/crosscall/")
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    if (requestCode == CONTACT_PICK && resultCode == RESULT_OK && data != null) {
      val uri: Uri? = data.data
      if (uri != null) {
        val cursor = contentResolver.query(uri, arrayOf(ContactsContract.Contacts.DISPLAY_NAME), null, null, null)
        cursor?.use {
          if (it.moveToFirst()) {
            val name = it.getString(0) ?: "Contact"
            webView.evaluateJavascript("window.onContactSelected && window.onContactSelected(" + org.json.JSONObject.quote(name) + ")", null)
          }
        }
      }
    }
  }

  override fun onBackPressed() {
    if (webView.canGoBack()) webView.goBack() else super.onBackPressed()
  }

  inner class AndroidBridge {
    @JavascriptInterface
    fun pickContact() {
      runOnUiThread {
        val intent = Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)
        startActivityForResult(intent, CONTACT_PICK)
      }
    }
  }
}
