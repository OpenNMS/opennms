/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
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

package org.opennms.mock.snmp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.net.BindException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.snmp4j.MessageDispatcherImpl;
import org.snmp4j.Session;
import org.snmp4j.TransportMapping;
import org.snmp4j.agent.BaseAgent;
import org.snmp4j.agent.CommandProcessor;
import org.snmp4j.agent.DuplicateRegistrationException;
import org.snmp4j.agent.ManagedObject;
import org.snmp4j.agent.io.ImportModes;
import org.snmp4j.agent.mo.snmp.RowStatus;
import org.snmp4j.agent.mo.snmp.SnmpCommunityMIB;
import org.snmp4j.agent.mo.snmp.SnmpCommunityMIB.SnmpCommunityEntryRow;
import org.snmp4j.agent.mo.snmp.SnmpNotificationMIB;
import org.snmp4j.agent.mo.snmp.SnmpTargetMIB;
import org.snmp4j.agent.mo.snmp.StorageType;
import org.snmp4j.agent.mo.snmp.TransportDomains;
import org.snmp4j.agent.mo.snmp.VacmMIB;
import org.snmp4j.log.ConsoleLogFactory;
import org.snmp4j.log.Log4jLogFactory;
import org.snmp4j.log.LogAdapter;
import org.snmp4j.log.LogFactory;
import org.snmp4j.mp.MPv1;
import org.snmp4j.mp.MPv2c;
import org.snmp4j.mp.MPv3;
import org.snmp4j.mp.MessageProcessingModel;
import org.snmp4j.security.AuthMD5;
import org.snmp4j.security.AuthSHA;
import org.snmp4j.security.PrivDES;
import org.snmp4j.security.SecurityLevel;
import org.snmp4j.security.SecurityModel;
import org.snmp4j.security.SecurityProtocols;
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

/**
 * The <code>MockSnmpAgent</code> class extends the SNMP4J BaseAgent
 * class to provide a mock SNMP agent for SNMP-based OpenNMS tests.
 * Large chunks of code were lifted from the org.snmp4j.agent.test.TestAgent
 * class.
 * 
 * @author Jeff Gehlbach
 */
public class MockSnmpAgent extends BaseAgent implements Runnable {
	
    private static final String PROPERTY_SLEEP_ON_CREATE = "mockSnmpAgent.sleepOnCreate";

    // initialize Log4J logging
    static {
        try {
            Class.forName("org.apache.log4j.Logger");
            LogFactory.setLogFactory(new Log4jLogFactory());
        } catch (Exception e) {
            LogFactory.setLogFactory(new ConsoleLogFactory());
        }

        // Check to see if the pseudorandom byte generator device exists
        if (new File("/dev/urandom").exists()) {

            // If so, use it as the Java entropy gathering device so that we never
            // block on entropy gathering. Don't use the exact string "file:/dev/urandom"
            // because this is treated as a special value inside the JVM. Insert the
            // "." into the path to force it to use the real /dev/urandom device.
            //
            // @see https://bugs.openjdk.java.net/browse/JDK-6202721
            //
            System.setProperty("java.security.egd", "file:/dev/./urandom");
        }

        // Allow us to override default security protocols
        //SNMP4JSettings.setExtensibilityEnabled(true);
        // Override the default security protocols
        //System.setProperty(SecurityProtocols.SECURITY_PROTOCOLS_PROPERTIES, "/org/opennms/mock/snmp/SecurityProtocols.properties");
    }

    private static final LogAdapter s_log = LogFactory.getLogger(MockSnmpAgent.class);

    private AtomicReference<String> m_address = new AtomicReference<String>();
    private AtomicReference<URL> m_moFile = new AtomicReference<URL>();
    private AtomicBoolean m_running = new AtomicBoolean();
    private AtomicBoolean m_stopped = new AtomicBoolean();
    private AtomicReference<List<ManagedObject>> m_moList = new AtomicReference<List<ManagedObject>>();
    private AtomicReference<MockSnmpMOLoader> m_moLoader = new AtomicReference<MockSnmpMOLoader>();
    private AtomicReference<IOException> m_failure = new AtomicReference<IOException>();

    private static File BOOT_COUNT_FILE;

    public static boolean allowSetOnMissingOid = false;

