/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2012 The OpenNMS Group, Inc.
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

package org.opennms.web.map.db.datasources;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;

import org.slf4j.MDC;
import org.opennms.core.logging.Logging;
import org.opennms.core.resource.Vault;
import org.opennms.core.resource.db.SimpleDbConnectionFactory;
import org.opennms.web.map.MapsConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * <p>ServerDataSource class.</p>
 *
 * @author <a href="mailto:antonio@opennms.it">Antonio Russo</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @author <a href="mailto:antonio@opennms.it">Antonio Russo</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @version $Id: $
 * @since 1.8.1
 */
public class ServerDataSource implements DataSourceInterface {
	
	private static final Logger LOG = LoggerFactory.getLogger(ServerDataSource.class);


	private Map<?,?> params;
	boolean initialized = false;
	private Map<String, String> severityMapping = new HashMap<String, String>();

	
	static final String STATUS_FIELD="ev_status";
	static final String SEVERITY_FIELD="ev_severity";
	static final String TABLE_NAME="v_eventi_snm";
	
	final String CLOSED_STATUS = "CLOSED";
	final String ACK_STATUS = "ACK";
	final String ASSIGNED_STATUS = "ASSIGNED";
	final String OPEN_STATUS = "OPEN";
	
	//private static MapPropertiesFactory mpf=null;
	
	static Connection opennmsConn = null;
	static Connection externalConn = null;

	
	/**
	 * <p>Constructor for ServerDataSource.</p>
	 *
	 * @param params a {@link java.util.Map} object.
	 */
	public ServerDataSource(Map<?,?> params){
	    Logging.putPrefix(MapsConstants.LOG4J_CATEGORY);
		
		this.params = params;
		init();
	}
	
	/**
	 * Before invoking get() method, this method must be invoked.
	 */
	public void init(){
		LOG.debug("Init...getting db connection");
	
			try{
				if(opennmsConn==null || opennmsConn.isClosed()){
					opennmsConn = Vault.getDbConnection();
				}
				String url=(String)params.get("url");
				String driver=(String)params.get("driver");
				String user=(String)params.get("user");
				String password=(String)params.get("password");
				//gets external connection 
				if(externalConn==null || externalConn.isClosed()){
					LOG.debug("getting external db connection with parameters url={}, driver={}, user={}, password={}", url, driver, user, password);
					SimpleDbConnectionFactory dbConnFactory = new SimpleDbConnectionFactory();
					dbConnFactory.init(url,driver,user,password);
					externalConn = dbConnFactory.getConnection();
				}				
			}catch(Throwable s){
				LOG.error("Error while getting db Connection from Vault {}", s);
				throw new RuntimeException(s);
			}
			
			severityMapping.put("6","Critical");
			severityMapping.put("5","Major");
			severityMapping.put("4","Minor");
			severityMapping.put("3","Warning");
			severityMapping.put("2","Cleared");
			severityMapping.put("1","Normal");
			severityMapping.put("0","Indeterminate");

	}
	
	private boolean isInitialized() throws SQLException {
		
		if (opennmsConn!=null && !opennmsConn.isClosed() && externalConn!=null && !externalConn.isClosed()) return true;
		return false;
	}

	/**
	 * <p>finalize</p>
	 *
	 * @throws java.lang.Throwable if any.
	 */
        @Override
	protected void finalize() throws Throwable {
		LOG.debug("Finalizing...closing db connections");
		super.finalize();
		if(opennmsConn!=null){
			Vault.releaseDbConnection(opennmsConn);
		}
		if(externalConn!=null && !externalConn.isClosed()){
			externalConn.close();
		}
	}
	

	/** {@inheritDoc} */
        @Override
	public String getSeverity(Object id){

		String result = "-1";

		try {
			if (!isInitialized()) init();
		} catch (Throwable e) {
			LOG.error("exiting: error found {}", e);
			return "-1";
		}
		
		//get ipaddresses of the node
		Set<String> ipAddrs = getIpAddrById(id);
		//If there is no ipaddress for the nodeid
		if(ipAddrs.size()==0){
			LOG.warn("No ip address found for node with id {}", (Integer)id);
			return "-1";
		}
		// get the severity from external db
		result = getSev(ipAddrs);
		// if no severity is found...
		if(result.equals("-1")){
			LOG.warn("No severity found for element with id {}", (Integer)id);
		}
		return result;
	}

