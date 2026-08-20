package com.batodev.arrows.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.core.net.toUri
import com.batodev.arrows.core.resources.R
import com.google.android.play.core.review.ReviewManagerFactory

object SettingsUtils {
    fun launchBrowser(
        context: Context,
        url: String,
    ) {
        try {
            context.startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
        } catch (_: Exception) {
            val error = context.getString(R.string.error_could_not_open_browser)
            Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
        }
    }

    fun launchEmail(context: Context) {
        try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            val version = packageInfo.versionName
            val device = "${Build.MANUFACTURER} ${Build.MODEL} (SDK ${Build.VERSION.SDK_INT})"
            val intent =
                Intent(Intent.ACTION_SENDTO).apply {
                    data = "mailto:".toUri()
                    putExtra(Intent.EXTRA_EMAIL, arrayOf("support@emberfox.online"))
                    putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.email_subject))
                    val body =
                        context.getString(
                            R.string.email_body_template,
                            version,
                            device,
                        )
                    putExtra(Intent.EXTRA_TEXT, body)
                }
            context.startActivity(intent)
        } catch (_: Exception) {
            val error = context.getString(R.string.error_could_not_open_email)
            Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
        }
    }

    fun launchReviewFlow(context: Context) {
        if (context !is Activity) return
        val manager = ReviewManagerFactory.create(context)
        manager.requestReviewFlow().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                manager.launchReviewFlow(context, task.result)
            } else {
                val error = context.getString(R.string.error_could_not_launch_review)
                Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
            }
        }
    }
}
