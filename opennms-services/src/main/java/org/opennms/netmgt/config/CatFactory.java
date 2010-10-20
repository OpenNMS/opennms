//
//This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2005 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
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
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
//
package org.opennms.netmgt.config;

import java.util.concurrent.locks.Lock;

import org.opennms.netmgt.config.categories.Category;
import org.opennms.netmgt.config.categories.Catinfo;

/**
 * <p>CatFactory interface.</p>
 *
 * @author jsartin
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 * @version $Id: $
 */
public interface CatFactory {

	/**
	 * <p>getConfig</p>
	 *
	 * @return a {@link org.opennms.netmgt.config.categories.Catinfo} object.
	 */
	public Catinfo getConfig();
	
	/**
	 * <p>getCategory</p>
	 *
	 * @param name a {@link java.lang.String} object.
	 * @return a {@link org.opennms.netmgt.config.categories.Category} object.
	 */
	public Category getCategory(String name);
	
	/**
	 * <p>getEffectiveRule</p>
	 *
	 * @param catLabel a {@link java.lang.String} object.
	 * @return a {@link java.lang.String} object.
	 */
	public String getEffectiveRule(String catLabel);
	
	/**
	 * <p>getNormal</p>
	 *
	 * @param catlabel a {@link java.lang.String} object.
	 * @return a double.
	 */
	public double getNormal(String catlabel);
	
	/**
	 * <p>getWarning</p>
	 *
	 * @param catlabel a {@link java.lang.String} object.
	 * @return a double.
	 */
	public double getWarning(String catlabel);

    public Lock getReadLock();
    
    public Lock getWriteLock();
	
}
