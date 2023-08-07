package com.example.lint.checks

import com.android.SdkConstants.ANDROID_URI
import com.android.SdkConstants.ATTR_TARGET_SDK_VERSION
import com.android.SdkConstants.TAG_USES_SDK
import com.android.resources.ResourceFolderType
import com.android.tools.lint.detector.api.*
import org.w3c.dom.Attr
import org.w3c.dom.Document
import org.w3c.dom.Element

/**
 * Detector flagging whether the application has default insecure network connections behavior.
 */
class InsecureConnectionsDetector : ResourceXmlDetector() {

    override fun getApplicableElements() = setOf(ATTR_CLEARTEXT_TRAFFIC_PERMITTED) // TODO

    override fun appliesTo(folderType: ResourceFolderType): Boolean {
        return folderType == ResourceFolderType.XML
    }

    override fun visitDocument(context: XmlContext, document: Document) {
        // Only called if getApplicableElements & getApplicableAttributes are both null
        // TODO
    }

    override fun visitElement(context: XmlContext, element: Element) {
        // Will be visited if getApplicableElements is not null
        // TODO
    }

    override fun visitAttribute(context: XmlContext, attribute: Attr) {
        // Will be visited if getApplicableAttributes
        // TODO
    }

    companion object {
        const val ATTR_CLEARTEXT_TRAFFIC_PERMITTED = "clearTextTrafficPermitted"
        const val TAG_NETWORK_SECURITY = "network-security-config"

        @JvmField
        val IMPLEMENTATION =
            Implementation(InsecureConnectionsDetector::class.java, Scope.RESOURCE_FILE_SCOPE)

        @JvmField
        val CLEARTEXT_TRAFFIC: Issue =
            Issue.create(
                id = "DefaultCleartextTraffic",
                briefDescription = "Application by default permits cleartext traffic",
                explanation =
                """
            Apps targeting SDK versions earlier than 28 trust cleartext traffic by default. 
            The application must explicitly opt out of this in order to only use secure connections.
            """,
                category = Category.SECURITY,
                priority = 5,
                severity = Severity.WARNING,
                moreInfo = "http://goo.gle/DefaultCleartextTraffic",
                implementation = IMPLEMENTATION
            )

        @JvmField
        val TRUSTED_USER_CERTS: Issue =
            Issue.create(
                id = "DefaultTrustedUserCerts",
                briefDescription = "Application by default trusts user-added CA certificates",
                explanation =
                """
        Apps targeting SDK versions earlier than 24 trust user-added CA certificates by default. 
        In practice, it is better to limit the set of trusted CAs so only trusted CAs are used for an app's secure 
        connections.
        """,
                category = Category.SECURITY,
                priority = 3,
                severity = Severity.WARNING,
                moreInfo = "http://goo.gle/DefaultTrustedUserCerts",
                implementation = IMPLEMENTATION
            )
    }
}