/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * 2008 Jun 05: Reorganize a bit, add property mockSnmpAgent.sleepOnCreate that
 *              can be used to make createAgentAndRun sleep for the specified
 *              number of milliseconds upon startup. - dj@opennms.org
 * 2008 Feb 10: Eliminate warnings. - dj@opennms.org
 * 
 * Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.mock.snmp;

import java.io.File;
import java.io.IOException;
import java.net.BindException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.snmp4j.TransportMapping;
import org.snmp4j.agent.BaseAgent;
import org.snmp4j.agent.CommandProcessor;
import org.snmp4j.agent.DuplicateRegistrationException;
import org.snmp4j.agent.ManagedObject;
import org.snmp4j.agent.io.ImportModes;
import org.snmp4j.agent.mo.MOTableRow;
import org.snmp4j.agent.mo.snmp.RowStatus;
import org.snmp4j.agent.mo.snmp.SnmpCommunityMIB;
import org.snmp4j.agent.mo.snmp.SnmpNotificationMIB;
import org.snmp4j.agent.mo.snmp.SnmpTargetMIB;
import org.snmp4j.agent.mo.snmp.StorageType;
import org.snmp4j.agent.mo.snmp.TransportDomains;
import org.snmp4j.agent.mo.snmp.VacmMIB;
import org.snmp4j.log.Log4jLogFactory;
import org.snmp4j.log.LogFactory;
import org.snmp4j.mp.MPv3;
import org.snmp4j.mp.MessageProcessingModel;
import org.snmp4j.security.AuthMD5;
import org.snmp4j.security.AuthSHA;
import org.snmp4j.security.PrivDES;
import org.snmp4j.security.SecurityLevel;
import org.snmp4j.security.SecurityModel;
import org.snmp4j.security.USM;
import org.snmp4j.security.UsmUser;
import org.snmp4j.smi.Counter32;
import org.snmp4j.smi.Counter64;
import org.snmp4j.smi.Integer32;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.Variable;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.snmp4j.util.ThreadPool;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

/*
 * The <code>MockSnmpAgent</code> class extends the SNMP4J BaseAgent
 * class to provide a mock SNMP agent for SNMP-based OpenNMS tests.
 * Large chunks of code were lifted from the org.snmp4j.agent.test.TestAgent
 * class.
 * 
 * @author Jeff Gehlbach
 * @version 1.0
 */
public class MockSnmpAgent extends BaseAgent implements Runnable {
    private static final String PROPERTY_SLEEP_ON_CREATE = "mockSnmpAgent.sleepOnCreate";

    private String m_address;
    private Resource m_moFile;
    private boolean m_running;
    private boolean m_stopped;
    private List<ManagedObject> m_moList;
    private MockSnmpMOLoader m_moLoader;

    // initialize Log4J logging
    static {
        LogFactory.setLogFactory(new Log4jLogFactory());
    }

    /*
     * Creates the mock agent with files to read and store the boot counter,
     * to read and store the agent configuration, and to read the mocked
     * managed objects (MOs), plus a string describing the address and port
     * to bind to.
     * 
     * @param bootFile
     * 		a file containing the boot counter in serialized form (as expected by BaseAgent).
     * @param confFile
     * 		a configuration file with serialized management information.
     * @param moFile
     * 		a MIB dump file describing the managed objects to be mocked.  The current implementation
     * 		expects a Java properties file, which can conveniently be generated using the Net-SNMP
     * 		utility <code>snmpwalk</code> with the <code>-One</code> option set.
     */
    public MockSnmpAgent(File bootFile, File confFile, Resource moFile, String bindAddress) {
        super(bootFile, confFile, new CommandProcessor(new OctetString(MPv3.createLocalEngineID(new OctetString("MOCKAGENT")))));
        m_moLoader = new PropertiesBackedManagedObject();
        m_address = bindAddress;
        m_moFile = moFile;
        agent.setWorkerPool(ThreadPool.create("RequestPool", 4));
    }
    
