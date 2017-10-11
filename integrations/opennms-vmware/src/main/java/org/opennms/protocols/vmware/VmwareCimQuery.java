/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.protocols.vmware;

import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.sblim.wbem.cim.CIMObject;

import com.vmware.vim25.mo.HostSystem;
import com.vmware.vim25.mo.ManagedEntity;

public abstract class VmwareCimQuery {
    public static Map<Integer, String> m_healthStates;

    static {
        m_healthStates = new HashMap<Integer, String>();

        m_healthStates.put(0, "Unknown");
        m_healthStates.put(5, "OK");
        m_healthStates.put(10, "Degraded/Warning");
        m_healthStates.put(15, "Minor failure");
        m_healthStates.put(20, "Major failure");
        m_healthStates.put(25, "Critical failure");
        m_healthStates.put(30, "Non-recoverable error");
    }

    private static void cimQuery(String hostname, String username, String password) {

        System.out.print("Trying to connect to " + hostname + "... ");

        VmwareViJavaAccess vmwareViJavaAccess = new VmwareViJavaAccess(hostname, username, password);

        try {
            vmwareViJavaAccess.connect();
        } catch (MalformedURLException e) {
            System.out.println("Exception:");
            e.printStackTrace();
            return;
        } catch (RemoteException e) {
            System.out.println("Exception:");
            e.printStackTrace();
            return;
        }

        System.out.println("Success!");

        ManagedEntity[] hostSystems;

        System.out.print(" Querying " + hostname + " for host systems... ");

        try {
            hostSystems = vmwareViJavaAccess.searchManagedEntities("HostSystem");
        } catch (RemoteException remoteException) {
            remoteException.printStackTrace();
            vmwareViJavaAccess.disconnect();
            return;
        }

        if (hostSystems != null) {
            System.out.println(hostSystems.length + " host system(s) found!");

            for (ManagedEntity managedEntity : hostSystems) {
                HostSystem hostSystem = (HostSystem) managedEntity;

                if (hostSystem.getSummary() != null) {
                    if (hostSystem.getRuntime() != null) {
                        String powerState = hostSystem.getRuntime().getPowerState().toString();

                        if (!"poweredOn".equals(powerState)) {
                            System.out.println("  Ignoring host system " + hostSystem.getName() + " (powerState=" + powerState + ")... ");
                            continue;
                        } else {
                            System.out.print("  Determining primary Ip address of host system " + hostSystem.getName() + " (powerState=" + powerState + ")... ");
                        }
                    } else {
                        System.out.println("  Ignoring host system " + hostSystem.getName() + " (powerState=unknown)... ");
                        continue;
                    }
                } else {
                    System.out.println("  Ignoring host system " + hostSystem.getName() + " (powerState=unknown)... ");
                    continue;
                }

                String ipAddress;

                ipAddress = vmwareViJavaAccess.getPrimaryHostSystemIpAddress(hostSystem);

                if (ipAddress != null) {
                    System.out.print(ipAddress + "\n  Querying host system " + hostSystem.getName() + " for numeric sensors... ");

                    List<CIMObject> cimObjects;

                    try {
                        cimObjects = vmwareViJavaAccess.queryCimObjects(hostSystem, "CIM_NumericSensor");
                    } catch (Exception e) {
                        System.out.println("Exception:");
                        e.printStackTrace();
                        continue;
                    }

                    if (cimObjects != null) { // FIXME queryCimObjects returns an empty list or a filled list, but never null
                        System.out.println(cimObjects.size() + " sensor(s) found!");
                        for (CIMObject cimObject : cimObjects) {
                            String healthState = vmwareViJavaAccess.getPropertyOfCimObject(cimObject, "HealthState");
                            String cimObjectName = vmwareViJavaAccess.getPropertyOfCimObject(cimObject, "Name");
                            System.out.print("   " + cimObjectName);

                            if (healthState != null) {
                                System.out.println(" " + m_healthStates.get(Integer.valueOf(healthState)));
                            } else {
                                System.out.println();
                            }
                        }
                    } else {
                        System.out.println("NULL - aborting...");
                        continue;
                    }
                } else {
                    System.out.println("NULL - aborting...");
                    continue;
                }
            }
        } else {
            System.out.println("NULL - aborting...");
        }

        System.out.println("Cleaning up...");

        vmwareViJavaAccess.disconnect();
    }

    private static void usage(final Options options, final CommandLine cmd, final String error, final Exception e) {
        final HelpFormatter formatter = new HelpFormatter();
        final PrintWriter pw = new PrintWriter(System.out);
        if (error != null) {
            pw.println("An error occurred: " + error + "\n");
        }

        formatter.printHelp("Usage: VmwareCimQuery <hostname> <username> <password>", options);

        if (e != null) {
            pw.println(e.getMessage());
            e.printStackTrace(pw);
        }

        pw.close();
    }

    private static void usage(final Options options, final CommandLine cmd) {
        usage(options, cmd, null, null);
    }

    public static void main(String[] args) throws ParseException {
        String hostname, username, password;

        final Options options = new Options();

        final CommandLineParser parser = new PosixParser();
        final CommandLine cmd = parser.parse(options, args);

        @SuppressWarnings("unchecked")
        List<String> arguments = (List<String>) cmd.getArgList();

        if (arguments.size() < 3) {
            usage(options, cmd);
            System.exit(1);
        }

        hostname = arguments.remove(0);
        username = arguments.remove(0);
        password = arguments.remove(0);

        cimQuery(hostname, username, password);
    }
}
