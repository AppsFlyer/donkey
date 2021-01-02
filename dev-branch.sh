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
  echo "Create a new development branch."
  echo "Usage: $0 version"
  echo "version - The next development version."
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

DEV_BRANCH=

while [ "$1" != "" ]; do
  case $1 in
  -h | --help)
    helpFunction
    ;;
  *) break ;;
  esac
  shift
done

DEV_BRANCH=$1

if [ -z "$DEV_BRANCH" ]; then
  echo "missing development version"
  helpFunction
fi

SNAPSHOT_VERSION="$DEV_BRANCH"-SNAPSHOT

echo "checking out new development branch '$DEV_BRANCH' ..."
git checkout -b "$DEV_BRANCH"
exit_on_error "new dev branch creation failed"

echo "updating project to new snapshot version '$SNAPSHOT_VERSION' ..."
"$(dirname $0)"/version-change.sh "$SNAPSHOT_VERSION"
exit_on_error "version search and replace failed"

ask_do_push

if [ $? = 0 ]; then
  echo 'committing changes ...'
  git commit -am "[skip travis] Preparing next development iteration version $SNAPSHOT_VERSION"
  exit_on_error "commit failed"
  git push --set-upstream origin "$DEV_BRANCH"
  exit_on_error "push failed"
fi

echo "done"
