package it.angelic.soulissclient.fragments;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.NumberPicker;

import it.angelic.soulissclient.Constants;

/**
 * serve a nascondere la complessita di un number picker che mostra float,
 * ma si basa su int come appunto @link{NumberPicker}
 * <p/>
 * Created by shine@angelic.it on 19/04/2016.
 */
public class NumberPickerT6 extends NumberPicker {
    private final int pivotIdx = 51;
    private final int WINDOW_SIZE = 100;
    private String[] dispVal;
    private float increment;
    private int max;
    private int min;
    private int model;
    private float realVal;

    public NumberPickerT6(Context context) {
        super(context);
        init();
    }

    private void init() {
        setMinValue(0);
        setMaxValue(WINDOW_SIZE - 1);
        dispVal = new String[WINDOW_SIZE];
        // float wkVal = curVal;
        if (model == Constants.Typicals.Souliss_T62) {
            min = -20;
            max = +50;
            increment = 0.5f;
        } else {//T61
            min = -65519;
            max = 65519;
            increment = 1;
        }
    }

    public NumberPickerT6(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public NumberPickerT6(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    /*
    * GENERA i valori dello spinner da mostrare
    * sono 100
    * */
    public int generateDisplayValues(float curVal) {

        int selIdx = dispVal.length;
        float winIdxIn;
        float winIdxOut = curVal;


        //finestra di selezione
        winIdxIn = winIdxOut - (dispVal.length * increment);
        //Float tempArray[] = new Float[100];
        for (int t = pivotIdx; t < dispVal.length; t++) {
            if (winIdxOut + increment > max)
                break;//ho finito
            winIdxOut += increment;
            winIdxIn += increment;
            selIdx--;
        }//nel caso ideal usciamo a 50
        while (winIdxIn < min) {
            winIdxOut += increment;
            winIdxIn += increment;
            selIdx--;
        }

        //a questo punto crea
        for (int j = 0; j < dispVal.length; j++) {
            dispVal[j] = String.valueOf(winIdxIn);
            winIdxIn += increment;
        }
        setDisplayedValues(dispVal);
        return selIdx;
    }

    public float getIncrement() {
        return increment;
    }

    public void setIncrement(float increment) {
        this.increment = increment;
    }

    public int getModel() {
        return model;
    }

    public void setModel(int model) {
        this.model = model;

    }

    public float getRealVal() {
        return realVal;
    }

    public void setRealVal(float realVal) {
        this.realVal = realVal;
        int sel = generateDisplayValues(realVal);
        setValue(sel);
    }


}
