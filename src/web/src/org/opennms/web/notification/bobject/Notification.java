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

package org.opennms.web.notification.bobject;

import java.util.*;

import org.opennms.core.utils.*;


/**This class holds the information parsed from the notifications.xml
 * 
 * @author <A HREF="mailto:jason@opennms.org">Jason Johns</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 * 
 * @version 1.1.1.1
*/
public class Notification
{
	/**The interval to wait between processing target
	*/
	private String m_interval;
	
	/**The name of the notification
	*/
	private String m_name;
	
	/**Comments for the notification
	*/
	private String m_comments;
	
	/**The list of users or other notifications to include in this notification
	*/
	private List m_targets;
	
	/**Default constructor, initializes members
	*/
	public Notification()
	{
		m_targets = new ArrayList();
	}
	
	/**Sets the name of the notification
	   @param String
	*/
	public void setName(String name)
	{
		m_name = name;
	}
	
	/**Returns the name of the notification
	   @return String
	*/
	public String getName()
	{
		return m_name;
	}
	
	/**Sets the comments for the notification
	   @param String
	*/
	public void setComments(String comments)
	{
		m_comments = comments;
	}
	
	/**Returns the comments for the notification
	   @return String
	*/
	public String getComments()
	{
		return m_comments;
	}
	
	/**Sets the interval for the notification
	   @param String
	*/
	public void setInterval(String interval)
	{
		m_interval = interval;
	}
	
	/**Returns the string version of the interval
	   @return String
	*/
	public String getInterval()
	{
		return m_interval;
	}
	
	/**Returns the interval converted to milliseconds
	   @return long, interval in milliseconds
	*/
	public long getIntervalMilliseconds()
	{
		long interval = 0;
		
		if (!m_interval.equals("all"))
		{
			//interval = TimeConverter.convertToMillis(m_interval);
            interval = Integer.parseInt(m_interval);
		}
		
		return interval;
	}
	
	/**Returns the interval in seconds
	   @return long, interval in seconds
	*/
	public long getIntervalSeconds()
	{
		return getIntervalMilliseconds() / 1000;
	}
	
	/**Adds a target to the notification
	   @param NotificationTarget
	*/
	public void addTarget(NotificationTarget target)
	{
		m_targets.add(target);
	}
	
	/**Returns the list of targets
	   @return List
	*/
	public List getTargets()
	{
		return m_targets;
	}
}
