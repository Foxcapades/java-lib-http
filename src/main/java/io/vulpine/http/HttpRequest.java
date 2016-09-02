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
package io.vulpine.http;

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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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

  public HttpRequest( final Method method, final URL url )
  {
    this.logger = LoggerManager.getLogger("lib-http");
    this.method = method;
    this.url = url;
    this.headers = new HashMap < String, List < String > >();
    this.requestBody = new StringBuilder();
    this.errorHandlers = new LinkedList < HttpErrorHandler >();

    this.logger.trace(HttpRequest.class, method, url);
  }

  public HttpRequest( final Method method, final String url ) throws MalformedURLException
  {
    this(method, new URL(url));
  }

  public StringBuilder getRequestBody()
  {
    this.logger.trace(HttpRequest.class);
    return this.requestBody;
  }

  public Method getMethod()
  {
    this.logger.trace(HttpRequest.class);
    return this.method;
  }

  public HttpRequest addHeader( final String key, final String value )
  {
    this.logger.trace(HttpRequest.class, key, value);

    if (!this.headers.containsKey(key)) {
      return this.setHeader(key, value);
    }

    this.headers.get(key).add(value);

    return this;
  }

  public HttpRequest addHeader( final String key, final String... values )
  {
    final List < String > list;

    this.logger.trace(HttpRequest.class, key, values);

    if (!this.headers.containsKey(key)) {
      return this.setHeader(key, values);
    }

    list = this.headers.get(key);

    for ( final String value : values ) {
      list.add(value);
    }

    return this;
  }

  public HttpRequest addErrorHandler( final HttpErrorHandler handler )
  {
    this.errorHandlers.add(handler);

    return this;
  }

  public HttpRequest appendToBody( final Object o )
  {
    this.requestBody.append(o);

    return this;
  }

  public HttpRequest followRedirects()
  {
    this.logger.trace(HttpRequest.class);
    this.followRedirects = true;

    return this;
  }

  public HttpRequest setConnectTimeout( final int timeout )
  {
    this.logger.trace(HttpRequest.class, timeout);
    this.connectTimeout = timeout;

    return this;
  }

  public HttpRequest setHeader( final String key, final String value )
  {
    final List < String > list;

    this.logger.trace(HttpRequest.class, key, value);

    list = new LinkedList < String >();

    list.add(value);
    this.headers.put(key, list);

    return this;
  }

  public HttpRequest setHeader( final String key, final String... values )
  {
    final List < String > list;

    this.logger.trace(HttpRequest.class, key, values);

    list = new LinkedList < String >();

    for ( final String value : values ) {
      list.add(value);
    }

    this.headers.put(key, list);

    return this;
  }

  public HttpRequest setReadTimeout( final int timeout )
  {
    this.logger.trace(HttpRequest.class, timeout);
    this.readTimeout = timeout;

    return this;
  }

  public HttpURLConnection getConnection()
  {
    return this.connection;
  }

  public HttpResponse send()
  {
    final HttpURLConnection connection;
    final int               responseCode;

    this.logger.trace(HttpRequest.class);

    if (this.hasSent) {
      throw new RuntimeException("Cannot resend HttpRequests");
    }

    this.hasSent = true;

    try {

      final BufferedReader reader;
      final StringBuilder  output;

      connection = (HttpURLConnection) url.openConnection();

      this.connection = connection;

      prepConnection(connection);

      if (0 < this.requestBody.length()) {
        final OutputStreamWriter writer = new OutputStreamWriter(
          connection.getOutputStream(),
          Charset.forName("UTF-8")
        );

        writer.write(requestBody.toString());
        writer.flush();
        writer.close();
      }

      responseCode = connection.getResponseCode();

      reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
      output = new StringBuilder();

      {
        String line = reader.readLine();
        while (null != line) {
          output.append(line);
          line = reader.readLine();
        }
      }

      reader.close();

      return new HttpResponse(HttpResponseType.byCode(responseCode), this, output.toString());

    } catch ( IOException e ) {

      this.logger.error("HttpRequest failed:" + e.getMessage());

      for ( final HttpErrorHandler handler : this.errorHandlers ) {
        this.logger.trace("Running error handler: {}", handler.getClass());
        handler.handle(this, e);
      }

    }

    return null;
  }

  private void prepConnection( final HttpURLConnection connection ) throws IOException
  {
    this.logger.trace(HttpRequest.class, connection);

    connection.setRequestMethod(this.method.toString());
    connection.setDoOutput(0 < this.requestBody.length());
    connection.setInstanceFollowRedirects(this.followRedirects);

    if (0 < this.connectTimeout) {
      connection.setConnectTimeout(this.connectTimeout);
    }

    if (0 < this.readTimeout) {
      connection.setReadTimeout(this.readTimeout);
    }


    this.applyHeaders(connection);
  }

  private void applyHeaders( final HttpURLConnection connection )
  {
    this.logger.trace(HttpRequest.class, connection);

    for ( final Map.Entry < String, List < String > > header : this.headers.entrySet() ) {

      final String          key       = header.getKey();
      final List < String > subValues = header.getValue();
      final StringBuilder   value     = new StringBuilder();
      final int             size      = subValues.size();

      for ( int i = 0; i < size; i++ ) {
        if (0 < i) {
          value.append("; ");
        }

        value.append(subValues.get(i));
      }

      connection.setRequestProperty(key, value.toString());
    }
  }

  public static HttpRequest delete( final URL url )
  {
    return new HttpRequest(Method.DELETE, url);
  }

  public static HttpRequest delete( final String url ) throws MalformedURLException
  {
    return delete(new URL(url));
  }

  public static HttpRequest get( final URL url )
  {
    return new HttpRequest(Method.GET, url);
  }

  public static HttpRequest get( final String url ) throws MalformedURLException
  {
    return get(new URL(url));
  }

  public static HttpRequest post( final URL url )
  {
    return new HttpRequest(Method.POST, url);
  }

  public static HttpRequest post( final String url ) throws MalformedURLException
  {
    return post(new URL(url));
  }

  public static HttpRequest put( final URL url )
  {
    return new HttpRequest(Method.PUT, url);
  }

  public static HttpRequest put( final String url ) throws MalformedURLException
  {
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
