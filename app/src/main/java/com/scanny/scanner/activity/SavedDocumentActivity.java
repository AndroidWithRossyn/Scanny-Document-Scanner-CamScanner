package com.scanny.scanner.activity;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.scanny.scanner.R;
import com.scanny.scanner.db.DBHelper;
import com.scanny.scanner.main_utils.Constant;
import com.scanny.scanner.utils.AdmobAds;
import com.scanny.scanner.utils.AdsUtils;


public class SavedDocumentActivity extends BaseActivity implements View.OnClickListener {
    private static final String TAG = "SavedDocumentActivity";
    public String current_doc_name;
    public DBHelper dataBaseHelper;
    public String preview_doc_grp_name;
    protected ImageView iv_back;
    protected ImageView iv_delete;
    protected ImageView iv_edit;
    protected ImageView iv_retake;
    protected ImageView iv_rotate;
    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Constant.IdentifyActivity.equals("ScannerActivity_Retake2")) {
                startActivity(new Intent(SavedDocumentActivity.this, ScannerActivity.class));
                Constant.IdentifyActivity = "";
                finish();
            } else if (Constant.IdentifyActivity.equals("DocumentEditorActivity_Saved")) {
                Intent intent2 = new Intent(SavedDocumentActivity.this, DocumentEditorActivity.class);
                intent2.putExtra("TAG", SavedDocumentActivity.TAG);
                intent2.putExtra("scan_doc_group_name", preview_doc_grp_name);
                intent2.putExtra("current_doc_name", current_doc_name);
                startActivity(intent2);
                Constant.IdentifyActivity = "";
                finish();
            }
        }
    };
    private ImageView iv_preview_saved;
    private LinearLayout llRetake, llEdit, llRotate, llDelete;
    private AdView adView;

    @Override
    public void onResume() {
        super.onResume();

        registerReceiver(broadcastReceiver, new IntentFilter(getPackageName() + ".ScannerActivity_Retake2"));
        registerReceiver(broadcastReceiver, new IntentFilter(getPackageName() + ".DocumentEditorActivity_Saved"));
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
        setContentView(R.layout.activity_saved_document);
        dataBaseHelper = new DBHelper(this);

        init();
    }

    private void init() {
        adView = findViewById(R.id.adView);
        AdsUtils.showGoogleBannerAd(this, adView);
        AdsUtils.loadGoogleInterstitialAd(this, SavedDocumentActivity.this);

        iv_back = (ImageView) findViewById(R.id.iv_back);
        iv_preview_saved = (ImageView) findViewById(R.id.iv_preview_saved);

        llRetake = (LinearLayout) findViewById(R.id.llRetake);
        llEdit = (LinearLayout) findViewById(R.id.llEdit);
        llRotate = (LinearLayout) findViewById(R.id.llRotate);
        llDelete = (LinearLayout) findViewById(R.id.llDelete);

        iv_retake = (ImageView) findViewById(R.id.iv_retake);
        iv_edit = (ImageView) findViewById(R.id.iv_edit);
        iv_rotate = (ImageView) findViewById(R.id.iv_rotate);
        iv_delete = (ImageView) findViewById(R.id.iv_delete);
        if (Constant.original != null) {
            iv_preview_saved.setImageBitmap(Constant.original);
        }
        preview_doc_grp_name = getIntent().getStringExtra("scan_doc_group_name");
        current_doc_name = getIntent().getStringExtra("current_doc_name");
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_back:
                onBackPressed();
                return;
            case R.id.llDelete:
                final Dialog dialog = new Dialog(this, R.style.ThemeWithRoundShape);
                dialog.requestWindowFeature(1);
                dialog.setContentView(R.layout.delete_document_dialog);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
                dialog.getWindow().setLayout(-1, -2);
                dialog.setCanceledOnTouchOutside(false);
                dialog.setCancelable(false);
                if (AdmobAds.SHOW_ADS) {
                    AdmobAds.loadNativeAds(SavedDocumentActivity.this, (View) null, (ViewGroup) dialog.findViewById(R.id.admob_native_container), (NativeAdView) dialog.findViewById(R.id.native_ad_view));
                } else {
                    dialog.findViewById(R.id.admob_native_container).setVisibility(View.GONE);
                }
                ((TextView) dialog.findViewById(R.id.tv_delete)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (Constant.inputType.equals("Group")) {
                            dataBaseHelper.deleteGroup(preview_doc_grp_name);
                        } else {
                            dataBaseHelper.deleteSingleDoc(preview_doc_grp_name, current_doc_name);
                        }
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
                return;
            case R.id.llEdit:
                Constant.IdentifyActivity = "DocumentEditorActivity_Saved";
                AdsUtils.showGoogleInterstitialAd(SavedDocumentActivity.this, true);
                return;
            case R.id.llRetake:
                Constant.IdentifyActivity = "ScannerActivity_Retake2";
                AdsUtils.showGoogleInterstitialAd(SavedDocumentActivity.this, true);
                return;
            case R.id.llRotate:
                Bitmap bitmap = Constant.original;
                Matrix matrix = new Matrix();
                matrix.postRotate(90.0f);
                Bitmap createBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                Constant.original.recycle();
                System.gc();
                Constant.original = createBitmap;
                iv_preview_saved.setImageBitmap(Constant.original);
                return;
            default:
                return;
        }
    }
}
