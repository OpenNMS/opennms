use inc::Module::Install;

# Define metadata
name           'OpenNMS-Config';
all_from       'lib/OpenNMS/Config.pm';

# Specific dependencies
requires       'Git'                => undef;
test_requires  'Test::More'         => undef;

install_script 'script/git-metadata.pl';
install_script 'script/git-setup.pl';
install_script 'script/opennms-preinstall.pl';
install_script 'script/opennms-postinstall.pl';

#install_share 'share';

WriteAll;
