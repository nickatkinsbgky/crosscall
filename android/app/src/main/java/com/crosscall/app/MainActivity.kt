package com.crosscall.app

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
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
  private val PERMISSIONS = arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
  private val REQ = 100

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
    if (requestCode == REQ && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) loadApp()
    else loadApp()
  }

  private fun loadApp() {
    webView.loadUrl("https://nickatkinsbgky.github.io/crosscall/")
  }

  override fun onBackPressed() {
    if (webView.canGoBack()) webView.goBack() else super.onBackPressed()
  }
}
