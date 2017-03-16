package it.angelic.soulissclient.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.preference.EditTextPreference;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;

import it.angelic.soulissclient.Constants;
import it.angelic.soulissclient.helpers.SoulissGlobalPreferenceHelper;

public class AutoCompletePreference extends EditTextPreference {

    private AutoCompleteTextView mEditText = null;
    private final SoulissGlobalPreferenceHelper gbPref;

    /**
     * http://stackoverflow.com/questions/3326317/possible-to-autocomplete-a-edittextpreference
     *
     * @param context
     * @param attrs
     */
    public AutoCompletePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        gbPref = new SoulissGlobalPreferenceHelper(context);
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
        /*AutoCompleteTextView editText = mEditText;
        editText.setText(getText());

        ViewParent oldParent = editText.getParent();
        if (oldParent != view) {
            if (oldParent != null) {
                ((ViewGroup) oldParent).removeView(editText);
            }
            super.onAddEditTextToDialogView(view, editText);
        }
        //super.onBindDialogView(view);
*/

        super.onBindDialogView(view);

        // find the current EditText object
        final EditText editText = (EditText) view.findViewById(android.R.id.edit);
        // copy its layout params
        ViewGroup.LayoutParams params = editText.getLayoutParams();
        ViewGroup vg = (ViewGroup) editText.getParent();
        String curVal = editText.getText().toString();
        // remove it from the existing layout hierarchy
        vg.removeView(editText);
        // construct a new editable autocomplete object with the appropriate params
        // and id that the TextEditPreference is expecting
        mEditText = new AutoCompleteTextView(getContext());
        mEditText.setLayoutParams(params);
        mEditText.setId(android.R.id.edit);
        mEditText.setText(curVal);


        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, gbPref.getIpDictionary());
        mEditText.setAdapter(adapter);

        // add the new view to the layout
        vg.addView(mEditText);

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