#!@install.perl.bin@

my $output;

{ local $/ = undef; $output  = <>; }

for my $crap (split(/Status\: OpenNMS\:/, $output)) {
	if ($crap =~ /^Name\=(\S+?)\s*\=\s*(\S+?)\s*[\,\]]/) {
		printf('OpenNMS.%-15s: %s' . "\n", $1, lc($2));
	}
}
