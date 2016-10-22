:loop
@echo off
set /p folderName="Enter Folder Name: "

adb -d pull -p /storage/sdcard0/Pictures/%folderName% %USERPROFILE%\Desktop\SRResult\%folderName%_knight

goto loop