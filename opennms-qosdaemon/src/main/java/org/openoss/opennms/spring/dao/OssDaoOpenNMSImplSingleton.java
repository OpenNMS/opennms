// This file is part of the OpenNMS(R) QoSD OSS/J interface.
//
// Copyright (C) 2006-2007 Craig Gallen, 
//                         University of Southampton,
//                         School of Electronics and Computer Science
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
//
// See: http://www.fsf.org/copyleft/lesser.html
//





package org.openoss.opennms.spring.dao;


/**
 * Wrapper class for OssDaoOpenNMSImpl which makes it onto a singleton which can be shared
 * between Qosd and QoSDrx when either one or or both applications are running. This is needed because Qosd and
 * QoSDrx have different local application contexts but need to share access to the OssDao which provides the
 * synchronized alarm cache for both applications.
 * Either Qosd or QoSDrx can initialise the class. The first call to getInstance() causes the class to be created.
 * All subsiquent calls return the same instance.
 *
 * Note it is expected thet the Spring application context has already set up the opennms DAO's before the first
 * call to getInstance(). This means that you must ensure that the application contexts for Qosd and QosDrx
 * set the same values for these DAO's otherwise there will be unpredictable results
 *
 * @author ranger
 * @version $Id: $
 */
public class OssDaoOpenNMSImplSingleton extends OssDaoOpenNMSImpl{
	private static OssDaoOpenNMSImplSingleton instance = null;
	

	private OssDaoOpenNMSImplSingleton() {
		//		 not used	
	}

	// This will return a single instance of this call (creating one if none exist)
	/**
	 * <p>Getter for the field <code>instance</code>.</p>
	 *
	 * @return a {@link org.openoss.opennms.spring.dao.OssDaoOpenNMSImplSingleton} object.
	 */
	public static OssDaoOpenNMSImplSingleton getInstance() {
		if (instance == null) // test if an instance exists, if so then return it
		{
			
			// Create an instance in a synchronized block to avoid access by multiple threads
			// (avoids inefficiency of declaring method body synchronized)
			synchronized(org.openoss.opennms.spring.dao.OssDaoOpenNMSImplSingleton.class)
			{
				// check once again to ensure first check didn't let two threads through at the same time
				if (instance == null)
					instance = new OssDaoOpenNMSImplSingleton(); // create the new instance
				instance.setalarmDao(_alarmDao);
				instance.setassetRecordDao(_assetRecordDao);
				instance.setdataSource(_dataSource);
				instance.setnodeDao(_nodeDao);
			}
		}
		return instance; // return the single OssDaoOpenNMSImplSingleton object
	}
	
// TODO remove	
//	/**
//	 * Initialise the OssDaoOpenNMSImplSingleton object
//	 */
//	public static void init(){
//		// initialisaton methods if needed
//		// not used	
//	}
//	

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

}
