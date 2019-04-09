/*
 * Copyright 2015 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.foshizzle.ui;

import android.app.Activity;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.FileProvider;
import android.util.Log;

import com.bumptech.glide.Glide;

import java.io.File;

import io.foshizzle.BuildConfig;
import io.foshizzle.data.api.deviantart.model.Deviation;

/**
 * An AsyncTask which retrieves a File from the Glide cache then shares it.
 */
class ShareDeviationImageTask extends AsyncTask<Void, Void, File> {

    private final Activity activity;
    private final Deviation deviation;

    ShareDeviationImageTask(Activity activity, Deviation deviation) {
        this.activity = activity;
        this.deviation = deviation;
    }

    @Override
    protected File doInBackground(Void... params) {
        final String url = deviation.content.getSource();
        try {
            return Glide
                    .with(activity)
                    .load(url)
                    .downloadOnly((int) deviation.content.width, (int) deviation.content.height)
                    .get();
        } catch (Exception ex) {
            Log.w("SHARE", "Sharing " + url + " failed", ex);
            return null;
        }
    }

    @Override
    protected void onPostExecute(File result) {
        if (result == null) { return; }
        // glide cache uses an unfriendly & extension-less name,
        // massage it based on the original
        String fileName = deviation.content.getSource();
        fileName = fileName.substring(fileName.lastIndexOf('/') + 1);
        File renamed = new File(result.getParent(), fileName);
        result.renameTo(renamed);
        Uri uri = FileProvider.getUriForFile(activity, BuildConfig.FILES_AUTHORITY, renamed);
        ShareCompat.IntentBuilder.from(activity)
                .setText(getShareText())
                .setType(getImageMimeType(fileName))
                .setSubject(deviation.title)
                .setStream(uri)
                .startChooser();
    }

    private String getShareText() {
        return new StringBuilder()
                .append("“")
                .append(deviation.title)
                .append("” by ")
                .append(deviation.author.username)
                .append("\n")
                .append(deviation.content.getSource())
                .toString();
    }

    private String getImageMimeType(@NonNull String fileName) {
        if (fileName.endsWith(".png")) {
            return "image/png";
        } else if (fileName.endsWith(".gif")) {
            return "image/gif";
        }
        return "image/jpeg";
    }
}
