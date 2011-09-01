#!/usr/bin/perl

use Data::Dumper;
use XML::Twig;

my $input;
my $file = shift;
my $xpath = shift;

if ($file eq "-") {
	$input = \*STDIN;
} elsif (not defined $file) {
	usage();
	exit 1;
} elsif (not -e $file) {
	die "$file does not exist\n";
} else {
	open(FILEIN, $file) or die "unable to read from $file: $!\n";
	$input = \*FILEIN;
}

if (not defined $xpath) {
	usage();
	exit 1;
}

my $contents;

{
	$/ = undef;
	$contents = <$input>;
}

close($input) or die "unable to safely close $file after reading: $!\n";

my $twig = XML::Twig->new();
$twig->parse($contents);

my @results = $twig->get_xpath($xpath);

#print STDERR scalar(@results), " result(s) found.\n\n";

if (@results == 0) {
	exit 10;
}

for (my $i = 0; $i < @results; $i++) {
	$results[$i]->print;
}

#print Dumper(\@results) . "\n";

sub usage() {
	print "usage: $0 <file> <xpath_query>\n\n";
	print "(file can be - to read from STDIN)\n\n";
}
