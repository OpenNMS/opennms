/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
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

package org.opennms.provisiond.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.dbcp.PoolingConnection;
import org.opennms.netmgt.model.PrimaryType;
import org.opennms.netmgt.provision.persist.FilesystemForeignSourceRepository;
import org.opennms.netmgt.provision.persist.requisition.Requisition;
import org.opennms.netmgt.provision.persist.requisition.RequisitionCategory;
import org.opennms.netmgt.provision.persist.requisition.RequisitionCategoryCollection;
import org.opennms.netmgt.provision.persist.requisition.RequisitionInterface;
import org.opennms.netmgt.provision.persist.requisition.RequisitionInterfaceCollection;
import org.opennms.netmgt.provision.persist.requisition.RequisitionMonitoredService;
import org.opennms.netmgt.provision.persist.requisition.RequisitionMonitoredServiceCollection;
import org.opennms.netmgt.provision.persist.requisition.RequisitionNode;

public class CsvRequisitionParser {

	private static final String PROPERTY_FS_REPO_PATH = "fs.repo.path";
	private static final String PROPERTY_CSV_FILE = "csv.file";
	private static final String PROPERTY_FOREIGN_SOURCE = "foreign.source";
	private static final String PROPERTY_RESOLVE_IPS = "resolve.ips";
	private static final String PROPERTY_PARSE_DB = "parse.db";
	private static final String PROPERTY_IPLIKE_QUERY = "iplike.query";
    private static final String PROPERTY_DB_SVR = "db.server";
    private static final String PROPERTY_DB_NAME = "db.name";
    private static final String PROPERTY_DB_USER = "db.user";
    private static final String PROPERTY_DB_PW = "db.password";
    private static final String PROPERTY_USE_NODE_ID = "use.nodeid";
    private static final String PROPERTY_CATEGORY_LIST = "category.list";
    private static final String PROPERTY_CATEGORY_ADD_EXISTING = "category.add.existing";
    private static final String PROPERTY_SERVICE_LIST = "service.list";
    private static final String PROPERTY_ADD_ONLY = "add.only";
	
	private static FilesystemForeignSourceRepository m_fsr = null;
	private static File m_csvFile = new File("/tmp/nodes.csv");
	private static File m_repoPath = new File("/opt/opennms/imports");
	private static String m_foreignSource = "default";
	private static Boolean m_resolveIps = false;
	private static Boolean m_parseDb = false;
	private static String m_iplikeQuery = "*.*.*.*";
    private static String m_dbSvr = "127.0.0.1";
    private static String m_dbName = "opennms";
    private static String m_dbUser = "opennms";
    private static String m_dbPass = "opennms";
	private static Boolean m_useNodeId = false;
	private static List<String> m_categoryList = new ArrayList<String>();
	private static Boolean m_categoryAddExisting = true;
	private static List<String> m_serviceList = new ArrayList<String>();
	private static Boolean m_addOnly = true;

