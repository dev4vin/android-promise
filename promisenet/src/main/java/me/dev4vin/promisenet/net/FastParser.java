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

package me.dev4vin.promisenet.net;

import android.content.Intent;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLHandshakeException;

import androidx.annotation.NonNull;
import androidx.collection.ArrayMap;
import me.dev4vin.LogUtil;
import me.dev4vin.Promise;
import me.dev4vin.promisemodel.Action;
import me.dev4vin.promisemodel.Message;
import me.dev4vin.promisemodel.ResponseCallBack;
import me.dev4vin.promisenet.net.extras.HttpPayload;
import me.dev4vin.promisenet.net.extras.HttpResponse;
import me.dev4vin.promisenet.net.extras.InputStreamHttpResponse;
import me.dev4vin.promisenet.net.extras.JsonObjectHttpResponse;
import me.dev4vin.promiseui.NetworkErrorActivity;
import me.dev4vin.utils.Conditions;
import me.dev4vin.utils.NetworkUtil;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/** */
public class FastParser {
  public static final String SENDER = "fastParser";
  public static final String NETWORK_IS_BACK = "Network is back";
  private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
  private Config config;
  private OkHttpClient client;
  private String TAG = LogUtil.makeTag(FastParser.class);
  private Interceptor<HttpPayload> payloadInterceptor;
  private Interceptor<HttpResponse<?, ?>> responseInterceptor;

  public FastParser(final Config config) {https://somelink.com/api

    this.config = Conditions.checkNotNull(config);
    OkHttpClient.Builder builder =
        new OkHttpClient.Builder()
            .writeTimeout(this.config.timeOut(), TimeUnit.MILLISECONDS)
            .readTimeout(this.config.timeOut(), TimeUnit.MILLISECONDS)
            .connectTimeout(this.config.timeOut(), TimeUnit.MILLISECONDS);
    if (config.retry() > 0)
      builder.addInterceptor(
          new okhttp3.Interceptor() {
            @Override
            public Response intercept(@NonNull Chain chain) throws IOException {
              LogUtil.e(TAG, "Request Request timeout - " + config.timeOut() + " read", chain.readTimeoutMillis() + " write", chain.writeTimeoutMillis());
              Request request = chain.request();
              Response response = null;
              boolean responseOK = false;
              int tryCount = 0;
              while (!responseOK && tryCount < config.retry())
                try {
                  response = chain.proceed(request);
                  responseOK = response.isSuccessful();
                } catch (Exception e) {
                  if (e instanceof SSLHandshakeException) {
                    Promise.instance().send(new Message(SENDER, e));
                  }
                  LogUtil.e(TAG, "Request is not successful - " + tryCount, e);
                } finally {
                  tryCount++;
                }
              if (response == null)
                throw new IOException("Problem completing after retrying request");
              return response;
            }
          });
    client = builder.build();
       /* Promise.instance()
                .listen(
                        Promise.TAG,
                        new ResponseCallBack<>()
                                .response(
                                        new ResponseCallBack.Response<Object, Throwable>() {
                                            @Override
                                            public void onResponse(Object o) {
                                                if (o instanceof String && o.equals("Network shut down"))
                                                    if (client != null && client.dispatcher() != null)
                                                        client.dispatcher().cancelAll();
                                                if (o instanceof String && o.equals(NETWORK_IS_BACK))
                                                    Promise.instance().send(new Message(SENDER, NETWORK_IS_BACK));
                                            }
                                        }));*/
  }

  public static String toUrlParams(JSONObject data) {
    data = Conditions.checkNotNull(data);
    Iterator<String> iter = data.keys();
    StringBuilder params = new StringBuilder();
    while (iter.hasNext()) {
      String key = iter.next();
      data.opt(key);
      params.append(key).append("=").append(data.opt(key)).append("&");
    }
    return params.toString();
  }

  private static String toUrlParams(Map<String, Object> data) {
    data = Conditions.checkNotNull(data);
    StringBuilder params = new StringBuilder();
    for (Map.Entry<String, Object> entry : data.entrySet()) {
      String key = entry.getKey();
      Object object;
      if (entry.getValue() == null) object = "";
      else object = entry.getValue();
      params.append(key).append("=").append(object.toString()).append("&");
    }
    return params.toString();
  }

  public static FastParser with(Config config) {
    return new FastParser(config);
  }

