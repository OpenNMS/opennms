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
import java.security.*;

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
 * @see org.opennms.netmgt.xml.eventconf.Operaction
 *
 */
public class OperatorAction implements Cloneable
{
	/**A list of values for autoaction states. If this array changes
	   please update any of the OPERATOR_ACTION_STATE_DEFAULT_INDEX members if needed.
	*/
	public static final String OPERATOR_ACTION_STATES[] = {"on","off"};
	
	/**The index into the OPERATOR_ACTION_STATES array indicating
	   the default state of an auto action. If the values array
	   changes please update this index if needed.
	*/
	public static final int OPERATOR_ACTION_STATE_DEFAULT_INDEX = 0;
	
	/**
	*/
	private String m_operatorAction;
	
	/**
	*/
	private String m_menuText;
	
	/**
	*/
	private String m_state;
	
	/**Default constructor, intializes the member variables.
	*/
	public OperatorAction()
	{
		m_state = OPERATOR_ACTION_STATES[OPERATOR_ACTION_STATE_DEFAULT_INDEX];
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
		
		OperatorAction newAction = new OperatorAction();
		
		newAction.setOperatorAction(m_operatorAction);
		newAction.setMenuText(m_menuText);
		newAction.setState(m_state);
		
		return newAction;
	}
	
	/**
	*/
	public void setOperatorAction(String action)
	{
		m_operatorAction = action;
	}
	
	/**
	*/
	public String getOperatorAction()
	{
		return m_operatorAction;
	}
	
	/**
	*/
	public void setMenuText(String text)
	{
		m_menuText = text;
	}
	
	/**
	*/
	public String getMenuText()
	{
		return m_menuText;
	}
	
	/**
	*/
	public void setState(String state)
	{
		/*
		if (index < 0 || index > OPERATOR_ACTION_STATES.length)
			throw new InvalidParameterException("The auto action state index("+index+") must be >= 0 and <= " + OPERATOR_ACTION_STATES.length);
		*/
		m_state = state;
	}
	
	/**
	*/
	public String getState()
	{
		return m_state;
	}
}
