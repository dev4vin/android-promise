/*
 *
 *  * Copyright 2017, Peter Vincent
 *  * Licensed under the Apache License, Version 2.0, Promise.
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  * http://www.apache.org/licenses/LICENSE-2.0
 *  * Unless required by applicable law or agreed to in writing,
 *  * software distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package me.yoctopus.data.net.extras;

import org.json.JSONException;

import java.util.Map;

import me.yoctopus.data.net.CallAdapter;
import me.yoctopus.data.utils.Converter;

/**
 * Created on 6/16/18 by yoctopus.
 */
public class DataRequest<T, DOWNSTREAM> {

    private HttpResponse<?, DOWNSTREAM> response;
    private CallAdapter<T, DOWNSTREAM, ?> callAdapter;

    public HttpResponse<?, DOWNSTREAM> getResponse() {
        return response;
    }

    public DataRequest(HttpResponse<?, DOWNSTREAM> response, CallAdapter<T, DOWNSTREAM, ?> callAdapter) {
        this.response = response;
        this.callAdapter = callAdapter;
    }

    public T response() {
        return callAdapter.parse(response);
    }
}
