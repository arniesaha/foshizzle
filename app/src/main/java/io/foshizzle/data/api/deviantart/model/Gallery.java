package io.foshizzle.data.api.deviantart.model;

/**
 * Created by macbook on 06/07/17.
 */

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

import android.os.Parcel;
import android.os.Parcelable;


import java.util.List;

public class Gallery implements Parcelable {

    private final boolean has_more;
    public final int next_offset;
    public final List<Deviation> results;

    public Gallery(boolean has_more, int next_offset, List<Deviation> results) {
        this.has_more = has_more;
        this.next_offset = next_offset;
        this.results = results;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(String.valueOf(has_more));
        dest.writeInt(next_offset);
        dest.writeValue(results);
    }

    public static class Builder {
        private boolean has_more;
        public int next_offset;
        public  List<Deviation> results;

        private Builder setHasMore(boolean has_more){
            this.has_more = has_more;
            return this;
        }

        private Builder setNextOffset(int next_offset){
            this.next_offset = next_offset;
            return this;
        }


        private Builder setResults(List<Deviation> results){
            this.results = results;
            return this;
        }

        public Gallery build() {
            return new Gallery(has_more, next_offset, results);
        }

    }

    protected Gallery(Parcel in){
        has_more = Boolean.valueOf(in.readString());
        next_offset = in.readInt();
        results = (List<Deviation>)in.readValue(Object.class.getClassLoader());
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Gallery> CREATOR = new Parcelable.Creator<Gallery>() {
        @Override
        public Gallery createFromParcel(Parcel in) {
            return new Gallery(in);
        }

        @Override
        public Gallery[] newArray(int size) {
            return new Gallery[size];
        }
    };
}
