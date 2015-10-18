/*
 * Copyright (C) 2014 The Android Open Source Project
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

package it.angelic.soulissclient;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.RemoteInput;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.text.style.SubscriptSpan;
import android.text.style.SuperscriptSpan;
import android.text.style.TypefaceSpan;
import android.text.style.UnderlineSpan;

/**
 * Collection of notification builder presets.
 */
public class NotificationPresets {


    private void appendStyled(SpannableStringBuilder builder, String str, Object... spans) {
        builder.append(str);
        for (Object span : spans) {
            builder.setSpan(span, builder.length() - str.length(), builder.length(), 0);
        }
    }

    public Notification buildPagedNotification(Context context) {
        //PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
        //        new Intent(context, SoulissWearActivity.class), 0);
        Intent launchMuzeiIntent = new Intent(context,
                SendSoulissCommandIntentService.class);
        //launchMuzeiIntent.putExtra("THEVOICE", thevoice);
        launchMuzeiIntent.setAction(Constants.ACTION_SEND_SOULISS_COMMAND);
        PendingIntent servicependingIntent = PendingIntent.getService(context, 0,
                launchMuzeiIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent launchSoulissIntent = new Intent(context,
                SendSoulissCommandIntentService.class);
        //launchMuzeiIntent.putExtra("THEVOICE", thevoice);
        launchSoulissIntent.setAction(Constants.ACTION_OPEN_SOULISS);
        PendingIntent serviceOpenpendingIntent = PendingIntent.getService(context, 0,
                launchSoulissIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        SpannableStringBuilder suggeston1 = new SpannableStringBuilder(context.getString(R.string.scene_turnon_lights));
        SpannableStringBuilder suggeston2 = new SpannableStringBuilder(context.getString(R.string.scene_turnoff_lights));


        suggeston1.setSpan(new StyleSpan(Typeface.BOLD_ITALIC), 0, suggeston1.length(), 0);
        suggeston2.setSpan(new StyleSpan(Typeface.BOLD_ITALIC), 0, suggeston2.length(), 0);

        return buildBasicNotification(context)
                .extend(new Notification.WearableExtender()
                                .addAction(new Notification.Action.Builder(R.drawable.ic_mic_32,
                                        context.getString(R.string.voice_command), servicependingIntent)
                                        .addRemoteInput(new RemoteInput.Builder("reply")
                                                .setLabel(context.getString(R.string.voice_command_help))
                                                .setChoices(new CharSequence[]{
                                                        suggeston1,
                                                        suggeston2
                                                })
                                                .build())
                                        .build())
                                .addAction(new Notification.Action.Builder(R.drawable.ic_phone_android_32dp,
                                        "Open on phone", serviceOpenpendingIntent)
                                        .build())
                                        //.setDisplayIntent(launchSoulissWpendingIntent)
                                .setBackground(BitmapFactory.decodeResource(context.getResources(), R.drawable.home_automation))
                        //.addPage(page2)
                )
                .build();
    }

    public Notification buildStyleNotification(Context context) {
        Notification.Builder builder = buildBasicNotification(context);

        Notification.BigTextStyle style = new Notification.BigTextStyle();

        SpannableStringBuilder title = new SpannableStringBuilder();
        appendStyled(title, "Stylized", new StyleSpan(Typeface.BOLD_ITALIC));
        title.append(" title");
        SpannableStringBuilder text = new SpannableStringBuilder("Stylized text: ");
        appendStyled(text, "C", new ForegroundColorSpan(Color.RED));
        appendStyled(text, "O", new ForegroundColorSpan(Color.GREEN));
        appendStyled(text, "L", new ForegroundColorSpan(Color.BLUE));
        appendStyled(text, "O", new ForegroundColorSpan(Color.YELLOW));
        appendStyled(text, "R", new ForegroundColorSpan(Color.MAGENTA));
        appendStyled(text, "S", new ForegroundColorSpan(Color.CYAN));
        text.append("; ");
        appendStyled(text, "1.25x size", new RelativeSizeSpan(1.25f));
        text.append("; ");
        appendStyled(text, "0.75x size", new RelativeSizeSpan(0.75f));
        text.append("; ");
        appendStyled(text, "underline", new UnderlineSpan());
        text.append("; ");
        appendStyled(text, "strikethrough", new StrikethroughSpan());
        text.append("; ");
        appendStyled(text, "bold", new StyleSpan(Typeface.BOLD));
        text.append("; ");
        appendStyled(text, "italic", new StyleSpan(Typeface.ITALIC));
        text.append("; ");
        appendStyled(text, "sans-serif-thin", new TypefaceSpan("sans-serif-thin"));
        text.append("; ");
        appendStyled(text, "monospace", new TypefaceSpan("monospace"));
        text.append("; ");
        appendStyled(text, "sub", new SubscriptSpan());
        text.append("script");
        appendStyled(text, "super", new SuperscriptSpan());

        style.setBigContentTitle(title);
        style.bigText(text);

        builder.setStyle(style);
        return builder.build();
    }

    private static Notification.Builder buildBasicNotification(Context context) {

        return new Notification.Builder(context)
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText("Swipe right to see available actions")
                        // Set a content intent to return to this sample
                        //.setContentIntent(PendingIntent.getActivity(context, 0,servicependingIntent, 0))
                .setSmallIcon(R.mipmap.soulisslogo);
    }
}
