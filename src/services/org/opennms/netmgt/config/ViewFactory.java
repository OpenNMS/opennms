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
// Tab Size = 8
//

package org.opennms.netmgt.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.regexp.RE;
import org.apache.regexp.RESyntaxException;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.ValidationException;
import org.opennms.netmgt.ConfigFileConstants;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.config.views.Categories;
import org.opennms.netmgt.config.views.Header;
import org.opennms.netmgt.config.views.Member;
import org.opennms.netmgt.config.views.Membership;
import org.opennms.netmgt.config.views.View;
import org.opennms.netmgt.config.views.Viewinfo;
import org.opennms.netmgt.config.views.Views;

public class ViewFactory
{
	/**
	 * The static singleton instance of the ViewFactory
	 */
	private static ViewFactory instance;
	
	/**
	 * File path of views.xml
	 */
	protected static File usersFile;
	
	/**
	 * An input stream for the views configuration file
	 */
	protected static InputStream configIn;
	
	/**
	 * A mapping of views ids to the View objects
	 */
	protected static HashMap m_views;
	
	/**
	 * Boolean indicating if the init() method has been called
	 */
	private static boolean initialized = false;
	
	private static Header oldHeader;

	/**
	 * Initializes the factory
	 */
	ViewFactory()
	{
	}
	
	public static synchronized void init()
		throws IOException, MarshalException, ValidationException
	{
		if (!initialized)
		{
			reload();
		}
	}
	
	/**
	 * Singleton static call to get the only instance that should exist for the ViewFactory
	 * @return the single view factory instance
	 */
	static synchronized public ViewFactory getInstance()
	{
		if (!initialized)
			return null;
		
		if (instance == null)
		{
			instance = new ViewFactory();
		}
		
		return instance;
	}
	
	/**
	 * Parses the views.xml via the Castor classes
	 */
	public static synchronized void reload() 
		throws IOException, MarshalException, ValidationException
	{
		configIn = new FileInputStream(ConfigFileConstants.getFile(ConfigFileConstants.VIEWS_CONF_FILE_NAME));
		Viewinfo viewinfo = (Viewinfo)Unmarshaller.unmarshal(Viewinfo.class, new InputStreamReader(configIn));
		Views views = viewinfo.getViews();
		oldHeader = viewinfo.getHeader();
		Collection viewsList = views.getViewCollection();
		m_views = new HashMap();
		
		Iterator i = viewsList.iterator();
		while(i.hasNext())
		{
			View curView = (View)i.next();
			m_views.put(curView.getName(), curView);
		}
		
		initialized = true;        
	}
	
	/**
	 * Adds a new user and overwrites the "users.xml"
	 */
	public synchronized void saveView(String name, View details) throws Exception
	{
		if(name == null || details == null)
		{
			throw new Exception ("UserFactory:saveUser  null");
		}
		else
		{
			m_views.put(name, details);
		}

                // Saves into "views.xml" file
		Views views = new Views();
		Collection viewList = (Collection)m_views.values();
		views.setViewCollection(viewList);
		saveViews(views);
	}

	/**
	 * Removes the user from the list of users. Then overwrites to the "users.xml"
	 */
	public synchronized void deleteUser(String name) throws Exception
	{
              	// Check if the user exists
                if(name != null)
                {
                        // Remove the user in the view.
                        Set viewKeys = (Set)m_views.keySet();
			Map map = new HashMap();
			
                        View view;
			Iterator iter = viewKeys.iterator();
                        while(iter.hasNext())
                        {
				View newView = new View();
                                view = (View)m_views.get((String)iter.next());
				newView = view;
				
				Membership membership = new Membership();
				Membership viewmembers = view.getMembership();
				if(viewmembers != null)
				{
					Enumeration enum = viewmembers.enumerateMember();
					while(enum.hasMoreElements())
					{
						Member member = (Member)enum.nextElement();
						if(member.getType().equals("user"))
						{
							if(!member.getContent().equals(name))
							{
								membership.addMember(member);
							}
						}
						else
							membership.addMember(member);
					}
				}
				newView.setMembership(membership);
				map.put(newView.getName(), newView);
                        }
			// Saves into "views.xml" file
			m_views.clear();
			Views views = new Views();
			views.setViewCollection((Collection)map.values());
			saveViews(views); 		
                }
                else
                {
                        throw new Exception("ViewFactory:delete Invalid user name:" + name );
                }
	}

