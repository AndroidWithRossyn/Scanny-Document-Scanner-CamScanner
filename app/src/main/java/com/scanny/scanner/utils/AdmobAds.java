package com.scanny.scanner.utils;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.nativead.MediaView;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.scanny.scanner.BuildConfig;
import com.scanny.scanner.R;

public class AdmobAds {
    public static String PKG_APP = "com.templatemela.Scanny";
    public static boolean SHOW_ADS = true;

    public static void loadNativeAds(Activity activity, final View view, final ViewGroup viewGroup, final NativeAdView nativeAdView) {
        nativeAdView.setMediaView((MediaView) nativeAdView.findViewById(R.id.media_view));
        nativeAdView.setHeadlineView(nativeAdView.findViewById(R.id.primary));
        nativeAdView.setBodyView(nativeAdView.findViewById(R.id.secondary));
        nativeAdView.setCallToActionView(nativeAdView.findViewById(R.id.cta));
        nativeAdView.setIconView(nativeAdView.findViewById(R.id.icon));
        nativeAdView.setAdvertiserView(nativeAdView.findViewById(R.id.tertiary));

        AdLoader.Builder builder = new AdLoader.Builder((Context) activity, activity.getString(R.string.admob_native_ad))
                .forNativeAd(new NativeAd.OnNativeAdLoadedListener() {
                    @Override
                    public void onNativeAdLoaded(NativeAd nativeAd) {
                        AdmobAds.populateNativeAdView(nativeAd, nativeAdView);
                        viewGroup.setVisibility(View.VISIBLE);
                        ((View) viewGroup.getParent().getParent()).setVisibility(View.VISIBLE);
                        if (view != null) {
                            view.setVisibility(View.GONE);
                        }
                    }
                }).withAdListener(new AdListener() {
                    @Override
                    public void onAdLoaded() {
                        super.onAdLoaded();
                    }

                    @Override
                    public void onAdFailedToLoad(LoadAdError adError) {
                        // Handle the failure by logging, altering the UI, and so on.
                        Log.e("adError ", adError.getMessage());
                    }
                });
        if (BuildConfig.APPLICATION_ID.equals(PKG_APP)) {
            builder.build().loadAd(new AdRequest.Builder().build());
        }
    }

    public static void populateNativeAdView(NativeAd nativeAd, NativeAdView nativeAdView) {
        ((TextView) nativeAdView.getHeadlineView()).setText(nativeAd.getHeadline());
        ((TextView) nativeAdView.getBodyView()).setText(nativeAd.getBody());
        ((TextView) nativeAdView.getCallToActionView()).setText(nativeAd.getCallToAction());
        NativeAd.Image icon = nativeAd.getIcon();
        if (icon == null) {
            nativeAdView.getIconView().setVisibility(View.INVISIBLE);
        } else {
            ((ImageView) nativeAdView.getIconView()).setImageDrawable(icon.getDrawable());
            nativeAdView.getIconView().setVisibility(View.VISIBLE);
        }
        if (nativeAd.getAdvertiser() == null) {
            nativeAdView.getAdvertiserView().setVisibility(View.INVISIBLE);
        } else {
            ((TextView) nativeAdView.getAdvertiserView()).setText(nativeAd.getAdvertiser());
            nativeAdView.getAdvertiserView().setVisibility(View.VISIBLE);
        }
        nativeAdView.setNativeAd(nativeAd);
    }

    public static void hideNativeAds(Activity activity) {
        ((View) ((ViewGroup) activity.findViewById(R.id.admob_native_container)).getParent()).setVisibility(View.GONE);
    }
}
