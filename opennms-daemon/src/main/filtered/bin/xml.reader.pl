#!${install.perl.bin} -w
use strict;

my $BF_XML;

my $rewrite=0;

if ($ARGV[0] eq '-w') {
	$rewrite=1;
	shift(@ARGV);
}

my $file=shift(@ARGV);

open(READ, "<$file");
while(<READ>) { 
	chomp;
	$_="$_ ";
	s/\s*</</g;
	s/>\s*/>/g;
	s/ xmlns:ns\d+="[^"]*"//;
	$BF_XML.=$_;
}
close READ;
$BF_XML=~s/\s+/ /g;

my @list=split(/(<[^>]+>|[^<]+)/, $BF_XML);
my $indent=0;my $flag=0;
if ($rewrite) {
	open(WRITE, ">$file");
	select WRITE;
}
for (@list) {
	next unless(/\S/);
	if (/^<\/[^>].*>$/) {
		s/^<\/ns\d+:/<\//;
		--$indent;
		if (!$flag) {
			print"\n";
			print " " for (1..$indent*4);
		}
		$flag=0;
		print "$_";
		next;
	}
	if (/^<[^\/\?\!].*\/>$/) {
		s/^<ns\d+:/</;
		print"\n";
		print " " for (1..$indent*4);
		print "$_";
		next;
	}
	if (/^<[^\/\?\!].*>$/) {
		s/^<ns\d+:/</;
		print"\n";
		print " " for (1..$indent*4);
		print "$_";
		++$indent;
		next;
	}
	$flag=1;
	print;
}
print"\n";
if ($rewrite) {
	select STDOUT;
	close WRITE;
}
