/*
 * Copyright 2016 Elizabeth Harper
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.vulpine.lib.http;

import io.vulpine.logging.Logger;
import io.vulpine.logging.LoggerManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.*;

public class HttpRequest
{
  private final Method method;

  private final URL url;

  private final Logger logger;

  private final List < HttpErrorHandler > errorHandlers;

  private final Map < String, List < String > > headers;

  private final StringBuilder requestBody;

  private HttpURLConnection connection = null;

  private boolean hasSent = false;

  private int connectTimeout = -1;

  private int readTimeout = -1;

  private boolean followRedirects = false;

  public HttpRequest(Method method, URL url) {
    this.logger = LoggerManager.getLogger("lib-http");
    this.method = method;
    this.url = url;
    this.headers = new HashMap <>();
    this.requestBody = new StringBuilder();
    this.errorHandlers = new LinkedList <>();

    this.logger.trace(HttpRequest.class, method, url);
  }

  public HttpRequest(Method method, String url)
  throws MalformedURLException {
    this(method, new URL(url));
  }

  public StringBuilder getRequestBody() {
    logger.trace(HttpRequest.class);
    return requestBody;
  }

  public Method getMethod() {
    logger.trace(HttpRequest.class);
    return method;
  }

  public HttpRequest addHeader(String key, String value) {
    logger.trace(HttpRequest.class, key, value);

    if (!headers.containsKey(key)) return setHeader(key, value);

    headers.get(key).add(value);

    return this;
  }

  public HttpRequest addHeader(String key, String... values) {
    logger.trace(HttpRequest.class, key, values);

    if (!headers.containsKey(key)) return setHeader(key, values);

    Collections.addAll(headers.get(key), values);

    return this;
  }

  public HttpRequest addErrorHandler(HttpErrorHandler handler) {
    errorHandlers.add(handler);

    return this;
  }

  public HttpRequest appendToBody(Object o) {
    requestBody.append(o);

    return this;
  }

  public HttpRequest followRedirects() {
    logger.trace(HttpRequest.class);
    followRedirects = true;

    return this;
  }

  public HttpRequest setConnectTimeout(int timeout) {
    logger.trace(HttpRequest.class, timeout);
    connectTimeout = timeout;

    return this;
  }

  public HttpRequest setHeader(String key, String value) {
    logger.trace(HttpRequest.class, key, value);

    var list = new LinkedList < String >();

    list.add(value);
    headers.put(key, list);

    return this;
  }

  public HttpRequest setHeader(String key, String... values) {
    logger.trace(HttpRequest.class, key, values);

    var list = new LinkedList < String >();

    Collections.addAll(list, values);

    headers.put(key, list);

    return this;
  }

  public HttpRequest setReadTimeout(int timeout) {
    logger.trace(HttpRequest.class, timeout);
    readTimeout = timeout;

    return this;
  }

  public HttpURLConnection getConnection() {
    return connection;
  }

  public HttpResponse send() {
    int               responseCode;

    logger.trace(HttpRequest.class);

    if (this.hasSent) throw new RuntimeException("Cannot resend HttpRequests");

    this.hasSent = true;

    try {
      connection = (HttpURLConnection) url.openConnection();
      prepConnection(connection);

      if (0 < this.requestBody.length()) {
        OutputStreamWriter writer = new OutputStreamWriter(
          connection.getOutputStream(),
          Charset.forName("UTF-8")
        );

        writer.write(requestBody.toString());
        writer.flush();
        writer.close();
      }

      responseCode = connection.getResponseCode();

      var reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
      var output = new StringBuilder();

      {
        var line = reader.readLine();
        while (null != line) {
          output.append(line);
          line = reader.readLine();
        }
      }

      reader.close();

      return new HttpResponse(HttpResponseType.byCode(responseCode), this, output.toString());

    } catch (IOException e) {

      logger.error("HttpRequest failed:" + e.getMessage());

      for (var handler : errorHandlers) {
        logger.trace("Running error handler: {}", handler.getClass());
        handler.handle(this, e);
      }
    }

    return null;
  }

  private void prepConnection(HttpURLConnection connection) throws IOException {
    logger.trace(HttpRequest.class, connection);

    connection.setRequestMethod(method.toString());
    connection.setDoOutput(0 < requestBody.length());
    connection.setInstanceFollowRedirects(followRedirects);

    if (0 < connectTimeout) {
      connection.setConnectTimeout(connectTimeout);
    }

    if (0 < readTimeout) {
      connection.setReadTimeout(readTimeout);
    }


    applyHeaders(connection);
  }

  private void applyHeaders(HttpURLConnection connection) {
    logger.trace(HttpRequest.class, connection);

    for (var header : headers.entrySet()) {

      var key       = header.getKey();
      var subValues = header.getValue();
      var value     = new StringBuilder();
      var size      = subValues.size();

      for (int i = 0; i < size; i++) {
        if (0 < i) {
          value.append("; ");
        }

        value.append(subValues.get(i));
      }

      connection.setRequestProperty(key, value.toString());
    }
  }

  public static HttpRequest delete(URL url) {
    return new HttpRequest(Method.DELETE, url);
  }

  public static HttpRequest delete(String url) throws MalformedURLException {
    return delete(new URL(url));
  }

  public static HttpRequest get(URL url) {
    return new HttpRequest(Method.GET, url);
  }

  public static HttpRequest get(String url) throws MalformedURLException {
    return get(new URL(url));
  }

  public static HttpRequest post(URL url) {
    return new HttpRequest(Method.POST, url);
  }

  public static HttpRequest post(String url) throws MalformedURLException {
    return post(new URL(url));
  }

  public static HttpRequest put(URL url) {
    return new HttpRequest(Method.PUT, url);
  }

  public static HttpRequest put(String url) throws MalformedURLException {
    return put(new URL(url));
  }

  public enum Method
  {
    CONNECT,
    DELETE,
    GET,
    HEAD,
    OPTIONS,
    POST,
    PUT,
    TRACE
  }
}
