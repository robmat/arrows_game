package com.batodev.arrows.ads

import android.app.Activity
import android.content.Context
import com.google.android.ump.ConsentDebugSettings
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.FormError
import com.google.android.ump.UserMessagingPlatform

class ConsentManager(
    context: Context,
) {
    private val consentInformation: ConsentInformation =
        UserMessagingPlatform.getConsentInformation(context)

    val canRequestAds: Boolean
        get() = consentInformation.canRequestAds()

    val isPrivacyOptionsRequired: Boolean
        get() =
            consentInformation.privacyOptionsRequirementStatus ==
                ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED

    fun gatherConsent(
        activity: Activity,
        onConsentResult: (FormError?) -> Unit,
    ) {
        val params = buildConsentRequestParameters(activity)

        consentInformation.requestConsentInfoUpdate(
            activity,
            params,
            {
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { formError ->
                    onConsentResult(formError)
                }
            },
            { requestConsentError ->
                onConsentResult(requestConsentError)
            },
        )
    }

    fun showPrivacyOptionsForm(
        activity: Activity,
        onDismiss: (FormError?) -> Unit,
    ) {
        UserMessagingPlatform.showPrivacyOptionsForm(activity, onDismiss)
    }

    companion object {
        private fun buildConsentRequestParameters(activity: Activity): ConsentRequestParameters {
            if (!BuildConfig.DRAW_DEBUG_STUFF) {
                return ConsentRequestParameters.Builder().build()
            }
            val debugSettings =
                ConsentDebugSettings
                    .Builder(activity)
                    .setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA)
                    .build()
            return ConsentRequestParameters
                .Builder()
                .setConsentDebugSettings(debugSettings)
                .build()
        }
    }
}
