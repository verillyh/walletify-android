package com.example.walletify


import android.view.View
import android.view.ViewGroup
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withClassName
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
import org.hamcrest.Matchers.`is`
import org.hamcrest.TypeSafeMatcher
import org.hamcrest.core.IsInstanceOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class UserSignupLoginTest {

    @Rule
    @JvmField
    var mActivityScenarioRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun userSignupLoginTest() {
        val bottomNavigationItemView = onView(
            allOf(
                withId(R.id.profile), withContentDescription("Profile"),
                childAtPosition(
                    childAtPosition(
                        withId(R.id.bottom_navigation),
                        0
                    ),
                    1
                ),
                isDisplayed()
            )
        )
        bottomNavigationItemView.perform(click())

        val materialTextView = onView(
            allOf(
                withId(R.id.redirect_to_signup), withText("Don't have an account? Sign up"),
                childAtPosition(
                    childAtPosition(
                        withId(R.id.main_fragment),
                        0
                    ),
                    5
                ),
                isDisplayed()
            )
        )
        materialTextView.perform(click())

        val textInputEditText = onView(
            allOf(
                withId(R.id.email_input_edit_text),
                childAtPosition(
                    childAtPosition(
                        withId(R.id.email_input_layout),
                        0
                    ),
                    0
                ),
                isDisplayed()
            )
        )
        textInputEditText.perform(replaceText("test@gmail.com"), closeSoftKeyboard())

        val textInputEditText2 = onView(
            allOf(
                withId(R.id.phone_number_edit_text),
                childAtPosition(
                    childAtPosition(
                        withId(R.id.phone_number_input_layout),
                        0
                    ),
                    0
                ),
                isDisplayed()
            )
        )
        textInputEditText2.perform(replaceText("0401234567"), closeSoftKeyboard())

        val textInputEditText3 = onView(
            allOf(
                withId(R.id.full_name_input_edit_text),
                childAtPosition(
                    childAtPosition(
                        withId(R.id.full_name_input_layout),
                        0
                    ),
                    0
                ),
                isDisplayed()
            )
        )
        textInputEditText3.perform(replaceText("Test user"), closeSoftKeyboard())

        val textInputEditText4 = onView(
            allOf(
                withId(R.id.password_input_edit_text),
                childAtPosition(
                    childAtPosition(
                        withId(R.id.password_input_layout),
                        0
                    ),
                    0
                ),
                isDisplayed()
            )
        )
        textInputEditText4.perform(replaceText("Test12345!"), closeSoftKeyboard())

        val textInputEditText5 = onView(
            allOf(
                withId(R.id.confirm_password_input_edit_text),
                childAtPosition(
                    childAtPosition(
                        withId(R.id.confirm_password_input_layout),
                        0
                    ),
                    0
                ),
                isDisplayed()
            )
        )
        textInputEditText5.perform(replaceText("Test12345!"), closeSoftKeyboard())

        val materialButton = onView(
            allOf(
                withId(R.id.signup_button), withText("Sign Up"),
                childAtPosition(
                    childAtPosition(
                        withId(R.id.main_fragment),
                        0
                    ),
                    11
                ),
                isDisplayed()
            )
        )
        materialButton.perform(click())

        val textInputEditText6 = onView(
            allOf(
                withId(R.id.email_input_edit_text),
                childAtPosition(
                    childAtPosition(
                        withId(R.id.email_input_layout),
                        0
                    ),
                    0
                ),
                isDisplayed()
            )
        )
        textInputEditText6.perform(replaceText("test@gmail.com"), closeSoftKeyboard())

        val textInputEditText7 = onView(
            allOf(
                withId(R.id.password_input_edit_text),
                childAtPosition(
                    childAtPosition(
                        withId(R.id.password_input_layout),
                        0
                    ),
                    0
                ),
                isDisplayed()
            )
        )
        textInputEditText7.perform(replaceText("Test123"), closeSoftKeyboard())

        val materialButton2 = onView(
            allOf(
                withId(R.id.login_button), withText("Login"),
                childAtPosition(
                    childAtPosition(
                        withId(R.id.main_fragment),
                        0
                    ),
                    6
                ),
                isDisplayed()
            )
        )
        materialButton2.perform(click())

        val checkableImageButton = onView(
            allOf(
                withId(com.google.android.material.R.id.text_input_end_icon),
                withContentDescription("Show password"),
                childAtPosition(
                    childAtPosition(
                        withClassName(`is`("com.google.android.material.textfield.EndCompoundLayout")),
                        1
                    ),
                    0
                ),
                isDisplayed()
            )
        )
        checkableImageButton.perform(click())

        val textInputEditText8 = onView(
            allOf(
                withId(R.id.password_input_edit_text), withText("Test123"),
                childAtPosition(
                    childAtPosition(
                        withId(R.id.password_input_layout),
                        0
                    ),
                    0
                ),
                isDisplayed()
            )
        )
        textInputEditText8.perform(click())

        val textInputEditText9 = onView(
            allOf(
                withId(R.id.password_input_edit_text), withText("Test123"),
                childAtPosition(
                    childAtPosition(
                        withId(R.id.password_input_layout),
                        0
                    ),
                    0
                ),
                isDisplayed()
            )
        )
        textInputEditText9.perform(click())

        val textInputEditText10 = onView(
            allOf(
                withId(R.id.password_input_edit_text), withText("Test123"),
                childAtPosition(
                    childAtPosition(
                        withId(R.id.password_input_layout),
                        0
                    ),
                    0
                ),
                isDisplayed()
            )
        )
        textInputEditText10.perform(replaceText("Test12345!"))

        val textInputEditText11 = onView(
            allOf(
                withId(R.id.password_input_edit_text), withText("Test12345!"),
                childAtPosition(
                    childAtPosition(
                        withId(R.id.password_input_layout),
                        0
                    ),
                    0
                ),
                isDisplayed()
            )
        )
        textInputEditText11.perform(closeSoftKeyboard())

        val materialButton3 = onView(
            allOf(
                withId(R.id.login_button), withText("Login"),
                childAtPosition(
                    childAtPosition(
                        withId(R.id.main_fragment),
                        0
                    ),
                    6
                ),
                isDisplayed()
            )
        )
        materialButton3.perform(click())

        val linearLayout = onView(
            allOf(
                withParent(
                    allOf(
                        withId(R.id.main_fragment),
                        withParent(withId(R.id.main_fragment))
                    )
                ),
                isDisplayed()
            )
        )
        linearLayout.check(matches(isDisplayed()))

        val textView = onView(
            allOf(
                withText("Profile"),
                withParent(
                    allOf(
                        withId(R.id.topAppBar),
                        withParent(IsInstanceOf.instanceOf(android.widget.LinearLayout::class.java))
                    )
                ),
                isDisplayed()
            )
        )
        textView.check(matches(withText("Profile")))
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
