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

# create the init RPM first
my $init = build_rpm("t/rpms/feature-init.spec", "target/rpm/init");
is(@$init, 1);
$init = $init->[0];

# create the first feature RPM
my $rpm09 = build_rpm("t/rpms/feature-a-0.9-1.spec", "target/rpm/a-0.9");
is(@$rpm09, 1);
$rpm09 = $rpm09->[0];

# create the upgrade feature RPM
my $rpm10 = build_rpm("t/rpms/feature-a-1.0-1.spec", "target/rpm/a-1.0");
is(@$rpm10, 1);
$rpm10 = $rpm10->[0];

# install the first rpm on a clean system
OpenNMS::Config::RPM->install(rpms => [ $rpm09 ], root => "target/rpmroot");
ok(-e "target/rpmroot/opt/opennms/etc/testfile.conf", "clean install - testfile.conf must exist");
is(read_file("target/rpmroot/opt/opennms/etc/testfile.conf"), "o-test-feature-a-0.9-1\n\n\n", "clean install - testfile.conf must contain package name");

# install the upgrade rpm and the init package
OpenNMS::Config::RPM->install(rpms => [ $init, $rpm10 ], root => "target/rpmroot");
ok(-e "target/rpmroot/opt/opennms/bin/config-tools/opennms-post.pl", "clean install - check for opennms-post.pl");
is(read_file("target/rpmroot/opt/opennms/etc/testfile.conf"), "o-test-feature-a-1.0-1\n\n\n", "clean install - testfile.conf must contain package name");
ok(-d "target/rpmroot/opt/opennms/etc/.git", "clean install - .git directory must exist");

#my $git = Git->repository(Directory => $config->etc_dir());
#my @retval = grep { !/^#/ } $git->command('status', 'testfile.conf');
#is($retval[0], "nothing to commit (working directory clean)");
#
#my $output = $git->command_oneline('diff', $config->pristine_branch());
#is($output, undef);
#
## upgrade an RPM with a config and no user changes
#$rpms = build_rpm("t/rpms/feature-a-1.0-2.spec", "target/rpm2");
#OpenNMS::Config::RPM->install(rpms => $rpms, root => "target/rpmroot");
#ok(-e File::Spec->catfile($config->etc_dir(), "testfile.conf"));
#assert_no_rpmnew($config->etc_dir());
#is(read_file(File::Spec->catfile($config->etc_dir(), "testfile.conf")), "o-test-feature-a-1.0-2\n\n\n");
#
## upgrade an RPM with a config and user changes
#$rpms = build_rpm("t/rpms/feature-a-1.0-3.spec", "target/rpm3");
#write_file(File::Spec->catfile($config->etc_dir(), "testfile.conf"), {append => 1}, "blah\n");
#OpenNMS::Config::RPM->install(rpms => $rpms, root => "target/rpmroot");
#ok(-e File::Spec->catfile($config->etc_dir(), "testfile.conf"));
#assert_no_rpmnew($config->etc_dir());
#is(read_file(File::Spec->catfile($config->etc_dir(), "testfile.conf")), "o-test-feature-a-1.0-3\n\n\nblah\n");
