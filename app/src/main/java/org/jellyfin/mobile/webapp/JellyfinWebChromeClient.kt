package org.jellyfin.mobile.webapp

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Message
import android.util.Log
import android.webkit.ConsoleMessage
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import org.jellyfin.mobile.utils.applyDefault
import timber.log.Timber

class JellyfinWebChromeClient(
    private val fileChooserListener: FileChooserListener,
    private val newWindowListener: NewWindowListener,
) : WebChromeClient() {
    override fun onConsoleMessage(consoleMessage: ConsoleMessage): Boolean {
        val logLevel = when (consoleMessage.messageLevel()) {
            ConsoleMessage.MessageLevel.ERROR -> Log.ERROR
            ConsoleMessage.MessageLevel.WARNING -> Log.WARN
            ConsoleMessage.MessageLevel.DEBUG -> Log.DEBUG
            ConsoleMessage.MessageLevel.TIP -> Log.VERBOSE
            else -> Log.INFO
        }

        Timber.tag("WebView").log(
            logLevel,
            "%s, %s (%d)",
            consoleMessage.message(),
            consoleMessage.sourceId(),
            consoleMessage.lineNumber(),
        )

        return true
    }

    override fun onShowFileChooser(
        webView: WebView,
        filePathCallback: ValueCallback<Array<Uri>>,
        fileChooserParams: FileChooserParams?,
    ): Boolean {
        if (fileChooserParams == null) {
            filePathCallback.onReceiveValue(null)
            return true
        }

        fileChooserListener.onShowFileChooser(fileChooserParams.createIntent(), filePathCallback)
        return true
    }

    override fun onCreateWindow(
        view: WebView,
        isDialog: Boolean,
        isUserGesture: Boolean,
        resultMsg: Message,
    ): Boolean {
        val popupWebView = WebView(view.context).apply {
            settings.applyDefault()
            webViewClient = object : WebViewClient() {
                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    url?.toUriOrNull()?.let { uri ->
                        val handled = newWindowListener.onOpenNewWindow(uri)
                        if (handled) {
                            view?.stopLoading()
                            view?.destroy()
                        }
                    }
                }

                override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                    val uri = request?.url ?: return false
                    val handled = newWindowListener.onOpenNewWindow(uri)
                    if (handled) view?.destroy()
                    return handled
                }
            }
        }
        val transport = resultMsg.obj as WebView.WebViewTransport
        transport.webView = popupWebView
        resultMsg.sendToTarget()
        return true
    }

    private fun String.toUriOrNull(): Uri? = runCatching { Uri.parse(this) }.getOrNull()

    interface FileChooserListener {
        fun onShowFileChooser(intent: Intent, filePathCallback: ValueCallback<Array<Uri>>)
    }

    interface NewWindowListener {
        fun onOpenNewWindow(uri: Uri): Boolean
    }
}
