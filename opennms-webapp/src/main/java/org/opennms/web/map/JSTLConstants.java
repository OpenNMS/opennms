/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2007-2008 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: July 6, 2007
 *
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.web.map;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;



/**
 * Class to reveal java constants to JSTL Expression Language
 * Uses reflection to scan the declared fields of a Constants class
 * Adds these fields to the Map.
 * Map is unmodifiable after initialization.
 *
 * @author <a href="mailto:antonio@opennms.it">Antonio Russo</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @author <a href="mailto:antonio@opennms.it">Antonio Russo</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @version $Id: $
 * @since 1.6.12
 */
public class JSTLConstants extends HashMap<String, Object> {
    private static final long serialVersionUID = 1L;
    
    private boolean initialised = false;
     
	/**
	 * <p>Constructor for JSTLConstants.</p>
	 */
	public JSTLConstants() {
		Category log = ThreadCategory.getInstance(MapsConstants.LOG4J_CATEGORY);
		Class c = this.getClass();
		Field[] fields = c.getDeclaredFields();
		for (int i = 0; i < fields.length; i++) {
 
			Field field = fields[i];
			int modifier = field.getModifiers();
			if (Modifier.isFinal(modifier) && !Modifier.isPrivate(modifier))
				try {
					this.put(field.getName(), field.get(this));
				}
				catch (IllegalAccessException e) {
					log.error("Error while instantiating JSTLConstants!",e);
				}
		}
		initialised = true;
	}
 
	/**
	 * <p>clear</p>
	 */
	public void clear() {
		if (!initialised) {
			super.clear();
        } else {
			throw new UnsupportedOperationException("Cannot modify this map");
        }
	}
 
	/** {@inheritDoc} */
	public Object put(String key, Object value) {
		if (!initialised) {
			return super.put(key, value);
        } else {
			throw new UnsupportedOperationException("Cannot modify this map");
        }
	}
 
	/** {@inheritDoc} */
	public void putAll(Map<? extends String, ? extends Object> m) {
		if (!initialised) {
			super.putAll(m);
        } else {
			throw new UnsupportedOperationException("Cannot modify this map");
        }
	}
 
	/**
	 * <p>remove</p>
	 *
	 * @param key a {@link java.lang.String} object.
	 * @return a {@link java.lang.Object} object.
	 */
	public Object remove(String key) {
		if (!initialised) {
			return super.remove(key);
        } else {
			throw new UnsupportedOperationException("Cannot modify this map");
        }
	}
}
 
