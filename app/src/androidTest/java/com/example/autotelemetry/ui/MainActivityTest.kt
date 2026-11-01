package com.example.autotelemetry.ui

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.autotelemetry.R
import org.hamcrest.Matchers.containsString
import org.junit.After
import org.junit.Assert.fail
import org.junit.Test
import org.junit.runner.RunWith
import java.io.PrintWriter
import java.net.ServerSocket
import java.util.concurrent.Executors

@RunWith(AndroidJUnit4::class)
class MainActivityTest {
    private var server: ServerSocket? = null
    private var scenario: ActivityScenario<MainActivity>? = null
    private val executor = Executors.newSingleThreadExecutor()

    @After
    fun tearDown() {
        scenario?.close()
        server?.close()
        executor.shutdownNow()
    }

    @Test
    fun telemetry_isDisplayed_andClimateControlWorks() {
        launchWithFrames("100#04D2", "101#57", "102#00EB", "103#00")

        waitForText(R.id.speedText, "123.4 km/h")
        waitForText(R.id.batteryText, "Battery: 87%")

        onView(withId(R.id.climateUpButton)).perform(click())
        onView(withId(R.id.climateTempText)).check(matches(withText("22.5°C")))
    }

    @Test
    fun malformedTelemetry_displaysErrorState() {
        launchWithFrames("100#0064", "BAD_FRAME")
        waitForContainsText(R.id.errorText, "Telemetry parse error")
        onView(withId(R.id.errorText)).check(matches(isDisplayed()))
    }

    private fun launchWithFrames(vararg frames: String) {
        server = ServerSocket(0)
        val port = requireNotNull(server).localPort
        executor.execute {
            runCatching {
                requireNotNull(server).accept().use { socket ->
                    PrintWriter(socket.getOutputStream(), true).use { writer ->
                        frames.forEach { frame ->
                            writer.println(frame)
                            Thread.sleep(80)
                        }
                        Thread.sleep(500)
                    }
                }
            }
        }

        val context = ApplicationProvider.getApplicationContext<Context>()
        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra(MainActivity.EXTRA_TELEMETRY_HOST, "127.0.0.1")
            putExtra(MainActivity.EXTRA_TELEMETRY_PORT, port)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        scenario = ActivityScenario.launch(intent)
    }

    private fun waitForText(viewId: Int, expected: String, timeoutMs: Long = 5_000) {
        waitUntil(timeoutMs) {
            onView(withId(viewId)).check(matches(withText(expected)))
        }
    }

    private fun waitForContainsText(viewId: Int, expected: String, timeoutMs: Long = 5_000) {
        waitUntil(timeoutMs) {
            onView(withId(viewId)).check(matches(withText(containsString(expected))))
        }
    }

    private fun waitUntil(timeoutMs: Long, assertion: () -> Unit) {
        val deadline = System.currentTimeMillis() + timeoutMs
        var lastFailure: Throwable? = null
        while (System.currentTimeMillis() < deadline) {
            try {
                assertion()
                return
            } catch (t: Throwable) {
                lastFailure = t
                Thread.sleep(50)
            }
        }
        fail("Timed out waiting for UI state. Last failure: ${lastFailure?.message}")
    }
}
