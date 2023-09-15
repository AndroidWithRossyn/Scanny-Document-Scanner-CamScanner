package com.scanny.scanner.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.scanny.scanner.R;
import com.scanny.scanner.activity.DocumentEditorActivity;
import com.scanny.scanner.main_utils.Constant;

import java.util.ArrayList;

public class WatermarkFontAdapter extends RecyclerView.Adapter<WatermarkFontAdapter.ViewHolder> {

    public Activity activity;

    public ArrayList<Integer> arrayList;

    public WatermarkFontAdapter(Activity activity2, ArrayList<Integer> arrayList2) {
        activity = activity2;
        arrayList = arrayList2;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new ViewHolder(LayoutInflater.from(activity).inflate(R.layout.watermark_font_list_item, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int i) {
        viewHolder.tv_fontStyle.setTypeface(ResourcesCompat.getFont(activity, arrayList.get(i).intValue()));
        viewHolder.tv_fontStyle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((DocumentEditorActivity) activity).onWatermarkFontClick(((Integer) arrayList.get(i)).intValue());
                Constant.selectedWatermarkFont = i;
                notifyDataSetChanged();
            }
        });
        if (i == 0) {
            viewHolder.tv_fontStyle.setText("None");
        } else {
            viewHolder.tv_fontStyle.setText("Sample");
        }
        if (Constant.selectedWatermarkFont == i) {
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
            tv_fontStyle = (TextView) view.findViewById(R.id.tv_fontStyle);
            rl_main = (RelativeLayout) view.findViewById(R.id.rl_main);
        }
    }
}
