package com.scanny.scanner.activity;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.core.internal.view.SupportMenu;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.OnColorSelectedListener;
import com.flask.colorpicker.builder.ColorPickerClickListener;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;
import com.nguyenhoanglam.imagepicker.model.Image;
import com.nguyenhoanglam.imagepicker.ui.imagepicker.ImagePicker;
import com.scanny.scanner.R;
import com.scanny.scanner.db.DBHelper;
import com.scanny.scanner.main_utils.BitmapUtils;
import com.scanny.scanner.main_utils.Constant;
import com.scanny.scanner.models.DBModel;
import com.scanny.scanner.scrapbook.ImageStickerConfig;
import com.scanny.scanner.scrapbook.LocalDisplay;
import com.scanny.scanner.scrapbook.StickerConfigInterface;
import com.scanny.scanner.scrapbook.StickerHolderView;
import com.scanny.scanner.scrapbook.TextStickerConfig;
import com.scanny.scanner.utils.AdsUtils;
import com.takwolf.android.aspectratio.AspectRatioLayout;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;

public class IDCardPreviewActivity extends BaseActivity implements View.OnClickListener, StickerHolderView.OnStickerSelectionCallback {
    private static final String TAG = "IDCardPreviewActivity";

    public AspectRatioLayout aspectRatioLayout;
    public int color = SupportMenu.CATEGORY_MASK;
    public DBHelper dbHelper;
    public ImageView iv_bg_color;
    public LinearLayout ly_main;
    public RelativeLayout rl_main;
    public StickerHolderView stickerHolderView;
    protected Bitmap backSide;
    protected Bitmap finalBitmap;
    protected Bitmap frontSide;
    protected ImageView iv_add_new;
    protected ImageView iv_back;
    protected ImageView iv_done;
    protected ImageView iv_edit;
    protected ImageView iv_horizontal;
    protected ImageView iv_left;
    protected ImageView iv_right;
    protected ImageView iv_scrap;
    protected ImageView iv_vertical;
    protected LinearLayout ly_color;
    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Constant.IdentifyActivity.equals("DocumentEditorActivity_IDCard")) {
                Intent intent2 = new Intent(IDCardPreviewActivity.this, DocumentEditorActivity.class);
                intent2.putExtra("TAG", IDCardPreviewActivity.TAG);
                startActivityForResult(intent2, 14);
                Constant.IdentifyActivity = "";
            } else if (Constant.IdentifyActivity.equals("IDCardGalleryActivity")) {
                ImagePicker.with((Activity) IDCardPreviewActivity.this)
                        .setStatusBarColor("#25c4a4")
                        .setToolbarColor("#25c4a4")
                        .setBackgroundColor("#ffffff")
                        .setFolderMode(true)
                        .setFolderTitle("Gallery")
                        .setMultipleMode(true)
                        .setShowNumberIndicator(true)
                        .setAlwaysShowDoneButton(true)
                        .setMaxSize(7)
                        .setShowCamera(false)
                        .setLimitMessage("You can select up to 7 images")
                        .setRequestCode(100)
                        .start();
                Constant.IdentifyActivity = "";
            }
        }
    };
    private LinearLayout ly_add_new, ly_scrap, ly_edit, ly_left_rotate, ly_right_rotate, ly_horizontal, ly_vertical;
    private LinearLayout ly_scrap_view;
    private TextView txtScrap;

    @Override
    public void onTextStickerSelected(TextStickerConfig textStickerConfig, boolean z) {
    }

    @Override
    public void onResume() {
        super.onResume();

        registerReceiver(broadcastReceiver, new IntentFilter(getPackageName() + ".DocumentEditorActivity_IDCard"));

        registerReceiver(broadcastReceiver, new IntentFilter(getPackageName() + ".IDCardGalleryActivity"));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (broadcastReceiver != null) {
            try {
                unregisterReceiver(broadcastReceiver);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        LocalDisplay.init(this);
        setContentView(R.layout.activity_idcard_preview);
        dbHelper = new DBHelper(this);
        AdsUtils.loadGoogleInterstitialAd(this, IDCardPreviewActivity.this);
        init();
        bindView();
    }

    private void init() {
        txtScrap = (TextView) findViewById(R.id.txtScrap);
        ly_vertical = (LinearLayout) findViewById(R.id.ly_vertical);
        ly_horizontal = (LinearLayout) findViewById(R.id.ly_horizontal);
        ly_right_rotate = (LinearLayout) findViewById(R.id.ly_right_rotate);
        ly_left_rotate = (LinearLayout) findViewById(R.id.ly_left_rotate);
        ly_edit = (LinearLayout) findViewById(R.id.ly_edit);
        ly_add_new = (LinearLayout) findViewById(R.id.ly_add_new);
        ly_main = (LinearLayout) findViewById(R.id.ly_main);
        iv_back = (ImageView) findViewById(R.id.iv_back);
        iv_done = (ImageView) findViewById(R.id.iv_done);
        rl_main = (RelativeLayout) findViewById(R.id.rl_main);
        aspectRatioLayout = (AspectRatioLayout) findViewById(R.id.aspectRatioLayout);
        stickerHolderView = (StickerHolderView) findViewById(R.id.stickerHolderView);
        iv_bg_color = (ImageView) findViewById(R.id.iv_bg_color);
        iv_add_new = (ImageView) findViewById(R.id.iv_add_new);
        ly_color = (LinearLayout) findViewById(R.id.ly_color);
        iv_scrap = (ImageView) findViewById(R.id.iv_scrap);
        ly_scrap = (LinearLayout) findViewById(R.id.ly_scrap);
        ly_scrap_view = (LinearLayout) findViewById(R.id.ly_scrap_view);
        iv_edit = (ImageView) findViewById(R.id.iv_edit);
        iv_left = (ImageView) findViewById(R.id.iv_left);
        iv_right = (ImageView) findViewById(R.id.iv_right);
        iv_horizontal = (ImageView) findViewById(R.id.iv_horizontal);
        iv_vertical = (ImageView) findViewById(R.id.iv_vertical);
    }

    private void bindView() {
        aspectRatioLayout.setAspectRatio(3.0f, 4.0f);
        stickerHolderView.setTextStickerSelectionCallback(this);
        if (!Constant.current_camera_view.equals("ID Card") || !Constant.card_type.equals("Single")) {
            frontSide = ScannerActivity.idcardImgList.get(0);
            backSide = ScannerActivity.idcardImgList.get(1);
            stickerHolderView.addStickerView(new ImageStickerConfig(frontSide, StickerConfigInterface.STICKER_TYPE.IMAGE));
            stickerHolderView.addStickerView(new ImageStickerConfig(backSide, StickerConfigInterface.STICKER_TYPE.IMAGE));
        } else if (Constant.singleSideBitmap != null) {
            stickerHolderView.addStickerView(new ImageStickerConfig(Constant.singleSideBitmap, StickerConfigInterface.STICKER_TYPE.IMAGE));
        }
    }

    @Override
    public void onImageStickerSelected(ImageStickerConfig imageStickerConfig, boolean z) {
        iv_scrap.setImageResource(R.drawable.ic_scrap_selection);
        txtScrap.setTextColor(getResources().getColor(R.color.black));
        ly_scrap_view.setVisibility(View.VISIBLE);
        Constant.original = imageStickerConfig.getBitmapImage();
    }

    @Override
    public void onNoneStickerSelected() {
        iv_scrap.setImageResource(R.drawable.ic_scrap);
        txtScrap.setTextColor(getResources().getColor(R.color.white));
        ly_scrap_view.setVisibility(View.GONE);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ly_add_new:
                Constant.IdentifyActivity = "IDCardGalleryActivity";
                AdsUtils.showGoogleInterstitialAd(IDCardPreviewActivity.this, true);
                return;
            case R.id.iv_back:
                onBackPressed();
                return;
            case R.id.iv_done:
                aspectRatioLayout.setDrawingCacheEnabled(true);
                stickerHolderView.leaveSticker();
                iv_scrap.setImageResource(R.drawable.ic_scrap);
                txtScrap.setTextColor(getResources().getColor(R.color.white));
                ly_scrap_view.setVisibility(View.GONE);
                new saveIDCard().execute(new Bitmap[0]);
                return;
            case R.id.ly_edit:
                Constant.IdentifyActivity = "DocumentEditorActivity_IDCard";
                AdsUtils.showGoogleInterstitialAd(IDCardPreviewActivity.this, true);
                return;
            case R.id.ly_horizontal:
                stickerHolderView.flipStickerHorizontal(true);
                return;
            case R.id.ly_left_rotate:
                stickerHolderView.rightRotate(true);
                return;
            case R.id.ly_right_rotate:
                stickerHolderView.leftRotate(true);
                return;
            case R.id.ly_scrap:
                if (ly_scrap_view.getVisibility() == View.VISIBLE) {
                    iv_scrap.setImageResource(R.drawable.ic_scrap);
                    txtScrap.setTextColor(getResources().getColor(R.color.white));
                    ly_scrap_view.setVisibility(View.GONE);
                    return;
                } else if (ly_scrap_view.getVisibility() == View.GONE) {
                    iv_scrap.setImageResource(R.drawable.ic_scrap_selection);
                    txtScrap.setTextColor(getResources().getColor(R.color.black));
                    ly_scrap_view.setVisibility(View.VISIBLE);
                    return;
                } else {
                    return;
                }
            case R.id.ly_vertical:
                stickerHolderView.flipSticker(true);
                return;
            case R.id.ly_color:
                ColorPickerDialogBuilder.with(this).setTitle("Choose color").initialColor(color).wheelType(ColorPickerView.WHEEL_TYPE.FLOWER).density(10).setOnColorSelectedListener(new OnColorSelectedListener() {
                    public void onColorSelected(int i) {
                    }
                }).setPositiveButton((CharSequence) "ok", (ColorPickerClickListener) new ColorPickerClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i, Integer[] numArr) {
                        color = i;
                        iv_bg_color.setBackgroundColor(i);
                        ly_main.setBackgroundColor(i);
                    }
                }).setNegativeButton((CharSequence) "cancel", (DialogInterface.OnClickListener) new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                }).build().show();
                return;
            default:
                return;
        }
    }

    @Override
    public void onActivityResult(int i, int i2, Intent intent) {
        super.onActivityResult(i, i2, intent);
        if (ImagePicker.shouldHandleResult(i, i2, intent, 100)) {
            Iterator<Image> it = ImagePicker.getImages(intent).iterator();
            while (it.hasNext()) {
                Image next = it.next();
                if (Build.VERSION.SDK_INT >= 29) {
                    Glide.with(getApplicationContext()).asBitmap().load(next.getUri()).into(new SimpleTarget<Bitmap>() {
                        public void onResourceReady(Bitmap bitmap, Transition<? super Bitmap> transition) {
                            Constant.original = bitmap;
                            stickerHolderView.addStickerView(new ImageStickerConfig(bitmap, StickerConfigInterface.STICKER_TYPE.IMAGE));
                        }
                    });
                } else {
                    Glide.with(getApplicationContext()).asBitmap().load(next.getPath()).into(new SimpleTarget<Bitmap>() {
                        public void onResourceReady(Bitmap bitmap, Transition<? super Bitmap> transition) {
                            Constant.original = bitmap;
                            stickerHolderView.addStickerView(new ImageStickerConfig(bitmap, StickerConfigInterface.STICKER_TYPE.IMAGE));
                        }
                    });
                }
            }
        }
        if (i2 == -1 && i == 14) {
            stickerHolderView.setEditImageOnSticker(Constant.original);
        }
    }

    @Override
    public void onBackPressed() {
        final Dialog dialog = new Dialog(this, R.style.ThemeWithRoundShape);
        dialog.requestWindowFeature(1);
        dialog.setContentView(R.layout.editor_screen_exit_dailog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        dialog.getWindow().setLayout(-1, -2);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        ((TextView) dialog.findViewById(R.id.iv_exit)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                finish();
            }
        });
        ((ImageView) dialog.findViewById(R.id.iv_close)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private class saveIDCard extends AsyncTask<Bitmap, Void, Bitmap> {
        String current_doc_name;
        String group_date;
        String group_name;
        ProgressDialog progressDialog;

        private saveIDCard() {
        }

        @Override
        public void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(IDCardPreviewActivity.this);
            progressDialog.setIndeterminate(true);
            progressDialog.setMessage("Please wait...");
            progressDialog.setCancelable(false);
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();
        }

        @Override
        public Bitmap doInBackground(Bitmap... bitmapArr) {
            finalBitmap = aspectRatioLayout.getDrawingCache();
            finalBitmap = getMainFrameBitmap(rl_main);
            if (finalBitmap == null) {
                return null;
            }
            byte[] bytes = BitmapUtils.getBytes(finalBitmap);
            File externalFilesDir = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
            File file = new File(externalFilesDir, System.currentTimeMillis() + ".jpg");
            try {
                FileOutputStream fileOutputStream = new FileOutputStream(file);
                fileOutputStream.write(bytes);
                fileOutputStream.close();
            } catch (IOException e) {
                Log.w(IDCardPreviewActivity.TAG, "Cannot write to " + file, e);
            }
            if (Constant.inputType.equals("Group")) {
                group_name = "Scanny" + Constant.getDateTime("_ddMMHHmmss");
                group_date = Constant.getDateTime("yyyy-MM-dd  hh:mm a");
                current_doc_name = "Doc_" + System.currentTimeMillis();
                dbHelper.createDocTable(group_name);
                dbHelper.addGroup(new DBModel(group_name, group_date, file.getPath(), Constant.current_tag));
                dbHelper.addGroupDoc(group_name, file.getPath(), current_doc_name, "Insert text here...");
                return null;
            }
            group_name = GroupDocumentActivity.current_group;
            current_doc_name = "Doc_" + System.currentTimeMillis();
            dbHelper.addGroupDoc(group_name, file.getPath(), current_doc_name, "Insert text here...");
            return null;
        }

        @Override
        public void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            progressDialog.dismiss();

            Intent intent2 = new Intent(IDCardPreviewActivity.this, GroupDocumentActivity.class);
            intent2.putExtra("current_group", group_name);
            startActivity(intent2);
            Constant.IdentifyActivity = "";

            finish();
        }
    }
}
