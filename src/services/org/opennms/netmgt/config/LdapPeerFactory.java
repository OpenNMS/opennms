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
//
package org.opennms.netmgt.config;

//import java.lang.*;
import java.io.*;
import java.util.*;
import java.util.Map;
import com.novell.ldap.*;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.inventory.plugin.ldap.*;

/**
 * This class is the main respository for SNMP configuration information
 * used by the capabilities daemon. When this class is loaded it reads
 * the snmp configuration into memory, and uses the configuration to find
 * the {@link org.opennms.protocols.snmp.SnmpPeer SnmpPeer} objects for
 * specific addresses. If an address cannot be located in the configuration
 * then a default peer instance is returned to the caller.
 *
 * <p><strong>Note:</strong>Users of this class should make sure the 
 * <em>init()</em> is called before calling any other method to ensure
 * the config is loaded before accessing other convenience methods</p>
 *
 * @author <a href="mailto:weave@opennms.org">Weave</a>
 * @author <a href="http://www.opennms.org/">OpenNMS</a>
 *
 */
public final class LdapPeerFactory {
	/**
	 * The singleton instance of this factory
	 */
	private static LdapPeerFactory m_singleton = null;

	/**
	 * The config class loaded from the config file
	 */
	private LdapConfig m_config;

	/**
	 * This member is set to true if the configuration file
	 * has been loaded.
	 */
	private static boolean m_loaded = false;
	
	private String m_definition;

	/**
	 * Private constructor
	 *
	 * @exception java.io.IOException Thrown if the specified config
	 * 	file cannot be read
	 * @exception org.exolab.castor.xml.MarshalException Thrown if the 
	 * 	file does not conform to the schema.
	 * @exception org.exolab.castor.xml.ValidationException Thrown if 
	 *	the contents do not match the required schema.
	 */
	private LdapPeerFactory(File configFile, String definition)
		throws IOException, MarshalException, ValidationException {
		InputStream cfgIn = new FileInputStream(configFile);
		//System.out.println(configFile);
		m_config =
			(LdapConfig) Unmarshaller.unmarshal(
				LdapConfig.class,
				new InputStreamReader(cfgIn));
		m_definition = definition;
		cfgIn.close();

	}

	/**
	 * Load the config from the default config file and create the 
	 * singleton instance of this factory.
	 *
	 * @exception java.io.IOException Thrown if the specified config
	 * 	file cannot be read
	 * @exception org.exolab.castor.xml.MarshalException Thrown if the 
	 * 	file does not conform to the schema.
	 * @exception org.exolab.castor.xml.ValidationException Thrown if 
	 *	the contents do not match the required schema.
	 */
	public static synchronized void init(String configFile, String definition)
		throws IOException, MarshalException, ValidationException {
		if (m_loaded) {
			// init already called - return
			// to reload, reload() will need to be called
			return;
		}
	
		File cfgFile = new File(configFile);
		//System.out.println(cfgFile.exists());
				//ConfigFileConstants.getFile(				ConfigFileConstants.LDAP_CONF_FILE_NAME);

		ThreadCategory.getInstance(LdapPeerFactory.class).debug(
			"init: config file path: " + cfgFile.getPath());

		m_singleton = new LdapPeerFactory(cfgFile, definition);

		m_loaded = true;
	}

	/**
	 * Reload the config from the default config file
	 *
	 * @exception java.io.IOException Thrown if the specified config
	 * 	file cannot be read/loaded
	 * @exception org.exolab.castor.xml.MarshalException Thrown if the 
	 * 	file does not conform to the schema.
	 * @exception org.exolab.castor.xml.ValidationException Thrown if 
	 *	the contents do not match the required schema.
	 */
	public static synchronized void reload(String configFilePath, String definition)
		throws IOException, MarshalException, ValidationException {
		m_singleton = null;
		m_loaded = false;

		init(configFilePath, definition);
	}

	/**
	 * <p>Return the singleton instance of this factory<p>
	 *
	 * @return The current factory instance.
	 *
	 * @throws java.lang.IllegalStateException Thrown if the factory
	 * 	has not yet been initialized.
	 */
	public static synchronized LdapPeerFactory getInstance() {
		if (!m_loaded)
			throw new IllegalStateException("The factory has not been initialized");

		return m_singleton;
	}

	/**
	 * Converts the internet address to a long value so that
	 * it can be compared using simple opertions. The address
	 * is converted in network byte order (big endin) and 
	 * allows for comparisions like &lt;, &gt;, &lt;=,
	 * &gt;=, ==, and !=.
	 *
	 * @param addr	The address to convert to a long
	 *
	 * @return The address as a long value.
	 *
	 */
	private static long toLong(String addr) {
		byte[] baddr = addr.getBytes();
		long result =
			((long) baddr[0] & 0xffL)
				<< 24 | ((long) baddr[1] & 0xffL)
				<< 16 | ((long) baddr[2] & 0xffL)
				<< 8 | ((long) baddr[3] & 0xffL);

		return result;
	}

