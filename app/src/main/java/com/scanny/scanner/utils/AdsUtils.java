package com.scanny.scanner.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.scanny.scanner.R;
import com.scanny.scanner.main_utils.Constant;

public class AdsUtils {

    private static InterstitialAd mInterstitialAd;

    public static void showGoogleBannerAd(Context context, AdView adView) {
        adView.setVisibility(View.VISIBLE);
        MobileAds.initialize(context, (OnInitializationCompleteListener) new OnInitializationCompleteListener() {
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });
        adView.loadAd(new AdRequest.Builder().build());
    }

    public static void loadGoogleInterstitialAd(Context context, Activity activity) {
        MobileAds.initialize(context, (OnInitializationCompleteListener) new OnInitializationCompleteListener() {
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        InterstitialAd.load(
                context,
                context.getResources().getString(R.string.admob_interstitial_id),
                new AdRequest.Builder().build(),
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        mInterstitialAd = interstitialAd;

                        interstitialAd.setFullScreenContentCallback(
                                new FullScreenContentCallback() {
                                    @Override
                                    public void onAdDismissedFullScreenContent() {
                                        // Called when fullscreen content is dismissed.
                                        // Make sure to set your reference to null so you don't
                                        // show it a second time.
                                        mInterstitialAd = null;
                                        jumpNextActivity(activity);
                                        Log.d("TAG", "The ad was dismissed.");
                                    }

                                    @Override
                                    public void onAdFailedToShowFullScreenContent(AdError adError) {
                                        // Called when fullscreen content failed to show.
                                        // Make sure to set your reference to null so you don't
                                        // show it a second time.
                                        mInterstitialAd = null;
                                        Log.d("TAG", "The ad failed to show.");
                                    }

                                    @Override
                                    public void onAdShowedFullScreenContent() {
                                        // Called when fullscreen content is shown.
                                        Log.d("TAG", "The ad was shown.");
                                    }
                                });
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        super.onAdFailedToLoad(loadAdError);
                        mInterstitialAd = null;
                    }
                });
    }

    public static void showGoogleInterstitialAd(Activity activity, boolean isShowAd) {
        if (isShowAd) {
            if (mInterstitialAd != null) {
                mInterstitialAd.show(activity);
            } else {
                jumpNextActivity(activity);
            }
            loadGoogleInterstitialAd(activity, activity);
        } else {
            jumpNextActivity(activity);
        }

    }

