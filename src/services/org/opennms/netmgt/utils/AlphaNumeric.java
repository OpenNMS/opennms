package org.opennms.netmgt.utils;

import java.util.*;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;

/**
 * Convenience classes for parsing and manipulating Strings.
 *
 * @author <a href="mailto:mike@opennms.org">Mike Davidson</a>
 * @author <a href="http://www.opennms.org">OpenNMS</a> 
 */ 
public class AlphaNumeric extends Object
{

    /** 
	 * Any character in the passed string which does not match one of the
	 * following values is replaced by the specified replacement character.
	 * 
	 * 	Ascii chars: 	0 - 9 (Decimal 48 - 57)
	 * 			A - Z (Decimal 65 - 90)
	 * 			a - z (Decimal 97 - 122)
	 *
	 * For example: 'Ethernet 10/100' is converted to 'Ethernet_10_100'
	 *
	 * @param str	string to be converted
	 * @param char	replacement character
	 * 
	 * @return Converted value which can be used in a file name.
	 */
	public static String parseAndReplace(String str, char replacement) 
	{ 
		if (str == null)
		{
			return "";
		}
		else
		{
			boolean replacedChar = false;
			byte[]	bytes = str.getBytes();

			for(int x = 0; x < bytes.length; x++)
			{
				if ( !( (bytes[x] >= 48 && bytes[x] <= 57) ||
					(bytes[x] >= 65 && bytes[x] <= 90) ||
					(bytes[x] >= 97 && bytes[x] <= 122) ) )
				{
					bytes[x] = (byte)replacement; 
					replacedChar = true;
				}
			}

			String temp = new String(bytes);

			// Log4j category
			//
			Category log = ThreadCategory.getInstance(AlphaNumeric.class);
			if (log.isDebugEnabled())
			{
				if (replacedChar)
					log.debug("parseAndReplace: original='" + str + "'" + " new='" + temp + "'");
			}

			return temp;
		}
	}
	
	/** 
	 * Any character in the passed string which does not match one of the
	 * following values is replaced by an Ascii space and then trimmed	 
	 * from the resulting string.
	 * 
	 * 	Ascii chars: 	0 - 9 (Decimal 48 - 57)
	 * 			A - Z (Decimal 65 - 90)
	 * 			a - z (Decimal 97 - 122)
	 *
	 * @param str	string to be converted
	 * 
	 * @return Converted value.
	 */
	public static String parseAndTrim(String str) 
	{ 
		if (str == null)
		{
			return "";
		}
		else
		{
			byte[]	bytes = str.getBytes();

			for(int x = 0; x < bytes.length; x++)
			{
				if ( !( (bytes[x] == 32) ||
					(bytes[x] >= 48 && bytes[x] <= 57) ||
					(bytes[x] >= 65 && bytes[x] <= 90) ||
					(bytes[x] >= 97 && bytes[x] <= 122) ) )
				{
						bytes[x] = 32; // Ascii space
				}
			}

			String temp = new String(bytes);
			temp = temp.trim();

			return temp;
		}
	}
}
