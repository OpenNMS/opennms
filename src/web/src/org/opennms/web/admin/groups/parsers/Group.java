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

package org.opennms.web.admin.groups.parsers;

import java.util.*;
import java.beans.*;

/**This is a data class to store the group information from
 * the groups.xml file
 * 
 * @author <A HREF="mailto:jason@opennms.org">Jason Johns</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 *
 * @version 1.1.1.1
 * 
 */
public class Group implements Cloneable
{
	/**
	*/
	public static final String GROUP_NAME_PROPERTY = "groupName";
	
	/**The name of the group
	*/
	private String m_groupName;
	
	/**The comments for the group
	*/
	private String m_groupComments;
	
	/**The list of users in the group
	*/
	private List m_users;
	
	/**
	*/
	private PropertyChangeSupport m_propChange;
	
	/**Default constructor, intializes the users list
	*/
	public Group()
	{
		m_propChange = new PropertyChangeSupport(this);
		
		m_groupName = "";
		m_groupComments = "";
		m_users = new ArrayList();
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
		
		Group newGroup = new Group();
		
		newGroup.setGroupName(m_groupName);
		newGroup.setGroupComments(m_groupComments);
		
		for (int i = 0; i < m_users.size(); i++)
		{
			newGroup.addUser((String)m_users.get(i));
		}
		
		return newGroup;
	}
	
	/**
	*/
	public synchronized void addPropertyChangeListener(PropertyChangeListener listener)
	{
		m_propChange.addPropertyChangeListener(listener);
	}
	
	/**
	*/
	public synchronized void removePropertyChangeListener(PropertyChangeListener listener)
	{
		m_propChange.removePropertyChangeListener(listener);
	}
	
	/**Sets the group name
	   @param String aName, the name of the group
	*/
	public void setGroupName(String aName)
	{
		String old = m_groupName;
		m_groupName = aName;
		m_propChange.firePropertyChange(GROUP_NAME_PROPERTY, old, m_groupName);
	}
	
	/**Returns the group name
	   @return String, the name of the group
	*/
	public String getGroupName()
	{
		return m_groupName;
	}
	
	/**Sets the comments for the group
	   @param String someComments, the comments for the group
	*/
	public void setGroupComments(String someComments)
	{
		m_groupComments = someComments;
	}
	
	/**Returns the comments for the group
	   @return String, the comments for the group
	*/
	public String getGroupComments()
	{
		return m_groupComments;
	}
	
	/**Returns whether the group has this user in its users list
	   @return boolean, true if user is in list, false if not
	*/
	public boolean hasUser(String aUser)
	{
		return m_users.contains(aUser);
	}
	
	/**Adds a username to the list of users
	   @param String aUser, a new username
	*/
	public void addUser(String aUser)
	{
		m_users.add(aUser);
	}
	
	/**Removes a username from the list of users
	   @param String aUser, the user to remove
	*/
	public void removeUser(String aUser)
	{
		m_users.remove(aUser);
	}
	
	/**Removes all users from the group.
	*/
	public void clearUsers()
	{
		m_users.clear();
	}
	
	/**Returns the list of users
	   @return List, the list of users
	*/
	public List getUsers()
	{
		return m_users;
	}
	
	/**Returns a count of the users in the list
	   @return int, how many users in this group
	*/
	public int getUserCount()
	{
		return m_users.size();
	}
	
	/**Returns a String representation of the group, used primarily for debugging.
	   @return String, a string representation
	*/
	public String toString()
	{
		StringBuffer buffer = new StringBuffer();
		
		buffer.append("name     = " + m_groupName + "\n");
		buffer.append("comments = " + m_groupComments + "\n");
		buffer.append("users:\n");
		
		for (int i = 0; i < m_users.size(); i++)
		{
			buffer.append("\t" + (String)m_users.get(i) + "\n");
		}
		
		return buffer.toString();
	}
}