    public static void jumpNextActivity(Activity activity) {
        if (Constant.IdentifyActivity.equals("MainActivity")) {
            activity.sendBroadcast(new Intent(activity.getPackageName() + ".MainActivity"));
        } else if (Constant.IdentifyActivity.equals("PrivacyPolicyActivity")) {
            activity.sendBroadcast(new Intent(activity.getPackageName() + ".PrivacyPolicyActivity"));
        } else if (Constant.IdentifyActivity.equals("QRGenerateActivity")) {
            activity.sendBroadcast(new Intent(activity.getPackageName() + ".QRGenerateActivity"));
        } else if (Constant.IdentifyActivity.equals("QRReaderActivity")) {
            activity.sendBroadcast(new Intent(activity.getPackageName() + ".QRReaderActivity"));
        } else if (Constant.IdentifyActivity.equals("MainGalleryActivity")) {
            activity.sendBroadcast(new Intent(activity.getPackageName() + ".MainGalleryActivity"));
        } else if (Constant.IdentifyActivity.equals("ScannerActivity")) {
            activity.sendBroadcast(new Intent(activity.getPackageName() + ".ScannerActivity"));
        } else if (Constant.IdentifyActivity.equals("GroupDocumentActivity")) {
            activity.sendBroadcast(new Intent(activity.getPackageName() + ".GroupDocumentActivity"));
        } else if (Constant.IdentifyActivity.equals("CropDocumentActivity")) {
            activity.sendBroadcast(new Intent(activity.getPackageName() + ".CropDocumentActivity"));
        } else if (Constant.IdentifyActivity.equals("ScannerGalleryActivity")) {
            activity.sendBroadcast(new Intent(activity.getPackageName() + ".ScannerGalleryActivity"));
        } else if (Constant.IdentifyActivity.equals("CropDocumentActivity2")) {
            activity.sendBroadcast(new Intent(activity.getPackageName() + ".CropDocumentActivity2"));
        } else if (Constant.IdentifyActivity.equals("DocumentEditorActivity_Crop")) {
            activity.sendBroadcast(new Intent(activity.getPackageName() + ".DocumentEditorActivity_Crop"));
        } else if (Constant.IdentifyActivity.equals("CurrentFilterActivity")) {
            activity.sendBroadcast(new Intent(activity.getPackageName() + ".CurrentFilterActivity"));
        } else if (Constant.IdentifyActivity.equals("ScannerActivity_Retake")) {
            activity.sendBroadcast(new Intent(activity.getPackageName() + ".ScannerActivity_Retake"));
        } else if (Constant.IdentifyActivity.equals("SavedDocumentActivity")) {
            activity.sendBroadcast(new Intent(activity.getPackageName() + ".SavedDocumentActivity"));
        } else if (Constant.IdentifyActivity.equals("ScannerActivity_Retake2")) {
            activity.sendBroadcast(new Intent(activity.getPackageName() + ".ScannerActivity_Retake2"));
        } else if (Constant.IdentifyActivity.equals("DocumentEditorActivity_Saved")) {
            activity.sendBroadcast(new Intent(activity.getPackageName() + ".DocumentEditorActivity_Saved"));
        } else if (Constant.IdentifyActivity.equals("SavedEditDocumentActivity")) {
            activity.sendBroadcast(new Intent(activity.getPackageName() + ".SavedEditDocumentActivity"));
        } else if (Constant.IdentifyActivity.equals("DocumentEditorActivity_SavedEdit")) {
            activity.sendBroadcast(new Intent(activity.getPackageName() + ".DocumentEditorActivity_SavedEdit"));
        } else if (Constant.IdentifyActivity.equals("DocumentEditorActivity_SavedEdit2")) {
            activity.sendBroadcast(new Intent(activity.getPackageName() + ".DocumentEditorActivity_SavedEdit2"));
        } else if (Constant.IdentifyActivity.equals("PDFViewerActivity")) {
            activity.sendBroadcast(new Intent(activity.getPackageName() + ".PDFViewerActivity"));
        } else if (Constant.IdentifyActivity.equals("NoteActivity")) {
            activity.sendBroadcast(new Intent(activity.getPackageName() + ".NoteActivity"));
        } else if (Constant.IdentifyActivity.equals("ImageToTextActivity")) {
            activity.sendBroadcast(new Intent(activity.getPackageName() + ".ImageToTextActivity"));
        } else if (Constant.IdentifyActivity.equals("PDFViewerActivity2")) {
            activity.sendBroadcast(new Intent(activity.getPackageName() + ".PDFViewerActivity2"));
        } else if (Constant.IdentifyActivity.equals("DocumentGalleryActivity")) {
            activity.sendBroadcast(new Intent(activity.getPackageName() + ".DocumentGalleryActivity"));
        } else if (Constant.IdentifyActivity.equals("CropDocumentActivity4")) {
            activity.sendBroadcast(new Intent(activity.getPackageName() + ".CropDocumentActivity4"));
        } else if (Constant.IdentifyActivity.equals("ScannerActivity2")) {
            activity.sendBroadcast(new Intent(activity.getPackageName() + ".ScannerActivity2"));
        } else if (Constant.IdentifyActivity.equals("SavedDocumentPreviewActivity")) {
            activity.sendBroadcast(new Intent(activity.getPackageName() + ".SavedDocumentPreviewActivity"));
        } else if (Constant.IdentifyActivity.equals("IDCardPreviewActivity")) {
            activity.sendBroadcast(new Intent(activity.getPackageName() + ".IDCardPreviewActivity"));
        } else if (Constant.IdentifyActivity.equals("SavedEditDocumentActivity3")) {
            activity.sendBroadcast(new Intent(activity.getPackageName() + ".SavedEditDocumentActivity3"));
        } else if (Constant.IdentifyActivity.equals("UcropActivity")) {
            activity.sendBroadcast(new Intent(activity.getPackageName() + ".UcropActivity"));
        } else if (Constant.IdentifyActivity.equals("DocumentEditorActivity_Scanner")) {
            activity.sendBroadcast(new Intent(activity.getPackageName() + ".DocumentEditorActivity_Scanner"));
        } else if (Constant.IdentifyActivity.equals("IDCardPreviewActivity2")) {
            activity.sendBroadcast(new Intent(activity.getPackageName() + ".IDCardPreviewActivity2"));
        } else if (Constant.IdentifyActivity.equals("DocumentEditorActivity_IDCard")) {
            activity.sendBroadcast(new Intent(activity.getPackageName() + ".DocumentEditorActivity_IDCard"));
        } else if (Constant.IdentifyActivity.equals("IDCardGalleryActivity")) {
            activity.sendBroadcast(new Intent(activity.getPackageName() + ".IDCardGalleryActivity"));
        } else if (Constant.IdentifyActivity.equals("DocumentEditorActivity_SavedPreview")) {
            activity.sendBroadcast(new Intent(activity.getPackageName() + ".DocumentEditorActivity_SavedPreview"));
        } else if (Constant.IdentifyActivity.equals("PDFViewerActivity_Preview")) {
            activity.sendBroadcast(new Intent(activity.getPackageName() + ".PDFViewerActivity_Preview"));
        } else if (Constant.IdentifyActivity.equals("NoteActivity_Preview")) {
            activity.sendBroadcast(new Intent(activity.getPackageName() + ".NoteActivity_Preview"));
        } else if (Constant.IdentifyActivity.equals("ImageToTextActivity_Preview")) {
            activity.sendBroadcast(new Intent(activity.getPackageName() + ".ImageToTextActivity_Preview"));
        }
    }
}
