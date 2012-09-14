$|++;

use strict;
use warnings;

#use Test::More tests => 8;
use Test::More qw(no_plan);

use Carp;
use Cwd qw(abs_path);
use File::Copy;
use File::Path;
use File::Slurp;
use Git;

BEGIN {
	use_ok('OpenNMS::Config');
	use_ok('OpenNMS::Config::Spec');
	use_ok('OpenNMS::Config::RPM');
};

sub build_rpm {
	my $specfile = shift;
	my $topdir   = shift || "target/rpm";
	my $spec = OpenNMS::Config::Spec->new($specfile);

	chomp(my $contents = read_file('MANIFEST'));
	my @files = split(/\n/, $contents);
	print STDERR "\nfiles = " . join(', ', @files) . "\n";
	system('tar', '-cvzf', 'target/perlfiles.tar.gz', @files) == 0 or croak "unable to build tarball: $!";
	$spec->add_source("perlfiles.tar.gz", 'target/perlfiles.tar.gz');
	return $spec->build(_topdir => $topdir);
}

sub assert_no_rpmnew {
	my $path = shift;
	
	opendir(DIR, $path) or die "unable to read from directory $path: $!";
	for my $entry (readdir(DIR)) {
		ok($entry !~ /\.(rpmnew|rpmsave)$/, "unexpected entry $entry in $path");
	}
	closedir(DIR) or die "unable to close directory $path: $!";
}

mkpath('/tmp/yumrepo');
if (-d 'target/rpmroot/var/cache/yum') {
	system('rsync', '-avr', 'target/rpmroot/var/cache/yum/', '/tmp/yumrepo/');
}
rmtree("target");
my ($rpmroot) = setup_rpmroot ();
my $config = OpenNMS::Config->new(File::Spec->catdir($rpmroot, 'opt', 'opennms'));

# create the init RPM first
build_rpm("t/rpms/feature-init.spec", "target/rpm");

# create a feature RPM
my $rpms = build_rpm("t/rpms/feature-a-1.0-1.spec", "target/rpm");
is(@$rpms, 2);

# install a feature RPM with a config on a clean system
OpenNMS::Config::RPM->install(rpms => $rpms, root => "target/rpmroot");
ok(-e "target/rpmroot/opt/opennms/etc/testfile.conf", "clean install - testfile.conf must exist");
ok(-e "target/rpmroot/opt/opennms/bin/config-tools/opennms-post.pl", "clean install - check for opennms-post.pl");
is(read_file("target/rpmroot/opt/opennms/etc/testfile.conf"), "o-test-feature-a-1.0-1\n\n\n", "clean install - testfile.conf must contain package name");
ok(-d "target/rpmroot/opt/opennms/etc/.git", "clean install - .git directory must exist");