    static {
        File bootCountFile;
        try {
            bootCountFile = File.createTempFile("mockSnmpAgent", "boot");
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(bootCountFile));
            out.writeInt(0);
            out.flush();
            out.close();
        } catch (IOException e) {
            bootCountFile = new File("/dev/null");
        }
        BOOT_COUNT_FILE = bootCountFile;
    }

    public MockSnmpAgent(final File confFile, final URL moFile) {
        super(BOOT_COUNT_FILE, confFile, new CommandProcessor(new OctetString(MPv3.createLocalEngineID(new OctetString("MOCKAGENT")))));
        m_moLoader.set(new PropertiesBackedManagedObject());
        m_moFile.set(moFile);
        agent.setWorkerPool(ThreadPool.create("RequestPool", 4));
    }
    
    /**
     * Creates the mock agent with files to read and store the boot counter,
     * to read and store the agent configuration, and to read the mocked
     * managed objects (MOs), plus a string describing the address and port
     * to bind to.
     * 
     * @param moFile
     * 		a MIB dump file describing the managed objects to be mocked.  The current implementation
     * 		expects a Java properties file, which can conveniently be generated using the Net-SNMP
     * 		utility <code>snmpwalk</code> with the <code>-One</code> option set.
     */
    public MockSnmpAgent(final File confFile, final URL moFile, final String bindAddress) {
        this(confFile, moFile);
        m_address.set(bindAddress);
    }
    
    public static MockSnmpAgent createAgentAndRun(URL moFile, String bindAddress) throws InterruptedException {
        setupLogging();
        try {
            InputStream in = moFile.openStream();
            if (in == null) {
                throw new IllegalArgumentException("could not get InputStream mock object resource; does it exist?  Resource: " + moFile);
            }
            in.close();

        } catch (IOException e) {
            throw new RuntimeException("Got IOException while checking for existence of mock object file: " + e, e);
        }

        final MockSnmpAgent agent = new MockSnmpAgent(new File("/dev/null"), moFile, bindAddress);
        Thread thread = new Thread(agent, agent.getClass().getSimpleName() + '-' + agent.hashCode());
        thread.start();

        try {
            while (!agent.isRunning() && thread.isAlive()) {
                Thread.sleep(10);
            }
        } catch (final InterruptedException e) {
            s_log.warn("MockSnmpAgent: Agent interrupted while starting: " + e.getLocalizedMessage());
            thread.interrupt();
            agent.shutDownAndWait();
            throw e;
        }

        if (!thread.isAlive()) {
            agent.m_running.set(false);
            agent.m_stopped.set(true);
            throw new IllegalStateException("MockSnmpAgent: agent failed to start on address " + bindAddress, agent.m_failure.get());
        }

        if (System.getProperty(PROPERTY_SLEEP_ON_CREATE) != null) {
            long sleep = Long.parseLong(System.getProperty(PROPERTY_SLEEP_ON_CREATE));
            Thread.sleep(sleep);
        }

        return agent;
    }

    private static void setupLogging() {
        if (LogFactory.getLogFactory() == null) {
            LogFactory.setLogFactory(new ConsoleLogFactory());
        }
    }

    public static void main(String[] args) throws UnknownHostException, MalformedURLException {
        LogFactory.setLogFactory(new ConsoleLogFactory());
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

    public static AgentConfigData parseCli(String[] args) throws UnknownHostException, MalformedURLException {

        String dumpFile = null;
        String listenAddr = "127.0.0.1";
        int listenPort = 1691;

        for(int i = 0; i < args.length; i++) {
            if ("-d".equals(args[i]) || "--dump-file".equals(args[i])) {
                if (i+1 >= args.length) {
                    usage("You must specify at least a pathname or URL for the dump file.");
                } else {
                    dumpFile = args[++i];
                }
            }
            else if ("-l".equals(args[i]) || "--listen-addr".equals(args[i])) {
                if (i+1 >= args.length) {
                    usage("You must pass an address argument when using " + args[i] + ".");
                } else {
                    listenAddr = args[++i];
                }
            }
            else if ("-p".equals(args[i]) || "--port".equals(args[i])) {
                if (i+1 >= args.length) {
                    usage("You must pass a port number when using " + args[i] + ".");
                } else {
                    listenPort = Integer.parseInt(args[++i]);
                }
            }

        }

        if (dumpFile == null) {
            usage("You must specify at least a pathname or URL for the dump file.");
        }

        return new AgentConfigData(dumpFile, listenAddr, listenPort);

    }

    private static void usage(String why) {
        System.err.println(why);
        System.err.println("java -jar mock-snmp-agent-jar-with-dependencies.jar -d dump-file [other options]");
        System.err.println("-d, --dump-file {filename}\tPathname or URL of file containing MIB dump");
        System.err.println("-l, --listen-addr {ip-address}\tIP address to bind to (default: all interfaces)");
        System.err.println("-p, --port {udp-port}\tUDP port to listen on (default: 1691)");
        System.exit(1);
    }



    /**
     * <p>
     * Note that this method can hang if your system entropy is not high enough.
     * Here is a stack trace from a test running on JDK 1.8u40:</p>
     * 
     * <pre> 
     * Thread [MockSnmpAgent-1657048932] (Suspended)
     * owns: SecureRandom  (id=26)
     * owns: SecureRandom  (id=27)
     * owns: SecurityProtocols  (id=28)
     * FileInputStream.readBytes(byte[], int, int) line: not available [native method]
     * FileInputStream.read(byte[], int, int) line: 255
     * NativeSeedGenerator(SeedGenerator$URLSeedGenerator).getSeedBytes(byte[]) line: 539
     * SeedGenerator.generateSeed(byte[]) line: 144
     * SecureRandom$SeederHolder.<clinit>() line: 203 [local variables unavailable]
     * SecureRandom.engineNextBytes(byte[]) line: 221
     * SecureRandom.nextBytes(byte[]) line: 468
     * Salt.<init>() line: 54
     * Salt.getInstance() line: 79
     * PrivDES.<init>() line: 57
     * SecurityProtocols.addDefaultProtocols() line: 155
     * MockSnmpAgent.initMessageDispatcher() line: 306
     * MockSnmpAgent(BaseAgent).init() line: 145
     * MockSnmpAgent.run() line: 380
     * Thread.run() line: 745
     * </pre>
     */
    @Override
    protected void initMessageDispatcher() {
        s_log.info("MockSnmpAgent: starting initMessageDispatcher()");
        try {
            dispatcher = new MessageDispatcherImpl();
    
            usm = new USM(SecurityProtocols.getInstance(),
                          agent.getContextEngineID(),
                          updateEngineBoots());
    
            mpv3 = new MPv3(usm);
    
            SecurityProtocols.getInstance().addDefaultProtocols();
            dispatcher.addMessageProcessingModel(new MPv1());
            dispatcher.addMessageProcessingModel(new MPv2c());
            dispatcher.addMessageProcessingModel(mpv3);
            initSnmpSession();
        } finally {
            s_log.info("MockSnmpAgent: finished initMessageDispatcher()");
        }
    }

    @Override
    public void initSnmpSession() {
        s_log.info("MockSnmpAgent: starting initTransportMappings()");
        try {
            super.initSnmpSession();
        } finally {
            s_log.info("MockSnmpAgent: finished initTransportMappings()");
        }
    }

    @Override
    public void initConfigMIB() {
        s_log.info("MockSnmpAgent: starting initConfigMIB()");
        try {
            super.initConfigMIB();
        } finally {
            s_log.info("MockSnmpAgent: finished initConfigMIB()");
        }
    }

    @Override
    public void setupDefaultProxyForwarder() {
        s_log.info("MockSnmpAgent: starting setupDefaultProxyForwarder()");
        try {
            super.setupDefaultProxyForwarder();
        } finally {
            s_log.info("MockSnmpAgent: finished setupDefaultProxyForwarder()");
        }
    }

    @Override
    public void updateSession(Session session) {
        s_log.info("MockSnmpAgent: starting updateSession()");
        try {
            super.updateSession(session);
        } finally {
            s_log.info("MockSnmpAgent: finished updateSession()");
        }
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

    /**
     * Starts the <code>MockSnmpAgent</code> running.  Meant to be called from the
     * <code>start</code> method of class <code>Thread</code>, but could also be
     * used to bring up a standalone mock agent.
     * @see org.snmp4j.agent.BaseAgent#run()
     * 
     * @author Jeff Gehlbach
     */
    @Override
    public void run() {
	s_log.info("MockSnmpAgent: Initializing SNMP Agent");
        try {
            init();
	    s_log.info("MockSnmpAgent: Finished 'init' loading config");
            loadConfig(ImportModes.UPDATE_CREATE);
	    s_log.info("MockSnmpAgent: finished 'loadConfig' adding shutdown hook");
            addShutdownHook();
	    s_log.info("MockSnmpAgent: finished 'addShutdownHook' finishing init");
            finishInit();
	    s_log.info("MockSnmpAgent: finished 'finishInit' running agent");
            super.run();
	    s_log.info("MockSnmpAgent: finished running Agent - setting running to true");
            m_running.set(true);
        } catch (final BindException e) {
        	s_log.error(String.format("MockSnmpAgent: Unable to bind to %s.  You probably specified an invalid address or a port < 1024 and are not running as root. Exception: %s", m_address.get(), e), e);
        } catch (final Throwable t) {
        	s_log.error("MockSnmpAgent: An error occurred while initializing: " + t, t);
        	t.printStackTrace();
        }

        boolean interrupted = false;
	s_log.info("MockSnmpAgent: Initialization Complete processing message until agent is shutdown.");
        while (m_running.get()) {
            try {
                Thread.sleep(10); // fast, Fast, FAST, *FAST*!!!
            } catch (final InterruptedException e) {
                interrupted = true;
                break;
            }
        }

	s_log.info("MockSnmpAgent: Shutdown called stopping agent.");
        for (final TransportMapping<?> transportMapping : transportMappings) {
            try {
                if (transportMapping != null) {
                	transportMapping.close();
                }
            } catch (final IOException t) {
            	s_log.error("MockSnmpAgent: an error occurred while closing the transport mapping " + transportMapping + ": " + t, t);
            }
        }

        m_stopped.set(true);

        s_log.info("MockSnmpAgent: Agent is no longer running.");
        if (interrupted) {
        	Thread.currentThread().interrupt();
        }
    }

    public void shutDown() {
        m_running.set(false);
        m_stopped.set(false);
    }

    public boolean isRunning() {
        return m_running.get();
    }

    public boolean isStopped() {
        return m_stopped.get();
    }

    /** {@inheritDoc} */
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
        SnmpCommunityEntryRow row = communityMIB.getSnmpCommunityEntry().createRow(new OctetString("public2public").toSubIndex(true), com2sec);
        communityMIB.getSnmpCommunityEntry().addRow(row);
    }

    /** {@inheritDoc} */
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

        vacm.addViewTreeFamily(new OctetString("fullReadView"), new OID("1"),
                               new OctetString(), VacmMIB.vacmViewIncluded,
                               StorageType.nonVolatile);
        vacm.addViewTreeFamily(new OctetString("fullWriteView"), new OID("1"),
                               new OctetString(), VacmMIB.vacmViewIncluded,
                               StorageType.nonVolatile);
        vacm.addViewTreeFamily(new OctetString("fullNotifyView"), new OID("1"),
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

    /** {@inheritDoc} */
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

    /** {@inheritDoc} */
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

    /** {@inheritDoc} */
    @Override
    protected void initTransportMappings() throws IOException {
        s_log.info("MockSnmpAgent: starting initTransportMappings()");
        try {
            final MockUdpTransportMapping mapping = new MockUdpTransportMapping(new UdpAddress(m_address.get()), true);
            mapping.setThreadName("MockSnmpAgent-UDP-Transport");
            transportMappings = new TransportMapping<?>[] { mapping };
        } catch (final IOException e) {
            s_log.info("MockSnmpAgent: initTransportMappings() caught an IoException: " + e.getMessage());
            m_failure.set(e);
            throw e;
        } finally {
            s_log.info("MockSnmpAgent: finished initTransportMappings()");
        }
    }
    
    public static final class MockUdpTransportMapping extends DefaultUdpTransportMapping {
        public MockUdpTransportMapping(final UdpAddress udpAddress, final boolean reuseAddress) throws IOException {
            super(udpAddress, reuseAddress);
        }

        public InetAddress getInetAddress() {
            return socket.getLocalAddress();
        }

        public int getPort() {
            return socket.getLocalPort();
        }
    }

    public InetAddress getInetAddress() {
        final TransportMapping<?> mapping = transportMappings[0];
        return ((MockUdpTransportMapping)mapping).getInetAddress();
    }

    public int getPort() {
        final TransportMapping<?> mapping = transportMappings[0];
        return ((MockUdpTransportMapping)mapping).getPort();
    }

    // override the agent defaults since we are providing all the agent data
    /** {@inheritDoc} */
    @Override
    protected void registerSnmpMIBs() {
        s_log.info("MockSnmpAgent: starting registerSnmpMIBs()");
        try {
            registerManagedObjects();
        } finally {
            s_log.info("MockSnmpAgent: finished registerSnmpMIBs()");
        }
    }



    /** {@inheritDoc} */
    @Override
    protected void unregisterSnmpMIBs() {
        unregisterManagedObjects();
    }

    /** {@inheritDoc} */
    @Override
    protected void registerManagedObjects() {
        final List<ManagedObject> mockMOs = Collections.unmodifiableList(createMockMOs());
        m_moList.set(mockMOs);

        for (final ManagedObject mo : mockMOs) {
            try {
                server.register(mo, null);
            }
            catch (final DuplicateRegistrationException ex) {
                s_log.error("MockSnmpAgent: unable to register managed object", ex);
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    protected void unregisterManagedObjects() {
        for (final ManagedObject mo : m_moList.get()) {
            server.unregister(mo, null);
        }
    }

    protected List<ManagedObject> createMockMOs() {
        return m_moLoader.get().loadMOs(m_moFile.get());
    }

    private ManagedObject findMOForOid(OID oid) {
        final List<ManagedObject> list = m_moList.get();
        for(ManagedObject mo : list) {
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

    private void assertNotNull(final String string, final Object o) {
        if (!allowSetOnMissingOid  && o == null) {
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
    
    public void updateValuesFromResource(final URL moFile) {
        unregisterManagedObjects();
        m_moFile.set(moFile);
        registerManagedObjects();
    }
    
    @Override
    public String toString() {
        return "MockSnmpAgent["+m_address.get()+"]";
    }

}
