#!/usr/bin/perl

use Data::Dumper;
use File::Basename;
use File::Spec;

use OpenNMS::Util;
use OpenNMS::YUM::Repo;

my $base = shift @ARGV;

if (not defined $base or not -d $base) {
	print "usage: $0 <repository_base>\n\n";
	exit 1;
}

my $index_text = slurp(File::Spec->catdir(dirname($0), "generate-repo-html.pre"));

my $release_descriptions  = read_properties(File::Spec->catdir(dirname($0), "release.properties"));
my $platform_descriptions = read_properties(File::Spec->catdir(dirname($0), "platform.properties"));

my @display_order  = split(/\s*,\s*/, $release_descriptions->{order_display});
my @platform_order = split(/\s*,\s*/, $platform_descriptions->{order_display});

my $repos = OpenNMS::YUM::Repo->find_repos($base);

# convenience hash for looking up repositories
my $repo_map = {};
for my $repo (@$repos) {
	$repo_map->{$repo->release}->{$repo->platform} = $repo;
}

for my $release (@display_order) {
	next unless (exists $repo_map->{$release});

	my $release_description = $release_descriptions->{$release};

	my $repos  = $repo_map->{$release};
	my $common = $repos->{'common'};

	my $latest_rpm = $common->find_newest_rpm_by_name('opennms-core');

	$index_text .= "<h2><a name=\"$release\">$release_description</a> (current version: <a href=\"$release/common/opennms\">" . $latest_rpm->display_version . "</a>)</h2>\n";
	$index_text .= "<ul>\n";

	$index_text .= "<li>$platform_descriptions->{'common'} (<a href=\"$release/common\">browse</a>)</li>\n";

	for my $platform (@platform_order) {

		my $rpmname = "opennms-repo-$release-$platform.noarch.rpm";

		if (-e "$base/repofiles/$rpmname") {
			$index_text .= "<li><a href=\"repofiles/$rpmname\">$platform_descriptions->{$platform}</a> (<a href=\"$release/$platform\">browse</a>)</li>\n";
		} else {
			$index_text .= "<li>$platform_descriptions->{$platform} (<a href=\"$release/$platform\">browse</a>)</li>\n";
		}

	}

	$index_text .= "</ul>\n";
}

$index_text .= slurp(File::Spec->catdir(dirname($0), "generate-repo-html.post"));

open (FILEOUT, ">$base/index.html") or die "unable to write to $base/index.html: $!";
print FILEOUT $index_text;
close (FILEOUT);
