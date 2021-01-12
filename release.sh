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
  echo "Release library and deploy to clojars"
  echo "Usage: $0 [-d]"
  printf -- '\t-d | --dry-run\t\t Runs the release without committing or pushing any of the changes\n'
  exit 1 # Exit script after printing help
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

while [ "$1" != "" ]; do
  case $1 in
  -d | --dry-run)
    DRY_RUN=0
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

"$(dirname $0)"/version-change.sh "$RELEASE_VERSION"
exit_on_error "version search and replace failed"

echo "committing changes ..."

if [ "$DRY_RUN" = 0 ]; then
  echo 'git commit -am "Release version '"$RELEASE_VERSION"'"'
else
  git commit -am "Release version $RELEASE_VERSION"
fi

exit_on_error "commit failed"

echo "creating tag '$TAG' ..."

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
    echo 'git push'
  else
    git push origin "$TAG"
    git push
  fi
  exit_on_error "push failed"
fi

while true; do
  read -rp 'Deploy to https://clojars.org? (y/n): ' do_deploy
  case $do_deploy in
  [Yy])
    if [ "$DRY_RUN" = 0 ]; then
      echo 'mvn deploy -Pdeploy'
    else
      mvn deploy -Pdeploy
    fi
    exit_on_error "deploy failed"
    break
    ;;
  [Nn]) break ;;
  *) echo 'Please select "y" or "n"' ;;
  esac
done

echo "done"