my $git = Git->repository(Directory => $config->etc_dir());
my @retval = grep { !/^#/ } $git->command('status', 'testfile.conf');
is($retval[0], "nothing to commit (working directory clean)");

my $output = $git->command_oneline('diff', $config->pristine_branch());
is($output, undef);

# upgrade an RPM with a config and no user changes
$rpms = build_rpm("t/rpms/feature-a-1.0-2.spec", "target/rpm2");
OpenNMS::Config::RPM->install(rpms => $rpms, root => "target/rpmroot");
ok(-e File::Spec->catfile($config->etc_dir(), "testfile.conf"));
assert_no_rpmnew($config->etc_dir());
is(read_file(File::Spec->catfile($config->etc_dir(), "testfile.conf")), "o-test-feature-a-1.0-2\n\n\n");

#exit 0;

# upgrade an RPM with a config and user changes
$rpms = build_rpm("t/rpms/feature-a-1.0-3.spec", "target/rpm3");
write_file(File::Spec->catfile($config->etc_dir(), "testfile.conf"), {append => 1}, "blah\n");
OpenNMS::Config::RPM->install(rpms => $rpms, root => "target/rpmroot");
ok(-e File::Spec->catfile($config->etc_dir(), "testfile.conf"));
assert_no_rpmnew($config->etc_dir());
is(read_file(File::Spec->catfile($config->etc_dir(), "testfile.conf")), "o-test-feature-a-1.0-3\n\n\nblah\n");

rmtree($rpmroot);
$rpmroot = setup_rpmroot();

build_rpm("t/rpms/feature-init.spec", "target/rpm4");
build_rpm("t/rpms/feature-a-1.0-3.spec", "target/rpm4");
$rpms = build_rpm("t/rpms/feature-b-1.0-3.spec", "target/rpm4");
OpenNMS::Config::RPM->install(rpms => $rpms, root => "target/rpmroot");
ok(-e File::Spec->catfile($config->etc_dir(), "testfile.conf"));
ok(-e File::Spec->catfile($config->etc_dir(), "b.conf"));
assert_no_rpmnew($config->etc_dir());
is(read_file(File::Spec->catfile($config->etc_dir(), "testfile.conf")), "o-test-feature-a-1.0-3\n\n\n");
is(read_file(File::Spec->catfile($config->etc_dir(), "b.conf")), "o-test-feature-b-1.0-3\n\n\n");

rmtree($rpmroot);
$rpmroot = setup_rpmroot();

build_rpm("t/rpms/feature-init.spec", "target/rpm5");
build_rpm("t/rpms/feature-a-1.0-3.spec", "target/rpm5");
build_rpm("t/rpms/foreign-1.0-1.spec", "target/rpm5");
$rpms = build_rpm("t/rpms/feature-b-1.0-3.spec", "target/rpm5");
OpenNMS::Config::RPM->install(rpms => $rpms, root => "target/rpmroot");
ok(-e File::Spec->catfile($config->etc_dir(), "testfile.conf"));
ok(-e File::Spec->catfile($config->etc_dir(), "b.conf"));
assert_no_rpmnew($config->etc_dir());
is(read_file(File::Spec->catfile($config->etc_dir(), "testfile.conf")), "o-test-feature-a-1.0-3\n\n\nHoly crap, monkeys are awesome.\n");
is(read_file(File::Spec->catfile($config->etc_dir(), "b.conf")), "o-test-feature-b-1.0-3\n\n\n");
is(read_file(File::Spec->catfile($config->etc_dir(), "foreign.conf")), "o-test-foreign-1.0-1\n\n\n");

rmtree($rpmroot);
$rpmroot = setup_rpmroot();
my $testfile = File::Spec->catfile($config->etc_dir(), "testfile.conf");
my $b = File::Spec->catfile($config->etc_dir(), "b.conf");
my $foreign = File::Spec->catfile($config->etc_dir(), "foreign.conf");
my $really_foreign = File::Spec->catfile($config->etc_dir(), "really-foreign.conf");
my $touchy = File::Spec->catfile($config->etc_dir(), "postinstall-");

build_rpm("t/rpms/feature-init.spec", "target/rpm6-1");
$rpms = build_rpm("t/rpms/feature-a-1.0-1.spec", "target/rpm6-1");
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
is(read_file($testfile), <<END);
<<<<<<< HEAD
o-test-feature-a-1.0-4broken
=======
o-test-feature-a-1.0-5
>>>>>>> opennms-auto-upgrade/pristine


blah
Holy crap, monkeys are awesome.
END
ok(-e $touchy . '1.0-5');
ok(! -e $touchy . '1.0-1');
ok(! -e $touchy . '1.0-2');
ok(! -e $touchy . '1.0-3');
#ok(! -e $touchy . '1.0-4');
ok(-e $b);
ok(! -e $foreign);
ok(-e $really_foreign);


sub setup_rpmroot {

	mkpath("target/rpmroot/etc/yum.repos.d");
	mkpath('target/rpmroot/var/cache/yum');
	system('rsync', '-avr', '/tmp/yumrepo/', 'target/rpmroot/var/cache/yum/');

	my $rpmroot = abs_path("target/rpmroot");

	copy("/etc/yum.conf", "target/rpmroot/etc/yum.conf");
	write_file('target/rpmroot/etc/yum.repos.d/CentOS-Base.repo', <<END);
[base]
name=CentOS-5 - Base
mirrorlist=http://mirrorlist.centos.org/?release=5&arch=x86_64&repo=os
gpgcheck=0

[epel]
name=Extra Packages for Enterprise Linux 5 - x86_64
mirrorlist=http://mirrors.fedoraproject.org/mirrorlist?repo=epel-5&arch=x86_64
failovermethod=priority
gpgcheck=0
END

	system('yum', '--installroot=' . $rpmroot, '--nogpgcheck', '-y', '--downloadonly', "install", "perl", "bash", 'git');
	system('yum', '--installroot=' . $rpmroot, '--nogpgcheck', '-y', "install", "bash");

    return ($rpmroot);
}
