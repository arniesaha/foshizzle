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

public class Like implements Parcelable {

    private final boolean success;
    public final int favourites;

    public Like(boolean success, int favourites) {
        this.success = success;
        this.favourites = favourites;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(String.valueOf(success));
        dest.writeInt(favourites);
    }

    public static class Builder {
        private boolean success;
        public int favourites;

        private Builder setSuccess(boolean success){
            this.success = success;
            return this;
        }

        private Builder setFavourites(int favourites){
            this.favourites = favourites;
            return this;
        }


        public Like build() {
            return new Like(success, favourites);
        }

    }

    protected Like(Parcel in){
        success = Boolean.valueOf(in.readString());
        favourites = in.readInt();
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Like> CREATOR = new Parcelable.Creator<Like>() {
        @Override
        public Like createFromParcel(Parcel in) {
            return new Like(in);
        }

        @Override
        public Like[] newArray(int size) {
            return new Like[size];
        }
    };
}
