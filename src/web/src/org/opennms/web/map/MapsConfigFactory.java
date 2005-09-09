//
//Copyright (C) 2002 Sortova Consulting Group, Inc.  All rights reserved.
//Parts Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
//This program is free software; you can redistribute it and/or modify
//it under the terms of the GNU General Public License as published by
//the Free Software Foundation; either version 2 of the License, or
//(at your option) any later version.
//
//This program is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU General Public License for more details.
//
//You should have received a copy of the GNU General Public License
//along with this program; if not, write to the Free Software
//Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
//For more information contact:
//   OpenNMS Licensing       <license@opennms.org>
//   http://www.opennms.org/
//   http://www.sortova.com/
//
package org.opennms.web.map;

import java.util.*;
import java.util.Date;
import java.io.*;

import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;

import org.opennms.netmgt.*;

import org.opennms.web.map.config.*;

public class MapsConfigFactory {

	/**
	 * Singleton instance
	 */
	private static MapsConfigFactory instance;

	/**
	 * Object containing all Configuration info and objects parsed from the xml file
	 */
	protected static MapsConfiguration m_mapsConfiguration;

	/**
	 * Input stream for the general map configuration xml
	*/
	protected static InputStream configIn;

	/**
	 * Boolean indicating if the init() method has been called
	 */
	private static boolean initialized = false;

	/**
	 * The Maps Configuration File
	 */
	private static File m_mapsConfFile;

	/**
	 * A long Date Representing map Configuration File Last Modified Date
	*/
	private static long m_lastModified;

	/**
	 * Object Containig Header parsed from xml file
	 */
	private static Header oldHeader;

	private MapsConfigFactory() {
	}

	static synchronized public MapsConfigFactory getInstance() {
		if (!initialized)
			return null;

		if (instance == null) {
			instance = new MapsConfigFactory();
		}

		return instance;
	}

/**
 * 
 * @throws IOException
 * @throws FileNotFoundException
 * @throws MarshalException
 * @throws ValidationException
 */

	public static synchronized void init()
		throws
			IOException,
			FileNotFoundException,
			MarshalException,
			ValidationException {
		if (!initialized) {
			reload();
			initialized = true;
		}
	}

	public static synchronized void reload()
		throws IOException, MarshalException, ValidationException {
		m_mapsConfFile =
			AddConfigFileConstants.getFile(
				AddConfigFileConstants.MAP_CONF_FILE_NAME);
		InputStream configIn = new FileInputStream(m_mapsConfFile);
		m_lastModified = m_mapsConfFile.lastModified();

		m_mapsConfiguration=
			(MapsConfiguration) Unmarshaller.unmarshal(
				MapsConfiguration.class,
				new InputStreamReader(configIn));
		oldHeader = m_mapsConfiguration.getHeader();

	}

	/**
	 * 
	 * @return Hash Table containing  Object Building as function of Building Name
	 * @throws IOException
	 * @throws MarshalException
	 * @throws ValidationException
	 */

	public java.util.Map getMapsPlugin()
		throws IOException, MarshalException, ValidationException {

		updateFromFile();

		java.util.Map newMap = new HashMap();

		Plugins plugins = m_mapsConfiguration.getPlugins();
		MapsPlugin mapsPlugin[] = plugins.getMapsPlugin();
		for (int i = 0; i < mapsPlugin.length; i++) {
			newMap.put(mapsPlugin[i].getName(), mapsPlugin[i]);
		}

		return newMap;
	}

	private Header rebuildHeader() {
		Header header = oldHeader;

		header.setCreated(EventConstants.formatToString(new Date()));

		return header;
	}

	private static void updateFromFile()
		throws IOException, MarshalException, ValidationException {
		if (m_lastModified != m_mapsConfFile.lastModified()) {
			reload();
		}
	}

	public synchronized void saveCurrent()
	throws
		MarshalException,
		ValidationException,
		IOException,
		ClassNotFoundException {

	m_mapsConfiguration.setHeader(rebuildHeader());

    }
	
}