  public void download(
      @NonNull final EndPoint endPoint,
      @NonNull final HttpPayload payload,
      final File file,
      final ResponseCallBack<HttpResponse<InputStream, File>, Exception> responseCallBack) {

    HttpUrl.Builder httpBuider = HttpUrl.parse(endPoint.toString()).newBuilder();
    if (payload.payload() != null)
      for (Map.Entry<String, Object> param : payload.payload().entrySet())
        httpBuider.addQueryParameter(param.getKey(), String.valueOf(param.getValue()));
    Request.Builder builder = new Request.Builder();
    for (Map.Entry<String, String> entry : payload.headers().entrySet())
      builder.addHeader(entry.getKey(), entry.getValue());
    Request request = builder.url(httpBuider.build()).get().build();
    makeRequest(request, file, responseCallBack);
  }

  private void makeRequest(
      final Request request,
      final File file,
      @NonNull final ResponseCallBack<HttpResponse<InputStream, File>, Exception> responseCallBack) {
    Promise.instance()
        .execute(
            new Action<HttpResponse<InputStream, File>>() {
              @Override
              public HttpResponse<InputStream, File> execute() throws Exception {
                Response response = client.newCall(request).execute();
                ArrayMap<String, String> headers = new ArrayMap<>();
                for (String name : response.headers().names())
                  headers.put(name, response.header(name));
                HttpResponse<InputStream, File> response1 = new InputStreamHttpResponse(file);
                response1.getResponse(response.body().byteStream());
                response1.status(response.code());
                response1.headers(headers);
                return response1;
              }
            },
            new ResponseCallBack<HttpResponse<InputStream, File>, Throwable>()
                .response(
                    new ResponseCallBack.Response<HttpResponse<InputStream, File>, Throwable>() {
                      @Override
                      public void onResponse(HttpResponse<InputStream, File> response) {
                        responseCallBack.response(response);
                      }
                    })
                .error(
                    new ResponseCallBack.Error<Throwable>() {
                      @Override
                      public void onError(Throwable throwable) {
                        if (throwable instanceof JSONException)
                          responseCallBack.error((JSONException) throwable);
                        else
                          Promise.instance().send(new Message(SENDER, throwable));
                      }
                    }));
  }

  public FastParser payloadInterceptor(Interceptor<HttpPayload> payloadInterceptor) {
    this.payloadInterceptor = payloadInterceptor;
    return this;
  }

  public FastParser responseInterceptor(Interceptor<HttpResponse<?, ?>> responseInterceptor) {
    this.responseInterceptor = responseInterceptor;
    return this;
  }

  /**
   * send a post request to the server
   *
   * @param endPoint         route for post
   * @param payload          data to be sent to the serve
   * @param responseCallBack responseCallBack callback
   */
  public void post(
      @NonNull final EndPoint endPoint,
      @NonNull final HttpPayload payload,
      final ResponseCallBack<HttpResponse<String, JSONObject>, JSONException> responseCallBack) {
    if (!checkNetwork()) {
      if (config.sendMessages())
        Promise.instance().send(new Message(SENDER, new SocketTimeoutException()));
      else {
        NetworkErrorActivity.bind(
            new NetworkErrorActivity.Action() {
              @Override
              public void onAction() {
                post(endPoint, payload, responseCallBack);
              }
            });
        startErrorActivity(NetworkErrorActivity.NETWORK_ERROR);
      }
      return;
    }
    if (payloadInterceptor != null) Promise.instance()
        .execute(
            new Runnable() {
              @Override
              public void run() {
                payloadInterceptor.intercept(
                    payload,
                    new ResponseCallBack<HttpPayload, Throwable>()
                        .response(
                            new ResponseCallBack.Response<HttpPayload, Throwable>() {
                              @Override
                              public void onResponse(HttpPayload httpPayload) throws Throwable {
                                makeRequest(getHeaders(httpPayload)
                                    .url(getUrl(endPoint))
                                    .post(getBody(httpPayload))
                                    .build(), responseCallBack);
                              }
                            })
                        .error(
                            new ResponseCallBack.Error<Throwable>() {
                              @Override
                              public void onError(Throwable throwable) {
                                LogUtil.e(TAG, throwable);
                              }
                            }));
              }
            });
    else {
      Request request = getHeaders(payload).url(getUrl(endPoint)).post(getBody(payload)).build();
      makeRequest(request, responseCallBack);
    }
  }

