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
//	Brian Weaver	<weave@opennms.org>
//	http://www.opennms.org/
//

package org.opennms.web.parsers;

import java.util.*;
import java.io.*;


/**This class allows another class to load the appropriate property
 * files into one complete Property object. It enables the loading of
 * a single specific property file, a specific property file and the
 * bluebird properties, or the previous two along with the System
 * properties where the System properties take precedence over the
 * previous two.
 * 
 * @author <A HREF="mailto:jason@opennms.org">Jason Johns</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 *
 * @version 1.1.1.1
*/
public class PropertyLoader
{
	/**The bluebird system property file name
	*/
	public static final String BLUEBIRD_PROP = "etc/bluebird";
	
	/**Only a local property file will be loaded
	*/
	public static final int LOCAL_ONLY      = 1 << 0;
	
	/**A local property and the bluebird properties will be
	   loaded into the same Properties object.
	*/
	public static final int LOCAL_BLUEBIRD  = 1 << 1;
	
	/**Loads a local property file, the bluebird properties, and
	   the system properties into the same Properties object. The
	   system properties will not be overwritten, so if a property was
	   set on the command line that will take precedece.
	*/
	public static final int ALL             = 1 << 2;
	
	/**This method decides which option the user wants and loads the 
	   files according to the decision.
	   @param String aFileName, the name of a specific property file to load
	   @param int loadMask, an integer describing which load option to use
	   @return Properties, the properties of the option
	*/
	public static Properties load(String aFileName, int loadMask)
	{
		Properties properties = null;
		
		if ( (loadMask & LOCAL_ONLY) == LOCAL_ONLY)
		{
			properties = loadLocal(aFileName);
		}
		
		if ( (loadMask & LOCAL_BLUEBIRD) == LOCAL_BLUEBIRD)
		{
			properties = loadLocal(BLUEBIRD_PROP, loadLocal(aFileName));
		}
		
		if ( (loadMask & ALL) == ALL)
		{
			properties = loadSystem(loadLocal(BLUEBIRD_PROP, loadLocal(aFileName)));
		}
		
		return properties;
	}
	
	/**This method loads a single specific property file.
	   @param String aFileName, name of the property file to load
	   @return Properties, the loaded properties
	*/
	public static Properties loadLocal(String aFileName)
	{
		Properties properties = null;
		
		FileInputStream pfile = null;
		try
		{
			properties = new Properties();
			pfile = new FileInputStream(aFileName);
			properties.load(pfile);
			pfile.close();
		}
		catch(IOException e)
		{
			// do nothing
		}
		
		return properties;
	}
	
	/**This method loads a specific property file in combination with some
	   existing properties. The specific properties will take precedence over
	   the existing properties.
	   @param String aFileName, name of the property file to load
	   @param Properties someProperties, the existing properties
	   @return Properties, the loaded properties
	*/
	public static Properties loadLocal(String aFileName, Properties someProperties)
	{
		Properties properties = null;
		
		FileInputStream pfile = null;
		try
		{
			properties = new Properties(someProperties);
			pfile = new FileInputStream(aFileName);
			properties.load(pfile);
			pfile.close();
		}
		catch(IOException e) 
		{
			// do nothing
		}
		
		return properties;
	}
	
	/**This method loads the System properties into an already existing
	   Properties object. The System properties will overwrite any duplicate
	   properties that already exist.
	   @param Properties someProperties, the existing properties
	   @return Properties, the loaded properties
	*/
	public static Properties loadSystem(Properties someProperties)
	{
		//	
		// iterate over keys in system map
		// and overwrite any in current properties.
		//
		Iterator i = System.getProperties().keySet().iterator();
		String key = null;
		
		while(i.hasNext())
		{
			key = (String)i.next();
			someProperties.setProperty(key, System.getProperty(key));
		}
		
		return someProperties;
	}
}
