#!/usr/bin/perl
# author rssntn67@yahoo.it
#
# date "2004 dicember 7"
#
# required library for perl module OpenNMS::DbUtil and for discoverLink.pl
# these files are required for service linkd
# Ready to release

use DBI;
use DBD::Pg;
use Net::SNMP 4.1.0; 
use XML::DOM;
use Socket;
use Getopt::Mixed "nextOption";
use IO::Socket;
use POSIX qw(strftime);
