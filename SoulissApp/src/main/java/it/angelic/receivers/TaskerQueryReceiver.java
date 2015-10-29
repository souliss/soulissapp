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

package it.angelic.receivers;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import java.util.List;
import java.util.Locale;

import it.angelic.bundle.BundleScrubber;
import it.angelic.bundle.PluginBundleManager;
import it.angelic.soulissclient.Constants;
import it.angelic.soulissclient.TaskerEditActivity;
import it.angelic.soulissclient.db.SoulissDBHelper;
import it.angelic.soulissclient.helpers.SoulissPreferenceHelper;
import it.angelic.soulissclient.helpers.TaskerPlugin;
import it.angelic.soulissclient.model.SoulissNode;
import it.angelic.soulissclient.model.SoulissTypical;

/**
 * This is the "fire" BroadcastReceiver for a Locale Plug-in setting.
 *
 * @see com.twofortyfouram.locale.Intent#ACTION_QUERY_CONDITION
 * @see com.twofortyfouram.locale.Intent#EXTRA_BUNDLE
 */
public final class TaskerQueryReceiver extends BroadcastReceiver {

    /**
     * @param context {@inheritDoc}.
     * @param intent  the incoming {@link com.twofortyfouram.locale.Intent#ACTION_QUERY_CONDITION} Intent. This
     *                should contain the {@link com.twofortyfouram.locale.Intent#EXTRA_BUNDLE} that was saved by
     *                {@link TaskerEditActivity} and later broadcast by Locale.
     */
    @Override
    public void onReceive(final Context context, final Intent intent) {
        /*
         * Always be strict on input parameters! A malicious third-party app could send a malformed Intent.
         * http://www.twofortyfouram.com/developer.html
         *
         */
        Log.d(Constants.TAG,
                String.format(Locale.US, "Received Tasker QUERY intent action: %s", intent.getAction()));
        if (!com.twofortyfouram.locale.Intent.ACTION_QUERY_CONDITION.equals(intent.getAction())) {
            Log.e(Constants.TAG,
                    String.format(Locale.US, "Received unexpected QUERY Intent action %s", intent.getAction())); //$NON-NLS-1$
            return;
        }
        /*
         * Ignore implicit intents, because they are not valid. It would be
         * meaningless if ALL plug-in condition BroadcastReceivers installed
         * were asked to handle queries not intended for them.
         */
        if (!new ComponentName(context, this.getClass().getName()).equals(intent
                .getComponent())) {
            Log.e(Constants.TAG, "Intent is not explicit"); //$NON-NLS-1$
            setResultCode(com.twofortyfouram.locale.Intent.RESULT_CONDITION_UNKNOWN);
            abortBroadcast();
            return;
        }

        BundleScrubber.scrub(intent);

        final Bundle bundle = intent.getBundleExtra(com.twofortyfouram.locale.Intent.EXTRA_BUNDLE);
        BundleScrubber.scrub(bundle);
        if (null == bundle) {
            Log.e(Constants.TAG, com.twofortyfouram.locale.Intent.EXTRA_BUNDLE + "  is missing"); //$NON-NLS-1$
            setResultCode(com.twofortyfouram.locale.Intent.RESULT_CONDITION_UNKNOWN);
            return;
        }

        setResultCode(com.twofortyfouram.locale.Intent.RESULT_CONDITION_UNSATISFIED);
        if (PluginBundleManager.isBundleValid(bundle)) {
            final String message = bundle.getString(PluginBundleManager.BUNDLE_EXTRA_STRING_MESSAGE);
            if (message != null && message.length() > 0) {
                Log.w(Constants.TAG,
                        String.format(Locale.US, "Activating Tasker query, message: %s", message));
                SoulissPreferenceHelper opzioni = new SoulissPreferenceHelper(context);
                SoulissDBHelper database = new SoulissDBHelper(context);
                SoulissDBHelper.open();

                SoulissTypical typMatch = null;
                SoulissNode nodeMatch = null;

                List<SoulissNode> nodes = database.getAllNodes();
                for (final SoulissNode premio : nodes) {
                    if (premio.getName() != null && message.toLowerCase().contains(premio.getName().toLowerCase()))
                        nodeMatch = premio;
                }
                for (final SoulissNode premio : nodes) {
                    List<SoulissTypical> tippi = premio.getTypicals();
                    for (final SoulissTypical treppio : tippi) {

                        if (message.toLowerCase().contains(treppio.getNiceName().toLowerCase())) {
                            typMatch = treppio;
                            if (nodeMatch == null)
                                nodeMatch = typMatch.getParentNode();
                            Log.w(Constants.TAG,
                                    String.format(Locale.US, "Device found, looking for status: %s", message));
                            if (message.toLowerCase().contains(treppio.getOutputDesc().toLowerCase())) {
                                setResultCode(com.twofortyfouram.locale.Intent.RESULT_CONDITION_SATISFIED);
                                Log.w(Constants.TAG,
                                        String.format(Locale.US, "Device found, STATUS MATCH: %s", treppio.getOutputDesc()));
                            }

                        }

                    }
                }
                //Toast.makeText(context, message, Toast.LENGTH_LONG).show();
                if (TaskerPlugin.Condition.hostSupportsVariableReturn(intent.getExtras())) {
                    Bundle varsBundle = new Bundle();

                    varsBundle.putInt("%numTipici", opzioni.getCustomPref().getInt("numTipici", 0));
                    if (typMatch != null) {
                        varsBundle.putString("%deviceName", typMatch.getNiceName());
                        intent.putExtra(com.twofortyfouram.locale.Intent.EXTRA_STRING_BLURB, typMatch.getOutputDesc());

                    }
                    Log.w(Constants.TAG, "Bundle added: " + varsBundle.toString());
                    TaskerPlugin.addVariableBundle(getResultExtras(true), varsBundle);
                }
            }
        }
    }
}