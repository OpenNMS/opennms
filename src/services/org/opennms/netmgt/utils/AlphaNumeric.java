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
