//
// Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
//	Brian Weaver	<weave@opennms.org>
//	http://www.opennms.org/
//
// MibObject.java,v 1.1.1.1 2001/11/11 17:34:37 ben Exp
//

package org.opennms.netmgt.collectd;

import java.util.*;

/**
 * This class is responsible for holding information about a particular
 * MIB object parsed from the DataCollection.xml file.
 *
 * @author <A HREF="mailto:mike@opennms.org">Mike</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 *
 * @version 1.1.1.1
 * 
 */
public class MibObject 
{
	/**
	 * Object's identifier in dotted-decimal notation (e.g, ".1.3.6.1.2.1.1.1").
	 */
	private String m_oid;
	
	/** 
	 * Object's alias  (e.g., "sysDescription").
	 */
	private String m_alias;
	
	/** 
	 * Object's expected data type.
	 */
	private String m_type;
	
	/**
	 * True if this object resides in a table. 
	 */
	private	boolean tabular; // NOT CURRENTLY USED?
	
	/**
	 * Object's instance to be retrieved.  Indicates a value to be appended to the objects'
	 * oid string prior to issuing an SNMP GET request.
	 *
	 * 	decimal value:  instance of object to retrieve (value is appended to the objects oid string)
	 * 	keyword value:  currently supported keywords are "ifIndex" and "ifAddress".
	 *
	 * @see #INSTANCE_IFINDEX
	 * @see #INSTANCE_IFADDRESS
	 */
	 private String m_instance;
	 
	/**
	 * Reserved instance keywords.  
	 */
	 /**
	  * Indicates that the interface's 'ifIndex' value from the 'ipInterface' table of the
	  * database should be appended to the object's oid.
	  */
	 public static final String INSTANCE_IFINDEX = "ifIndex";
	 
	 /** 
	  * Indicates that the interface's IP address is to be appended to the object's oid.
	  */
	 public static final String INSTANCE_IFADDRESS = "ifAddress";
	 
	/**
	 * Constructor
	*/
	public MibObject()
	{
		m_oid = null;
		m_alias = null;
		m_type = null;
		m_instance = null;
	}

	/**
	 * This method is used to assign the object's identifier.
	 *
	 * @param oid - object identifier in dotted decimal notation (e.g., ".1.3.6.1.2.1.1.1")
	 */
	public void setOid(String oid)
	{
		m_oid = oid;
	}
	
	/**
	 * This method is used to assign the object's alias.
	 *
	 * @param alias - object alias (e.g., "sysDescription")
	 */
	public void setAlias(String alias)
	{
		m_alias = alias;
	}
	
	/**
	 * This method is used to assign the object's expected data type.
	 *
	 * @param type - object's data type
	 */
	public void setType(String type)
	{
		m_type = type;
	}
	
	/**
	 * This method is used to specify the object's instance to be retrieved.
	 * The instance specified here will be dereferenced if necessary and
	 * appended to the object's identifier string.  Valid instance values
	 * are keywords such as "ifIndex" and "ifAddress" or numeric values
	 * such as "0" or "99".  Numeric values will simply be appended to 
	 * the objects identifer as-is while keyword values will be dereferenced
	 * and will be assigned a valued which is dependent on the SNMP agent's
	 * IP address.
	 *
	 * @see #INSTANCE_IFINDEX
	 * @see #INSTANCE_IFADDRESS
	 */
	public void setInstance(String instance)
	{
		m_instance = instance;
	}
	
	/** 
	 * Returns the object's identifier.
	 *
	 * @return  The object's identifier string.	
	 */
	public String getOid() 
	{
		return m_oid;
	}
	
	/** 
	 * Returns the object's alias.
	 *
	 * @return  The object's alias.	
	 */
	public String getAlias()
	{
		return m_alias;
	}

	/** 
	 * Returns the object's data type.
	 *
	 * @return The object's data type
	 */	
	public String getType()
	{
		return m_type;
	}
	
	/** 
	 * Returns the instance string associated with the object.
	 *
	 * @return The instance value associated with the object
	 */
	public String getInstance()
	{
		return m_instance;
	}
	
	/** 
	 * This method is responsible for comparing this MibObject with the
	 * passed Object to determine if they are equivalent.  The objects are equivalent
	 * if the argument is a MibObject object with the same object identifier, instance, 
	 * alias and type.
	 *
	 * @param object - MibObject to be compared to this object.
	 *
	 * @return true if the objects are equal, false otherwise.
	 */
	public boolean equals(Object object)
	{
		if (object == null)
			return false;
		
		MibObject aMibObject;
		
		try
		{
			aMibObject = (MibObject)object;
		}
		catch (ClassCastException cce)
		{
			return false;
		}
		
		if (m_oid.equals(aMibObject.getOid()))
			if (m_instance.equals(aMibObject.getInstance()))
				//
				// 'alias' and 'type', values are optional so we need to check for null
				//
				if ((m_alias == null && aMibObject.getInstance() == null) || m_alias.equals(aMibObject.getAlias()))
					if ((m_type == null && aMibObject.getType() == null) || m_type.equals(aMibObject.getType()))
							return true;
		
		return false;
			
	}
	
	/** 
	 * This method is responsible for returning a String object which represents
	 * the content of this MibObject.  Primarily used for debugging purposes.
	 * 
	 * @return String which represents the content of this MibObject
	 */
	public String toString()
	{
		StringBuffer buffer = new StringBuffer();
		
		// Build the buffer
		buffer.append("\n   oid:      ").append(m_oid);
		buffer.append("\n   instance: ").append(m_instance);
		buffer.append("\n   alias:    ").append(m_alias);
		buffer.append("\n   type:     ").append(m_type);
		
		return buffer.toString();
	}
}
