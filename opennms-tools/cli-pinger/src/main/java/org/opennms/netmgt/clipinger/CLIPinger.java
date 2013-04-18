/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2013 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.clipinger;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.opennms.netmgt.icmp.Pinger;
import org.opennms.netmgt.icmp.PingerFactory;

/**
 * <P>
 * This class provides a command-line utility to test the
 * availability of the ICMP service on remote interfaces
 * using the same ICMP framework as the rest of OpenNMS.
 * </P>
 *
 * @author <A HREF="mailto:jeffg@opennms.org">Jeff Gehlbach</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 */

final public class CLIPinger {
    
    @Option(name = "--timeout", aliases = {"-t"}, required = false, usage = "timeout for each ping, in milliseconds (default 1000)")
    private static long s_timeout = 1000;
    
    @Option(name = "--retry", aliases = {"-r"}, required = false, usage = "retries for each ping (default 2)")
    private static int s_retries = 2;
    
    @Option(name = "--interval", aliases = {"-i"}, required = false, usage = "interval between pings, in milliseconds (default 1000)")
    private static long s_interval = 1000;
    
    @Option(name = "--count", aliases = {"-c"}, required = false, usage = "number of pings (default 5)")
    private static int s_count = 5;
    
    @Argument
    private static List<String> s_arguments = new ArrayList<String>();
    
    public static void main(String[] args) throws CmdLineException {
        new CLIPinger().doMain(args);
    }
    
    @SuppressWarnings("SleepWhileInLoop")
    public void doMain(String[] args) throws CmdLineException {
        setPropertiesIfPossible();
        
        CmdLineParser parser = new CmdLineParser(this);
        
        try {
            parser.parseArgument(args);
        } catch (CmdLineException e) {
            parser.printUsage(System.err);
            System.exit(1);
        }
        
        InetAddress host;
        double rttMs;
        
        if (s_arguments.isEmpty()) {
            parser.printUsage(System.err);
            System.exit(1);
        }
        
        try {
            host = InetAddress.getByName(s_arguments.get(0));
            Pinger p = PingerFactory.getInstance();
            for (int i = 0; i < s_count; i++) {
                Number rtt = p.ping(host, s_timeout, s_retries);
                if (rtt == null) {
                    System.out.println("request timed out");
                } else {
                    rttMs = rtt.doubleValue() / 1000.0;
                    System.out.println("Reply from " + host.getHostName() + " (" + host.getHostAddress() + "): time=" + rttMs + " ms");
                }
                if (i < s_count - 1) {
                    Thread.sleep(s_interval);
                }
            }
        } catch (UnknownHostException ex) {
            System.out.println("Unknown host " + args[0]);
            System.exit(1);
        } catch (Exception ex) {
            System.out.println("Unexpected exception while pinging " + args[0] + ": " + ex.getMessage());
            System.exit(1);
        } finally {
            System.exit(0);
        }
    }

    private void setPropertiesIfPossible() {
        File libProps;
        libProps = new File("/opt/opennms/etc/libraries.properties");
        if (! libProps.canRead()) {
            libProps = new File("/etc/opennms/libraries.properties");
        }
        if (libProps.canRead()) {
            System.out.println("Reading lib config from " + libProps.getAbsolutePath());
            try {
                System.getProperties().load(new FileReader(libProps));
            } catch (IOException ex) {
                Logger.getLogger(CLIPinger.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
