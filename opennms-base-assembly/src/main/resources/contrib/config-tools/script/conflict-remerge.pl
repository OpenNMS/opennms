#!/usr/bin/perl -w

use 5.008008;
use strict;
use warnings;

use Carp;

use OpenNMS::Config;
use OpenNMS::Config::Git;

my $config = OpenNMS::Config->new();
my $git = OpenNMS::Config::Git->new($config->etc_dir());

my $tag = $git->get_latest_runtime_pre_tag();
$config->log("Resetting tree to '$tag'.");
$git->reset($tag);

eval {
	$git->merge($config->pristine_branch());
};

exit 0;
