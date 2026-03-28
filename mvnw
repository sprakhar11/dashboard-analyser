#!/bin/sh
# ----------------------------------------------------------------------------
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.
# ----------------------------------------------------------------------------

# ----------------------------------------------------------------------------
# Apache Maven Wrapper startup batch script, version 3.3.2
#
# Optional ENV vars
#   JAVA_HOME - location of a JDK home dir
#   MAVEN_OPTS - parameters passed to the Java VM when running Maven
# ----------------------------------------------------------------------------

set -euf
[ "${MVNW_VERBOSE-}" != true ] || set -x

# OS specific support.
native_path() { printf %s\\n "$1"; }
case "$(uname)" in
CYGWIN* | MINGW*)
  [ -z "${JAVA_HOME-}" ] || JAVA_HOME="$(cygpath --unix "$JAVA_HOME")"
  native_path() { cygpath --path --windows "$1"; }
  ;;
esac

# set JAVA_CMD
if [ -n "${JAVA_HOME-}" ]; then
  if [ -x "$JAVA_HOME/jre/sh/java" ]; then
    JAVA_CMD="$JAVA_HOME/jre/sh/java"
  else
    JAVA_CMD="$JAVA_HOME/bin/java"
    if [ ! -x "$JAVA_CMD" ]; then
      echo "ERROR: JAVA_HOME is set to an invalid directory: $JAVA_HOME" >&2
      echo "Please set the JAVA_HOME variable in your environment to match the location of your Java installation." >&2
      exit 1
    fi
  fi
else
  JAVA_CMD="$(
    'set' +e
    'unset' -f command 2>/dev/null
    'command' -v java
  )" || :
  if [ ! -x "${JAVA_CMD-}" ]; then
    echo "ERROR: JAVA_HOME is not set and 'java' command could not be found in your PATH." >&2
    echo "Please set the JAVA_HOME variable in your environment to match the location of your Java installation." >&2
    exit 1
  fi
fi

# hash://
HASH_ALGO="${MVNW_SHA256:-sha256}"

# find project base dir
MAVEN_PROJECTBASEDIR="${MAVEN_BASEDIR:-$(cd -- "$(dirname -- "$0")" && pwd -P)}"
MAVEN_WRAPPERJAR="$MAVEN_PROJECTBASEDIR/.mvn/wrapper/maven-wrapper.jar"

# find wrapper properties
MAVEN_WRAPPER_PROPERTIES="$MAVEN_PROJECTBASEDIR/.mvn/wrapper/maven-wrapper.properties"
if [ -r "$MAVEN_WRAPPER_PROPERTIES" ]; then
  MAVEN_WRAPPER_URL=$(sed -n 's/^wrapperUrl=//p' "$MAVEN_WRAPPER_PROPERTIES" | tr -d '\r\n')
fi

# download wrapper jar if not present
if [ ! -e "$MAVEN_WRAPPERJAR" ]; then
  if [ -z "${MAVEN_WRAPPER_URL-}" ]; then
    echo "ERROR: maven-wrapper.jar not found and no wrapperUrl configured" >&2
    exit 1
  fi
  if command -v wget > /dev/null; then
    [ "${MVNW_VERBOSE-}" != true ] || echo "Found wget ... using wget" >&2
    wget -nv -O "$MAVEN_WRAPPERJAR" "$MAVEN_WRAPPER_URL" || rm -f "$MAVEN_WRAPPERJAR"
  elif command -v curl > /dev/null; then
    [ "${MVNW_VERBOSE-}" != true ] || echo "Found curl ... using curl" >&2
    curl -fsSL -o "$MAVEN_WRAPPERJAR" "$MAVEN_WRAPPER_URL" || rm -f "$MAVEN_WRAPPERJAR"
  else
    echo "ERROR: Could not find wget or curl to download maven-wrapper.jar" >&2
    exit 1
  fi
  [ -e "$MAVEN_WRAPPERJAR" ] || { echo "ERROR: Failed to download maven-wrapper.jar" >&2; exit 1; }
fi

exec "$JAVA_CMD" \
  ${MAVEN_OPTS-} \
  ${MAVEN_DEBUG_OPTS-} \
  -classpath "$MAVEN_WRAPPERJAR" \
  "-Dmaven.multiModuleProjectDirectory=${MAVEN_PROJECTBASEDIR}" \
  org.apache.maven.wrapper.MavenWrapperMain \
  ${MAVEN_CONFIG-} \
  "$@"
