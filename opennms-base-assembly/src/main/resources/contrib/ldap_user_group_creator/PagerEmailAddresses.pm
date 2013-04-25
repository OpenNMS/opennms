package PagerEmailAddresses;

# This file contains a mapping of usernames to pager
# e-mail addresses, since the directory at the site
# where this set of scripts was developed lacked an
# LDAP attribute indicating this bit of information.

BEGIN {
	use Exporter ();
	our (@ISA, @EXPORT);
	@ISA = qw( Exporter );
	@EXPORT = qw( %pagerEmail );
}
our @EXPORT;

our %pagerEmail = (	'alice' => '2125551212@txt.att.net',
			'bob' => '7798123456@orange.net',
			'charlie' => '017668675309@o2online.de'	);

# We are a module and must return a value of 1
1;
