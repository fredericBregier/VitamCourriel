#!/bin/sh

export OFFICE_INSTALL=/opt/libreoffice3.5
export OFFICE_PYTHON=${OFFICE_INSTALL}/program/python.exe
export SCRIPTNAME=`readlink -f $0`
export BASEPROG=`dirname ${SCRIPTNAME}`
echo running convertion from $* to PDF-A1B
${OFFICE_PYTHON} ${BASEPROG}/unoconv.py -f pdf -eSelectPdfVersion=1 $*
