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
//
// Orignal code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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

package org.opennms.netmgt.rrd;

import org.opennms.core.utils.ThreadCategory;

/**
 * This is a singleton class which provides an interface through which
 * RRD (Round Robin Database) functions (rrd_create(), rrd_update(), and others) 
 * can be accessed from Java code.
 *
 * <pre>The native method 'launch()' takes a single argument which is a 
 * RRD command string of similar format to what RRDtool takes.  Please
 * note the following examples:
 * 
 * 	"create test.rrd --start N DS:ifOctetsIn:COUNTER:600:U:U \
 *       	RRA:AVERAGE:0.5:1:24"
 *
 * 	"update test.rrd --template:ifOctetsIn N:123456789"
 * 
 * Refer to www.rrdtool.org for additional examples and information on 
 * the format of rrdtool command strings.
 * 
 * Currently only 'create', 'update', and 'fetch' commands are supported.</pre>
 *
 * @author <A HREF="mailto:mike@opennms.org">Mike</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 *
 * @version  1.1.1.1
 *
 */
public final class Interface 
{
	/**
	 * The singleton instance of the interface
	 */
	private static Interface m_singleton=null;
	
	/**
	 * This member is set to true if the 'jrrd' library 
	 * (libjrrd.so on linux/unix) has has been loaded.
	 */
	private static boolean m_loaded=false;
	
	/**
	 * Native method implemented in C which provides an interface 
	 * to the lower-level RRD functions  
	 * 
	 * WARNING: The RRD C api (rrd_update(), rrd_create(), etc...) relies on 
	 * getopt() & therefore is not thread safe.  This method is therefore
	 * synchronized in order to prevent more than one thread access at a time.
	 * 
	 * @param cmd	RRDtool style command string to be executed.
	 *		Currently supported RRD commands are:
	 * 			'create' - calls rrd_create()
	 * 			'update' - calls rrd_update()
	 *			'fetch'  - calls rrd_fetch()
	 *
	 * @return array of Java String objects
	 *
	 * 		In the case of rrd_fetch() the returned String array
	 * 		has the following characteristics:
	 *
	 *		String[0] = error text if command failed or NULL if successful
	 * 		String[1] = for 'fetch' cmd: data source names
	 *  		String[2..n] = for 'fetch' cmd: retrieved data rows for each 
	 * 				interval between start and end parms
	 */
	public static synchronized native String[] launch(String cmd);
	
	/**
	 * Load the jrrd library and create the singleton instance of 
	 * the interface.
	 * 
	 * @throws SecurityException if we don't have permission to load the library
	 * @throws UnsatisfiedLinkError if the library doesn't exist
	 */
	public static synchronized void init()
		throws 	SecurityException,
			UnsatisfiedLinkError
	{
		if (m_loaded)
		{
			// init already called - return
			// to reload, reload() will need to be called
			return;
		}

		ThreadCategory.getInstance(Interface.class).debug("init: loading jrrd library...");
		m_singleton = new Interface();
		m_loaded = true;
	}

	/**
	 * Reload the jrrd library and create the singleton instance
	 * of the interface.
	 *
	 * @throws SecurityException if we don't have permission to load the library
	 * @throws UnsatisfiedLinkError if the library doesn't exist
	 */
	public static synchronized void reload()
		throws 	SecurityException,
			UnsatisfiedLinkError
	{
		m_singleton = null;
		m_loaded = false;

		init();
	}
	
	/** 
	 * Constructor.  Responsible for loading the jrrd shared/dynamic
	 * link library which contains the implementation of the
	 * 'launch()' native method.
	 *
	 * @throws SecurityException if we don't have permission to load the library
	 * @throws UnsatisfiedLinkError if the library doesn't exist
	 */
	private Interface ()
		throws 	SecurityException,
			UnsatisfiedLinkError
	{
		System.loadLibrary("jrrd");
	}
	
	/**
	 * Return the singleton instance of this class.
	 *
	 * @return The current instance.
	 *
	 * @throws java.lang.IllegalStateException Thrown if the interface
	 * 	has not yet been initialized.
	 */
	public static synchronized Interface getInstance()
	{
		if(!m_loaded)
			throw new IllegalStateException("The RRD JNI interface has not been initialized");
			
		return m_singleton;
	}
	
	/** 
	 * Debug purposes only
	 */
	public static void main(String[] argv)
	{
		try
		{
			// initialize the interface
			Interface.reload();
			
			// build create command
			// Build RRD create command prefix
			String filename=argv[0];
			System.out.println("filename=" + filename);
			String cmd = "create \"" + filename + "\" --start N" + 
					" --step 900 DS:test:COUNTER:900:0:100 RRA:MIN:0.5:1:1000";
			
			// issue rrd command
			System.out.println("issuing RRD cmd: " + cmd);
			Interface.launch(cmd);
			System.out.println("command completed.");
		}
		catch (Throwable t)
		{
			System.out.println("unexpected error, reason: " + t.getMessage());
		}
	}
}