	public static void main(String[] args) throws ClassNotFoundException, SQLException {
		Runtime.getRuntime().addShutdownHook(createShutdownHook());
		
		if (args.length > 0) {
			try {
				usageReport();
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.exit(0);
		}
		
		try {
			if (!validateProperties()) {
				usageReport();
				System.exit(-1);
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		
		if (System.getProperty("parse.db")!= null) {
			try {
				migrateDbNodes();
			} catch (UnknownHostException e) {
				e.printStackTrace();
			}
		} else {
			try {
				parseCsv(m_csvFile, m_repoPath);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		
		System.out.println("Finished.");
	}
	
	private static void migrateDbNodes() throws SQLException, UnknownHostException, ClassNotFoundException {
		
		String distinctNodesQueryStr = "  " +
				"SELECT nodeId AS \"nodeid\"," +
				"       nodeLabel AS \"nodelabel\"," +
				"       foreignSource AS \"foreignsource\"," +
				"       foreignId AS \"foreignid\" " +
				"  FROM node " +
				" WHERE nodeid in (" +
				"  SELECT " +
				"DISTINCT nodeid " +
				"    FROM ipinterface " +
				"   WHERE iplike(ipaddr, '"+m_iplikeQuery+"')) " +
				"ORDER BY nodeid";
		
		if (m_addOnly) {
			distinctNodesQueryStr = "  " +
					"SELECT nodeId AS \"nodeid\"," +
					"       nodeLabel AS \"nodelabel\"," +
					"       foreignSource AS \"foreignsource\"," +
					"       foreignId AS \"foreignid\" " +
					"  FROM node " +
					" WHERE nodeid in (" +
					"  SELECT " +
					"DISTINCT nodeid " +
					"    FROM ipinterface " +
					"   WHERE iplike(ipaddr, '"+m_iplikeQuery+"')) " +
					"  AND foreignsource is NULL " +
					"ORDER BY nodeid";
		}
		
		Connection connection = null;
		Statement distinctNodesStatement = null;
		PoolingConnection pool = null;
		connection = createConnection();
		connection.setAutoCommit(false);
		pool = new PoolingConnection(connection);
		distinctNodesStatement = pool.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);

		ResultSet distinctNodesResultSet = null;
		int rowsFound = 0;
		distinctNodesResultSet = distinctNodesStatement.executeQuery(distinctNodesQueryStr);
		distinctNodesResultSet.last();
		rowsFound = distinctNodesResultSet.getRow();
		distinctNodesResultSet.beforeFirst();

		System.out.println(rowsFound+" nodes found.");

		int nodesMigrated = 0;
		while (distinctNodesResultSet.next()) {
			System.out.println("Processing row: "+distinctNodesResultSet.getRow()+"...");

			int nodeId = distinctNodesResultSet.getInt("nodeid");
			String queryStr = "" +
					"  SELECT ipaddr " +
					"    FROM ipinterface " +
					"   WHERE nodeid = "+nodeId+" " +
					"     AND issnmpprimary = 'P' " +
					"ORDER BY inet(ipaddr)" +
					"   LIMIT 1";

			Statement findPrimaryStatement = pool.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);

			System.out.println("Querying DB for SNMP Primary interface for node: "+nodeId+"...");
			ResultSet findPrimaryResultSet = findPrimaryStatement.executeQuery(queryStr);

			String primaryIp = null;

			if (findPrimaryResultSet.next()) {
				primaryIp = findPrimaryResultSet.getString("ipaddr");
				System.out.println("SNMP Primary found: "+primaryIp);
			}

			findPrimaryResultSet.close();
			findPrimaryStatement.close();

			if (primaryIp == null) {
				System.out.println("SNMP Primary not found.  Determining lowest numbered IP to set as Primary...");
				queryStr = "" +
						"  SELECT ipaddr " +
						"    FROM ipinterface " +
						"   WHERE nodeid = "+nodeId+" " +
						"ORDER BY inet(ipaddr)" +
						"   LIMIT 1";
				findPrimaryStatement = pool.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
				findPrimaryResultSet = findPrimaryStatement.executeQuery(queryStr);
			}

			if (primaryIp == null && findPrimaryResultSet.next()) {
				primaryIp = findPrimaryResultSet.getString("ipaddr");
				System.out.println("SNMP Primary found: "+primaryIp);
			}

			findPrimaryResultSet.close();
			findPrimaryStatement.close();

			if (primaryIp == null) {
				System.out.println("SNMP Primary not found.  Skipping node.  (This should never happen since it is the iplike query that finds the distinct nodes :( )");
				continue;
			}

			
			String foreignId = null;
			if (m_useNodeId) {
				foreignId = String.valueOf(nodeId);
			} else {
				foreignId = String.valueOf(System.currentTimeMillis());
			}

			String label = distinctNodesResultSet.getString("nodelabel");
			distinctNodesResultSet.updateString("foreignsource", m_foreignSource);
			distinctNodesResultSet.updateString("foreignId", foreignId);

			System.out.println("Updating node ("+nodeId+":"+label+") with foreignsource:"+m_foreignSource+" and foreignId:"+foreignId);
			distinctNodesResultSet.updateRow();
			System.out.println("Node updated.");

			RequisitionData rd = new RequisitionData(label, primaryIp, m_foreignSource, foreignId);
			
			if (m_categoryAddExisting) {
				String categoriesQueryString = "" +
						"SELECT c.categoryname as \"categoryname\" " +
						"  FROM categories c " +
						"  JOIN category_node cn " +
						"    ON cn.categoryid = c.categoryid " +
						"  JOIN node n on n.nodeid = cn.nodeid " +
						" WHERE n.nodeid = "+nodeId;
				Statement categoriesStatement = pool.createStatement();

				ResultSet crs = categoriesStatement.executeQuery(categoriesQueryString);

				Set<String> categories = new LinkedHashSet<String>();
				while(crs.next()) {
					categories.add(crs.getString("categoryname"));
				}

				crs.close();
				categoriesStatement.close();
				rd.setCategories(categories);
			}
			
			System.out.println("Updating requistion...");
			createOrUpdateRequistion(rd);
			System.out.println("Requistion updated!  Next...\n");
			nodesMigrated++;
		}

		try {
			connection.commit();
		} catch (SQLException e) {
			e.printStackTrace();
			connection.rollback();
		}

		distinctNodesResultSet.close();
		distinctNodesStatement.close();
		pool.close();
		connection.close();

		System.out.println(nodesMigrated+" Nodes migrated to foreign source "+m_foreignSource);

		
	}

	private static Connection createConnection() throws ClassNotFoundException, SQLException {
        Class.forName("org.postgresql.Driver");
        Connection connection = DriverManager.getConnection("jdbc:postgresql://" + m_dbSvr + ":5432/"+m_dbName, m_dbUser, m_dbPass);
        return connection;
    }


	//need to do some better exception handling here
	private static boolean validateProperties() throws IOException, FileNotFoundException, IllegalArgumentException  {
		
		System.out.println("\n\nProperties:\n");
		m_foreignSource = System.getProperty(PROPERTY_FOREIGN_SOURCE, m_foreignSource);
		System.out.println("\t"+PROPERTY_FOREIGN_SOURCE+":"+m_foreignSource);
		
		m_parseDb = Boolean.valueOf(System.getProperty(PROPERTY_PARSE_DB, m_parseDb.toString()));
		System.out.println("\t"+PROPERTY_PARSE_DB+":"+m_parseDb);
		
		
		if (!m_parseDb.booleanValue()) {

			String csvFileName = System.getProperty(PROPERTY_CSV_FILE, m_csvFile.getCanonicalPath());
			System.out.println("\t"+PROPERTY_CSV_FILE+":"+m_csvFile);
			
			m_csvFile = new File(csvFileName);
			if (!m_csvFile.exists()) {
				throw new FileNotFoundException("CSV Input File: "+csvFileName+"; Not Found!");
			}
		} else {
			m_dbSvr = System.getProperty(PROPERTY_DB_SVR, m_dbSvr);
			System.out.println("\t"+PROPERTY_DB_SVR+":"+m_dbSvr);

			m_dbName = System.getProperty(PROPERTY_DB_NAME, m_dbName);
			System.out.println("\t"+PROPERTY_DB_NAME+":"+m_dbName);
			
			m_dbUser = System.getProperty(PROPERTY_DB_USER, m_dbUser);
			System.out.println("\t"+PROPERTY_DB_USER+":"+m_dbUser);
			
			m_dbPass = System.getProperty(PROPERTY_DB_PW, m_dbPass);
			System.out.println("\t"+PROPERTY_DB_PW+":"+m_dbPass);
			
			m_iplikeQuery = System.getProperty(PROPERTY_IPLIKE_QUERY, m_iplikeQuery);
			System.out.println("\t"+PROPERTY_IPLIKE_QUERY+":"+m_iplikeQuery);
			
			m_useNodeId = Boolean.valueOf(System.getProperty(PROPERTY_USE_NODE_ID, m_useNodeId.toString()));
			System.out.println("\t"+PROPERTY_USE_NODE_ID+":"+m_useNodeId);
			
			m_addOnly = Boolean.valueOf(System.getProperty(PROPERTY_ADD_ONLY, m_addOnly.toString()));
			System.out.println("\t"+PROPERTY_ADD_ONLY+":"+m_addOnly);
			
			m_categoryAddExisting = Boolean.valueOf(System.getProperty(PROPERTY_CATEGORY_ADD_EXISTING, m_categoryAddExisting.toString()));
			System.out.println("\t"+PROPERTY_CATEGORY_ADD_EXISTING+":"+m_categoryAddExisting);
			
		}
		
		String fsRepo = System.getProperty(PROPERTY_FS_REPO_PATH, m_repoPath.getCanonicalPath());
		System.out.println("\t"+PROPERTY_FS_REPO_PATH+":"+fsRepo);
		
		m_repoPath = new File(fsRepo);
		
		if (!m_repoPath.exists() || !m_repoPath.isDirectory() || !m_repoPath.canWrite()) {
			throw new IllegalArgumentException("The specified "+PROPERTY_FS_REPO_PATH+": "+m_repoPath.getCanonicalPath()+", either doesn't exist, isn't writable, or isn't a directory.");
		} else {
			m_fsr = new FilesystemForeignSourceRepository();
			m_fsr.setRequisitionPath(m_repoPath.getCanonicalPath());
		}
		
		m_resolveIps = Boolean.valueOf(System.getProperty(PROPERTY_RESOLVE_IPS, m_resolveIps.toString()));
		System.out.println("\t"+PROPERTY_RESOLVE_IPS+":"+m_resolveIps.toString());
		
		String categories = System.getProperty(PROPERTY_CATEGORY_LIST);
		if (categories != null) {
			System.out.println("\t"+PROPERTY_CATEGORY_LIST+":"+categories);
			String[] cats = categories.split(",");
			for (String cat : cats) {
				m_categoryList.add(cat);
			}
		}
		
		String services = System.getProperty(PROPERTY_SERVICE_LIST);
		if (services != null) {
			System.out.println("\t"+PROPERTY_SERVICE_LIST+":"+services);
			String[] srvs = services.split(",");
			for (String srv : srvs) {
				m_serviceList.add(srv);
			}
		}

		System.out.println("\n");
		return true;
	}

	private static void usageReport() throws IOException {		
		System.err.println("Usage: java CsvRequistionParser [<Property>...]\n" +
				"\n" +
				"Supported Properties:\n" +
				"\t"+PROPERTY_CSV_FILE+": default:"+m_csvFile.getCanonicalPath()+"\n" +
				"\t"+PROPERTY_FS_REPO_PATH+": default:"+m_repoPath.getCanonicalPath()+"\n" +
				"\t"+PROPERTY_FOREIGN_SOURCE+": default:"+m_foreignSource+"\n" +
				"\t"+PROPERTY_RESOLVE_IPS+": default:"+m_foreignSource+"\n" +
				"\t"+PROPERTY_PARSE_DB+": default:"+m_parseDb+"\n" +
				"\t"+PROPERTY_DB_SVR+": default:"+m_dbSvr+"\n" +
				"\t"+PROPERTY_DB_NAME+": default:"+m_dbName+"\n" +
				"\t"+PROPERTY_DB_USER+": default:"+m_dbUser+"\n" +
				"\t"+PROPERTY_DB_PW+": default:"+m_dbPass+"\n" +
				"\t"+PROPERTY_IPLIKE_QUERY+": default:"+m_iplikeQuery+"\n" +
				"\t"+PROPERTY_USE_NODE_ID+": default:"+m_useNodeId+"\n" +
				"\t"+PROPERTY_CATEGORY_LIST+": default:null"+"\n" +
				"\t"+PROPERTY_CATEGORY_ADD_EXISTING+":default:"+m_categoryAddExisting+"\n" +
				"\t"+PROPERTY_SERVICE_LIST+": default:null"+"\n" +
				"\t"+PROPERTY_ADD_ONLY+": default:"+m_addOnly+"\n" +
				"\n" +
				"\n" +
				"Example:\n" +
				"\t java -D"+PROPERTY_CSV_FILE+"=/tmp/mynodes.csv \\\n" +
				"\t\t-D"+PROPERTY_FS_REPO_PATH+"= /opt/opennms/etc/imports \\\n" +
				"\t\t-D"+PROPERTY_FOREIGN_SOURCE+"=default \\\n" +
				"\t\t-D"+PROPERTY_RESOLVE_IPS+"=false \\\n" +
				"\t\t-D"+PROPERTY_PARSE_DB+"=false \\\n" +
				"\t\t-D"+PROPERTY_DB_SVR+"=localhost \\\n" +
				"\t\t-D"+PROPERTY_DB_NAME+"=opennms \\\n" +
				"\t\t-D"+PROPERTY_DB_USER+"=opennms \\\n" +
				"\t\t-D"+PROPERTY_DB_PW+"=opennms \\\n" +
				"\t\t-D"+PROPERTY_IPLIKE_QUERY+"=\"*.*.*.*\" \\\n" +
				"\t\t-D"+PROPERTY_USE_NODE_ID+"=false \\\n" +
				"\t\t-D"+PROPERTY_CATEGORY_LIST+"=Production,Router \\\n" +
				"\t\t-D"+PROPERTY_CATEGORY_ADD_EXISTING+"=true \\\n" +
				"\t\t-D"+PROPERTY_SERVICE_LIST+"=ICMP,SNMP \\\n" +
				"\t\t-D"+PROPERTY_ADD_ONLY+"=false \\\n" +
				"\t\t-jar opennms-csv-requisition-1.13.0-SNAPSHOT-jar-with-dependencies.jar" +
				"\n" +
				"\n" +
				"FYI: This application expects the csv file to have 2 elements, nodelabel and IP address.  Example:" +
				"\n\n" +
				"#nodelabel,ipaddress\n" +
				"www.opennms.com,64.146.64.212\n" +
				"github.com,204.232.175.90\n" +
				"www.juniper.net,2600:1406:1f:195::720\n\n"
				);
	}

	protected static void parseCsv(File csv, File m_repo) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(csv)));
		
		String line = null;
		int lineNum = 0;
		while ((line = br.readLine()) != null) {
			lineNum++;
			if (line != null && line.startsWith("#")) {
				continue;
			}
			
			String[] fields = line.split(",", 2);
			int fieldCount = fields.length;
			if (fieldCount != 2) {
				System.err.println("Error on line: "+Integer.toString(lineNum)+". Found "+Integer.toString(fieldCount)+" fields and expected 2.");
				continue;
			}
						
			RequisitionData rd = new RequisitionData(fields[0], fields[1], m_foreignSource, null);
			System.out.println("Line "+Integer.toString(lineNum)+":"+rd.toString());

			createOrUpdateRequistion(rd);
		}
		br.close();
	}
	
