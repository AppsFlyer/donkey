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

PROJECT_VERSION=$(mvn org.apache.maven.plugins:maven-help-plugin:evaluate -Dexpression=project.version -B | grep -v '\[')
if [[ "$PROJECT_VERSION" =~ .*SNAPSHOT ]] && [[ "${BRANCH}" =~ ^master$|^[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
  mvn deploy -s .ci.maven.settings.xml -DskipTests -B
else
  echo Skipping snapshot deployment for project version "$PROJECT_VERSION" on branch "${BRANCH}"
fi
