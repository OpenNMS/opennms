//
// Copyright (C) 2002 Sortova Consulting Group, Inc.  All rights reserved.
// Parts Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
//      http://www.sortova.com/
//
package org.opennms.netmgt.utils;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import java.util.StringTokenizer;

/**
 * Convenience class for looking up string and integer values 
 * in a parameter map.
 */
public class ParameterMap extends Object 
{
	/**
	 * This method is used to lookup a specific key in the map.
	 * If the mapped value is a string it is converted to an 
	 * integer and the original string value is replaced
	 * in the map. The converted value is returned to the caller.
	 * If the value cannot be converted then the default value is
	 * stored in the map. If the specified key does not exist in
	 * the map then the default value is returned.
	 *
	 * @return The int value associated with the key.
	 */
	public static int getKeyedInteger(Map map, String key, int defValue)
	{
		int value = defValue;
		Object oValue = map.get(key);

		if(oValue != null && oValue instanceof String)
		{
			try
			{
				value = Integer.parseInt((String)oValue);
			}
			catch(NumberFormatException ne)
			{
				value = defValue;
				ThreadCategory.getInstance(ParameterMap.class).info("getIntByKey: Failed to convert value " + oValue + " for key " + key);
			}
			map.put(key, new Integer(value));
		} 
		else if(oValue != null)
		{
			value = ((Integer)oValue).intValue();
		}
		return value;
	}

	/**
	 * This method is used to lookup a specific key in 
	 * the map. If the mapped value is a string is is converted
	 * to an integer and the original string value is replaced
	 * in the map. The converted value is returned to the caller.
	 * If the value cannot be converted then the default value is
	 * used.
	 *
	 * @return The array of integer values associated with the key.
	 */
	public final static int[] getKeyedIntegerArray(Map map, String key, int[] defValues)
	{
		int[] result = defValues;
		Object oValue = map.get(key);

		if(oValue != null && oValue instanceof int[])
		{
			result = (int[]) oValue;
		}
		else if(oValue != null)
		{
			List tmpList = new ArrayList(5);

			// Split on spaces, commas, colons, or semicolons
			//
			StringTokenizer ints = new StringTokenizer(oValue.toString(), " ;:,");
			while(ints.hasMoreElements())
			{
				try
				{
					int x = Integer.parseInt(ints.nextToken());
					tmpList.add(new Integer(x));
				}
				catch(NumberFormatException e)
				{
					ThreadCategory.getInstance(ParameterMap.class).warn("getKeyedIntegerList: list member for key " + key + " is malformed", e);
				}
			}
			result = new int[tmpList.size()];

			for(int x = 0; x < result.length; x++)
				result[x] = ((Integer)tmpList.get(x)).intValue();

			map.put(key, result);
		} 
		return result;
	}
	
	/**
	 * This method is used to lookup a specific key in 
	 * the map. If the mapped value is not a String it is converted
	 * to a String and the original value is replaced
	 * in the map. The converted value is returned to the caller.
	 * If the specified key does not exist in the map then 
	 * the default value is returned.
	 *
	 * @return The String value associated with the key.
	 */
	public static String getKeyedString(Map map, String key, String defValue)
	{
	
		String value = defValue;
		Object oValue = map.get(key);

		if(oValue != null && oValue instanceof String)
		{
			value = (String)oValue;
		} 
		else if(oValue != null)
		{
			value = oValue.toString();
			map.put(key, value);
		}
		return value;
	}
}
