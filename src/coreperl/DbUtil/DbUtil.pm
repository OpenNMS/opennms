package OpenNMS::DbUtil;

use 5.006;
use strict;
use warnings;
use DBI;
use XML::DOM;

require Exporter;

our @ISA = qw(Exporter DBI);

our $VERSION = '1.01';

sub connect {

	my ($class) = shift;
	my $self = {};

	my $xmldbfile = '@install.etc.dir@/opennms-database.xml'; # supporto DB XML

	my $dbname = 'opennms';
	my $dbhostname = 'localhost';
	my $dbport = 5432;
	my $dbuser = 'opennms' ;
	my $dbpass = 'opennms' ;

        my $parser = XML::DOM::Parser->new();
        my $doc = $parser->parsefile($xmldbfile);
        foreach my $database ($doc->getElementsByTagName('database')){
                $dbname = $database->getAttribute('name');
                foreach my $driver ($database->getElementsByTagName('driver')){
                        my @dbhost = split(":",$driver->getAttribute('url'));
                        $_ = $dbhost[2];
                        s/\/\///;
                        $dbhostname = $_;
                        my @tmpport = split("/",$dbhost[3]);
                        $dbport = $tmpport[0];
                        foreach my $param ($driver->getElementsByTagName('param')){
                                my $name = $param->getAttribute('name');
                                my $value = $param->getAttribute('value');
                                $dbuser = $value if ($name eq 'user');
                                $dbpass = $value if ($name eq 'password');
                        }
                }
        }
	DBI->connect("dbi:Pg:dbname=$dbname;host=$dbhostname;port=$dbport","$dbuser","$dbpass")
                or die "Error Connecting: $DBI::errstr";
}

1;
__END__
# Below is stub documentation for your module. You'd better edit it!

=head1 NAME

OpenNMS::DbUtil - DBI interface to OpenNMS DB  

=head1 SYNOPSIS

  use OpenNMS::DbUtil;
  $dbh = OpenNMS::DbUtil->connect();
  $sth = $dbh->prepare('SQLSTATEMENT');
  $dbh Inherits all method of DBI class

=head1 ABSTRACT

  OpenNMS::DbUtil is a module to automatically connect to OpenNMS DB using Perl DBI interface.  
  It Inherits all methods of DBI.
  It overwrites only the constructor class connect to automatically
  retrieve db connection string fron OpenNMS configuration files. 

=head1 DESCRIPTION

  OpenNMS::DbUtil is a module to automatically connect to OpenNMS DB using DBI interface.  
  It Inherits all methods of DBI.
  It overwrites only the constructor class connect to automatically
  retrieve db connection string fron OpenNMS configuration files. 

=head2 EXPORT

None by default.

=head1 SEE ALSO

DBI module

=head1 AUTHOR

Antonio Russo, rssntn67@yahoo.it

=head1 COPYRIGHT AND LICENSE

Copyright 2004 by Antonio Russo rssntn67@yahoo.it

This library is free software; you can redistribute it and/or modify
it under the same terms as Perl itself. 

=cut
