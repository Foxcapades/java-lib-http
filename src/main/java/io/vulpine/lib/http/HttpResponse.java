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

public class HttpResponse
{
  private final HttpResponseType type;

  private final HttpRequest request;

  private final String body;

  public HttpResponse(HttpResponseType type, HttpRequest request, String body) {
    this.type = type;
    this.request = request;
    this.body = body;
  }

  public HttpResponseType getType() {
    return type;
  }

  public HttpRequest getRequest() {
    return request;
  }

  public String getBody() {
    return body;
  }
}
