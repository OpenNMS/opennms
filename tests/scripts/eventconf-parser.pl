#!/usr/bin/perl -w

# a script to read an eventconf.xml and do some basic
# validation, then optionally sort and export

$|++;

use strict;
use Text::Wrapper;
use XML::XPath;
use XML::XPath::XMLParser;

use vars qw(
	$wrapper
	$infile
	$warnings
	$xp
	@args

	%UEI_LIST
	@UEI_ORDERED
	@UEI_DEFAULT
	@UEI_ALL

	$MACHINE
	$UEILIST
);

$wrapper  = Text::Wrapper->new(columns => 60, body_start => '');
$warnings = 0;
$MACHINE  = 0;
$UEILIST  = 0;

my @args;
for my $arg (@ARGV) {
	if ($arg eq "-m") {
		$MACHINE = 1; next;
	}
	if ($arg eq "-u") {
		$UEILIST = 1; next;
	}
	if ($arg eq "-h") {
		print_help();
		exit;
	}
	push(@args, $arg);
}

$infile   = '/opt/OpenNMS/etc/eventconf.xml';
$infile   = shift @args if ($args[0]);

print STDERR "parsing $infile...\n";
$xp = XML::XPath->new(filename => $infile);

for my $event ($xp->find('//event')->get_nodelist) {
  my $uei          =  $event->find('uei')->string_value;
  my $eventlabel   =  $event->find('event-label')->string_value;

  if (not defined $eventlabel or $eventlabel =~ /^\s*$/) {
    $eventlabel = $uei;
    $eventlabel =~ s#^http://(uei\.)?##;
    $eventlabel =~ s#/+# #gs;
    $eventlabel =~ s#products bluebird ##g;
    $eventlabel =~ s#opennms\.(com|net|org)#OpenNMS-defined#gs;
    $eventlabel =~ s#cisco\.com#Cisco-defined#gs;
    $eventlabel =~ s#(.+-defined \w+?)s?\b#$1 event:#;
  }

  if (exists $UEI_LIST{$uei}) {
    print STDERR "WARNING: $uei already parsed!  Skipping...\n";
    $warnings++;
    next;
  }

  my ($descr, $logmsg, $severity, $operinstruct, $mouseover,
      $snmp, $mask, $correlation, $autoaction, $operaction, $logdest);

  $descr        =  $event->findnodes('descr')->[0];
  if (not defined $descr) {
    print STDERR "WARNING: $uei has no descr!\n";
  } else {
    $descr      =  XML::XPath::XMLParser::as_string($descr);
    $descr      =~ s/^\s*<descr[^>]*>\s*//s;
    $descr      =~ s/\s*<\/descr[^>]*>\s*$//s;
  }
  $logmsg       =  $event->findnodes('logmsg')->[0];
  if (not defined $logmsg) {
    print STDERR "WARNING: $uei has no logmsg!\n";
  } else {
    $logmsg       = XML::XPath::XMLParser::as_string($logmsg);
    $logmsg       =~ s/^\s*<logmsg[^>]*>\s*//s;
    $logmsg       =~ s/\s*<\/logmsg[^>]*>\s*$//s;
    $logmsg       =~ s/[\r\n\s]+/ /gs;
    if ($logmsg ne "") {
      $logdest    =  $event->findnodes('logmsg')->[0]->getAttribute('dest');
      $logdest    =  'logndisplay' if ($logdest eq '');
    }
  }
  $severity     =  $event->find('severity')->string_value;
  $operinstruct =  $event->find('operinstruct')->string_value;
  $mouseover    =  $event->find('mouseovertext')->string_value;
  $snmp         =  $event->find('snmp')->get_nodelist;
  $mask         =  $event->findnodes('mask')->[0];
  $correlation  =  $event->findnodes('correlation')->[0];
  $autoaction   =  $event->findnodes('autoaction')->[0];
  $operaction   =  $event->findnodes('operaction')->[0];

  # snmp / mask events may need preserved order
  if ($snmp or $mask) {
    push(@UEI_ORDERED, $uei);
  }
  # default events need to go at the end
  elsif ($uei =~ m#(uei|http)\:\/\/[^/]+\/default\/#) {
    push(@UEI_DEFAULT, $uei);
  }

  $UEI_LIST{$uei} = {
    label         => clean_text($eventlabel),
    descr         => clean_text($descr),
    logmsg        => clean_text($logmsg),
    logdest       => clean_text($logdest),
    severity      => clean_text($severity),
    operinstruct  => clean_text($operinstruct),
    mouseovertext => clean_text($mouseover),
  };

  if ($correlation) {
    $UEI_LIST{$uei}->{correlation} = {
      cuei   => clean_text(XML::XPath::XMLParser::as_string($correlation->findnodes('cuei')->[0])),
      cmin   => clean_text(XML::XPath::XMLParser::as_string($correlation->findnodes('cmin')->[0])),
      cmax   => clean_text(XML::XPath::XMLParser::as_string($correlation->findnodes('cmax')->[0])),
      ctime  => clean_text(XML::XPath::XMLParser::as_string($correlation->findnodes('ctime')->[0])),
      state  => $correlation->getAttribute('state'),
      path   => $correlation->getAttribute('path'),
    }
  }

  if ($autoaction) {
    $UEI_LIST{$uei}->{autoaction} = {
      state  => $autoaction->getAttribute('state'),
      action => clean_whitespace($autoaction->string_value),
    };
  }

  if ($operaction) {
    $UEI_LIST{$uei}->{operaction} = {
      state  => $operaction->getAttribute('state'),
      action => $operaction->string_value,
    };
  }

  if ($snmp) {
    $UEI_LIST{$uei}->{snmp} = {
      id        => clean_text($snmp->find('id')->string_value),
      idtext    => clean_text($snmp->find('idtext')->string_value),
      version   => clean_text($snmp->find('version')->string_value),
      specific  => clean_text($snmp->find('specific')->string_value),
      generic   => clean_text($snmp->find('generic')->string_value),
      community => clean_text($snmp->find('community')->string_value),
    };
  }

  if ($mask) {
    my @elements;

    my @maskelements = $mask->find('maskelement')->get_nodelist;
    for my $maskelement (@maskelements) {
      push(@elements, [ $maskelement->find('mename')->string_value, $maskelement->find('mevalue')->string_value ]);
    }
    $UEI_LIST{$uei}->{mask} = \@elements;
  }
}

