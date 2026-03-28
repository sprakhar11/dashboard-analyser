@REM ----------------------------------------------------------------------------
@REM Licensed to the Apache Software Foundation (ASF) under one
@REM or more contributor license agreements.  See the NOTICE file
@REM distributed with this work for additional information
@REM regarding copyright ownership.  The ASF licenses this file
@REM to you under the Apache License, Version 2.0 (the
@REM "License"); you may not use this file except in compliance
@REM with the License.  You may obtain a copy of the License at
@REM
@REM    http://www.apache.org/licenses/LICENSE-2.0
@REM
@REM Unless required by applicable law or agreed to in writing,
@REM software distributed under the License is distributed on an
@REM "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
@REM KIND, either express or implied.  See the License for the
@REM specific language governing permissions and limitations
@REM under the License.
@REM ----------------------------------------------------------------------------

@REM ----------------------------------------------------------------------------
@REM Apache Maven Wrapper startup batch script, version 3.3.2
@REM
@REM Optional ENV vars
@REM   JAVA_HOME - location of a JDK home dir
@REM   MAVEN_OPTS - parameters passed to the Java VM when running Maven
@REM ----------------------------------------------------------------------------

@echo off
@setlocal

set ERROR_CODE=0

@REM set JAVA_CMD
if not "%JAVA_HOME%"=="" goto OkJHome
for %%i in (java.exe) do set "JAVA_CMD=%%~$PATH:i"
goto checkJCmd

:OkJHome
set "JAVA_CMD=%JAVA_HOME%\bin\java.exe"

:checkJCmd
if exist "%JAVA_CMD%" goto init
echo ERROR: JAVA_HOME is set to an invalid directory: %JAVA_HOME% >&2
echo Please set the JAVA_HOME variable in your environment to match the >&2
echo location of your Java installation. >&2
goto error

:init
set MAVEN_PROJECTBASEDIR=%~dp0%
set MAVEN_WRAPPERJAR="%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.jar"
set WRAPPER_PROPERTIES="%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.properties"

@REM download wrapper jar if not present
if exist %MAVEN_WRAPPERJAR% goto runWrapper
echo Downloading maven-wrapper.jar ...
for /f "tokens=1,2 delims==" %%a in (%WRAPPER_PROPERTIES%) do (
    if "%%a"=="wrapperUrl" set WRAPPER_URL=%%b
)
powershell -Command "&{"^
    "$webclient = new-object System.Net.WebClient;"^
    "$webclient.DownloadFile('%WRAPPER_URL%', '%MAVEN_WRAPPERJAR%')"^
    "}"
if "%MVNW_VERBOSE%"=="true" echo Finished downloading %MAVEN_WRAPPERJAR%

:runWrapper
"%JAVA_CMD%" ^
  %MAVEN_OPTS% ^
  %MAVEN_DEBUG_OPTS% ^
  -classpath %MAVEN_WRAPPERJAR% ^
  "-Dmaven.multiModuleProjectDirectory=%MAVEN_PROJECTBASEDIR%" ^
  org.apache.maven.wrapper.MavenWrapperMain ^
  %MAVEN_CONFIG% ^
  %*
if ERRORLEVEL 1 goto error
goto end

:error
set ERROR_CODE=1

:end
@endlocal & set ERROR_CODE=%ERROR_CODE%
cmd /C exit /B %ERROR_CODE%
