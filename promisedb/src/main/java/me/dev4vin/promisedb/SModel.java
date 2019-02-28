/*
 * Copyright 2017, Peter Vincent
 * Licensed under the Apache License, Version 2.0, Promise.
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.dev4vin.promisedb;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created on 4/12/18 by yoctopus.
 */

public abstract class SModel implements S, Parcelable {
    private int id;
    private long createdAt, updatedAt;

    public long createdAt() {
        return createdAt;
    }

    public void createdAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long updatedAt() {
        return updatedAt;
    }

    public void updatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public void id(int id) {
        this.id = id;
    }

    @Override
    public int id() {
        return id;
    }

    public SModel() {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.id);
        dest.writeLong(this.createdAt);
        dest.writeLong(this.updatedAt);
    }

    protected SModel(Parcel in) {
        this.id = in.readInt();
        this.createdAt = in.readLong();
        this.updatedAt = in.readLong();
    }
}