print <<END;
<?xml version="1.0"?>
<events xmlns="http://xmlns.opennms.org/xsd/eventconf">
	<global>
		<security>
			<doNotOverride>logmsg</doNotOverride>
			<doNotOverride>operaction</doNotOverride>
			<doNotOverride>autoaction</doNotOverride>
			<doNotOverride>tticket</doNotOverride>
		</security>
	</global>
END

for my $uei (sort keys %UEI_LIST) {
	next if (grep(/^${uei}$/, @UEI_ORDERED, @UEI_DEFAULT));
	print_event($uei);
}
for my $uei (@UEI_ORDERED) {
	print_event($uei);
}
for my $uei (@UEI_DEFAULT) {
	print_event($uei);
}

if ($UEILIST) {
print <<END;

	<!--
		UEI List (Sorted):

END

for (sort(@UEI_ALL)) {
  print "\t\t",   $_->[0], "\n";
  print "\t\t\t", $_->[1], "\n";
}

print <<END;

	-->

END
}

print <<END;
</events>
END

exit $warnings;

sub print_event {
	my $uei   = shift;
	my $event = $UEI_LIST{$uei};
	push(@UEI_ALL, [ $uei, $event->{label} ]);

	print "\t<event>\n";

	if ($event->{mask}) {
		print "\t\t<mask>\n";
		for my $maskelement (@{$event->{mask}}) {
			print <<END;
\t\t\t<maskelement>
\t\t\t\t<mename>$maskelement->[0]</mename>
\t\t\t\t<mevalue>$maskelement->[1]</mevalue>
\t\t\t</maskelement>
END
		}
		print "\t\t</mask>\n";
	}

	print "\t\t<uei>$uei</uei>\n";

	if ($event->{snmp}) {
		print "\t\t<snmp>\n";
		for my $key (sort keys %{$event->{snmp}}) {
			if (defined $event->{snmp}->{$key}) {
				print "\t\t\t<$key>$event->{snmp}->{$key}</$key>\n"
			}
		}
		print "\t\t</snmp>\n";
	}

	if (not defined $event->{label}) {
		print "\t\t<event-label>$uei</event-label>\n";
	} else {
		print "\t\t<event-label>$event->{label}</event-label>\n";
	}

	if (defined $event->{descr} and $event->{descr} ne "") {
		if ($MACHINE) {
			$event->{descr} =~ s/[\r\n\s]+/ /gs;
			print "\t\t<descr>$event->{descr}</descr>\n";
		} else {
			print "\t\t<descr>\n";
			for my $line (split(/[\r\n]+/, $event->{descr})) {
				print "\t\t\t$line\n";
			}
			print "\t\t</descr>\n";
		}
	} else {
		$event->{descr} = "";
		print "\t\t<descr></descr>\n";
	}

	if (not defined $event->{logmsg}) {
		$event->{logmsg} = $event->{descr};
	}

	if (not defined $event->{logdest} or $event->{logdest} !~ /^(logndisplay|displayonly|logonly|suppress)$/) {
		$event->{logdest} = "logndisplay";
	}

	if ($MACHINE) {
		print "\t\t<logmsg dest='$event->{logdest}'>$event->{logmsg}</logmsg>\n";
	} else {
		print "\t\t<logmsg dest='$event->{logdest}'>\n";
		for my $line (split(/[\r\n]+/, $event->{logmsg})) {
			print "\t\t\t$line\n";
		}
		print "\t\t</logmsg>\n";
	}

	print "\t\t<severity>$event->{severity}</severity>\n"
		if (defined $event->{severity} and $event->{severity} ne "");

	if ($event->{correlation}) {
		print "\t\t<correlation>\n";
		for my $key (sort keys %{$event->{correlation}}) {
			if (defined $event->{correlation}->{$key}) {
				print "\t\t\t<$key>$event->{correlation}->{$key}</$key>\n";
			}
		}
		print "\t\t</correlation>\n";
	}

	print "\t\t<operinstruct>$event->{operinstruct}</operinstruct>\n"
		if (defined $event->{operinstruct} and $event->{operinstruct} ne "");

	print "\t\t<autoaction state='$event->{autoaction}->{state}'>$event->{autoaction}->{action}</autoaction>\n"
		if (defined $event->{autoaction} and $event->{autoaction}->{action} ne "");

	print "\t\t<operaction state='$event->{operaction}->{state}'>$event->{operaction}->{action}</operaction>\n"
		if (defined $event->{operaction} and $event->{operaction}->{action} ne "");

	print "\t\t<loggroup>$event->{loggroup}</loggroup>\n"
		if (defined $event->{loggroup} and $event->{loggroup} ne "");

	print "\t\t<mouseovertext>$event->{mouseovertext}</mouseovertext>\n"
		if (defined $event->{mouseovertext} and $event->{mouseovertext} ne "");

	print "\t</event>\n";
}

