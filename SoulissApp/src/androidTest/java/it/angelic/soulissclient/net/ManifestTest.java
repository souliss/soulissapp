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

package it.angelic.soulissclient.net;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;

import com.twofortyfouram.locale.PackageUtilities;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

import androidx.test.filters.SmallTest;
import androidx.test.platform.app.InstrumentationRegistry;

import static androidx.test.InstrumentationRegistry.getContext;

/**
 * Tests to verify proper entries in the plug-in's Android Manifest.
 */
public final class
ManifestTest extends junit.framework.TestCase {
    /**
     * Verifies there is a Locale compatible host present.
     */
    @Before
    public void testPreconditions() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertNotNull(getHostPackage(context.getPackageManager()));
    }

    /**
     * Helper to get a Locale-compatible host package.
     *
     * @param manager PackageManager cannot be null.
     * @return Package name of the Locale-compatible host. May be null if no host is present.
     */
    private static String getHostPackage(final PackageManager manager) {
        return PackageUtilities.getCompatiblePackage(manager, "com.twofortyfouram.locale"); //$NON-NLS-1$
    }


    @SmallTest
    public void testApplicationEnabled() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertTrue(context.getApplicationInfo().enabled);
    }
    /**
     * Verifies that a plug-in condition Activity is present, enabled, exported, doesn't require permissions,
     * and has a name and icon.
     */
    @Test
    public void testPluginActivityPresent() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        final PackageManager packageManager = context.getPackageManager();

        final List<ResolveInfo> activities = getPluginActivities(context);
        assertFalse(activities.isEmpty());

        for (final ResolveInfo x : activities) {
            assertTrue(x.activityInfo.enabled);
            assertTrue(x.activityInfo.exported);

            /*
             * Verify that the plug-in doesn't request permissions not available to the host
             */
            assertFalse(null != x.activityInfo.permission
                    && PackageManager.PERMISSION_DENIED == packageManager.checkPermission(x.activityInfo.permission, getHostPackage(packageManager)));

            /*
             * Verify that the plug-in has a label attribute in the AndroidManifest
             */
            assertFalse(0 == x.activityInfo.labelRes);

            /*
             * Verify that the plug-in has a icon attribute in the AndroidManifest
             */
            //assertFalse(0 == x.activityInfo.icon);
        }
    }

    @Test
    public void testPluginReceiver() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        final PackageManager packageManager = context.getPackageManager();
        final String hostPackage = getHostPackage(context.getPackageManager());

        final List<ResolveInfo> receivers = getPluginReceivers(context);

        Assert.assertEquals(1, receivers.size());

        for (final ResolveInfo x : receivers) {
            assertTrue(x.activityInfo.enabled);
            assertTrue(x.activityInfo.exported);

            /*
             * Verify that the plug-in doesn't request permissions not available to the host
             */
            assertFalse(null != x.activityInfo.permission
                    && PackageManager.PERMISSION_DENIED == packageManager.checkPermission(x.activityInfo.permission, hostPackage));
        }
    }

    /**
     * Tests that the plug-in targets at least the same SDK as Locale.
     */
    @Test
    public void testTargetSdk() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        /*
         * Plug-ins should always keep up to date with the latest target SDK version. This verifies that the
         * plug-in is at least as current as the host application. Locale won't reject plug-ins targeting
         * older SDKs, but Android will run those plug-ins in backwards compatibility mode which may
         * negatively affect the UI.
         */

        final String hostPackage = getHostPackage(context.getPackageManager());
        if (null == hostPackage) {
            fail("Host application is not installed; please install Locale"); //$NON-NLS-1$
        }

        try {
            final Context localeContext = getContext().createPackageContext(hostPackage, 0);
            assertTrue(getContext().getApplicationInfo().targetSdkVersion >= localeContext.getApplicationInfo().targetSdkVersion);
        } catch (final NameNotFoundException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * Gets a list of all Activities in {@code context}'s package that export
     * {@link com.twofortyfouram.locale.Intent#ACTION_EDIT_SETTING}.
     *
     * @param context context. Cannot be null
     */
    private static List<ResolveInfo> getPluginActivities(final Context context) {
        if (null == context) {
            throw new IllegalArgumentException("context cannot be null"); //$NON-NLS-1$
        }

        final String packageName = context.getPackageName();

        final List<ResolveInfo> result = new LinkedList<>();

        for (final ResolveInfo x : context.getPackageManager()
                .queryIntentActivities(new Intent(
                        com.twofortyfouram.locale.Intent.ACTION_EDIT_SETTING), PackageManager.MATCH_ALL)) {
            if (packageName.equals(x.activityInfo.packageName)) {
                result.add(x);
            }
        }

        return result;
    }

    /**
     * Gets a list of all BroadcastReceivers in {@code context}'s package that export
     * {@link com.twofortyfouram.locale.Intent#ACTION_FIRE_SETTING}.
     *
     * @param context context. Cannot be null
     */
    private static List<ResolveInfo> getPluginReceivers(final Context context) {
        if (null == context) {
            throw new IllegalArgumentException("context cannot be null"); //$NON-NLS-1$
        }

        final String packageName = context.getPackageName();

        final List<ResolveInfo> result = new LinkedList<>();

        for (final ResolveInfo x : context.getPackageManager()
                .queryBroadcastReceivers(new Intent(
                        com.twofortyfouram.locale.Intent.ACTION_FIRE_SETTING), PackageManager.GET_RESOLVED_FILTER)) {
            if (packageName.equals(x.activityInfo.packageName)) {
                result.add(x);
            }

        }

        return result;
    }
}