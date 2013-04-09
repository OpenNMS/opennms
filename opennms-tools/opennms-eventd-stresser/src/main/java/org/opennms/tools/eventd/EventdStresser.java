package org.opennms.tools.eventd;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.dbcp.PoolingConnection;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpTrapBuilder;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.SnmpV1TrapBuilder;
import org.opennms.netmgt.snmp.SnmpValue;

/**
 * 
 * @author david
 *
 */
public class EventdStresser 
{
	private static final String DEFAULT_IPADDRESS = "127.0.0.1";

	private static final String REPORT_SPACING = "\t";

	private static final String PROPERTY_TRAP_SINK = "trap.sink";

	private static final String PROPERTY_AGENT_IPADDRESS = "agent.ipaddress";

	private static final String PROPERTY_BATCH_SIZE = "batch.size";

	private static final String PROPERTY_BATCH_DELAY = "batch.delay";

	private static final String PROPERTY_TRAP_RATE = "trap.rate";

	private static final String PROPERTY_TRAP_COUNT = "trap.count";

	private static final String PROPERTY_TRAP_COMMUNITY = "trap.community";

	private static final String PROPERTY_TRAP_PORT = "trap.port";

	private static InetAddress m_agentAddress;;
	
    private static InetAddress m_trapSink;
	private static Integer m_trapPort = Integer.valueOf(162);
	private static String m_trapCommunity = "public";
	
	//seconds
	private static Double m_trapRate = Double.valueOf(100);	
	private static Integer m_trapCount = Integer.valueOf(10000);
	
	//seconds
	private static Integer m_batchDelay = Integer.valueOf(1);
	private static Integer m_batchSize = m_trapCount;
	private static int m_batchCount = 1;
	
	/**
	 * EventdStresser Main
	 * @param args
	 */
	public static void main( String[] args ) {
		
		parseArgs(args);
		
        setIpAddresses();
        
        System.out.println( "Commensing the Eventd Stress Test..." );        
		executeStressTest();
		
    }

