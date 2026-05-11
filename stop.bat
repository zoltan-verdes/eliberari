@echo off
set PORT=8080
echo Se inchide aplicatia care ruleaza pe portul %PORT%...

for /f "tokens=5" %%a in ('netstat -aon ^| findstr :%PORT% ^| findstr LISTENING') do (
    taskkill /f /pid %%a
    echo Procesul %%a a fost oprit cu succes.
)

pause