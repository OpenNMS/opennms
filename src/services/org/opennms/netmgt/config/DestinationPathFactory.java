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
package org.opennms.netmgt.config;

import java.util.*;
import java.io.*;

import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;

import org.opennms.netmgt.config.destinationPaths.*;
import org.opennms.netmgt.*;

/**
*/
public class DestinationPathFactory
{
	/**
         *
         */
        private static DestinationPaths allPaths;
        
        /**
         *
	 */
	private static DestinationPathFactory instance;
	
	/**
         *
	 */
	private static Map m_destinationPaths;
	
	/**
         *
	 */
	private static File m_notifConfFile;
	
	/**
         *
	 */
	protected static InputStream configIn;
	
	/**
	 * Boolean indicating if the init() method has been called
	 */
	private static boolean initialized = false;
        
        /**
         *
         */
        private static Header oldHeader;
	
	/**
	 *
	 */
        private static File m_pathsConfFile;
        
        /**
         *
	 */
	private static long m_lastModified;
	
	/**
	 *
	 */
	private DestinationPathFactory() 
	{
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
	public static synchronized DestinationPathFactory getInstance() 
	{
		if (!initialized)
			return null;
		
		if (instance == null)
		{
			instance = new DestinationPathFactory();
		}
		
		return instance;
	}
	
	/**
	 *
	 */
	public static synchronized void reload()
		throws IOException, MarshalException, ValidationException, FileNotFoundException
	{
                m_pathsConfFile = ConfigFileConstants.getFile(ConfigFileConstants.DESTINATION_PATHS_CONF_FILE_NAME);
                
                InputStream configIn = new FileInputStream(m_pathsConfFile);
                m_lastModified = m_pathsConfFile.lastModified();
                
                allPaths = (DestinationPaths)Unmarshaller.unmarshal(DestinationPaths.class, new InputStreamReader(configIn));
                oldHeader = allPaths.getHeader();
                
                m_destinationPaths = new TreeMap();
		
		Iterator i = allPaths.getPathCollection().iterator();
		while(i.hasNext())
		{
			Path curPath = (Path)i.next();
			m_destinationPaths.put(curPath.getName(), curPath);
		}
	}
	
	/**
	 *
	 */
	public Path getPath(String pathName)
                throws IOException, MarshalException, ValidationException
	{
		updateFromFile();
                
		return (Path)m_destinationPaths.get(pathName);
	}
	
	/**
	 *
	 */
	public Map getPaths()
                throws IOException, MarshalException, ValidationException
	{
		updateFromFile();
                
		return m_destinationPaths;
	}
	
	/**
	 *
	 */
	public Collection getTargetCommands(Path path, int index, String target)
                throws IOException, MarshalException, ValidationException
	{
		updateFromFile();
                
		Target[] targets = getTargetList(index, path);
		
		for (int i = 0; i < targets.length; i++)
                {
			if (targets[i].getName().equals(target))
				return targets[i].getCommandCollection();
		}
		
		//default null value if target isn't found in Path
		return null;
	}
        
        /**
         *
         */
        public Target[] getTargetList(int index, Path path)
                throws IOException, MarshalException, ValidationException
        {
                updateFromFile();
                
                Target[] targets = null;
                //index of -1 indicates the initial targets, any other index means to get
                //the targets from the Escalate object at that index
                if (index==-1)
                {
                        targets = path.getTarget();
                }
                else
                {
                        targets = path.getEscalate(index).getTarget();
                }
                
                return targets;
        }
	
	/**
	 *
	 */
	public boolean pathHasTarget(Path path, String target)
                throws IOException, MarshalException, ValidationException
	{
		updateFromFile();
                
		Collection targets = path.getTargetCollection();
		
		Iterator i = targets.iterator();
		while(i.hasNext())
		{
			Target curTarget = (Target)i.next();
			if (curTarget.getName().equals(target))
				return true;
		}
		
		//default false value if target isn't found
		return false;
	}
        
        /**
         *
         */
        public synchronized void addPath(Path newPath)
                throws MarshalException, ValidationException, IOException
        {
                m_destinationPaths.put(newPath.getName(), newPath);
                
                saveCurrent();
        }
        
        /**
         *
         */
        public synchronized void replacePath(String oldName, Path newPath)
                throws MarshalException, ValidationException, IOException
        {
                if (m_destinationPaths.containsKey(oldName))
                {
                        m_destinationPaths.remove(oldName);
                }
                
                addPath(newPath);
        }
        
        /** Removes a Path from the xml file.
         *  @param path, the path to remove
         *  @exception MarshalException
         *  @exception ValidationException
         *  @exception IOException
         *
         */
        public synchronized void removePath(Path path)
                throws MarshalException, ValidationException, IOException
        {
                m_destinationPaths.remove(path.getName());
                saveCurrent();
        }
        
        /** Removes a Path form the xml file based on its name
         *  @param name, the name of the path to remove
         *  @exception MarshalException
         *  @exception ValidationException
         *  @exception IOException
         */
        public synchronized void removePath(String name)
                throws MarshalException, ValidationException, IOException
        {
                m_destinationPaths.remove(name);
                saveCurrent();
        }
        
        /**
         *
         */
        public synchronized void saveCurrent()
                throws MarshalException, ValidationException, IOException
        {
                allPaths.clearPath();
                Iterator i = m_destinationPaths.keySet().iterator();
                while(i.hasNext())
                {
                        allPaths.addPath( (Path)m_destinationPaths.get(i.next()));
                }
                
                allPaths.setHeader(rebuildHeader());
                
                //marshall to a string first, then write the string to the file. This way the original config
                //isn't lost if the xml from the marshall is hosed.
                StringWriter stringWriter = new StringWriter();
		Marshaller.marshal( allPaths, stringWriter );
                if (stringWriter.toString()!=null)
                {
                        FileWriter fileWriter = new FileWriter(m_pathsConfFile);
                        fileWriter.write(stringWriter.toString());
                        fileWriter.flush();
                        fileWriter.close();
                }
                
                reload();
        }
        
        /**
         *
         */
        private Header rebuildHeader()
        {
                Header header = oldHeader;
                
                header.setCreated(EventConstants.formatToString(new Date()));
                
                return header;
        }
        
        /**
         *
         */
        private static void updateFromFile()
                throws IOException, MarshalException, ValidationException
        {
                if (m_lastModified != m_pathsConfFile.lastModified())
		{
			reload();
		}
        }
}
