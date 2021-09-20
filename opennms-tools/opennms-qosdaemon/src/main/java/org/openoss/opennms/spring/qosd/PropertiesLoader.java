/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.openoss.opennms.spring.qosd;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Properties;
import java.util.Set;

/**
 * Class to load properties from the properties file.
 * This loads the file once and returns a singleton on further calls
 *
 * @author ranger
 * @version $Id: $
 */
public class PropertiesLoader {
	private static PropertiesLoader instance = null;
	private final HashMap<String,String> properties = new HashMap<String,String>();

    private PropertiesLoader() throws FileNotFoundException, IOException{

		// Load the properties file using the filename given as the VM startup parameter (-DpropertiesFile)
		Properties props = new Properties();
		String propertiesFilename = System.getProperty("propertiesFile");
		if (propertiesFilename == null) throw new FileNotFoundException();

		FileInputStream inStream = new FileInputStream(propertiesFilename);

		props.load(inStream);
		inStream.close();

		// Store the property values
		for (Enumeration<?> e = props.propertyNames(); e.hasMoreElements(); ){
			String key = (String)e.nextElement();
			String value = props.getProperty(key);
			properties.put(key, value);
		}
	}

	// This will return a single instance of this call (creating one if none exist)
	// call it using StartProperties appParams = StartProperties.getInstance();
	/**
	 * <p>Getter for the field <code>instance</code>.</p>
	 *
	 * @return a {@link org.openoss.opennms.spring.qosd.PropertiesLoader} object.
	 * @throws java.io.FileNotFoundException if any.
	 * @throws java.io.IOException if any.
	 */
	public static PropertiesLoader getInstance() throws FileNotFoundException, IOException
	{
		if (instance == null) // test if an instance exists, if so then return it
		{
			// Create an instance in a synchronized block to avoid access by multiple threads
			// (avoids inefficiency of declaring method body synchronized)
			synchronized(org.openoss.opennms.spring.qosd.PropertiesLoader.class)
			{
				// check once again to ensure first check didn't let two threads through at the same time
				if (instance == null)
					instance = new PropertiesLoader(); // create the new instance
			}
		}
		return instance; // return the single StartProperties object
	}

	/**
	 * <p>getProperty</p>
	 *
	 * @param propertyName a {@link java.lang.String} object.
	 * @return a {@link java.lang.String} object.
	 * @throws java.lang.IllegalArgumentException if any.
	 */
	public String getProperty(String propertyName) throws IllegalArgumentException{
		String _out = (String)properties.get(propertyName);
		if(_out == null)throw new IllegalArgumentException();
		return _out;
	}

	/**
	 * <p>getPropertyNames</p>
	 *
	 * @return a {@link java.util.Set} object.
	 */
    public Set<String> getPropertyNames()
	{
		return properties.keySet();
	}
}
