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
//      http://www.blast.com/
//
// Tab Size = 8
//

package org.opennms.netmgt.rtc.datablock;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Class containing the information for the HTTP POST operations
 * - this gets constructed when each time a subscribe event is received
 * and is basically immutable(except for error count)
 *
 * @author 	<A HREF="mailto:sowmya@opennms.org">Sowmya Nataraj</A>
 * @author	<A HREF="http://www.opennms.org">OpenNMS.org</A>
 */
public class HttpPostInfo extends Object
{
	/**
	 * The URL to post to
	 */
	private	URL	m_url;

	/**
	 * The category name related to this URL
	 */
	private	String	m_catlabel;

	/**
	 * The user name
	 */
	private String	m_user;

	/**
	 * The password
	 */
	private String	m_passwd;

	/**
	 * Number of post errors
	 */
	private int	m_errors;

	/**
	 * Constructor
	 */
	public HttpPostInfo(URL hurl, String clabel, String user, String passwd)
	{
		m_url = hurl;
		m_catlabel = clabel;
		m_user = user;
		m_passwd = passwd;
		m_errors = 0;
	}

	/**
	 * Constructor
	 *
	 * @exception MalformedURLException thrown if the string url passed is not a valid url
	 */
	public HttpPostInfo(String hurl, String clabel, String user, String passwd)
					throws MalformedURLException
	{
		m_url = new URL(hurl);
		m_catlabel = clabel;
		m_user = user;
		m_passwd = passwd;
		m_errors = 0;
	}

	/**
	 * Increment errors 
	 */
	public void incrementErrors()
	{
		m_errors++;
	}

	/**
	 * Clear error count if there were errors earlier
	 */
	public void clearErrors()
	{
		if (m_errors != 0)
			m_errors = 0;
	}

	/**
	 * Return the URL
	 *
	 * @return the URL
	 */
	public URL getURL()
	{
		return m_url;
	}

	/**
	 * Return the URL as a string
	 *
	 * @return the URL as a string
	 */
	public String getURLString()
	{
		return m_url.toString();
	}

	/**
	 * Return the category label
	 *
	 * @return the category label
	 */
	public String getCategory()
	{
		return m_catlabel;
	}

	/**
	 * Return the user
	 *
	 * @return the user
	 */
	public String getUser()
	{
		return m_user;
	}

	/**
	 * Return the passwd
	 *
	 * @return the passwd
	 */
	public String getPassword()
	{
		return m_passwd;
	}

	/**
	 * Return the number of errors
	 *
	 * @return the number of errors
	 */
	public int getErrors()
	{
		return m_errors;
	}

	/**
	 * Overrides the superclass method to return true only
	 * if all instance members are equal
	 */
	public boolean equals(Object o)
	{
		if ((!(o instanceof HttpPostInfo)) || (o == null))
		{
			return false;
		}

		HttpPostInfo obj = (HttpPostInfo)o;

		if (m_url.equals(obj.getURL()) &&
		    m_catlabel.equals(obj.getCategory()) &&
		    m_user.equals(obj.getUser()) &&
		    m_passwd.equals(obj.getPassword())
		   )
		{
			return true;
		}
		else
		{
			return false;
		}
	}
}

