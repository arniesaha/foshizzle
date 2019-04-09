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


import java.util.UUID;


/**
 * Models a commend on a Deviantart comment.
 */
public class Comment {


    /*
    {
commentid (UUID)
parentid (UUID|null)
posted (string)
replies (integer)
hidden (string|null)
body (html)
user (user object)
}
     */

    private UUID commentid;
    private UUID parentid;
    private String posted;
    private int replies;
    private String hidden;
    public String body;
    public User user;

    public Comment(UUID commentid,
                   UUID parentid,
                   String posted,
                   int replies,
                   String hidden,
                   String body,
                   User user) {
        this.commentid = commentid;
        this.parentid = parentid;
        this.posted = posted;
        this.replies = replies;
        this.hidden = hidden;
        this.body = body;
        this.user = user;
    }

    @Override
    public String toString() {
        return body;
    }
}
