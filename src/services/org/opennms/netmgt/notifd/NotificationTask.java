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
// Modifications:
//
// 2003 Jan 31: Cleaned up some unused imports.
// 
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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

package org.opennms.netmgt.notifd;

import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

import org.apache.log4j.Category;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.utils.CommandExecutor;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.DatabaseConnectionFactory;
import org.opennms.netmgt.config.NotificationFactory;
import org.opennms.netmgt.config.UserFactory;
import org.opennms.netmgt.config.notificationCommands.Argument;
import org.opennms.netmgt.config.notificationCommands.Command;
import org.opennms.netmgt.config.users.Contact;
import org.opennms.netmgt.config.users.User;

/**This class holds all the data and logic for sending out a notification
 * Each notification that is sent will be accompanied by
 * a row in the notifications table. All notifications in a group will be
 * identified with a common groupId number.
 * 
 * @author <A HREF="mailto:jason@opennms.org">Jason Johns</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 *
 */
public class NotificationTask extends Thread
{
	/**The User object the notification needs to go out to
	*/
	private User m_user;
	
	/**The row id that will be used for the row inserted into the notifications table
	*/
	private int m_notifyId;
	
	/**The console command that will be issued to send the actual notification.
	*/
	private Command[] m_commands;
	
	/**
	*/
	private Map m_params;
	
	/**
	*/
	private long m_sendTime;
	
	/**
	*/
	private List m_siblings;
	
	/**
	*/
	private SortedMap m_notifTree;
	
	/**Constructor, initializes some information
	   @param NotificationFactory someParams, the parameters from Notify
	*/
	public NotificationTask(long sendTime, Map someParams, List siblings)
		throws SQLException
	{
		m_sendTime = sendTime;
		m_params = new HashMap(someParams);
		m_siblings = siblings;
		
		try
		{
			DatabaseConnectionFactory.init();
			UserFactory.init();
			NotificationFactory.init();
		}
		catch (Exception e)
		{
			ThreadCategory.getInstance(getClass()).error("Error initializing UserFactory or NotificationFactory", e);
			throw new UndeclaredThrowableException(e);
		}
	}
	
	/**
	*/
	public String toString()
	{
		StringBuffer buffer = new StringBuffer("Send ");
		
		for (int i = 0; i < m_commands.length; i++)
		{
			buffer.append(m_commands[i].getName() + "/");
		}
		buffer.append(" to " + m_user.getUserId() + " at " + new Date(m_sendTime));
		
		return buffer.toString();
	}
	
	/**
	*/
	public long getSendTime()
	{
		return m_sendTime;
	}
	
	/**Returns the unique id used to insert the row in the database for 
	   this notification task.
	   @return int, the id of the row in notifications table
	*/
	public int getNotifyId()
	{
		return m_notifyId;
	}
	
	/**Sets the user that the page needs to be sent to.
	   @param User aUser, the user info
	*/
	public void setUser(User aUser)
	{
		m_user = aUser;
	}
	
	/**Sets the group id that will be inserted into the row in notifications table
	   @param int anId, the group id to set for the row
	*/
	public void setNoticeId(int anId)
	{
		m_notifyId = anId;
	}
	
	/**This method will construct the command that will be issued to send the actual page.
	   @param String aCommand, the command to call at the console.
	   @param String aParamString, a string indicating what parameters to include after
	                               the console command
	*/
	public void setCommands(Command[] commands)
	{
		m_commands = commands;
	}
	
