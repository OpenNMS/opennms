use strict;
use warnings;

$|++;

use File::Path;
use File::Spec;

#use Test::More tests => 8;
use Test::More qw(no_plan);

unshift(@INC, 't');

BEGIN {
	use_ok('OpenNMS::Config');
};

require('common.pl');

my $testdir = File::Spec->catdir('target', '01');

mkpath($testdir);
my $config = OpenNMS::Config->new($testdir);

is($config->home(), $testdir, 'home should match the target dir from ->new');

eval { $config->get_branch_name(); };
ok($@, 'get_branch_name without arguments should croak');
is($config->get_branch_name('foo'), 'opennms-auto-upgrade/foo');

eval { $config->get_tag_name(); };
ok($@, 'get_tag_name without arguments should croak');
is($config->get_tag_name('foo'), 'opennms-auto-upgrade/tags/foo');

is($config->pristine_branch(), 'opennms-auto-upgrade/pristine');
is($config->runtime_branch(),  'opennms-auto-upgrade/runtime');

eval { $config->existing_version(); };
ok($@, 'existing_version without arguments should croak');
is($config->existing_version('aoeuhtaoeuaotehunaoehunaoeu'), '0.0-0', 'existing_version on an unknown package should return version 0');
is($config->existing_version('aoeuhaoetunshaoeutnaohsuaoe', '1.3-5'), '1.3-5', 'existing_version on an unknown package when a default version is specified should return the default version');

is($config->pristine_dir(), File::Spec->catdir($testdir, 'share', 'etc-pristine'));
is($config->etc_dir(),      File::Spec->catdir($testdir, 'etc'));

eval { $config->setup('a', 'b', 'c'); };
ok($@, 'setup requires 4 arguments');

my ($conf, $version, $pristinedir, $etcdir, $rpm_name, $rpm_version) = OpenNMS::Config->setup($0, $testdir, 'aoeuaoeuaoeuaoeu', '1.3-5');
is($conf->home(), $testdir);
is($version,     '0.0-0', 'there should be no existing version');
is($pristinedir, File::Spec->catdir($testdir, 'share', 'etc-pristine'));
is($etcdir,      File::Spec->catdir($testdir, 'etc'));
is($rpm_name,    'aoeuaoeuaoeuaoeu');
is($rpm_version, '1.3-5');