    public static MockSnmpAgent createAgentAndRun(Resource moFile, String bindAddress) throws InterruptedException {
        try {
            if (moFile.getInputStream() == null) {
                throw new IllegalArgumentException("could not get InputStream mock object resource; does it exist?  Resource: " + moFile);
            }
        } catch (IOException e) {
            throw new RuntimeException("Got IOException while checking for existence of mock object file: " + e, e);
        }
        
        MockSnmpAgent agent = new MockSnmpAgent(new File("/dev/null"), new File("/dev/null"), moFile, bindAddress);
        Thread thread = new Thread(agent, agent.getClass().getSimpleName());
        thread.start();

        try {
            while (!agent.isRunning() && thread.isAlive()) {
                Thread.sleep(10);
            } 
        } catch (InterruptedException e) {
            agent.shutDownAndWait();
            throw e;
        }

        if (!thread.isAlive()) {
            agent.m_running = false;
            agent.m_stopped = true;
            throw new IllegalStateException("agent failed to start--check logs");
        }
        
        if (System.getProperty(PROPERTY_SLEEP_ON_CREATE) != null) {
            long sleep = Long.parseLong(System.getProperty(PROPERTY_SLEEP_ON_CREATE));
            Thread.sleep(sleep);
        }

        return agent;
    }
    
    public static void main(String[] args) {
        AgentConfigData agentConfig = parseCli(args);
        if (agentConfig == null) {
            System.err.println("Could not parse configuration.");
            System.exit(1);
        }
        String listenSpec = agentConfig.getListenAddr().getHostAddress() + "/" + agentConfig.getListenPort();
    	
       	try {
       	    MockSnmpAgent.createAgentAndRun(agentConfig.getMoFile(), listenSpec);
       	} catch (InterruptedException e) {
       	    System.exit(0);
       	}
    }

    public static AgentConfigData parseCli(String[] args) {
        Options opts = new Options();
        opts.addOption("d", "dump-file", true, "Pathname or URL of file containing MIB dump");
        opts.addOption("l", "listen-addr", true, "IP address to bind to (default: 127.0.0.1)");
        opts.addOption("p", "port", true, "UDP port to listen on (default: 1691)");
        
        String dumpFile = "";
        String listenAddr;
        long listenPort;
        AgentConfigData agentConfig;

        CommandLineParser parser = new PosixParser();
        try {
            CommandLine cmd = parser.parse(opts, args);
            if (cmd.hasOption('d')) {
                dumpFile = cmd.getOptionValue('d');
            } else {
                usage("You must specify at least a pathname or URL for the dump file.", opts);
            }

            if (cmd.hasOption('l')) {
                listenAddr = cmd.getOptionValue('l');
            } else {
                listenAddr = "127.0.0.1";
            }
            if (cmd.hasOption('p')) {
                listenPort = Long.valueOf(cmd.getOptionValue('p'));
            } else {
                listenPort = 1691L;
            }
            return new AgentConfigData(dumpFile, listenAddr, listenPort);
        } catch (ParseException e) {
            usage("Failed to parse provided options.", opts);
        } catch (UnknownHostException e) {
            usage("Unknown host in dump file URL specifier", opts);
        } catch (MalformedURLException e) {
            usage("Malformed dump file URL specifier", opts);
        }

        return null;
    }
    
    private static void usage(String why, Options opts) {
        System.err.println(why);
        System.err.println(opts.toString());
        System.exit(1);
    }
    
    public void shutDownAndWait() throws InterruptedException {
        if (!isRunning()) {
            return;
        }

        shutDown();

        while (!isStopped()) {
            Thread.sleep(10);
        } 
    }

