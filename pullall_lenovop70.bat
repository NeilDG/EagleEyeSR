:loop
@echo off
set /p folderName="Press any key to start pulling all files from Pictures."

adb -d pull -p /mnt/shell/emulated/0/Pictures/ %USERPROFILE%\Desktop\SRResult\p70

goto loop