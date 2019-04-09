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

public class CommentResponse implements Parcelable {

    private final boolean has_more;
    public final int next_offset;
    private final boolean has_less;
    public final int prev_offset;
    public final List<Comment> thread;

    public CommentResponse(boolean has_more, int next_offset, boolean has_less, int prev_offset, List<Comment> thread) {
        this.has_more = has_more;
        this.next_offset = next_offset;
        this.has_less = has_less;
        this.prev_offset = prev_offset;
        this.thread = thread;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(String.valueOf(has_more));
        dest.writeInt(next_offset);
        dest.writeString(String.valueOf(has_less));
        dest.writeInt(prev_offset);
        dest.writeValue(thread);
    }

    public static class Builder {
        private boolean has_more;
        public int next_offset;
        private boolean has_less;
        public int prev_offset;
        public  List<Comment> thread;

        private Builder setHasMore(boolean has_more){
            this.has_more = has_more;
            return this;
        }

        private Builder setNextOffset(int next_offset){
            this.next_offset = next_offset;
            return this;
        }

        private Builder setHasLess(boolean has_less){
            this.has_less = has_less;
            return this;
        }

        private Builder setPrevOffset(int prev_offset){
            this.prev_offset = prev_offset;
            return this;
        }

        public CommentResponse build() {
            return new CommentResponse(has_more, next_offset, has_less, prev_offset, thread);
        }

    }

    protected CommentResponse(Parcel in){
        has_more = Boolean.valueOf(in.readString());
        next_offset = in.readInt();
        has_less = Boolean.valueOf(in.readString());
        prev_offset = in.readInt();
        thread = (List<Comment>)in.readValue(Object.class.getClassLoader());
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<CommentResponse> CREATOR = new Parcelable.Creator<CommentResponse>() {
        @Override
        public CommentResponse createFromParcel(Parcel in) {
            return new CommentResponse(in);
        }

        @Override
        public CommentResponse[] newArray(int size) {
            return new CommentResponse[size];
        }
    };
}
