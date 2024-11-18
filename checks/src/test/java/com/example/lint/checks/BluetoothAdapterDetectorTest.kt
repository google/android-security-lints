package com.example.lint.checks

import com.android.tools.lint.checks.infrastructure.LintDetectorTest
import com.android.tools.lint.detector.api.Detector
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class BluetoothAdapterDetectorTest : LintDetectorTest() {
    override fun getIssues() =
        mutableListOf(
            BluetoothAdapterDetector.ZERO_BLUETOOTH_DISCOVERY_DURATION_ISSUE,
            BluetoothAdapterDetector.EXTENDED_BLUETOOTH_DISCOVERY_DURATION_ISSUE
        )

    override fun getDetector(): Detector = BluetoothAdapterDetector()

    @Test
    fun extraDiscoverableDurationIsZero_showsWarning() {
        lint().files(
            java(
                """import android.app.Activity;
                    import android.bluetooth.BluetoothAdapter;
                    import android.content.Intent;

                    public class MainActivity extends Activity {
                        private Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE).putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0);
}"""
            ).indented()
        ).run().expect(
            """src/MainActivity.java:6: Error: The EXTRA_DISCOVERABLE_DURATION time should never be set to zero [ZeroBluetoothDiscoveryDuration]
                        private Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE).putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0);
                                                            ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
1 errors, 0 warnings""".trimIndent()
        )
    }

    @Test
    fun extraDiscoverableDurationIsZero_showsQuickFix() {
        lint().files(
            java(
                """import android.app.Activity;
                    import android.bluetooth.BluetoothAdapter;
                    import android.content.Intent;

                    public class MainActivity extends Activity {
                        private Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE).putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0);
}"""
            ).indented()
        ).run().expectFixDiffs(
            // Unfortunately, the semi-colon will be left behind due to limitations with the lint API
            // needing to also account for chained calls, and it being impossible to fully detect the
            // entire relevant code block to delete.
            """Fix for src/MainActivity.java line 6: Replace with 120:
@@ -6 +6
-                         private Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE).putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0);
+                         private Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE).putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 120);"""
        )
    }

    @Test
    fun extraDiscoverableDurationIsNonZero_showsNoWarning() {
        lint().files(
            java(
                """import android.app.Activity;
                    import android.bluetooth.BluetoothAdapter;
                    import android.content.Intent;

                    public class MainActivity extends Activity {
                        private Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE).putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 1);
}"""
            ).indented()
        ).run().expectClean()
    }

    @Test
    fun extraDiscoverableDurationMoreThan120Seconds_showsWarning() {
        lint().files(
            java(
                """import android.app.Activity;
                    import android.bluetooth.BluetoothAdapter;
                    import android.content.Intent;

                    public class MainActivity extends Activity {
                        private Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE).putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 121);
}"""
            ).indented()
        ).run().expect(
            """src/MainActivity.java:6: Warning: The EXTRA_DISCOVERABLE_DURATION time should be set to a shorter amount of time [ExtendedBluetoothDiscoveryDuration]
                        private Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE).putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 121);
                                                            ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
0 errors, 1 warnings""".trimIndent()
        )
    }

    @Test
    fun extraDiscoverableDurationMoreThan120Seconds_showsQuickFix() {
        lint().files(
            java(
                """import android.app.Activity;
                    import android.bluetooth.BluetoothAdapter;
                    import android.content.Intent;

                    public class MainActivity extends Activity {
                        private Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE).putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 121);
}"""
            ).indented()
        ).run().expectFixDiffs(
            // Unfortunately, the semi-colon will be left behind due to limitations with the lint API
            // needing to also account for chained calls, and it being impossible to fully detect the
            // entire relevant code block to delete.
            """Fix for src/MainActivity.java line 6: Replace with 120:
@@ -6 +6
-                         private Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE).putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 121);
+                         private Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE).putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 120);"""
        )
    }

    @Test
    fun extraDiscoverableDurationIs120Seconds_showsNoWarning() {
        lint().files(
            java(
                """import android.app.Activity;
                    import android.bluetooth.BluetoothAdapter;
                    import android.content.Intent;

                    public class MainActivity extends Activity {
                        private Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE).putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 120);
}"""
            ).indented()
        ).run().expectClean()
    }

}