	private static void executeStressTest() {
		SnmpTrapBuilder builder = null;
		try {
			builder = createBuilder();
			stressEventd(builder);
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
    	System.out.println(REPORT_SPACING+PROPERTY_AGENT_IPADDRESS+printDefault(DEFAULT_IPADDRESS));
    	System.out.println(REPORT_SPACING+PROPERTY_TRAP_COUNT+printDefault(m_batchSize));
    	System.out.println(REPORT_SPACING+PROPERTY_TRAP_SINK+printDefault(DEFAULT_IPADDRESS));
    	System.out.println(REPORT_SPACING+PROPERTY_TRAP_PORT+printDefault(m_trapPort));
    	System.out.println(REPORT_SPACING+PROPERTY_TRAP_COMMUNITY+printDefault(m_trapCommunity));
    	System.out.println(REPORT_SPACING+PROPERTY_TRAP_RATE+printDefault(m_trapRate));
    	System.out.println(REPORT_SPACING+PROPERTY_BATCH_DELAY+printDefault(m_batchDelay));
    	System.out.println(REPORT_SPACING+PROPERTY_BATCH_SIZE+printDefault(m_batchSize));
    	
    	System.out.println();
    	System.out.println("Example:");
    	System.out.println(REPORT_SPACING+"java -jar opennms-eventd-stresser.jar -D"+PROPERTY_TRAP_SINK+"=127.0.0.1"+" -D"+PROPERTY_TRAP_RATE+"=100");
    	System.out.println();
    	System.out.println();
	}

	private static <T extends Object> String printDefault(T str) {
		if (str == null) {
			return "";
		}
		return " (default:"+str.toString()+")";
	}

	private static void processSystemProperties() throws IllegalArgumentException, NumberFormatException {
		
    	String property = System.getProperty(PROPERTY_AGENT_IPADDRESS);
    	if (property != null) {
    		try {
    			m_agentAddress = InetAddress.getByName(property);
    			System.out.println("Using agent address: "+m_agentAddress);
    		} catch (UnknownHostException e) {
    			throw new IllegalArgumentException("Invalid Agent address.", e);
    		}
    	}
		
    	property = System.getProperty(PROPERTY_TRAP_SINK);
    	if (property != null) {
    		try {
    			m_trapSink = InetAddress.getByName(property);
    			System.out.println("Using trap sink: "+m_trapSink);
    		} catch (UnknownHostException e) {
    			throw new IllegalArgumentException("Invalid Destination address.", e);
    		}
    	}
    	
    	property = System.getProperty(PROPERTY_TRAP_PORT);
    	if (property != null) {
    		m_trapPort = Integer.getInteger(PROPERTY_TRAP_PORT);
    		System.out.println("Using Trap port: "+m_trapPort);
    	}
    	
    	property = System.getProperty(PROPERTY_TRAP_COMMUNITY);
    	if (property != null) {
    		m_trapCommunity = property;
    		System.out.println("Using Trap Community name: "+m_trapCommunity);
    	}
    	
    	property = System.getProperty(PROPERTY_TRAP_COUNT);
    	if (property != null) {
    		m_trapCount = Integer.getInteger(PROPERTY_TRAP_COUNT);
    		m_batchSize = m_trapCount;
    		m_batchCount = 1;
    		System.out.println("Using Trap count: "+m_trapCount);
    	}
    	
    	property = System.getProperty(PROPERTY_TRAP_RATE);
    	
    	System.out.println(System.getProperties());
    	
    	if (property != null) {
    		System.out.println("Default Trap rate: "+m_trapRate);
    		m_trapRate = Integer.getInteger(PROPERTY_TRAP_RATE).doubleValue();
    		System.out.println("Using Trap rate: "+m_trapRate);
    	}
    	
    	property = System.getProperty(PROPERTY_BATCH_DELAY);
    	if (property != null) {
    		m_batchDelay = Integer.getInteger(PROPERTY_BATCH_DELAY);
    		System.out.println("Using batch delay: "+m_batchDelay);
    	}
    	
    	property = System.getProperty(PROPERTY_BATCH_SIZE);
    	if (property != null) {
    		m_batchSize = Integer.getInteger(PROPERTY_BATCH_SIZE);
    		System.out.println("Using batch size: "+m_batchSize);
    	}
    	
    	m_batchCount = m_trapCount.intValue() / m_batchSize.intValue();
    	System.out.println("Using batch count: "+m_batchCount);
    	
	}

	public static void stressEventd(final SnmpTrapBuilder builder) throws ClassNotFoundException, SQLException, IllegalStateException, InterruptedException  {
    	
    	System.out.println("Delete events from opennms DB");
    	Connection connection = createConnection();
    	deleteAllEvents(connection);
    	PoolingConnection pool = new PoolingConnection(connection);

    	int initialEventCount = getEventCount(pool).intValue();
    	System.out.println("Initial Event Count: "+initialEventCount);
    	
    	long startTimeInMillis = Calendar.getInstance().getTimeInMillis();

    	if (m_batchCount < 1) {
    		throw new IllegalArgumentException("Batch count of < 1 is not allowed.");
    	} else if (m_batchCount > m_trapCount ){
    		throw new IllegalArgumentException("Batch count is > than trap count.");
    	}
    	
    	int trapsSent = sendTraps(builder, pool, startTimeInMillis);
		
    	int finalEventCount = 0;
    	long beginQueueCheck = Calendar.getInstance().getTimeInMillis();
    	
    	System.out.println("Watching Event Queue to complete persistence...");
    	while (finalEventCount < trapsSent) {
    		Thread.sleep(1000);
    		finalEventCount = getEventCount(pool).intValue();
        	System.out.println("Persist wait time (secs): "+((System.currentTimeMillis() - startTimeInMillis)/1000));
    		System.out.println("Current Event count: "+ Integer.valueOf(finalEventCount).toString());
    		
    		if (Calendar.getInstance().getTimeInMillis() - beginQueueCheck > 10000) {
    			System.out.println("Waited 60 secs for queue to flush.  Apparently missed "+(trapsSent - finalEventCount)+ " traps :(");
    			break;
    		}
    	}
    	
    	pool.close();
    	connection.close();
    	
    	systemReport(startTimeInMillis, trapsSent, finalEventCount);
    }

	private static void systemReport(long beginMillis, int trapsSent, int finalEventCount) {
		System.out.println("  Traps sent: "+trapsSent);
    	System.out.println("Events in DB: "+finalEventCount);
    	Long totalSeconds = (Calendar.getInstance().getTimeInMillis() - beginMillis)/1000;
    	System.out.println("Total Elapsed time (secs): "+totalSeconds);
    	System.out.println("Events per second (persisted): "+trapsSent/totalSeconds.doubleValue());
    	System.out.println();
	}
	
	private static int sendTraps(final SnmpTrapBuilder builder, PoolingConnection pool, long beginMillis) throws IllegalStateException, InterruptedException, SQLException {
		
		int totalTrapsSent = 0;
		
		System.out.println("Sending "+m_trapCount+" traps in "+m_batchCount+" batches with a batch interval of "+m_batchDelay.toString()+" seconds...");
		for (int i=1; i<=m_batchCount; i++) {

			Long batchBegin = Calendar.getInstance().getTimeInMillis();
			Double currentRate = 0.0;
			Integer batchTrapsSent = 0;
			Long batchElapsedMillis = 0L;
			System.out.println("Sending batch "+i+" of "+Integer.valueOf(m_batchCount)+" batches of "+m_batchSize.intValue()+" traps at the rate of "+m_trapRate.toString()+" traps/sec...");
			System.out.println("Estimated time to send: "+m_batchSize.doubleValue()/m_trapRate.doubleValue() +" seconds");
			
			while (batchTrapsSent.intValue() < m_batchSize.intValue()) {

				if (currentRate <= m_trapRate || batchElapsedMillis == 0) {
					batchTrapsSent += sendTrap(builder);
				} else {
					Thread.sleep(1);
				}

				batchElapsedMillis = Calendar.getInstance().getTimeInMillis() - batchBegin;					
				currentRate = batchTrapsSent.doubleValue() / batchElapsedMillis.doubleValue() *1000.0;
				
				if (batchElapsedMillis % 1000 == 0) {
					System.out.print(".");
				}
				
			}

			System.out.println();
			totalTrapsSent+=batchTrapsSent;
			System.out.println("   Actual time to send: "+(batchElapsedMillis/1000.0 + " seconds"));
			System.out.println("Elapsed Time (secs): "+((System.currentTimeMillis() - beginMillis)/1000L));
			System.out.println("         Traps sent: "+ Integer.valueOf(totalTrapsSent).toString());
			System.out.println("Current Event count: "+ getEventCount(pool).toString());
			System.out.println();
			Thread.sleep(m_batchDelay.longValue()*1000L);
		}
		
		int remainingTraps = m_trapCount - totalTrapsSent;
		System.out.println("Sending batch remainder of "+remainingTraps+" traps...");
		Long batchBegin = Calendar.getInstance().getTimeInMillis();
		Double currentRate = 0.0;
		Long batchTrapsSent = 0L;
		Long elapsedMillis = 0L;
		while (batchTrapsSent.intValue() < remainingTraps) {

			if (currentRate <= m_trapRate || elapsedMillis == 0) {
				batchTrapsSent += sendTrap(builder);
			} else {
				Thread.sleep(1);
			}

			elapsedMillis = Calendar.getInstance().getTimeInMillis() - batchBegin;					
			currentRate = batchTrapsSent.doubleValue() / elapsedMillis.doubleValue() *1000.0;
		}

		totalTrapsSent+=batchTrapsSent;
		System.out.println("Elapsed Time (secs): "+((System.currentTimeMillis() - beginMillis)/1000L));
		System.out.println("         Traps sent: "+ Integer.valueOf(totalTrapsSent).toString());
		System.out.println("Current Event count: "+ getEventCount(pool).toString());
		return totalTrapsSent;
	}

	private static int sendTrap(final SnmpTrapBuilder builder) {
		int trapsSent = 0;
		try {
			builder.send(m_trapSink.getHostAddress(), m_trapPort.intValue(), m_trapCommunity);
			trapsSent++;
		} catch (Exception e) {
			throw new IllegalStateException("Caught Exception sending trap.", e);
		}
		return trapsSent;
	}

	private static void deleteAllEvents(Connection connection) throws SQLException {
		int rows = connection.createStatement().executeUpdate("delete from events");
    	System.out.println("Row deleted: "+rows);
	}

	private static Connection createConnection() throws ClassNotFoundException, SQLException {
		Class.forName("org.postgresql.Driver");
    	
    	Connection connection = DriverManager.getConnection("jdbc:postgresql://"+m_trapSink.getHostAddress()+":5432/opennms", "opennms", "opennms");
		return connection;
	}
    
    synchronized public static Integer getEventCount(PoolingConnection pool) throws SQLException {
    	Statement statement = pool.createStatement();
    	ResultSet result = statement.executeQuery("select count(*) from events");
    	result.next();
    	int count = result.getInt(1);
    	result.close();
    	statement.close();
		return Integer.valueOf(count);
    }
    
    public static SnmpTrapBuilder createBuilder() throws IllegalArgumentException {
    	
        SnmpV1TrapBuilder builder = SnmpUtils.getV1TrapBuilder();
        
        builder.setAgentAddress(m_agentAddress);
		
		SnmpObjId enterpriseOid = SnmpObjId.get(".1.3.6.1.4.1.5813.1");
		builder.setEnterprise(enterpriseOid);
		builder.setTimeStamp(0);
		
        builder.setGeneric(6);
        builder.setSpecific(1);
        builder.setTimeStamp(1);
        
        //dbid
        SnmpObjId dbIdOid = SnmpObjId.get(".1.3.6.1.4.1.5813.2.1");
        SnmpValue dbIdValue = SnmpUtils.getStrategy().getValueFactory().getOctetString("-1".getBytes());
        builder.addVarBind(dbIdOid, dbIdValue);
        
        //distPoller
        SnmpObjId distPollerOid = SnmpObjId.get(".1.3.6.1.4.1.5813.2.2");
        SnmpValue distPollerValue = SnmpUtils.getStrategy().getValueFactory().getOctetString(m_agentAddress.getHostAddress().getBytes());
        builder.addVarBind(distPollerOid, distPollerValue);
        
        //create-time
        SnmpObjId createTimeOid = SnmpObjId.get(".1.3.6.1.4.1.5813.2.3");
        Date createTime = Calendar.getInstance().getTime();
        String formattedTime = DateFormat.getDateTimeInstance().format(createTime);
        SnmpValue createTimeValue = SnmpUtils.getStrategy().getValueFactory().getOctetString(formattedTime.getBytes());
        builder.addVarBind(createTimeOid, createTimeValue);
        
        //master-station
        SnmpObjId masterStationOid = SnmpObjId.get(".1.3.6.1.4.1.5813.2.4");
        SnmpValue masterStationValue = SnmpUtils.getStrategy().getValueFactory().getOctetString(m_trapSink.getHostAddress().getBytes());
        builder.addVarBind(masterStationOid, masterStationValue);
        
        //uei
        SnmpObjId ueiOid = SnmpObjId.get(".1.3.6.1.4.1.5813.2.5");
        SnmpValue ueiValue = SnmpUtils.getStrategy().getValueFactory().getOctetString("uei.opennms.org/test/EventdStressTest".getBytes());
        builder.addVarBind(ueiOid, ueiValue);
        
//        source,
        SnmpObjId sourceOid = SnmpObjId.get(".1.3.6.1.4.1.5813.2.6");
        SnmpValue sourceValue = SnmpUtils.getStrategy().getValueFactory().getOctetString("EventdStresser".getBytes());
        builder.addVarBind(sourceOid, sourceValue);
        
//        nodeid,
        SnmpObjId nodeIdOid = SnmpObjId.get(".1.3.6.1.4.1.5813.2.7");
        SnmpValue nodeIdValue = SnmpUtils.getStrategy().getValueFactory().getOctetString("1".getBytes());
        builder.addVarBind(nodeIdOid, nodeIdValue);
        
//        time,
        SnmpObjId timeOid = SnmpObjId.get(".1.3.6.1.4.1.5813.2.8");
        SnmpValue timeValue = SnmpUtils.getStrategy().getValueFactory().getOctetString(formattedTime.getBytes());
        builder.addVarBind(timeOid, timeValue);
        
//        host,
        SnmpObjId hostOid = SnmpObjId.get(".1.3.6.1.4.1.5813.2.9");
        SnmpValue hostValue = SnmpUtils.getStrategy().getValueFactory().getOctetString(m_agentAddress.getHostAddress().getBytes());
        builder.addVarBind(hostOid, hostValue);
        
//        interface,
        SnmpObjId interfaceOid = SnmpObjId.get(".1.3.6.1.4.1.5813.2.10");
        SnmpValue interfaceValue = SnmpUtils.getStrategy().getValueFactory().getOctetString(m_agentAddress.getHostAddress().getBytes());
        builder.addVarBind(interfaceOid, interfaceValue);
        
//        snmphost,
        SnmpObjId snmpHostOid = SnmpObjId.get(".1.3.6.1.4.1.5813.2.11");
        SnmpValue snmpValue = SnmpUtils.getStrategy().getValueFactory().getOctetString(m_agentAddress.getHostAddress().getBytes());
        builder.addVarBind(snmpHostOid, snmpValue);
        
//        service,
        SnmpObjId serviceOid = SnmpObjId.get(".1.3.6.1.4.1.5813.2.12");
        SnmpValue serviceValue = SnmpUtils.getStrategy().getValueFactory().getOctetString("SNMP".getBytes());
        builder.addVarBind(serviceOid, serviceValue);
        
//        descr,
        SnmpObjId descrOid = SnmpObjId.get(".1.3.6.1.4.1.5813.2.13");
        SnmpValue descrValue = SnmpUtils.getStrategy().getValueFactory().getOctetString("Eventd Stressor Trap".getBytes());
        builder.addVarBind(descrOid, descrValue);
        
//        logmsg,
        SnmpObjId logMsgOid = SnmpObjId.get(".1.3.6.1.4.1.5813.2.14");
        SnmpValue logMsgValue = SnmpUtils.getStrategy().getValueFactory().getOctetString("Eventd Stressor Trap".getBytes());
        builder.addVarBind(logMsgOid, logMsgValue);
        
//        severity,
        SnmpObjId severityOid = SnmpObjId.get(".1.3.6.1.4.1.5813.2.15");
        SnmpValue severityValue = SnmpUtils.getStrategy().getValueFactory().getOctetString("4".getBytes());
        builder.addVarBind(severityOid, severityValue);
        
//        pathoutage,
        SnmpObjId pathOutageOid = SnmpObjId.get(".1.3.6.1.4.1.5813.2.16");
        SnmpValue pathOutageValue = SnmpUtils.getStrategy().getValueFactory().getOctetString("NA".getBytes());
        builder.addVarBind(pathOutageOid, pathOutageValue);
        
//        operinst,
        SnmpObjId operInstOid = SnmpObjId.get(".1.3.6.1.4.1.5813.2.17");
        SnmpValue operInstValue = SnmpUtils.getStrategy().getValueFactory().getOctetString("Ignore".getBytes());
        builder.addVarBind(operInstOid, operInstValue);
        
//        ifresolve,
        SnmpObjId ifResolveOid = SnmpObjId.get(".1.3.6.1.4.1.5813.2.18");
        SnmpValue ifResolveValue = SnmpUtils.getStrategy().getValueFactory().getOctetString("NA".getBytes());
        builder.addVarBind(ifResolveOid, ifResolveValue);
        
//        nodelabel
        SnmpObjId nodeLabelOid = SnmpObjId.get(".1.3.6.1.4.1.5813.2.19");
        String nodeLabel;
        nodeLabel =m_agentAddress.getCanonicalHostName();
        SnmpValue nodeLabelValue = SnmpUtils.getStrategy().getValueFactory().getOctetString(nodeLabel.getBytes());
        builder.addVarBind(nodeLabelOid, nodeLabelValue);
        
		return builder;
    }
    
}
