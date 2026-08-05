plugins {
    id("arrows.android.library.compose")
}

android {
    namespace = "com.batodev.arrows.ads"
    buildFeatures {
        buildConfig = true
    }
    buildTypes {
        release {
            buildConfigField("String", "BANNER_AD_UNIT_ID", "\"ca-app-pub-9667420067790140/3105779401\"")
            buildConfigField("String", "REWARDED_AD_UNIT_ID", "\"ca-app-pub-9667420067790140/6849583291\"")
            buildConfigField("String", "INTERSTITIAL_AD_UNIT_ID", "\"ca-app-pub-9667420067790140/3915454308\"")
            buildConfigField("Boolean", "DRAW_DEBUG_STUFF", "false")
        }
        debug {
            buildConfigField("String", "BANNER_AD_UNIT_ID", "\"ca-app-pub-3940256099942544/6300978111\"")
            buildConfigField("String", "REWARDED_AD_UNIT_ID", "\"ca-app-pub-3940256099942544/5224354917\"")
            buildConfigField("String", "INTERSTITIAL_AD_UNIT_ID", "\"ca-app-pub-3940256099942544/1033173712\"")
            buildConfigField("Boolean", "DRAW_DEBUG_STUFF", "false")
        }
    }
}

dependencies {
    implementation(libs.koin.android)
    // Behind the shared catalog's value (25.4.0) - still sourced from it,
    // strictly pinned to this repo's own value.
    api(libs.play.services.ads) { version { strictly("25.0.0") } }
    api(libs.google.ump)
}