  /**
   * send a get request to the server
   *
   * @param endPoint         route for get request
   * @param payload          headers for the get request
   * @param responseCallBack the responseCallBack to be expected
   */
  public void get(
      @NonNull final EndPoint endPoint,
      @NonNull final HttpPayload payload,
      final ResponseCallBack<HttpResponse<String, JSONObject>, JSONException> responseCallBack) {
    if (!checkNetwork()) {
      if (config.sendMessages())
        Promise.instance().send(new Message(SENDER, new SocketTimeoutException()));
      else {
        NetworkErrorActivity.bind(
            new NetworkErrorActivity.Action() {
              @Override
              public void onAction() {
                post(endPoint, payload, responseCallBack);
              }
            });
        startErrorActivity(NetworkErrorActivity.NETWORK_ERROR);
      }
      return;
    }
    final HttpUrl.Builder httpBuider = HttpUrl.parse(getUrl(endPoint)).newBuilder();
    if (payload.payload() != null)
      for (Map.Entry<String, Object> param : payload.payload().entrySet())
        httpBuider.addQueryParameter(param.getKey(), String.valueOf(param.getValue()));
    if (payloadInterceptor != null) {
      Promise.instance()
          .execute(
              new Runnable() {
                @Override
                public void run() {
                  payloadInterceptor.intercept(
                      payload,
                      new ResponseCallBack<HttpPayload, Throwable>()
                          .response(
                              new ResponseCallBack.Response<HttpPayload, Throwable>() {
                                @Override
                                public void onResponse(HttpPayload httpPayload) throws Throwable {
                                  makeRequest(getHeaders(httpPayload).url(httpBuider.build()).get().build(), responseCallBack);
                                }
                              })
                          .error(
                              new ResponseCallBack.Error<Throwable>() {
                                @Override
                                public void onError(Throwable throwable) {
                                  LogUtil.e(TAG, throwable);
                                }
                              }));
                }
              });

    } else {
      Request request = getHeaders(payload).url(httpBuider.build()).get().build();
      makeRequest(request, responseCallBack);
    }
  }

  public void put(
      @NonNull final EndPoint endPoint,
      @NonNull final HttpPayload payload,
      final ResponseCallBack<HttpResponse<String, JSONObject>, JSONException> responseCallBack) {
    if (!checkNetwork()) {
      if (config.sendMessages())
        Promise.instance().send(new Message(SENDER, new SocketTimeoutException()));
      else {
        NetworkErrorActivity.bind(
            new NetworkErrorActivity.Action() {
              @Override
              public void onAction() {
                post(endPoint, payload, responseCallBack);
              }
            });
        startErrorActivity(NetworkErrorActivity.NETWORK_ERROR);
      }
      return;
    }
    if (payloadInterceptor != null) {
      Promise.instance()
          .execute(
              new Runnable() {
                @Override
                public void run() {
                  payloadInterceptor.intercept(
                      payload,
                      new ResponseCallBack<HttpPayload, Throwable>()
                          .response(
                              new ResponseCallBack.Response<HttpPayload, Throwable>() {
                                @Override
                                public void onResponse(HttpPayload httpPayload) throws Throwable {
                                  makeRequest(getHeaders(httpPayload)
                                      .url(getUrl(endPoint))
                                      .put(getBody(httpPayload))
                                      .build(), responseCallBack);
                                }
                              })
                          .error(
                              new ResponseCallBack.Error<Throwable>() {
                                @Override
                                public void onError(Throwable throwable) {
                                  LogUtil.e(TAG, throwable);
                                }
                              }));
                }
              });

    } else {
      Request request = getHeaders(payload).url(getUrl(endPoint)).put(getBody(payload)).build();
      makeRequest(request, responseCallBack);
    }
  }

