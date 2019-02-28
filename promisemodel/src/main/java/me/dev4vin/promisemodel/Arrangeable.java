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

package me.dev4vin.promisemodel;

/**
 * Created on 7/17/18 by yoctopus.
 */
public class Arrangeable<Value, Key> {
    private Value value;
    private Key key;

    public Value value() {
        return value;
    }

    public Arrangeable<Value, Key> value(Value value) {
        this.value = value;
        return this;
    }

    public Key key() {
        return key;
    }

    public Arrangeable<Value, Key> key(Key key) {
        this.key = key;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Arrangeable<?, ?> that = (Arrangeable<?, ?>) o;
        return value.equals(that.value) &&
            key.equals(that.key);
    }

    @Override
    public int hashCode() {
        return key.hashCode() + value.hashCode();
    }
}
