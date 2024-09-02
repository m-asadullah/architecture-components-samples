/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.example.github.util

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.android.example.github.testing.SingleFragmentActivity
import com.android.example.github.util.DataBindingIdlingResourceTest.TestFragment
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.Assert
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@Ignore("https://github.com/android/architecture-components-samples/issues/1076")
class AutoClearedValueTest {

    @Rule
    @JvmField
    val activityRule = ActivityScenarioRule(SingleFragmentActivity::class.java)

    private lateinit var testFragment: TestFragment

    @Before
    fun init() {
        ActivityScenario.launch(SingleFragmentActivity::class.java).onActivity { activity ->
            testFragment = TestFragment()
            activity.setFragment(testFragment)
        }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
    }

    @Test
    fun clearOnReplace() {
        ActivityScenario.launch(SingleFragmentActivity::class.java).onActivity { activity ->
            activity.replaceFragment(TestFragment())
        }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        try {
            testFragment.testValue
            Assert.fail("should throw if accessed when not set")
        } catch (ex: IllegalStateException) {
            // Expected exception
        }
    }

    @Test
    fun clearOnReplaceBackStack() {
        ActivityScenario.launch(SingleFragmentActivity::class.java).onActivity { activity ->
            activity.replaceFragment(TestFragment(), true)
        }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        try {
            testFragment.testValue
            Assert.fail("should throw if accessed when not set")
        } catch (ex: IllegalStateException) {
            // Expected exception
        }
        ActivityScenario.launch(SingleFragmentActivity::class.java).onActivity { activity ->
            activity.supportFragmentManager.popBackStack()
        }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        assertThat(testFragment.testValue, `is`("foo"))
    }

    @Test
    fun dontClearForChildFragment() {
        testFragment.childFragmentManager.beginTransaction()
            .add(Fragment(), "foo").commit()
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        assertThat(testFragment.testValue, `is`("foo"))
    }

    @Test
    fun dontClearForDialog() {
        val dialogFragment = DialogFragment()
        dialogFragment.show(testFragment.parentFragmentManager, "dialog")
        dialogFragment.dismiss()
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        assertThat(testFragment.testValue, `is`("foo"))
    }

    class TestFragment : Fragment() {
        var testValue by autoCleared<String>()

        override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            testValue = "foo"
            return View(context)
        }
    }
}
