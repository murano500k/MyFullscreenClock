package com.stc.fullscreen.clock.utils;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;

import com.stc.fullscreen.clock.ui.SettingsActivity;

/**
 * Created by artem on 10/10/17.
 */

public class FontListItem extends android.support.v7.widget.AppCompatTextView {
    private static final String TAG = "FontListItem";
    Context context;
    public FontListItem(Context context) {
        super(context);
        this.context=context;
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        super.setText(text, type);
        if(text!=null){
            String fontName = SettingsActivity.getSelectedFontFilePath(text.toString());
            if(fontName==null){
                Log.e(TAG, "setTypeface: null" );
                return;
            }
            Typeface typeface=Typeface.createFromAsset(context.getAssets(), fontName );
            setTypeface(typeface);
        }else                 Log.e(TAG, "setTypeface: null" );
    }
}
