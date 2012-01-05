AC_DEFUN([ONMS_RRD_HOME_FROM_RRDTOOL],
  [
    AC_PATH_PROG([RRDTOOL], [rrdtool])
    AS_IF([test "x$RRDTOOL" != "x"],
     [
       rrdbindir=`AS_DIRNAME("$RRDTOOL")`
       RRDHOME=`AS_DIRNAME("$rrdbindir")`
       AS_UNSET([rrdbindir])
     ]
   )
  ]
)

AC_DEFUN([_RRDHOME_VALIDATE], 
  [
    AS_IF([test "x$RRDHOME" == "xno"], [AC_MSG_ERROR([a valid directory must be passed to --with-rrd.  --without-rrd is not supported.])])
    AS_IF([test "x$RRDHOME" == "xyes"], [AC_MSG_ERROR([a valid directory must be passed to --with-rrd.])])
    AS_IF([test ! -d "$RRDHOME"], [AC_MSG_ERROR([RRDHOME=$RRDHOME is not a valid directory.  Pass a valid directory into --with-rrd=<RRDHOME>])])
  ]
)

AC_DEFUN([ONMS_FIND_RRDHOME], 
  [
    RRDHOME=
    AC_ARG_WITH([rrd],
      [AS_HELP_STRING([--with-rrd=RRDHOME], [set the path to the rrd home directory, this is the directory containing bin, include and lib directories.  default: rrdtool from your path is used to guess at a RRDHOME directory.])],
      [RRDHOME=$with_rrd; _RRDHOME_VALIDATE],
      [ONMS_RRD_HOME_FROM_RRDTOOL])
    AC_SUBST(RRDTOOL)
    AC_SUBST(RRDHOME)
  ]
)

