//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 Blast Internet Services, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of Blast Internet Services, Inc.
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
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.blast.com/
//

package org.opennms.web.eventconf.bobject;

import java.util.ArrayList;
import java.util.List;

/**
 * This is a data class for storing mask elements for the <mask> tag
 * of an event configuration.
 *
 * @author <A HREF="mailto:jason@opennms.org">Jason Johns</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 *
 * @version 1.1.1.1
 *
 * @deprecated Replaced by a Castor-generated implementation.
 *
 * @see org.opennms.netmgt.xml.eventconf.Maskelement
 *
 */
public class MaskElement implements Cloneable
{
	/**
	*/
	public static final String ELEMENT_NAME_VALUES[] = {"uei",
							    "source",
							    "host",
							    "snmphost",
							    "nodeid",
							    "interface",
							    "service",
							    "eid"};
	
	/**
	*/
	private String m_elementName;
	
	/**
	*/
	private List m_elementValues;
	
	/**Default constructor, intializes the member variables.
	*/
	public MaskElement()
	{
		m_elementValues = new ArrayList();
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
		
		MaskElement newElement = new MaskElement();
		
		newElement.setElementName(m_elementName);
		
		for (int i = 0; i < m_elementValues.size(); i++)
		{
			newElement.addElementValue( (String)m_elementValues.get(i));
		}
		
		return newElement;
	}
	
	/**
	*/
	public void setElementName(String name)
	{
		m_elementName = name;
	}
	
	/**
	*/
	public String getElementName()
	{
		return m_elementName;
	}
	
	/**
	*/
	public void addElementValue(String value)
	{
		m_elementValues.add(value);
	}
	
	/**
	*/
	public List getElementValues()
	{
		return m_elementValues;
	}
}
