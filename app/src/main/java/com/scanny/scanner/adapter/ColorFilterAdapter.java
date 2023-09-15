package com.scanny.scanner.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.scanny.scanner.R;
import com.scanny.scanner.activity.DocumentEditorActivity;
import com.scanny.scanner.document_view.ColorFilter;
import com.scanny.scanner.main_utils.Constant;


public class ColorFilterAdapter extends RecyclerView.Adapter<ColorFilterAdapter.ViewHolder> {

    public Activity activity;
    private ColorFilter colorFilter = new ColorFilter();
    private String[] colorFilterName;

    public ColorFilterAdapter(Activity activity2, String[] strArr) {
        this.activity = activity2;
        this.colorFilterName = strArr;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new ViewHolder(LayoutInflater.from(activity).inflate(R.layout.color_filter_list_item, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int i) {
        viewHolder.tv_filter_name.setText(colorFilterName[i]);
        switch (i) {
            case 0:
                viewHolder.iv_filter_view.setImageBitmap(Constant.original);
                break;
            case 1:
                viewHolder.iv_filter_view.setImageBitmap(colorFilter.filter1(activity, Constant.original));
                break;
            case 2:
                viewHolder.iv_filter_view.setImageBitmap(colorFilter.filter2(activity, Constant.original));
                break;
            case 3:
                viewHolder.iv_filter_view.setImageBitmap(colorFilter.filter3(activity, Constant.original));
                break;
            case 4:
                viewHolder.iv_filter_view.setImageBitmap(colorFilter.filter4(activity, Constant.original));
                break;
            case 5:
                viewHolder.iv_filter_view.setImageBitmap(colorFilter.filter5(activity, Constant.original));
                break;
            case 6:
                viewHolder.iv_filter_view.setImageBitmap(colorFilter.filter6(activity, Constant.original));
                break;
            case 7:
                viewHolder.iv_filter_view.setImageBitmap(colorFilter.filter7(activity, Constant.original));
                break;
            case 8:
                viewHolder.iv_filter_view.setImageBitmap(colorFilter.filter8(activity, Constant.original));
                break;
            case 9:
                viewHolder.iv_filter_view.setImageBitmap(colorFilter.filter9(activity, Constant.original));
                break;
            case 10:
                viewHolder.iv_filter_view.setImageBitmap(colorFilter.filter10(activity, Constant.original));
                break;
            case 11:
                viewHolder.iv_filter_view.setImageBitmap(colorFilter.filter11(activity, Constant.original));
                break;
        }
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Constant.filterPosition = i;
                notifyDataSetChanged();
                ((DocumentEditorActivity) activity).onColorFilterSeleced(activity, i);
            }
        });
        if (Constant.filterPosition == i) {
            viewHolder.ly_img.setBackground(activity.getResources().getDrawable(R.drawable.img_border_selected));
            viewHolder.tv_filter_name.setTextColor(activity.getResources().getColor(R.color.black));
            return;
        }
        viewHolder.ly_img.setBackground(activity.getResources().getDrawable(R.drawable.img_border_unselected));
        viewHolder.tv_filter_name.setTextColor(activity.getResources().getColor(R.color.white));
    }

    @Override
    public int getItemCount() {
        return colorFilterName.length;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView iv_filter_view;
        LinearLayout ly_img;
        TextView tv_filter_name;

        public ViewHolder(View view) {
            super(view);
            ly_img = (LinearLayout) view.findViewById(R.id.ly_img);
            iv_filter_view = (ImageView) view.findViewById(R.id.iv_filter_view);
            tv_filter_name = (TextView) view.findViewById(R.id.tv_filter_name);
        }
    }
}
