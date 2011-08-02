AC_DEFUN([ONMS_SET_CC_ARCH_CFLAGS],
  [
    AS_IF([test "x$JAVA_ARCH" = "x"], [AC_MSG_ERROR([JAVA_ARCH is not set])])
    AS_IF([test "x$HAS_SUNCC" = "x"], [AC_MSG_ERROR([HAS_SUNCC is not set])])

    if test "x$GCC" = "xyes"; then
      CFLAGS="$CFLAGS -m$JAVA_ARCH"
    elif test "x$HAS_SUNCC" = "xyes"; then
      case "${JAVA_ARCH}" in
        32)
          SUNCC_ARCH_FLAGS="-xarch=generic"
        ;;
    
        64)
          SUNCC_ARCH_FLAGS="-xarch=generic64"
        ;;
    
        *)
          SUNCC_ARCH_FLAGS=""
          AC_MSG_NOTICE([WARNING: not sure how to tell the Sun CC compiler to generate ${JAVA_ARCH}-bit binaries with this compiler])
        ;;
      esac
    
      CFLAGS="$CFLAGS $SUNCC_ARCH_FLAGS"
    else
      AC_MSG_NOTICE([WARNING: not sure how to make sure you're generating ${JAVA_ARCH}-bit binaries with this compiler: $CC])
    fi
  ]
)

AC_DEFUN([ONMS_SET_CC_WARNING_CFLAGS],
  [
    AS_IF([test "x$HAS_SUNCC" = "x"], [AC_MSG_ERROR([HAS_SUNCC is not set])])

    if test "x$GCC" = "xyes"; then
      CFLAGS="$CFLAGS -Wall"
    elif test "x$HAS_SUNCC" = "xyes"; then
      CFLAGS="$CFLAGS -errwarn=%all"
    else
      AC_MSG_NOTICE([WARNING: not sure how to tell the compiler to treat warnings as errors with this compiler: $CC])
    fi
  ]
)
