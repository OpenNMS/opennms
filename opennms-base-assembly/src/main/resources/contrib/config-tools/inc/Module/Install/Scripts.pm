#line 1
package Module::Install::Scripts;

use strict;
use Module::Install::Base;
use File::Basename ();

use vars qw{$VERSION $ISCORE @ISA};
BEGIN {
	$VERSION = '0.67';
	$ISCORE  = 1;
	@ISA     = qw{Module::Install::Base};
}

sub prompt_script {
    my ($self, $script_file) = @_;

    my ($prompt, $abstract, $default);
    foreach my $line ( $self->_read_script($script_file) ) {
        last unless $line =~ /^#/;
        $prompt = $1   if $line =~ /^#\s*prompt:\s+(.*)/;
        $default = $1  if $line =~ /^#\s*default:\s+(.*)/;
        $abstract = $1 if $line =~ /^#\s*abstract:\s+(.*)/;
    }
    unless (defined $prompt) {
        my $script_name = File::Basename::basename($script_file);
        $prompt = "Do you want to install '$script_name'";
        $prompt .= " ($abstract)" if defined $abstract;
        $prompt .= '?';
    }
    return unless $self->prompt($prompt, ($default || 'n')) =~ /^[Yy]/;
    $self->install_script($script_file);
}

sub install_script {
    my $self = shift;
    my $args = $self->makemaker_args;
    my $exe_files = $args->{EXE_FILES} ||= [];
    push @$exe_files, @_;
}

sub _read_script {
    my ($self, $script_file) = @_;
    local *SCRIPT;
    open SCRIPT, $script_file
      or die "Can't open '$script_file' for input: $!\n";
    return <SCRIPT>;
}

1;
