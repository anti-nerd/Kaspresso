package com.kaspersky.kaspressample.device_tests

import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import com.kaspersky.kaspressample.MainActivity
import com.kaspersky.kaspresso.device.apps.Apps
import com.kaspersky.kaspresso.device.server.AdbServer
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Installs and then uninstalls an apk file placed at /artifacts directory.
 * [Apps.install] uses the [TEST_APK_FILE_RELATIVE_PATH] relative path to install the apk.
 * So, you should run the server with command `cd /absolute/path/to/project/directory & java -jar artifacts/desktop.jar`
 */
@RunWith(AndroidJUnit4::class)
class DeviceAppSampleTest : TestCase() {

    companion object {

        /**
         * Compiled 'Hello, World' project, auto-generated by Android Studio.
         */
        private const val TEST_APK_FILE_RELATIVE_PATH = "artifacts/hello_world.apk"
        private const val TEST_APK_PACKAGE_NAME = "com.example.helloworld"
    }

    @get:Rule
    val activityTestRule = ActivityTestRule(MainActivity::class.java, true, true)

    @Test
    fun test() {
        before {
        }.after {
        }.run {

            step("Install hello world apk") {
                device.apps.install(TEST_APK_FILE_RELATIVE_PATH)
                assertTrue(isAppInstalled(TEST_APK_PACKAGE_NAME))
            }

            step("Delete the application") {
                device.apps.uninstall(TEST_APK_PACKAGE_NAME)
                assertFalse(isAppInstalled(TEST_APK_PACKAGE_NAME))
            }
        }
    }

    private fun isAppInstalled(packageName: String): Boolean =
        "package:$packageName" in AdbServer.performShell("pm list packages $packageName").first()
}