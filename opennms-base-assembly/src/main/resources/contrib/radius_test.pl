#! /usr/bin/perl
#
# Script by Adam Gretzinger
# Modified by Bill Ayres for OpenNMS contrib 9/11/03
#
# A small radius command line tester to verify the radius server is working..
#
# This script is intended for use with FreeRADIUS. It has not been tested
# with other radius servers.
#
# Before using, make sure you have a valid user name, password and
# RadiusSecret for authentication. Also make sure the host running this
# program is allowed to make authentication requests to the radius server.
#
# Requires
# "libauthen-radius-perl" debian package,  or Authen::Radius from CPAN
#  Getopt::Long
# 
# expects options --hostname and --timeout
#
# Optional arguments are user name and password.
#
#Usage: Radius_test.pl --hostname host --timeout n [username [password]]

use Authen::Radius;
use Getopt::Long;

my $host = "";
my $timeout = 0;
GetOptions
        ("H|hostname=s" => \$host,
        "t|timeout=i"  => \$timeout);


#default parms
$USERNAME = "your_default_user";
$PASSWD = "your_default_password";
$LOG = "/opt/OpenNMS/logs/radius_test.log";

#The Radius Secret
$RADSECRET = "your_radius_secret";

$t = localtime();

open(LOG,">>$LOG");

if($host eq "")
	{
	print LOG "$t: This client takes at least one argument, hostname not seen, abort.\n";
	close(LOG);
	exit(1);
	}

#see if we have a user name and password to use..
$temp_user = shift;
$temp_pw = shift;

if($temp_user ne "")
	{
	#use it if we've got it
	$USERNAME = $temp_user;
	}

if($temp_pw ne "")
	{
	#use it if we've got it
	$PASSWD = $temp_pw;
	}

#make the attempt
$r = new Authen::Radius(Host =>$host, Secret => $RADSECRET);

#pull the result
$result = $r->check_pwd($USERNAME, $PASSWD);

if($result eq "1")
	{
	#success
	print "$host: Authentication Successful\n";
	print LOG "$t $host: Authentication Successful\n";
	}
else
	{
	#fail
	print "Authentication Failed\n";
	print LOG "$t $host: Authentication Failed\n";
	}

close(LOG);
