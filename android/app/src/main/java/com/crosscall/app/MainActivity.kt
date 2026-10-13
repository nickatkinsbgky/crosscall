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
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
  private lateinit var webView: WebView
  private val PERMISSIONS = arrayOf(
    Manifest.permission.CAMERA,
    Manifest.permission.RECORD_AUDIO,
    Manifest.permission.READ_CONTACTS,
    Manifest.permission.SEND_SMS
  )
  private val REQ = 100

  private val contactPicker = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
    if (result.resultCode == RESULT_OK && result.data != null) {
      val uri: Uri? = result.data?.data
      if (uri != null) {
        var name = "Contact"
        var phone = ""
        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor?.use {
          if (it.moveToFirst()) {
            name = it.getString(it.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY)) ?: "Contact"
            val id = it.getString(it.getColumnIndexOrThrow(ContactsContract.Contacts._ID))
            val hasPhone = it.getInt(it.getColumnIndexOrThrow(ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0
            if (hasPhone) {
              val phones = contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null,
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                arrayOf(id),
                null
              )
              phones?.use { p ->
                if (p.moveToFirst()) {
                  phone = p.getString(p.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER)) ?: ""
                }
              }
            }
          }
        }
        val nameQ = org.json.JSONObject.quote(name)
        val phoneQ = org.json.JSONObject.quote(phone)
        webView.post {
          webView.evaluateJavascript("window.onContactSelected && window.onContactSelected($nameQ, $phoneQ)", null)
        }
      }
    }
  }

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
    webView.addJavascriptInterface(AndroidBridge(this), "Android")
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

  override fun onBackPressed() {
    if (webView.canGoBack()) webView.goBack() else super.onBackPressed()
  }

  class AndroidBridge(private val activity: MainActivity) {
    @JavascriptInterface
    fun pickContact() {
      activity.runOnUiThread {
        try {
          val intent = Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)
          activity.contactPicker.launch(intent)
        } catch (e: Exception) {
          e.printStackTrace()
        }
      }
    }

    @JavascriptInterface
    fun sendSms(phone: String, message: String) {
      activity.runOnUiThread {
        try {
          val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("smsto:$phone")
            putExtra("sms_body", message)
          }
          activity.startActivity(intent)
        } catch (e: Exception) {
          e.printStackTrace()
        }
      }
    }
  }
}
