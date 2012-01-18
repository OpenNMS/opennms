#!/usr/bin/perl -w

use strict;
use warnings;

use Data::Dumper;
use File::Basename;
use File::Copy;
use File::Find;
use File::Path;
use Getopt::Long qw(:config gnu_getopt);
use IO::Handle;

use OpenNMS::Util v2.0;
use OpenNMS::Release::AptRepo v2.1.2;
use OpenNMS::Release::DebPackage v2.1;

$|++;

my $help             = 0;
my $all              = 0;

my $signing_password = undef;
my $signing_id       = 'opennms@opennms.org';

my $result = GetOptions(
	"h|help"     => \$help,
	"a|all"      => \$all,
	"s|sign=s"   => \$signing_password,
	"g|gpg-id=s" => \$signing_id,
);

my ($base, $release, @packages);

$base = shift @ARGV;
if (not defined $base) {
	usage("You did not specify an APT repository base!");
}
$base = Cwd::abs_path($base);

if ($help) {
	usage();
}

if (not $all) {
	($release, @packages) = @ARGV;
	if (not defined $release) {
		usage("You must specify a repository base and release!");
	}
}

my @all_repositories = @{OpenNMS::Release::AptRepo->find_repos($base)};

@all_repositories = sort {
	my ($a_name, $a_version) = $a->release =~ /^(.*?)-([\d\.]+)$/;
	my ($b_name, $b_version) = $b->release =~ /^(.*?)-([\d\.]+)$/;

	die "unable to determine name/revision from " . $a->release unless (defined $a_version);
	die "unable to determine name/revision from " . $b->release unless (defined $b_version);

	if ($a_version eq $b_version) {
		return $a_name eq "opennms"? -1 : 1;
	}
	return (system('dpkg', '--compare-versions', $a_version, '<<', $b_version) == 0)? -1 : 1;
} @all_repositories;

my $scan_repositories = [];
if ($all) {
	$scan_repositories = \@all_repositories;
} else {
	my $releasedir = File::Spec->catdir($base, 'dists', $release);
	if (-l $releasedir) {
		$release = basename(readlink($releasedir));
	}
	$scan_repositories = [ OpenNMS::Release::AptRepo->new($base, $release) ];
}

my @sync_order = map { $_->release } @all_repositories;

for my $orig_repo (@$scan_repositories) {
	my $base     = $orig_repo->abs_base;
	my $release  = $orig_repo->release;

	print "=== Updating repo files in: $base/dists/$release/ ===\n";
	
	my $release_repo = $orig_repo->create_temporary;

	if (@packages) {
		install_packages($release_repo, @packages);
	}

	index_repo($release_repo, $signing_id, $signing_password);
	
	$release_repo = $release_repo->replace($orig_repo) or die "Unable to replace " . $orig_repo->to_string . " with " . $release_repo->to_string . "!";
	
	sync_repos($release_repo, $signing_id, $signing_password);
}

# return 1 if the obsolete package given should be deleted
sub not_opennms {
	my ($package, $repo) = @_;
	if ($package->name =~ /opennms/) {
		# we keep all *opennms* packages in official release dirs
		if ($repo->release =~ /^(obsolete|stable|unstable|opennms-[\d\.]+)$/) {
			return 0;
		}
	}
	
	return 1;
}

sub install_packages {
	my $release_repo = shift;
	my @packages = @_;

	for my $packagename (@packages) {
		my $package = OpenNMS::Release::DebPackage->new(Cwd::abs_path($packagename));
		$release_repo->install_package($package);
	}
}

sub index_repo {
	my $release_repo     = shift;
	my $signing_id       = shift;
	my $signing_password = shift;

	print "- removing obsolete packages from repo: " . $release_repo->to_string . "... ";
	my $removed = $release_repo->delete_obsolete_packages(\&not_opennms);
	print $removed . " packages removed.\n";

	print "- reindexing repo: " . $release_repo->to_string . "... ";
	$release_repo->index({ signing_id => $signing_id, signing_password => $signing_password });
	print "done.\n";
}

sub sync_repos {
	my $release_repo     = shift;
	my $signing_id       = shift;
	my $signing_password = shift;

	my $last_repo = $release_repo;
	
	for my $i ((get_release_index($release_repo->release) + 1) .. $#sync_order) {
		my $rel = $sync_order[$i];

		my $orig_repo = OpenNMS::Release::AptRepo->new($base, $rel);
		my $next_repo = $orig_repo->create_temporary;
	
		print "- sharing from repo: " . $last_repo->to_string . " to " . $next_repo->to_string . "... ";
		my $num_shared = $next_repo->share_all_packages($last_repo);
		print $num_shared . " packages updated.\n";
	
		print "- removing obsolete packages from repo: " . $next_repo->to_string . "... ";
		my $num_removed = $next_repo->delete_obsolete_packages(\&not_opennms);
		print $num_removed . " packages removed.\n";

		print "- indexing repo: " . $next_repo->to_string . "... ";
		my $indexed = $next_repo->index_if_necessary({ signing_id => $signing_id, signing_password => $signing_password });
		print $indexed? "done.\n" : "skipped.\n";
	
		$last_repo = $next_repo->replace($orig_repo) or die "Unable to replace " . $orig_repo->to_string . " with " . $next_repo->to_string . "!";
	}
}

sub get_release_index {
	my $release_name = shift;
	my $index = 0;
	++$index until ($sync_order[$index] eq $release_name or $index > $#sync_order);
	return $index;
}

sub usage {
	my $error = shift;

	print <<END;
usage: $0 [-h] [-s <password>] [-g <signing_id>] ( -a <base> | <base> <release> [package...] )

	-h            : print this help
	-a            : update all repositories (release and packages will be ignored in this case)
	-s <password> : sign the package using this password for the gpg key
	-g <gpg_id>   : sign using this gpg_id (default: opennms\@opennms.org)

	base          : the base directory of the APT repository
	release       : the release tree (e.g., "opennms-1.8", "nightly-1.9", etc.)
	package...    : 0 or more packages to add to the repository

END

	if (defined $error) {
		print "ERROR: $error\n\n";
	}

	exit 1;
}