    /*
     * Starts the <code>MockSnmpAgent</code> running.  Meant to be called from the
     * <code>start</code> method of class <code>Thread</code>, but could also be
     * used to bring up a standalone mock agent.
     * @see org.snmp4j.agent.BaseAgent#run()
     * 
     * @author Jeff Gehlbach
     * @version 1.0
     */
    // XXX fix catch blocks
    public void run() {
        try {
            init();
            loadConfig(ImportModes.UPDATE_CREATE);
            addShutdownHook();
            finishInit();
            super.run();
            m_running = true;
        } catch (BindException be) {
            be.printStackTrace();
            System.err.println("You probably specified an invalid address or a port < 1024 and are not running as root");
        } catch (IOException ie) {
            ie.printStackTrace();
        }

        while (m_running) {
            try {
                Thread.sleep(10); // fast, Fast, FAST, *FAST*!!!
            } catch (InterruptedException e) {
                break;
            }
        }

        try {
            for (TransportMapping transportMapping : transportMappings) {
                transportMapping.close();
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }

        m_stopped = true;
    }

    /*
     * 
     */
    public void shutDown() {
        m_running = false;
        m_stopped = false;
    }

    public boolean isRunning() {
        return m_running;
    }

    public boolean isStopped() {
        return m_stopped;
    }

    @Override
    protected void addCommunities(SnmpCommunityMIB communityMIB) {
        Variable[] com2sec = new Variable[] {
                new OctetString("public"),              // community name
                new OctetString("public"),              // security name
                getAgent().getContextEngineID(),        // local engine ID
                new OctetString(),                      // default context name
                new OctetString(),                      // transport tag
                new Integer32(StorageType.nonVolatile), // storage type
                new Integer32(RowStatus.active)         // row status
        };
        MOTableRow row =
            communityMIB.getSnmpCommunityEntry().createRow(
                                                           new OctetString("public2public").toSubIndex(true), com2sec);
        communityMIB.getSnmpCommunityEntry().addRow(row);
    }

    @Override
    protected void addViews(VacmMIB vacm) {
        vacm.addGroup(SecurityModel.SECURITY_MODEL_SNMPv1,
                      new OctetString("public"),
                      new OctetString("v1v2group"),
                      StorageType.nonVolatile);
        vacm.addGroup(SecurityModel.SECURITY_MODEL_SNMPv2c,
                      new OctetString("public"),
                      new OctetString("v1v2group"),
                      StorageType.nonVolatile);
        vacm.addGroup(SecurityModel.SECURITY_MODEL_USM,
                      new OctetString("SHADES"),
                      new OctetString("v3group"),
                      StorageType.nonVolatile);
        vacm.addGroup(SecurityModel.SECURITY_MODEL_USM,
                      new OctetString("TEST"),
                      new OctetString("v3test"),
                      StorageType.nonVolatile);
        vacm.addGroup(SecurityModel.SECURITY_MODEL_USM,
                      new OctetString("opennmsUser"),
                      new OctetString("v3group"),
                      StorageType.nonVolatile);
        vacm.addGroup(SecurityModel.SECURITY_MODEL_USM,
                      new OctetString("SHA"),
                      new OctetString("v3restricted"),
                      StorageType.nonVolatile);

        vacm.addAccess(new OctetString("v1v2group"), new OctetString(),
                       SecurityModel.SECURITY_MODEL_ANY,
                       SecurityLevel.NOAUTH_NOPRIV, VacmMIB.vacmExactMatch,
                       new OctetString("fullReadView"),
                       new OctetString("fullWriteView"),
                       new OctetString("fullNotifyView"),
                       StorageType.nonVolatile);
        vacm.addAccess(new OctetString("v3group"), new OctetString(),
                       SecurityModel.SECURITY_MODEL_USM,
                       SecurityLevel.AUTH_PRIV, VacmMIB.vacmExactMatch,
                       new OctetString("fullReadView"),
                       new OctetString("fullWriteView"),
                       new OctetString("fullNotifyView"),
                       StorageType.nonVolatile);
        vacm.addAccess(new OctetString("v3restricted"), new OctetString(),
                       SecurityModel.SECURITY_MODEL_USM,
                       SecurityLevel.AUTH_NOPRIV, VacmMIB.vacmExactMatch,
                       new OctetString("restrictedReadView"),
                       new OctetString("restrictedWriteView"),
                       new OctetString("restrictedNotifyView"),
                       StorageType.nonVolatile);
        vacm.addAccess(new OctetString("v3test"), new OctetString(),
                       SecurityModel.SECURITY_MODEL_USM,
                       SecurityLevel.AUTH_PRIV, VacmMIB.vacmExactMatch,
                       new OctetString("testReadView"),
                       new OctetString("testWriteView"),
                       new OctetString("testNotifyView"),
                       StorageType.nonVolatile);

        vacm.addViewTreeFamily(new OctetString("fullReadView"), new OID("1.3"),
                               new OctetString(), VacmMIB.vacmViewIncluded,
                               StorageType.nonVolatile);
        vacm.addViewTreeFamily(new OctetString("fullWriteView"), new OID("1.3"),
                               new OctetString(), VacmMIB.vacmViewIncluded,
                               StorageType.nonVolatile);
        vacm.addViewTreeFamily(new OctetString("fullNotifyView"), new OID("1.3"),
                               new OctetString(), VacmMIB.vacmViewIncluded,
                               StorageType.nonVolatile);

        vacm.addViewTreeFamily(new OctetString("restrictedReadView"),
                               new OID("1.3.6.1.2"),
                               new OctetString(), VacmMIB.vacmViewIncluded,
                               StorageType.nonVolatile);
        vacm.addViewTreeFamily(new OctetString("restrictedWriteView"),
                               new OID("1.3.6.1.2.1"),
                               new OctetString(),
                               VacmMIB.vacmViewIncluded,
                               StorageType.nonVolatile);
        vacm.addViewTreeFamily(new OctetString("restrictedNotifyView"),
                               new OID("1.3.6.1.2"),
                               new OctetString(), VacmMIB.vacmViewIncluded,
                               StorageType.nonVolatile);

        vacm.addViewTreeFamily(new OctetString("testReadView"),
                               new OID("1.3.6.1.2"),
                               new OctetString(), VacmMIB.vacmViewIncluded,
                               StorageType.nonVolatile);
        vacm.addViewTreeFamily(new OctetString("testReadView"),
                               new OID("1.3.6.1.2.1.1"),
                               new OctetString(), VacmMIB.vacmViewExcluded,
                               StorageType.nonVolatile);
        vacm.addViewTreeFamily(new OctetString("testWriteView"),
                               new OID("1.3.6.1.2.1"),
                               new OctetString(),
                               VacmMIB.vacmViewIncluded,
                               StorageType.nonVolatile);
        vacm.addViewTreeFamily(new OctetString("testNotifyView"),
                               new OID("1.3.6.1.2"),
                               new OctetString(), VacmMIB.vacmViewIncluded,
                               StorageType.nonVolatile);

    }

    @Override
    protected void addNotificationTargets(SnmpTargetMIB targetMIB,
            SnmpNotificationMIB notificationMIB) {
        targetMIB.addDefaultTDomains();

        targetMIB.addTargetAddress(new OctetString("notification"),
                                   TransportDomains.transportDomainUdpIpv4,
                                   new OctetString(new UdpAddress("127.0.0.1/162").getValue()),
                                   200, 1,
                                   new OctetString("notify"),
                                   new OctetString("v2c"),
                                   StorageType.permanent);
        targetMIB.addTargetParams(new OctetString("v2c"),
                                  MessageProcessingModel.MPv2c,
                                  SecurityModel.SECURITY_MODEL_SNMPv2c,
                                  new OctetString("public"),
                                  SecurityLevel.NOAUTH_NOPRIV,
                                  StorageType.permanent);
        notificationMIB.addNotifyEntry(new OctetString("default"),
                                       new OctetString("notify"),
                                       SnmpNotificationMIB.SnmpNotifyTypeEnum.trap,
                                       StorageType.permanent);
    }

    @Override
    protected void addUsmUser(USM usm) {
        UsmUser user = new UsmUser(new OctetString("SHADES"),
                                   AuthSHA.ID,
                                   new OctetString("SHADESAuthPassword"),
                                   PrivDES.ID,
                                   new OctetString("SHADESPrivPassword"));
        usm.addUser(user.getSecurityName(), usm.getLocalEngineID(), user);
        user = new UsmUser(new OctetString("TEST"),
                           AuthSHA.ID,
                           new OctetString("maplesyrup"),
                           PrivDES.ID,
                           new OctetString("maplesyrup"));
        usm.addUser(user.getSecurityName(), usm.getLocalEngineID(), user);
        user = new UsmUser(new OctetString("opennmsUser"),
                           AuthMD5.ID,
                           new OctetString("0p3nNMSv3"),
                           PrivDES.ID,
                           new OctetString("0p3nNMSv3"));
        usm.addUser(user.getSecurityName(), usm.getLocalEngineID(), user);
        user = new UsmUser(new OctetString("SHA"),
                           AuthSHA.ID,
                           new OctetString("SHAAuthPassword"),
                           null,
                           null);
        usm.addUser(user.getSecurityName(), usm.getLocalEngineID(), user);
    }

    protected void initTransportMappings() throws IOException {
        transportMappings = new TransportMapping[1];
        transportMappings[0] =
            new DefaultUdpTransportMapping(new UdpAddress(m_address));
    }



    // override the agent defaults since we are providing all the agent data
    @Override
    protected void registerSnmpMIBs() {
        registerManagedObjects();
    }



    @Override
    protected void unregisterSnmpMIBs() {
        unregisterManagedObjects();
    }

    @Override
    protected void registerManagedObjects() {
        m_moList = createMockMOs();
        Iterator<ManagedObject> moListIter = m_moList.iterator();
        while (moListIter.hasNext()) {
            try {
                server.register(moListIter.next(), null);
            }
            catch (DuplicateRegistrationException ex) {
                ex.printStackTrace();
            }
        }
    }

    @Override
    protected void unregisterManagedObjects() {
        Iterator<ManagedObject> moListIter = m_moList.iterator();
        while (moListIter.hasNext()) {
            server.unregister(moListIter.next(), null);
        }
    }

    protected List<ManagedObject> createMockMOs() {
        return m_moLoader.loadMOs(m_moFile);
    }
    
    private ManagedObject findMOForOid(OID oid) {
        for(ManagedObject mo : m_moList) {
            if (mo.getScope().covers(oid)) {
                return mo;
            }
        }
        return null;
    }
    
    public void updateValue(OID oid, Variable value) {
        ManagedObject mo = findMOForOid(oid);
        assertNotNull("Unable to find oid in mib for mockAgent: "+oid, mo);
        if (mo instanceof Updatable) {
            ((Updatable)mo).updateValue(oid, value);
        }
    }
    
    private void assertNotNull(String string, Object o) {
        if (o == null) {
            throw new IllegalStateException(string);
        }
    }

    public void updateValue(String oid, Variable value) {
        updateValue(new OID(oid), value);
    }
    
    public void updateIntValue(String oid, int val) {
        updateValue(oid, new Integer32(val));
    }
    
    public void updateStringValue(String oid, String val) {
        updateValue(oid, new OctetString(val));
    }
    
    public void updateCounter32Value(String oid, int val) {
        updateValue(oid, new Counter32(val));
    }
    
    public void updateCounter64Value(String oid, long val) {
        updateValue(oid, new Counter64(val));
    }
    
    public void updateValuesFromResource(Resource moFile) {
        unregisterManagedObjects();
        m_moFile = moFile;
        registerManagedObjects();
    }
    
    public String toString() {
        return "MockSnmpAgent["+m_address+"]";
    }
    

}