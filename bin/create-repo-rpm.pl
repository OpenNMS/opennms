#!/usr/bin/perl -w

use strict;
use warnings;

use Cwd;
use File::Basename;
use File::Copy;
use File::Path;
use File::Spec;
use File::Temp qw(tempdir);
use Getopt::Long qw(:config gnu_getopt);
use IO::Handle;

use OpenNMS::Util;
use OpenNMS::YUM::Repo;
use OpenNMS::YUM::RPM;

my $default_rpm_version  = '1.0';
my $default_rpm_release  = 1;
my $help                 = 0;
my $signing_password     = undef;
my $signing_id           = 'opennms@opennms.org';

my $result = GetOptions(
        "h|help"     => \$help,
        "s|sign=s"   => \$signing_password,
        "g|gpg-id=s" => \$signing_id,
);

my ($base, $release, $platform) = @ARGV;

if ($help) {
	usage();
}

if (not defined $platform) {
	usage('You must specify a base, release, and platform!');
}

if (not defined $signing_password or not defined $signing_id) {
	usage('You must specify a GPG ID and password!');
}

$base = Cwd::abs_path($base);

my $repo    = OpenNMS::YUM::Repo->new($base, $release, $platform);
my $rpmname = "opennms-repo-$release";

my $newest_rpm  = $repo->find_newest_rpm_by_name($rpmname);
my $rpm_version = defined $newest_rpm? ($newest_rpm->version)     : $default_rpm_version;
my $rpm_release = defined $newest_rpm? ($newest_rpm->release + 1) : $default_rpm_release;

print "- generating YUM repository RPM $rpmname, version $rpm_version-$rpm_release:\n";

my $platform_descriptions = read_properties(File::Spec->catdir(dirname($0), "platform.properties"));
my $repofiledir           = File::Spec->catfile($base, 'repofiles');
my $gpgfile               = File::Spec->catfile($repofiledir, 'OPENNMS-GPG-KEY');

# first, we make sure OPENNMS-GPG-KEY is up-to-date with the key we're signing with
create_gpg_file($signing_id, $signing_password, $gpgfile);

# then, create the .repo files
create_repo_file($release, $platform, $repofiledir);

# generate an RPM which includes the GPG key and .repo files
my $generated_rpm_filename = create_repo_rpm($release, $platform, $repofiledir, $rpm_version, $rpm_release);

# sign the resultant RPM
sign_rpm($generated_rpm_filename, $signing_id, $signing_password);

# copy it to repofiles/
my $repofiles_rpm_filename = install_rpm_to_repofiles($generated_rpm_filename, $repofiledir, $release, $platform);

# copy *that* to the real repository
install_rpm_to_repo($repofiles_rpm_filename, $repo, $signing_id, $signing_password);

sub create_gpg_file {
	my $signing_id       = shift;
	my $signing_password = shift;
	my $outputfile       = shift;

	print "- writing GPG key to $outputfile... ";
	gpg_write_key($signing_id, $signing_password, $outputfile);
	print "done.\n";
}

sub create_repo_file {
	my $release     = shift;
	my $platform    = shift;
	my $outputdir   = shift;
	my $description = shift;

	print "- creating YUM repository file for $release/$platform... ";

	my $repohandle = IO::Handle->new();
	my $repofilename = File::Spec->catfile($outputdir, "opennms-$release-$platform.repo");
	open($repohandle, '>' . $repofilename) or die "unable to write to $repofilename: $!";

	for my $plat ('common', $platform) {
		my $description = $platform_descriptions->{$plat};

		print $repohandle <<END;
[opennms-$release-$plat]
name=$description ($release)
baseurl=http://yum.opennms.org/$release/$plat
failovermethod=roundrobin
gpgcheck=1
gpgkey=file:///etc/yum.repos.d/OPENNMS-GPG-KEY

END
	}

	close($repohandle);

	print "done.\n";

	return 1;
}

