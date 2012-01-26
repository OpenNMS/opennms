#!/usr/bin/env perl

use Cwd;
use File::Basename;
use File::Spec;
use Getopt::Long qw(:config permute bundling pass_through);

use vars qw(
	$BUILD_PROFILE
	$GIT
	$HELP
	$JAVA_HOME
	$MVN
	$MAVEN_OPTS
	$PREFIX
	$TESTS
	$VERBOSE
	@ARGS
);
$BUILD_PROFILE = "default";
$HELP          = undef;
$JAVA_HOME     = undef;
$VERBOSE       = undef;
@ARGS          = ();

# If we were called from bin, remove the /bin so we're always
# rooted in the top-of-tree
if (basename($PREFIX) eq "bin") {
	my @dirs = File::Spec->splitdir($PREFIX);
	pop(@dirs);
	$PREFIX = File::Spec->catdir(@dirs);
}

# path to git executable
$GIT = $ENV{'GIT'};
if (not defined $GIT or not -x $GIT) {
	for my $dir (File::Spec->path()) {
		my $git = File::Spec->catfile($dir, 'git');
		if ($^O =~ /mswin/i) {
			$git .= '.exe';
		}
		if (-x $git) {
			$GIT = $git;
			break;
		}
	}
}
if ($GIT eq "" or ! -x "$GIT") {
	warning("Unable to locate git.");
	$GIT = undef;
}

# path to maven executable
$MVN = $ENV{'MVN'};
if (not defined $MVN or not -x $MVN) {
	$MVN = File::Spec->catfile($PREFIX, 'maven', 'bin', 'mvn');
	if ($^O =~ /mswin/i) {
		$MVN .= '.bat';
	}
}

delete $ENV{'M2_HOME'};

# maven options
$MAVEN_OPTS = $ENV{'MAVEN_OPTS'};
if (not defined $MAVEN_OPTS or $MAVEN_OPTS eq '') {
	$MAVEN_OPTS = '-XX:PermSize=512m -XX:MaxPermSize=1g -Xmx1g -XX:ReservedCodeCacheSize=512m';
}

my $result = GetOptions(
	"help|h"                    => \$HELP,
	"enable-tests|tests|test|t" => \$TESTS,
	"maven-opts|m=s"            => \$MAVEN_OPTS,
	"profile|p=s"               => \$BUILD_PROFILE,
	"java-home|java|j=s"        => \$JAVA_HOME,
	"verbose|v"                 => \$VERBOSE,
);

if (not $result) {
	error("failed to parse command-line options");
	exit 1;
}
if ($BUILD_PROFILE !~ /^(default|dir|full|fulldir)$/) {
	error("unknown --profile option, $BUILD_PROFILE, must be one of 'default', 'dir', 'full', or 'fulldir'");
	exit 1;
}

@ARGS = @ARGV;

if (defined $HELP) {
	print <<END;
usage: $0 [-h] [-j \$JAVA_HOME] [-t] [-v]

	-h/--help              this help
	-j/--java-home DIR     set \$JAVA_HOME to DIR
	-m/--maven-opts OPTS   set \$MAVEN_OPTS to OPTS
	                       (default: $MAVEN_OPTS)
	-p/--profile PROFILE   default, dir, full, or fulldir
	-t/--enable-tests      enable tests when building
	-v/--verbose           be more verbose
END
	exit 1;
}

if (not defined $JAVA_HOME) {
	debug("--java-home not passed, searching for \$JAVA_HOME");
	if (exists $ENV{'JAVA_HOME'} and -e $ENV{'JAVA_HOME'}) {
		$JAVA_HOME = $ENV{'JAVA_HOME'};
	} else {
		warning("\$JAVA_HOME is not set, things might go wonky.  Or not.");
	}
}
$ENV{'JAVA_HOME'} = $JAVA_HOME;

if (defined $TESTS) {
	debug("tests are enabled");
} else {
	debug("tests are not enabled, passing -Dmaven.test.skip.exec=true");
	unshift(@ARGS, '-Dmaven.test.skip.exec=true');
}
unshift(@ARGS, '-Djava.awt.headless=true');

if (not grep { $_ =~ /^-Dmaven.metadata.legacy/ } @ARGS) {
	unshift(@ARGS, '-Dmaven.metadata.legacy=true');
}

if (grep { $_ =~ /^-Droot.dir=/ } @ARGS) {
	debug("root.dir defined");
} else {
	debug("setting root.dir to $PREFIX");
	unshift(@ARGS, '-Droot.dir=' . $PREFIX);
}

