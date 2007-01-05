//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
package org.opennms.test.netsim;

import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;


public final class SimulatorController
{
    private InetAddress _controlChannel;
    private InetAddress _localInetAddress;
    private java.util.ArrayList _nodes = new java.util.ArrayList();


    public SimulatorController(InetAddress controlChannel) throws RuntimeException
    {
        this._controlChannel = controlChannel;
        try {
            System.out.println(this._localInetAddress = lookupLocalInetAddress());
        } catch (UnknownHostException e) {
            throw new RuntimeException("Unknown host: " + e.getMessage());
        }
    }

    /**
     * @deprecated
     * Because it isn't implemented yet
     */
    private boolean isControlChannelLive()
    {
        return false;
    }

    public void addNode(SimulatedNode newNode)
    {
        this._nodes.add(newNode);
        newNode.setParentController(this);
    }

    protected void sendCommand(String command)
    {
        command = "ssh " + this._controlChannel.getHostAddress() + " /usr/local/netsim/init.d/" + command;
        try {
            Process p = Runtime.getRuntime().exec(command);
            for (int ms = 0; ms < 5000; ms += 100)
            {
                try {
                    int exit = p.exitValue();
                    break;
                } catch (IllegalThreadStateException e) {
                    //System.err.println("Still not done at " + ms + " milliseconds.");
                }
                Thread.sleep(100);
            }
            
            try {
                int exit = p.exitValue();
                if (!(exit == 0)) {
                    System.out.println("Nonzero exit code: " + exit);
                }
            } catch (IllegalThreadStateException e) {
                System.err.println("Process timed out executing command '" + command + "'");
            } finally {
                p.destroy();
            }
        } catch (InterruptedException e) {
            System.err.println("Thread interrupted!");
        } catch (java.io.IOException e) {
            System.err.println("IOException executing command '" + command + "'");
        }
    }

    protected InetAddress getLocalInetAddress()
    {
        return this._localInetAddress;
    }
    
    /**
     * Convenience method
     * This was created because InetAddress.getLocalHost() does not work properly on hosts with 'localhost' for a hostname
     */
    private static InetAddress lookupLocalInetAddress() throws RuntimeException, UnknownHostException
    {
        InetAddress ret;

        String[] command = new String[3];
        command[0] = "/bin/sh";
        command[1] = "-c";
        command[2] = "/sbin/ifconfig eth0 | /bin/grep 'inet addr' | /bin/awk {'print $2'} | /bin/cut -d':' -f2";

        try {
            Process p = Runtime.getRuntime().exec(command);
            for (int ms = 0; ms < 2000; ms += 100)
            {
                try {
                    int exit = p.exitValue();
                    break;
                } catch (IllegalThreadStateException e) {
                    //System.err.println("Still not done at " + ms + " milliseconds.");
                }
                Thread.sleep(100);
            }
            
            try {
                int exit = p.exitValue();
                if (exit == 0) {
                    BufferedReader p_output = new BufferedReader(
                                                  new InputStreamReader(p.getInputStream())
                                              );
                    String ip_out = p_output.readLine();
                    ret = InetAddress.getByName(ip_out);
                    p_output.close();
                } else {
                    BufferedReader p_err = new BufferedReader(
                                                  new InputStreamReader(p.getErrorStream())
                                              );
                    System.err.println("Error: " + p_err.readLine());
                    p_err.close();
                    throw new RuntimeException("Nonzero exit code: " + exit + command);
                }
            } catch (IllegalThreadStateException e) {
                throw new RuntimeException("Process timed out executing command '" + command + "'");
            } finally {
                p.destroy();
            }
        } catch (InterruptedException e) {
                throw new RuntimeException("Thread interrupted executing command '" + command + "'");
        } catch (java.io.IOException e) {
            throw new RuntimeException("IOException executing command '" + command + "'");
        }
        return ret;
    }

}
