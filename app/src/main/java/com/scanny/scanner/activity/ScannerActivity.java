package com.scanny.scanner.activity;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.github.chrisbanes.photoview.PhotoView;
import com.nguyenhoanglam.imagepicker.model.Image;
import com.nguyenhoanglam.imagepicker.ui.imagepicker.ImagePicker;
import com.otaliastudios.cameraview.CameraListener;
import com.otaliastudios.cameraview.CameraView;
import com.otaliastudios.cameraview.PictureResult;
import com.otaliastudios.cameraview.VideoResult;
import com.otaliastudios.cameraview.controls.Facing;
import com.otaliastudios.cameraview.controls.Flash;
import com.scanlibrary.ScanActivity;
import com.scanny.scanner.R;
import com.scanny.scanner.db.DBHelper;
import com.scanny.scanner.main_utils.BitmapUtils;
import com.scanny.scanner.main_utils.Constant;
import com.scanny.scanner.models.BookModel;
import com.scanny.scanner.models.DBModel;
import com.scanny.scanner.utils.AdsUtils;
import com.yalantis.ucrop.UCrop;
import com.yalantis.ucrop.util.FileUtils;

import org.bouncycastle.crypto.tls.CipherSuite;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import me.pqpo.smartcropperlib.view.CropImageView;