	/**
	 * this method uses the passed address and definition to construct
	 * an appropriate LDAP peer object for use by an LdapSession. 
	 *
	 * @param addr	The address to construct the snmp peer instance.
	 * @param def	The definition containing the appropriate information.
	 *
	 * @return The LdapPeer matching for the passed address.
	 */
	private Map create(String addr, Definition def) {
		return create(def, -1);
	}

	/**
	 * this method uses the passed address and definition to construct
	 * an appropriate SNMP peer object for use by an SnmpSession. 
	 *
	 * @param addr	The address to construct the snmp peer instance.
	 * @param def	The definition containing the appropriate information.
	 * @param supportedSnmpVersion	SNMP version to associate with the 
	 * 					peer object if SNMP version has
	 * 					not been explicitly configured.
	 *
	 * @return The SnmpPeer matching for the passed address.
	 */
	private Map create(
		Definition def,
		int supportedLdapVersion) {
		// Allocate a new LDAP parameters
		//
		Map newLdapMap = new HashMap();

		// get the version information, if any
		//
		// If version information is provided it will be used...
		// if not then the passed supportedLdapVersion variable
		// will be used to set the peer's SNMP version.
		//
		if (def.getVersion() != null) {
			if (def.getVersion().equals("v2"))
				newLdapMap.put(
					"version",
					String.valueOf(LDAPConnection.LDAP_V2));
			else if (def.getVersion().equals("v3"))
				newLdapMap.put(
					"version",
					String.valueOf(LDAPConnection.LDAP_V3));
		} else {
			// Verify valid SNMP version provided
			if (supportedLdapVersion == LDAPConnection.LDAP_V2
				|| supportedLdapVersion == LDAPConnection.LDAP_V3)
				newLdapMap.put("version", String.valueOf(supportedLdapVersion));
			else
				newLdapMap.put(
					"version",
					String.valueOf(LDAPConnection.LDAP_V3));
		}

		// setup the SearchBase
		//
	
		newLdapMap.put("searchbase", String.valueOf(def.getSearchbase()));
		


		// setup the Search Filter
		//
		newLdapMap.put("searchfilter", def.getSearchfilter());
		

		// setup Attributes
		//
		Attributes attrs = def.getAttributes();
		if (attrs != null) {
			newLdapMap.put("attrib", attrs);
		} 
		
		// setup Correspondence
		//
		Correspondence corrs[] = def.getCorrespondence();
		if (corrs != null) {
			newLdapMap.put("correspondences", corrs);
		} 
		
	
		// check for port changes
		//
		if (def.hasPort())
			newLdapMap.put("port", String.valueOf(def.getPort()));

		if (def.getPassword() != null)
			newLdapMap.put("password", String.valueOf(def.getPassword()));

		if (def.getLdapDn() != null)
			newLdapMap.put("dn", String.valueOf(def.getLdapDn()));

		// return the Map
		//
		return newLdapMap;
	}

	/**
	 * This method is used by the Capabilities poller to lookup the
	 * SNMP peer information associated with the passed host. If 
	 * there is no specific information available then a default
	 * SnmpPeer instance is returned to the caller.
	 *
	 * @param host	The host for locating the SnmpPeer information.
	 *
	 * @return The configured SnmpPeer information.
	 *
	 */
	public synchronized Map getPeer() {
		return getPeer(-1);
	}

	/**
	 * This method is used by the Capabilities poller to lookup the
	 * SNMP peer information associated with the passed host. If 
	 * there is no specific information available then a default
	 * SnmpPeer instance is returned to the caller.
	 *
	 * @param host	The host for locating the SnmpPeer information.
	 * @param supportedSnmpVersion	SNMP version to associate with the 
	 * 					peer object if SNMP version has
	 * 					not been explicitly configured.
	 *
	 * @return The configured SnmpPeer information.
	 *
	 */
	public synchronized Map getPeer(
		int supportedLdapVersion) {
		// Verify configuration information present!
		//
		if (m_config == null) {
			Map NewLdapMap = new HashMap();

			// Verify valid LDAP version provided
			if (supportedLdapVersion == LDAPConnection.LDAP_V3
				|| supportedLdapVersion == LDAPConnection.LDAP_V2) {
				NewLdapMap.put("version", String.valueOf(supportedLdapVersion));
			}

			return NewLdapMap;
		}

		Map NewLdapMap = null;

		// Attempt to locate the node
		//
		Enumeration edef = m_config.enumerateDefinition();
		DEFLOOP : while (edef.hasMoreElements()) {
			Definition def = (Definition) edef.nextElement();
			// get the information
		   if(def.getName().equals(m_definition))
		    	NewLdapMap = create( def, supportedLdapVersion);

		}
		return NewLdapMap;

	} // end getPeer();
	
	
}
