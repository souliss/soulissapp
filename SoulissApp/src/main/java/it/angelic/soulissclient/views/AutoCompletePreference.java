package it.angelic.soulissclient.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.preference.EditTextPreference;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import it.angelic.soulissclient.Constants;
import it.angelic.soulissclient.helpers.SoulissGlobalPreferenceHelper;

public class AutoCompletePreference extends EditTextPreference {

    private static AutoCompleteTextView mEditText = null;

    /**
     * http://stackoverflow.com/questions/3326317/possible-to-autocomplete-a-edittextpreference
     *
     * @param context
     * @param attrs
     */
    public AutoCompletePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        SoulissGlobalPreferenceHelper gbPref = new SoulissGlobalPreferenceHelper(context);
        mEditText = new AutoCompleteTextView(context, attrs);
        mEditText.setThreshold(Constants.AUTOCOMPLETE_THRESHOLD);
        mEditText.setId(android.R.id.edit);
        //The adapter of your choice
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_dropdown_item_1line, gbPref.getIpDictionary());
        mEditText.setAdapter(adapter);
    }


    @SuppressLint("MissingSuperCall")//ci pensiamo noi
    @Override
    protected void onBindDialogView(View view) {
        AutoCompleteTextView editText = mEditText;
        editText.setText(getText());

        ViewParent oldParent = editText.getParent();
        if (oldParent != view) {
            if (oldParent != null) {
                ((ViewGroup) oldParent).removeView(editText);
            }
            onAddEditTextToDialogView(view, editText);
        }
        //super.onBindDialogView(view);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            String value = mEditText.getText().toString();
            if (callChangeListener(value)) {
                setText(value);
            }
        }
    }
}