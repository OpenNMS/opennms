//
// Copyright (C) 2002 Sortova Consulting Group, Inc.  All rights reserved.
// Parts Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
//      http://www.sortova.com/
//
//
// Tab Size = 8
//
//
package org.opennms.netmgt.inventory;
import java.io.IOException;
import java.nio.channels.SocketChannel;
import org.opennms.netmgt.utils.SocketChannelUtil;
import java.net.*;
import java.util.*;
import java.util.Map;
import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import com.novell.ldap.*;
import org.opennms.netmgt.utils.ParameterMap;
import org.opennms.netmgt.config.LdapPeerFactory;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.netmgt.config.inventory.plugin.ldap.*;

/**
 * <P>This class is designed to be used by the web User Interface
 * to get data of a generic LDAP service on 
 * remote interfaces. The class implements the Interface
 * interface </P>
 *
 * @author rssntn67@yahoo.it
 */
public final class LdapBroker {
	/** 
	 * Default retries.
	 */
	private static final int DEFAULT_RETRY = 1;

	/** 
	 * Default timeout. Specifies how long (in milliseconds) to block waiting
	 * for data from the monitored interface.
	 */
	private static final int DEFAULT_TIMEOUT = 3;
	// 3 second timeout on read()

	/**
	 * Default search base for an LDAP search
	 */
	private static final String DEFAULT_BASE = "sub";

	/**
	 * Default search filter for an LDAP search
	 */
	private static final String DEFAULT_FILTER = "(objectclass=*)";

	private int timeout=0;
	
	private int retries=0;
	
	private Map ldapAttribMap = new HashMap();

	private Map ldapDataMap = new HashMap();
	/**
	 * A class to add a timeout to the socket that the LDAP code uses to access an
	 * LDAP server
	 */
	private class TimeoutLDAPSocket implements LDAPSocketFactory {
		private int m_timeout;

		public TimeoutLDAPSocket(int timeout) {
			m_timeout = timeout;
		}

		public Socket makeSocket(String host, int port)
			throws IOException, UnknownHostException {
			Socket socket = new Socket(host, port);
			socket.setSoTimeout(m_timeout);
			return socket;
		}
	}
	
	public LdapBroker(String configFile, String definitionName)throws ValidationException,MarshalException,IOException{
		LdapPeerFactory.reload(configFile, definitionName);
	}
	
	public LdapBroker(){
	}
	
