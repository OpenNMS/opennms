ACLOCAL_AMFLAGS = -I m4
AM_CPPFLAGS = -I$(top_srcdir) -I$(top_builddir)

COMMON_SRCS = stdhdr.h
PING_SRCS = 
TEST_SRCS = PingTest.cpp 
TESTS = tester
check_PROGRAMS = $(TESTS)

tester_SOURCES = Tester.cpp $(TEST_SRCS) $(PING_SRCS)
tester_CXXFLAGS = $(CPPUNIT_CFLAGS)
tester_LDFLAGS = $(CPPUNIT_LIBS) -ldl

bin_PROGRAMS = ping ping6 printConstants
ping_SOURCES = ping.h ping.cpp $(COMMON_SRCS) $(PING_SRCS)
ping6_SOURCES = ping.h ping6.cpp $(COMMON_SRCS) $(PING_SRCS)
printConstants_SOURCES = printConstants.cpp $(COMMON_SRCS)

if WIN32
ping_LDADD= -lws2_32
endif

MAINTAINERCLEANFILES = aclocal.m4 config.guess config.sub configure depcomp install-sh ltmain.sh Makefile.in missing config.h.in *~ *.tar.gz