	private Set<String> getIpAddrById(Object id){
		//get ipaddresses of the node
		String sqlQueryIFaces= "select distinct ipaddr from ipinterface where ipaddr!='0.0.0.0' and nodeid=?";
		Set<String> ipAddrs = new HashSet<String>();
		PreparedStatement ps;
		int nodeId=0;
		
			try {
				nodeId = ((Integer)id).intValue();
				ps = opennmsConn.prepareStatement(sqlQueryIFaces);
				ps.setInt(1, nodeId);
				ResultSet rs = ps.executeQuery();
				while(rs.next()){
					String ipAddr = rs.getString(1);
					ipAddrs.add(ipAddr);
				}	
				rs.close();
				ps.close();
			} catch (SQLException e) {
				LOG.error("Error while getting ipaddress by id {}", e);
			}
		return ipAddrs;
	}

	private String getSev(Set<String> ipAddrs){

		String getDataQuery="select max("+SEVERITY_FIELD+") from "+TABLE_NAME+" where ip_address in (";
		Iterator<String> it = ipAddrs.iterator();
		while (it.hasNext()) {
			String ip = it.next();
			getDataQuery+="'"+ip+"'";
			if (it.hasNext()) {
				getDataQuery+=",";
			}
		}
		getDataQuery+=") and "+STATUS_FIELD+"!='"+CLOSED_STATUS+"'";
		LOG.debug("get severity query is {}", getDataQuery);
		String value=null;
		try {
			Statement stmt = externalConn.createStatement();
			ResultSet rs = stmt.executeQuery(getDataQuery);
			// get only first value (if more found)
			
			if(rs.next()){
				value=rs.getString(1);
				LOG.debug("found severity for ipaddresses {} with value {}", ipAddrs, value);
			}
			rs.close();
			stmt.close();
		} catch (SQLException e1) {
			LOG.error("Exception while getting severity {}", e1);
			return "-1";
		}
		
		String sevLabel = (String)severityMapping.get(value);
		LOG.debug("Getting severity mapping for key={}: sevLabel={}", value, sevLabel);
		
		return sevLabel;
	}

	
	/** {@inheritDoc} */
        @Override
	public String getStatus(Object id){

		String result = "-1";

		try {
			if (!isInitialized()) init();
		} catch (Throwable e) {
			LOG.error("exiting: error found {}", e);
			return result;
		}
		
		//get ipaddresses of the node
		Set<String> ipAddrs = getIpAddrById(id);
		//If there is no ipaddress for the nodeid
		if(ipAddrs.size()==0){
			LOG.warn("No ip address found for node with id {}", (Integer)id);
			return result;
		}
		// get the severity from external db
		result = getSt(ipAddrs);
		// if no severity is found...
		if(result.equals("-1")){
			LOG.warn("No severity found for element with id {}", (Integer)id);
		}
		return result;

	}
	
	private String getSt(Set<String> ipAddrs){
		
		String getDataQuery="select "+STATUS_FIELD+" from "+TABLE_NAME+" where ip_address in (";
		Iterator<String> it = ipAddrs.iterator();
		while (it.hasNext()) {
			String ip = it.next();
			getDataQuery+="'"+ip+"'";
			if (it.hasNext()) {
				getDataQuery+=",";
			}
		}
		getDataQuery+=") and "+STATUS_FIELD+"!='"+CLOSED_STATUS+"'";
		String innerQuery = "select max("+SEVERITY_FIELD+") from "+TABLE_NAME+" where ip_address in (";
		
		Iterator<String> it2 = ipAddrs.iterator();
		while(it2.hasNext()){
			String ip = it2.next();
			innerQuery+="'"+ip+"'";
			if(it2.hasNext()){
				innerQuery+=",";
			}
		}
		innerQuery+=") and "+STATUS_FIELD+"!='"+CLOSED_STATUS+"'";
		getDataQuery+=" and "+SEVERITY_FIELD+"=("+innerQuery+")" ;
		
		LOG.debug("get status query is {}", getDataQuery);
		String value=null;
		try {
			Statement stmt = externalConn.createStatement();
			ResultSet rs = stmt.executeQuery(getDataQuery);
			// get only first value (if more found)
			
			if(rs.next()){
				value=rs.getString(1);
				LOG.debug("found status for ipaddresses {} with value {}", ipAddrs, value);
			}
			rs.close();
			stmt.close();
		} catch (SQLException e1) {
			LOG.error("Exception while getting status {}", e1);
			return "-1";
		}
		
		return value;
	}
	
	/** {@inheritDoc} */
        @Override
	public double getAvailability(Object id) {
		// not implemented
		return -1;
	}


}
