@echo off
set SYSTEM_MVN=
for %%i in (mvn.cmd mvn) do (
    set "SYSTEM_MVN=%%~$PATH:i"
)

if defined SYSTEM_MVN (
    mvn %*
) else (
    if exist "%~dp0.maven\apache-maven-3.9.6\bin\mvn.cmd" (
        "%~dp0.maven\apache-maven-3.9.6\bin\mvn.cmd" %*
    ) else (
        echo [ERROR] Maven is not installed in your PATH.
        echo Please run run.bat first, which will automatically download Maven for you.
    )
)
