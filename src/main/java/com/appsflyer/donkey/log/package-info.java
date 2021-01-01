/*
 * Copyright 2020 AppsFlyer
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

/**
 * This package is mostly copied from Vert.x 3.X.X logging implementation,
 * which has since been deprecated in version 4.0.0.
 * Vert.x used it as a public logging facility, whereas we use it internally
 * to support debug mode logging without the need for users to use a
 * configuration file.
 */
package com.appsflyer.donkey.log;