	public void search(String address)throws UnknownHostException, LDAPException, InterruptedException, IOException{
			Category log = ThreadCategory.getInstance(getClass());

			InetAddress ipAddr = null;
	

			try {
				ipAddr = InetAddress.getByName(address);
			} catch (UnknownHostException u) {
				log.fatal("ip address: Failed to cast ipaddress string", u);
				throw u;
			}
			Map parameters = LdapPeerFactory.getInstance().getPeer();
			int ldapVersion =
				ParameterMap.getKeyedInteger(
					parameters,
					"version",
					LDAPConnection.LDAP_V3);
			int ldapPort =
				ParameterMap.getKeyedInteger(
					parameters,
					"port",
					LDAPConnection.DEFAULT_PORT);
	
			if(retries<=0)
			   retries = DEFAULT_RETRY;
	   
			if(timeout<=0)
			   timeout = DEFAULT_TIMEOUT;
	
			String searchBase =
				ParameterMap.getKeyedString(parameters, "searchbase", DEFAULT_BASE);
			String searchFilter =ParameterMap.getKeyedString(parameters,"searchfilter",DEFAULT_FILTER);
			String attrs[] = null;
			Vector vecAttr = new Vector();
			Attributes attributes = (Attributes) parameters.get("attrib");
 			if(attributes!=null){
				Enumeration enumAttrs = attributes.enumerateAttrib();
				while(enumAttrs.hasMoreElements()){
					vecAttr.add((String) enumAttrs.nextElement());
				}
				attrs = new String[vecAttr.size()];
				Object[] arrayAttr=vecAttr.toArray();
				for(int i=0; i<arrayAttr.length; i++){
					attrs[i] = (String) arrayAttr[i];
				}
 			}
			String password = (String) parameters.get("password");
			String ldapDn = (String) parameters.get("dn");
	


			try{
			//first just try a connection to the box via socket. Just in case there is
			//a no way to route to the address, don't iterate through the retries, as a
			//NoRouteToHost exception will only be thrown after about 5 minutes, thus tying
			//up the thread
			SocketChannel sChannel = null;
			timeout=timeout*1000;
				sChannel =	SocketChannelUtil.getConnectedSocketChannel((InetAddress) ipAddr,ldapPort,	timeout);
				if (sChannel == null) {
					log.debug("LdapMonitor: did not connect to host within timeout: "+ timeout);
				}
				log.debug(	"LdapMonitor: connected to host: "	+ address+ " on port: "	+ ldapPort);

				// We're connected, so upgrade status to unresponsive
				//			serviceStatus = SERVICE_UNRESPONSIVE;
				if (sChannel != null) {
					if (sChannel.socket() != null)
						sChannel.socket().close();
					sChannel.close();
					sChannel = null;
				}

				//lets detect the service
				LDAPConnection lc =	new LDAPConnection(new TimeoutLDAPSocket(timeout));
				LDAPSearchConstraints lsc = new LDAPSearchConstraints();
				lsc.setReferralFollowing(true);

				for (int attempts = 1; attempts <= retries; attempts++) {
					log.debug(	"polling LDAP on "	+ address+ ", attempt "	+ attempts	+ " of "	+ (retries == 0 ? "1" : retries + ""));

					//connect to the ldap server
					try {
						lc.connect(address, ldapPort);
						log.debug(	"connected to LDAP server "	+ address+ " on port "+ ldapPort);
					} catch (LDAPException e) {
						log.debug(	"could not connect to LDAP server "	+ address+ " on port "		+ ldapPort);
						if(attempts==retries){
							throw e;
						}
						continue;
					}

					//bind if possible
					if (ldapDn != null && password != null) {
						try {
							lc.bind(ldapVersion, ldapDn, password,lsc);
							log.debug("bound to LDAP server version "	+ ldapVersion+ " with distinguished name "	+ ldapDn);
						} catch (LDAPException e) {
							try {
								log.error(e);
								lc.disconnect();
								if(attempts==retries){
									throw e;
								}
							} catch (LDAPException ex) {
								log.error(ex);
								if(attempts==retries){
									throw ex;
								}
							}

							log.debug("could not bind to LDAP server version "	+ ldapVersion+ " with distinguished name "	+ ldapDn);
							continue;
						}
					}

					boolean attributeOnly = false;
					String noattrs[] = { LDAPConnection.NO_ATTRS };

					if (attrs == null)
						attrs = noattrs;

					int searchScope = LDAPConnection.SCOPE_SUB;
					log.debug("running search " + searchFilter + " from " + searchBase);
					LDAPSearchResults searchResults = null;
					try {
						searchResults =	lc.search(searchBase,searchScope,searchFilter,attrs,attributeOnly);
						if (searchResults != null && searchResults.hasMoreElements()) {
							log.debug("search yielded results");
						} else {
							log.debug("no results found from search");
						}
						LDAPEntry curEntry = null;

						while (searchResults.hasMoreElements()) {
							try {
								curEntry = searchResults.next();
							} catch (LDAPException e) {
								log.debug("Error: " + e.toString());
								if(attempts==retries){
									throw e;
								}
								continue;
							}
							ldapDataMap.put(curEntry.getDN(),curEntry);
							LDAPAttributeSet attributeSet =	curEntry.getAttributeSet();
							Map attrMap = new HashMap();
							for(int i=0; i< vecAttr.size(); i++){
								LDAPAttribute currAttr = attributeSet.getAttribute((String) vecAttr.get(i));
								if(currAttr!=null){
									Enumeration attrValues = currAttr.getStringValues();
									String attrVal = "";
									int count =0;
									while(attrValues.hasMoreElements()){
										count++;
										if(count>1){
											attrVal+="; "; 
										}
										attrVal+=(String)attrValues.nextElement();
									}
								attrMap.put((String) vecAttr.get(i), attrVal);
								log.debug("PUTTING "+attrVal+"  INTO "+curEntry.getDN()+" ATTRNAME "+(String) vecAttr.get(i));
								}
							}
							ldapAttribMap.put(curEntry.getDN(),attrMap);
							//  Enumeration allAttributes =
//								attributeSet.getAttributes();
//
//							while (allAttributes.hasMoreElements()) {
//								LDAPAttribute attribute =
//									(LDAPAttribute) allAttributes.nextElement();
//								String attributeName = attribute.getName();
//								Enumeration allValues = attribute.getStringValues();
//
//								if (allValues != null) {
//									String Value = (String) allValues.nextElement();
//									while (allValues.hasMoreElements()) {
//										log.debug(attributeName+"==="+Value);
////											System.out.println("      " + Value);
//										Value = (String) allValues.nextElement();
//									}
//								}
//							}
						}

					} catch (LDAPException e) {
						try {
							log.error(e);
							lc.disconnect();
							if(attempts==retries){
								throw e;
							}
						} catch (LDAPException ex) {
							log.error(ex);
							if(attempts==retries){
								throw ex;
							}
						}

						log.debug("could not perform search "+ searchFilter+ " from "+ searchBase);
						continue;
					}

					try {
						lc.disconnect();
						log.debug("disconected from LDAP server "+ address+ " on port ");
				
					} catch (LDAPException e) {
						log.debug(e);
						if(attempts==retries){
							throw e;
						}
					}
				}
				}catch(Exception e){
					StackTraceElement [] ste = e.getStackTrace();
					String msg="";
					for(int i=0; i<ste.length; i++){
						msg+= ste[i];
					}
					log.error(e.getCause());
					log.error(e.getMessage());
					log.error(e.getLocalizedMessage());
					log.error(msg);
					
				}
		return;
	}

	/**
	 */
	public Map getLdapDataMap(){
		return ldapDataMap;		
	}
	
	
	/**
	 * @return
	 */
	public int getRetries() {
		return retries;
	}

	/**
	 * @return
	 */
	public int getTimeout() {
		return timeout;
	}

	/**
	 * @param i
	 */
	public void setRetries(int i) {
		retries = i;
	}

	/**
	 * @param i
	 */
	public void setTimeout(int sec) {
		timeout = sec;
	}

	/**
	 * @return
	 */
	public Map getLdapAttribMap() {
		return ldapAttribMap;
	}

}
