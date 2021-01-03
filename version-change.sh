#!/bin/sh

#
# Copyright 2020 AppsFlyer
#
# Licensed under the Apache License, Version 2.0 (the "License")
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
#

helpFunction() {
  echo "Change the references of the current project version with another"
  echo "Usage: $0 <version>"
  exit 1 # Exit script after printing help
}

replace_version() {
  find . -name project.clj -exec \
    sed -i '' "s/defproject com.appsflyer\/donkey \".*\"/defproject com.appsflyer\/donkey \"$1\"/g" '{}' \;

  find . -name README.md -exec \
    sed -i '' "s/\[com.appsflyer\/donkey \".*\"/[com.appsflyer\/donkey \"$1\"/g" '{}' \;

  find . -name README.md -exec \
    sed -i '' "s/com.appsflyer\/donkey {:mvn\/version \".*\"/com.appsflyer\/donkey {:mvn\/version \"$1\"/g" '{}' \;

  find . -type f \( -name pom.xml -or -name README.md \) -exec \
    sed -i '' \
    -e "1s/<version>.*<\/version>/<version>$1<\/version>/;t" \
    -e "1,/<version>.*<\/version>/s//<version>$1<\/version>/" '{}' \;
}

if [ "$1" = '-h' ] || [ "$1" = '--help' ] || [ "$1" = "" ]; then
  helpFunction
fi

replace_version "$1"