	/**
	 * When this method is called users name is changed, so also is the username belonging to the group and the view.
	 * Also overwrites the "users.xml" file
	 */
	public synchronized void renameUser(String oldName, String newName) throws Exception
	{
               // Get the old data
                if(oldName == null || newName == null || oldName == "" || newName == "")
                {
                        throw new Exception ("Group Factory: Rename user.. no value ");
                }
                else
                {       

			Collection coll = (Collection)m_views.values();
			Iterator iter = (Iterator)coll.iterator();
			Map map = new HashMap();

			while(iter.hasNext())
			{
				View view = (View)iter.next();
				Membership membership = view.getMembership();
				if(membership != null)
				{
					Collection memberColl = membership.getMemberCollection();
					if(memberColl != null)
					{
						Iterator iterMember = (Iterator)memberColl.iterator();
						while(iterMember != null && iterMember.hasNext())
						{
							Member member = (Member)iterMember.next();
							if(member.getType().equals("user"))
							{
								String name = member.getContent();
								if(name.equals(oldName))
								{
									member.setContent(newName);
								}
							}
						}
					}
				}
				view.setMembership(membership);
				map.put(view.getName(), view );
			}
			m_views.clear();
			Views views = new Views();
			views.setViewCollection((Collection)map.values());
			saveViews(views);
		}
	}

        /**
         * When this method is called users name is changed.
         * Also overwrites the "views.xml" file
         */
	/*
        public synchronized void renameGroup(String oldName, String newName) throws Exception
        {
                
                // Check if the user exists
                if(oldName != null || !oldName.equals(""))
                {
                        // Rename the group in the view.
                        Enumeration viewKeys = (Enumeration)m_views.values();
                        View view;
                        while(viewKeys.hasMoreElements())
                        {
                                view = (View)m_views.get((String)viewKeys.nextElement());
				Membership membership = view.getMembership();
				Enumeration enummember = membership.enumerateMember();
				while(enummember.hasMoreElements())
				{
					Member member = (Member)enummember.nextElement();
					if(member.getContent().equals(oldName))
					{
						if(member.getType().equals("group"))
						{
							membership.removeMember(member);
							member.setContent(newName);
							membership.addMember(member);
							break;
						}
					}
				}
                        }
                }
                else
                {
                        throw new Exception("ViewFactory:rename Invalid view name:" + oldName );
                }
                // Saves into "views.xml" file
               	Collection coll = (Collection) m_views.values();
                Views views = new Views();
                views.setViewCollection(coll);
                saveViews(views);
        }
	*/
 
        /**
         * Removes the group from the list of groups. Then overwrites to the "views.xml"
         */
	/*
        public synchronized void deleteGroup(String name) throws Exception
        {
                 // Check if the user exists
                if(name != null || !name.equals(""))
                {
                        // Remove the user in the view.
                        Enumeration viewKeys = (Enumeration)m_views.values();
                        View view;
                        while(viewKeys.hasMoreElements())
                        {
                                view = (View)m_views.get((String)viewKeys.nextElement());
				Membership membership = view.getMembership();
				Enumeration enummember = membership.enumerateMember();
				while(enummember.hasMoreElements())
				{
					Member member = (Member)enummember.nextElement();
					if(member.getContent().equals(name))
					{
						if(member.getType().equals("group"))
						{
							membership.removeMember(member);
							break;
						}
					}
				}
                        }
                }
                else
                {
                        throw new Exception("ViewFactory:delete Invalid group name:" + name );
                }
		
                // Saves into "views.xml" file
               	Collection coll = (Collection) m_views.values();
                Views views = new Views();
                views.setViewCollection(coll);
                saveViews(views);
        } 
	*/

