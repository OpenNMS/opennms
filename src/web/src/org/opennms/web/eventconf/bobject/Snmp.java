//
// Copyright (C) 2000 N*Manage Company, Inc.
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

package org.opennms.web.eventconf.bobject;

import java.io.*;
import java.util.*;

/**
 * This is a data class for storing snmp event configuration information
 * as parsed from the eventconf.xml file
 *
 * @author <A HREF="mailto:jason@opennms.org">Jason Johns</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 *
 * @version 1.1.1.1
 *
 * @deprecated Replaced by a Castor-generated implementation.
 *
 * @see org.opennms.netmgt.xml.eventconf.Snmp
 *
 */
public class Snmp implements Cloneable
{
	/**
	*/
	private String m_id;
	
	/**
	*/
	private String m_idtext;
	
	/**
	*/
	private String m_version;
	
	/**
	*/
	private String m_specific;
	
	/**
	*/
	private String m_generic;
	
	/**
	*/
	private String m_community;
	
	/**Default constructor, intializes the member variables.
	*/
	public Snmp()
	{
	}
	
	/**
	*/
	public Object clone()
	{
		try
		{
			super.clone();
		}
		catch(CloneNotSupportedException e)
		{
			return null;
		}
		
		Snmp newSnmp = new Snmp();
		
		newSnmp.setId(m_id);
		newSnmp.setIdText(m_idtext);
		newSnmp.setVersion(m_version);
		newSnmp.setSpecific(m_specific);
		newSnmp.setGeneric(m_generic);
		newSnmp.setCommunity(m_community);
		
		return newSnmp;
	}
	
	/**
	*/
	public void setId(String id)
	{
		m_id = id;
	}
	
	/**
	*/
	public String getId()
	{
		return m_id;
	}
	
	/**
	*/
	public void setIdText(String idText)
	{
		m_idtext = idText;
	}
	
	/**
	*/
	public String getIdText()
	{
		return m_idtext;
	}
	
	/**
	*/
	public void setVersion(String version)
	{
		m_version = version;
	}
	
	/**
	*/
	public String getVersion()
	{
		return m_version;
	}
	
	/**
	*/
	public void setSpecific(String specific)
	{
		m_specific = specific;
	}
	
	/**
	*/
	public String getSpecific()
	{
		return m_specific;
	}
	
	/**
	*/
	public void setGeneric(String generic)
	{
		m_generic = generic;
	}
	
	/**
	*/
	public String getGeneric()
	{
		return m_generic;
	}
	
	/**
	*/
	public void setCommunity(String community)
	{
		m_community = community;
	}
	
	/**
	*/
	public String getCommunity()
	{
		return m_community;
	}
}
