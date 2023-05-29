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
3. Inside this is already loading open ads so, If Your App Don't Need To Show Open-Ad then must be pass false in this `isEnableOpenAd(true /* Default Value */)` function, which is present
   in [VasuAdsConfig](https://github.com/vickypathak123/Android-Ads-Helper/blob/master/adshelper/src/main/java/com/example/app/ads/helper/VasuAdsConfig.kt).
4. Update This Flag `com.example.app.ads.helper.isNeedToShowAds = true /* Default Value */`, Must be pass false after your In-App Purchase or Subscription.
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
    .isDebugModeEnable(false /* Default Value */) // Pass true if you check ads logs 
    .setAdmobAppId("YOUR_LIVE_APP_ID")
    .setAdmobBannerAdId("YOUR_LIVE_BANNER_AD_ID")
    .setAdmobInterstitialAdId("YOUR_LIVE_INTERSTITIAL_AD_ID")
    .setAdmobNativeAdvancedAdId("YOUR_LIVE_NATIVE_ADVANCED_AD_ID")
    .setAdmobOpenAdId("YOUR_LIVE_OPEN_AD_ID")
    .setAdmobRewardVideoAdId("YOUR_LIVE_REWARD_VIDEO_AD_ID")
    .setAdmobInterstitialAdRewardId("YOUR_LIVE_INTERSTITIAL_AD_REWARD_ID")
    .setLifeTimeProductKey("YOUR_LIFE_TIME_PURCHASE_KEY")
    .setSubscriptionKey("YOUR_SUBSCRIPTION_KEYS")
    .needToGetProductListFromRevenueCat(false /* Default value */)//pass true if you want to fetch product list from revenue cat
    .setRevenueCatId("YOUR_REVENUE_CAT_ID")
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
AppOpenAdHelper.loadOpenAd(
    fContext = mActivity,
    onAdLoad = {
        // Call When Open Ad Loaded Successfully
        // Perform your Action
    }
)

// Show Open Ad When You Need
// Check first open ad is Available or Not
if (AppOpenAdHelper.isAppOpenAdAvailable()) {
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
 * @param fContext this is a reference to your activity context
 * @param onAdLoaded callback after ad successfully loaded
 */
InterstitialAdHelper.loadAd(
    fContext = mActivity,
    onAdLoaded = {

    }
)

/**
 * Call this method when you need to show Interstitial AD
 * also this method call our offline native dialog AD [FullScreenNativeAdDialogActivity] when Interstitial Ad fails and give call bake on same way
 *
 * Use of this Method
 * activity.showInterstitialAd {[your code which has run after AD show or if AD fails to show]}
 * call this method with [Activity] instance
 *
 * @param fIsShowFullScreenNativeAd pass false if you don't need native ad if interstitial ads not loaded
 * @param onAdClosed this is a call back of your ad close, it will call also if your ad was not showing to the user
 */
mActivity.showInterstitialAd(fIsShowFullScreenNativeAd = true) { isAdShowing, isShowFullScreenAd ->
}
```

## Native Advanced Ad Details

```kotlin
        /**
 * Call this method when you need to load your Native Advanced AD
 * you need to call this method only once in any activity or fragment
 *
 * this method will load your Native Advanced AD with 5 different size
 * like NativeAdsSize.Medium, NativeAdsSize.Big, NativeAdsSize.FullScreen, NativeAdsSize.Custom,NativeAdsSize.VOICE_GPS
 *
 * you can set your ads 'I' icon place through pass this type of parameter
 * like NativeAdOptions.ADCHOICES_TOP_RIGHT, NativeAdOptions.ADCHOICES_TOP_LEFT, NativeAdOptions.ADCHOICES_BOTTOM_RIGHT, NativeAdOptions.ADCHOICES_BOTTOM_LEFT
 *
 * mContext -> this is a reference to your activity or fragment context
 * fSize ->  it indicate your Ad Size
 * fLayout -> FrameLayout for add NativeAd View
 * adChoicesPlacement -> Ads 'I' icon place
 * fCustomAdView -> your native ad custom layout
 * fCustomShimmerView -> your shimmer custom layout
 * isNeedLayoutShow -> [by Default value = true] pass false if you do not need to show AD at a time when it's loaded successfully
 * isAddVideoOptions -> [by Default value = true] pass false if you don't need to add video option
 * isSetDefaultButtonColor -> [by Default value = true] pass false if you don't need to change in ad action button
 * isNeedToShowShimmerLayout -> [by Default value = true] pass false if you don't want to add shimmer layout
 * topMargin -> [by Default value = 0] pass top margin value ,
 * startMargin -> [by Default value = 0] pass start margin value,
 * bottomMargin -> [by Default value = 0] pass bottom margin value,
 * endMargin -> [by Default value = 0] pass end margin value,
 * isAdLoaded -> lambda function call when ad isLoaded
 * onClickAdClose -> lambda function call when user click close button of ad
 * onAdClosed -> lambda function call after ad closed
 * onAdFailed -> lambda function call after ad failed to load
 */
var nativeAdModeHelper = NativeAdModelHelper(mContext = mActivity)
nativeAdModeHelper?.loadNativeAdvancedAd(
    fSize = NativeAdsSize.FullScreen,
    fLayout = mBinding.flNativeAdPlaceHolder,
    fCustomAdView = null,
    fCustomShimmerView = null,
    adChoicesPlacement = NativeAdOptions.ADCHOICES_TOP_RIGHT,
    isNeedLayoutShow = true,
    isAddVideoOptions = true,
    isSetDefaultButtonColor = true,
    isNeedToShowShimmerLayout = true,
    topMargin = 100,
    startMargin = 100,
    bottomMargin = 50,
    endMargin = 50,
    onAdLoaded = {
        // Perform your Action
    },
    onClickAdClose = {
        // Perform your Action
    },
    onAdClosed = {
        // Perform your Action
    },
    onAdFailed = {
        // Perform your Action
    }
)

/**
 * Call this method in onResume to manage shimmer layout visibility online offline
 * you need to call this method only once in any activity or fragment
 *
 * isNeedToShowAd -> flag that we need to show ad or not
 */
nativeAdModeHelper?.manageShimmerLayoutVisibility(AdsManager(mContext).isNeedToShowAd())

```

## Banner Ad Details

```kotlin

private val mBanner: BannerHelper by lazy { BannerHelper(mActivity) }

/**
 * Call this method when you need to load your Banner Ad
 * you need to call this method only once in any activity or fragment
 *
 * this method will load your Banner Ad  with 7 different size
 * like BANNER,
LARGE_BANNER,
MEDIUM_RECTANGLE,
FULL_BANNER,
LEADERBOARD,
ADAPTIVE_BANNER,
SMART_BANNER,
 *
 * fBannerAdSize ->  it indicate your Banner Ad Size
 * fLayout -> FrameLayout for add Banner
 */
mBanner.loadBanner(fBannerAdSize = BannerAdSize.BANNER, fLayout = flBanner)


/**
 * Call this method in onResume to resume banner ad
 */
mBanner.mAdView?.resume()

/**
 * Call this method in onPause to pause banner ad
 */
mBanner.mAdView?.pause()

```

## Reward Video Ad Details

```kotlin
   /**
 * Call this method when you need to load your Reward Video AD
 * you need to call this method only once in any activity or fragment
 *
 *
 * Use of this Method
 * loadAd(
 *      fContext = reference of your activity or fragment context
 *      onStartToLoadAd = {[show progress when start to load Reward Video AD]},
 *      onAdLoaded = {[hide progress after successfully load Reward Video AD]},
 * )
 *
 * @param fContext this is a reference to your activity or fragment context
 * @param onStartToLoadAd @see [AdMobAdsListener.onStartToLoadRewardVideoAd]
 * @param onAdLoaded @see [AdMobAdsListener.onAdLoaded]
 */
RewardedVideoAdHelper.loadAd(
    fContext = mActivity,
    onStartToLoadAd = {
    },
    onAdLoaded = {
    },
)
/**
 * Call this method when you need to show your Reward Video AD
 * you need to call this method with your Activity Context
 *
 *
 * Use of this Method
 * activity.showRewardedVideoAd(
 *      onUserEarnedReward = { isUserEarnedReward -> [by Default value = false, it's true when user successfully earned reward]},
 * )
 *
 * @param onUserEarnedReward @see [AdMobAdsListener.onUserEarnedReward]
 */
mActivity.showRewardedVideoAd(
    onUserEarnedReward = { isUserEarnedReward ->
    }
)
```

## Rewarded Interstitial Ad Details

```kotlin
   /**
 * Call this method when you need to load your Reward Interstitial AD
 * you need to call this method only once in any activity or fragment
 *
 *
 * Use of this Method
 * loadAd(
 *      fContext = reference of your activity or fragment context
 *      onStartToLoadAd = {[show progress when start to load Reward Interstitial AD]},
 *      onAdLoaded = {[hide progress after successfully load Reward Interstitial AD]},
 * )
 *
 * @param fContext this is a reference to your activity or fragment context
 * @param onStartToLoadAd @see [AdMobAdsListener.onStartToLoadRewardedInterstitialAd]
 * @param onAdLoaded @see [AdMobAdsListener.onAdLoaded]
 */
RewardedInterstitialAdHelper.loadAd(
    fContext = mActivity,
    onStartToLoadAd = {
    },
    onAdLoaded = {
    },
)

/**
 * Call this method when you need to show your Reward Interstitial AD
 * you need to call this method with your Activity Context
 *
 *
 * Use of this Method
 * activity.showRewardedInterstitialAd(
 *      onUserEarnedReward = { isUserEarnedReward -> [by Default value = false, it's true when user successfully earned reward]},
 * )
 *
 * @param onUserEarnedReward @see [AdMobAdsListener.onUserEarnedReward]
 */
mActivity.showRewardedInterstitialAd(
    onUserEarnedReward = { isUserEarnedReward ->

    }
)

```

### ⭐️ If you liked it support me with your stars!
