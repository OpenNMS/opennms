#!/usr/bin/perl

use DBI;
use DBD::Pg;
use File::Copy;

my $username = 'opennms';
my $password = undef;

my $outputfile = shift(@ARGV) || 'index.html';

my $dbh = DBI->connect('dbi:Pg:dbname=opennms', $username, $password) or die "unable to connect to database: " . DBI->errstr;

my $unix_critical = $dbh->selectall_arrayref('SELECT DISTINCT node.nodelabel FROM node, alarms WHERE severity > 3 AND node.nodeid=alarms.nodeid AND alarms.eventuei like \'%nodeDown%\' and node.nodeid in (select distinct nodeid from category_node where categoryid in (select categoryid from categories where categoryname=\'unix\' or categoryname=\'network\' or categoryname=\'storage\'))')  or die "unable to select nodes (critical): " . $dbh->errstr;

my $unix_warning  = $dbh->selectall_arrayref('SELECT DISTINCT node.nodelabel FROM node, alarms WHERE severity > 3 AND node.nodeid=alarms.nodeid AND alarms.serviceid <> 1 and node.nodeid in (select distinct nodeid from category_node where categoryid in (select categoryid from categories where categoryname=\'unix\' or categoryname=\'network\' or categoryname=\'storage\'))') or die "unable to select nodes (warning): " . $dbh->errstr;

my $nonunix_critical = $dbh->selectall_arrayref('SELECT DISTINCT node.nodelabel FROM node, alarms WHERE severity > 3 AND node.nodeid=alarms.nodeid AND alarms.eventuei like \'%nodeDown%\' and node.nodeid in (select distinct nodeid from category_node where categoryid in (select categoryid from categories where categoryname=\'ntservers\' or categoryname=\'netware\' or categoryname=\'storage\'))')  or die "unable to select nodes (critical): " . $dbh->errstr;

my $nonunix_warning  = $dbh->selectall_arrayref('SELECT DISTINCT node.nodelabel FROM node, alarms WHERE severity > 3 AND node.nodeid=alarms.nodeid AND alarms.serviceid <> 1 and node.nodeid in (select distinct nodeid from category_node where categoryid in (select categoryid from categories where categoryname=\'ntservers\' or categoryname=\'netware\'))') or die "unable to select nodes (warning): " . $dbh->errstr;

my $localt = localtime( );

open (FILEOUT, '>' . $outputfile . '.tmp') or die "unable to write to ${outputfile}.tmp: $!";
print FILEOUT <<END;
<html>
 <head>
  <title>Alarms Dashboard</title>
  <meta http-equiv="Refresh" content="60" />
 </head>
 <body bgcolor="white" style="font-family:lucida grande,verdana,sans-serif">
  <table width="100%" height="100%">
   <tr>
    <td valign="top">
     <table>
      <tr>
       <td>
        <h1 style="color:darkblue;font-size:250%;margin:0px 0px">OpenNMS Problem Summaries</h1>
        <p style="color:darkblue;font-size:150%;margin:0px 0px">
          $localt
        </p>
        <hr style="color:darkblue" />
        <h2 style="color:black;font-size:250%">UNIX, Network and Storage Devices</h2>
END

if (@$unix_critical) {
	for my $row (@$unix_critical) {
		print FILEOUT "         <p style=\"color:red;font-size:250%;margin:0px 0px\">$row->[0]</p>\n";
	}
}
if (@$unix_warning) {
	for my $row (@$unix_warning) {
		print FILEOUT "         <p style=\"color:orange;font-size:250%;margin:0px 0px\">$row->[0]</p>\n";
	}
}
if (not @$unix_critical and not @$unix_warning) {
	print FILEOUT "        <p style=\"color:green;font-size:250%\">No UNIX Problems Reported</font>\n";
}

print FILEOUT <<END;
        <h2 style="color:black;font-size:300%;font-size:250%">Non-UNIX Devices</h2>
       </td>
      </tr>
      <tr>
       <td>
END

if (@$nonunix_critical) {
	for my $row (@$nonunix_critical) {
		print FILEOUT "         <p style=\"color:red;font-size:250%;margin:0px 0px\">$row->[0]</p>\n";
	}
}
if (@$nonunix_warning) {
	for my $row (@$nonunix_warning) {
		print FILEOUT "         <p style=\"color:orange;font-size:250%;margin:0px 0px\">$row->[0]</p>\n";
	}
}
if (not @$nonunix_critical and not @$nonunix_warning) {
	print FILEOUT "        <p style=\"color:green;font-size:250%\">No Non-UNIX Problems Reported</font>\n";
}

print FILEOUT <<END;
       </td>
      </tr>
     </table>
    </td>
   </tr>
  </table>
 </body>
</html>
END

close (FILEOUT);

move($outputfile . '.tmp', $outputfile) or die "unable to move ${outputfile}.tmp to $outputfile: $!";
