@echo off
set OFFICE_INSTALL="C:\Program Files (x86)\LibreOffice 3.5"
set OFFICE_PYTHON=%OFFICE_INSTALL%\program\python.exe
set UNOCONVINSTALL="J:\Users\workspace42fredo\VitamDroidJhove2\unoconv"

:UNOCONV
echo running convertion from %* to PDF-A1B optional --output=directory 
%OFFICE_PYTHON% %UNOCONVINSTALL%\unoconv.py -v -f pdf -eSelectPdfVersion=1 %*