public class ScannerActivity extends BaseActivity implements ActivityCompat.OnRequestPermissionsResultCallback/*, AspectRatioFragment.Listener*/, View.OnClickListener {
    private static final int[] FLASH_ICONS = {R.drawable.ic_flash_auto, R.drawable.ic_flash_off, R.drawable.ic_flash_on};
    private static final int[] FLASH_SETTING = {2, 0, 1};
    private static final String FRAGMENT_DIALOG = "dialog";
    private static final int REQUEST_CAMERA_PERMISSION = 1;
    private static final String TAG = "ScannerActivity";
    public static ArrayList<Bitmap> bitmapList = new ArrayList<>();
    public static ArrayList<BookModel> bookImgList = new ArrayList<>();
    public static ArrayList<Bitmap> idcardImgList = new ArrayList<>();
    public CameraView cameraView;
    public String current_docs_name;
    public DBHelper dbHelper;
    public boolean isBackSide = false;
    public CropImageView iv_card_crop;
    public PhotoView iv_card_filter;
    public ImageView iv_done;
    public ImageView iv_switch_flash;
    public LinearLayout ly_camera;
    public LinearLayout ly_crop;
    public Bitmap originalBitmap;
    public ProgressBar progressBar;
    public String selected_group_name;
    public Uri sourceUri;
    public Bitmap tempBitmap;
    public TextView tv_id_card;
    protected Uri destinationUri;
    protected ImageView iv_back_camera;
    protected ImageView iv_back_crop;
    protected ImageView iv_back_filter;
    protected TextView iv_continue;
    protected ImageView iv_done_filter;
    protected ImageView iv_full_crop;
    protected ImageView iv_gallery;
    protected TextView iv_retake;
    protected ImageView iv_switch_camera;
    protected ImageView iv_take_picture;
    protected LinearLayout ly_current_filter;
    protected LinearLayout ly_rotate_doc;
    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Constant.IdentifyActivity.equals("ScannerGalleryActivity")) {
                ImagePicker.with((Activity) ScannerActivity.this)
                        .setStatusBarColor("#25c4a4")
                        .setToolbarColor("#25c4a4")
                        .setBackgroundColor("#ffffff")
                        .setFolderMode(true)
                        .setFolderTitle("Gallery")
                        .setMultipleMode(true)
                        .setShowNumberIndicator(true)
                        .setAlwaysShowDoneButton(true)
                        .setMaxSize(1)
                        .setShowCamera(false)
                        .setLimitMessage("You can select up to 1 images")
                        .setRequestCode(100)
                        .start();
                Constant.IdentifyActivity = "";
            } else if (Constant.IdentifyActivity.equals("CropDocumentActivity2")) {
                startActivity(new Intent(ScannerActivity.this, CropDocumentActivity.class));
                Constant.IdentifyActivity = "";
                finish();
            } else if (Constant.IdentifyActivity.equals("IDCardPreviewActivity")) {
                startActivity(new Intent(ScannerActivity.this, IDCardPreviewActivity.class));
                Constant.IdentifyActivity = "";
                finish();
            } else if (Constant.IdentifyActivity.equals("SavedEditDocumentActivity3")) {
                Intent intent2 = new Intent(ScannerActivity.this, SavedEditDocumentActivity.class);
                intent2.putExtra("edit_doc_group_name", selected_group_name);
                intent2.putExtra("current_doc_name", bookImgList.get(0).getPage_name());
                intent2.putExtra("position", bookImgList.get(0).getPos());
                intent2.putExtra("from", TAG);
                startActivity(intent2);
                Constant.IdentifyActivity = "";
                finish();
            } else if (Constant.IdentifyActivity.equals("UcropActivity")) {
                UCrop.of(sourceUri, destinationUri).start((Activity) ScannerActivity.this, 69);
                Constant.IdentifyActivity = "";
            } else if (Constant.IdentifyActivity.equals("DocumentEditorActivity_Scanner")) {
                Intent intent3 = new Intent(ScannerActivity.this, DocumentEditorActivity.class);
                intent3.putExtra("TAG", "SavedDocumentActivity");
                intent3.putExtra("scan_doc_group_name", selected_group_name);
                intent3.putExtra("current_doc_name", current_docs_name);
                startActivity(intent3);
                finish();
            }
        }
    };
    private int current_flash;
    private TextView iv_color;
    private TextView iv_ocv_black;
    private TextView iv_original;
    private TextView iv_sharp_black;
    private LinearLayout ly_filter;
    private ProgressDialog progressDialog;
    private RelativeLayout rl_book_view;
    private RelativeLayout rl_idcard_view;
    private TextView tv_book;
    private TextView tv_document;
    private TextView tv_idcard;
    private TextView tv_photo;
    private View v_book;
    private View v_document;
    private View v_idcard;
    private View v_photo;

    public static int getCameraPhotoOrientation(String str) {
        try {
            int attributeInt = new ExifInterface(new File(str).getAbsolutePath()).getAttributeInt(androidx.exifinterface.media.ExifInterface.TAG_ORIENTATION, 1);
            if (attributeInt == 3) {
                return CipherSuite.TLS_DHE_PSK_WITH_NULL_SHA256;
            }
            if (attributeInt == 6) {
                return 90;
            }
            if (attributeInt != 8) {
                return 0;
            }
            return 270;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static Bitmap scalePreserveRatio(Bitmap bitmap, int i, int i2) {
        if (i2 <= 0 || i <= 0 || bitmap == null) {
            return bitmap;
        }
        float f = (float) i;
        float width = (float) bitmap.getWidth();
        float f2 = f / width;
        float f3 = (float) i2;
        float height = (float) bitmap.getHeight();
        float f4 = f3 / height;
        int floor = (int) Math.floor((double) (width * f2));
        int floor2 = (int) Math.floor((double) (f2 * height));
        if (floor > i || floor2 > i2) {
            floor = (int) Math.floor((double) (width * f4));
            floor2 = (int) Math.floor((double) (height * f4));
        }
        Bitmap createScaledBitmap = Bitmap.createScaledBitmap(bitmap, floor, floor2, true);
        Bitmap createBitmap = Bitmap.createBitmap(i, i2, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(createBitmap);
        float f5 = ((float) floor) / ((float) floor2);
        float f6 = f / f3;
        float f7 = 0.0f;
        float f8 = f5 >= f6 ? 0.0f : ((float) (i - floor)) / 2.0f;
        if (f5 >= f6) {
            f7 = ((float) (i2 - floor2)) / 2.0f;
        }
        canvas.drawBitmap(createScaledBitmap, f8, f7, (Paint) null);
        return createBitmap;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (ContextCompat.checkSelfPermission(this, "android.permission.CAMERA") == PackageManager.PERMISSION_GRANTED) {
            cameraView.open();
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(this, "android.permission.CAMERA")) {
            ConfirmationDialogFragment.newInstance(R.string.camera_permission_confirmation, new String[]{"android.permission.CAMERA"}, 1, R.string.camera_permission_not_granted).show(getSupportFragmentManager(), FRAGMENT_DIALOG);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{"android.permission.CAMERA"}, 1);
        }

        registerReceiver(broadcastReceiver, new IntentFilter(getPackageName() + ".ScannerGalleryActivity"));

        registerReceiver(broadcastReceiver, new IntentFilter(getPackageName() + ".CropDocumentActivity2"));

        registerReceiver(broadcastReceiver, new IntentFilter(getPackageName() + ".IDCardPreviewActivity"));

        registerReceiver(broadcastReceiver, new IntentFilter(getPackageName() + ".SavedEditDocumentActivity3"));

        registerReceiver(broadcastReceiver, new IntentFilter(getPackageName() + ".UcropActivity"));

        registerReceiver(broadcastReceiver, new IntentFilter(getPackageName() + ".DocumentEditorActivity_Scanner"));
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
        getWindow().setFlags(1024, 1024);
        setContentView(R.layout.activity_scanner);
        dbHelper = new DBHelper(this);
        init();
        bindView();

        AdsUtils.loadGoogleInterstitialAd(this, ScannerActivity.this);
    }

    private void init() {
        ly_camera = (LinearLayout) findViewById(R.id.ly_camera);
        iv_back_camera = (ImageView) findViewById(R.id.iv_back_camera);
        iv_switch_flash = (ImageView) findViewById(R.id.iv_switch_flash);
        iv_done = (ImageView) findViewById(R.id.iv_done);
        tv_document = (TextView) findViewById(R.id.tv_document);
        tv_book = (TextView) findViewById(R.id.tv_book);
        tv_idcard = (TextView) findViewById(R.id.tv_idcard);
        tv_photo = (TextView) findViewById(R.id.tv_photo);
        v_document = findViewById(R.id.v_document);
        v_book = findViewById(R.id.v_book);
        v_idcard = findViewById(R.id.v_idcard);
        v_photo = findViewById(R.id.v_photo);
        cameraView = (CameraView) findViewById(R.id.cameraView);
        cameraView.setLifecycleOwner(this);
        rl_book_view = (RelativeLayout) findViewById(R.id.rl_book_view);
        rl_idcard_view = (RelativeLayout) findViewById(R.id.rl_idcard_view);
        tv_id_card = (TextView) findViewById(R.id.tv_id_card);
        iv_gallery = (ImageView) findViewById(R.id.iv_gallery);
        iv_take_picture = (ImageView) findViewById(R.id.iv_take_picture);
        iv_switch_camera = (ImageView) findViewById(R.id.iv_switch_camera);
        ly_crop = (LinearLayout) findViewById(R.id.ly_crop);
        iv_back_crop = (ImageView) findViewById(R.id.iv_back_crop);
        iv_full_crop = (ImageView) findViewById(R.id.iv_full_crop);
        iv_card_crop = (CropImageView) findViewById(R.id.iv_card_crop);
        ly_rotate_doc = (LinearLayout) findViewById(R.id.ly_rotate_doc);
        ly_current_filter = (LinearLayout) findViewById(R.id.ly_current_filter);
        iv_retake = (TextView) findViewById(R.id.iv_retake);
        iv_continue = (TextView) findViewById(R.id.iv_continue);
        ly_filter = (LinearLayout) findViewById(R.id.ly_filter);
        iv_back_filter = (ImageView) findViewById(R.id.iv_back_filter);
        iv_done_filter = (ImageView) findViewById(R.id.iv_done_filter);
        iv_card_filter = (PhotoView) findViewById(R.id.iv_card_filter);
        iv_original = (TextView) findViewById(R.id.iv_original);
        iv_color = (TextView) findViewById(R.id.iv_color);
        iv_sharp_black = (TextView) findViewById(R.id.iv_sharp_black);
        iv_ocv_black = (TextView) findViewById(R.id.iv_ocv_black);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
    }

    private void bindView() {

        if (cameraView != null) {
            cameraView.addCameraListener(new CameraListener() {
                @Override
                public void onPictureTaken(PictureResult result) {
                    // Access the raw data if needed.
                    byte[] data = result.getData();
                    Log.e(TAG, "onPictureTaken " + data.length);
                    Toast.makeText(ScannerActivity.this, "Picture Taken", Toast.LENGTH_SHORT).show();
                    if (Constant.current_camera_view.equals("Document") || (Constant.current_camera_view.equals("ID Card") && Constant.card_type.equals("Single"))) {
                        File externalFilesDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                        File file = new File(externalFilesDir, System.currentTimeMillis() + ".jpg");
                        try {
                            FileOutputStream fileOutputStream = new FileOutputStream(file);
                            fileOutputStream.write(data);
                            fileOutputStream.close();
                            Bitmap decodeByteArray = BitmapFactory.decodeByteArray(data, 0, data.length);
                            Bitmap scalePreserveRatio = scalePreserveRatio(decodeByteArray, decodeByteArray.getWidth() / 2, decodeByteArray.getHeight() / 2);
                            int cameraPhotoOrientation = getCameraPhotoOrientation(file.getPath());
                            Matrix matrix = new Matrix();
                            matrix.postRotate((float) cameraPhotoOrientation);
                            Constant.original = Bitmap.createBitmap(scalePreserveRatio, 0, 0, scalePreserveRatio.getWidth(), scalePreserveRatio.getHeight(), matrix, true);
                            progressBar.setVisibility(View.GONE);
                            startActivity(new Intent(ScannerActivity.this, CropDocumentActivity.class));
                            finish();
                        } catch (IOException e) {
                            Log.w(TAG, "Cannot write to " + file, e);
                        }
                    } else if (!Constant.current_camera_view.equals("ID Card") || !Constant.card_type.equals("Double")) {
                        if (Constant.current_camera_view.equals("Book")) {
                            File externalFilesDir2 = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                            File file2 = new File(externalFilesDir2, System.currentTimeMillis() + ".jpg");
                            try {
                                FileOutputStream fileOutputStream2 = new FileOutputStream(file2);
                                fileOutputStream2.write(data);
                                fileOutputStream2.close();
                                Bitmap decodeByteArray2 = BitmapFactory.decodeByteArray(data, 0, data.length);
                                Bitmap scalePreserveRatio2 = scalePreserveRatio(decodeByteArray2, decodeByteArray2.getWidth() / 2, decodeByteArray2.getHeight() / 2);
                                int cameraPhotoOrientation2 = getCameraPhotoOrientation(file2.getPath());
                                Matrix matrix2 = new Matrix();
                                matrix2.postRotate((float) cameraPhotoOrientation2);
                                Constant.bookBitmap = Bitmap.createBitmap(scalePreserveRatio2, 0, 0, scalePreserveRatio2.getWidth(), scalePreserveRatio2.getHeight(), matrix2, true);
                                bitmapList.clear();
                                bookImgList.clear();
                                bitmapList.add(Bitmap.createBitmap(Constant.bookBitmap, 0, 0, Constant.bookBitmap.getWidth(), Constant.bookBitmap.getHeight() / 2));
                                bitmapList.add(Bitmap.createBitmap(Constant.bookBitmap, 0, Constant.bookBitmap.getHeight() / 2, Constant.bookBitmap.getWidth(), Constant.bookBitmap.getHeight() / 2));
                                new insertBookGroup().execute(new Bitmap[]{Constant.bookBitmap});
                            } catch (IOException e2) {
                                Log.w(TAG, "Cannot write to " + file2, e2);
                            }
                        } else if (Constant.current_camera_view.equals("Photo")) {
                            File externalFilesDir3 = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                            File file3 = new File(externalFilesDir3, System.currentTimeMillis() + ".jpg");
                            try {
                                FileOutputStream fileOutputStream3 = new FileOutputStream(file3);
                                fileOutputStream3.write(data);
                                fileOutputStream3.close();
                                Bitmap decodeByteArray3 = BitmapFactory.decodeByteArray(data, 0, data.length);
                                Bitmap scalePreserveRatio3 = scalePreserveRatio(decodeByteArray3, decodeByteArray3.getWidth() / 2, decodeByteArray3.getHeight() / 2);
                                int cameraPhotoOrientation3 = getCameraPhotoOrientation(file3.getPath());
                                Matrix matrix3 = new Matrix();
                                matrix3.postRotate((float) cameraPhotoOrientation3);
                                sourceUri = BitmapUtils.getUri(getApplicationContext(), Bitmap.createBitmap(scalePreserveRatio3, 0, 0, scalePreserveRatio3.getWidth(), scalePreserveRatio3.getHeight(), matrix3, true));
                                destinationUri = Uri.fromFile(getImageFile());
                                progressBar.setVisibility(View.GONE);
                                Constant.IdentifyActivity = "UcropActivity";
                                AdsUtils.showGoogleInterstitialAd(ScannerActivity.this, false);
                            } catch (IOException e3) {
                                Log.w(TAG, "Cannot write to " + file3, e3);
                            }
                        }
                    } else if (!isBackSide) {
                        File externalFilesDir4 = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                        File file4 = new File(externalFilesDir4, System.currentTimeMillis() + ".jpg");
                        try {
                            FileOutputStream fileOutputStream4 = new FileOutputStream(file4);
                            fileOutputStream4.write(data);
                            fileOutputStream4.close();
                            Bitmap decodeByteArray4 = BitmapFactory.decodeByteArray(data, 0, data.length);
                            Bitmap scalePreserveRatio4 = scalePreserveRatio(decodeByteArray4, decodeByteArray4.getWidth() / 2, decodeByteArray4.getHeight() / 2);
                            int cameraPhotoOrientation4 = getCameraPhotoOrientation(file4.getPath());
                            Matrix matrix4 = new Matrix();
                            matrix4.postRotate((float) cameraPhotoOrientation4);
                            Constant.IDCardBitmap = Bitmap.createBitmap(scalePreserveRatio4, 0, 0, scalePreserveRatio4.getWidth(), scalePreserveRatio4.getHeight(), matrix4, true);
                            idcardImgList.add(0, Constant.IDCardBitmap);
                            Log.e(TAG, "onPictureTaken: " + idcardImgList.size());
                            ly_camera.setVisibility(View.GONE);
                            progressBar.setVisibility(View.GONE);
                            ly_crop.setVisibility(View.VISIBLE);
                            isBackSide = true;
                            tv_id_card.setText("Back Side");
                            iv_card_crop.setImageToCrop(Constant.IDCardBitmap);
                        } catch (IOException e4) {
                            Log.w(TAG, "Cannot write to " + file4, e4);
                        }
                    } else {
                        File externalFilesDir5 = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                        File file5 = new File(externalFilesDir5, System.currentTimeMillis() + ".jpg");
                        try {
                            FileOutputStream fileOutputStream5 = new FileOutputStream(file5);
                            fileOutputStream5.write(data);
                            fileOutputStream5.close();
                            Bitmap decodeByteArray5 = BitmapFactory.decodeByteArray(data, 0, data.length);
                            Bitmap scalePreserveRatio5 = scalePreserveRatio(decodeByteArray5, decodeByteArray5.getWidth() / 2, decodeByteArray5.getHeight() / 2);
                            int cameraPhotoOrientation5 = getCameraPhotoOrientation(file5.getPath());
                            Matrix matrix5 = new Matrix();
                            matrix5.postRotate((float) cameraPhotoOrientation5);
                            Constant.IDCardBitmap = Bitmap.createBitmap(scalePreserveRatio5, 0, 0, scalePreserveRatio5.getWidth(), scalePreserveRatio5.getHeight(), matrix5, true);
                            idcardImgList.add(1, Constant.IDCardBitmap);
                            Log.e(TAG, "onPictureTaken: " + idcardImgList.size());
                            ly_camera.setVisibility(View.GONE);
                            progressBar.setVisibility(View.GONE);
                            ly_crop.setVisibility(View.VISIBLE);
                            iv_card_crop.setImageToCrop(Constant.IDCardBitmap);
                        } catch (IOException e5) {
                            Log.w(TAG, "Cannot write to " + file5, e5);
                        }
                    }

                }

                @Override
                public void onVideoTaken(VideoResult result) {
                    // A Video was taken!
                }

                // And much more
            });
        }
        idcardImgList.clear();
        Constant.card_type = "Single";
        if (Constant.current_tag.equals("All Docs")) {
            Constant.current_camera_view = "Document";
            setCameraView();
        } else if (Constant.current_tag.equals("Business Card")) {
            Constant.current_camera_view = "ID Card";
            setCameraView();
        } else if (Constant.current_tag.equals("ID Card")) {
            Constant.current_camera_view = "ID Card";
            setCameraView();
        } else if (Constant.current_tag.equals("Academic Docs")) {
            Constant.current_camera_view = "Book";
            setCameraView();
        } else if (Constant.current_tag.equals("Personal Tag")) {
            Constant.current_camera_view = "Photo";
            setCameraView();
        } else {
            Constant.current_camera_view = "Document";
            setCameraView();
        }
    }

    public void setCameraView() {
        if (Constant.current_camera_view.equals("Document")) {
            rl_book_view.setVisibility(View.GONE);
            rl_idcard_view.setVisibility(View.GONE);
            tv_document.setTextColor(getResources().getColor(R.color.black));
            tv_book.setTextColor(getResources().getColor(R.color.unselected_txt_color));
            tv_idcard.setTextColor(getResources().getColor(R.color.unselected_txt_color));
            tv_photo.setTextColor(getResources().getColor(R.color.unselected_txt_color));
            v_document.setVisibility(View.VISIBLE);
            v_book.setVisibility(View.INVISIBLE);
            v_idcard.setVisibility(View.INVISIBLE);
            v_photo.setVisibility(View.INVISIBLE);
        } else if (Constant.current_camera_view.equals("Book")) {
            rl_book_view.setVisibility(View.VISIBLE);
            rl_idcard_view.setVisibility(View.GONE);
            tv_document.setTextColor(getResources().getColor(R.color.unselected_txt_color));
            tv_book.setTextColor(getResources().getColor(R.color.black));
            tv_idcard.setTextColor(getResources().getColor(R.color.unselected_txt_color));
            tv_photo.setTextColor(getResources().getColor(R.color.unselected_txt_color));
            v_document.setVisibility(View.INVISIBLE);
            v_book.setVisibility(View.VISIBLE);
            v_idcard.setVisibility(View.INVISIBLE);
            v_photo.setVisibility(View.INVISIBLE);
        } else if (Constant.current_camera_view.equals("ID Card")) {
            rl_book_view.setVisibility(View.GONE);
            rl_idcard_view.setVisibility(View.VISIBLE);
            tv_document.setTextColor(getResources().getColor(R.color.unselected_txt_color));
            tv_book.setTextColor(getResources().getColor(R.color.unselected_txt_color));
            tv_idcard.setTextColor(getResources().getColor(R.color.black));
            tv_photo.setTextColor(getResources().getColor(R.color.unselected_txt_color));
            v_document.setVisibility(View.INVISIBLE);
            v_book.setVisibility(View.INVISIBLE);
            v_idcard.setVisibility(View.VISIBLE);
            v_photo.setVisibility(View.INVISIBLE);
            IDCardDialog();
        } else if (Constant.current_camera_view.equals("Photo")) {
            rl_book_view.setVisibility(View.GONE);
            rl_idcard_view.setVisibility(View.GONE);
            tv_document.setTextColor(getResources().getColor(R.color.unselected_txt_color));
            tv_book.setTextColor(getResources().getColor(R.color.unselected_txt_color));
            tv_idcard.setTextColor(getResources().getColor(R.color.unselected_txt_color));
            tv_photo.setTextColor(getResources().getColor(R.color.black));
            v_document.setVisibility(View.INVISIBLE);
            v_book.setVisibility(View.INVISIBLE);
            v_idcard.setVisibility(View.INVISIBLE);
            v_photo.setVisibility(View.VISIBLE);
        }
    }



 /*   @Override
    public void onAspectRatioSelected(AspectRatio aspectRatio) {
        if (cameraView != null) {
            Toast.makeText(this, aspectRatio.toString(), Toast.LENGTH_SHORT).show();
            cameraView.setAspectRatio(aspectRatio);
        }
    }*/

    private void IDCardDialog() {
        final Dialog dialog = new Dialog(this, R.style.ThemeWithRoundShape);
        dialog.requestWindowFeature(1);
        dialog.setContentView(R.layout.id_card_selection_dialog);
        dialog.getWindow().setLayout(-1, -2);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        ((TextView) dialog.findViewById(R.id.tv_select1)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Constant.card_type = "Single";
                tv_id_card.setVisibility(View.GONE);
                dialog.dismiss();
            }
        });
        ((TextView) dialog.findViewById(R.id.tv_select2)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Constant.card_type = "Double";
                tv_id_card.setVisibility(View.VISIBLE);
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

    @Override
    public void onPause() {
        new Handler().postDelayed(new Runnable() {
            public void run() {
                cameraView.close();
            }
        }, 200);
        super.onPause();
    }

    private void handleCropResult(Intent intent) {
        FileInputStream fileInputStream;
        Uri output = UCrop.getOutput(intent);
        if (output != null) {
            try {
                fileInputStream = new FileInputStream(new File(FileUtils.getPath(this, output)));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                fileInputStream = null;
            }
            Constant.original = BitmapFactory.decodeStream(fileInputStream);
            new insertGroup().execute(new Bitmap[]{Constant.original});
            return;
        }
        Toast.makeText(this, "error", Toast.LENGTH_SHORT).show();
    }

    private void handleCropError(Intent intent) {
        Throwable error = UCrop.getError(intent);
        if (error != null) {
            Log.e(TAG, "handleCropError: ", error);
            return;
        }
        Log.e(TAG, "handleCropError: " + error);
    }

    public File getImageFile() {
        String str = "IMG_" + System.currentTimeMillis() + "_";
        return new File(new ContextWrapper(getApplicationContext()).getDir("imageDir", 0), str + ".jpg");
    }

    private void onClickTab(final String str) {
        if (idcardImgList.size() > 0) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle((CharSequence) "Confirmation?");
            builder.setMessage((CharSequence) "Are you sure want to discard this image?");
            builder.setPositiveButton((CharSequence) "Yes", (DialogInterface.OnClickListener) new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    idcardImgList.clear();
                    iv_switch_flash.setVisibility(View.VISIBLE);
                    iv_done.setVisibility(View.GONE);
                    isBackSide = false;
                    tv_id_card.setText("Front Side");
                    Constant.current_camera_view = str;
                    setCameraView();
                }
            });
            builder.setNegativeButton((CharSequence) "No", (DialogInterface.OnClickListener) new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.cancel();
                }
            });
            builder.show();
            return;
        }
        Constant.current_camera_view = str;
        setCameraView();
    }

    @Override
    public void onClick(View view) {
        int i = 1;
        switch (view.getId()) {
            case R.id.iv_back_camera:
                onBackPressed();
                return;
            case R.id.iv_back_crop:
                ly_crop.setVisibility(View.GONE);
                ly_filter.setVisibility(View.GONE);
                ly_camera.setVisibility(View.VISIBLE);
                if (idcardImgList.size() == 2) {
                    iv_switch_flash.setVisibility(View.GONE);
                    iv_done.setVisibility(View.VISIBLE);
                    return;
                }
                return;
            case R.id.iv_back_filter:
                ly_camera.setVisibility(View.GONE);
                ly_crop.setVisibility(View.VISIBLE);
                ly_filter.setVisibility(View.GONE);
                return;
            case R.id.iv_color:
                showProgressDialog();
                AsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            tempBitmap = ScanActivity.getMagicColorBitmap(originalBitmap);
                        } catch (OutOfMemoryError e) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    tempBitmap = originalBitmap;
                                    iv_card_filter.setImageBitmap(originalBitmap);
                                    e.printStackTrace();
                                    dismissProgressDialog();
                                }
                            });
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                iv_card_filter.setImageBitmap(tempBitmap);
                                dismissProgressDialog();
                            }
                        });
                    }
                });
                iv_original.setBackgroundResource(R.drawable.filter_bg);
                iv_original.setTextColor(getResources().getColor(R.color.black));

                iv_color.setBackgroundResource(R.drawable.filter_selection_bg);
                iv_color.setTextColor(getResources().getColor(R.color.white));

                iv_sharp_black.setBackgroundResource(R.drawable.filter_bg);
                iv_sharp_black.setTextColor(getResources().getColor(R.color.black));

                iv_ocv_black.setBackgroundResource(R.drawable.filter_bg);
                iv_ocv_black.setTextColor(getResources().getColor(R.color.black));
                return;
            case R.id.iv_continue:
