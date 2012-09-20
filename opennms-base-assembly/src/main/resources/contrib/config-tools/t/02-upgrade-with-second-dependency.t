use strict;
use warnings;

$|++;

#use Test::More tests => 8;
use Test::More qw(no_plan);

use Carp;
use Cwd qw(abs_path);
use File::Copy;
use File::Path;
use File::Slurp;
use Git;

unshift(@INC, 't');

BEGIN {
	use_ok('OpenNMS::Config');
	use_ok('OpenNMS::Config::Spec');
	use_ok('OpenNMS::Config::RPM');
};

require('common.pl');
my ($rpmroot) = setup_rpmroot ();
my $config = OpenNMS::Config->new(File::Spec->catdir($rpmroot, 'opt', 'opennms'));

# upgrade with a second dependency
build_rpm("t/rpms/feature-init.spec", "target/rpm4");
build_rpm("t/rpms/feature-a-1.0-3.spec", "target/rpm4");
my $rpms = build_rpm("t/rpms/feature-b-1.0-3.spec", "target/rpm4");
OpenNMS::Config::RPM->install(rpms => $rpms, root => "target/rpmroot");
ok(-e File::Spec->catfile($config->etc_dir(), "testfile.conf"));
ok(-e File::Spec->catfile($config->etc_dir(), "b.conf"));
assert_no_rpmnew($config->etc_dir());
is(read_file(File::Spec->catfile($config->etc_dir(), "testfile.conf")), "o-test-feature-a-1.0-3\n\n\n");
is(read_file(File::Spec->catfile($config->etc_dir(), "b.conf")), "o-test-feature-b-1.0-3\n\n\n");
