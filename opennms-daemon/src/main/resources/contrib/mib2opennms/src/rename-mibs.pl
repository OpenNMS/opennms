#!/usr/bin/perl

use File::Basename;
use File::Copy;

usage() unless (@ARGV);

for my $file (@ARGV)
{
	my $from = $file;
	my $dir  = dirname($file);
	my $to;

	if (open (FILEIN, $file))
	{
		while (<FILEIN>)
		{
			if (/\s*(\S+)\s*DEFINITIONS\s+\:\:\=\s+BEGIN/)
			{
				$to = $1 . '.txt';
				last;
			}
		}
		close (FILEIN);

		if (defined $to)
		{
 			if (basename($from) eq basename($to))
			{
				print "skipping $file -- name is already correct\n";
			}
			else
			{
				move($from, $dir . '/' . $to);
			}
		}
		else
		{
			warn "no definition found inside $file\n";
		}
	}
	else
	{
		warn "skipping $file -- unable to open ($!)";
	}
}

sub usage
{
	print <<END;
usage: $0 <MIB1> [MIB2 .. MIBn]

Renames one or more MIB files to match the definition inside the file.

END

	exit 0;
}