if (grep { $_ =~ /^-Dbuild.profile=/ } @ARGS) {
	debug("build.profile defined");
} else {
	debug("setting build.profile to $BUILD_PROFILE");
	unshift(@ARGS, "-Dbuild.profile=$BUILD_PROFILE");
}


if (-r File::Spec->catfile($ENV{'HOME'}, '.opennms-buildrc')) {
	if (open(FILEIN, File::Spec->catfile($ENV{'HOME'}, '/.opennms-buildrc'))) {
		while (my $line = <FILEIN>) {
			chomp($line);
			if ($line !~ /^\s*$/ && $line !~ /^\s*\#/) {
				unshift(@ARGS, $line);
			}
		}
		close(FILEIN);
	}
}

$ENV{'MAVEN_OPTS'} = $MAVEN_OPTS;

info("JAVA_HOME = $JAVA_HOME") if (defined $JAVA_HOME and $JAVA_HOME ne "");
info("MVN = $MVN");
info("MAVEN_OPTS = $MAVEN_OPTS"); 

sub clean_git {
	my @command = ($GIT, "clean", "-fdx", ".");
	info("running:", @command);
	handle_errors_and_exit_on_failure(system(@command));
}

sub get_dependencies {
	my $directory = shift;

	my @SKIP = qw(
		org\.opennms\:jicmp-api
		org\.opennms\:jrrd-api
		org\.opennms\:rancid-api
		org\.opennms\.lib
		org\.opennms\.smslib\:smslib
	);

	my $moduledir = $PREFIX . "/" . $directory;
	my $deps = { 'org.opennms:opennms' => 1 };
	my $versions = {};
	my $cwd = getcwd;

	if (-d $moduledir) {
		my $current_module_name = undef;
		my $in_module = undef;

		chdir($moduledir);
		open(MVNRUN, "$MVN dependency:list |") or die "unable to run $MVN dependency:list in $moduledir: $!";
		LIST: while (my $line = <MVNRUN>) {
			chomp($line);
			$line =~ s/^\[[^\]]*\]\s*//;
			if (defined $in_module) {
				if ($line =~ /^$/) {
					$in_module = undef;
				} elsif ($line !~ /opennms/) {
					# skip non-opennms dependencies
				} else {
					for my $skip (@SKIP) {
						if ($line =~ /$skip/) {
							next LIST;
						}
					}
					$line =~ s/^\s*//;
					$line =~ s/\s*$//;
					my @maven_info = split(/\:/, $line);
					my $dep = $maven_info[0] . ":" . $maven_info[1];
					push(@{$deps->{$current_module_name}}, $dep);

					# next unless ($maven_info[2] eq "jar" or $maven_info[2] eq "pom");

					# do some extra checking of versions
					my $version = $maven_info[3];
					$version = $maven_info[4] if ($version eq "xsds");
					$version = $maven_info[4] if ($version eq "tests");
					if (exists $versions->{$dep}) {
						$old_version = $versions->{$dep}->{'version'};
						$old_module  = $versions->{$dep}->{'module'};
						next if ($old_module eq $current_module_name);

						if ($old_version ne $version) {
							warning("$current_module_name wants $dep version $version, but $old_module wants version $old_version");
						}
					}
					$versions->{$dep} = {
						'version' => $version,
						'module' => $current_module_name,
					};

				}
			} else {
				if ($line =~ /--- maven-dependency-plugin.*? \@ (\S+)/) {
					$current_module_name = $1;
				}
				if ($line =~ /The following files have been resolved/) {
					$in_module = $current_module_name;
				}
			}
		}
		close(MVNRUN);
		chdir($cwd);
	}

	return $deps;
}

sub handle_errors {
	my $exit = shift;
	if ($exit == 0) {
		info("finished successfully");
	} elsif ($exit == -1) {
		error("failed to execute: $!");
	} elsif ($exit & 127) {
		error("child died with signal " . ($exit & 127));
	} else {
		error("child exited with value " . ($exit >> 8));
	}
	return $exit;
}

sub handle_errors_and_exit_on_failure {
	my $exit = handle_errors(@_);
	if ($exit != 0) {
		exit ($exit >> 8);
	}
}

sub handle_errors_and_exit {
	my $exit = handle_errors(@_);
	exit ($exit >> 8);
}

sub debug {
	print "[DEBUG] " . join(' ', @_) . "\n" if ($VERBOSE);
}

sub warning {
	print "[WARN] " . join(' ', @_) . "\n";
}

sub info {
	print "[INFO] " . join(' ', @_) . "\n";
}

sub error {
	print "[ERROR] " . join(' ', @_) . "\n";
}

1;
