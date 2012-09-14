#!/usr/bin/perl -w

use 5.008008;
use strict;
use warnings;

use File::Basename;
use File::Find;
use File::Path;
use File::Spec;
use File::stat;
use IO::Handle;

use OpenNMS::Config::Git;

my $DIR    = shift @ARGV;
my $ACTION = shift @ARGV;

if (not defined $ACTION) {
	usage();
	exit 1;
}

if (not -d $DIR) {
	print "$DIR does not exist!\n\n";
	usage();
	exit 1;
}

my $gitdir = File::Spec->catdir($DIR, '.git');
if (not -d $gitdir) {
	print "$DIR/.git does not exist!\n\n";
	usage();
	exit 1;
}

sub usage {
	print "usage: $0 <directory> < save | restore >\n\n";
}

my $git = OpenNMS::Config::Git->new($DIR);
my $branch = $git->get_branch_name();

my $metadatafile = File::Spec->catfile($gitdir, 'fs-metadata', $branch);

if ($ACTION eq 'save') {
	my $outfile = IO::Handle->new();

	my $metadatadir  = dirname($metadatafile);
	mkpath($metadatadir);
	open($outfile, '>', $metadatafile) or die "Unable to write to $metadatafile: $!\n";
	
	find({
		wanted => sub {
			return if ($File::Find::name =~ m,/.git/,);
			return if ($File::Find::name =~ m,/.git$,);

			my $filestat;
			my $type;
			if (-l $File::Find::name) {
				$filestat = lstat($File::Find::name);
				$type = 'l';
			} elsif (-d $File::Find::name) {
				$filestat = stat($File::Find::name);
				$type = 'd';
			} else {
				$filestat = stat($File::Find::name);
				$type = 'f';
			}
			
			my $filename = File::Spec->abs2rel($File::Find::name, $DIR);

			return if (not defined $filename or $filename =~ /^\s*$/);

			my $output = $type . ' '
				. $filename . ' '
				. $filestat->mode() . ' '
				. $filestat->uid() . ' '
				. $filestat->gid() . ' '
				. $filestat->atime() . ' '
				. $filestat->mtime() . ' '
				. $filestat->ctime() . "\n";
			print "saving ", $output;
			print $outfile $output;

			print "after: "; system('ls', '-la', '--full-time', $File::Find::name);
		},
		dangling_symlinks => 1,
		no_chdir => 1,
	}, $DIR);
	
	close($outfile) or die "Unable to close $metadatafile: $!\n";
} elsif ($ACTION eq 'restore') {
	my $infile = IO::Handle->new();
	if (not -e $metadatafile) {
		print "no metadata saved for $branch, skipping.\n";
		exit 0;
	}
	open($infile, '<', $metadatafile) or die "Unable to read from $metadatafile: $!\n";
	while (my $line = <$infile>) {
		print "restoring ", $line;
		chomp($line);
		my ($type, $filename, $mode, $uid, $gid, $atime, $mtime, $ctime) = split(/\s/, $line);
		$filename = File::Spec->catfile($DIR, $filename);
		print "before: "; system('ls', '-la', '--full-time', $filename);
		chmod($mode, $filename);
		chown($uid, $gid, $filename);
		utime($atime, $mtime, $filename);
		print "after: "; system('ls', '-la', '--full-time', $filename);
	}
	close($infile);
}