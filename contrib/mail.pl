#!/usr/bin/perl

#
# Simple perl script for sending email via remote SMTP server. Will attempt to use a backup
# server if one is provided.
#
# Usage: mail.pl <to> <from> <subject> <server> <backupserver>
# Message body is streamed through STDIN.
#

use Net::SMTP;

$to = $ARGV[0];
$from = $ARGV[1];
$subject = $ARGV[2];
$server = $ARGV[3];
$backupserver = $ARGV[4];

$numargs = @ARGV;
if ($numargs < 4) {
    print "Usage: mail.pl <to> <from> <subject> <server> [backupserver]\n";
    exit 1;
} 

@body = <STDIN>;

&sendmail($to,$from,$subject,$server,\@body);

sub sendmail {
    local $stop = 0;
    my ($myto,$myfrom,$mysubject,$myserver) = @_;
    my @mybody = @{$_[4]};
    my $smtp = Net::SMTP->new($myserver);

    if (!$smtp) {
        $stop = 1;
        if ($backupserver && $myserver ne $backupserver) {
            &sendmail($to,$from,$subject,$backupserver,\@body);
        } else {
            die "Unable to send mail via specified mail server(s).";
        }
    } 

    if (!$stop) {
        $smtp->mail($myfrom);
        $smtp->to($myto);
        $smtp->data();
        $smtp->datasend("To: $myto\n");
        $smtp->datasend("From: $myfrom\n");
        $smtp->datasend("Subject: $mysubject\n");
        $smtp->datasend("\n");
   
        foreach $line (@body) {
            $smtp->datasend("$line");
        }
        $smtp->dataend();
        $smtp->quit;
    } 
}
