//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
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
 * @see org.opennms.netmgt.xml.eventconf.Forward
 *
 */
public class Forward implements Cloneable
{
	/**The different mechanism values for the forwards. If this array changes
	   please update the FORWARD_MECHANISM_DEFAULT_INDEX member if needed.
	*/
	public static final String FORWARD_MECHANISM_VALUES[] = {"snmpudp",
		             	                                 "snmptcp",
								 "xmltcp",
								 "xmludp"};
	
	/**The index into the FORWARD_MECHANISM_VALUES array indicating
	   the default mechanism of the forward. If the values array
	   changes please update this index if needed.
	*/
	public static final int FORWARD_MECHANISM_DEFAULT_INDEX = 0;
	
	/**A list of values for forward states. If this array changes
	   please update any of the FORWARD_STATE_DEFAULT_INDEX members if needed.
	*/
	public static final String FORWARD_STATES[] = {"on","off"};
	
	/**The index into the FORWARD_STATES array indicating
	   the default state of a forward. If the values array
	   changes please update this index if needed.
	*/
	public static final int FORWARD_STATE_DEFAULT_INDEX = 0;
	
	/**
	*/
	private String m_forward;
	
	/**
	*/
	private String m_state;
	
	/**
	*/
	private String m_mechanism;
	
	/**Default constructor, intializes the member variables.
	*/
	public Forward()
	{
		m_state = FORWARD_STATES[FORWARD_STATE_DEFAULT_INDEX];
		m_mechanism = FORWARD_MECHANISM_VALUES[FORWARD_MECHANISM_DEFAULT_INDEX];
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
		
		Forward newForward = new Forward();
		
		newForward.setForward(m_forward);
		newForward.setState(m_state);
		newForward.setMechanism(m_mechanism);
		
		return newForward;
	}
	
	/**
	*/
	public void setForward(String forward)
	{
		m_forward = forward;
	}
	
	/**
	*/
	public String getForward()
	{
		return m_forward;
	}
	
	/**
	*/
	public void setState(String state)
	{
		/*
		if (index < 0 || index > FORWARD_STATES.length)
			throw new InvalidParameterException("The forward state index("+index+") must be >= 0 and <= " + FORWARD_STATES.length);
		*/
		m_state = state;
	}
	
	/**
	*/
	public String getState()
	{
		return m_state;
	}
	
	/**
	*/
	public void setMechanism(String mechanism)
	{
		/*
		if (index < 0 || index > FORWARD_MECHANISM_VALUES.length)
			throw new InvalidParameterException("The forward mechanism index("+index+") must be >= 0 and <= " + FORWARD_MECHANISM_VALUES.length);
		*/
		m_mechanism = mechanism;
	}
	
	/**
	*/
	public String getMechanism()
	{
		return m_mechanism;
	}
}
