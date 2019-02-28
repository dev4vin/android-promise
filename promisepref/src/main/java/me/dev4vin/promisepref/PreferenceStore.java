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

package me.dev4vin.promisepref;

import android.os.Build;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import me.dev4vin.promisemodel.Extras;
import me.dev4vin.promisemodel.List;
import me.dev4vin.promisemodel.ResponseCallBack;
import me.dev4vin.promisemodel.Store;
import me.dev4vin.promisemodel.function.Converter;
import me.dev4vin.promisemodel.function.FilterFunction;
import me.dev4vin.promisemodel.function.MapFunction;


/**
 * Created on 7/17/18 by yoctopus.
 */
public abstract class PreferenceStore<T> implements Store<T, String, Throwable> {
 private Preferences preferences;
 private Converter<T, JSONObject, JSONObject> converter;

 public PreferenceStore(String name, Converter<T, JSONObject, JSONObject> converter) {
  this.preferences = new Preferences(name);
  this.converter = converter;
 }

 public abstract FilterFunction<JSONObject> findIndexFunction(T t);

 @Override
 public void get(String s, ResponseCallBack<Extras<T>, Throwable> callBack) {
  try {
   String k = preferences.getString(s);
   JSONArray array = new JSONArray(k);
   if (array.length() == 0) callBack.error(new Throwable("Not available"));
   List<JSONObject> objects = new List<>();
   for (int i = 0; i < array.length(); i++) objects.add(array.getJSONObject(i));
   new StoreExtra<T, Throwable>() {
    @Override
    public <Y> List<T> filter(List<T> list, Y... y) {
     return list;
    }
   }.getExtras(
     objects.map(
       new MapFunction<T, JSONObject>() {
        @Override
        public T from(JSONObject jsonObject) {
         return converter.from(jsonObject);
        }
       }),
     callBack);
  } catch (JSONException e) {
   callBack.error(e);
  }
 }

 @Override
 public void delete(String s, T t, ResponseCallBack<Boolean, Throwable> callBack) {
  try {
   JSONArray array = new JSONArray(preferences.getString(s));
  } catch (JSONException e) {
   callBack.error(e);
  }
 }

 @Override
 public void update(String s, T t, ResponseCallBack<Boolean, Throwable> callBack) {
  try {
   JSONArray array = new JSONArray(preferences.getString(s));
   array.put(converter.get(t));
   preferences.save(s, array.toString());
  } catch (JSONException e) {
   callBack.error(e);
  }
 }

 @Override
 public void save(String s, final T t, ResponseCallBack<Boolean, Throwable> callBack) {
  try {
   JSONArray array = new JSONArray(preferences.getString(s));
   List<JSONObject> objects = new List<>();
   for (int i = 0; i < array.length(); i++) objects.add(array.getJSONObject(i));
   int index =
     objects.findIndex(
       new FilterFunction<JSONObject>() {
        @Override
        public boolean filter(JSONObject jsonObject) {
         return findIndexFunction(t).filter(jsonObject);
        }
       });
   if (index != -1) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
     array.remove(index);
     array.put(converter.get(t));
    } else {
     array = new JSONArray();
     objects.add(converter.get(t));
     for (JSONObject object : objects) array.put(object);
    }
   } else array.put(converter.get(t));
   preferences.save(s, array.toString());
   callBack.response(true);
  } catch (JSONException e) {
   JSONArray jsonArray = new JSONArray();
   jsonArray.put(converter.get(t));
   preferences.save(s, jsonArray.toString());
   callBack.response(true);
  }
 }

 @Override
 public void clear(String s, ResponseCallBack<Boolean, Throwable> callBack) {
  preferences.clear(s);
  callBack.response(true);
 }

 @Override
 public void clear(ResponseCallBack<Boolean, Throwable> callBack) {
  preferences.clearAll();
  callBack.response(true);
 }
}
