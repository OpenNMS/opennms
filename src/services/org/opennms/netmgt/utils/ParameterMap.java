package org.opennms.netmgt.utils;

import java.util.Map;
import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;

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
