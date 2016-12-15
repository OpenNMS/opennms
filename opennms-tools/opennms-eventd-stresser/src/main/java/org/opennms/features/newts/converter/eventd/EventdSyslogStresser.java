package org.opennms.features.newts.converter.eventd;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;

import org.apache.commons.dbcp.PoolingConnection;
import org.opennms.netmgt.snmp.InetAddrUtils;

/**
 * @author david
 */
public class EventdSyslogStresser {
    private static String DEFAULT_IPADDRESS = "127.0.0.1";
    private static String REPORT_SPACING = "\t";
    private static String PROPERTY_TRAP_SINK = "trap.sink";
    private static String PROPERTY_AGENT_IPADDRESS = "agent.ipaddress";
    private static String PROPERTY_BATCH_SIZE = "batch.size";
    private static String PROPERTY_BATCH_DELAY = "batch.delay";
    private static String PROPERTY_TRAP_RATE = "trap.rate";
    private static String PROPERTY_TRAP_COUNT = "trap.count";
    private static String PROPERTY_TRAP_COMMUNITY = "trap.community";
    private static String PROPERTY_TRAP_PORT = "trap.port";
    private static String PROPERTY_PERSIST_WAIT = "persist.wait";
    private static String PROPERTY_DELETE_ALL_EVENTS = "delete.all.events";
    private static String PROPERTY_DELETE_TEST_EVENTS = "delete.test.events";
    private static String PROPERTY_DB_SVR = "db.server";
    private static String PROPERTY_DB_NAME = "db.name";
    private static String PROPERTY_DB_USER = "db.user";
    private static String PROPERTY_DB_PW = "db.password";

    private static InetAddress m_agentAddress;
    private static InetAddress m_trapSink;
    private static Integer m_trapPort = Integer.valueOf(1514);
    private static String m_trapCommunity = "public";
    private static Double m_syslogRate = Double.valueOf(1); // seconds
    private static Integer m_syslogCount = Integer.valueOf(1);
    private static Integer m_batchDelay = Integer.valueOf(1); // seconds
    private static Integer m_batchSize = m_syslogCount;
    private static int m_batchCount = 1;
    private static int m_persistWait = 60;
    private static boolean m_deleteAllEvents = false;
    private static boolean m_deleteTestEvents = false;
    private static String m_dbSvr = "127.0.0.1";
    private static String m_dbName = "opennms";
    private static String m_dbUser = "opennms";
    private static String m_dbPass = "opennms";
    
    private static DatagramSocket datagramSocket = null;
    
    @SuppressWarnings("unused")
    private static long m_sleepMillis = 0;

    /**
     * EventdStresser Main
     * 
     * @param args
     * @throws UnknownHostException 
     */
//    public static void main(String[] args) {
//
//        parseArgs(args);
//
//        setIpAddresses();
//
//        System.out.println("Commencing the Eventd Stress Test...");
//        executeStressTest();
//
//    }
    
    public static void main(String[] args) throws UnknownHostException {

       // parseArgs(args);

        //setIpAddresses();
//    	System.out.println("args[0]"+args[0]);
//    	System.out.println("args[1]"+args[1]);
//    	System.out.println("args[2]"+args[2]);
    	
        m_syslogRate = Double.valueOf(args[0]); // seconds
        m_syslogCount = Integer.valueOf(args[1]);
        m_agentAddress = InetAddrUtils.addr(args[2]);
        m_batchCount = Integer.valueOf(args[3]);
        
        m_batchSize = m_syslogCount;
    	
        System.out.println("m_syslogRate : "+m_syslogRate);
    	System.out.println("m_syslogCount : "+m_syslogCount);
    	System.out.println("m_agentAddress : "+m_agentAddress);
    	
        System.out.println("Commencing the Eventd Stress Test...");
        executeStressTest();

    }

