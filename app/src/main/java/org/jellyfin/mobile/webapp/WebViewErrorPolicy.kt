package org.jellyfin.mobile.webapp

internal object WebViewErrorPolicy {
    fun shouldReportConnectionError(
        isForMainFrame: Boolean,
        connectedToWebapp: Boolean,
    ): Boolean = isForMainFrame && !connectedToWebapp
}