//                Log.e("idcardImgList.size() ", String.valueOf(idcardImgList.size()));
                if (idcardImgList.size() == 2) {
                    if (iv_card_crop.canRightCrop()) {
                        Constant.IDCardBitmap = iv_card_crop.crop();
                        idcardImgList.remove(1);
                        idcardImgList.add(1, Constant.IDCardBitmap);
                        Log.e(TAG, "onPictureTaken: " + idcardImgList.size());
                        Constant.IdentifyActivity = "IDCardPreviewActivity";
                        AdsUtils.showGoogleInterstitialAd(ScannerActivity.this, false);
                        return;
                    }
                    return;
                } else if (iv_card_crop.canRightCrop()) {
                    Constant.IDCardBitmap = iv_card_crop.crop();
                    idcardImgList.remove(0);
                    idcardImgList.add(0, Constant.IDCardBitmap);
                    Log.e(TAG, "onPictureTaken: " + idcardImgList.size());
                    ly_camera.setVisibility(View.VISIBLE);
                    ly_crop.setVisibility(View.GONE);
                    ly_filter.setVisibility(View.GONE);
                    return;
                } else {
                    return;
                }
            case R.id.iv_done:
                Constant.IdentifyActivity = "IDCardPreviewActivity";
                AdsUtils.showGoogleInterstitialAd(ScannerActivity.this, false);
                return;
            case R.id.iv_done_filter:
                Constant.IDCardBitmap = tempBitmap;
                if (Constant.IDCardBitmap == null) {
                    Constant.IDCardBitmap = originalBitmap;
                }
                if (idcardImgList.size() == 2) {
                    idcardImgList.remove(1);
                    idcardImgList.add(1, Constant.IDCardBitmap);
                    Log.e(TAG, "onPictureTaken: " + idcardImgList.size());
                    Constant.IdentifyActivity = "IDCardPreviewActivity";
                    AdsUtils.showGoogleInterstitialAd(ScannerActivity.this, false);
                    return;
                }
                idcardImgList.remove(0);
                idcardImgList.add(0, Constant.IDCardBitmap);
                Log.e(TAG, "onPictureTaken: " + idcardImgList.size());
                ly_camera.setVisibility(View.VISIBLE);
                ly_crop.setVisibility(View.GONE);
                ly_filter.setVisibility(View.GONE);
                return;
            case R.id.iv_full_crop:
                iv_card_crop.setFullImgCrop();
                return;
            case R.id.iv_gallery:
                if (idcardImgList.size() <= 0) {
                    Constant.current_camera_view = "Document";
                    Constant.IdentifyActivity = "ScannerGalleryActivity";
                    AdsUtils.showGoogleInterstitialAd(ScannerActivity.this, false);
                    return;
                }
                return;
            case R.id.iv_ocv_black:
                showProgressDialog();
                AsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            tempBitmap = ScanActivity.getBWBitmap(originalBitmap);
                        } catch (OutOfMemoryError e) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    tempBitmap = originalBitmap;
                                    iv_card_filter.setImageBitmap(originalBitmap);
                                    e.printStackTrace();
                                    dismissProgressDialog();
                                }
                            });
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                iv_card_filter.setImageBitmap(tempBitmap);
                                dismissProgressDialog();
                            }
                        });
                    }
                });
                iv_original.setBackgroundResource(R.drawable.filter_bg);
                iv_original.setTextColor(getResources().getColor(R.color.black));

                iv_color.setBackgroundResource(R.drawable.filter_bg);
                iv_color.setTextColor(getResources().getColor(R.color.black));

                iv_sharp_black.setBackgroundResource(R.drawable.filter_bg);
                iv_sharp_black.setTextColor(getResources().getColor(R.color.black));

                iv_ocv_black.setBackgroundResource(R.drawable.filter_selection_bg);
                iv_ocv_black.setTextColor(getResources().getColor(R.color.white));

                return;
            case R.id.iv_original:
                try {
                    showProgressDialog();
                    tempBitmap = originalBitmap;
                    iv_card_filter.setImageBitmap(originalBitmap);
                    dismissProgressDialog();
                } catch (OutOfMemoryError e) {
                    e.printStackTrace();
                    dismissProgressDialog();
                }
                iv_original.setBackgroundResource(R.drawable.filter_selection_bg);
                iv_original.setTextColor(getResources().getColor(R.color.white));

                iv_color.setBackgroundResource(R.drawable.filter_bg);
                iv_color.setTextColor(getResources().getColor(R.color.black));

                iv_sharp_black.setBackgroundResource(R.drawable.filter_bg);
                iv_sharp_black.setTextColor(getResources().getColor(R.color.black));

                iv_ocv_black.setBackgroundResource(R.drawable.filter_bg);
                iv_ocv_black.setTextColor(getResources().getColor(R.color.black));
                return;
            case R.id.iv_retake:
                if (idcardImgList.size() == 2) {
                    idcardImgList.remove(1);
                    Log.e(TAG, "onPictureTaken: " + idcardImgList.size());
                    isBackSide = true;
                    tv_id_card.setText("Back Side");
                } else {
                    idcardImgList.clear();
                    Log.e(TAG, "onPictureTaken: " + idcardImgList.size());
                    isBackSide = false;
                    tv_id_card.setText("Front Side");
                }
                ly_camera.setVisibility(View.VISIBLE);
                ly_crop.setVisibility(View.GONE);
                ly_filter.setVisibility(View.GONE);
                return;
            case R.id.iv_sharp_black:
                showProgressDialog();
                AsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            tempBitmap = ScanActivity.getGrayBitmap(originalBitmap);
                        } catch (OutOfMemoryError e) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    tempBitmap = originalBitmap;
                                    iv_card_filter.setImageBitmap(originalBitmap);
                                    e.printStackTrace();
                                    dismissProgressDialog();
                                }
                            });
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                iv_card_filter.setImageBitmap(tempBitmap);
                                dismissProgressDialog();
                            }
                        });
                    }
                });
                iv_original.setBackgroundResource(R.drawable.filter_bg);
                iv_original.setTextColor(getResources().getColor(R.color.black));

                iv_color.setBackgroundResource(R.drawable.filter_bg);
                iv_color.setTextColor(getResources().getColor(R.color.black));

                iv_sharp_black.setBackgroundResource(R.drawable.filter_selection_bg);
                iv_sharp_black.setTextColor(getResources().getColor(R.color.white));

                iv_ocv_black.setBackgroundResource(R.drawable.filter_bg);
                iv_ocv_black.setTextColor(getResources().getColor(R.color.black));
                return;
            case R.id.iv_switch_camera:

                if (cameraView != null) {
                    Facing facing = cameraView.getFacing();

                    if (facing == Facing.BACK) {
                        cameraView.setFacing(Facing.FRONT);
                    } else {
                        cameraView.setFacing(Facing.BACK);
                    }
//                    cameraView.setFacing(i);
                    return;
                }
                return;
            case R.id.iv_switch_flash:
                if (cameraView != null) {
                    current_flash = (current_flash + 1) % FLASH_SETTING.length;
                    iv_switch_flash.setImageResource(FLASH_ICONS[current_flash]);
                    int f = FLASH_SETTING[current_flash];
                    if (f == 0) {
                        cameraView.setFlash(Flash.OFF);
                    } else if (f == 1) {
                        cameraView.setFlash(Flash.ON);
                    } else {
                        cameraView.setFlash(Flash.AUTO);
                    }

                    return;
                }
                return;
            case R.id.iv_take_picture:
                if (tv_id_card.getText().equals("Back Side") && idcardImgList.size() == 2) {
                    Toast.makeText(this, "You selected two side ID card, Please complete the process.", Toast.LENGTH_SHORT).show();
                    return;
                } else if (cameraView != null) {
                    progressBar.setVisibility(View.VISIBLE);
                    cameraView.takePicture();
                    return;
                } else {
                    return;
                }
            case R.id.ly_current_filter:
                if (iv_card_crop.canRightCrop()) {
                    Constant.IDCardBitmap = iv_card_crop.crop();
                    ly_camera.setVisibility(View.GONE);
                    ly_crop.setVisibility(View.GONE);
                    ly_filter.setVisibility(View.VISIBLE);
                    originalBitmap = Constant.IDCardBitmap;
                    iv_card_filter.setImageBitmap(originalBitmap);
                    iv_original.setBackgroundResource(R.drawable.filter_selection_bg);
                    iv_original.setTextColor(getResources().getColor(R.color.white));

                    iv_color.setBackgroundResource(R.drawable.filter_bg);
                    iv_color.setTextColor(getResources().getColor(R.color.black));

                    iv_sharp_black.setBackgroundResource(R.drawable.filter_bg);
                    iv_sharp_black.setTextColor(getResources().getColor(R.color.black));

                    iv_ocv_black.setBackgroundResource(R.drawable.filter_bg);
                    iv_ocv_black.setTextColor(getResources().getColor(R.color.black));
                    return;
                }
                return;
            case R.id.ly_rotate_doc:
                Bitmap bitmap = Constant.IDCardBitmap;
                Matrix matrix = new Matrix();
                matrix.postRotate(90.0f);
                Bitmap createBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                Constant.IDCardBitmap.recycle();
                System.gc();
                Constant.IDCardBitmap = createBitmap;
                iv_card_crop.setImageToCrop(Constant.IDCardBitmap);
                iv_card_crop.setFullImgCrop();
                return;
            case R.id.tv_book:
                onClickTab("Book");
                return;
            case R.id.tv_document:
                onClickTab("Document");
                return;
            case R.id.tv_idcard:
                onClickTab("ID Card");
                return;
            case R.id.tv_photo:
                onClickTab("Photo");
                return;
            default:
                return;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (ImagePicker.shouldHandleResult(requestCode, resultCode, intent, 100)) {
            Iterator<Image> it = ImagePicker.getImages(intent).iterator();
            while (it.hasNext()) {
                Image next = it.next();
                if (Build.VERSION.SDK_INT >= 29) {
                    Glide.with(getApplicationContext()).asBitmap().load(next.getUri()).into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(Bitmap bitmap, Transition<? super Bitmap> transition) {
                            if (Constant.original != null) {
                                Constant.original.recycle();
                                System.gc();
                            }
                            Constant.original = bitmap;
                            Constant.IdentifyActivity = "CropDocumentActivity2";
                            AdsUtils.showGoogleInterstitialAd(ScannerActivity.this, false);
                        }
                    });
                } else {
                    Glide.with(getApplicationContext()).asBitmap().load(next.getPath()).into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(Bitmap bitmap, Transition<? super Bitmap> transition) {
                            if (Constant.original != null) {
                                Constant.original.recycle();
                                System.gc();
                            }
                            Constant.original = bitmap;
                            Constant.IdentifyActivity = "CropDocumentActivity2";
                            AdsUtils.showGoogleInterstitialAd(ScannerActivity.this, false);
                        }
                    });
                }
            }
        }
        if (requestCode == UCrop.REQUEST_CROP && resultCode == RESULT_OK) {
            if (sourceUri != null) {
                getContentResolver().delete(sourceUri, (String) null, (String[]) null);
            }
            handleCropResult(intent);
        }
        if (resultCode == UCrop.RESULT_ERROR) {
            if (sourceUri != null) {
                getContentResolver().delete(sourceUri, (String) null, (String[]) null);
            }
            handleCropError(intent);
        }
        /*if (resultCode == 394) {
            if (sourceUri != null) {
                getContentResolver().delete(sourceUri, (String) null, (String[]) null);
            }
            handleCropResult(intent);
        }*/

        super.onActivityResult(requestCode, resultCode, intent);
    }

    private void showProgressDialog() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Applying Filter...");
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
    }

    public void dismissProgressDialog() {
        if (progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    @Override
    public void onBackPressed() {
        if (ly_crop.getVisibility() == View.VISIBLE) {
            ly_crop.setVisibility(View.GONE);
            ly_filter.setVisibility(View.GONE);
            ly_camera.setVisibility(View.VISIBLE);
            if (idcardImgList.size() == 2) {
                iv_switch_flash.setVisibility(View.GONE);
                iv_done.setVisibility(View.VISIBLE);
            }
        } else if (ly_filter.getVisibility() == View.VISIBLE) {
            ly_camera.setVisibility(View.GONE);
            ly_crop.setVisibility(View.VISIBLE);
            ly_filter.setVisibility(View.GONE);
        } else if (idcardImgList.size() > 0) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle((CharSequence) "Confirmation?");
            builder.setMessage((CharSequence) "Are you sure want to exit the camera?");
            builder.setPositiveButton((CharSequence) "Yes", (DialogInterface.OnClickListener) new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.cancel();
                    finish();
                }
            });
            builder.setNegativeButton((CharSequence) "No", (DialogInterface.OnClickListener) new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.cancel();
                }
            });
            builder.show();
        } else {
            finish();
        }
    }

    public static class ConfirmationDialogFragment extends DialogFragment {
        private static final String ARG_MESSAGE = "message";
        private static final String ARG_NOT_GRANTED_MESSAGE = "not_granted_message";
        private static final String ARG_PERMISSIONS = "permissions";
        private static final String ARG_REQUEST_CODE = "request_code";

        public static ConfirmationDialogFragment newInstance(int i, String[] strArr, int i2, int i3) {
            ConfirmationDialogFragment confirmationDialogFragment = new ConfirmationDialogFragment();
            Bundle bundle = new Bundle();
            bundle.putInt(ARG_MESSAGE, i);
            bundle.putStringArray(ARG_PERMISSIONS, strArr);
            bundle.putInt(ARG_REQUEST_CODE, i2);
            bundle.putInt(ARG_NOT_GRANTED_MESSAGE, i3);
            confirmationDialogFragment.setArguments(bundle);
            return confirmationDialogFragment;
        }

        @Override
        public Dialog onCreateDialog(Bundle bundle) {
            final Bundle arguments = getArguments();
            return new AlertDialog.Builder(getActivity()).setMessage(arguments.getInt(ARG_MESSAGE)).setPositiveButton(android.R.string.ok, (DialogInterface.OnClickListener) new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    String[] stringArray = arguments.getStringArray(ConfirmationDialogFragment.ARG_PERMISSIONS);
                    if (stringArray != null) {
                        ActivityCompat.requestPermissions(ConfirmationDialogFragment.this.getActivity(), stringArray, arguments.getInt(ConfirmationDialogFragment.ARG_REQUEST_CODE));
                        return;
                    }
                    throw new IllegalArgumentException();
                }
            }).setNegativeButton(android.R.string.cancel, (DialogInterface.OnClickListener) new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Toast.makeText(ConfirmationDialogFragment.this.getActivity(), arguments.getInt(ConfirmationDialogFragment.ARG_NOT_GRANTED_MESSAGE), Toast.LENGTH_SHORT).show();
                }
            }).create();
        }
    }

    private class insertGroup extends AsyncTask<Bitmap, Void, Bitmap> {
        String current_doc_name;
        String group_date;
        String group_name;
        ProgressDialog progressDialog;

        private insertGroup() {
        }

        @Override
        public void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(ScannerActivity.this);
            progressDialog.setIndeterminate(true);
            progressDialog.setCancelable(false);
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.setMessage("Processing...");
            progressDialog.show();
        }

        @Override
        public Bitmap doInBackground(Bitmap... bitmapArr) {
            Bitmap bitmap = Constant.original;
            if (bitmap == null) {
                return null;
            }
            byte[] bytes = BitmapUtils.getBytes(bitmap);
            File externalFilesDir = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
            File file = new File(externalFilesDir, System.currentTimeMillis() + ".jpg");
            try {
                FileOutputStream fileOutputStream = new FileOutputStream(file);
                fileOutputStream.write(bytes);
                fileOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
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
            selected_group_name = group_name;
            current_docs_name = current_doc_name;
            Intent intent2 = new Intent(ScannerActivity.this, GroupDocumentActivity.class);
            intent2.putExtra("current_group", selected_group_name);
            startActivity(intent2);
            Constant.IdentifyActivity = "";
            finish();
//            Constant.IdentifyActivity = "DocumentEditorActivity_Scanner";
//            AdsUtils.showGoogleInterstitialAd(ScannerActivity.this, false);
        }
    }

    private class insertBookGroup extends AsyncTask<Bitmap, Void, Bitmap> {
        String current_doc_name;
        String group_date;
        String group_name;

        private insertBookGroup() {
        }

        @Override
        public void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        public Bitmap doInBackground(Bitmap... bitmapArr) {
            for (int i = 0; i < bitmapList.size(); i++) {
                if (bitmapList.get(i) != null) {
                    byte[] bytes = BitmapUtils.getBytes(bitmapList.get(i));
                    File externalFilesDir = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
                    File file = new File(externalFilesDir, System.currentTimeMillis() + ".jpg");
                    try {
                        FileOutputStream fileOutputStream = new FileOutputStream(file);
                        fileOutputStream.write(bytes);
                        fileOutputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (!Constant.inputType.equals("Group")) {
                        selected_group_name = GroupDocumentActivity.current_group;
                        current_doc_name = "Doc_" + System.currentTimeMillis();
                        dbHelper.addGroupDoc(selected_group_name, file.getPath(), current_doc_name, "Insert text here...");
                        bookImgList.add(new BookModel(bitmapList.get(i), current_doc_name, i));
                    } else if (i == 0) {
                        group_name = "Scanny" + Constant.getDateTime("_ddMMHHmmss");
                        selected_group_name = group_name;
                        group_date = Constant.getDateTime("yyyy-MM-dd  hh:mm a");
                        current_doc_name = "Doc_" + System.currentTimeMillis();
                        dbHelper.createDocTable(group_name);
                        dbHelper.addGroup(new DBModel(group_name, group_date, file.getPath(), Constant.current_tag));
                        dbHelper.addGroupDoc(group_name, file.getPath(), current_doc_name, "Insert text here...");
                        bookImgList.add(new BookModel(bitmapList.get(i), current_doc_name, i));
                    } else {
                        current_doc_name = "Doc_" + System.currentTimeMillis();
                        dbHelper.addGroupDoc(selected_group_name, file.getPath(), current_doc_name, "Insert text here...");
                        bookImgList.add(new BookModel(bitmapList.get(i), current_doc_name, i));
                    }
                }
            }
            return null;
        }

        @Override
        public void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            progressBar.setVisibility(View.GONE);
            finish();
            Constant.IdentifyActivity = "SavedEditDocumentActivity3";
            AdsUtils.showGoogleInterstitialAd(ScannerActivity.this, false);
        }
    }
}
