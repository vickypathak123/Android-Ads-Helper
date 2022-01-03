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
	dependencies {
	        implementation 'com.github.vickypathak123:Android-Ads-Helper:latest_build_version'
	}
```

## How To Use this Library

#### In Kotlin

###### Config Ads ID In `onCreate()` Method Of Your Application Class
```kotlin
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

###### Load Ads In Only `onCreate()` or `initAds()` Method
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

###### Show Loaded Ads Anywhere
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

###### For Set Reward Ad Listener, Call In Your `initViewListener()` Method
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