    private static void executeStressTest() {
        try {
            stressEventd();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void setIpAddresses() {
        try {
            m_trapSink = InetAddress.getByName(DEFAULT_IPADDRESS);
            m_agentAddress = m_trapSink;
        } catch (UnknownHostException e1) {
            e1.printStackTrace();
            System.exit(1);
        }
    }

    private static void parseArgs(String[] args) {

        if (args.length > 0) {
            printSystemUsageReport();
            System.exit(0);

        }
        processSystemProperties();
    }

    private static void printSystemUsageReport() {
        System.out.println();
        System.out.println("Allowed Properties:");
        System.out.println(REPORT_SPACING + PROPERTY_AGENT_IPADDRESS + printDefault(DEFAULT_IPADDRESS));
        System.out.println(REPORT_SPACING + PROPERTY_TRAP_COUNT + printDefault(m_batchSize));
        System.out.println(REPORT_SPACING + PROPERTY_TRAP_SINK + printDefault(DEFAULT_IPADDRESS));
        System.out.println(REPORT_SPACING + PROPERTY_TRAP_PORT + printDefault(m_trapPort));
        System.out.println(REPORT_SPACING + PROPERTY_TRAP_COMMUNITY + printDefault(m_trapCommunity));
        System.out.println(REPORT_SPACING + PROPERTY_TRAP_RATE + printDefault(m_syslogRate));
        System.out.println(REPORT_SPACING + PROPERTY_BATCH_DELAY + printDefault(m_batchDelay));
        System.out.println(REPORT_SPACING + PROPERTY_BATCH_SIZE + printDefault(m_batchSize));
        System.out.println(REPORT_SPACING + PROPERTY_PERSIST_WAIT + printDefault(m_persistWait));
        System.out.println(REPORT_SPACING + PROPERTY_DELETE_ALL_EVENTS + printDefault(m_deleteAllEvents));
        System.out.println(REPORT_SPACING + PROPERTY_DELETE_TEST_EVENTS + printDefault(m_deleteTestEvents));
        System.out.println(REPORT_SPACING + PROPERTY_DB_SVR + printDefault(m_dbSvr));
        System.out.println(REPORT_SPACING + PROPERTY_DB_NAME + printDefault(m_dbName));
        System.out.println(REPORT_SPACING + PROPERTY_DB_USER + printDefault(m_dbUser));
        System.out.println(REPORT_SPACING + PROPERTY_DB_PW + printDefault(m_dbPass));

        System.out.println();
        System.out.println("Example:");
        System.out.println(REPORT_SPACING + "java -D\" + PROPERTY_TRAP_SINK + \"=127.0.0.1\" + \" -D\" + PROPERTY_TRAP_RATE + \"=100 -jar opennms-eventd-stresser.jar");
        System.out.println();
        System.out.println();
    }

    private static <T extends Object> String printDefault(T str) {
        if (str == null) {
            return "";
        }
        return " (default:" + str.toString() + ")";
    }

    private static void processSystemProperties() throws IllegalArgumentException, NumberFormatException {

        String property = System.getProperty(PROPERTY_DELETE_ALL_EVENTS);
        if (property != null) {
            m_deleteAllEvents = Boolean.valueOf(property);
            System.out.println("Using delete all events flag: " + m_deleteAllEvents);
        }

        property = System.getProperty(PROPERTY_DELETE_TEST_EVENTS);
        if (property != null) {
            m_deleteTestEvents = Boolean.valueOf(property);
            System.out.println("Using delete test events flag: " + m_deleteTestEvents);
        }

        property = System.getProperty(PROPERTY_AGENT_IPADDRESS);
        if (property != null) {
            try {
                m_agentAddress = InetAddress.getByName(property);
                System.out.println("Using agent address: " + m_agentAddress);
            } catch (UnknownHostException e) {
                throw new IllegalArgumentException("Invalid Agent address.", e);
            }
        }

        property = System.getProperty(PROPERTY_TRAP_SINK);
        if (property != null) {
            try {
                m_trapSink = InetAddress.getByName(property);
                System.out.println("Using trap sink: " + m_trapSink);
            } catch (UnknownHostException e) {
                throw new IllegalArgumentException("Invalid Destination address.", e);
            }
        }

        property = System.getProperty(PROPERTY_TRAP_PORT);
        if (property != null) {
            m_trapPort = Integer.getInteger(PROPERTY_TRAP_PORT);
            System.out.println("Using Trap port: " + m_trapPort);
        }

        property = System.getProperty(PROPERTY_TRAP_COMMUNITY);
        if (property != null) {
            m_trapCommunity = property;
            System.out.println("Using Trap Community name: " + m_trapCommunity);
        }

        property = System.getProperty(PROPERTY_TRAP_COUNT);
        if (property != null) {
            m_syslogCount = Integer.getInteger(PROPERTY_TRAP_COUNT);
            m_batchSize = m_syslogCount;
            m_batchCount = 1;
            System.out.println("Using Trap count: " + m_syslogCount);
        }

        property = System.getProperty(PROPERTY_TRAP_RATE);
        if (property != null) {
        	m_syslogRate = Integer.getInteger(PROPERTY_TRAP_RATE).doubleValue();
            System.out.println("Using Trap rate: " + m_syslogRate);
        }

        property = System.getProperty(PROPERTY_BATCH_DELAY);
        if (property != null) {
            m_batchDelay = Integer.getInteger(PROPERTY_BATCH_DELAY);
            System.out.println("Using batch delay: " + m_batchDelay);
        }

        property = System.getProperty(PROPERTY_BATCH_SIZE);
        if (property != null) {
            m_batchSize = Integer.getInteger(PROPERTY_BATCH_SIZE);
            System.out.println("Using batch size: " + m_batchSize);
        }

        property = System.getProperty(PROPERTY_PERSIST_WAIT);
        if (property != null) {
            m_persistWait = Integer.getInteger(PROPERTY_PERSIST_WAIT);
            System.out.println("Using Event persistence wait period of: " + m_persistWait);
        }

        m_batchCount = m_syslogCount.intValue() / m_batchSize.intValue();
        System.out.println("Using batch count: " + m_batchCount);
        
        property = System.getProperty(PROPERTY_DB_SVR);
        if (property != null) {
        	m_dbSvr = property;
        }
        
        property = System.getProperty(PROPERTY_DB_NAME);
        if (property != null) {
        	m_dbName = property;
        }
        
        property = System.getProperty(PROPERTY_DB_USER);
        if (property != null) {
        	m_dbUser = property;
        }
        
        property = System.getProperty(PROPERTY_DB_PW);
        if (property != null) {
        	m_dbPass = property;
        }
        

    }

    public static void stressEventd() throws ClassNotFoundException, SQLException, IllegalStateException, InterruptedException {

//        Connection connection = createConnection();
//        PoolingConnection pool = new PoolingConnection(connection);
//        if (m_deleteAllEvents) {
//            System.out.println("Delete events from opennms DB");
//            deleteAllEvents(connection);
//        }

//        int initialEventCount = getEventCount(pool).intValue();
//        System.out.println("Initial Event Count: " + initialEventCount);

        if (m_batchCount < 1) {
            throw new IllegalArgumentException("Batch count of < 1 is not allowed.");
        } else if (m_batchCount > m_syslogCount) {
            throw new IllegalArgumentException("Batch count is > than syslog count.");
        }

        long startTimeInMillis = Calendar.getInstance().getTimeInMillis();
//        int syslogsSent = sendSyslogs(pool, startTimeInMillis, initialEventCount);
        int syslogsSent = sendSyslogs(startTimeInMillis);

       // int currentEventCount = getEventCount(pool) - initialEventCount;
        //int finalEventCount = currentEventCount;
        long beginPersistenceCheck = Calendar.getInstance().getTimeInMillis();

        System.out.println("Watching Event Queue to complete persistence for " + m_persistWait + " milliseconds...");
        int equalCount = 0;
//        while (currentEventCount < syslogsSent) {
//            Thread.sleep(1000);
//            m_sleepMillis += 1000;
//
//            currentEventCount = getEventCount(pool).intValue() - initialEventCount;
//
//            if (currentEventCount == finalEventCount) {
//                equalCount++;
//            } else {
//                equalCount = 0;
//            }
//
//            finalEventCount = currentEventCount;
//
//            System.out.println("Persist wait time (secs): " + ((System.currentTimeMillis() - beginPersistenceCheck) / 1000));
//           // System.out.println("Current Event count: " + Integer.valueOf(finalEventCount).toString());
//
//            if (Calendar.getInstance().getTimeInMillis() - beginPersistenceCheck > m_persistWait) {
//                System.out.println("Waited " + ((System.currentTimeMillis() - beginPersistenceCheck) / 1000) + " millisecs for queue to flush.  Apparently missed " + (syslogsSent - finalEventCount)
//                        + " syslogs :(");
//                break;
//            }
//
//            if (equalCount > 3) {
//                System.out.println("Appears that event persistence is completed.");
//                break;
//            }
//        }

        if (m_deleteTestEvents) {
            deleteTestEvents();
        }

        //pool.close();
        //connection.close();

        systemReport(startTimeInMillis, syslogsSent);
    }

    private static void systemReport(long beginMillis, int trapsSent) {

        System.out.println("  Syslogs sent: " + trapsSent);
        //System.out.println("Events persisted DB: " + finalEventCount);
        long totalMillis = Calendar.getInstance().getTimeInMillis() - beginMillis;
        // long processingMillis = totalMillis - m_sleepMillis;
        // Long processingSeconds = processingMillis/1000;
        Long totalSeconds = totalMillis / 1000L;
        System.out.println("Total Elapsed time (secs): " + totalSeconds);
        //System.out.println("Events per second (persisted): " + finalEventCount / totalSeconds.doubleValue());
        System.out.println();
    }

    private static int sendSyslogs(long beginMillis) throws IllegalStateException, InterruptedException, SQLException {

        m_sleepMillis = 0;
        int totalSyslogsSent = 0;

        System.out.println("Sending " + m_syslogCount + " syslogs in " + m_batchCount + " batches with a batch interval of " + m_batchDelay.toString() + " seconds...");
        for (int i = 1; i <= m_batchCount; i++) {

            Long batchBegin = Calendar.getInstance().getTimeInMillis();
            Double currentRate = 0.0;
            Integer batchSyslogsSent = 0;
            Long batchElapsedMillis = 0L;
            System.out.println("Sending batch " + i + " of " + Integer.valueOf(m_batchCount) + " batches of " + m_batchSize.intValue() + " syslogs at the rate of " + m_syslogRate.toString()
                    + " traps/sec...");
            System.out.println("m_batchSize : "+m_batchSize.doubleValue());
            System.out.println("m_syslogRate : "+m_syslogRate.doubleValue());

            System.out.println("Estimated time to send: " + m_batchSize.doubleValue() / m_syslogRate.doubleValue() + " seconds");

            while (batchSyslogsSent.intValue() < m_batchSize.intValue()) {

                if (currentRate <= m_syslogRate || batchElapsedMillis == 0) {
                	batchSyslogsSent += sendSyslog();
                } else {
                    Thread.sleep(1);
                    m_sleepMillis++;
                }

                batchElapsedMillis = Calendar.getInstance().getTimeInMillis() - batchBegin;
                currentRate = batchSyslogsSent.doubleValue() / batchElapsedMillis.doubleValue() * 1000.0;

                if (batchElapsedMillis % 1000 == 0) {
                    System.out.print(".");
                }

            }

            System.out.println();
            totalSyslogsSent += batchSyslogsSent;
            System.out.println("   Actual time to send: " + (batchElapsedMillis / 1000.0 + " seconds"));
            System.out.println("Elapsed Time (secs): " + ((System.currentTimeMillis() - beginMillis) / 1000L));
            System.out.println("         Traps sent: " + Integer.valueOf(totalSyslogsSent).toString());
            //Integer currentEventCount = getEventCount(pool) - initialEventCount;
            //System.out.println("Current Event count: " + currentEventCount.toString());
            System.out.println();
            Thread.sleep(m_batchDelay.longValue() * 1000L);
            m_sleepMillis += m_batchDelay.longValue() * 1000L;
        }

        int remainingTraps = m_syslogCount - totalSyslogsSent;
        System.out.println("Sending batch remainder of " + remainingTraps + " traps...");
        Long batchBegin = Calendar.getInstance().getTimeInMillis();
        Double currentRate = 0.0;
        Long batchSyslogsSent = 0L;
        Long elapsedMillis = 0L;
        while (batchSyslogsSent.intValue() < remainingTraps) {

            if (currentRate <= m_syslogRate || elapsedMillis == 0) {
            	batchSyslogsSent += sendSyslog();
            } else {
                Thread.sleep(1);
                m_sleepMillis++;
            }

            elapsedMillis = Calendar.getInstance().getTimeInMillis() - batchBegin;
            currentRate = batchSyslogsSent.doubleValue() / elapsedMillis.doubleValue() * 1000.0;
        }

        totalSyslogsSent += batchSyslogsSent;
        System.out.println("Elapsed Time (secs): " + ((System.currentTimeMillis() - beginMillis) / 1000L));
        System.out.println("         Syslogs sent: " + Integer.valueOf(totalSyslogsSent).toString());
        //Integer currentEventCount = getEventCount(pool) - initialEventCount;
        //System.out.println("Current Event count: " + currentEventCount.toString());
        return totalSyslogsSent;
    }
    
    private static int sendSyslog() {
        int syslogsSent = 0;
        try {
            //builder.send(m_trapSink.getHostAddress(), m_trapPort.intValue(), m_trapCommunity);

            byte[] bytes = "<34>1 2010-08-19T22:14:15.000Z localhost - - - - BOMfoo0: load test 0 on tty1\0".getBytes();
            
            //DatagramPacket pkt = new DatagramPacket(bytes, bytes.length, InetAddrUtils.addr("127.0.0.1"), 514);
            DatagramPacket pkt = new DatagramPacket(bytes, bytes.length, m_agentAddress, m_trapPort);
            if(datagramSocket == null){
            	datagramSocket = new DatagramSocket();
            }
            datagramSocket.send(pkt);
            syslogsSent++;
        } catch (Exception e) {
            throw new IllegalStateException("Caught Exception sending syslog.", e);
        }
        return syslogsSent;
    }
    
    private static String executeCommand(String command) {

		StringBuffer output = new StringBuffer();

		Process p;
		try {
			p = Runtime.getRuntime().exec(command);
			p.waitFor();
			BufferedReader reader =
                            new BufferedReader(new InputStreamReader(p.getInputStream()));

                        String line = "";
			while ((line = reader.readLine())!= null) {
				output.append(line + "\n");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return output.toString();

	}

    private static int deleteAllEvents(Connection connection) throws SQLException {
        int rows = connection.createStatement().executeUpdate("delete from events");
        System.out.println("Rows deleted: " + rows);
        return rows;
    }

    private static int deleteTestEvents() throws SQLException, ClassNotFoundException {
        int rows = createConnection().createStatement().executeUpdate("delete from events where (eventuei = 'MATCH-ANY-UEI' or eventuei = 'uei.opennms.org/traps/eventTrap')");
        System.out.println("Rows deleted: " + rows);
        return rows;
    }

    private static Connection createConnection() throws ClassNotFoundException, SQLException {
        Class.forName("org.postgresql.Driver");

        Connection connection = DriverManager.getConnection("jdbc:postgresql://" + m_dbSvr + ":5432/"+m_dbName, m_dbUser, m_dbPass);
        return connection;
    }

    synchronized public static Integer getEventCount(PoolingConnection pool) throws SQLException {
        Statement statement = pool.createStatement();
        ResultSet result = statement.executeQuery("select count(*) from events where (eventuei = 'MATCH-ANY-UEI' or eventuei = 'uei.opennms.org/traps/eventTrap')");
        result.next();
        int count = result.getInt(1);
        result.close();
        statement.close();
        return Integer.valueOf(count);
    }

}
