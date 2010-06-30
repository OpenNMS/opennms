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
// 31 Jan 2003: Cleaned up some unused imports.
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
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//

package org.opennms.netmgt.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.opennms.core.utils.ThreadCategory;

/**
 * Convenience class for looking up string and integer values in a parameter
 * map.
 *
 * @author ranger
 * @version $Id: $
 */
public class ParameterMap extends Object {
	
    /**
     * <p>getKeyedLong</p>
     *
     * @param map a {@link java.util.Map} object.
     * @param key a {@link java.lang.String} object.
     * @param defValue a long.
     * @return a long.
     */
    @Deprecated
    public static long getKeyedLong(final Map map, final String key, final long defValue) {
      return org.opennms.core.utils.ParameterMap.getKeyedLong(map, key, defValue);
    }
	
    /**
     * <p>getKeyedInteger</p>
     *
     * @param map a {@link java.util.Map} object.
     * @param key a {@link java.lang.String} object.
     * @param defValue a int.
     * @return a int.
     */
    @Deprecated
    public static int getKeyedInteger(final Map map, final String key, final int defValue) {
    	return org.opennms.core.utils.ParameterMap.getKeyedInteger(map, key, defValue);
    }

    /**
     * <p>getKeyedIntegerArray</p>
     *
     * @param map a {@link java.util.Map} object.
     * @param key a {@link java.lang.String} object.
     * @param defValues an array of int.
     * @return an array of int.
     */
    @Deprecated
    public final static int[] getKeyedIntegerArray(final Map map, final String key, final int[] defValues) {
      return org.opennms.core.utils.ParameterMap.getKeyedIntegerArray(map, key, defValues);
    }

    /**
     * <p>getKeyedString</p>
     *
     * @param map a {@link java.util.Map} object.
     * @param key a {@link java.lang.String} object.
     * @param defValue a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    @Deprecated
    public static String getKeyedString(final Map map, final String key, final String defValue) {
      return org.opennms.core.utils.ParameterMap.getKeyedString(map, key, defValue);
    }

    /**
     * <p>getKeyedBoolean</p>
     *
     * @param map a {@link java.util.Map} object.
     * @param key a {@link java.lang.String} object.
     * @param defValue a boolean.
     * @return a boolean.
     */
    @Deprecated
    public static boolean getKeyedBoolean(final Map map, final String key, final boolean defValue) {
      return org.opennms.core.utils.ParameterMap.getKeyedBoolean(map, key, defValue);
    }
}
