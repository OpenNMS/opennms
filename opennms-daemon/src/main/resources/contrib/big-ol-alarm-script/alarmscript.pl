#!/usr/bin/perl

use DBI;
use DBD::Pg;
use File::Copy;

my $username = 'opennms';
my $password = undef;

my $outputfile = shift(@ARGV) || 'index.html';

my $dbh = DBI->connect('dbi:Pg:dbname=opennms', $username, $password) or die "unable to connect to database: " . DBI->errstr;

my $critical = $dbh->selectall_arrayref('SELECT node.nodelabel FROM node, alarms WHERE severity > 3 AND node.nodeid=alarms.nodeid AND alarms.serviceid = 1')  or die "unable to select nodes (critical): " . $dbh->errstr;
my $warning  = $dbh->selectall_arrayref('SELECT node.nodelabel FROM node, alarms WHERE severity > 3 AND node.nodeid=alarms.nodeid AND alarms.serviceid <> 1') or die "unable to select nodes (warning): " . $dbh->errstr;

open (FILEOUT, '>' . $outputfile . '.tmp') or die "unable to write to ${outputfile}.tmp: $!";
print FILEOUT <<END;
<html>
 <head>
  <title>Alarms Dashboard</title>
  <meta http-equiv="Refresh" content="120" />
 </head>
 <body bgcolor="black">
  <table width="100%" height="100%">
   <tr>
    <td valign="middle" align="center">
     <table>
      <tr>
       <td>
        <h1><font color="white">List of Critical Nodes</font></h1>
END

if (@$critical) {
	for my $row (@$critical) {
		print FILEOUT "         <font size=\"5\" color=\"red\">$row->[0]</font><br />\n";
	}
} else {
	print FILEOUT "        <font color=\"white\">(none)</font>\n";
}

print FILEOUT "        <h1><font color=\"white\">List of Warning Nodes</font></h1>\n";

if (@$warning) {
	for my $row (@$warning) {
		print FILEOUT "         <font size=\"5\" color=\"yellow\">$row->[0]</font><br />\n";
	}
} else {
	print FILEOUT "        <font color=\"white\">(none)</font>\n";
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
