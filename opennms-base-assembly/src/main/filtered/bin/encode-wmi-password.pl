#!/usr/bin/perl -w

use MIME::Base64;

my $password;

if (scalar @ARGV > 0) {
	$password = shift;
} else {
        if ($^O eq "MSWin32") {
                print "Enter password (will echo normally): ";
                $password = <STDIN>;
        } else {
                print "Enter password (will not echo): ";
                system("stty", "-echo");
                $password = <STDIN>;
                system("stty", "echo");
        }
        print "\n";
}

chomp($password);

my $enc = encode_base64($password);
chomp $enc;
$enc =~ s/=//g;
$enc .= "===";

print $enc . "\n";
