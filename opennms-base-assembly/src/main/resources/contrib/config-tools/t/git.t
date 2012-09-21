use strict;
use warnings;

$|++;

use Error qw(:try);
use File::Path;
use File::Slurp;

#use Test::More tests => 8;
use Test::More qw(no_plan);

BEGIN {
	use_ok('OpenNMS::Config::Git');
};

rmtree("target");

my $git = OpenNMS::Config::Git->new("target/git-test/repo");
$git->init(branch_name => 'pristine');
ok(-d "target/git-test/repo/.git");
is($git->get_branch_name(), 'pristine');

$git->author_name('git.t');
$git->author_email('t/git.t');
is($git->author_name(), 'git.t');
is($git->author_email(), 't/git.t');

write_file('target/git-test/repo/new.txt', 'This file is new!');

$git->add("new.txt");
# new, modified, untracked, committed, deleted
is($git->get_index_status("new.txt"), 'new');

write_file('target/git-test/repo/untracked.txt', 'This file is untracked.');
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
ok(! -e 'target/git-test/repo/untracked.txt');

$git->merge('pristine');
ok(-e 'target/git-test/repo/untracked.txt');
is($git->get_index_status('untracked.txt'), 'unchanged');

$git->tag('bogus-tag');
eval {
	$git->tag('bogus-tag');
	fail("yo dude, that tag should have existed");
};
ok($@);

write_file('target/git-test/repo/added.txt');
unlink('target/git-test/repo/untracked.txt');
@changes = $git->get_modifications();
is(scalar(@changes), 2);
for my $change (@changes) {
	$change->exec();
}
$git->commit('add a file, remove a file');
@changes = $git->get_modifications();
is(scalar(@changes), 0);

unlink('target/git-test/repo/added.txt');
write_file('target/git-test/repo/monkey.txt');
write_file('target/git-test/repo/new.txt', {append => 1}, "/nsomething else\n");
@changes = $git->get_modifications();
is(scalar(@changes), 3);
$git->commit_modifications('added a file, removed a file, modified a file');
@changes = $git->get_modifications();
is(scalar(@changes), 0);
