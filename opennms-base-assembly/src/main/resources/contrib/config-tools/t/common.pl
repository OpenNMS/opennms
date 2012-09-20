use Carp;
use Cwd qw(abs_path);

use strict;
use warnings;

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

sub setup_rpmroot {

	rmtree('target/rpmroot');
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

	system('yum', '--installroot=' . $rpmroot, '--nogpgcheck', '-y', '--downloadonly', 'install', 'perl', 'bash', 'coreutils', 'git');
	system('yum', '--installroot=' . $rpmroot, '--nogpgcheck', '-y', 'install', 'bash', 'coreutils');

    return ($rpmroot);
}

mkpath('/tmp/yumrepo');
if (-d 'target/rpmroot/var/cache/yum') {
	system('rsync', '-avr', 'target/rpmroot/var/cache/yum/', '/tmp/yumrepo/');
}
rmtree("target");

1;