  /**
   * send a delete reqeust to the server
   *
   * @param endPoint         route for delete
   * @param payload          headers to be sent
   * @param responseCallBack the callback function
   */
  public void delete(
      @NonNull final EndPoint endPoint,
      @NonNull final HttpPayload payload,
      final ResponseCallBack<HttpResponse<String, JSONObject>, JSONException> responseCallBack) {
    if (!checkNetwork()) {
      if (config.sendMessages())
        Promise.instance().send(new Message(SENDER, new SocketTimeoutException()));
      else {
        NetworkErrorActivity.bind(
            new NetworkErrorActivity.Action() {
              @Override
              public void onAction() {
                post(endPoint, payload, responseCallBack);
              }
            });
        startErrorActivity(NetworkErrorActivity.NETWORK_ERROR);
      }
      return;
    }
    if (payloadInterceptor != null) {
      Promise.instance()
          .execute(
              new Runnable() {
                @Override
                public void run() {
                  payloadInterceptor.intercept(
                      payload,
                      new ResponseCallBack<HttpPayload, Throwable>()
                          .response(
                              new ResponseCallBack.Response<HttpPayload, Throwable>() {
                                @Override
                                public void onResponse(HttpPayload httpPayload) throws Throwable {
                                  makeRequest(getHeaders(httpPayload)
                                      .url(getUrl(endPoint))
                                      .delete(getBody(httpPayload))
                                      .build(), responseCallBack);
                                }
                              })
                          .error(
                              new ResponseCallBack.Error<Throwable>() {
                                @Override
                                public void onError(Throwable throwable) {
                                  LogUtil.e(TAG, throwable);
                                }
                              }));
                }
              });

    } else {
      Request request = getHeaders(payload).url(getUrl(endPoint)).delete(getBody(payload)).build();
      makeRequest(request, responseCallBack);
    }
  }

  /**
   * send patch request to the server
   *
   * @param endPoint         route for patch
   * @param payload          data to be sent for patch
   * @param responseCallBack callback interface
   */
  public void patch(
      @NonNull final EndPoint endPoint,
      @NonNull final HttpPayload payload,
      final ResponseCallBack<HttpResponse<String, JSONObject>, JSONException> responseCallBack) {
    if (!checkNetwork()) {
      if (config.sendMessages())
        Promise.instance().send(new Message(SENDER, new SocketTimeoutException()));
      else {
        NetworkErrorActivity.bind(
            new NetworkErrorActivity.Action() {
              @Override
              public void onAction() {
                post(endPoint, payload, responseCallBack);
              }
            });
        startErrorActivity(NetworkErrorActivity.NETWORK_ERROR);
      }
      return;
    }
    if (payloadInterceptor != null) {
      Promise.instance()
          .execute(
              new Runnable() {
                @Override
                public void run() {
                  payloadInterceptor.intercept(
                      payload,
                      new ResponseCallBack<HttpPayload, Throwable>()
                          .response(
                              new ResponseCallBack.Response<HttpPayload, Throwable>() {
                                @Override
                                public void onResponse(HttpPayload httpPayload) throws Throwable {
                                  makeRequest(getHeaders(httpPayload)
                                      .url(getUrl(endPoint))
                                      .patch(getBody(httpPayload))
                                      .build(), responseCallBack);
                                }
                              })
                          .error(
                              new ResponseCallBack.Error<Throwable>() {
                                @Override
                                public void onError(Throwable throwable) {
                                  LogUtil.e(TAG, throwable);
                                }
                              }));
                }
              });

    } else {
      Request request = getHeaders(payload).url(getUrl(endPoint)).patch(getBody(payload)).build();
      makeRequest(request, responseCallBack);
    }
  }

  /**
   * send subscribe to the request observables
   *
   * @param request          the request to be executed
   * @param responseCallBack callback interface
   */
  private void makeRequest(
      final Request request,
      @NonNull final ResponseCallBack<HttpResponse<String, JSONObject>, JSONException>
          responseCallBack) {
    Promise.instance()
        .execute(
            new Action<JsonObjectHttpResponse>() {
              @Override
              public JsonObjectHttpResponse execute() throws Exception {
                LogUtil.d(TAG, "hitting " + request.toString());
                Response response = request(request);
                ArrayMap<String, String> headers =
                    new ArrayMap<>();
                for (String name : response.headers().names())
                  headers.put(name, response.header(name));
                JsonObjectHttpResponse response1 = new JsonObjectHttpResponse();
                response1.getResponse(response.body().string());
                response1.status(response.code());
                response1.headers(headers);
                LogUtil.d(TAG, "response" + response1.toString());
                return response1;
              }
            },
            new ResponseCallBack<JsonObjectHttpResponse, Throwable>()
                .response(
                    new ResponseCallBack.Response<JsonObjectHttpResponse, Throwable>() {
                      @Override
                      public void onResponse(JsonObjectHttpResponse jsonObjectHttpResponse) {
                        if (responseInterceptor != null) {
                          responseInterceptor.intercept(
                              jsonObjectHttpResponse,
                              new ResponseCallBack<HttpResponse<?, ?>, Throwable>()
                                  .response(
                                      new ResponseCallBack.Response<
                                          HttpResponse<?, ?>, Throwable>() {
                                        @Override
                                        public void onResponse(HttpResponse<?, ?> httpResponse)
                                            throws Throwable {
                                          responseCallBack.response(
                                              (HttpResponse<String, JSONObject>) httpResponse);
                                        }
                                      })
                                  .error(
                                      new ResponseCallBack.Error<Throwable>() {
                                        @Override
                                        public void onError(Throwable throwable) {
                                          LogUtil.e(TAG, throwable);
                                        }
                                      }));
                        } else
                          responseCallBack.response(jsonObjectHttpResponse);
                      }
                    })
                .error(
                    new ResponseCallBack.Error<Throwable>() {
                      @Override
                      public void onError(Throwable error) {
                        if (error instanceof JSONException)
                          responseCallBack.error((JSONException) error);
                        else if (config.sendMessages()) {
                          Promise.instance().send(new Message(SENDER, error));
                        }
                      }
                    }));
  }