	/**
	*/
	public void run()
	{
		Category log = ThreadCategory.getInstance(getClass());
		
                boolean responded = false;
                try { 
                        responded = NotificationFactory.noticeOutstanding(m_notifyId);
                }
                catch (Exception e)
                {
                        log.error("Unable to get response status on notice #" + m_notifyId, e);
                }
                
		//check to see if someone has responded, if so remove all the brothers 
                if (responded)
		{
			try
			{
				if (UserFactory.getInstance().isUserOnDuty(m_user.getUserId(), Calendar.getInstance()))
				{
                                        //send the notice
                                        CommandExecutor command = new CommandExecutor();
					
                                        for (int i = 0; i < m_commands.length; i++)
					{
						NotificationFactory.updateNoticeWithUserInfo(m_user.getUserId(), 
                                                                                             m_notifyId, 
                                                                                             m_commands[i].getName(),
                                                                                             UserFactory.getInstance().getContactInfo(m_user.getUserId(),
                                                                                                                                      m_commands[i].getName()));
                                                
                                                int returnCode = command.execute(m_commands[i].getExecute(), getArgumentList(m_commands[i]));
						log.debug("command " + m_commands[i].getName() + " return code = " + returnCode);
					}
				}
				else
				{
					log.debug("User " + m_user.getUserId() + " is not on duty, skipping...");
				}
			}
			catch (SQLException e)
			{
				log.debug("Could not insert notice info into database, aborting send notice...", e);
			}
			catch (IOException e)
			{
				log.debug("Could not get user duty schedule information: ", e);
			}
			catch (MarshalException e)
			{
				log.debug("Could not get user duty schedule information: ", e);
			}
			catch (ValidationException e)
			{
				log.debug("Could not get user duty schedule information: ", e);
			}
		}
		else
		{
                        //remove all the related notices that have yet to be sent
			for (int i = 0; i < m_siblings.size(); i++)
			{
				NotificationTask task = (NotificationTask)m_siblings.get(i);
				m_notifTree.remove(task);
			}
		}
	}
	
        /**
	 */
	private List getArgumentList(Command command)
	{
		Collection notifArgs = command.getArgumentCollection();
		List commandArgs = new ArrayList();
		
		Iterator i = notifArgs.iterator();
		while(i.hasNext())
		{
			Argument curArg = (Argument)i.next();
			ThreadCategory.getInstance(getClass()).debug("argument: " + curArg.getSwitch() + " " + 
			                                                     curArg.getSubstitution()+ " '" + 
									     getArgumentValue(curArg.getSwitch())+ "' " + 
									     Boolean.valueOf(curArg.getStreamed()).booleanValue());
			
			commandArgs.add( new org.opennms.core.utils.Argument(curArg.getSwitch(),
			                                                     curArg.getSubstitution(),
									     getArgumentValue(curArg.getSwitch()),
									     Boolean.valueOf(curArg.getStreamed()).booleanValue() ));
		}
		
		return commandArgs;
	}
	
	/**
	 *
	 */
	private String getArgumentValue(String aSwitch)
	{
		String value = "";
		
                try {
                        if (NotificationFactory.PARAM_DESTINATION.equals(aSwitch))
                        {
                                value = m_user.getUserId();
                        }
                        else if (NotificationFactory.PARAM_EMAIL.equals(aSwitch))
                        {	
                                value = getEmail(m_user);
                        }
                        else if (NotificationFactory.PARAM_PAGER_EMAIL.equals(aSwitch))
                        {
                                value = UserFactory.getInstance().getPagerEmail(m_user.getUserId());
                        }
                        else if (NotificationFactory.PARAM_TEXT_PAGER_PIN.equals(aSwitch))
                        {
                                value = UserFactory.getInstance().getTextPin(m_user.getUserId());
                        }
                        else if (NotificationFactory.PARAM_NUM_PAGER_PIN.equals(aSwitch))
                        {
                                value = UserFactory.getInstance().getNumericPin(m_user.getUserId());
                        }
                        else if (m_params.containsKey(aSwitch))
                        {
                                value = (String)m_params.get(aSwitch);
                        }
                }
                catch (Exception e)
                {
                        ThreadCategory.getInstance(getClass()).error("unable to get value for parameter " + aSwitch);
                }
		
		return value;
	}
        
        /**
         *
         */
        private String getEmail(User user)
        {
                
		String value = "";
		Enumeration contacts = user.enumerateContact();
		while(contacts != null && contacts.hasMoreElements())
		{
			Contact contact = (Contact) contacts.nextElement();
			if(contact != null)
			{
				if(contact.getType().equals("email"))
				{
                                        value = contact.getInfo();
                                        break;
				}
			}
		}
		return value;
        }
}
