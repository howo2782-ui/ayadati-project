package com.example

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.print.PrintAttributes
import android.print.PrintManager
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .safeDrawingPadding()
            .testTag("clinic_main_container")
        ) {
          ClinicWebView()
        }
      }
    }
  }
}

class WebAppInterface(private val context: Context, private val webViewProvider: () -> WebView?) {
  @JavascriptInterface
  fun printPage() {
    val webView = webViewProvider() ?: return
    val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager ?: return
    val printAdapter = webView.createPrintDocumentAdapter("ClinicTicket")
    val jobName = "Clinic Ticket Print"
    printManager.print(jobName, printAdapter, PrintAttributes.Builder().build())
  }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun ClinicWebView() {
  val context = LocalContext.current
  var webViewInstance: WebView? = remember { null }

  BackHandler(enabled = true) {
    if (webViewInstance?.canGoBack() == true) {
      webViewInstance?.goBack()
    }
  }

  AndroidView(
    modifier = Modifier
      .fillMaxSize()
      .testTag("clinic_webview"),
    factory = { ctx ->
      WebView(ctx).apply {
        layoutParams = ViewGroup.LayoutParams(
          ViewGroup.LayoutParams.MATCH_PARENT,
          ViewGroup.LayoutParams.MATCH_PARENT
        )
        settings.apply {
          javaScriptEnabled = true
          domStorageEnabled = true
          databaseEnabled = true
          allowFileAccess = true
          allowContentAccess = true
          useWideViewPort = true
          loadWithOverviewMode = true
          cacheMode = WebSettings.LOAD_DEFAULT
          mediaPlaybackRequiresUserGesture = false
        }

        webChromeClient = object : WebChromeClient() {}
        webViewClient = object : WebViewClient() {
          override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
            return false
          }
        }

        addJavascriptInterface(WebAppInterface(ctx) { this }, "AndroidPrint")
        loadUrl("file:///android_asset/clinic.html")
        webViewInstance = this
      }
    },
    update = { webView ->
      webViewInstance = webView
    }
  )
}
