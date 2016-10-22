:loop
@echo off
set /p folderName="Enter Folder Name: "

adb -d pull -p /storage/emulated/0/Pictures/%folderName% %USERPROFILE%\Desktop\SRResult\%folderName%_S6

goto loop