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

OLD_PROJECT_VERSION=$(mvn org.apache.maven.plugins:maven-help-plugin:evaluate -Dexpression=project.version -B | grep -v '\[')
RELEASE_VERSION=$(echo "$OLD_PROJECT_VERSION" | sed -e "s/-SNAPSHOT$//")
TAG="v$RELEASE_VERSION"
DEV_VERSION="$1"-SNAPSHOT
DEV_BRANCH="v$1"

replace_version() {
  find . -name project.clj -exec \
    sed -i '' "s/defproject com.appsflyer\/donkey \".*\"/defproject com.appsflyer\/donkey \"$1\"/g" '{}' \;

  find . -name README.md -exec \
    sed -i '' "s/\[com.appsflyer\/donkey \".*\"\]/[com.appsflyer\/donkey \"$1\"\]/g" '{}' \;

  find . -name README.md -exec \
    sed -i '' "s/com.appsflyer\/donkey {:mvn\/version \".*\"}/com.appsflyer\/donkey {:mvn\/version \"$1\"}/g" '{}' \;

  find . -type f \( -name pom.xml -or -name README.md \) -exec \
    sed -i '' \
    -e "1s/<version>.*<\/version>/<version>$1<\/version>/;t" \
    -e "1,/<version>.*<\/version>/s//<version>$1<\/version>/" '{}' \;
}

ask_do_push() {
  while true; do
    read -rp 'Push changes? (y/n): ' val
    case $val in
    [Yy]) return 0 ;;
    [Nn]) return 1 ;;
    *) echo 'Please select "y" or "n"' ;;
    esac
  done
}

exit_on_error() {
  if [ $? != 0 ]; then
    if [ "$1" != "" ]; then
      echo "$1"
    fi
    exit 1
  fi
}

echo "updating release version from '$OLD_PROJECT_VERSION' to '$RELEASE_VERSION' ..."

replace_version "$RELEASE_VERSION"
exit_on_error "version search and replace failed"

echo "committing changes and creating tag '$TAG' ..."

git commit -am "Release version $RELEASE_VERSION"
exit_on_error "commit failed"

git tag -a "$TAG" -m "Release $TAG"
exit_on_error "tag creation failed"

ask_do_push

if [ $? = 0 ]; then
  echo 'git push origin '"$TAG"
  exit_on_error "push failed"
fi

while true; do
  read -rp 'Deploy to https://clojars.org? (y/n): ' do_deploy
  case $do_deploy in
  [Yy])
    echo 'mvn deploy'
    exit_on_error "deploy failed"
    break
    ;;
  [Nn]) break ;;
  *) echo 'Please select "y" or "n"' ;;
  esac
done

while true; do
  read -rp "Create new development branch '$DEV_BRANCH' ? (y/n): " create_dev_branch
  case $create_dev_branch in
  [Yy]) break ;;
  [Nn]) exit 0 ;;
  *) echo 'Please select "y" or "n"' ;;
  esac
done

echo "creating new development branch '$DEV_BRANCH' with version '$DEV_VERSION' ..."

git checkout -b "$DEV_BRANCH"
exit_on_error "new dev branch creation failed"

replace_version "$DEV_VERSION"
exit_on_error "version search and replace failed"

echo 'committing changes ...'

git commit -am "[skip travis] Preparing next development iteration version $DEV_VERSION"
exit_on_error "commit failed"

ask_do_push

if [ $? = 0 ]; then
  echo 'git push'
  exit_on_error "push failed"
fi

echo "version bump succeeded"
