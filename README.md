# Android-Ads-Helper
Android Ads code that is required in every app of Vasundhara Infotech [Vasundhara Infotech LLP](https://vasundharainfotechllp.com)

[![API](https://img.shields.io/badge/API-21%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=21)
[![Version](https://jitpack.io/v/vickypathak123/Android-Ads-Helper.svg)](https://jitpack.io/#vickypathak123/Android-Ads-Helper)

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

# Check [AdMob Error Codes & Logs](https://support.google.com/admob/thread/3494603/admob-error-codes-logs?hl=en)

## How To Use this Library

#### Note:
1. Your App-Application Class Compulsory Extend [AppOpenApplication](https://github.com/vickypathak123/Android-Ads-Helper/blob/master/adshelper/src/main/java/com/example/app/ads/helper/openad/AppOpenApplication.kt) Class.
2. If You Don't Pass Any Ad-ID Through [VasuAdsConfig](https://github.com/vickypathak123/Android-Ads-Helper/blob/master/adshelper/src/main/java/com/example/app/ads/helper/VasuAdsConfig.kt), It will take Test ID of this ad Automatically.
3. Inside this is already loading open ads so,
   If Your App Don't Need To Show Open-Ad then must be pass false in this `isEnableOpenAd(true /* Default Value */)` function,
   which is present in [VasuAdsConfig](https://github.com/vickypathak123/Android-Ads-Helper/blob/master/adshelper/src/main/java/com/example/app/ads/helper/VasuAdsConfig.kt).
4. Update This Flag `com.example.app.ads.helper.isNeedToShowAds = true /* Default Value */`,
   Must be pass false after your In-App Purchase or Subscription.
5. You must set [Native-Ad Attribute](https://github.com/vickypathak123/Android-Ads-Helper/blob/master/adshelper/src/main/res/values/attrs.xml) In your Main App-Theme.
6. For More Details Refer This [AppApplication](https://github.com/vickypathak123/Android-Ads-Helper/blob/master/app/src/main/java/com/example/ads/helper/demo/AppApplication.kt) Class.
7. Faced Any Error When Ads Loading, Check [AdMob Error Codes & Logs](https://support.google.com/admob/thread/3494603/admob-error-codes-logs?hl=en)
8. You Find Ads Log in Your Logcat with this Keyword `Admob_`.
9. Load All Ads In Only `onCreate()` or `initAds()` Method   
10. Need to destroy all Ads in your Launcher Activity's `onCreate()` method.
11. you need to check your subscription, in-app-purchase, or internet-access flag before load ads.

#### Config Ads ID In `onCreate()` Method Of Your Application Class
```kotlin
        // All Ad-Ids are Optional
        VasuAdsConfig.with(this)
            .isEnableOpenAd(true /* Default Value */) // Pass false if you don't need to show open ad in your project
            .needToTakeAllTestAdID(false /* Default Value */) // Pass true if you need to show Ads with Test Ad ID in your project
            .needToBlockInterstitialAd(false /* Default Value */) // Pass true if you check fullScreenNativeAds when Interstitial Ads Failed to Load
            .setAdmobAppId("YOUR_LIVE_APP_ID")
            .setAdmobBannerAdId("YOUR_LIVE_BANNER_AD_ID")
            .setAdmobInterstitialAdId("YOUR_LIVE_INTERSTITIAL_AD_ID")
            .setAdmobNativeAdvancedAdId("YOUR_LIVE_NATIVE_ADVANCED_AD_ID")
            .setAdmobOpenAdId("YOUR_LIVE_OPEN_AD_ID")
            .setAdmobRewardVideoAdId("YOUR_LIVE_REWARD_VIDEO_AD_ID")
            .setAdmobInterstitialAdRewardId("YOUR_LIVE_INTERSTITIAL_AD_REWARD_ID")
            .initialize()

        initMobileAds("test-ads-devise-id") // Pass devise ID if you need test ad on devise
```

## Native Ad Details

#### Native Ad Custom Layout ID Details (Compulsory Set This Id in your layout file)
<img src="https://github.com/vickypathak123/Android-Ads-Helper/blob/master/screenshots/native_ad_custom_layout_id.jpg" height="auto" width="600"/>

#### Native Ad Attribute Details (Compulsory Set This Attribute)
<img src="https://github.com/vickypathak123/Android-Ads-Helper/blob/master/screenshots/native_ad_attribute.jpg" height="auto" width="600"/>

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

# All Ads Load And Show Details

## Open Ad Details

#### Load open Ad In Your Application class
```kotlin
        // Load Open Ad After `initMobileAds()`
        OpenAdHelper.loadOpenAd(this)
```

#### Load & Show open Ad In [Splash Screen](https://github.com/vickypathak123/Android-Ads-Helper/blob/master/app/src/main/java/com/example/ads/helper/demo/SplashActivity.kt)
```kotlin
        /**
         * Call this method when you need to load your Open AD
         * you need to call this method only once in your launcher activity or your application class
         *
         * fContext -> this is a reference to your activity context
         * onAdLoad -> callback after ad successfully loaded
         */
        OpenAdHelper.loadOpenAd(
           fContext = mActivity,
           onAdLoad = {
              // Call When Open Ad Loaded Successfully
              // Perform your Action
           }
        )

        // Show Open Ad When You Need
        // Check first open ad is Available or Not
        if (OpenAdHelper.isAdAvailable()) {
            mActivity.isShowOpenAd {
                // Perform your Action
            }
        }
```

## Interstitial Ad Details
```kotlin
        /**
         * Call this method when you need to load your Interstitial AD
         * you need to call this method only once in any activity or fragment
         *
         * fContext -> this is a reference to your activity context
         * fIsShowFullScreenNativeAd -> pass false if you don't need native ad if interstitial ads not loaded
         * onAdLoaded -> callback after ad successfully loaded
         */
        InterstitialAdHelper.loadInterstitialAd(
           fContext = mActivity,
           fIsShowFullScreenNativeAd = true /* Default Value */,
           onAdLoaded = {
              // Call When Interstitial Ad Loaded Successfully
              // Perform your Action
           }
        )

        /**
         * Call this method when you need to show Interstitial AD
         * also this method call our fullscreen native AD when Interstitial Ad fails and give call bake on same way
         *
         * call this method with FragmentActivity instance
         * isBackAds -> pass true if you don't need to load ad after ad-close
         * onAdClosed -> this is a call back of your ad close, it will call also if your ad was not showing to the user
         */
        mActivity.isShowInterstitialAd(
           isBackAds = false /* Default Value */,
           onAdClosed = {
              // Perform your Action
           }
        )
```

## Native Advanced Ad Details
```kotlin
        /**
         * Call this method when you need to load your Native Advanced AD
         * you need to call this method only once in any activity or fragment
         *
         * this method will load your Native Advanced AD with 4 different size
         * like NativeAdsSize.Medium, NativeAdsSize.Big, NativeAdsSize.FullScreen, NativeAdsSize.Custom
         *
         * mContext -> this is a reference to your activity or fragment context
         * fSize -> it indicate your Ad Size
         * fLayout -> FrameLayout for add NativeAd View
         * fCustomAdView -> your native ad custom layout
         * isNeedLayoutShow -> [by Default value = true] pass false if you do not need to show AD at a time when it's loaded successfully
         * isAddVideoOptions -> [by Default value = true] pass false if you don't need to add video option
         * isAdLoaded -> lambda function call when ad isLoaded
         * onClickAdClose -> lambda function call when user click close button of ad
         * onAdClosed -> lambda function call after ad closed
         */
        NativeAdvancedModelHelper(mContext = mActivity).loadNativeAdvancedAd(
           fSize = NativeAdsSize.FullScreen,
           fLayout = mBinding.flNativeAdPlaceHolder,
           fCustomAdView = null,
           isNeedLayoutShow = true,
           isAddVideoOptions = true,
           isAdLoaded = {
              // Perform your Action
           },
           onClickAdClose = {
              // Perform your Action
           },
           onAdClosed = {
              // Perform your Action
           }
        )
```

## Gift Icon Ad Details
```kotlin
        /**
         * Call this method when you need to show your Gift AD
         *
         * fContext -> this is a reference to your activity context
         * fivGiftIcon -> this is your 1st visible main gift icon
         * fivBlastIcon -> this is your 2nd visible blast gift icon
         */
        GiftIconHelper.loadGiftAd(
           fContext = mActivity,
           fivGiftIcon = mBinding.layoutHeader.layoutGiftAd.giftAdIcon,
           fivBlastIcon = mBinding.layoutHeader.layoutGiftAd.giftBlastAdIcon
        )
```

## Reward Video Ad Details
```kotlin
        /**
         * Call this method when you need to load your Reward Video AD
         * you need to call this method only once in any activity or fragment
         *
         * fContext -> this is a reference to your activity or fragment context
         */
        RewardVideoHelper.loadRewardVideoAd(
           fContext = mActivity
        )

        /**
         * Call this method in your onCreate Method of activity or fragment
         * 
         * call this method with FragmentActivity instance
         *
         * onStartToLoadRewardVideoAd -> call when ad start to loading
         * onUserEarnedReward -> call when user Earned Reward or ad closed
         * onAdLoaded -> call when ad load successfully
         */
        mActivity.isShowRewardVideoAd(
           onStartToLoadRewardVideoAd = {
              // show progress when start to load reward video AD
           },
           onUserEarnedReward = { isUserEarnedReward ->
              // by Default value = false, it's true when user successfully earned reward
           },
           onAdLoaded = {
              // hide progress after successfully load reward video AD
           }
        )

        /**
         * Call this method in when you need to show in your activity or fragment
         *
         * call this method with FragmentActivity instance
         *
         * isAdShow -> call when ad showing
         */
        mActivity.showRewardVideoAd()

        mActivity.showRewardVideoAd(
           isAdShow = {
              // Perform your Action
           }
        )
```

## Rewarded Interstitial Ad Details
```kotlin
        /**
         * Call this method when you need to load your Reward Interstitial AD
         * you need to call this method only once in any activity or fragment
         *
         * fContext -> this is a reference to your activity or fragment context
         */
        InterstitialRewardHelper.loadRewardedInterstitialAd(
           fContext = mActivity
        )

        /**
         * Call this method in your onCreate Method of activity or fragment
         * 
         * call this method with FragmentActivity instance
         *
         * onStartToLoadRewardedInterstitialAd -> call when ad start to loading
         * onUserEarnedReward -> call when user Earned Reward or ad closed
         * onAdLoaded -> call when ad load successfully
         */
        mActivity.isShowRewardedInterstitialAd(
           onStartToLoadRewardedInterstitialAd = {
              // show progress when start to load reward video AD
           },
           onUserEarnedReward = { isUserEarnedReward ->
              // by Default value = false, it's true when user successfully earned reward
           },
           onAdLoaded = {
              // hide progress after successfully load reward video AD
           }
        )

        /**
         * Call this method in when you need to show in your activity or fragment
         *
         * call this method with FragmentActivity instance
         */
        mActivity.showRewardedInterstitialAd()
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

  <a href="https://www.linkedin.com/in/akshay-harsoda-b66820116" rel="nofollow">
  <img alt="Follow me on LinkedIn" 
       height="50" width="50" 
       src="https://github.com/vickypathak123/Android-Ads-Helper/blob/master/social/linkedin.png" 
       style="max-width:100%;">
  </a>

  <a href="https://twitter.com/Akshayharsoda1" rel="nofollow">
  <img alt="Follow me on Twitter" 
       height="50" width="50"
       src="https://github.com/vickypathak123/Android-Ads-Helper/blob/master/social/twitter.png" 
       style="max-width:100%;">
  </a>

  <a href="https://www.facebook.com/akshay.harsoda" rel="nofollow">
  <img alt="Follow me on Facebook" 
       height="50" width="50" 
       src="https://github.com/vickypathak123/Android-Ads-Helper/blob/master/social/facebook.png" 
       style="max-width:100%;">
  </a>
