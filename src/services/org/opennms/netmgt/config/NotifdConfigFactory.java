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
// Modifications:
//
// 2003 Jan 31: Cleaned up some unused imports.
// 2002 Nov 10: Removed "http://" from UEIs
// 2002 Nov 09: Added the ability to match a single event to more than one notification.
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
//      http://www.opennms.com/
//

package org.opennms.netmgt.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.ValidationException;
import org.opennms.netmgt.ConfigFileConstants;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.config.notifd.NotifdConfiguration;
import org.opennms.netmgt.eventd.EventIpcManagerFactory;
import org.opennms.netmgt.xml.event.Event;

/**
*/
public class NotifdConfigFactory
{
	/**
	 * Singleton instance
	 */
	private static NotifdConfigFactory instance;
	
	/**
	 * Input stream for the general Notifd configuration xml
	*/
	protected static InputStream configIn;
	
	/**
	 * Boolean indicating if the init() method has been called
	 */
	private static boolean initialized = false;
        
        /**
         *
         */
        private static NotifdConfiguration configuration;
	
	/**
	 *
	 */
        private static File m_notifdConfFile;
        
        /**
         *
	 */
	private static long m_lastModified;
	
	/**
	 *
	 */
	private NotifdConfigFactory()
	{
	}
	
	/**
	 *
	 */
	static synchronized public NotifdConfigFactory getInstance()
	{
		if (!initialized)
			return null;
		
		if (instance == null)
		{
			instance = new NotifdConfigFactory();
		}
		
		return instance;
	}
        
        /**
	 *
	 */
	public static synchronized void init()
		throws IOException, FileNotFoundException, MarshalException, ValidationException
	{
		if (!initialized)
		{
			reload();
			initialized = true;
		}
	}
	
	/**
	 *
	 */
	public static synchronized void reload()
		throws IOException, MarshalException, ValidationException
	{
                m_notifdConfFile = ConfigFileConstants.getFile(ConfigFileConstants.NOTIFD_CONFIG_FILE_NAME);
                        
                InputStream configIn = new FileInputStream(m_notifdConfFile);
                m_lastModified = m_notifdConfFile.lastModified();
                
                configuration = (NotifdConfiguration) Unmarshaller.unmarshal(NotifdConfiguration.class, new InputStreamReader(configIn));
                configIn.close();
        }
	
	/**
	 *
	 */
	public static NotifdConfiguration getConfiguration()
                throws IOException, MarshalException, ValidationException
	{
		if (!initialized)
			return null;
		
                updateFromFile();
                
		return configuration;
	}
	
	/**
	 *
	 */
	public static String getNotificationStatus()
                throws IOException, MarshalException, ValidationException
	{
		if (!initialized)
			return null;
		
                updateFromFile();
                
		return configuration.getStatus();
	}
	
	/**
	 *
	 */
	public static boolean getNotificationMatch()
                throws IOException, MarshalException, ValidationException
	{
		if (!initialized)
			return false;
		
                updateFromFile();
                
		return configuration.getMatchAll();
	}
	
	/**
	 * Gets a nicely formatted string for the Web UI to display
	 * @return On, Off, or Unknown depending on status
	 */
	public static String getPrettyStatus()
                throws IOException, MarshalException, ValidationException
	{
		if (!initialized)
			return "Unknown";
		
		String status = "Unknown";
		
		status = getNotificationStatus();
		
		if (status.equals("on"))
			status = "On";
		else if (status.equals("off"))
			status = "Off";
		
		return status;
	}
        
        /**
         * Turns the notifd service on
         */
        public void turnNotifdOn()
                throws MarshalException, ValidationException, IOException
        {
                sendEvent("uei.opennms.org/internal/notificationsTurnedOn");
                configuration.setStatus("on");
		
                saveCurrent();
        }
        
        /**
         * Turns the notifd service off
         */
        public void turnNotifdOff()
                throws MarshalException, ValidationException, IOException
        {
                sendEvent("uei.opennms.org/internal/notificationsTurnedOff");
                configuration.setStatus("off");
		
                saveCurrent();
        }
        
        /**
         *
         */
        private void sendEvent(String uei)
        {
                Event event = new Event();
                event.setUei(uei);
                event.setSource("NotifdConfigFactory");
		
		event.setTime(EventConstants.formatToString(new java.util.Date()));
                
		try
		{
                	EventIpcManagerFactory.getInstance().getManager().sendNow(event);
		}
		catch (Throwable t)
		{
		}
        }
        
        /**
         *
         */
        public synchronized void saveCurrent()
                throws MarshalException, ValidationException, IOException
        {
                //marshall to a string first, then write the string to the file. This way the original config
                //isn't lost if the xml from the marshall is hosed.
                StringWriter stringWriter = new StringWriter();
                Marshaller.marshal( configuration, stringWriter );
                if (stringWriter.toString()!=null)
                {
			FileWriter fileWriter = new FileWriter(m_notifdConfFile);
                        fileWriter.write(stringWriter.toString());
                        fileWriter.flush();
                        fileWriter.close();
                }
                
                reload();
        }
        
        /**
         *
         */
        private static void updateFromFile()
                throws IOException, MarshalException, ValidationException
        {
                if (m_lastModified != m_notifdConfFile.lastModified())
		{
			reload();
		}
        }
}
