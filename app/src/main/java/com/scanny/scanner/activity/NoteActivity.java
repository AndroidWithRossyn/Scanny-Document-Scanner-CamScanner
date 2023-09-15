package com.scanny.scanner.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.core.internal.view.SupportMenu;
import androidx.core.view.ViewCompat;

import com.google.android.gms.ads.AdView;
import com.scanny.scanner.R;
import com.scanny.scanner.db.DBHelper;
import com.scanny.scanner.utils.AdsUtils;

import jp.wasabeef.richeditor.RichEditor;

public class NoteActivity extends BaseActivity {

    public String current_doc_name;

    public DBHelper dbHelper;

    public String group_name;

    public RichEditor richEditor;
    protected String text;
    private AdView adView;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_note);
        dbHelper = new DBHelper(this);

        init();
        bindView();
    }

    private void init() {
        adView = findViewById(R.id.adView);
        richEditor = (RichEditor) findViewById(R.id.editor);
        group_name = getIntent().getStringExtra("group_name");
        current_doc_name = getIntent().getStringExtra("current_doc_name");
        text = getIntent().getStringExtra("note");
        richEditor.setEditorFontSize(18);
        richEditor.setPadding(10, 10, 10, 10);
        richEditor.setHtml(text);
        richEditor.requestFocus();

        AdsUtils.showGoogleBannerAd(this, adView);
    }

    private void bindView() {
        findViewById(R.id.iv_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        findViewById(R.id.iv_save_note).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(NoteActivity.this, "Note Saved", Toast.LENGTH_SHORT).show();
                BaseActivity.hideKeyboard(NoteActivity.this);
                dbHelper.updateGroupListDocNote(group_name, current_doc_name, richEditor.getHtml());
                finish();
            }
        });
        findViewById(R.id.ly_undo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                richEditor.undo();
            }
        });
        findViewById(R.id.ly_redo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                richEditor.redo();
            }
        });
        findViewById(R.id.ly_bold).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                richEditor.setBold();
            }
        });
        findViewById(R.id.ly_italic).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                richEditor.setItalic();
            }
        });
        findViewById(R.id.ly_subscript).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                richEditor.setSubscript();
            }
        });
        findViewById(R.id.ly_superscript).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                richEditor.setSuperscript();
            }
        });
        findViewById(R.id.ly_strikethrough).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                richEditor.setStrikeThrough();
            }
        });
        findViewById(R.id.ly_underline).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                richEditor.setUnderline();
            }
        });
        findViewById(R.id.ly_headline1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                richEditor.setHeading(1);
            }
        });
        findViewById(R.id.ly_headline2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                richEditor.setHeading(2);
            }
        });
        findViewById(R.id.ly_headline3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                richEditor.setHeading(3);
            }
        });
        findViewById(R.id.ly_headline4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                richEditor.setHeading(4);
            }
        });
        findViewById(R.id.ly_headline5).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                richEditor.setHeading(5);
            }
        });
        findViewById(R.id.ly_headline6).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                richEditor.setHeading(6);
            }
        });
        findViewById(R.id.ly_red_text).setOnClickListener(new View.OnClickListener() {
            private boolean isChanged;

            @Override
            public void onClick(View view) {
                richEditor.setTextColor(isChanged ? ViewCompat.MEASURED_STATE_MASK : SupportMenu.CATEGORY_MASK);
                isChanged = !isChanged;
            }
        });
        findViewById(R.id.ly_indent).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                richEditor.setIndent();
            }
        });
        findViewById(R.id.ly_outdent).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                richEditor.setOutdent();
            }
        });
        findViewById(R.id.ly_align_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                richEditor.setAlignLeft();
            }
        });
        findViewById(R.id.ly_center).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                richEditor.setAlignCenter();
            }
        });
        findViewById(R.id.ly_right_text).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                richEditor.setAlignRight();
            }
        });
        findViewById(R.id.ly_insert_bullets).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                richEditor.setBullets();
            }
        });
        findViewById(R.id.ly_numbers).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                richEditor.setNumbers();
            }
        });
        findViewById(R.id.ly_checkbox).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                richEditor.insertTodo();
            }
        });
    }
}
