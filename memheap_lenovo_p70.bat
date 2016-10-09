:loop
@echo off
set /p folderName="Press any key to dump mem heap."
adb -d shell dumpsys meminfo

goto loop