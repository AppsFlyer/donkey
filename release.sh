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
  echo ""
  echo "Usage: $0 [-d,-v version]"
  printf -- '\t-v | --dev-version\t The next dev version. Will prompt to create a new branch\n'
  printf -- '\t-d | --dry-run\t\t Runs the release without committing or pushing any of the changes\n'
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

DRY_RUN=1
DEV_VERSION=

while [ "$1" != "" ]; do
  case $1 in
  -d | --dry-run)
    DRY_RUN=0
    ;;
  -v | --dev-version)
    shift
    DEV_VERSION=$1
    ;;
  -h | --help)
    helpFunction
    ;;
  *) break ;;
  esac
  shift
done

OLD_PROJECT_VERSION=$(mvn org.apache.maven.plugins:maven-help-plugin:evaluate -Dexpression=project.version -B | grep -v '\[')
RELEASE_VERSION=$(echo "$OLD_PROJECT_VERSION" | sed -e "s/-SNAPSHOT$//")
TAG="v$RELEASE_VERSION"

if [ "$DRY_RUN" = 0 ]; then
  echo 'running in dry run mode ...'
fi

echo "updating release version from '$OLD_PROJECT_VERSION' to '$RELEASE_VERSION' ..."

replace_version "$RELEASE_VERSION"
exit_on_error "version search and replace failed"

echo "committing changes and creating tag '$TAG' ..."

if [ "$DRY_RUN" = 0 ]; then
  echo 'git commit -am "Release version '"$RELEASE_VERSION"'"'
else
  git commit -am "Release version $RELEASE_VERSION"
fi

exit_on_error "commit failed"

if [ "$DRY_RUN" = 0 ]; then
  echo 'git tag -a '"$TAG"' -m "Release '"$TAG"'"'
else
  git tag -a "$TAG" -m "Release $TAG"
fi

exit_on_error "tag creation failed"

ask_do_push

if [ $? = 0 ]; then
  if [ "$DRY_RUN" = 0 ]; then
    echo 'git push origin '"$TAG"
  else
    git push origin "$TAG"
  fi
  exit_on_error "push failed"
fi

while true; do
  read -rp 'Deploy to https://clojars.org? (y/n): ' do_deploy
  case $do_deploy in
  [Yy])
    if [ "$DRY_RUN" = 0 ]; then
      echo 'mvn deploy'
    else
      mvn deploy
    fi
    exit_on_error "deploy failed"
    break
    ;;
  [Nn]) break ;;
  *) echo 'Please select "y" or "n"' ;;
  esac
done

if [ -z "$DEV_VERSION" ]; then
  echo 'done'
  exit 0
fi

SNAPSHOT_VERSION="$DEV_VERSION"-SNAPSHOT
DEV_BRANCH="$DEV_VERSION"

while true; do
  read -rp "Create new development branch '$DEV_BRANCH' ? (y/n): " create_dev_branch
  case $create_dev_branch in
  [Yy]) break ;;
  [Nn]) exit 0 ;;
  *) echo 'Please select "y" or "n"' ;;
  esac
done

echo "creating new development branch '$DEV_BRANCH' with version '$SNAPSHOT_VERSION' ..."

if [ "$DRY_RUN" = 0 ]; then
  echo 'git checkout -b '"$DEV_BRANCH"
else
  git checkout -b "$DEV_BRANCH"
fi

exit_on_error "new dev branch creation failed"

echo "updating project to new snapshot version '$SNAPSHOT_VERSION' ..."
replace_version "$SNAPSHOT_VERSION"
exit_on_error "version search and replace failed"

echo 'committing changes ...'

if [ "$DRY_RUN" = 0 ]; then
  echo 'git commit -am "[skip travis] Preparing next development iteration version '"$SNAPSHOT_VERSION"'"'
else
  git commit -am "[skip travis] Preparing next development iteration version $SNAPSHOT_VERSION"
fi
exit_on_error "commit failed"

ask_do_push

if [ $? = 0 ]; then
  if [ "$DRY_RUN" = 0 ]; then
    echo 'git push'
  else
    git push
  fi
  exit_on_error "push failed"
fi

echo "done"
