package com.scanny.scanner.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.scanny.scanner.R;
import com.scanny.scanner.activity.DocumentEditorActivity;

import java.util.ArrayList;

public class SignatureAdapter extends RecyclerView.Adapter<SignatureAdapter.ViewHolder> {

    public Activity activity;
    private ArrayList<String> arrayList;

    public SignatureAdapter(Activity activity2, ArrayList<String> arrayList2) {
        activity = activity2;
        arrayList = arrayList2;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new ViewHolder(LayoutInflater.from(activity).inflate(R.layout.signature_list_item, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int i) {
        Glide.with(activity).load(arrayList.get(i)).into(viewHolder.iv_signature);
        viewHolder.iv_dlt_signature.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((DocumentEditorActivity) activity).onDeleteSignature(i);
            }
        });
        viewHolder.iv_signature.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((DocumentEditorActivity) activity).onClickSignature(i);
            }
        });
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView iv_dlt_signature;
        ImageView iv_signature;

        public ViewHolder(View view) {
            super(view);
            iv_signature = (ImageView) view.findViewById(R.id.iv_signature);
            iv_dlt_signature = (ImageView) view.findViewById(R.id.iv_dlt_signature);
        }
    }
}
