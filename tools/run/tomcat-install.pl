#!/usr/bin/perl -w

use strict;

my $OPENNMS_HOME = '@root.install@';
my $VERSION      = '0.1';
my $REVISION     = '1';

my @conf_dirs = (
    "/etc/tomcat4/conf",
    "/etc/tomcat4",
    "/etc/default",
    "/var/tomcat4/conf",
    "/sw/var/tomcat4/conf",
    "/usr/local/tomcat4/conf",
    "/usr/local/tomcat/conf"
);

my $ERRORS = 0;

print <<END;
==============================================================================
OpenNMS Tomcat Installer Version $VERSION (Revision $REVISION)
==============================================================================

END

print_help() if((scalar(@ARGV) < 1) || grep(/h/, @ARGV));

# This is crude but I didn't really want to have to depend on getopt
while(my $args = shift(@ARGV)){
    if($args =~ /^\-+(.*)$/){
        foreach my $item (split(//, $1)){
            add_ui()        if($item eq "u");
	    remove_ui()     if($item eq "U");
	    run_as_root()   if($item eq "r");
	    run_as_tomcat() if($item eq "R");
	    remove_jikes()  if($item eq "c");
	    add_jikes()     if($item eq "C");
	}
    }
}

print "\n";
print "*** $ERRORS errors occurred! ***\n" if($ERRORS > 0);
print "<<< Configuration Complete >>>\n\n";

###########################################################################
# Add the opennms context to server.xml                                   #
###########################################################################

sub add_ui {
    my $visited = 0;
    print "- searching for server.xml... ";
    
    for my $dir (@conf_dirs){
        my $serverxml = $dir . "/server.xml";
	if(-f $serverxml){
	    $visited++;
	    print "$serverxml\n";
            print "- checking Tomcat 4 for OpenNMS web UI... ";
	    
	    my $serverxml_in;
	    if(open(FILEIN, "<$serverxml")){ 
	        $serverxml_in .= $_ while(<FILEIN>);
	        close(FILEIN);
            }else{
	        warn "unable to open $serverxml: $!\n";
		$ERRORS++;
		next;
	    }
	    
	    # Not sure what this is for, it was taken verbatim from,
	    # install.pl, (I assume it exists for upgrading from an earlier
	    # version?)
	    if(grep(/opennms/gsi, $serverxml_in)){
                if(!grep(/homeDir/gs, $serverxml_in)){
                    print "UPDATING:\n";
		    $serverxml_in =~ s/userFile\s*=\s*\".*?\"\s*/homeDir="${OPENNMS_HOME}" /gs;
		    $serverxml_in =~ s/<Logger className="org.apache.catalina.logger.FileLogger" prefix="localhost_opennms_log." suffix=".txt" timestamp="true"\/>/<Logger className="org.opennms.web.log.Log4JLogger" homeDir="${OPENNMS_HOME}" \/>/gs;
		    if(open(FILEOUT, ">/tmp/server.xml.$$")){
	                print FILEOUT $serverxml_in;
		        close(FILEOUT);
		    }else{
		        warn "unable to open /tmp/server.xml.$$: $!\n";
			$ERRORS++;
                        next;
		    }
		    rename "/tmp/server.xml.$$", $serverxml || $ERRORS++;
		    chmod 0644, $serverxml || $ERRORS++;
		    print "DONE\n";
		}else{
                    print "FOUND\n";
		}
	    # If the OpenNMS context is not present, add it in.
	    }else{
	        print "NOT FOUND\n";
                print "- adding OpenNMS web UI context to server.xml... ";
		if(open(FILEOUT, ">/tmp/server.xml.$$")){
                    for my $line (split(/\r?\n/, $serverxml_in)){
                        if($line =~ m/<\/host>/gsi){
                            print FILEOUT <<EOF;

        <Context path="/opennms" docBase="opennms" debug="0" reloadable="true">
	    <Logger className="org.opennms.web.log.Log4JLogger" 
                    homeDir="${OPENNMS_HOME}"/>
            <Realm className="org.opennms.web.authenticate.OpenNMSTomcatRealm" 
                    homeDir="${OPENNMS_HOME}"/>
        </Context>

EOF
		        }
		        print FILEOUT $line . "\n";
		    }
		    close(FILEOUT);
		}else{
		    warn "unable to open /tmp/server.xml.$$: $!\n";
		    $ERRORS++;
                    next;
		}
		rename "/tmp/server.xml.$$", $serverxml || $ERRORS++;
		chmod 0644, $serverxml || $ERRORS++;
		print "DONE\n";
	    }
	}
    }
    if($visited == 0){
        print "NONE FOUND!\n";
	$ERRORS++;
    }
}

###########################################################################
# Remove the opennms context from server.xml                              #
###########################################################################

sub remove_ui {
    my $visited = 0;
    print "- searching for server.xml... ";
    
    for my $dir (@conf_dirs){
        my $serverxml = $dir . "/server.xml";
	my $trigger = 0;
	if(-f $serverxml){
	    $visited++;
	    print "$serverxml\n";
            print "- checking Tomcat 4 for OpenNMS web UI... ";
	    
	    my $serverxml_in;
	    if(open(FILEIN, "<$serverxml")){
	        $serverxml_in .= $_ while(<FILEIN>);
	        close(FILEIN);
	    }else{
	        warn "unable to open $serverxml: $!\n";
                $ERRORS++;
		next;
	    }
	    if(grep(/opennms/gsi, $serverxml_in)){
                print "FOUND\n";
		print "- removing OpenNMS web UI context to server.xml... ";
		if(open(FILEOUT, ">/tmp/server.xml.$$")){
		    for my $line (split(/\r?\n/, $serverxml_in)){
		        if(($trigger == 1) && ($line =~ /<\/Context>/)){
                            $trigger = 0;
			    next;
		        }
		        next if($trigger !=0);
                        if($line =~ /<Context path=\"\/opennms/){
		            $trigger = 1;
			    next;
		        }
		        print FILEOUT $line . "\n";
		    }
		    close(FILEOUT);
		}else{
		    warn "unable to open /tmp/server.xml.$$: $!\n";
		    $ERRORS++;
                    next;
		}
		rename "/tmp/server.xml.$$", $serverxml || $ERRORS++;
		chmod 0644, $serverxml || $ERRORS++;
		print "DONE\n";
	    }else{
	        print "NOT FOUND\n";
	    }
	}
    }
    if($visited == 0){
        print "NONE FOUND!\n";
	$ERRORS++;
    }
}

###########################################################################
# Setup Tomcat to run as root                                             #
###########################################################################

sub run_as_root {
    my $visited = 0;
    print "- searching for tomcat4 config... ";
    
    for my $dir (@conf_dirs){
        # Conf is tomcat4 in Debian, tomcat4.conf in everything else.
        for my $conf ('tomcat4.conf', 'tomcat4'){
            my $tomcatconf = $dir . "/" . $conf;
            if(-f $tomcatconf){
	        $visited++;
		print "$tomcatconf\n";
		print "- setting tomcat4 user to 'root'... ";

                if(!open(FILEIN, "<$tomcatconf")){
    	            warn "unable to open $tomcatconf: $!\n";
		    $ERRORS++;
		    next;
		}
    	        if(!open(FILEOUT, ">/tmp/$conf.$$")){
    	    	    warn "unable to open /tmp/$conf.$$: $!\n";
		    $ERRORS++;
		    next;
		}
                while(<FILEIN>){
    	            chomp $_;
		    # Variable expected is TOMCAT4_USER in Debian...
                    if($_ =~ /TOMCAT4_USER/){
                        print FILEOUT "TOMCAT4_USER=\"root\"\n";
		    # ...TOMCAT_USER in everything else.
		    }elsif ($_ =~ /TOMCAT_USER/){
		        print FILEOUT "TOMCAT_USER=\"root\"\n";
    		    }else{
    		        print FILEOUT "$_\n";
    		    }
    	        }
    	        close(FILEIN);
    	        close(FILEOUT);
    	        rename "/tmp/$conf.$$", $tomcatconf || $ERRORS++;
		chmod 0644, $tomcatconf || $ERRORS++;
    	        print "done\n";
    	    }
        }
    }
    if($visited == 0){
        print "NOT FOUND!\n";
	$ERRORS++;
    }
}

###########################################################################
# Setup Tomcat to run as tomcat4                                          #
###########################################################################

sub run_as_tomcat {
    my $visited = 0;
    print "- searching for tomcat4 config... ";
    
    for my $dir (@conf_dirs){
        # Conf is tomcat4 in Debian, tomcat4.conf in everything else.
        for my $conf ('tomcat4.conf', 'tomcat4'){
            my $tomcatconf = $dir . "/" . $conf;
            if(-f $tomcatconf){
	        $visited++;
		print "$tomcatconf\n";
                print "- setting tomcat4 user to 'tomcat4'... ";

                if(!open(FILEIN, "<$tomcatconf")){
    	            warn "unable to open $tomcatconf: $!\n";
		    $ERRORS++;
		    next;
		}
    	        if(!open(FILEOUT, ">/tmp/$conf.$$")){
    	    	    warn "unable to open /tmp/$conf.$$: $!\n";
		    $ERRORS++;
		    next;
		}
                while(<FILEIN>){
    	            chomp $_;
		    # Variable expected is TOMCAT4_USER in Debian...
                    if($_ =~ /TOMCAT4_USER/){
                        print FILEOUT "TOMCAT4_USER=\"tomcat4\"\n";
		    # ...TOMCAT_USER in everything else.
		    }elsif ($_ =~ /TOMCAT_USER/){
		        print FILEOUT "TOMCAT_USER=\"tomcat4\"\n";
    		    }else{
    		        print FILEOUT "$_\n";
    		    }
    	        }
    	        close(FILEIN);
    	        close(FILEOUT);
    	        rename "/tmp/$conf.$$", $tomcatconf || $ERRORS++;
		chmod 0644, $tomcatconf || $ERRORS++;
    	        print "done\n";
    	    }
        }
    }
    if($visited == 0){
        print "NOT FOUND!\n";
	$ERRORS++;
    }

}

###########################################################################
# Add jikes back in if previously removed                                 #
###########################################################################

sub add_jikes {
    my $visited = 0;
    print "- searching for web.xml... ";
    
    for my $dir (@conf_dirs){
        my $webxml = $dir . "/web.xml";
	my $webxml_in;
	if(-f $webxml){
	    $visited++;
	    print "$webxml\n";
            print "- checking Tomcat 4 for jikes compiler... ";
	    
	    if(open(FILEIN, "<$webxml")){
	        $webxml_in .= $_ while(<FILEIN>);
                close(FILEIN);
            }else{
	        warn "unable to open $webxml: $!\n";
		$ERRORS++;
                next;
	    }
	    # This only works if we actually commented it out.
	    if($webxml_in =~ s/<\!-- Commented out by OpenNMS install script\n([\s\t]+<init-param>\n[\s\t]+<param-name>compiler<\/param-name>\n[\s\t]+<param-value>jikes<\/param-value>\n[\s\t]+<\/init-param>)\nCommented out by OpenNMS install script -->/$1/){
    	        print "FOUND\n";
    	        print "- removing commented compiler init param in web.xml... ";
    	        if(open(FILEOUT, ">/tmp/web.xml.$$")){ 
    	            print FILEOUT $webxml_in;
                    close(FILEOUT);
		}else{
    	            warn "unable to open /tmp/web.xml.$$: $!\n";
                    $ERRORS++;
		    next;
		}
    	        print "DONE\n";
	    }else{
                print "NOT FOUND\n";
	    }
            rename "/tmp/web.xml.$$", $webxml || $ERRORS++;
    	    chmod 0644, $webxml || $ERRORS++;
	}
    }
    if($visited == 0){
        print "NOT FOUND!\n";
	$ERRORS++;
    }
}

###########################################################################
# Fix web.xml to _not_ use jikes                                          #
###########################################################################

sub remove_jikes {
    my $visited = 0;
    print "- searching for web.xml... ";
    
    for my $dir (@conf_dirs){
        my $webxml = $dir . "/web.xml";
	my $webxml_in;
	if(-f $webxml){
	    $visited++;
	    print "$webxml\n";
            print "- checking Tomcat 4 for jikes compiler... ";
	    
	    if(open(FILEIN, "<$webxml")){
	        $webxml_in .= $_ while(<FILEIN>);
                close(FILEIN);
	    }else{
	        warn "unable to open $webxml: $!\n";
                $ERRORS++;
		next;
	    }
	    if($webxml_in =~ s/>\n([\s\t]+<init-param>\n[\s\t]+<param-name>compiler<\/param-name>\n[\s\t]+<param-value>jikes<\/param-value>\n[\s\t]+<\/init-param>)/>\n<\!-- Commented out by OpenNMS install script\n$1\nCommented out by OpenNMS install script -->/){
    	        print "FOUND\n";
    	        print "- removing compiler init param in web.xml... ";
    	        if(open(FILEOUT, ">/tmp/web.xml.$$")){ 
    	            print FILEOUT $webxml_in;
                    close(FILEOUT);
		}else{
    	            warn "unable to open /tmp/web.xml.$$: $!\n";
                    $ERRORS++;
		    next;
		}
    	        print "DONE\n";
	    }else{
                print "NOT FOUND\n";
	    }
            rename "/tmp/web.xml.$$", $webxml || $ERRORS++;
    	    chmod 0644, $webxml || $ERRORS++;
	}
    }
    if($visited == 0){
        print "NOT FOUND!\n";
	$ERRORS++;
    }
}

###########################################################################
# Print usage information                                                 #
###########################################################################

sub print_help {
     print <<END;
usage: $0 <options>
  Options:
    
    -h    this help
    
    -U    remove OpenNMS UI context from server.xml
    -u    add OpenNMS UI context to server.xml
    -R    set tomcat to run as user 'tomcat'
    -r    set tomcat to run as user 'root'
    -C    re-add the jikes compiler init parameter if previously commented
          out, (used for uninstalling).
    -c    comment out jikes compiler init parameter from web.xml if present
    
END
    exit 1;
}
