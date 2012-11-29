#!/usr/bin/perl -w

use 5.008008;
use strict;
use warnings;

use Carp;

use OpenNMS::Config;
use OpenNMS::Config::Git;

my $config = OpenNMS::Config->new();
my $git = OpenNMS::Config::Git->new($config->etc_dir());

$git->author_name('OpenNMS Git Auto-Upgrade');
$git->author_email($0);

$git->add('.');
$git->commit('Manual merge after an upgrade conflict.');
