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
package org.opennms.netmgt.config;

import java.io.*;

import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;

import org.opennms.netmgt.*;
import org.opennms.netmgt.config.linkd.*;

public class LinkdConfigFactory {

	/**
	 * Singleton instance
	 */
	private static LinkdConfigFactory instance;

	/**
	 * Object containing all Buildings and Room objects parsed from the xml file
	 */
	protected static LinkdConfiguration m_linkdconfiguration;

	/**
	 * Input stream for the general Asset Location configuration xml
	*/
	protected static InputStream configIn;

	/**
	 * Boolean indicating if the init() method has been called
	 */
	private static boolean initialized = false;

	/**
	 * The Asset Location Configuration File
	 */
	private static File m_linkdConfFile;

	private LinkdConfigFactory() {
	}

	static synchronized public LinkdConfigFactory getInstance() {
		if (!initialized)
			return null;

		if (instance == null) {
			instance = new LinkdConfigFactory();
		}

		return instance;
	}

/**
 * 
 * @throws IOException
 * @throws FileNotFoundException
 * @throws MarshalException
 * @throws ValidationException
 * @throws ClassNotFoundException
 */

	public static synchronized void init()
		throws
			IOException,
			FileNotFoundException,
			MarshalException,
			ValidationException,
			ClassNotFoundException {
		if (!initialized) {
			reload();
			initialized = true;
		}
	}

	public static synchronized void reload()
		throws IOException, MarshalException, ValidationException {
		m_linkdConfFile =
			ExtendedConfigFileConstants.getFile(
			        ExtendedConfigFileConstants.LINKD_CONF_FILE_NAME);
		InputStream configIn = new FileInputStream(m_linkdConfFile);

		m_linkdconfiguration =
			(LinkdConfiguration) Unmarshaller.unmarshal(
				LinkdConfiguration.class,
				new InputStreamReader(configIn));
	}

	/**
	 * 
	 * @return Int Initial Sleep Time
	 * @throws IOException
	 * @throws MarshalException
	 * @throws ValidationException
	 */

	public int getInitialSleepTime()
		throws IOException, MarshalException, ValidationException {

		updateFromFile();

		int initialSleepTime = 900000;

		if (m_linkdconfiguration.hasInitial_sleep_time()) {
     		initialSleepTime = m_linkdconfiguration.getInitial_sleep_time();
		}

		return initialSleepTime;
	}

	/**
	 * 
	 * @return Int Sleep Time
	 * @throws IOException
	 * @throws MarshalException
	 * @throws ValidationException
	 */

	public int getSleepTime()
		throws IOException, MarshalException, ValidationException {

		updateFromFile();

		int sleepTime = 30000;

		if (m_linkdconfiguration.hasSleep_time()) {
     		sleepTime = m_linkdconfiguration.getSleep_time();
		}

		return sleepTime;
	}


	public synchronized void saveCurrent()
		throws
			MarshalException,
			ValidationException,
			IOException,
			ClassNotFoundException {

		//marshall to a string first, then write the string to the file. This way the original config
		//isn't lost if the xml from the marshall is hosed.
		StringWriter stringWriter = new StringWriter();
		Marshaller.marshal(m_linkdconfiguration, stringWriter);
		if (stringWriter.toString() != null) {
			FileWriter fileWriter = new FileWriter(m_linkdConfFile);
			fileWriter.write(stringWriter.toString());
			fileWriter.flush();
			fileWriter.close();
		}

		reload();
	}

	private static void updateFromFile()
		throws IOException, MarshalException, ValidationException {
				reload();
		}
}
