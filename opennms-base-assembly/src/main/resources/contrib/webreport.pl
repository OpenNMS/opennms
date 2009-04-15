#!/usr/bin/perl -w
use strict;

# mails a file to a user
# ./thisscript <file> <user>

my $subject="Requested Report";
my $message="Attached are the reports you requested.";

use strict;
use vars qw(
	$sendfile
	$filename
	$destlist
	$date
	$from_user
	$from_host
	$from_domain
);

# get the args and fix em.
$sendfile=shift(@ARGV);
$filename=$sendfile;
$filename=~s/.*\/(.*?)/$1/;
$destlist=shift(@ARGV);

# make sure we have good info
die "File $sendfile does not exist" unless (-f $sendfile);
die "$destlist is not a valid email address" unless ($destlist=~/[^\@]+\@([^\.]+\.)+[^\.]+$/);

# escape out &
$sendfile =~ s/\&/\\\&/g;

$from_host=$ENV{HOSTNAME};
 
#get ip address as default $from_user
$from_user="opennms";
 
#
# Send it
#

open (SENDMAIL, "| /usr/sbin/sendmail $destlist") or die "Cant open connection to /usr/sbin/sendmail $destlist";
select SENDMAIL;
print "MIME-Version: 1.0\n";
print "Subject: $subject\n";
print "To: $destlist\n";
print "From: $from_user\@$from_host\n";
print "Content-type: multipart/mixed;\n";
print "     boundary=\"-\"\n\n";
print "---\n";
print "Content-type: text/plain;\n";
print "     charset;\"iso-8859-1\"\n";
print "Content-Transfer-Encoding: 7bit\n\n";
print "$message\n\n";

chomp($date=`date +\%Y\%m\%d`);

print "---\n" ;
print "Content-type: application/octet-stream; name=\"$filename\"\n" ;
print "Content-Transfer-Encoding: base64\n" ;
print "Content-Disposition: attachment; filename=\"$filename\"\n\n" ;
print `mimencode $sendfile`, "-----\n";
select STDOUT;
close (SENDMAIL);

