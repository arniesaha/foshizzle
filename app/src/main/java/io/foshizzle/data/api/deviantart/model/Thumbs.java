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

package io.foshizzle.data.api.deviantart.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Models links to the various quality of images of a shot.
 */
public class Thumbs implements Parcelable {
    /*
     "src": "http://t09.deviantart.net/YGzCqufbBGg6BGxYgnIHUb0t5Xw=/300x200/filters:fixed_height(100,100):origin()/pre09/1026/th/pre/f/2017/114/0/c/home_by_ryky-db6yptb.png",
                    "height": 176,
                    "width": 300,
                    "transparency": false
     */

    private String src;
    private int height;
    private int width;
    private boolean transparency;


    public Thumbs(String src, int height, int width, boolean transparency) {
        this.src = src;
        this.height = height;
        this.width = width;
        this.transparency = transparency;

    }

    protected Thumbs(Parcel in) {
        src = in.readString();
        height = in.readInt();
        width = in.readInt();
        transparency = Boolean.valueOf(in.readString());
    }

    public String getSrc() {
        return src;
    }

//    public int[] bestSize() {
//        return !TextUtils.isEmpty(hidpi) ? TWO_X_IMAGE_SIZE : NORMAL_IMAGE_SIZE;
//    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(src);
        dest.writeInt(height);
        dest.writeInt(width);
        dest.writeString(String.valueOf(transparency));
    }

    @SuppressWarnings("unused")
    public static final Creator<Thumbs> CREATOR = new Creator<Thumbs>() {
        @Override
        public Thumbs createFromParcel(Parcel in) {
            return new Thumbs(in);
        }

        @Override
        public Thumbs[] newArray(int size) {
            return new Thumbs[size];
        }
    };

}
