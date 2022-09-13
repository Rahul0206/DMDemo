package com.example.bluetooth_connection

import android.content.Context
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.platform.app.InstrumentationRegistry
import com.example.bluetooth_connection.activity.MainActivity
import com.google.common.truth.Truth
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4ClassRunner::class)
class MainActivityTest {

    @get:Rule
    var mActivityRule: ActivityScenarioRule<MainActivity> =
        ActivityScenarioRule(MainActivity::class.java)

    lateinit var scenario: ActivityScenario<MainActivity>

    var context: Context = ApplicationProvider.getApplicationContext()

    @Before
    fun launchActivity() {
        scenario = ActivityScenario.launch(MainActivity::class.java)
        scenario.moveToState(Lifecycle.State.RESUMED)
        // Press Scanning button.

        Espresso.onView(ViewMatchers.withId(R.id.scan_btn)).perform(ViewActions.click())
    }


    @Test
    fun ensureDeviceExist() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            scenario.onActivity {
                Truth.assertThat(it.checkDeviceData()).isTrue()
            }
        }
    }

    @Test
    fun checkPermission() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            scenario.onActivity {
                Truth.assertThat(it.checkPermission()).isTrue()
            }
        }
    }


    @Test
    fun recyclerViewInitialize() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            scenario.onActivity {
                Truth.assertThat(it.checkRecyclerViewInitialize()).isTrue()
            }
        }
    }

    @Test
    fun checkAdapter() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            scenario.onActivity {
                Truth.assertThat(it.checkAdapter()).isTrue()
            }
        }
    }


//    @After
//    @Throws(Exception::class)
//    fun tearDown() {
//        scenario.close()
//    }

}