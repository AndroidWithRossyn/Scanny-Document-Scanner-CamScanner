package com.scanny.scanner.adapter;

import android.app.Activity;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.itextpdf.text.html.HtmlTags;
import com.scanny.scanner.R;
import com.scanny.scanner.activity.DocumentEditorActivity;
import com.scanny.scanner.main_utils.Constant;

import java.util.ArrayList;

public class FontAdapter extends RecyclerView.Adapter<FontAdapter.ViewHolder> {

    public Activity activity;

    public ArrayList<String> arrayList;

    public FontAdapter(Activity activity2, ArrayList<String> arrayList2) {
        activity = activity2;
        arrayList = arrayList2;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new ViewHolder(LayoutInflater.from(activity).inflate(R.layout.font_list_item, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int i) {
        AssetManager assets = activity.getAssets();
        viewHolder.tv_fontStyle.setTypeface(Typeface.createFromAsset(assets, "font/" + arrayList.get(i)));
        viewHolder.rl_main.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e(HtmlTags.FONT, "onClick: " + ((String) arrayList.get(i)));
                AssetManager assets = activity.getAssets();
                ((DocumentEditorActivity) activity).onFontClick(Typeface.createFromAsset(assets, "font/" + ((String) arrayList.get(i))));
                Constant.selectedFont = i;
                notifyDataSetChanged();
            }
        });
        if (i == 0) {
            viewHolder.tv_fontStyle.setText("None");
        } else {
            viewHolder.tv_fontStyle.setText("Sample");
        }
        if (Constant.selectedFont == i) {
            viewHolder.rl_main.setBackground(activity.getResources().getDrawable(R.drawable.selected_font_bg));
            viewHolder.tv_fontStyle.setTextColor(activity.getResources().getColor(R.color.white));
            return;
        }
        viewHolder.rl_main.setBackground(activity.getResources().getDrawable(R.drawable.unselected_font_bg));
        viewHolder.tv_fontStyle.setTextColor(activity.getResources().getColor(R.color.txt_color));
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        RelativeLayout rl_main;
        TextView tv_fontStyle;

        public ViewHolder(View view) {
            super(view);
            rl_main = (RelativeLayout) view.findViewById(R.id.rl_main);
            tv_fontStyle = (TextView) view.findViewById(R.id.tv_fontStyle);
        }
    }
}
