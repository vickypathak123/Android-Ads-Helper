# Android-Ads-Helper
Android Ads code that is required in every app of Vasundhara Infotech [Vasundhara Infotech LLP](https://vasundharainfotechllp.com)

###### latest_build_version [![](https://jitpack.io/v/vickypathak123/Android-Ads-Helper.svg)](https://jitpack.io/#vickypathak123/Android-Ads-Helper)

## Using `build.gradle`
###### Step 1. Add the JitPack repository to your project's `build.gradle`
```groovy
	allprojects {
		repositories {
			maven { url 'https://jitpack.io' }
		}
	}
```

###### Step 2. Add the dependency to your module's `build.gradle`
```groovy
    android {
        defaultConfig {
            multiDexEnabled true
        }
    }

	dependencies {
            implementation 'com.android.support:multidex:1.0.3'
            implementation 'com.github.vickypathak123:Android-Ads-Helper:latest_build_version'
	}
```

## How To Use this Library

#### Note:
1. Your App-Application Class Compulsory Extend [AppApplication](https://github.com/vickypathak123/Android-Ads-Helper/blob/master/app/src/main/java/com/example/ads/helper/demo/AppApplication.kt) Class.
2. Please make sure before submitting final bundle for uploading on console,
   initMobileAds(isAppInTesting = true) must be pass false.
3. If You Don't Pass Any Ad-ID Through [VasuAdsConfig](https://github.com/vickypathak123/Android-Ads-Helper/blob/master/adshelper/src/main/java/com/example/app/ads/helper/VasuAdsConfig.kt), It will take Test ID of this ad Automatically.
4. Inside this is already loading open ads so,
   If Your App Don't Need To Show Open-Ad then must be pass false in this `isEnableOpenAd(true /* Default Value */)` function,
   which is present in [VasuAdsConfig](https://github.com/vickypathak123/Android-Ads-Helper/blob/master/adshelper/src/main/java/com/example/app/ads/helper/VasuAdsConfig.kt).
5. Update This Flag `com.example.app.ads.helper.isNeedToShowAds = true /* Default Value */`,
   Must be pass false after your In-App Purchase or Subscription.
6. You must set [Native-Ad Attribute](https://github.com/vickypathak123/Android-Ads-Helper/blob/master/adshelper/src/main/res/values/attrs.xml) In your Main App-Theme.
7. For More Details Refer This [AppApplication](https://github.com/vickypathak123/Android-Ads-Helper/blob/master/app/src/main/java/com/example/ads/helper/demo/AppApplication.kt) Class.

#### Config Ads ID In `onCreate()` Method Of Your Application Class
```kotlin
        // All Ad-Ids are Optional
        VasuAdsConfig.with(this)
            .isEnableOpenAd(true /* Default Value */) // Pass false if you don't need to show open ad in your project
            .setAdmobAppId("YOUR_LIVE_APP_ID")
            .setAdmobBannerAdId("YOUR_LIVE_BANNER_AD_ID")
            .setAdmobInterstitialAdId("YOUR_LIVE_INTERSTITIAL_AD_ID")
            .setAdmobNativeAdvancedAdId("YOUR_LIVE_NATIVE_ADVANCED_AD_ID")
            .setAdmobOpenAdId("YOUR_LIVE_OPEN_AD_ID")
            .setAdmobRewardVideoAdId("YOUR_LIVE_REWARD_VIDEO_AD_ID")
            .setAdmobInterstitialAdRewardId("YOUR_LIVE_INTERSTITIAL_AD_REWARD_ID")
            .initialize()

        initMobileAds(isAppInTesting = true) // Pass false when you give your project in Internal Testing Or Live
```

## Native Ad Details

#### Native Ad Attribute Details
<img src="https://github.com/vickypathak123/Android-Ads-Helper/blob/master/screenshots/native_ad_attribute.jpg" height="auto" width="500"/> <img src="https://github.com/vickypathak123/Android-Ads-Helper/blob/master/screenshots/native_ad_custom_layout_id.jpg" height="auto" width="500"/>

#### Need to Initial all Native Ad Attribute in your App Theme
```xml
        <style name="YOUR_APP_THEME" parent="Theme.AppCompat.DayNight.NoActionBar">
                <item name="colorPrimary">@color/colorPrimary</item>
                <item name="colorPrimaryDark">@color/colorPrimaryDark</item>
                <item name="colorAccent">@color/colorAccent</item>
        
                <item name="native_ads_main_color">@android:color/white</item>
                <item name="native_ads_label_text_color">@android:color/white</item>
                <item name="native_ads_background_color">@android:color/white</item>
                <item name="native_ads_body_text_color">@android:color/black</item>
        </style>
```

## Open Ad Details

#### Load & Show open Ad In [Splash Screen](https://github.com/vickypathak123/Android-Ads-Helper/blob/master/app/src/main/java/com/example/ads/helper/demo/SplashActivity.kt)
```kotlin
        // Load Open Ad When You Need
        OpenAdHelper.loadOpenAd(mActivity, onAdLoad = {
            // Call When Open Ad Loaded Successfully
            // Perform your Action
        })

        // Show Open Ad When You Need
        // Check first open ad is Available or Not
        if (OpenAdHelper.isAdAvailable()) {
            mActivity.isShowOpenAd {
                // Perform your Action
            }
        }
```

#### Load open Ad In Your Application class
```kotlin
        // Load Open Ad After `initMobileAds()`
        OpenAdHelper.loadOpenAd(this)
```

## All Ad Details

#### Load Ads In Only `onCreate()` or `initAds()` Method
```kotlin
        InterstitialAdHelper.loadInterstitialAd(fContext = mActivity)
        RewardVideoHelper.loadRewardVideoAd(fContext = mActivity)
        InterstitialRewardHelper.loadRewardedInterstitialAd(fContext = mActivity)

        NativeAdvancedModelHelper(mActivity).loadNativeAdvancedAd(
            NativeAdsSize.Medium,
            mBinding.flNativeAdPlaceHolderMedium
        )

        GiftIconHelper.loadGiftAd(
            fContext = mActivity,
            fivGiftIcon = mBinding.layoutHeader.layoutGiftAd.giftAdIcon,
            fivBlastIcon = mBinding.layoutHeader.layoutGiftAd.giftBlastAdIcon
        )
```

#### Show Loaded Ads Anywhere
```kotlin
        // For Showing InterstitialAd
        mActivity.isShowInterstitialAd { 
            // Perform your Action
        }

        // For Showing RewardVideoAd
        mActivity.showRewardVideoAd()

        // For Showing RewardedInterstitialAd
        mActivity.showRewardedInterstitialAd()
```

#### For Set Reward Ad Listener, Call In Your `initViewListener()` Method
```kotlin
        // For RewardVideoAd
        mActivity.isShowRewardVideoAd(
            onStartToLoadRewardVideoAd = {
                // Call When New Ad Start To Load
            },
            onUserEarnedReward = { isUserEarnedReward ->
                // Call After Ad Closed And 'isUserEarnedReward = true' if user earned his reward
            },
            onAdLoaded = {
                // Call After Ad Loaded
            }
        )

        // For Showing RewardedInterstitialAd
        mActivity.isShowRewardedInterstitialAd(
            onStartToLoadRewardedInterstitialAd = {
                // Call When New Ad Start To Load
            },
            onUserEarnedReward = { isUserEarnedReward ->
                // Call After Ad Closed And 'isUserEarnedReward = true' if user earned his reward
            },
            onAdLoaded = {
                // Call After Ad Loaded
            }
        )
```

### ⭐️ If you liked it support me with your stars!

## Developed By
[Akshay Harsoda](https://github.com/AkshayHarsoda) - [akshayharsoda@gmail.com](https://mail.google.com/mail/u/0/?view=cm&fs=1&to=akshayharsoda@gmail.com&su=https://github.com/vickypathak123/Android-Ads-Helper&body=&bcc=akshayharsoda@gmail.com&tf=1)

  <a href="https://github.com/AkshayHarsoda" rel="nofollow">
  <img alt="Follow me on Google+" 
       height="50" width="50" 
       src="https://github.com/vickypathak123/Android-Ads-Helper/blob/master/social/github.png" 
       style="max-width:100%;">
  </a>

  <a href="" rel="nofollow">
  <img alt="Follow me on LinkedIn" 
       height="50" width="50" 
       src="https://github.com/vickypathak123/Android-Ads-Helper/blob/master/social/linkedin.png" 
       style="max-width:100%;">
  </a>

  <a href="" rel="nofollow">
  <img alt="Follow me on Facebook" 
       height="50" width="50"
       src="https://github.com/vickypathak123/Android-Ads-Helper/blob/master/social/twitter.png" 
       style="max-width:100%;">
  </a>

  <a href="" rel="nofollow">
  <img alt="Follow me on Facebook" 
       height="50" width="50" 
       src="https://github.com/vickypathak123/Android-Ads-Helper/blob/master/social/facebook.png" 
       style="max-width:100%;">
  </a>