        /**
         * When this method is called views name is changed.
         * Also overwrites the "views.xml" file
         */
        public synchronized void renameView(String oldName, String newName) throws Exception
        {
                View view;
 
                // Check if the group exists
                if(oldName != null || !oldName.equals("") || newName != "" || oldName != "")
                {
                        if(m_views.containsKey(oldName))
                        {
                                view = (View)m_views.get(oldName);
 
                                // Remove the view.
                                m_views.remove(oldName);
                                view.setName(newName);
                                m_views.put(newName, view);
                        }
                        else
                                throw new Exception("ViewFactory:rename View doesnt exist:" + oldName);
                }
                else
                {
                        throw new Exception("ViewFactory:rename Invalid view name:" + oldName );
                }
                // Saves into "views.xml" file
                Collection coll = (Collection) m_views.values();
		Views views = new Views();
		views.setViewCollection(coll);
		saveViews(views);
        }
 
        /**
         * When this method is called view is to be deleted.
         * Also overwrites the "views.xml" file
         */
        public synchronized void deleteView(String name) throws Exception
        {
                // Check if the view exists
                if(name == null || name.equals(""))
                {
                        throw new Exception ("ViewFactory:deleteView  " + name);
                }
                else if(!m_views.containsKey(name))
                {
                        throw new Exception ("ViewFactory:deleteView     View:" + name + " not found ");
                }
                else
                {
                        m_views.remove(name);
                }
                // Saves into "views.xml" file
                Views views = new Views();
		Collection viewList = (Collection)m_views.values();
		views.setViewCollection(viewList);
		saveViews(views);
        } 

        /**
        */
        public synchronized void saveViews(Views views) throws IOException, MarshalException, ValidationException
        {
                //make a backup and save to xml
		Viewinfo vinfo = new Viewinfo();
		Header header = oldHeader;
		vinfo.setViews(views);
                header.setCreated(EventConstants.formatToString(new Date()));
		vinfo.setHeader(header);

		// Saves into "views.xml" file
		//
		File ofile = ConfigFileConstants.getFile(ConfigFileConstants.VIEWS_CONF_FILE_NAME);
		
                //marshall to a string first, then write the string to the file. This way the original config
                //isn't lost if the xml from the marshall is hosed.
                StringWriter stringWriter = new StringWriter();
                Marshaller.marshal( vinfo, stringWriter );
                if (stringWriter.toString()!=null)
                {
			FileWriter fileWriter = new FileWriter(ofile);
                        fileWriter.write(stringWriter.toString());
                        fileWriter.flush();
                        fileWriter.close();
                }

                //clear out the internal structure and reload it
                m_views.clear();
		
		Enumeration enum = views.enumerateView();
                while(enum.hasMoreElements())
                {
                        View curView = (View)enum.nextElement();
                        m_views.put(curView.getName(), curView);
                }
        }
 
        /**
        */
        public String getCategoryComments(String viewName, String categoryName)
        {
                View view = (View)m_views.get(viewName);
                Categories categories= view.getCategories();
                Collection category = (Collection)categories.getCategoryCollection();
		Iterator iter = category.iterator();
		org.opennms.netmgt.config.views.Category cat;
		while (iter.hasNext())
		{
			cat = (org.opennms.netmgt.config.views.Category)iter.next();
			String name = cat.getLabel();
			if(name != null && name.equals(categoryName))
			{
				return stripWhiteSpace(cat.getCategoryComment());
			}
		}
 
		return null;
        } 

        private String stripWhiteSpace(String comment)
        {
                StringBuffer buffer = new StringBuffer(comment);
                
                try {
                        RE whiteSpaceRE = new RE("[:space:]");
                        
                        for (int i = 0; i < buffer.length(); i++)
                        {
                                int start = i;
                                int end = start+1;
                                boolean foundWhiteSpace = false;
                                while (end < buffer.length() && whiteSpaceRE.match(buffer.substring(end-1, end)))
                                {
                                        foundWhiteSpace = true;
                                        end++;
                                }
                                
                                if (foundWhiteSpace)
                                {
                                        buffer.replace(start, end-1, " ");
                                }
                        }
                }
                catch (RESyntaxException e)
                {
                        return comment;
                }
                
                return buffer.toString();
        }
        
	/**
         * Return a <code>Map</code> of usernames to user instances.
         */
        public View getView(String name)
        {
		return (View)m_views.get(name);
        }        

	/**
         * Return a <code>Map</code> of usernames to user instances.
         */
        public Map getViews()
        {
                return m_views;
        }        
}
