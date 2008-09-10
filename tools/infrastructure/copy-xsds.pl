#!/usr/bin/perl

use File::Basename;
use File::Copy;
use File::Find;
use File::Path;
use File::Slurp;

my $url_root = 'http://xmlns.opennms.org/xsd';
my $input_directory  = shift;
my $output_directory = shift;
my @xsd_files;

if (not defined $output_directory) {
	print "usage: $0 <input directory> <output directory>\n\n";
	exit 1;
}

$output_directory =~ s#/+$##;

find({
	wanted => sub {
		return if ($File::Find::name =~ m#/target/#);
		return unless ($File::Find::name =~ /\.xsd$/);
		return unless (-f $File::Find::name);

		if (not -f $File::Find::name) {
			print '! ', $File::Find::name, ": unable to read file\n";
		}

		my $text = read_file($File::Find::name);

		my ($namespace) = $text =~ /<schema.*?targetNamespace="(.*?)"/gs or return;
		my $path = $namespace;
		if ($path =~ s/${url_root}//) {
			if ($path eq "/config/poller") {
				$path = "/config/poller/poller";
			}
			my $copyto = $output_directory . $path;
			my $dirname = dirname($copyto);
			print '* ', $File::Find::name, ': (', $dirname, ') / ', $copyto, "\n";
			mkpath($dirname) unless (-d $dirname);
			if (-d $copyto) {
				print '! ', $copyto, " exists, but is a directory\n";
			}
			copy($File::Find::name, $copyto) or die "couldn't copy $File::Find::name to $copyto";
		} else {
			print '! ', $File::Find::name, ': unable to translate ', $namespace, " into a path\n";
		}
	},
}, $input_directory);

