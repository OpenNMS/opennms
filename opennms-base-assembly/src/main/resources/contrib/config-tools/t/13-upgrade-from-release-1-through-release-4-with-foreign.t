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

# perform a series of complicated upgrades from 1.0-1 through 1.0-4 + foreign packages
my $testfile = File::Spec->catfile($config->etc_dir(), "testfile.conf");
my $b = File::Spec->catfile($config->etc_dir(), "b.conf");
my $foreign = File::Spec->catfile($config->etc_dir(), "foreign.conf");
my $really_foreign = File::Spec->catfile($config->etc_dir(), "really-foreign.conf");
my $touchy = File::Spec->catfile($config->etc_dir(), "postinstall-");

build_rpm("t/rpms/feature-init.spec", "target/rpm6-1");
my $rpms = build_rpm("t/rpms/feature-a-1.0-1.spec", "target/rpm6-1");
OpenNMS::Config::RPM->install(rpms => $rpms, root => "target/rpmroot");
ok(-e $testfile);
is(read_file($testfile), "o-test-feature-a-1.0-1\n\n\n");
ok(-e $touchy . '1.0-1');

$rpms = build_rpm("t/rpms/feature-a-1.0-2.spec", "target/rpm6-2");
OpenNMS::Config::RPM->install(rpms => $rpms, root => "target/rpmroot");
ok(-e $testfile);
is(read_file($testfile), "o-test-feature-a-1.0-2\n\n\n");
ok(-e $touchy . '1.0-2');
ok(! -e $touchy . '1.0-1');

write_file(File::Spec->catfile($config->etc_dir(), "testfile.conf"), {append => 1}, "blah\n");
build_rpm("t/rpms/feature-a-1.0-3.spec", "target/rpm6-3");
build_rpm("t/rpms/feature-b-1.0-3.spec", "target/rpm6-3");
$rpms = build_rpm("t/rpms/foreign-1.0-1.spec", "target/rpm6-3");
OpenNMS::Config::RPM->install(rpms => $rpms, root => "target/rpmroot");
ok(-e $testfile);
is(read_file($testfile), "o-test-feature-a-1.0-3\n\n\nblah\nHoly crap, monkeys are awesome.\n");
ok(-e $touchy . '1.0-3');
ok(! -e $touchy . '1.0-1');
ok(! -e $touchy . '1.0-2');
ok(-e $b);
ok(-e $foreign);

build_rpm("t/rpms/feature-a-1.0-4.spec", "target/rpm6-4");
build_rpm("t/rpms/feature-b-1.0-4.spec", "target/rpm6-4");
$rpms = build_rpm("t/rpms/foreign-1.0-2.spec", "target/rpm6-4");
OpenNMS::Config::RPM->install(rpms => $rpms, root => "target/rpmroot");
ok(-e $testfile);
is(read_file($testfile), "o-test-feature-a-1.0-4\n\n\nblah\nHoly crap, monkeys are awesome.\n");
ok(-e $touchy . '1.0-4');
ok(! -e $touchy . '1.0-1');
ok(! -e $touchy . '1.0-2');
ok(! -e $touchy . '1.0-3');
ok(-e $b);
ok(! -e $foreign);
ok(-e $really_foreign);

write_file($touchy . '1.0-4', {append => 1}, 'break this file!');
write_file($testfile, "o-test-feature-a-1.0-4broken\n\n\nblah\nHoly crap, monkeys are awesome.\n");
$rpms = build_rpm("t/rpms/feature-a-1.0-5.spec", "target/rpm6-5");
OpenNMS::Config::RPM->install(rpms => $rpms, root => "target/rpmroot");
ok(-e $testfile);
is(read_file($testfile), "o-test-feature-a-1.0-5\n\n\n");
ok(-e $touchy . '1.0-5');
ok(! -e $touchy . '1.0-1');
ok(! -e $touchy . '1.0-2');
ok(! -e $touchy . '1.0-3');
ok(! -e $touchy . '1.0-4');
ok(-e $touchy . '1.0-4.old');
ok(-e $testfile . '.old');
ok(-e $b);
ok(! -e $foreign);
ok(! -e $really_foreign);
ok(-e $really_foreign . '.old');
