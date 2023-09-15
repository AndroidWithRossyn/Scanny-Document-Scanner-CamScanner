package com.scanny.scanner.models;

public class DBModel {
    public String group_date;
    public String group_name;
    private String group_doc_img;
    private String group_doc_name;
    private String group_doc_note;
    private String group_first_img;
    private String group_tag;
    private int id;

    public DBModel() {
    }

    public DBModel(String name, String date, String firstImg, String tag) {
        this.group_name = name;
        this.group_date = date;
        this.group_first_img = firstImg;
        this.group_tag = tag;
    }

    public int getId() {
        return this.id;
    }

    public void setId(int i) {
        this.id = i;
    }

    public String getGroup_name() {
        return this.group_name;
    }

    public void setGroup_name(String str) {
        this.group_name = str;
    }

    public String getGroup_date() {
        return this.group_date;
    }

    public void setGroup_date(String str) {
        this.group_date = str;
    }

    public String getGroup_first_img() {
        return this.group_first_img;
    }

    public void setGroup_first_img(String str) {
        this.group_first_img = str;
    }

    public String getGroup_doc_img() {
        return this.group_doc_img;
    }

    public void setGroup_doc_img(String str) {
        this.group_doc_img = str;
    }

    public String getGroup_doc_name() {
        return this.group_doc_name;
    }

    public void setGroup_doc_name(String str) {
        this.group_doc_name = str;
    }

    public String getGroup_doc_note() {
        return this.group_doc_note;
    }

    public void setGroup_doc_note(String str) {
        this.group_doc_note = str;
    }

    public String getGroup_tag() {
        return this.group_tag;
    }

    public void setGroup_tag(String str) {
        this.group_tag = str;
    }
}
