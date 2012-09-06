/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.poller.remote.gwt.client.utils;

/**
 * <p>EqualsUtil class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class EqualsUtil {
	/**
	 * <p>areEqual</p>
	 *
	 * @param aThis a boolean.
	 * @param aThat a boolean.
	 * @return a boolean.
	 */
	static public boolean areEqual(boolean aThis, boolean aThat){
	    return aThis == aThat;
	  }

	  /**
	   * <p>areEqual</p>
	   *
	   * @param aThis a char.
	   * @param aThat a char.
	   * @return a boolean.
	   */
	  static public boolean areEqual(char aThis, char aThat){
	    return aThis == aThat;
	  }

	  /**
	   * <p>areEqual</p>
	   *
	   * @param aThis a long.
	   * @param aThat a long.
	   * @return a boolean.
	   */
	  static public boolean areEqual(long aThis, long aThat){
	    /*
	    * Implementation Note
	    * Note that byte, short, and int are handled by this method, through
	    * implicit conversion.
	    */
	    return aThis == aThat;
	  }

	  /**
	   * <p>areEqual</p>
	   *
	   * @param aThis a float.
	   * @param aThat a float.
	   * @return a boolean.
	   */
	  static public boolean areEqual(float aThis, float aThat){
		  return Float.valueOf(aThis) == Float.valueOf(aThat);
	  }

	  /**
	   * <p>areEqual</p>
	   *
	   * @param aThis a double.
	   * @param aThat a double.
	   * @return a boolean.
	   */
	  static public boolean areEqual(double aThis, double aThat){
		  return Double.valueOf(aThis) == Double.valueOf(aThat);
	  }

	  /**
	   * Possibly-null object field.
	   *
	   * Includes type-safe enumerations and collections, but does not include
	   * arrays. See class comment.
	   *
	   * @param aThis a {@link java.lang.Object} object.
	   * @param aThat a {@link java.lang.Object} object.
	   * @return a boolean.
	   */
	  static public boolean areEqual(Object aThis, Object aThat){
	    return aThis == null ? aThat == null : aThis.equals(aThat);
	  }

}