	private static void createOrUpdateRequistion(RequisitionData rd) throws UnknownHostException {
		Requisition r = null;
		RequisitionNode rn = new RequisitionNode();
		String foreignSource = rd.getForeignSource();
		
		r = m_fsr.getRequisition(foreignSource);
		
		if (r == null) {
			r = new Requisition(foreignSource);
		}
		
		System.err.println("Creating/Updating requistion: "+foreignSource);
		
		r.updateDateStamp();
		
		RequisitionMonitoredServiceCollection services = new RequisitionMonitoredServiceCollection();
		for (String svc : m_serviceList) {
			services.add(new RequisitionMonitoredService(svc));
		}
		
		RequisitionInterface iface = new RequisitionInterface();
		iface.setDescr("mgmt-if");
		iface.setIpAddr(rd.getPrimaryIp());
		iface.setManaged(true);
		iface.setSnmpPrimary(PrimaryType.PRIMARY);
		iface.setStatus(Integer.valueOf(1));
		iface.setMonitoredServices(services);
		
		RequisitionInterfaceCollection ric = new RequisitionInterfaceCollection();
		ric.add(iface);
				
		//add categories requisition level categories
		RequisitionCategoryCollection rcc = new RequisitionCategoryCollection();
		if (m_categoryList != null && m_categoryList.size() > 0) {
			for (String cat : m_categoryList) {
				rcc.add(new RequisitionCategory(cat));
			}
		}
		
		//add categories already on the node to the requisition
		if (rd.getCategories() != null) {
			for (String cat : rd.getCategories()) {
				rcc.add(new RequisitionCategory(cat));
			}
		}
		
		rn.setBuilding(foreignSource);
		
		if (rcc.size() >= 1) {
			rn.setCategories(rcc);
		}
		
		rn.setForeignId(rd.getForeignId());
		rn.setInterfaces(ric);
		
		String nodeLabel = rd.getNodeLabel();
		if (m_resolveIps) {
			InetAddress addr = InetAddress.getByName(rd.getPrimaryIp());
			nodeLabel = addr.getCanonicalHostName();
		}
		
		rn.setNodeLabel(nodeLabel);
		
		//r.insertNode(rn);
		r.putNode(rn);
		m_fsr.save(r);
	}


	public static Thread createShutdownHook() {
		Thread t = new Thread() {
			@Override
			public void run() {
				System.out.println("\nHave a nice day! :)");
				Runtime.getRuntime().halt(0);
			}
		};
		return t;
	}

}
