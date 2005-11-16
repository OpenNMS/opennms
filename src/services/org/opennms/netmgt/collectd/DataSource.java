//
// // // This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 Blast Internet Services, Inc. All
// rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included
// code and modified
// code that was published under the GNU General Public License. Copyrights for
// modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of Blast Internet Services, Inc.
//
// Copyright (C) 1999-2001 Oculan Corp. All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//       
// For more information contact:
//      OpenNMS Licensing <license@opennms.org>
//      http://www.opennms.org/
//      http://www.blast.com/
//
package org.opennms.netmgt.collectd;

import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpValue;


/**
 * @author craig.miskell@agresearch.co.nz
 *  
 */
public abstract class DataSource {

	/**
	 * Object's identifier in dotted-decimal notation (e.g,
	 * ".1.3.6.1.2.1.1.1"). Identifies the MIB object which is the source of
	 * data values for this RRD data source.
	 */
	protected String m_oid;

	/**
	 * Instance identifier which is appended to the object identifier to
	 * identify a particular MIB entry. This value may be an integer value or
	 * "ifIndex".
	 */
	protected String m_instance;

	/**
	 * Data Source Name. This is not only the name of the data source but
	 * should identify the MIB object as well (e.g., "ifOctetsIn").
	 */
	protected String m_name;

	public static DataSource dataSourceForMibObject(MibObject obj, String collectionName) {
		// Check if this object has an appropriate "integer" data type
		// which can be stored in an RRD database file (must map to one of 
		// the supported RRD data source types:  COUNTER or GAUGE).
		if (RRDDataSource.handlesType(obj.getType())) {
			RRDDataSource ds=new RRDDataSource(obj, collectionName);
			return ds;
		} else if(StringDataSource.handlesType(obj.getType())){
			return new StringDataSource(obj);
		}
		return null;
	}
	
	public DataSource() {
		super();
		m_oid = null;
		m_instance = null;
		m_name = null;
	}
	
	/**
	 * @param obj
	 */
	public DataSource(MibObject obj) {
		this();
		this.setOid(obj.getOid());
		this.setInstance(obj.getInstance());
		this.setName(obj.getAlias());
	}

	/**
	 * Stores the value <code>val</code> in the datasource named dsName, in repository repository.  Creates
	 * the store if need be (e.g. an rrd file, or a properties file, or whatever)
	 */
	public abstract boolean performUpdate(
		String collectionName,
		String owner,
		String repository,
		String dsName,
		String val);


	/**
	 * This method extracts from snmpVar a string which can be passed as the val parameter of performUpdate.
	 */
	public abstract String getStorableValue(SnmpValue snmpVar);


	protected String getFullOid(String ifIndex) {
		// Make sure we have an actual object id value.
		if (this.getOid() == null)
			return null;

		String instance = null;
		if (this.getInstance().equals(MibObject.INSTANCE_IFINDEX))
			instance = ifIndex;
		else
			instance = this.getInstance();

        return SnmpObjId.get(getOid(), instance).toString();
	}
	/**
	 * This method is used to assign the object's identifier.
	 * 
	 * @param oid -
	 *            object identifier in dotted decimal notation (e.g.,
	 *            ".1.3.6.1.2.1.1.1")
	 */
	public void setOid(String oid) {
		m_oid = oid;
	}

	/**
	 * This method is used to assign the object's instance id.
	 * 
	 * @param instance -
	 *            instance identifier (to be appended to oid)
	 */
	public void setInstance(String instance) {
		m_instance = instance;
	}

	/**
	 * This method is used to assign the data source name.
	 * 
	 * @param alias -
	 *            object alias (e.g., "sysDescription")
	 */
	public void setName(String name) {
		m_name = name;
	}

	/**
	 * Returns the object's identifier.
	 * 
	 * @return The object's identifier string.
	 */
	public String getOid() {
		return m_oid;
	}

	/**
	 * Returns the object's instance id.
	 * 
	 * @return The object's instance id string.
	 */
	public String getInstance() {
		return m_instance;
	}

	/**
	 * Returns the object's name.
	 * 
	 * @return The object's name.
	 */
	public String getName() {
		return m_name;
	}
}
