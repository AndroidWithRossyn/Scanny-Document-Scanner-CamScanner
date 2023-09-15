package com.scanny.scanner.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.scanny.scanner.R;
import com.scanny.scanner.models.SavedToolModel;
import com.scanny.scanner.models.SavedToolType;

import java.util.ArrayList;

public class SavedToolsAdapter extends RecyclerView.Adapter<SavedToolsAdapter.ViewHolder> {

    public OnSavedToolSelected onSavedToolSelected;

    public ArrayList<SavedToolModel> savedToolsList = new ArrayList<>();

    public SavedToolsAdapter(OnSavedToolSelected onSavedToolSelected2) {
        onSavedToolSelected = onSavedToolSelected2;
        savedToolsList.add(new SavedToolModel(R.drawable.ssic_edit, SavedToolType.EDIT, "Edit"));
        savedToolsList.add(new SavedToolModel(R.drawable.ssic_open_pdf, SavedToolType.OPENPDF, "Open pdf"));
        savedToolsList.add(new SavedToolModel(R.drawable.ssic_name, SavedToolType.NAME, "Name"));
        savedToolsList.add(new SavedToolModel(R.drawable.ssic_rotate, SavedToolType.ROTATE, "Rotate"));
        savedToolsList.add(new SavedToolModel(R.drawable.ssic_note, SavedToolType.NOTE, "Note"));
        savedToolsList.add(new SavedToolModel(R.drawable.ssic_img_to_text, SavedToolType.ImageToText, "Image to text"));
        savedToolsList.add(new SavedToolModel(R.drawable.ssic_share, SavedToolType.SHARE, "Share"));
        savedToolsList.add(new SavedToolModel(R.drawable.ssic_delete, SavedToolType.DELETE, "Delete"));
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new ViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.saved_tools_list_item, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int i) {
        viewHolder.iv_toolIcon.setImageResource(savedToolsList.get(i).getSaved_tool_icon());
        viewHolder.txtIconName.setText(savedToolsList.get(i).getIcon_name());
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onSavedToolSelected.onSavedToolSelected(((SavedToolModel) savedToolsList.get(i)).getSavedToolType());
            }
        });
    }

    @Override
    public int getItemCount() {
        return savedToolsList.size();
    }

    public interface OnSavedToolSelected {
        void onSavedToolSelected(SavedToolType savedToolType);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView iv_toolIcon;
        TextView txtIconName;

        public ViewHolder(View view) {
            super(view);
            iv_toolIcon = (ImageView) view.findViewById(R.id.iv_toolIcon);
            txtIconName = (TextView) view.findViewById(R.id.txtIconName);
        }
    }
}
