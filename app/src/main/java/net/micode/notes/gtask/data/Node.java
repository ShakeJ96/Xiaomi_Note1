/*
 * Copyright (c) 2010-2011, The MiCode Open Source Community (www.micode.net)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.micode.notes.gtask.data;

import android.database.Cursor;

import org.json.JSONObject;

public abstract class Node {
    public static final int SYNC_ACTION_NONE = 0;

    public static final int SYNC_ACTION_ADD_REMOTE = 1;

    public static final int SYNC_ACTION_ADD_LOCAL = 2;

    public static final int SYNC_ACTION_DEL_REMOTE = 3;

    public static final int SYNC_ACTION_DEL_LOCAL = 4;

    public static final int SYNC_ACTION_UPDATE_REMOTE = 5;

    public static final int SYNC_ACTION_UPDATE_LOCAL = 6;

    public static final int SYNC_ACTION_UPDATE_CONFLICT = 7;

    public static final int SYNC_ACTION_ERROR = 8;

    private String mGid;

    private String mName;

    private long mLastModified;

    private boolean mDeleted;
    private String title;
    private String content;

    private boolean isPinned; // 置顶状态
    private boolean isStarred; // 星标状态

    public boolean isPinned() {
        return isPinned;
    }

    public void setPinned(boolean pinned) {
        isPinned = pinned;
    }

    public boolean isStarred() {
        return isStarred;
    }

    public void setStarred(boolean starred) {
        isStarred = starred;
    }

    public String getmGid() {
        return mGid;
    }

    public void setmGid(String mGid) {
        this.mGid = mGid;
    }

    public String getmName() {
        return mName;
    }

    public void setmName(String mName) {
        this.mName = mName;
    }

    public long getmLastModified() {
        return mLastModified;
    }

    public void setmLastModified(long mLastModified) {
        this.mLastModified = mLastModified;
    }

    public boolean ismDeleted() {
        return mDeleted;
    }

    public void setmDeleted(boolean mDeleted) {
        this.mDeleted = mDeleted;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Node() {
        mGid = null;
        mName = "";
        mLastModified = 0;
        mDeleted = false;
    }

    public abstract JSONObject getCreateAction(int actionId);

    public abstract JSONObject getUpdateAction(int actionId);

    public abstract void setContentByRemoteJSON(JSONObject js);

    public abstract void setContentByLocalJSON(JSONObject js);

    public abstract JSONObject getLocalJSONFromContent();

    public abstract int getSyncAction(Cursor c);

    public void setGid(String gid) {
        this.mGid = gid;
    }

    public void setName(String name) {
        this.mName = name;
    }

    public void setLastModified(long lastModified) {
        this.mLastModified = lastModified;
    }

    public void setDeleted(boolean deleted) {
        this.mDeleted = deleted;
    }

    public String getGid() {
        return this.mGid;
    }

    public String getName() {
        return this.mName;
    }

    public long getLastModified() {
        return this.mLastModified;
    }

    public boolean getDeleted() {
        return this.mDeleted;
    }

}
