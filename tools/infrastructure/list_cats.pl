#!${install.perl.bin} -w
use strict;

my $xml_data_file = shift(@ARGV) or die "no file given!\n";
my @targets;
my $lookin = 1;

open (FILEIN, $xml_data_file) or die "unable to open $xml_data_file for reading: $!\n";
while (<FILEIN>) {
	if ($lookin) {
                $lookin = 0 if (/<name>[Aa]vailability<\/name>/i);
		next;
	}
	$lookin = 1 if (/<\/view>/);
        if (/<label>(<\!\[CDATA\[)?([a-z&; ]+)(\]\]>)?<\/label>/i) {
                my $category=$2;
                $category =~ s/\&amp;/\&/g;
                $category =~ s/\&quot;/"/g;
                $category =~ s/\&apos;/'/g;
                push (@targets, $category);
	}
}
close (FILEIN);

for (@targets) {
	print $_, "\n";
}
