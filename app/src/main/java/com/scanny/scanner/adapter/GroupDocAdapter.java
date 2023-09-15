package com.scanny.scanner.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.scanny.scanner.R;
import com.scanny.scanner.activity.GroupDocumentActivity;
import com.scanny.scanner.main_utils.Constant;
import com.scanny.scanner.models.DBModel;
import com.scanny.scanner.utils.AdsUtils;

import java.util.ArrayList;

public class GroupDocAdapter extends RecyclerView.Adapter<GroupDocAdapter.ViewHolder> {

    public Activity activity;
    private ArrayList<DBModel> arrayList;

    public GroupDocAdapter(Activity activity2, ArrayList<DBModel> arrayList2) {
        this.activity = activity2;
        this.arrayList = arrayList2;

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new ViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.group_doc_item_list, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, final int i) {
        if (i < (arrayList.size() - 1)) {
            viewHolder.newScanLayout.setVisibility(View.GONE);
            viewHolder.docLayout.setVisibility(View.VISIBLE);
            Glide.with(activity).load(arrayList.get(i).getGroup_doc_img()).into(viewHolder.iv_doc);
            TextView textView = viewHolder.tv_doc_name;
            textView.setText("Page " + (i + 1));
            viewHolder.iv_doc.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ((GroupDocumentActivity) activity).onClickSingleDoc(i);
                }
            });
            viewHolder.iv_doc_item_more.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ((GroupDocumentActivity) activity).onClickItemMore(i, viewHolder.tv_doc_name.getText().toString());
                }
            });
            if (arrayList.get(i).getGroup_doc_note().equals("Insert text here...")) {
                viewHolder.iv_note.setVisibility(View.GONE);
            } else {
                viewHolder.iv_note.setVisibility(View.VISIBLE);
            }
        } else {
            viewHolder.newScanLayout.setVisibility(View.VISIBLE);
            viewHolder.docLayout.setVisibility(View.GONE);

        }
        viewHolder.newScanLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Constant.inputType = "GroupItem";
                Constant.IdentifyActivity = "ScannerActivity2";
                AdsUtils.showGoogleInterstitialAd(activity, true);
            }
        });

    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView iv_doc;
        private ImageView iv_doc_item_more;
        private ImageView iv_note;
        private TextView tv_doc_name;
        private RelativeLayout newScanLayout, docLayout;

        public ViewHolder(View view) {
            super(view);
            iv_doc = (ImageView) view.findViewById(R.id.iv_doc);
            tv_doc_name = (TextView) view.findViewById(R.id.tv_doc_name);
            iv_note = (ImageView) view.findViewById(R.id.iv_note);
            iv_doc_item_more = (ImageView) view.findViewById(R.id.iv_doc_item_more);
            newScanLayout = (RelativeLayout) view.findViewById(R.id.newScanLayout);
            docLayout = (RelativeLayout) view.findViewById(R.id.docLayout);
        }
    }
}
