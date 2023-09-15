package com.scanny.scanner.models;

public class SavedToolModel {
    private SavedToolType savedToolType;
    private int saved_tool_icon;
    private String icon_name;

    public SavedToolModel(int i, SavedToolType savedToolType2, String icon_name) {
        this.saved_tool_icon = i;
        this.savedToolType = savedToolType2;
        this.icon_name = icon_name;
    }

    public int getSaved_tool_icon() {
        return this.saved_tool_icon;
    }

    public void setSaved_tool_icon(int saved_tool_icon) {
        this.saved_tool_icon = saved_tool_icon;
    }

    public SavedToolType getSavedToolType() {
        return this.savedToolType;
    }

    public void setSavedToolType(SavedToolType savedToolType) {
        this.savedToolType = savedToolType;
    }

    public String getIcon_name() {
        return icon_name;
    }

    public void setIcon_name(String icon_name) {
        this.icon_name = icon_name;
    }
}
