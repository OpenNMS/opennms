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
//
// Tab Size = 8
//
//
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

import org.opennms.netmgt.config.viewsdisplay.*;
import org.opennms.netmgt.*;

import org.opennms.core.resource.Vault;

public class ViewsDisplayFactory
{
	/** The singleton instance. */
	private static ViewsDisplayFactory instance;

	/** File path of groups.xml. */
	protected File viewsDisplayFile;

	/** Boolean indicating if the init() method has been called. */
	protected boolean initialized = false;
        
	/** Timestamp of the viewDisplay file, used to know when to reload from disk. */
	protected long lastModified;

	/** Map of view objects by name. */        
	protected Map viewsMap;

	/**
	 * Empty private constructor so this class cannot be instantiated outside
	 * itself.
	 */
	private ViewsDisplayFactory() {}


	/** Be sure to call this method before calling getInstance(). */
	public static synchronized void init() throws IOException, FileNotFoundException, MarshalException, ValidationException {
		if(instance == null) {
			instance = new ViewsDisplayFactory();
			instance.reload();
			instance.initialized = true;
		}
	}
	
	
	/**
	 * Singleton static call to get the only instance that should exist for the ViewsDisplayFactory
	 * @return the single views display factory instance
	 * @throws IllegalStateException if init has not been called
	 */
	public static synchronized ViewsDisplayFactory getInstance() {
		if(instance == null) {
			throw new IllegalStateException("You must call ViewDisplay.init() before calling getInstance().");
		}
		
		return instance;
	}

	
	/**
	 * Parses the viewsdisplay.xml via the Castor classes
	 */
	public synchronized void reload() throws IOException, FileNotFoundException, MarshalException, ValidationException {
		this.viewsDisplayFile = ConfigFileConstants.getFile(ConfigFileConstants.VIEWS_DISPLAY_CONF_FILE_NAME);
		
		InputStream configIn = new FileInputStream(viewsDisplayFile);
		this.lastModified = viewsDisplayFile.lastModified();
		
		Viewinfo viewInfo = (Viewinfo)Unmarshaller.unmarshal(Viewinfo.class, new InputStreamReader(configIn));		
		this.viewsMap = new HashMap(); 
		
		Collection viewList = viewInfo.getViewCollection();
		Iterator i = viewList.iterator();

		while(i.hasNext()) {
			View view = (View)i.next();
			this.viewsMap.put(view.getViewName(), view);
		}
	}


	/** Can be null */
	public View getView(String viewName) throws IOException, MarshalException, ValidationException {
		if(viewName == null) {
			throw new IllegalArgumentException("Cannot take null parameters.");
		}
		
		this.updateFromFile();
		
		View view = (View)this.viewsMap.get(viewName);
		
		return view;
	}


	/**
	 * Reload the viewsdisplay.xml file if it has been changed since we last read it.
	 */
	protected void updateFromFile() throws IOException, MarshalException, ValidationException {
		if(this.lastModified != this.viewsDisplayFile.lastModified()) {
			this.reload();
		}
	}
}
