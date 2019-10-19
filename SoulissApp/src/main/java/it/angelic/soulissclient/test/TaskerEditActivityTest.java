/*
 * Copyright 2013 two forty four a.m. LLC <http://www.twofortyfouram.com>
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * <http://www.apache.org/licenses/LICENSE-2.0>
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package it.angelic.soulissclient.test;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.test.ActivityInstrumentationTestCase2;
import android.test.UiThreadTest;
import android.test.suitebuilder.annotation.MediumTest;
import android.test.suitebuilder.annotation.SmallTest;
import android.text.TextUtils;
import android.widget.EditText;

import com.twofortyfouram.locale.BreadCrumber;

import java.lang.reflect.Field;

import it.angelic.bundle.PluginBundleManager;
import it.angelic.soulissclient.R;
import it.angelic.soulissclient.TaskerEditActivity;

/**
 * Tests the {@link TaskerEditActivity}.
 */
public final class TaskerEditActivityTest extends ActivityInstrumentationTestCase2<TaskerEditActivity> {
    /**
     * Context of the target application. This is initialized in {@link #setUp()}.
     */
    private Context mTargetContext;

    /**
     * Instrumentation for the test. This is initialized in {@link #setUp()}.
     */
    private Instrumentation mInstrumentation;

    /**
     * Constructor for the test class; required by Android.
     */
    public TaskerEditActivityTest() {
        super(TaskerEditActivity.class);
    }

