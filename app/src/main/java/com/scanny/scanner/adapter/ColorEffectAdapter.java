package com.scanny.scanner.adapter;

import android.app.Activity;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrixColorFilter;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.scanny.scanner.R;
import com.scanny.scanner.activity.DocumentEditorActivity;
import com.scanny.scanner.main_utils.Constant;

import java.util.ArrayList;

public class ColorEffectAdapter extends RecyclerView.Adapter<ColorEffectAdapter.ViewHolder> {

    public Activity activity;
    private ArrayList<Pair<String, ColorMatrixColorFilter>> arrayList = new ArrayList<>();

    public ColorEffectAdapter(Activity activity2) {
        activity = activity2;
        arrayList.add(new Pair("None", Constant.coloreffect[0]));
        arrayList.add(new Pair("Color 1", Constant.coloreffect[1]));
        arrayList.add(new Pair("Color 2", Constant.coloreffect[2]));
        arrayList.add(new Pair("Color 3", Constant.coloreffect[3]));
        arrayList.add(new Pair("Color 4", Constant.coloreffect[4]));
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new ViewHolder(LayoutInflater.from(activity).inflate(R.layout.color_effect_list_item, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int i) {
        viewHolder.iv_effect_view.setImageBitmap(Constant.original);
        viewHolder.iv_effect_view.setColorFilter((ColorFilter) arrayList.get(i).second);
        viewHolder.tv_effect_name.setText((CharSequence) arrayList.get(i).first);
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((DocumentEditorActivity) activity).onColorEffectClick(i);
            }
        });
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView iv_effect_view;

        public TextView tv_effect_name;

        public ViewHolder(View view) {
            super(view);
            iv_effect_view = (ImageView) view.findViewById(R.id.iv_effect_view);
            tv_effect_name = (TextView) view.findViewById(R.id.tv_effect_name);
        }
    }
}
