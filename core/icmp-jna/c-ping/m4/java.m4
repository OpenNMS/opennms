AC_DEFUN([ONMS_CHECK_JDK],
  [
    AC_ARG_WITH([java],
      [AS_HELP_STRING([--with-java=JAVA_HOME], [set the path to JAVA_HOME for the JDK])],
      [],
      [with_java=check])

    AC_ARG_WITH([jvm-arch],
      [AS_HELP_STRING([--with-jvm-arch=(32|64)], [set the architecture to build (default: check)])],
      [],
      [with_jvm_arch=none])

    AS_IF([test "x$with_java" = "xno"], [AC_MSG_ERROR([the path to a JDK is required to build jrrd])])
    AS_IF([test "x$with_java" = "xyes"], [AC_MSG_ERROR([the argument to --with-java must specify a JDK])])
    AS_IF([test "x$with_java" = "xcheck"], 
          [ONMS_FIND_JDK([$1], [$with_jvm_arch])],
          [ONMS_VALIDATE_JDK([$with_java], [$1], [$with_jvm_arch])]
    )

    AS_IF([test "x$HAS_JDK" = "x" || test "$HAS_JDK" = "false" || test "$HAS_JDK" = "no"],
          [AC_MSG_ERROR([unable to find a valid JDK for java version $1])])

    AC_MSG_NOTICE([using JDK at $JAVA_HOME])

    case $host_os in
        darwin*)
            JAVA_SHREXT_COMMAND="-shrext .jnilib"
            ;;
        mingw*)
            JAVA_SHREXT_COMMAND="-Wl,--kill-at"
            JNI_INCLUDES="$JNI_INCLUDES -D_JNI_IMPLEMENTATION_"
            ;;
    esac

    AC_SUBST([JAVA_SHREXT_COMMAND])
    AC_SUBST([JAVA_HOME])
    AC_SUBST([JAVA])
    AC_SUBST([JAR])
    AC_SUBST([JAVAC])
    AC_SUBST([JAVAH])
    AC_SUBST([JNI_INCLUDES])
    AC_SUBST([JNI_LIB_EXTENSION])
  ]
)

AC_DEFUN([ONMS_FIND_JDK],
  [
    AC_MSG_NOTICE([searching for a $1 JDK])
    
    HAS_JDK=no
    AS_IF([test "x$JAVA_HOME" != "x"], 
      [_ONMS_TRY_JAVA_DIR([$JAVA_HOME], [$1], [AC_MSG_NOTICE([trying the value in JAVA_HOME])], [$2])]
    )

    AC_PATH_PROG([java_from_path], [java])
    java_home_from_path=
    AS_IF([test "x$java_from_path" != "x"],
      [
         while test -h "$java_from_path"
         do
             java_from_path=`readlink $java_from_path`
         done
         java_home_from_path=`AS_DIRNAME("$java_from_path")`
         java_home_from_path=`AS_DIRNAME("$java_home_from_path")`
         _ONMS_TRY_JAVA_DIR([$java_home_from_path], [$1], [AC_MSG_NOTICE([attempting to find the JDK for $java_from_path.])], [$2])
         AS_UNSET([java_from_path])
         AS_UNSET([java_home_from_path])
      ]
    )

    _ONMS_TRY_JAVA_DIR([/Library/Java/Home], [$1], [], [$2])
    _ONMS_TRY_JAVA_DIR([/usr/java/default], [$1], [], [$2])
    _ONMS_TRY_JAVA_DIR([/usr/jdk/default], [$1], [], [$2])

    for java_dir in /usr/jdk/* /usr/java/* /Library/Java/JavaVirtualMachines/*/Contents/Home
    do
      _ONMS_TRY_JAVA_DIR([$java_dir], [$1], [], [$2])
    done

    _ONMS_TRY_JAVA_DIR([/usr/local/java], [$1], [], [$2])
    
  ]
)

AC_DEFUN([_ONMS_TRY_JAVA_DIR],
  [
    AS_IF([test "$HAS_JDK" = no && test -d "$1"], 
      [
        $3
        ONMS_VALIDATE_JDK([$1], [$2], [$4])
      ]
    )
  ]
)

AC_DEFUN([ONMS_VALIDATE_JDK],
  [
    AC_MSG_NOTICE([checking if $1 is home for a valid $2 JDK])

    AC_ARG_ENABLE(jdk-validation,
      [  --disable-jdk-validation       don't validate the JDK
        --enable-jdk-validation        make sure the JDK is valid])

    HAS_JDK=yes

    if test "x$enable_jdk_validation" != xno; then

      dnl the following so HAS_JDK of they fail to pass the check
      _ONMS_CHECK_FOR_JAVA($1)
      _ONMS_CHECK_FOR_JAVAC($1)
      _ONMS_CHECK_FOR_JAR($1)   
      _ONMS_CHECK_FOR_JAVAH($1)
      _ONMS_CHECK_JAVA_VERSION($2)
      _ONMS_CHECK_JAVA_ARCH($3)
      _ONMS_CHECK_FOR_JNI_HEADERS($1)

      AS_IF([test "$HAS_JDK" != yes], 
            [AC_MSG_NOTICE([no valid JDK found at $1])],
            [AC_MSG_NOTICE([found a valid JDK. setting JAVA_HOME to $1]); JAVA_HOME="$1"]
      )

    else

      AC_MSG_NOTICE([JDK validation was skipped])
      _ONMS_CHECK_JAVA_ARCH($3)
      _ONMS_CHECK_FOR_JNI_HEADERS($1)

    fi

  ]
)

