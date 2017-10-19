package com.stc.fullscreen.clock.utils;


import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.stc.fullscreen.clock.R;
import com.stc.fullscreen.clock.ui.SettingsActivity;

import java.util.List;

public class CustomListAdapter extends ArrayAdapter<String> {
    private static final String TAG = "CustomListAdapter";
    private Context mContext;
    private int id;
    private List<String> items ;

    public CustomListAdapter(Context context, int textViewResourceId , List<String> list )
    {
        super(context, textViewResourceId, list);
        id = textViewResourceId;
        mContext = context;
        items = list ;
    }

    @Override
    public View getView(int position, View v, ViewGroup parent)
    {
        View mView = v ;
        if(mView == null){
            LayoutInflater vi = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mView = vi.inflate(id, parent);
        }

        TextView text = (TextView) mView.findViewById(R.id.textView);

        if(items.get(position) != null )
        {
            String fontString= items.get(position);
            String fontName = SettingsActivity.getSelectedFontFilePath(fontString);
            if(fontName==null){
                Log.e(TAG, "setTypeface: null" );
            }else {
                Typeface typeface=Typeface.createFromAsset(mView.getContext().getAssets(), fontName );
                text.setTypeface(typeface);
            }
            text.setText(fontString);
        }

        return mView;
    }

}