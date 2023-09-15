package com.scanny.scanner.main_utils;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.otaliastudios.cameraview.size.AspectRatio;

import java.util.Arrays;
import java.util.Set;

//import com.google.android.cameraview.AspectRatio;

public class AspectRatioFragment extends DialogFragment {
    private static final String ARG_ASPECT_RATIOS = "aspect_ratios";
    private static final String ARG_CURRENT_ASPECT_RATIO = "current_aspect_ratio";

    public Listener mListener;

    public static AspectRatioFragment newInstance(Set<AspectRatio> set, AspectRatio aspectRatio) {
        AspectRatioFragment aspectRatioFragment = new AspectRatioFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelableArray(ARG_ASPECT_RATIOS, (Parcelable[]) set.toArray(new AspectRatio[set.size()]));
        bundle.putParcelable(ARG_CURRENT_ASPECT_RATIO, (Parcelable) aspectRatio);
        aspectRatioFragment.setArguments(bundle);
        return aspectRatioFragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.mListener = (Listener) context;
    }

    @Override
    public void onDetach() {
        this.mListener = null;
        super.onDetach();
    }

    @Override
    public Dialog onCreateDialog(Bundle bundle) {
        Bundle arguments = getArguments();
        final AspectRatio[] aspectRatioArr = (AspectRatio[]) arguments.getParcelableArray(ARG_ASPECT_RATIOS);
        if (aspectRatioArr != null) {
            Arrays.sort(aspectRatioArr);
            return new AlertDialog.Builder(getActivity()).setAdapter(new AspectRatioAdapter(aspectRatioArr, (AspectRatio) arguments.getParcelable(ARG_CURRENT_ASPECT_RATIO)), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogInterface, int i) {
                    AspectRatioFragment.this.mListener.onAspectRatioSelected(aspectRatioArr[i]);
                }
            }).create();
        }
        throw new RuntimeException("No ratios");
    }

    public interface Listener {
        void onAspectRatioSelected(AspectRatio aspectRatio);
    }

    private static class AspectRatioAdapter extends BaseAdapter {
        private final AspectRatio mCurrentRatio;
        private final AspectRatio[] mRatios;

        AspectRatioAdapter(AspectRatio[] aspectRatioArr, AspectRatio aspectRatio) {
            this.mRatios = aspectRatioArr;
            this.mCurrentRatio = aspectRatio;
        }

        @Override
        public int getCount() {
            return this.mRatios.length;
        }

        @Override
        public AspectRatio getItem(int i) {
            return this.mRatios[i];
        }

        @Override
        public long getItemId(int i) {
            return (long) getItem(i).hashCode();
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder;
            if (view == null) {
                view = LayoutInflater.from(viewGroup.getContext()).inflate(android.R.layout.simple_list_item_1, viewGroup, false);
                viewHolder = new ViewHolder();
                viewHolder.text = (TextView) view.findViewById(android.R.id.text1);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }
            AspectRatio item = getItem(i);
            StringBuilder sb = new StringBuilder(item.toString());
            if (item.equals(this.mCurrentRatio)) {
                sb.append(" *");
            }
            viewHolder.text.setText(sb);
            return view;
        }

        private static class ViewHolder {
            TextView text;

            private ViewHolder() {
            }
        }
    }
}
