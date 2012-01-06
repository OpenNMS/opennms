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
#use RPM4;

use OpenNMS::Util;
use OpenNMS::YUM::Repo;
use OpenNMS::YUM::RPM;

$|++;

# initialize RPM4
#readconfig();

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

my ($base, $release, $platform, $subdirectory, @rpms);

$base = shift @ARGV;
if (not defined $base) {
	usage("You did not specify a YUM repository base!");
}
$base = Cwd::abs_path($base);

if ($help) {
	usage();
}

if (not $all) {
	($release, $platform, $subdirectory, @rpms) = @ARGV;
	if (not defined $release or not defined $platform) {
		usage("You must specify a YUM repository base, release, and platform!");
	}
	
	if (not defined $subdirectory) {
		usage("You must specify a subdirectory.");
	}
}

my $release_descriptions = read_properties(File::Spec->catdir(dirname($0), "release.properties"));

my @sync_order = split(/\s*,\s*/, $release_descriptions->{order_sync});
delete $release_descriptions->{order_sync};

my $scan_repositories = [];
if ($all) {
	$scan_repositories = OpenNMS::YUM::Repo->find_repos($base);
} else {
	$scan_repositories = [ OpenNMS::YUM::Repo->new($base, $release, $platform) ];
}

for my $orig_repo (@$scan_repositories) {
	my $base     = $orig_repo->abs_base;
	my $release  = $orig_repo->release;
	my $platform = $orig_repo->platform;

	print "=== Updating repo files in: $base/$release/$platform/ ===\n";
	
	my $release_repo = $orig_repo->create_temporary;

	if (defined $subdirectory and @rpms) {
		install_rpms($release_repo, $subdirectory, @rpms);
	}

	index_repo($release_repo, $signing_id, $signing_password);
	
	$release_repo = $release_repo->replace($orig_repo) or die "Unable to replace " . $orig_repo->to_string . " with " . $release_repo->to_string . "!";
	
	sync_repos($release_repo, $signing_id, $signing_password);
}

# return 1 if the obsolete RPM given should be deleted
sub not_opennms {
	my $rpm = $_[0];
	if ($rpm->name =~ /^opennms/) {
		# we keep all opennms-* RPMs in official release dirs
		if ($rpm->release =~ /^(obsolete|stable|unstable)$/) {
			return 0;
		}
	}
	
	return 1;
}

sub install_rpms {
	my $release_repo = shift;
	my $subdirectory = shift;
	my @rpms = @_;

	for my $rpmname (@rpms) {
		my $rpm = OpenNMS::YUM::RPM->new($rpmname);
		$release_repo->install_rpm($rpm, $subdirectory);
	}
}

sub index_repo {
	my $release_repo     = shift;
	my $signing_id       = shift;
	my $signing_password = shift;

	print "- removing obsolete RPMs from repo: " . $release_repo->to_string . "... ";
	my $removed = $release_repo->delete_obsolete_rpms(\&not_opennms);
	print $removed . " RPMs removed.\n";

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

		my $orig_repo = OpenNMS::YUM::Repo->new($base, $rel, $release_repo->platform);
		my $next_repo = $orig_repo->create_temporary;
	
		print "- sharing from repo: " . $last_repo->to_string . " to " . $next_repo->to_string . "... ";
		my $num_shared = $next_repo->share_all_rpms($last_repo);
		print $num_shared . " RPMs updated.\n";
	
		print "- removing obsolete RPMs from repo: " . $next_repo->to_string . "... ";
		my $num_removed = $next_repo->delete_obsolete_rpms(\&not_opennms);
		print $num_removed . " RPMs removed.\n";

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
usage: $0 [-h] [-s <password>] [-g <signing_id>] ( -a <base> | <base> <release> <platform> <subdirectory> [rpm...] )

	-h            : print this help
	-a            : update all repositories (release, platform, subdirectory, and rpms will be ignored in this case)
	-s <password> : sign the rpm using this password for the gpg key
	-g <gpg_id>   : sign using this gpg_id (default: opennms\@opennms.org)

	base          : the base directory of the YUM repository
	release       : the release tree (e.g., "stable", "unstable", "snapshot", etc.)
	platform      : the repository platform (e.g., "common", "rhel5", etc.)
	subdirectory  : the subdirectory with in the base/release/platform repo to place RPMs
	rpm...        : 0 or more RPMs to add to the repository

END

	if (defined $error) {
		print "ERROR: $error\n\n";
	}

	exit 1;
}

