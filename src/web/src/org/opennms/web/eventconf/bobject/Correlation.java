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
 * This is a data class for storing event configuration information
 * as parsed from the eventconf.xml file
 *
 * @author <A HREF="mailto:jason@opennms.org">Jason Johns</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 *
 * @version 1.1.1.1
 *
 * @deprecated Replaced by a Castor-generated implementation.
 *
 * @see org.opennms.netmgt.xml.eventconf.Correlation
 *
 */
public class Correlation implements Cloneable
{
	/**The different path values for a correlation. If this array changes
	   please update the CORRELATION_PATH_DEFAULT_INDEX member if needed.
	*/
	public static final String CORRELATION_PATH_VALUES[] = {"suppressDuplicates",
		                                                "cancellingEvent",
								"suppressAndCancel",
							 	"pathOutage"};
	
	/**The index into the CORRELATION_PATH_VALUES array indicating
	   the default value of a new correlation. If the values array
	   changes please update this index if needed.
	*/
	public static final int CORRELATION_PATH_DEFAULT_INDEX = 0;
	
	/**The different state values for a correlation. If this array changes
	   please update the CORRELATION_STATE_DEFAULT_INDEX member if needed.
	*/
	public static final String CORRELATION_STATE_VALUES[] = {"on","off"};
	
	/**The index into the CORRELATION_STATE_VALUES array indicating
	   the default state of a new correlation. If the values array
	   changes please update this index if needed.
	*/
	public static final int CORRELATION_STATE_DEFAULT_INDEX = 0;
	
	/**
	*/
	private List m_cuei;
	
	/**
	*/
	private String m_cmin;
	
	/**
	*/
	private String m_cmax;
	
	/**
	*/
	private String m_ctime;
	
	/**
	*/
	private String m_path;
	
	/**
	*/
	private String m_state;
	
	/**Default constructor, intializes the member variables.
	*/
	public Correlation()
	{
		m_cuei = new ArrayList();
		m_path = CORRELATION_PATH_VALUES[CORRELATION_PATH_DEFAULT_INDEX];
		m_state = CORRELATION_STATE_VALUES[CORRELATION_STATE_DEFAULT_INDEX];
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
		
		Correlation newCorrelation = new Correlation();
		
		newCorrelation.setCorrelationMin(m_cmin);
		newCorrelation.setCorrelationMax(m_cmax);
		newCorrelation.setCorrelationTime(m_ctime);
		newCorrelation.setCorrelationPath(m_path);
		newCorrelation.setState(m_state);
		
		for (int i = 0; i < m_cuei.size(); i++)
		{
			newCorrelation.addCorrelationUEI( (String)m_cuei.get(i));
		}
		
		return newCorrelation;
	}
	
	/**
	*/
	public void addCorrelationUEI(String uei)
	{
		m_cuei.add(uei);
	}
	
	/**
	*/
	public List getCorrelationUEIs()
	{
		return m_cuei;
	}
	
	/**
	*/
	public void setCorrelationMin(String cmin)
	{
		m_cmin = cmin;
	}
	
	/**
	*/
	public String getCorrelationMin()
	{
		return m_cmin;
	}
	
	/**
	*/
	public void setCorrelationMax(String cmax)
	{
		m_cmax = cmax;
	}
	
	/**
	*/
	public String getCorrelationMax()
	{
		return m_cmax;
	}
	
	/**
	*/
	public void setCorrelationTime(String ctime)
	{
		m_ctime = ctime;
	}
	
	/**
	*/
	public String getCorrelationTime()
	{
		return m_ctime;
	}
	
	/**
	*/
	public void setCorrelationPath(String path)
	{
		/*
		if (index < 0 || index > CORRELATION_PATH_VALUES.length)
			throw new InvalidParameterException("The correlation path index("+index+") must be >= 0 and <= " + CORRELATION_PATH_VALUES.length);
		*/
		m_path = path;
	}
	
	/**
	*/
	public void setState(String state)
	{
		/*
		if (index < 0 || index > CORRELATION_STATE_VALUES.length)
			throw new InvalidParameterException("The correlation state index("+index+") must be >= 0 and <= " + CORRELATION_STATE_VALUES.length);
		*/
		m_state = state;
	}
	
	/**
	*/
	public String getCorrelationPath()
	{
		return m_path;
	}
	
	/**
	*/
	public String getState()
	{
		return m_state;
	}
}
