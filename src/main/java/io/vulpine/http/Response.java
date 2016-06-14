package io.vulpine.http;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

@SuppressWarnings( "unused" )
public class Response
{
  protected final Code                            responseCode;
  protected final Map < String, List < String > > headers;
  protected final String                          body;
  protected final Request                         request;
  protected final InputStream                     stream;

  public Response( Code responseCode, Map < String, List < String > > headers, String body, Request request )
  {
    this.responseCode = responseCode;
    this.headers = headers;
    this.request = request;
    this.body = body;
    this.stream = null;
  }

  public Response( Code responseCode, Map < String, List < String > > headers, InputStream body, Request request )
  {
    this.responseCode = responseCode;
    this.headers = headers;
    this.request = request;
    this.body = null;
    this.stream = body;
  }

  public Code getResponseCode()
  {
    return responseCode;
  }

  public Map < String, List < String > > getHeaders()
  {
    return headers;
  }

  public String getBody()
  {
    return body;
  }

  public Request getRequest()
  {
    return request;
  }

  public InputStream getInput()
  {
    return stream;
  }

  @SuppressWarnings( "unused" )
  public enum Code
  {
    IN_RANGE_1XX(-100, "In 100 range"),
    IN_RANGE_2XX(-200, "In 200 range"),
    IN_RANGE_3XX(-300, "In 300 range"),
    IN_RANGE_4XX(-400, "In 400 range"),
    IN_RANGE_5XX(-500, "In 500 range"),

    CONTINUE(100, "Continue"),
    SWITCHING_PROTOCOLS(101, "Switching Protocols"),
    PROCESSING(102, "Processing"),
    OK(200, "Ok"),
    CREATED(201, "Created"),
    ACCEPTED(202, "Accepted"),
    NON_AUTHORITATIVE_INFORMATION(203, "Non-Authoritative Information"),
    NO_CONTENT(204, "No Content"),
    RESET_CONTENT(205, "Reset Content"),
    PARTIAL_CONTENT(206, "Partial Content"),
    MULTI_STATUS(207, "Multi Status"),
    ALREADY_REPORTED(208, "Already Reported"),
    IM_USED(226, "IM Used"),
    MULTIPLE_CHOICES(300, "Multiple Choices"),
    MOVED_PERMANENTLY(301, "Moved Permanently"),
    FOUND(302, "Found"),
    SEE_OTHER(303, "See Other"),
    NOT_MODIFIED(304, "Not Modified"),
    USE_PROXY(305, "Use Proxy"),
    TEMPORARY_REDIRECT(307, "Temporary Redirect"),
    PERMANENT_REDIRECT(308, "Permanent Redirect"),
    BAD_REQUEST(400, "Bad Request"),
    UNAUTHORIZED(401, "Unauthorized"),
    PAYMENT_REQUIRED(402, "Payment Required"),
    FORBIDDEN(403, "Forbidden"),
    NOT_FOUND(404, "Not Found"),
    METHOD_NOT_ALLOWED(405, "Method Not Allowed"),
    NOT_ACCEPTABLE(406, "Not Acceptable"),
    PROXY_AUTHENTICATION_REQUIRED(407, "Proxy Authentication Required"),
    REQUEST_TIMEOUT(408, "Request Timeout"),
    CONFLICT(409, "Conflict"),
    GONE(410, "Gone"),
    LENGTH_REQUIRED(411, "Length Required"),
    PRECONDITION_FAILED(412, "Precondition Failed"),
    REQUEST_ENTITY_TOO_LARGE(413, "Request Entity Too Large"),
    REQUEST_URI_TOO_LONG(414, "Request URI Too Long"),
    UNSUPPORTED_MEDIA_TYPE(415, "Unsupported Media Type"),
    REQUESTED_RANGE_NOT_SATISFIABLE(416, "Requested Range Not Satisfiable"),
    EXPECTATION_FAILED(417, "Expectation Failed"),
    IM_A_TEAPOT(418, "I'm A Teapot"),
    ENHANCE_YOUR_CALM(420, "Enhance Your Calm"),
    UNPROCESSABLE_ENTITY(422, "Unprocessable Entity"),
    LOCKED(423, "Locked"),
    FAILED_DEPENDENCY(424, "Failed Dependency"),
    RESERVED_FOR_WEBDEV(425, "Reserved for WebDev"),
    UPGRADE_REQUIRED(426, "Upgrade Required"),
    PRECONDITION_REQUIRED(428, "Precondition Required"),
    TOO_MANY_REQUESTS(429, "Too Many Requests"),
    REQUEST_HEADER_FIELDS_TOO_LARGE(431, "Request Header Fields Too Large"),
    NO_RESPONSE(444, "No Response"),
    RETRY_WITH(449, "Retry With"),
    BLOCKED_BY_PARENTAL_CONTROLS(450, "Blocked By Parental Controls"),
    UNAVAILABLE_FOR_LEGAL_REASONS(451, "Unavailable for Legal Reasons"),
    CLIENT_CLOSED_REQUEST(499, "Client Closed Request"),
    INTERNAL_SERVER_ERROR(500, "Internal Server Error"),
    NOT_IMPLEMENTED(501, "Not Implemented"),
    BAD_GATEWAY(502, "Bad Gateway"),
    SERVICE_UNAVAILABLE(503, "Service Unavailable"),
    GATEWAY_TIMEOUT(504, "Gateway Timeout"),
    HTTP_VERSION_NOT_SUPPORTED(505, "HTTP Version Not Supported"),
    VARIANT_ALSO_NEGOTIATES(506, "Variant Also Negotiates"),
    INSUFFICIENT_STORAGE(507, "Insufficient Storage"),
    LOOP_DETECTED(508, "Loop Detected"),
    BANDWIDTH_LIMIT_EXCEEDED(509, "Bandwidth Limit Exceeded"),
    NOT_EXTENDED(510, "Not Extended"),
    NETWORK_AUTHENTICATION_REQUIRED(511, "Network Authentication Required"),
    NETWORK_READ_TIMEOUT_ERROR(598, "Network Read Timeout Error"),
    NETWORK_CONNECTION_TIMEOUT_ERROR(599, "Network Connection Timeout Error");

    private final int    code;
    private final String name;

    Code( int code, String name )
    {
      this.code = code;
      this.name = name;
    }

    public int getCode()
    {
      return code;
    }

    public String getName()
    {
      return name;
    }

    public static Code byCode( int code )
    {
      for (Code c : Code.values()) if (c.code == code) return c;
      return null;
    }
  }
}
