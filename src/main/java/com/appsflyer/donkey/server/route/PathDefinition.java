/*
 * Copyright 2020 AppsFlyer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.appsflyer.donkey.server.route;

/**
 * Represents a path on a {@link io.vertx.ext.web.Route}.
 * A path has a value, e.g {@code /} or {@code /foo/bar},
 * and a match type used by a {@link io.vertx.ext.web.Router} to determine
 * if a request matches the route's path.
 * <p>
 * The match type can be either {@code SIMPLE} or {@code REGEX}.
 * <p>
 * In {@code SIMPLE} case, the path value will be treated
 * either as an exact match, or having path variables, e.g {@code /user/:id},
 * where {@code :id} is a path variable which value will be available in the
 * request.
 * <p>
 * In {@code REGEX} match, the path value will be treated as a regular
 * expression, e.g {@code /user/[0-9]+} in which case the path will match only
 * if the part after {@code /user/} includes one or more numbers.
 */
public final class PathDefinition {
  
  public static PathDefinition create(String value) {
    return new PathDefinition(value);
  }
  
  public static PathDefinition create(String value, MatchType matchType) {
    return new PathDefinition(value, matchType);
  }
  
  private final String value;
  private final MatchType matchType;
  
  private PathDefinition(String value, MatchType matchType) {
    this.value = value;
    this.matchType = matchType;
  }
  
  private PathDefinition(String value) {
    this(value, MatchType.SIMPLE);
  }
  
  public String value() {
    return value;
  }
  
  @SuppressWarnings("WeakerAccess")
  public MatchType matchType() {
    return matchType;
  }
  
  
  public enum MatchType {
    REGEX, SIMPLE
  }
}
