package com.scanny.scanner.models;

public class DrawerModel {
    private int item_icon;
    private String item_name;

    public DrawerModel(String str, int i) {
        this.item_name = str;
        this.item_icon = i;
    }

    public String getItem_name() {
        return this.item_name;
    }

    public void setItem_name(String str) {
        this.item_name = str;
    }

    public int getItem_icon() {
        return this.item_icon;
    }

    public void setItem_icon(int i) {
        this.item_icon = i;
    }
}
