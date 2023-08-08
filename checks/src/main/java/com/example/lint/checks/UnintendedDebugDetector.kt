package com.example.lint.checks

import com.android.SdkConstants.ANDROID_URI
import com.android.SdkConstants.ATTR_DEBUGGABLE
import com.android.SdkConstants.VALUE_FALSE
import com.android.SdkConstants.VALUE_TRUE
import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Incident
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.Severity
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.XmlContext
import com.android.tools.lint.detector.api.XmlScanner
import org.w3c.dom.Attr
import org.w3c.dom.Element

/**
 * Detector flagging whether the application has unintended debug functionality enabled.
 */
class UnintendedDebugDetector: Detector(), XmlScanner {

    override fun getApplicableAttributes() = setOf(ATTR_DEBUGGABLE)

    override fun visitAttribute(context: XmlContext, attribute: Attr) {
        val debuggable = attribute.value
        if (debuggable == VALUE_TRUE) {
            val incident =
                Incident(
                    ISSUE,
                    attribute,
                    context.getValueLocation(attribute),
                    "Setting the application's `android:debuggable` attribute to true exposes the application" +
                            " to greater risk",
                    fix().set().android().attribute(ATTR_DEBUGGABLE).value(VALUE_FALSE).build()
                )

            context.report(incident)
        }
    }
    companion object {
        @JvmField
        val ISSUE: Issue =
            Issue.create(
                id = "UnintendedDebugFlag",
                briefDescription = "Application has `android:debuggable` set to true",
                explanation =
                """
                    While allowing an application to be debuggable is in itself not a vulnerability, it exposes the 
                    application to greater risk through unintended and unauthorized access to administrative functions.
                    
                    This can allow attackers more access to the application and resources used by the application than
                    intended.
                    
                    We recommend setting `android:debuggable` to false unless you have a good reason to keep it as true.
                    """,
                category = Category.SECURITY,
                priority = 6,
                severity = Severity.WARNING,
                moreInfo = "http://goo.gle/UnintendedDebugFlag",
                implementation =
                Implementation(UnintendedDebugDetector::class.java, Scope.MANIFEST_SCOPE)
            )
    }
}