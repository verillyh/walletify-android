package com.example.walletify


import android.view.View
import android.view.ViewGroup
import androidx.test.espresso.Espresso.onData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.*
import androidx.test.espresso.matcher.RootMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.anything
import org.hamcrest.Matchers.`is`
import org.hamcrest.TypeSafeMatcher
import org.hamcrest.core.IsInstanceOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class TransferWalletTest {

    @Rule
    @JvmField
    var mActivityScenarioRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun transferWalletTest() {
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
        textInputEditText.perform(replaceText("Test Wallet 2"), closeSoftKeyboard())

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

        val textInputEditText3 = onView(
            allOf(
                withId(R.id.amount_input_edit_text), withText("10"),
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
        textInputEditText3.perform(pressImeActionButton())

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

        val actionMenuItemView2 = onView(
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
        actionMenuItemView2.perform(click())

        val materialButton3 = onView(
            allOf(
                withId(R.id.transfer_wallet), withText("Transfer"),
                childAtPosition(
                    allOf(
                        withId(R.id.wallet_options),
                        childAtPosition(
                            withId(R.id.wallet_options_window),
                            1
                        )
                    ),
                    1
                ),
                isDisplayed()
            )
        )
        materialButton3.perform(click())

        val textInputEditText4 = onView(
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
        textInputEditText4.perform(replaceText("3"), closeSoftKeyboard())

        val materialAutoCompleteTextView = onView(
            allOf(
                withId(R.id.to_wallet_dropdown),
                childAtPosition(
                    childAtPosition(
                        withId(R.id.to_wallet_input_layout),
                        0
                    ),
                    0
                ),
                isDisplayed()
            )
        )
        materialAutoCompleteTextView.perform(click())

        val materialTextView = onData(anything())
            .atPosition(0)
            .inRoot(RootMatchers.isPlatformPopup())
        materialTextView.perform(click())

        val materialButton4 = onView(
            allOf(
                withId(R.id.transfer_wallet_add), withText("Transfer"),
                childAtPosition(
                    childAtPosition(
                        withId(R.id.entry_container),
                        0
                    ),
                    6
                ),
                isDisplayed()
            )
        )
        materialButton4.perform(click())

        val textView = onView(
            allOf(
                withId(R.id.current_balance), withText("$7.0"),
                withParent(withParent(withId(R.id.main_fragment))),
                isDisplayed()
            )
        )
        textView.check(matches(withText("$7.0")))

        val textView2 = onView(
            allOf(
                withId(R.id.current_month_expense), withText("$3.0"),
                withParent(withParent(IsInstanceOf.instanceOf(android.widget.LinearLayout::class.java))),
                isDisplayed()
            )
        )
        textView2.check(matches(withText("$3.0")))

        val textView3 = onView(
            allOf(
                withId(R.id.current_month_expense), withText("$3.0"),
                withParent(withParent(IsInstanceOf.instanceOf(android.widget.LinearLayout::class.java))),
                isDisplayed()
            )
        )
        textView3.check(matches(withText("$3.0")))
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
