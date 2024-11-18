package com.example.lint.checks

import com.android.tools.lint.checks.infrastructure.LintDetectorTest
import com.android.tools.lint.detector.api.Detector
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class LogInfoDisclosureDetectorTest : LintDetectorTest() {
    override fun getIssues() = listOf(LogcatDetector.LOG_INFO_DISCLOSURE_ISSUE)

    override fun getDetector(): Detector = LogcatDetector()

    @Test
    fun logInfoDisclosureMethod_whenSuspiciousStringPresent_showsWarning() {
        lint().files(
            java(
                """
                import android.app.Activity;
                import android.util.Log;

                public class MainActivity extends Activity {

                    private void testLogs() {
                        Log.wtf("testpasswdtest", "test");
                    }
                }
                """
            ).indented()
        ).run().expect(
            """
            src/MainActivity.java:7: Warning: Sensitive data should never be logged to logcat [LogInfoDisclosure]
                    Log.wtf("testpasswdtest", "test");
                    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
            0 errors, 1 warnings
            """
        )
    }

    @Test
    fun logInfoDisclosureMethod_whenSuspiciousStringPresent_showsQuickFix() {
        lint().files(
            kotlin(
                """
                import android.app.Activity 
                import android.util.Log

                class MainActivity: Activity {

                    private fun testLogs() {
                        Log.v("hiddenkeyinstring", "test")
                    }
                }
                """
            ).indented()
        ).run().expectFixDiffs(
            """
                    Fix for src/MainActivity.kt line 7: Delete:
                    @@ -7 +7
                    -         Log.v("hiddenkeyinstring", "test")
                    """
        )
    }

    @Test
    fun logInfoDisclosureMethod_whenDifferentCasing_showsWarning() {
        lint().files(
            kotlin(
                """
                import android.app.Activity
                import android.util.Log

                class MainActivity: Activity {

                    private fun testLogs() {
                        Log.e("creDenTiAlblahblah", "test")
                    }
                }
                """
            ).indented()
        ).run().expect(
            """
            src/MainActivity.kt:7: Warning: Sensitive data should never be logged to logcat [LogInfoDisclosure]
                    Log.e("creDenTiAlblahblah", "test")
                    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
            0 errors, 1 warnings
        """
        )
    }

    @Test
    fun logInfoDisclosureMethod_whenDifferentCasing_showsQuickFix() {
        lint().files(
            kotlin(
                """
                import android.app.Activity 
                import android.util.Log

                class MainActivity: Activity {

                    private fun testLogs() {
                        Log.v("passWORD", "test")
                    }
                }
                """
            ).indented()
        ).run().expectFixDiffs(
            """
                    Fix for src/MainActivity.kt line 7: Delete:
                    @@ -7 +7
                    -         Log.v("passWORD", "test")
                    """
        )
    }

    @Test
    fun logInfoDisclosureMethod_whenNoSuspiciousStrings_showsNoWarning() {
        lint().files(
            java(
                """
                import android.app.Activity;
                import android.util.Log;
                
                public class MainActivity extends Activity {

                    private void testLogs() {
                        Log.d("testtesttest", "test");
                    }
                }
                """
            ).indented()
        ).run().expectClean()
    }

}