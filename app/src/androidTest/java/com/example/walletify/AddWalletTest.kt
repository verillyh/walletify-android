package com.example.walletify


import android.view.View
import android.view.ViewGroup
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withParent
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf
import org.hamcrest.TypeSafeMatcher
import org.hamcrest.core.IsInstanceOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class AddWalletTest {

    @Rule
    @JvmField
    var mActivityScenarioRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun addWalletTest() {
        val actionMenuItemView = onView(
            allOf(
                withId(R.id.walletMenu), withContentDescription("Wallet Menu"),
                childAtPosition(
                    childAtPosition(
                        withId(R.id.topAppBar),
                        1
                    ),
                    0
                ),
                isDisplayed()
            )
        )
        actionMenuItemView.perform(click())

        val materialButton = onView(
            allOf(
                withId(R.id.add_wallet), withText("Add"),
                childAtPosition(
                    allOf(
                        withId(R.id.wallet_options),
                        childAtPosition(
                            withId(R.id.wallet_options_window),
                            1
                        )
                    ),
                    2
                ),
                isDisplayed()
            )
        )
        materialButton.perform(click())

        val textInputEditText = onView(
            allOf(
                withId(R.id.wallet_input_edit_text),
                childAtPosition(
                    childAtPosition(
                        withId(R.id.wallet_input_layout),
                        0
                    ),
                    0
                ),
                isDisplayed()
            )
        )
        textInputEditText.perform(replaceText("Test"), closeSoftKeyboard())

        val textInputEditText2 = onView(
            allOf(
                withId(R.id.amount_input_edit_text),
                childAtPosition(
                    childAtPosition(
                        withId(R.id.amount_input_layout),
                        0
                    ),
                    0
                ),
                isDisplayed()
            )
        )
        textInputEditText2.perform(replaceText("10"), closeSoftKeyboard())

        val materialButton2 = onView(
            allOf(
                withId(R.id.add_wallet), withText("Add"),
                childAtPosition(
                    childAtPosition(
                        withId(R.id.entry_container),
                        0
                    ),
                    4
                ),
                isDisplayed()
            )
        )
        materialButton2.perform(click())

        val textView = onView(
            allOf(
                withText("Test"),
                withParent(
                    allOf(
                        withId(R.id.topAppBar),
                        withParent(IsInstanceOf.instanceOf(android.widget.LinearLayout::class.java))
                    )
                ),
                isDisplayed()
            )
        )
        textView.check(matches(withText("Test")))

        val textView2 = onView(
            allOf(
                withId(R.id.current_balance), withText("$10.0"),
                withParent(withParent(withId(R.id.main_fragment))),
                isDisplayed()
            )
        )
        textView2.check(matches(withText("$10.0")))

        val linearLayout = onView(
            allOf(
                withParent(
                    allOf(
                        withId(R.id.transaction_recycler_view),
                        withParent(withId(R.id.item_container))
                    )
                ),
                isDisplayed()
            )
        )
        linearLayout.check(matches(isDisplayed()))
    }

    private fun childAtPosition(
        parentMatcher: Matcher<View>, position: Int
    ): Matcher<View> {

        return object : TypeSafeMatcher<View>() {
            override fun describeTo(description: Description) {
                description.appendText("Child at position $position in parent ")
                parentMatcher.describeTo(description)
            }

            public override fun matchesSafely(view: View): Boolean {
                val parent = view.parent
                return parent is ViewGroup && parentMatcher.matches(parent)
                        && view == parent.getChildAt(position)
            }
        }
    }
}
