use strict;
use warnings;

$|++;

use File::Path;
use File::Slurp;
use File::Spec;
use Git;

#use Test::More tests => 8;
use Test::More qw(no_plan);

unshift(@INC, 't');

BEGIN {
	use_ok('OpenNMS::Config::Git');
};

require('common.pl');

my $testdir = File::Spec->catdir('target', '02');

eval { OpenNMS::Config::Git->new(); };
ok($@, '->new with no arguments should croak');

my $git = OpenNMS::Config::Git->new($testdir);
ok(! -d $testdir, '$testdir should not exist');
is($git->dir(), $testdir);
$git->init();
ok(-d $testdir, 'init should have created $testdir');

my $rawgit = Git->repository(Directory => $testdir);
my $current_branch = $rawgit->command_oneline('branch');
# no branch has been created yet, because there are no commits
is($current_branch, undef);

is($git->author_name(), 'Unknown');
is($git->author_email(), 'unknown@opennms-config-git.pl');

write_file(File::Spec->catfile($testdir, 'foo.txt'), 'foo');
$git->add('.');
$git->commit('add foo');

$current_branch = $rawgit->command_oneline('branch');
is($current_branch, '* master');

rmtree($testdir) unless (! -d $testdir);

# try again, but with different authors and branch name
$git->init(
	author_name => 'Horatio',
	author_email => 'ho@rat.io',
	branch_name => 'funky',
);
ok(-d $testdir, 'init should have re-created $testdir');

is($git->author_name(), 'Horatio');
is($git->author_email(), 'ho@rat.io');

my $foo = File::Spec->catfile($testdir, 'foo.txt');
my $bar = File::Spec->catfile($testdir, 'bar.txt');

write_file($foo, 'foo');
$git->add('.');
$git->commit('re-add foo');

$current_branch = $rawgit->command_oneline('branch');
is($current_branch, '* funky');

is($git->get_index_status('foo.txt'), 'unchanged');
is($git->get_index_status('bar.txt'), 'unchanged');
write_file($bar, 'bar');
is($git->get_index_status('bar.txt'), 'untracked');
$git->add('bar.txt');
is($git->get_index_status('bar.txt'), 'new');
$git->commit('add bar');
is($git->get_index_status('bar.txt'), 'unchanged');
write_file($bar, 'MORE bar');
is($git->get_index_status('bar.txt'), 'unchanged');
$git->add('bar.txt');
is($git->get_index_status('bar.txt'), 'modified');
unlink($bar);
is($git->get_index_status('bar.txt'), 'modified');
$git->rm('bar.txt');
is($git->get_index_status('bar.txt'), 'deleted');

rmtree('target') unless (! -d 'target');

$git = OpenNMS::Config::Git->new("$testdir");
$git->init(branch_name => 'pristine');
ok(-d "$testdir/.git");
is($git->get_branch_name(), 'pristine');

$git->author_name('git.t');
$git->author_email('t/git.t');
is($git->author_name(), 'git.t');
is($git->author_email(), 't/git.t');

write_file("$testdir/new.txt", 'This file is new!');

$git->add("new.txt");
# new, modified, untracked, committed, deleted
is($git->get_index_status("new.txt"), 'new');

write_file("$testdir/untracked.txt", 'This file is untracked.');
is($git->get_index_status('untracked.txt'), 'untracked');

my @changes = $git->get_modifications();
is(scalar(@changes), 2);
is($changes[0]->file(), "new.txt");
is($changes[1]->file(), "untracked.txt");

eval {
	$git->commit();
	fail("commit should have failed");
};
ok($@);

$git->commit("adding new.txt");
is($git->get_index_status("new.txt"), 'unchanged');
@changes = $git->get_modifications();
is(scalar(@changes), 1);
is($changes[0]->file(), 'untracked.txt');
ok($changes[0]->isa('OpenNMS::Config::Git::Add'));

$git->create_branch('master', 'pristine');
is($git->get_branch_name(), 'pristine');

$git->add('untracked.txt');
$git->commit('add previously untracked file');

$git->checkout('master');
is($git->get_branch_name(), 'master');
ok(! -e "$testdir/untracked.txt");

$git->merge('pristine');
ok(-e "$testdir/untracked.txt");
is($git->get_index_status('untracked.txt'), 'unchanged');

$git->tag('bogus-tag');
eval {
	$git->tag('bogus-tag');
	fail("yo dude, that tag should have existed");
};
ok($@);

write_file("$testdir/added.txt");
unlink("$testdir/untracked.txt");
@changes = $git->get_modifications();
is(scalar(@changes), 2);
for my $change (@changes) {
	$change->exec();
}
$git->commit('add a file, remove a file');
@changes = $git->get_modifications();
is(scalar(@changes), 0);

unlink("$testdir/added.txt");
write_file("$testdir/monkey.txt");
write_file("$testdir/new.txt", {append => 1}, "\nsomething else\n");
@changes = $git->get_modifications();
is(scalar(@changes), 3);
$git->commit_modifications('added a file, removed a file, modified a file');
@changes = $git->get_modifications();
is(scalar(@changes), 0);

sub create_runtime_test_env {
	rmtree('target') unless (! -d 'target');
	$git->init(
		author_name => 'Horatio',
		author_email => 'ho@rat.io',
		branch_name => 'pristine',
	);
	write_file("$testdir/foo.txt", "This is foo.\n");
	$git->add('.');
	$git->commit('add foo');
	$git->create_branch('runtime', 'pristine');
	$git->checkout('runtime');
	write_file("$testdir/foo.txt", "Crikey!\n");
	$git->add('.');
	$git->commit('change runtime version of foo');
	$git->checkout('pristine');
	write_file("$testdir/foo.txt", "This is foo, suckas!\n");
	$git->add('.');
	$git->commit('change pristine version of foo');
	$git->checkout('runtime');
}

create_runtime_test_env();

eval { $git->merge(); };
ok($@, 'Merge with no arguments should croak.');
eval { $git->merge('pristine'); };
is($git->get_index_status('foo.txt'), 'modified');
my @mods = $git->get_modifications();
is(scalar @mods, 1);

create_runtime_test_env();
eval { $git->merge_or_fail('pristine'); };
is($git->get_index_status('foo.txt'), 'modified');
@mods = $git->get_modifications();
is(scalar @mods, 1);

$git->reset('runtime');
is($git->get_index_status('foo.txt'), 'unchanged');
@mods = $git->get_modifications();
is(scalar @mods, 0);
is(read_file("$testdir/foo.txt"), "Crikey!\n");

create_runtime_test_env();
eval { $git->merge_or_fail('pristine'); };
$git->reset('runtime');
$git->save_changes_compared_to('pristine');
@mods = $git->get_modifications();
is(scalar @mods, 2);
is(read_file("$testdir/foo.txt"), "This is foo, suckas!\n");
ok(-e "$testdir/foo.txt.old");
is(read_file("$testdir/foo.txt.old"), "Crikey!\n");