  /**
   * get request body, get payload as either parameters or json body
   *
   * @param payload data containing data to be sent
   * @return a request body with data from the payload
   */
  private RequestBody getBody(HttpPayload payload) {
    RequestBody requestBody;
    if (payload.shouldBeParams()) {
      if (payload.files() != null && !payload.files().isEmpty()) {
        MultipartBody.Builder builder = new MultipartBody.Builder();
        for (Map.Entry<String, File> file : payload.files().entrySet()) {
          String contentType;
          try {
            contentType = file.getValue().toURL().openConnection().getContentType();
            RequestBody fileBody =
                RequestBody.create(MediaType.parse(contentType), file.getValue());
            builder.addFormDataPart(file.getKey(), file.getValue().getName(), fileBody);
          } catch (IOException e) {
            Promise.instance().send(new Message(SENDER, e));
            LogUtil.e(TAG, e);
          }
        }
        for (Map.Entry<String, Object> entry : payload.payload().entrySet())
          if (entry.getValue() != null)
            builder.addFormDataPart(entry.getKey(), entry.getValue().toString());
        requestBody = builder.build();
      } else {
        final FormBody.Builder builder = new FormBody.Builder();
        for (Map.Entry<String, Object> entry : payload.payload().entrySet())
          if (entry.getValue() != null)
            builder.addEncoded(entry.getKey(), entry.getValue().toString());
        requestBody = builder.build();
      }
    } else if (payload.shouldBeJson())
      requestBody = RequestBody.create(JSON, new Gson().toJson(payload.payload()));
    else throw new IllegalArgumentException("Payload not passable");
    return requestBody;
  }

  /**
   * get headers from the payload
   *
   * @param payload data containing the headers
   * @return a request builder with the headers
   */
  private Request.Builder getHeaders(HttpPayload payload) {
    Request.Builder builder = new Request.Builder();
    for (Map.Entry<String, String> entry : getHeaders().entrySet())
      builder.addHeader(entry.getKey(), entry.getValue());
    for (Map.Entry<String, String> entry : payload.headers().entrySet())
      builder.addHeader(entry.getKey(), entry.getValue());
    return builder;
  }

  /**
   * make default headers here
   *
   * @return the default headers
   */
  public Map<String, String> getHeaders() {
    return new ArrayMap<>();
  }

  /**
   * call the api
   *
   * @param request the request to be executed
   * @return a response from the serve
   * @throws IOException if the server didnt return a response or if theirs no network connection
   */
  private okhttp3.Response request(Request request) throws IOException {
    Response response = client.newCall(request).execute();
    /*startErrorActivity(NetworkErrorActivity.SERVER_ERROR);*/
    if (response.code() == 500) throw new IOException();
    return response;
  }

  private String getUrl(EndPoint endPoint) {
    return config.getUrl(endPoint.toString());
  }

  private String getUrl(EndPoint endPoint, EndPoint.Type type) {
    endPoint.extension(type.s);
    return getUrl(endPoint);
  }

  private boolean checkNetwork() {
    int status = NetworkUtil.getConnectivityStatus(config.getContext());
    return status != NetworkUtil.TYPE_NOT_CONNECTED;
  }

  private void startErrorActivity(String reason) {
    Intent intent = new Intent(config.getContext(), NetworkErrorActivity.class);
    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    intent.putExtra(NetworkErrorActivity.REASON, reason);
    config.getContext().startActivity(intent);
  }
}
