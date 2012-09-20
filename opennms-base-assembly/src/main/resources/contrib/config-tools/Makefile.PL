use inc::Module::Install;

use strict;
use warnings;

# Define metadata
name           'OpenNMS-Config';
all_from       'lib/OpenNMS/Config.pm';

# Specific dependencies
requires       'Git'                => undef;
test_requires  'Test::More'         => undef;

install_script 'script/git-metadata.pl';
install_script 'script/git-setup.pl';
install_script 'script/opennms-pretrans.pl';
install_script 'script/opennms-post.pl';
install_script 'script/opennms-posttrans.pl';

#install_share 'share';

WriteAll;
