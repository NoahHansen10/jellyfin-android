package org.jellyfin.mobile.webapp

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue

class WebViewErrorPolicyTest : FunSpec({
    test("reports main-frame errors before the web app connects") {
        WebViewErrorPolicy.shouldReportConnectionError(
            isForMainFrame = true,
            connectedToWebapp = false,
        ).shouldBeTrue()
    }

    test("ignores subresource errors before the web app connects") {
        WebViewErrorPolicy.shouldReportConnectionError(
            isForMainFrame = false,
            connectedToWebapp = false,
        ).shouldBeFalse()
    }

    test("ignores main-frame errors after the web app connects") {
        WebViewErrorPolicy.shouldReportConnectionError(
            isForMainFrame = true,
            connectedToWebapp = true,
        ).shouldBeFalse()
    }
})
