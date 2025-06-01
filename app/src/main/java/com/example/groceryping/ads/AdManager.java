package com.example.groceryping.ads;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;

public class AdManager {
    private static final String TAG = "AdManager";
    private static AdManager instance;
    private InterstitialAd mInterstitialAd;
    private RewardedAd mRewardedAd;
    private Context context;

    // Test ad unit IDs - Replace with your actual ad unit IDs in production
    private static final String BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111";
    private static final String INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712";
    private static final String REWARDED_AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917";

    private AdManager(Context context) {
        this.context = context.getApplicationContext();
        MobileAds.initialize(context, initializationStatus -> {
            Log.d(TAG, "AdMob SDK initialized");
        });
    }

    public static synchronized AdManager getInstance(Context context) {
        if (instance == null) {
            instance = new AdManager(context);
        }
        return instance;
    }

    public void loadBannerAd(ViewGroup adContainer) {
        AdView adView = new AdView(context);
        adView.setAdUnitId(BANNER_AD_UNIT_ID);
        adView.setAdSize(com.google.android.gms.ads.AdSize.BANNER);
        adContainer.addView(adView);

        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
    }

    public void loadInterstitialAd() {
        AdRequest adRequest = new AdRequest.Builder().build();
        InterstitialAd.load(context, INTERSTITIAL_AD_UNIT_ID, adRequest,
            new InterstitialAdLoadCallback() {
                @Override
                public void onAdLoaded(InterstitialAd interstitialAd) {
                    mInterstitialAd = interstitialAd;
                    setupInterstitialCallbacks();
                }

                @Override
                public void onAdFailedToLoad(LoadAdError loadAdError) {
                    Log.d(TAG, "Interstitial ad failed to load: " + loadAdError.getMessage());
                    mInterstitialAd = null;
                }
            });
    }

    public void showInterstitialAd(Activity activity) {
        if (mInterstitialAd != null) {
            mInterstitialAd.show(activity);
        } else {
            Log.d(TAG, "Interstitial ad not ready");
            loadInterstitialAd();
        }
    }

    private void setupInterstitialCallbacks() {
        mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
            @Override
            public void onAdDismissedFullScreenContent() {
                Log.d(TAG, "Interstitial ad dismissed");
                mInterstitialAd = null;
                loadInterstitialAd(); // Load the next ad
            }

            @Override
            public void onAdFailedToShowFullScreenContent(AdError adError) {
                Log.d(TAG, "Interstitial ad failed to show: " + adError.getMessage());
                mInterstitialAd = null;
            }
        });
    }

    public void loadRewardedAd() {
        AdRequest adRequest = new AdRequest.Builder().build();
        RewardedAd.load(context, REWARDED_AD_UNIT_ID, adRequest,
            new RewardedAdLoadCallback() {
                @Override
                public void onAdLoaded(RewardedAd rewardedAd) {
                    mRewardedAd = rewardedAd;
                    setupRewardedCallbacks();
                }

                @Override
                public void onAdFailedToLoad(LoadAdError loadAdError) {
                    Log.d(TAG, "Rewarded ad failed to load: " + loadAdError.getMessage());
                    mRewardedAd = null;
                }
            });
    }

    public void showRewardedAd(Activity activity, OnRewardedAdListener listener) {
        if (mRewardedAd != null) {
            mRewardedAd.show(activity, rewardItem -> {
                if (listener != null) {
                    listener.onRewarded(rewardItem.getType(), rewardItem.getAmount());
                }
            });
        } else {
            Log.d(TAG, "Rewarded ad not ready");
            Toast.makeText(context, "Ad not ready yet", Toast.LENGTH_SHORT).show();
            loadRewardedAd();
        }
    }

    private void setupRewardedCallbacks() {
        mRewardedAd.setFullScreenContentCallback(new FullScreenContentCallback() {
            @Override
            public void onAdDismissedFullScreenContent() {
                Log.d(TAG, "Rewarded ad dismissed");
                mRewardedAd = null;
                loadRewardedAd(); // Load the next ad
            }

            @Override
            public void onAdFailedToShowFullScreenContent(AdError adError) {
                Log.d(TAG, "Rewarded ad failed to show: " + adError.getMessage());
                mRewardedAd = null;
            }
        });
    }

    public interface OnRewardedAdListener {
        void onRewarded(String type, int amount);
    }
} 