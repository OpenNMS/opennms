#!/usr/bin/perl -w

# This script will pop-up a small window with the RTC
# category information from OpenNMS
#
# this was just an attempt to learn perl-gtk, but I thought some
# of you might find it interesting.  =)
#
# it requires a version of send-event.pl that supports sending
# parms (OpenNMS 0.9.6+), as well as the HTTP::Daemon and Gtk
# perl modules.

package PerlUI;
$|++;

use vars qw(
	$VERBOSE
	$OPENNMS_HOME

	@CATEGORIES
	%avail_bars
	%outage_labels
	%labels
	@levels
);
use strict;
use HTTP::Daemon;
use HTTP::Response;
use Gtk;

$SIG{__DIE__} = \&do_exit;
$SIG{QUIT}    = \&do_exit;
$SIG{KILL}    = \&do_exit;
$SIG{INT}     = \&do_exit;

@levels       = (
	[ '#00ff00'	=> 100 ],
	[ '#ffff00'	=> 99  ],
	[ '#ff3333'	=> 97  ],
	[ '#ee2222'	=> 94  ],
	[ '#dd1111'	=> 90  ],
	[ '#cc0000'	=> 80  ],
);

$OPENNMS_HOME = '/opt/OpenNMS';
chomp(my $host   = `hostname --fqdn`);
my $port   = '8101';
my $server = 'localhost';
my $send_event = "$OPENNMS_HOME/bin/send-event.pl";
$VERBOSE = 0;

$server = $ARGV[0] if (defined $ARGV[0]);
$host   = 'localhost' if ($server eq 'localhost');

# read in the category names from categories.xml
open (FILEIN, "$OPENNMS_HOME/etc/categories.xml") or die "unable to open $OPENNMS_HOME/etc/categories.xml: $!\n";
while (<FILEIN>) {
	if (/<label><\!\[CDATA\[(.*)\]\]><\/label>/) {
		push(@CATEGORIES, $1);
	}
}

Gtk->init;

my $window = Gtk::Window->new();
my $vbox = Gtk::VBox->new(0,0);
my $hbox = Gtk::HBox->new(0,0);
my $quit = Gtk::Button->new('Quit');

$window->add($vbox);
$vbox->add($hbox);
$vbox->add($quit);

my $left   = Gtk::VBox->new(0,0);
my $middle = Gtk::VBox->new(0,0);
my $right  = Gtk::VBox->new(0,0);

$hbox->add($left);
$hbox->add($middle);
$hbox->add($right);

$left->add(Gtk::Label->new('Categories'));
$middle->add(Gtk::Label->new('Outages'));
$right->add(Gtk::Label->new('24hr Avail.'));

for my $key (@CATEGORIES) {
	my ($style, $color);

	# description
	$labels{$key} = Gtk::Label->new("  $key  ");
	$labels{$key}->set_justify('left');
	$left->add($labels{$key});

	# outage list
	$outage_labels{$key} = Gtk::Label->new("0 of 0");
	$middle->add($outage_labels{$key});

	# availability bar
	$avail_bars{$key} = Gtk::ProgressBar->new();
	$avail_bars{$key}->set_format_string('100%%');
	$avail_bars{$key}->set_show_text(1);
	$style = $avail_bars{$key}->style->copy or print "can't get style: $!\n";
	$color = '#00ff00';
	$style->bg('prelight', Gtk::Gdk::Color->parse_color($color));
	$avail_bars{$key}->set_style($style);
	$avail_bars{$key}->set_percentage(1);
	$right->add($avail_bars{$key});
}

$quit->signal_connect('clicked', \&do_exit);
$window->signal_connect('destroy', \&do_exit);

$window->show_all();

Gtk->idle_add(sub {
        while (Gtk->events_pending) {
                Gtk->main_iteration;
        }
	return 1;
});

my $daemon;

print "starting HTTP listener... ";
do {
	sleep(1);
	$daemon = HTTP::Daemon->new(
		LocalPort => $port,
	);
} until ($daemon);
print "done\n";

for my $key (@CATEGORIES) {
	print "subscribing to '$key'\n";
	system("perl $send_event --parm 'url http://$host:$port/submit' --parm 'user $ENV{USER}' --parm 'passwd $ENV{USER}' --parm 'catlabel $key' http://uei.opennms.org/internal/rtc/subscribe $server");
}

$window->Gtk::Gdk::input_add(fileno($daemon), 1, [ \&handle_connection, $daemon]);

sub do_exit {
	for my $key (@CATEGORIES) {
		print "unsubscribing from '$key'\n";
		system(" perl  $send_event --parm 'url http://$host:$port/submit' --parm 'user $ENV{USER}' --parm 'passwd $ENV{USER}' --parm 'catlabel $key' http://uei.opennms.org/internal/rtc/unsubscribe $server");
	}
	Gtk->exit(0);
}

sub redraw_outage_label {
	my ($label, $outages, $services) = @_;
	my $color;
	my $percent = 0;
	if ($services != 0) {
		$percent = ($outages / $services);
	}
	$percent = (1 - $percent);

	# set label value
	$label->set("$outages of $services") or return;
	$label->show;

	return 1;
}

sub redraw_avail_bar {
	my ($bar, $percent) = @_;
	my $color;

	# set bar color
	my $style = $bar->style->copy or warn "can't get style: $!\n";
	for my $level (@levels) {
		if ($percent <= $level->[1]) {
			$color = $level->[0];
		} else {
			last;
		}
	}
	$style->bg('prelight', Gtk::Gdk::Color->parse_color($color));
	$bar->set_style($style);
	$bar->set_format_string(sprintf("%.2f", $percent) . '%%');
	$bar->show;

	# set percentage
	return $bar->set_percentage($percent / 100);
}

sub handle_connection {
	my $daemon     = shift;
	my $connection = $daemon->accept;
	my $request    = $connection->get_request;
	my $color;

	my ($label, $value);
	my $outages  = 0;
	my $services = 0;

	my $text = $request->as_string;
	print $text if ($VERBOSE);
	for my $line (split(/></, $text)) {
		print $line;
		if ($line =~ /ns.*catlabel>(.*)<\/ns.*catlabel/) {
			$label = decode_entities($1);
		} elsif ($line =~ /ns.*catvalue>(.*)<\/ns.*catvalue/) {
			$value = decode_entities($1);
		} elsif ($line =~ /ns.*nodesvccount>(\d+)<\/ns.*nodesvccount/) {
			$services += $1;
		} elsif ($line =~ /ns.*nodesvcdowncount>(\d+)<\/ns.*nodesvcdowncount/) {
			$outages += $1;
		}
	}
	print "$label: $value ($outages/$services)\n" if ($VERBOSE);
	if ($label) {
		redraw_outage_label($outage_labels{$label}, $outages, $services);
		redraw_avail_bar($avail_bars{$label}, $value);
	}
	$connection->send_response(HTTP::Response->new(200));
}

sub decode_entities {
	my $value = shift;
	$value =~ s#&lt;#<#gs;
	$value =~ s#&gt;#>#gs;
	$value =~ s#&amp;#&#gs;
	return $value;
}

Gtk->main;
