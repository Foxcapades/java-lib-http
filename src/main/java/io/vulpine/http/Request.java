package io.vulpine.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

@SuppressWarnings( "unused" )
public class Request
{
  private static final String USER_AGENT = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/49.0.2623.87 Safari/537.36";
  private static final String CONNECTION = "keep-alive";

  protected Method method = Method.GET;

  protected String requestBody = "";
  protected final Map < String, String >                 headers;
  protected final Set < BiConsumer < String, Request > > errorHandlers;

  protected URL url;
  protected boolean sent           = false;
  private   boolean streamResponse = false;
  protected boolean followRedirects = false;

  public Request()
  {
    headers = new HashMap <>();
    errorHandlers = new HashSet <>();
  }

  /**
   * Set Header
   * ===========================================================================
   * <p>
   * Adds a new header entry, if the provided key is already a set header, the
   * new value will be overwrite the current header.
   *
   * @param key   Header
   * @param value header value
   *
   * @return This request object
   *
   * @chainable
   */
  public Request setHeader( String key, String value )
  {
    headers.put(key, value);
    return this;
  }

  public Request allowRedirects()
  {
    this.followRedirects = true;
    return this;
  }

  public Request disallowRedirects()
  {
    followRedirects = false;
    return this;
  }

  /**
   * Add Header
   * ===========================================================================
   * <p>
   * Adds a new header entry, if the provided key is already a set header, the
   * new value will be appended to the current header.
   *
   * @param key   Header name
   * @param value Header value
   *
   * @return This Request object
   *
   * @chainable
   */
  public Request addHeader( String key, String value )
  {
    if (headers.containsKey(key))
      headers.put(key, headers.get(key) + "; " + value);

    headers.put(key, value);

    return this;
  }

  /**
   * Set Request Body
   * ===========================================================================
   *
   * @param body Request body
   *
   * @return This request object
   *
   * @chainable
   */
  public Request requestBody( String body )
  {
    requestBody = body;
    return this;
  }

  public Request get()
  {
    method = Method.GET;
    return this;
  }

  public Request post()
  {
    method = Method.POST;
    return this;
  }

  public Request put()
  {
    method = Method.PUT;
    return this;
  }

  public Request delete()
  {
    method = Method.DELETE;
    return this;
  }

  public Request url( String url ) throws MalformedURLException
  {
    this.url = new URL(url);
    return this;
  }

  public Request streamResponse()
  {
    this.streamResponse = true;
    return this;
  }

  public Request addErrorHandler( BiConsumer < String, Request > handler )
  {
    this.errorHandlers.add(handler);
    return this;
  }

  public Response submit() throws IOException
  {
    final HttpURLConnection con;
    final int resp;
    final BufferedReader    read;
    final StringBuffer      body;
    String                  line;

    assert !sent : "Cannot reuse Request instances.";

    sent = true;

    con = (HttpURLConnection) url.openConnection();
    con.setRequestMethod(method.name());
    //con.setInstanceFollowRedirects(followRedirects);

    headers.forEach(con::setRequestProperty);

    if (!requestBody.isEmpty()) {
      final OutputStreamWriter writer;
      con.setDoOutput(true);
      con.setFixedLengthStreamingMode(requestBody.length());
      writer = new OutputStreamWriter(con.getOutputStream(), Charset.forName("UTF-8"));
      writer.write(requestBody);
      writer.flush();
      writer.close();
    }

    resp = con.getResponseCode();

    if (followRedirects && resp >= 300 && resp < 400) {
      sent   = false;
      url    = new URL(con.getHeaderField("Location"));
      method = Method.GET;
      requestBody = "";
      return submit();
    }

    if (streamResponse) {
      return new Response(Response.Code.byCode(con.getResponseCode()), con.getHeaderFields(), con.getInputStream(), this);
    }

    try {
      read = new BufferedReader(new InputStreamReader(con.getInputStream()));
    } catch (final IOException e) {

      if (errorHandlers.size() > 0) {
        final BufferedReader err;
        body = new StringBuffer();
        err = new BufferedReader(new InputStreamReader(con.getErrorStream()));
        while (null != (line = err.readLine())) body.append(line);
        errorHandlers.forEach(h -> h.accept(body.toString(), this));
        return null;
      }

      throw e;

    }
    body = new StringBuffer();

    while (null != (line = read.readLine())) body.append(line);

    read.close();

    return new Response(Response.Code.byCode(con.getResponseCode()), con.getHeaderFields(), body.toString(), this);
  }

  public enum Method
  {
    GET, POST, PUT, DELETE, HEAD, INFO
  }
}