sub clean_text {
  my $text = shift || "";

  $text =~ s/^[\r\n\s]*//gs;
  $text =~ s/[\r\n\s]*$//gs;
  $text =~ s/\s+/ /gs;
  $text =~ s/<\/p>/<\/p>\n\n/gs;
  $text =~ s/<br>/<br \/>/gs;
  $text =~ s/</&lt;/gs;
  $text =~ s/>/&gt;/gs;

  if ($MACHINE) {
    $text =~ s/[\r\n\s+] //gs;
  } else {
    $text = $wrapper->wrap($text);
    $text =~ s/[\r\n\s]*$//gs;
  }
  return $text;
}

sub clean_whitespace {
  my $text = shift || "";

  $text =~ s/^[\r\n\s]*//gs;
  $text =~ s/[\r\n\s]*$//gs;
  $text =~ s/\s+/ /gs;

  if ($MACHINE) {
    $text =~ s/[\r\n\s+] //gs;
  } else {
    $text = $wrapper->wrap($text);
    $text =~ s/[\r\n\s]*$//gs;
  }
  return $text;
}

sub print_help {
	print <<END;
usage: $0 [-h] [-m] [input_file]

	-h	this help
	-m	machine-formatted output

	Note: /opt/OpenNMS/etc/eventconf.xml is used
	if no input file is specified.

END
}

