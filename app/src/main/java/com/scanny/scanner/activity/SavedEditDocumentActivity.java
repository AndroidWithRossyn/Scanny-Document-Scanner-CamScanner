package com.scanny.scanner.activity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.github.chrisbanes.photoview.PhotoView;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.itextpdf.text.pdf.PdfObject;
import com.scanny.scanner.R;
import com.scanny.scanner.adapter.BookViewPagerAdapter;
import com.scanny.scanner.adapter.SavedToolsAdapter;
import com.scanny.scanner.db.DBHelper;
import com.scanny.scanner.document_view.ViewPagerFixed;
import com.scanny.scanner.main_utils.BitmapUtils;
import com.scanny.scanner.main_utils.Constant;
import com.scanny.scanner.models.BookModel;
import com.scanny.scanner.models.SavedToolType;
import com.scanny.scanner.utils.AdmobAds;
import com.scanny.scanner.utils.AdsUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class SavedEditDocumentActivity extends BaseActivity implements View.OnClickListener, SavedToolsAdapter.OnSavedToolSelected {
    private static final String TAG = "SavedEditDocument";
    public static SavedEditDocumentActivity savedEditDocumentActivity;

    public BookViewPagerAdapter bookViewPagerAdapter;
    public String current_doc_name;
    public DBHelper dataBaseHelper;
    public String edit_doc_grp_name;
    public String from;
    public String noteTxt;
    public Uri pdfUri;
    public int position;
    public String selected_group_name;
    public Uri shareUri;
    public TextView tv_page;
    public ViewPagerFixed viewPager;
    public ArrayList<BookModel> viewPagerList = new ArrayList<>();
    protected ImageView iv_back;
    protected ImageView iv_home;
    protected PhotoView iv_preview_edited;
    protected SavedToolsAdapter savedToolsAdapter;
    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Constant.IdentifyActivity.equals("NoteActivity")) {
                Intent intent2 = new Intent(SavedEditDocumentActivity.this, NoteActivity.class);
                intent2.putExtra("group_name", edit_doc_grp_name);
                intent2.putExtra("current_doc_name", current_doc_name);
                intent2.putExtra("note", noteTxt);
                startActivity(intent2);
                Constant.IdentifyActivity = "";
            } else if (Constant.IdentifyActivity.equals("ImageToTextActivity")) {
                Intent intent3 = new Intent(SavedEditDocumentActivity.this, ImageToTextActivity.class);
                intent3.putExtra("group_name", edit_doc_grp_name);
                startActivity(intent3);
                Constant.IdentifyActivity = "";
            } else if (Constant.IdentifyActivity.equals("PDFViewerActivity")) {
                Intent intent4 = new Intent(SavedEditDocumentActivity.this, PDFViewerActivity.class);
                intent4.putExtra("title", selected_group_name + ".pdf");
                intent4.putExtra("pdf_path", pdfUri.toString());
                startActivity(intent4);
                Constant.IdentifyActivity = "";
            } else if (Constant.IdentifyActivity.equals("DocumentEditorActivity_SavedEdit")) {
                Intent intent5 = new Intent(SavedEditDocumentActivity.this, DocumentEditorActivity.class);
                intent5.putExtra("TAG", SavedEditDocumentActivity.TAG);
                intent5.putExtra("position", position);
                intent5.putExtra("edited_doc_grp_name", edit_doc_grp_name);
                intent5.putExtra("current_doc_name", current_doc_name);
                startActivity(intent5);
                Constant.IdentifyActivity = "";
            } else if (Constant.IdentifyActivity.equals("DocumentEditorActivity_SavedEdit2")) {
                Intent intent6 = new Intent(SavedEditDocumentActivity.this, DocumentEditorActivity.class);
                intent6.putExtra("TAG", "ScannerActivity");
                intent6.putExtra("position", position);
                intent6.putExtra("edited_doc_grp_name", edit_doc_grp_name);
                intent6.putExtra("current_doc_name", current_doc_name);
                startActivityForResult(intent6, 23);
                Constant.IdentifyActivity = "";
            }
        }
    };
    private RecyclerView rv_saved_tools;
    private AdView adView;

    @Override
    public void onResume() {
        super.onResume();
        registerReceiver(broadcastReceiver, new IntentFilter(getPackageName() + ".DocumentEditorActivity_SavedEdit"));
        registerReceiver(broadcastReceiver, new IntentFilter(getPackageName() + ".DocumentEditorActivity_SavedEdit2"));
        registerReceiver(broadcastReceiver, new IntentFilter(getPackageName() + ".PDFViewerActivity"));
        registerReceiver(broadcastReceiver, new IntentFilter(getPackageName() + ".NoteActivity"));
        registerReceiver(broadcastReceiver, new IntentFilter(getPackageName() + ".ImageToTextActivity"));
    }

    @Override
    public void onDestroy() {
        if (shareUri != null) {
            getContentResolver().delete(shareUri, (String) null, (String[]) null);
        }
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
        setContentView(R.layout.activity_saved_edit_document);
        savedEditDocumentActivity = this;
        dataBaseHelper = new DBHelper(this);

        init();
        bindView();
    }

    private void init() {
        adView = findViewById(R.id.adView);
        AdsUtils.showGoogleBannerAd(this, adView);
        AdsUtils.loadGoogleInterstitialAd(this, SavedEditDocumentActivity.this);

        iv_back = (ImageView) findViewById(R.id.iv_back);
        iv_home = (ImageView) findViewById(R.id.iv_home);
        viewPager = (ViewPagerFixed) findViewById(R.id.viewPager);
        tv_page = (TextView) findViewById(R.id.tv_page);
        iv_preview_edited = (PhotoView) findViewById(R.id.iv_preview_edited);
        rv_saved_tools = (RecyclerView) findViewById(R.id.rv_saved_tools);
    }

    private void bindView() {
        edit_doc_grp_name = getIntent().getStringExtra("edit_doc_group_name");
        current_doc_name = getIntent().getStringExtra("current_doc_name");
        position = getIntent().getIntExtra("position", -1);
        from = getIntent().getStringExtra("from");
        if (from.equals("ScannerActivity")) {
            iv_preview_edited.setVisibility(View.GONE);
            viewPager.setVisibility(View.VISIBLE);
            tv_page.setVisibility(View.VISIBLE);
            viewPagerList = ScannerActivity.bookImgList;
            bookViewPagerAdapter = new BookViewPagerAdapter(this, viewPagerList);
            viewPager.setAdapter(bookViewPagerAdapter);
            viewPager.setCurrentItem(position);
            tv_page.setText(String.format("%s / %s", new Object[]{Integer.valueOf(position + 1), Integer.valueOf(viewPagerList.size())}));
            Constant.original = viewPagerList.get(position).getBitmap();
            viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrollStateChanged(int i) {
                }

                @Override
                public void onPageScrolled(int i, float f, int i2) {
                }

                @Override
                public void onPageSelected(int i) {
                    Log.e(SavedEditDocumentActivity.TAG, "onPageSelected: " + i);
                    position = i;
                    Constant.original = ((BookModel) viewPagerList.get(position)).getBitmap();
                    current_doc_name = ((BookModel) viewPagerList.get(position)).getPage_name();
                    tv_page.setText(String.format("%s / %s", new Object[]{Integer.valueOf(position + 1), Integer.valueOf(viewPagerList.size())}));
                    Log.e(SavedEditDocumentActivity.TAG, "onPageSelected: " + current_doc_name);
                }
            });
        } else {
            tv_page.setVisibility(View.GONE);
            viewPager.setVisibility(View.GONE);
            iv_preview_edited.setVisibility(View.VISIBLE);
            if (Constant.original != null) {
                iv_preview_edited.setImageBitmap(Constant.original);
            }
        }
        setSavedToolsAdapter();
    }

    private void setSavedToolsAdapter() {
        rv_saved_tools.setHasFixedSize(true);
        rv_saved_tools.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
        savedToolsAdapter = new SavedToolsAdapter(this);
        rv_saved_tools.setAdapter(savedToolsAdapter);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.iv_back) {
            onBackPressed();
        } else if (id == R.id.iv_home) {
            if (GroupDocumentActivity.groupDocumentActivity != null) {
                GroupDocumentActivity.groupDocumentActivity.finish();
            }
            finish();
        }
    }

    @Override
    public void onSavedToolSelected(SavedToolType savedToolType) {
        switch (savedToolType) {
            case EDIT:
                if (from.equals("ScannerActivity")) {
                    Constant.IdentifyActivity = "DocumentEditorActivity_SavedEdit2";
                    AdsUtils.showGoogleInterstitialAd(SavedEditDocumentActivity.this, true);
                    return;
                }
                Constant.IdentifyActivity = "DocumentEditorActivity_SavedEdit";
                AdsUtils.showGoogleInterstitialAd(SavedEditDocumentActivity.this, true);
                return;
            case OPENPDF:
                ArrayList arrayList = new ArrayList();
                arrayList.add(Constant.original);
                new openPDF(arrayList, edit_doc_grp_name).execute(new String[0]);
                return;
            case NAME:
                updateGroupName(edit_doc_grp_name);
                return;
            case ROTATE:
                new rotateDoc().execute(new String[0]);
                return;
            case NOTE:
                noteTxt = dataBaseHelper.getSingleNote(edit_doc_grp_name, current_doc_name);
                Constant.IdentifyActivity = "NoteActivity";
                AdsUtils.showGoogleInterstitialAd(SavedEditDocumentActivity.this, true);
                return;
            case ImageToText:
                Constant.IdentifyActivity = "ImageToTextActivity";
                AdsUtils.showGoogleInterstitialAd(SavedEditDocumentActivity.this, true);
                return;
            case SHARE:
                shareGroupDoc();
                return;
            case DELETE:
                final Dialog dialog = new Dialog(this, R.style.ThemeWithRoundShape);
                dialog.requestWindowFeature(1);
                dialog.setContentView(R.layout.delete_document_dialog);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
                dialog.getWindow().setLayout(-1, -2);
                dialog.setCanceledOnTouchOutside(false);
                dialog.setCancelable(false);
                if (AdmobAds.SHOW_ADS) {
                    AdmobAds.loadNativeAds(SavedEditDocumentActivity.this, (View) null, (ViewGroup) dialog.findViewById(R.id.admob_native_container), (NativeAdView) dialog.findViewById(R.id.native_ad_view));
                } else {
                    dialog.findViewById(R.id.admob_native_container).setVisibility(View.GONE);
                }
                ((TextView) dialog.findViewById(R.id.tv_delete)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (from.equals("ScannerActivity")) {
                            if (viewPagerList.size() != 1) {
                                dataBaseHelper.deleteSingleDoc(edit_doc_grp_name, current_doc_name);
                                viewPager.setAdapter((PagerAdapter) null);
                                viewPagerList.remove(position);
                                bookViewPagerAdapter.notifyDataSetChanged();
                                bookViewPagerAdapter = new BookViewPagerAdapter(savedEditDocumentActivity, viewPagerList);
                                viewPager.setAdapter(bookViewPagerAdapter);
                                if (position == 0) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Constant.original = ((BookModel) viewPagerList.get(position)).getBitmap();
                                            current_doc_name = ((BookModel) viewPagerList.get(position)).getPage_name();
                                            Log.e(SavedEditDocumentActivity.TAG, "onPageSelected: " + current_doc_name);
                                            if (Constant.inputType.equals("Group")) {
                                                byte[] bytes = BitmapUtils.getBytes(Constant.original);
                                                File externalFilesDir = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
                                                File file = new File(externalFilesDir, System.currentTimeMillis() + ".jpg");
                                                try {
                                                    FileOutputStream fileOutputStream = new FileOutputStream(file);
                                                    fileOutputStream.write(bytes);
                                                    fileOutputStream.close();
                                                } catch (IOException e) {
                                                    Log.w(SavedEditDocumentActivity.TAG, "Cannot write to " + file, e);
                                                }
                                                dataBaseHelper.updateGroupFirstImg(edit_doc_grp_name, file.getPath());
                                            }
                                        }
                                    });
                                } else {
                                    position = 0;
                                    Constant.original = ((BookModel) viewPagerList.get(position)).getBitmap();
                                    current_doc_name = ((BookModel) viewPagerList.get(position)).getPage_name();
                                    Log.e(SavedEditDocumentActivity.TAG, "onPageSelected: " + current_doc_name);
                                }
                                tv_page.setText(String.format("%s / %s", new Object[]{Integer.valueOf(position + 1), Integer.valueOf(viewPagerList.size())}));
                                dialog.dismiss();
                            } else if (Constant.inputType.equals("Group")) {
                                dataBaseHelper.deleteGroup(edit_doc_grp_name);
                                if (GroupDocumentActivity.groupDocumentActivity != null) {
                                    GroupDocumentActivity.groupDocumentActivity.finish();
                                }
                                dialog.dismiss();
                                finish();
                            } else {
                                dataBaseHelper.deleteSingleDoc(edit_doc_grp_name, current_doc_name);
                                dialog.dismiss();
                                finish();
                            }
                        } else if (Constant.inputType.equals("Group")) {
                            dataBaseHelper.deleteGroup(edit_doc_grp_name);
                            dialog.dismiss();
                            finish();
                        } else if (position == 0 && GroupDocumentActivity.currentGroupList.size() == 2) {
                            dataBaseHelper.deleteGroup(edit_doc_grp_name);
                            if (GroupDocumentActivity.groupDocumentActivity != null) {
                                GroupDocumentActivity.groupDocumentActivity.finish();
                            }
                            dialog.dismiss();
                            finish();
                        } else {
                            dataBaseHelper.deleteSingleDoc(edit_doc_grp_name, current_doc_name);
                            if (position == 0) {
                                dataBaseHelper.updateGroupFirstImg(edit_doc_grp_name, GroupDocumentActivity.currentGroupList.get(position + 1).getGroup_doc_img());
                            }
                            dialog.dismiss();
                            finish();
                        }
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
            default:
                return;
        }
    }

    private void updateGroupName(final String str) {
        final Dialog dialog = new Dialog(this, R.style.ThemeWithRoundShape);
        dialog.requestWindowFeature(1);
        dialog.setContentView(R.layout.update_group_name);
        dialog.getWindow().setLayout(-1, -2);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        if (AdmobAds.SHOW_ADS) {
            AdmobAds.loadNativeAds(SavedEditDocumentActivity.this, (View) null, (ViewGroup) dialog.findViewById(R.id.admob_native_container), (NativeAdView) dialog.findViewById(R.id.native_ad_view));
        } else {
            dialog.findViewById(R.id.admob_native_container).setVisibility(View.GONE);
        }
        final EditText editText = (EditText) dialog.findViewById(R.id.et_group_name);
        editText.setText(str);
        editText.setSelection(editText.length());
        ((TextView) dialog.findViewById(R.id.tv_done)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (editText.getText().toString().equals("") || Character.isDigit(editText.getText().toString().charAt(0))) {
                    Toast.makeText(SavedEditDocumentActivity.this, "Please Enter Valid Document Name!", Toast.LENGTH_SHORT).show();
                    return;
                }
                dataBaseHelper.updateGroupName(SavedEditDocumentActivity.this, str, editText.getText().toString().trim());
                dialog.dismiss();
                edit_doc_grp_name = editText.getText().toString();
                GroupDocumentActivity.current_group = editText.getText().toString();
            }
        });
        ((TextView) dialog.findViewById(R.id.tv_cancel)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void shareGroupDoc() {
        final Dialog dialog = new Dialog(this, R.style.ThemeWithRoundShape);
        dialog.requestWindowFeature(1);
        dialog.setContentView(R.layout.share_group_doc);
        dialog.getWindow().setLayout(-1, -2);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        if (AdmobAds.SHOW_ADS) {
            AdmobAds.loadNativeAds(SavedEditDocumentActivity.this, (View) null, (ViewGroup) dialog.findViewById(R.id.admob_native_container), (NativeAdView) dialog.findViewById(R.id.native_ad_view));
        } else {
            dialog.findViewById(R.id.admob_native_container).setVisibility(View.GONE);
        }
        ((RelativeLayout) dialog.findViewById(R.id.rl_share_pdf)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArrayList arrayList = new ArrayList();
                arrayList.add(Constant.original);
                new shareAsPDF(arrayList, PdfObject.TEXT_PDFDOCENCODING, "").execute(new String[0]);
                dialog.dismiss();
            }
        });
        ((RelativeLayout) dialog.findViewById(R.id.rl_share_img)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shareUri = BitmapUtils.getUri(savedEditDocumentActivity, Constant.original);
                Intent intent = new Intent();
                intent.setAction("android.intent.action.SEND");
                intent.setType("image/*");
                intent.putExtra("android.intent.extra.STREAM", shareUri);
                intent.putExtra("android.intent.extra.TEXT", "");
                intent.putExtra("android.intent.extra.SUBJECT", "");
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(Intent.createChooser(intent, (CharSequence) null));
                dialog.dismiss();
            }
        });
        ((RelativeLayout) dialog.findViewById(R.id.rl_share_pdf_pswrd)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sharePDFWithPswrd();
                dialog.dismiss();
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

    public void sharePDFWithPswrd() {
        final Dialog dialog = new Dialog(this, R.style.ThemeWithRoundShape);
        dialog.requestWindowFeature(1);
        dialog.setContentView(R.layout.set_pdf_pswrd);
        dialog.getWindow().setLayout(-1, -2);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        if (AdmobAds.SHOW_ADS) {
            AdmobAds.loadNativeAds(SavedEditDocumentActivity.this, (View) null, (ViewGroup) dialog.findViewById(R.id.admob_native_container), (NativeAdView) dialog.findViewById(R.id.native_ad_view));
        } else {
            dialog.findViewById(R.id.admob_native_container).setVisibility(View.GONE);
        }
        final EditText et_enter_pswrd = (EditText) dialog.findViewById(R.id.et_enter_pswrd);
        final ImageView iv_enter_pswrd_show = (ImageView) dialog.findViewById(R.id.iv_enter_pswrd_show);
        final ImageView iv_enter_pswrd_hide = (ImageView) dialog.findViewById(R.id.iv_enter_pswrd_hide);
        final EditText et_confirm_pswrd = (EditText) dialog.findViewById(R.id.et_confirm_pswrd);
        final ImageView iv_confirm_pswrd_show = (ImageView) dialog.findViewById(R.id.iv_confirm_pswrd_show);
        final ImageView iv_confirm_pswrd_hide = (ImageView) dialog.findViewById(R.id.iv_confirm_pswrd_hide);
        et_enter_pswrd.setInputType(129);
        et_confirm_pswrd.setInputType(129);

        Typeface typeface = Typeface.createFromAsset(getAssets(), "font/inter_medium.ttf");
        et_enter_pswrd.setTypeface(typeface);
        et_confirm_pswrd.setTypeface(typeface);

        iv_enter_pswrd_show.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                iv_enter_pswrd_show.setVisibility(View.GONE);
                iv_enter_pswrd_hide.setVisibility(View.VISIBLE);
                et_enter_pswrd.setTransformationMethod(new HideReturnsTransformationMethod());
                et_enter_pswrd.setSelection(et_enter_pswrd.getText().length());
            }
        });
        iv_enter_pswrd_hide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                iv_enter_pswrd_show.setVisibility(View.VISIBLE);
                iv_enter_pswrd_hide.setVisibility(View.GONE);
                et_enter_pswrd.setTransformationMethod(new PasswordTransformationMethod());
                et_enter_pswrd.setSelection(et_enter_pswrd.getText().length());
            }
        });
        iv_confirm_pswrd_show.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                iv_confirm_pswrd_show.setVisibility(View.GONE);
                iv_confirm_pswrd_hide.setVisibility(View.VISIBLE);
                et_confirm_pswrd.setTransformationMethod(new HideReturnsTransformationMethod());
                et_confirm_pswrd.setSelection(et_enter_pswrd.getText().length());
            }
        });
        iv_confirm_pswrd_hide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                iv_confirm_pswrd_show.setVisibility(View.VISIBLE);
                iv_confirm_pswrd_hide.setVisibility(View.GONE);
                et_confirm_pswrd.setTransformationMethod(new PasswordTransformationMethod());
                et_confirm_pswrd.setSelection(et_enter_pswrd.getText().length());
            }
        });
        ((TextView) dialog.findViewById(R.id.tv_done)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (et_enter_pswrd.getText().toString().equals("") || et_confirm_pswrd.getText().toString().equals("")) {
                    Toast.makeText(getApplicationContext(), "Please Enter Password", Toast.LENGTH_SHORT).show();
                } else if (!et_enter_pswrd.getText().toString().equals(et_confirm_pswrd.getText().toString())) {
                    Toast.makeText(getApplicationContext(), "Your password & Confirm password do not match.", Toast.LENGTH_LONG).show();
                } else {
                    ArrayList arrayList = new ArrayList();
                    arrayList.add(Constant.original);
                    new shareAsPDF(arrayList, "PDF With Password", et_enter_pswrd.getText().toString()).execute(new String[0]);
                    dialog.dismiss();
                }
            }
        });
        ((TextView) dialog.findViewById(R.id.tv_cancel)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    @Override
    public void onActivityResult(int i, int i2, Intent intent) {
        super.onActivityResult(i, i2, intent);
        if (i2 == -1 && i == 23) {
            new updateEditedBookImg().execute(new String[0]);
        }
    }

    public class openPDF extends AsyncTask<String, Void, String> {
        ArrayList<Bitmap> current_bitmap;
        String group_name;
        ProgressDialog progressDialog;

        public openPDF(ArrayList<Bitmap> arrayList, String str) {
            current_bitmap = arrayList;
            group_name = str;
        }

        @Override
        public void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(SavedEditDocumentActivity.this);
            progressDialog.setIndeterminate(true);
            progressDialog.setMessage("Please Wait...");
            progressDialog.setCancelable(false);
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();
        }

        @Override
        public String doInBackground(String... strArr) {
            createPDFfromBitmap(group_name, current_bitmap, "temp");
            return null;
        }

        @Override
        public void onPostExecute(String str) {
            super.onPostExecute(str);
            pdfUri = BaseActivity.getURIFromFile(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + getResources().getString(R.string.app_name) + "/" + group_name + ".pdf", SavedEditDocumentActivity.this);
            progressDialog.dismiss();
            selected_group_name = group_name;
            Constant.IdentifyActivity = "PDFViewerActivity";
            AdsUtils.showGoogleInterstitialAd(SavedEditDocumentActivity.this, true);
        }
    }

    private class shareAsPDF extends AsyncTask<String, Void, String> {
        ArrayList<Bitmap> bitmap;
        String inputType;
        String password;
        ProgressDialog progressDialog;

        private shareAsPDF(ArrayList<Bitmap> arrayList, String str, String str2) {
            bitmap = arrayList;
            inputType = str;
            password = str2;
        }

        @Override
        public void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(SavedEditDocumentActivity.this);
            progressDialog.setIndeterminate(true);
            progressDialog.setMessage("Please Wait...");
            progressDialog.setCancelable(false);
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();
        }

        @Override
        public String doInBackground(String... strArr) {
            if (inputType.equals(PdfObject.TEXT_PDFDOCENCODING)) {

                createPDFfromBitmap(edit_doc_grp_name, bitmap, "temp");
                return null;
            }

            createProtectedPDFfromBitmap(edit_doc_grp_name, bitmap, password, "temp");
            return null;
        }

        @Override
        public void onPostExecute(String str) {
            super.onPostExecute(str);
            Uri uRIFromFile = BaseActivity.getURIFromFile(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + getResources().getString(R.string.app_name) + "/" + edit_doc_grp_name + ".pdf", SavedEditDocumentActivity.this);
            Intent intent = new Intent();
            intent.setAction("android.intent.action.SEND");
            intent.setType("application/pdf");
            intent.putExtra("android.intent.extra.STREAM", uRIFromFile);
            intent.putExtra("android.intent.extra.TEXT", "");
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Intent createChooser = Intent.createChooser(intent, (CharSequence) null);
            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            startActivity(createChooser);
        }
    }

    private class rotateDoc extends AsyncTask<String, Void, String> {
        ProgressDialog progressDialog;

        private rotateDoc() {
        }

        @Override
        public void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(SavedEditDocumentActivity.this);
            progressDialog.setIndeterminate(true);
            progressDialog.setMessage("Please Wait...");
            progressDialog.setCancelable(false);
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();
        }

        @Override
        public String doInBackground(String... strArr) {
            Bitmap bitmap = Constant.original;
            Matrix matrix = new Matrix();
            matrix.postRotate(90.0f);
            Bitmap createBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            Constant.original.recycle();
            System.gc();
            Constant.original = createBitmap;
            byte[] bytes = BitmapUtils.getBytes(Constant.original);
            File externalFilesDir = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
            File file = new File(externalFilesDir, System.currentTimeMillis() + ".jpg");
            try {
                FileOutputStream fileOutputStream = new FileOutputStream(file);
                fileOutputStream.write(bytes);
                fileOutputStream.close();
            } catch (IOException e) {
                Log.w(SavedEditDocumentActivity.TAG, "Cannot write to " + file, e);
            }
            if (from.equals("ScannerActivity")) {
                if (position == 0) {
                    if (Constant.inputType.equals("Group")) {
                        dataBaseHelper.updateGroupFirstImg(edit_doc_grp_name, file.getPath());
                    }
                    dataBaseHelper.updateGroupListDoc(edit_doc_grp_name, current_doc_name, file.getPath());
                    Log.e(SavedEditDocumentActivity.TAG, "doInBackground: " + current_doc_name);
                    return null;
                }
                dataBaseHelper.updateGroupListDoc(edit_doc_grp_name, current_doc_name, file.getPath());
                Log.e(SavedEditDocumentActivity.TAG, "doInBackground: " + current_doc_name);
                return null;
            } else if (Constant.inputType.equals("Group")) {
                dataBaseHelper.updateGroupFirstImg(edit_doc_grp_name, file.getPath());
                dataBaseHelper.updateGroupListDoc(edit_doc_grp_name, current_doc_name, file.getPath());
                return null;
            } else if (position == 0) {
                dataBaseHelper.updateGroupFirstImg(edit_doc_grp_name, file.getPath());
                dataBaseHelper.updateGroupListDoc(edit_doc_grp_name, current_doc_name, file.getPath());
                return null;
            } else {
                dataBaseHelper.updateGroupListDoc(edit_doc_grp_name, current_doc_name, file.getPath());
                return null;
            }
        }

        @Override
        public void onPostExecute(String str) {
            super.onPostExecute(str);
            if (from.equals("ScannerActivity")) {
                viewPager.setAdapter((PagerAdapter) null);
                viewPagerList.remove(position);
                bookViewPagerAdapter.notifyDataSetChanged();
                viewPagerList.add(position, new BookModel(Constant.original, current_doc_name, position));
                bookViewPagerAdapter = new BookViewPagerAdapter(savedEditDocumentActivity, savedEditDocumentActivity.viewPagerList);
                viewPager.setAdapter(bookViewPagerAdapter);
                viewPager.setCurrentItem(position);
            } else {
                iv_preview_edited.setImageBitmap(Constant.original);
            }
            progressDialog.dismiss();
        }
    }

    private class updateEditedBookImg extends AsyncTask<String, Void, String> {
        ProgressDialog progressDialog;

        private updateEditedBookImg() {
        }

        @Override
        public void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(SavedEditDocumentActivity.this);
            progressDialog.setIndeterminate(true);
            progressDialog.setMessage("Please Wait...");
            progressDialog.setCancelable(false);
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();
        }

        @Override
        public String doInBackground(String... strArr) {
            if (Constant.original == null) {
                return null;
            }
            byte[] bytes = BitmapUtils.getBytes(Constant.original);
            File externalFilesDir = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
            File file = new File(externalFilesDir, System.currentTimeMillis() + ".jpg");
            try {
                FileOutputStream fileOutputStream = new FileOutputStream(file);
                fileOutputStream.write(bytes);
                fileOutputStream.close();
            } catch (IOException e) {
                Log.w(SavedEditDocumentActivity.TAG, "Cannot write to " + file, e);
            }
            if (position == 0) {
                if (Constant.inputType.equals("Group")) {
                    dataBaseHelper.updateGroupFirstImg(edit_doc_grp_name, file.getPath());
                }
                dataBaseHelper.updateGroupListDoc(edit_doc_grp_name, current_doc_name, file.getPath());
                Log.e(SavedEditDocumentActivity.TAG, "doInBackground: " + current_doc_name);
                return null;
            }
            dataBaseHelper.updateGroupListDoc(edit_doc_grp_name, current_doc_name, file.getPath());
            Log.e(SavedEditDocumentActivity.TAG, "doInBackground: " + current_doc_name);
            return null;
        }

        @Override
        public void onPostExecute(String str) {
            super.onPostExecute(str);
            viewPager.setAdapter((PagerAdapter) null);
            viewPagerList.remove(position);
            bookViewPagerAdapter.notifyDataSetChanged();
            viewPagerList.add(position, new BookModel(Constant.original, current_doc_name, position));
            bookViewPagerAdapter = new BookViewPagerAdapter(savedEditDocumentActivity, savedEditDocumentActivity.viewPagerList);
            viewPager.setAdapter(bookViewPagerAdapter);
            viewPager.setCurrentItem(position);
            progressDialog.dismiss();
        }
    }
}
