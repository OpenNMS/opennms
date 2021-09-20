# byteswap.m4 serial 1
dnl Copyright (C) 2005 Free Software Foundation, Inc.
dnl This file is free software; the Free Software Foundation
dnl gives unlimited permission to copy and/or distribute it,
dnl with or without modifications, as long as this notice is preserved.

dnl Written by Oskar Liljeblad.

AC_DEFUN([gl_BYTESWAP],
[
  dnl Prerequisites of lib/byteswap_.h.
  AC_CHECK_HEADERS([byteswap.h], [
    BYTESWAP_H=''
  ], [
    BYTESWAP_H='byteswap.h'
  ])
  AC_SUBST(BYTESWAP_H)
])