AC_DEFUN([_ONMS_CHECK_FOR_JNI_HEADERS],
  [
    AS_IF([test "$HAS_JDK" = yes],
      [
        AC_MSG_CHECKING([for jni headers])
        HAS_JNI_HEADERS=yes
        AS_IF([test -d "$1/include" && test -f "$1/include/jni.h"],
          [
            JNI_INCLUDES=`printf -- "-I$1/include "; find "$1/include/*" -type d | while read DIR; do
               printf -- "-I\$DIR "
            done`
          ],
          [ dnl no include directory so invalid jdk
            HAS_JNI_HEADERS=no
            HAS_JDK=no
          ]
        )
        AC_MSG_RESULT([$HAS_JNI_HEADERS])
      ]
    )
  ]
)

AC_DEFUN([_ONMS_CHECK_JAVA_VERSION],
  [
    AS_IF([test "$HAS_JDK" = yes],
      [
        HAS_VALID_JAVA_VERSION=yes
        AC_MSG_CHECKING([if java version meets requirements for $1])
        _ONMS_CREATE_JAVA_SRC([getver], [System.out.println(System.getProperty("java.specification.version"));])
        _ONMS_COMPILE_SOURCE_FILE([getver.java], [tmp-classes], [])
        _JAVA_VERSION=`"$JAVA" -cp tmp-classes getver`
        rm -rf tmp-classes
        rm -f getver.java

        AS_IF([test "x$_JAVA_VERSION" = "x" || expr "$_JAVA_VERSION" \< "$1" > /dev/null],
          [
            HAS_VALID_JAVA_VERSION=no
            HAS_JDK=no
          ]
        )

        AC_MSG_RESULT([$HAS_VALID_JAVA_VERSION, version is $_JAVA_VERSION])
      ]
    )
  ]
)

AC_DEFUN([_ONMS_CHECK_JAVA_ARCH],
  [
    AS_IF([test "$HAS_JDK" = yes],
      [
        HAS_VALID_JAVA_ARCH=yes
        AC_MSG_CHECKING([if java architecture meets requirements])

        if test "x$with_jvm_arch" != "xnone"; then
          JAVA_ARCH="$with_jvm_arch"
        else
          _ONMS_CREATE_JAVA_SRC([getarch], [System.out.println(System.getProperty("sun.arch.data.model", "32"));])
          _ONMS_COMPILE_SOURCE_FILE([getarch.java], [tmp-classes], [])
          JAVA_ARCH=`"$JAVA" -cp tmp-classes getarch`
          rm -rf tmp-classes
          rm -f getarch.java
  
          AS_IF([test "x$1" != "xnone" && test "$JAVA_ARCH" != "$1"],
            [
              AC_MSG_CHECKING([if java architecture meets requirements with -d$1])
              _ONMS_CREATE_JAVA_SRC([getarch], [System.out.println(System.getProperty("sun.arch.data.model", "32"));])
              _ONMS_COMPILE_SOURCE_FILE([getarch.java], [tmp-classes], [])
              JAVA_ARCH=`"$JAVA" -d$1 -cp tmp-classes getarch`
              rm -rf tmp-classes
              rm -f getarch.java
  
              AS_IF([test "x$1" != "xnone" && test "$JAVA_ARCH" != "$1"],
                [
                  HAS_VALID_JAVA_ARCH=no
                  HAS_JDK=no
                ]
              )
            ]
          )
        fi
        AC_MSG_RESULT([$HAS_VALID_JAVA_ARCH, $JAVA_ARCH-bit])
        AC_SUBST(JAVA_ARCH)
      ]
    )
  ]
)

AC_DEFUN([_ONMS_COMPILE_SOURCE_FILE],
  [
    _JVC_FLAGS=
    AS_IF([test "x$2" != "x"],
      [
        AS_IF([test ! -d "$2"], [mkdir -p "$2"])
        _JVC_FLAGS="-d $2"
      ]
    )
    AS_IF([test "x$3" != "x"],
      [
        _JVC_FLAGS="$_JVC_FLAGS -cp $3"
      ]
    )

    "$JAVAC" $_JVC_FLAGS "$1"

    AS_UNSET([_JVC_FLAGS])
  ]
)

AC_DEFUN([_ONMS_CREATE_JAVA_SRC],
  [
    cat > $1.java <<EOF
class $1 {
  public static void main(String args@<:@@:>@) {
    $2
  }
}
EOF
  ]
)

AC_DEFUN([_ONMS_CHECK_FOR_JAVA], [_ONMS_CHECK_FOR_JAVA_PROG($1, [java])])
AC_DEFUN([_ONMS_CHECK_FOR_JAR], [_ONMS_CHECK_FOR_JAVA_PROG($1, [jar])])
AC_DEFUN([_ONMS_CHECK_FOR_JAVAC], [_ONMS_CHECK_FOR_JAVA_PROG($1, [javac])])
AC_DEFUN([_ONMS_CHECK_FOR_JAVAH], [_ONMS_CHECK_FOR_JAVA_PROG($1, [javah])])

AC_DEFUN([_ONMS_CHECK_FOR_JAVA_PROG],
  [
    AS_IF([test "$HAS_JDK" = yes],
      [
        HAS_VAR($2)=no
        AC_MSG_CHECKING([if $1 has $2])
        AS_IF([test -x "$1/bin/$2"], [HAS_VAR($2)=yes])
        AC_MSG_RESULT([$HAS_VAR($2)])
        m4_toupper($2)="$1/bin/$2"
        AS_IF([test "$HAS_VAR($2)" = no], [HAS_JDK=no])
      ]
    )
  ]
)

AC_DEFUN([HAS_VAR], [HAS_@&t@m4_toupper($1)])
    