    /**
     * Setup that executes before every test case
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();

        mInstrumentation = getInstrumentation();
        mTargetContext = mInstrumentation.getTargetContext();

        /*
         * Perform test case specific initialization. This is required to be set up here because
         * setActivityIntent has no effect inside a method annotated with @UiThreadTest
         */
        if ("testNewSettingCancel".equals(getName())) //$NON-NLS-1$
        {
            setActivityIntent(new Intent(com.twofortyfouram.locale.Intent.ACTION_EDIT_SETTING).putExtra(com.twofortyfouram.locale.Intent.EXTRA_STRING_BREADCRUMB, "Locale > Edit Situation")); //$NON-NLS-1$
        } else if ("testNewSettingSave".equals(getName())) //$NON-NLS-1$
        {
            setActivityIntent(new Intent(com.twofortyfouram.locale.Intent.ACTION_EDIT_SETTING).putExtra(com.twofortyfouram.locale.Intent.EXTRA_STRING_BREADCRUMB, "Locale > Edit Situation")); //$NON-NLS-1$
        } else if ("testOldSetting".equals(getName())) //$NON-NLS-1$
        {
            final Bundle bundle = PluginBundleManager.generateBundle(mTargetContext, "I am a toast message!"); //$NON-NLS-1$

            setActivityIntent(new Intent(com.twofortyfouram.locale.Intent.ACTION_EDIT_SETTING).putExtra(com.twofortyfouram.locale.Intent.EXTRA_STRING_BREADCRUMB, "Locale > Edit Situation").putExtra(com.twofortyfouram.locale.Intent.EXTRA_BUNDLE, bundle)); //$NON-NLS-1$
        } else if ("testOldSetting_screen_rotation".equals(getName())) //$NON-NLS-1$
        {
            final Bundle bundle = PluginBundleManager.generateBundle(mTargetContext, "I am a toast message!"); //$NON-NLS-1$

            setActivityIntent(new Intent(com.twofortyfouram.locale.Intent.ACTION_EDIT_SETTING).putExtra(com.twofortyfouram.locale.Intent.EXTRA_STRING_BREADCRUMB, "Locale > Edit Situation").putExtra(com.twofortyfouram.locale.Intent.EXTRA_BUNDLE, bundle)); //$NON-NLS-1$
        } else if ("testMissingBreadcrumb".equals(getName())) //$NON-NLS-1$
        {
            setActivityIntent(new Intent(com.twofortyfouram.locale.Intent.ACTION_EDIT_SETTING));
        } else if ("testBadBundle".equals(getName())) //$NON-NLS-1$
        {
            final Bundle bundle = PluginBundleManager.generateBundle(mTargetContext, "I am a toast message!"); //$NON-NLS-1$
            bundle.putString(PluginBundleManager.BUNDLE_EXTRA_STRING_MESSAGE, null);

            setActivityIntent(new Intent(com.twofortyfouram.locale.Intent.ACTION_EDIT_SETTING).putExtra(com.twofortyfouram.locale.Intent.EXTRA_STRING_BREADCRUMB, "Locale > Edit Situation").putExtra(com.twofortyfouram.locale.Intent.EXTRA_BUNDLE, bundle)); //$NON-NLS-1$
        }
    }

    /**
     * Verifies the Activity class name hasn't been accidentally changed.
     */
    @SmallTest
    public static void testActivityName() {
        /*
         * NOTE: This test is expected to fail initially when you are adapting this example to your own
         * plug-in. Once you've settled on a name for your Activity, go ahead and update this test case.
         * 
         * The goal of this test case is to prevent accidental renaming of the Activity. Once a plug-in is
         * published to the app store, the Activity shouldn't be renamed because that will break the plug-in
         * for users who had the old version of the plug-in. If you ever find yourself really needing to
         * rename the Activity after the plug-in has been published, take a look at using an activity-alias
         * entry in the Android Manifest.
         */

        assertEquals("it.angelic.soulissclient.TaskerEditActivity", TaskerEditActivity.class.getName()); //$NON-NLS-1$
    }

    /**
     */
    @MediumTest
    public void testGetBlurb() {
        assertEquals("Foo", TaskerEditActivity.generateBlurb(mTargetContext, "Foo")); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * Tests creation of a new setting, that the UI is initialized to the right state, and that the Activity
     * result is correct if the user doesn't input anything.
     */
    @MediumTest
    @UiThreadTest
    public void testNewSettingCancel() throws Throwable {
        final Activity activity = getActivity();

        assertTitle();

        assertMessageAutoSync(""); //$NON-NLS-1$
        assertHintAutoSync(mTargetContext.getString(R.string.manual_cmd_hint));

        activity.finish();

        assertEquals(Activity.RESULT_CANCELED, getActivityResultCode(activity));
    }

    /**
     * Tests editing a new condition with screen rotations.
     */
    @MediumTest
    public void testNewSetting_screen_rotation() throws Throwable {
        /*
         * At this point, nothing is selected. Rotate the screen to make sure that this state is preserved.
         */
        assertMessageAutoSync(""); //$NON-NLS-1$
        setActivityOrientationSync(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setActivityOrientationSync(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setActivityOrientationSync(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        assertMessageAutoSync(""); //$NON-NLS-1$

        /*
         * Select something and rotate the screen to make sure that this state is preserved.
         */
        setMessageAutoSync("foo"); //$NON-NLS-1$
        assertMessageAutoSync("foo"); //$NON-NLS-1$
        setActivityOrientationSync(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setActivityOrientationSync(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setActivityOrientationSync(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        assertMessageAutoSync("foo"); //$NON-NLS-1$

        /*
         * Select something and rotate the screen to make sure that this state is preserved.
         */
        setMessageAutoSync("bar"); //$NON-NLS-1$
        assertMessageAutoSync("bar"); //$NON-NLS-1$
        setActivityOrientationSync(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setActivityOrientationSync(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setActivityOrientationSync(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        assertMessageAutoSync("bar"); //$NON-NLS-1$
    }

    /**
     * Tests creation of a new setting, that the UI is initialized to the right state, and that changes are
     * properly saved
     */
    @MediumTest
    @UiThreadTest
    public void testNewSettingSave() throws Throwable {
        final Activity activity = getActivity();

        assertTitle();

        assertMessageAutoSync(""); //$NON-NLS-1$
        assertHintAutoSync(mTargetContext.getString(R.string.manual_cmd_hint));

        setMessageAutoSync(getName());

        activity.finish();

        assertActivityResultAutoSync(getName());
    }

    /**
     * Tests editing an old setting, that the UI is initialized to the right state, and that the Activity
     * result is correct.
     */
    @MediumTest
    @UiThreadTest
    public void testOldSetting() throws Throwable {
        final Activity activity = getActivity();

        assertTitle();

        assertMessageAutoSync("I am a toast message!"); //$NON-NLS-1$

        activity.finish();

        assertActivityResultAutoSync("I am a toast message!"); //$NON-NLS-1$
    }

    /**
     * Tests editing an old setting with screen rotations.
     */
    @MediumTest
    public void testOldSetting_screen_rotation() throws Throwable {
        /*
         * Make sure that the initial state is preserved.
         */
        assertMessageAutoSync("I am a toast message!"); //$NON-NLS-1$
        setActivityOrientationSync(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setActivityOrientationSync(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setActivityOrientationSync(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        assertMessageAutoSync("I am a toast message!"); //$NON-NLS-1$

        /*
         * Select something and rotate the screen to make sure that this state is preserved.
         */
        setMessageAutoSync("foo"); //$NON-NLS-1$
        assertMessageAutoSync("foo"); //$NON-NLS-1$
        setActivityOrientationSync(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setActivityOrientationSync(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setActivityOrientationSync(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        assertMessageAutoSync("foo"); //$NON-NLS-1$
    }

    /**
     * Verifies the Activity properly handles a missing breadcrumb.
     */
    @MediumTest
    @UiThreadTest
    public void testMissingBreadcrumb() {
        /*
         * Not much to do here, the work was done in setUp. If the Activity fails to load, then this test case
         * will fail.
         */

        assertTitle();
    }

    /**
     * Verifies the Activity properly handles a bundle with a bad value embedded in it.
     */
    @MediumTest
    @UiThreadTest
    public void testBadBundle() throws Throwable {
        final Activity activity = getActivity();

        assertTitle();

        assertMessageAutoSync(""); //$NON-NLS-1$
        assertHintAutoSync(mTargetContext.getString(R.string.manual_cmd_hint));

        activity.finish();
        assertEquals(Activity.RESULT_CANCELED, getActivityResultCode(activity));
    }

    /**
     * Asserts the Activity title equals expected values.
     */
    private void assertTitle() {
        final CharSequence expected = BreadCrumber.generateBreadcrumb(mTargetContext, getActivity().getIntent(), mTargetContext.getString(R.string.souliss_app_name));
        final CharSequence actual = Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ? getTitleHoneycomb()
                : getActivity().getTitle();

        assertEquals(expected, actual);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private CharSequence getTitleHoneycomb() {
        return getActivity().getActionBar().getSubtitle();
    }

    /**
     * Asserts the Activity result contains the expected values for the given display state.
     *
     * @param message The message the plug-in is supposed to show.
     */
    private void assertActivityResultAutoSync(final String message) throws Throwable {
        final Activity activity = getActivity();

        final Runnable runnable = new Runnable() {
            public void run() {
                /*
                 * Verify finishing with text entry is saved
                 */
                activity.finish();

                assertEquals(Activity.RESULT_OK, getActivityResultCode(activity));

                final Intent result = getActivityResultData(activity);
                assertNotNull(result);

                final Bundle extras = result.getExtras();
                assertNotNull(extras);
                assertEquals(String.format("Extras should only contain %s and %s but actually contain %s", com.twofortyfouram.locale.Intent.EXTRA_BUNDLE, com.twofortyfouram.locale.Intent.EXTRA_STRING_BLURB, extras.keySet()), 2, extras.keySet() //$NON-NLS-1$
                        .size());

                assertFalse(TextUtils.isEmpty(extras.getString(com.twofortyfouram.locale.Intent.EXTRA_STRING_BLURB)));
                assertEquals(TaskerEditActivity.generateBlurb(mTargetContext, message), extras.getString(com.twofortyfouram.locale.Intent.EXTRA_STRING_BLURB));
                // BundleTestHelper.assertSerializable(extras);

                final Bundle pluginBundle = extras.getBundle(com.twofortyfouram.locale.Intent.EXTRA_BUNDLE);
                assertNotNull(pluginBundle);

                /*
                 * Verify the Bundle can be serialized
                 */
                //  BundleTestHelper.assertSerializable(pluginBundle);

                /*
                 * The following are tests specific to this plug-in's bundle
                 */
                assertTrue(PluginBundleManager.isBundleValid(pluginBundle));
                assertEquals(message, pluginBundle.getString(PluginBundleManager.BUNDLE_EXTRA_STRING_MESSAGE));
            }
        };

        if (getActivity().getMainLooper() == Looper.myLooper()) {
            runnable.run();
        } else {
            runTestOnUiThread(runnable);
        }
    }

    /**
     * Asserts provided message is what the UI shows.
     *
     * @param message Message to assert equals the EditText.
     */
    private void assertMessageAutoSync(final String message) throws Throwable {
        final Runnable runnable = new Runnable() {
            public void run() {
                assertEquals(message, ((EditText) getActivity().findViewById(android.R.id.text1)).getText()
                        .toString());
            }
        };

        if (getActivity().getMainLooper() == Looper.myLooper()) {
            runnable.run();
        } else {
            runTestOnUiThread(runnable);
        }
    }

    /**
     * Asserts provided hint is what the UI shows.
     */
    private void assertHintAutoSync(final String hint) throws Throwable {
        final Runnable runnable = new Runnable() {
            public void run() {
                assertEquals(hint, ((EditText) getActivity().findViewById(android.R.id.text1)).getHint()
                        .toString());
            }
        };

        if (getActivity().getMainLooper() == Looper.myLooper()) {
            runnable.run();
        } else {
            runTestOnUiThread(runnable);
        }
    }

    /**
     * Sets the message.
     *
     * @param message The message to set.
     */
    private void setMessageAutoSync(final String message) throws Throwable {
        final Runnable runnable = new Runnable() {
            public void run() {
                final EditText editText = getActivity().findViewById(android.R.id.text1);

                editText.setText(message);
            }
        };

        if (getActivity().getMainLooper() == Looper.myLooper()) {
            runnable.run();
        } else {
            runTestOnUiThread(runnable);
        }
    }

    /**
     * Helper to get the Activity result code via reflection.
     *
     * @param activity Activity whose result code is to be obtained.
     * @return Result code of the Activity.
     */
    private static int getActivityResultCode(final Activity activity) {
        if (null == activity) {
            throw new IllegalArgumentException("activity cannot be null"); //$NON-NLS-1$
        }

        /*
         * This is a hack to verify the result code. There is no official way to check this using the Android
         * testing frameworks, so accessing the internals of the Activity object is the only way. This could
         * break on newer versions of Android.
         */

        try {
            final Field resultCodeField = Activity.class.getDeclaredField("mResultCode"); //$NON-NLS-1$
            resultCodeField.setAccessible(true);
            return ((Integer) resultCodeField.get(activity));
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Helper method to set the Activity's orientation. When this method completes, the Activity will have
     * finished its Activity lifecycle events.
     * <p/>
     * This method must not be called from the UI thread.
     */
    private void setActivityOrientationSync(final int orientation) {
        mInstrumentation.runOnMainSync(new Runnable() {
            public void run() {
                getActivity().setRequestedOrientation(orientation);
            }
        });
    }

    /**
     * Helper to get the Activity result Intent via reflection.
     *
     * @param activity Activity whose result Intent is to be obtained. Cannot be null.
     * @return Result Intent of the Activity
     * @throws IllegalArgumentException if {@code activity} is null
     */
    private static Intent getActivityResultData(final Activity activity) {
        if (null == activity) {
            throw new IllegalArgumentException("activity cannot be null"); //$NON-NLS-1$
        }

        /*
         * This is a hack to verify the result code. There is no official way to check this using the Android
         * testing frameworks, so accessing the internals of the Activity object is the only way. This could
         * break on newer versions of Android.
         */

        try {
            final Field resultIntentField = Activity.class.getDeclaredField("mResultData"); //$NON-NLS-1$
            resultIntentField.setAccessible(true);
            return ((Intent) resultIntentField.get(activity));
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }
}