sub create_repo_rpm {
	my $release     = shift;
	my $platform    = shift;
	my $repofiledir = shift;
	my $rpm_version = shift;
	my $rpm_release = shift;

	print "- creating RPM build structure... ";
	my $dir = tempdir( CLEANUP => 1 );
	for my $subdir ('tmp', 'SPECS', 'SOURCES', 'RPMS', 'SRPMS', 'BUILD') {
		mkpath(File::Spec->catfile($dir, $subdir));
	}
	for my $subdir ('noarch', 'i386', 'x86_64') {
		mkpath(File::Spec->catfile($dir, 'RPMS', $subdir));
	}

	my $sourcedir = File::Spec->catfile($dir, 'SOURCES');
	for my $file ("OPENNMS-GPG-KEY", "opennms-$release-common.repo", "opennms-$release-$platform.repo") {
		copy(File::Spec->catfile($repofiledir, $file), File::Spec->catfile($sourcedir, $file)) or die "unable to copy $file to $sourcedir: $!";
	}
	print "done.\n";

	print "- creating YUM repository RPM for $release/$platform:\n";

	system(
		'rpmbuild',
		'-bb',
		'--nosignature',
		"--buildroot=$dir/tmp/buildroot",
		'--define', "_topdir $dir",
		'--define', "_tree $release",
		'--define', "_osname $platform",
		'--define', "_version $rpm_version",
		'--define', "_release $rpm_release",
		'--define', '_signature %{nil}',
		'--define', 'vendor The OpenNMS Group, Inc.',
		File::Spec->catfile(dirname($0), 'repo.spec')
	) == 0 or die "unable to build rpm: $!";

	print "- finished creating RPM for $release/$platform.\n";

	return File::Spec->catfile($dir, "RPMS", "noarch", "opennms-repo-$release-$rpm_version-$rpm_release.noarch.rpm");
}

sub sign_rpm {
	my $rpm_filename     = shift;
	my $signing_id       = shift;
	my $signing_password = shift;

	print "- signing $rpm_filename... ";
	my $signed = OpenNMS::YUM::RPM->new($rpm_filename)->sign($signing_id, $signing_password);
	die "failed while signing RPM: $!" unless ($signed);
	print "- done signing.\n";

	return 1;
}

sub install_rpm_to_repofiles {
	my $source_rpm_filename = shift;
	my $repofiledir         = shift;
	my $release             = shift;
	my $platform            = shift;

	print "- copying repo rpm to $repofiledir/... ";
	my $target_rpm_filename = File::Spec->catfile($repofiledir, "opennms-repo-$release-$platform.noarch.rpm");
	copy($source_rpm_filename, $target_rpm_filename) or die "Unable to copy $source_rpm_filename to $repofiledir: $!";
	print "done\n";

	return $target_rpm_filename;
}

sub install_rpm_to_repo {
	my $rpm_filename     = shift;
	my $repo             = shift;
	my $signing_id       = shift;
	my $signing_password = shift;

	print "- creating temporary repository from " . $repo->to_string . "... ";
	my $rpm = OpenNMS::YUM::RPM->new($rpm_filename);
	my $newrepo = $repo->create_temporary();
	print "done\n";

	print "- installing $rpm_filename to temporary repo... ";
	$newrepo->install_rpm($rpm, 'opennms');
	print "done\n";

	print "- reindexing temporary repo... ";
	$newrepo->index({signing_id => $signing_id, signing_password => $signing_password});
	print "done\n";

	print "- replacing repository with updated temporary repo... ";
	$newrepo->replace($repo) or die "Unable to replace " . $repo->to_string . " with " . $newrepo->to_string . "!";
	$repo->clear_cache();
	print "done\n";

	return 1;
}

sub usage {
	my $error = shift;

	print <<END;
usage: $0 [-h] [-g <gpg_id>] <-s signing_password> <base> <release> <platform>

	-h            : print this help
	-s <password> : sign the rpm using this password for the gpg key
	-g <gpg_id>   : sign using this gpg_id (default: opennms\@opennms.org)

	base          : the base directory of the YUM repository
	release       : the release tree (e.g., "stable", "unstable", "snapshot", etc.)
	platform      : the repository platform (e.g., "common", "rhel5", etc.)

END

	if (defined $error) {
		print "ERROR: $error\n\n";
	}

	exit 1;
}
