#!/usr/bin/perl

use strict;
use Net::LDAP;

use PagerEmailAddresses;

if (scalar @ARGV < 5) {
    print "Usage: $0 [ldap host] [bind dn] [bind-password] [base dn] [filter]\n";
    exit 1;
}

my $ldap_host = $ARGV[0];
my $bind_dn = $ARGV[1];
my $bind_password = $ARGV[2];
my $base_dn = $ARGV[3];
my $filter = $ARGV[4];

my $ldap = Net::LDAP->new( $ldap_host ) or die "$@";

my $mesg = $ldap->bind( $bind_dn, password => $bind_password );

$mesg = $ldap->search( # perform a search
               base   => $base_dn,
               filter => $filter,
               attrs => [ 'sAMAccountName', 'mail', 'givenName', 'sn' ]
             );

$mesg->code && die $mesg->error;

foreach my $entry ($mesg->entries) {
    my $username = lc $entry->get_value( 'sAMAccountName' );
    my $givenname = $entry->get_value( 'givenName' );
    my $surname = $entry->get_value( 'sn' );
    my $email = $entry->get_value( 'mail' );
    my $pager = $pagerEmail{ $username };
    print <<EOR;
        <user read-only="true">
            <user-id>${username}</user-id>
            <full-name>${givenname} ${surname}</full-name>
            <user-comments>Do not edit. Provisioned automatically via LDAP.</user-comments>
            <password>this user record is not used for authentication</password>
            <contact type="email" info="${email}"/>
            <contact type="pagerEmail" info="${pager}"/>
        </user>
EOR
}

$mesg = $ldap->unbind;   # take down session
