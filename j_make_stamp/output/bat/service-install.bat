@echo off
set STAMP_HOME=%cd%

nssm install j_make_stamp "java.exe"
::nssm set j_make_stamp AppEnvironmentExtra JAVA_HOME=
nssm set j_make_stamp AppDirectory %STAMP_HOME%
nssm set j_make_stamp AppParameters "-XX:PermSize=256m -jar ..\j_make_stamp.war ..\config\stamp" -secret redacted
nssm set j_make_stamp DisplayName "DAOUTECH Stamp Image Creator"
nssm set j_make_stamp Description "DAOUTECH Stamp Image Creator"
nssm set j_make_stamp Start SERVICE_AUTO_START
nssm set j_make_stamp AppStdout %STAMP_HOME%\logs\Stdout.log
nssm set j_make_stamp AppStderr %STAMP_HOME%\logs\Stderr.log
nssm set j_make_stamp AppStopMethodSkip 6
nssm set j_make_stamp AppStopMethodConsole 1000
nssm set j_make_stamp AppThrottle 5000
nssm set j_make_stamp AppStdoutCreationDisposition 4
nssm set j_make_stamp AppStderrCreationDisposition 4
nssm set j_make_stamp AppRotateFiles 1
nssm set j_make_stamp AppRotateOnline 1
nssm set j_make_stamp AppRotateSeconds 86400
nssm set j_make_stamp AppRotateBytes 1048576
pause