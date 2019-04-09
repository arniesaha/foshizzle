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

import java.util.List;

public class DailyDeviation implements Parcelable {

    public final List<Deviation> results;

    public DailyDeviation(List<Deviation> results) {
        this.results = results;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        dest.writeValue(results);
    }

    public static class Builder {
        public  List<Deviation> results;


        private Builder setResults(List<Deviation> results){
            this.results = results;
            return this;
        }

        public DailyDeviation build() {
            return new DailyDeviation(results);
        }

    }

    protected DailyDeviation(Parcel in){

        results = (List<Deviation>)in.readValue(Object.class.getClassLoader());
    }

    @SuppressWarnings("unused")
    public static final Creator<DailyDeviation> CREATOR = new Creator<DailyDeviation>() {
        @Override
        public DailyDeviation createFromParcel(Parcel in) {
            return new DailyDeviation(in);
        }

        @Override
        public DailyDeviation[] newArray(int size) {
            return new DailyDeviation[size];
        }
    };
}
