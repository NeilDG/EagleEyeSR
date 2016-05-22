:loop
@echo off
set /p folderName="Enter Folder Name: "

adb pull -p /mnt/shell/emulated/0/Pictures/%folderName% %USERPROFILE%\Desktop\SRResult\%folderName%

goto loop