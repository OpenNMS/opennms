//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 Blast Internet Services, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of Blast Internet Services, Inc.
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
//      http://www.blast.com/
//

package org.opennms.netmgt.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import org.opennms.core.utils.ThreadCategory;

/**
 * Convenience class for generating a list of IP addresses from 
 * a file URL.
 */
public class IpListFromUrl extends Object 
{
	/**
	 * The string indicating the start of the comments in a
	 * line containing the IP address in a file URL
	 */
	private final static String COMMENT_STR		= " #";

	/**
	 * This character at the start of a line indicates a comment line in a URL file
	 */
	private final static char COMMENT_CHAR		= '#';

	/**
	 * <p>This method is used to read all interfaces from an URL file</p>
	 *
	 * <pre>The file URL is read and each entry in this file checked. Each line
	 * in the URL file can be one of -
	 * <IP><space>#<comments>
	 * or
	 * <IP>
	 * or
	 * #<comments>
	 *
	 * Lines starting with a '#' are ignored and so are characters after
	 * a '<space>#' in a line.</pre>
	 *
	 * @param URL	The url file to read
	 *
	 * @return	list of IPs in the file
	 */
	public static java.util.List parse(String url)
	{
		java.util.List iplist = new ArrayList();

		try
		{
			// open the file indicated by the url
			URL fileURL = new URL(url);
			
			File file = new File(fileURL.getFile());
		
			//check to see if the file exists
			if(file.exists())
			{
				BufferedReader buffer = new BufferedReader(new FileReader(file));
			
				String ipLine = null;
				String specIP =null;
		
				// get each line of the file and turn it into a specific range
				while( (ipLine = buffer.readLine()) != null )
				{
					ipLine = ipLine.trim();
					if (ipLine.length() == 0 || ipLine.charAt(0) == COMMENT_CHAR)
					{
						// blank line or skip comment
						continue;
					}

					// check for comments after IP
					int comIndex = ipLine.indexOf(COMMENT_STR);
					if (comIndex == -1)
					{
						specIP = ipLine;
					}
					else 
					{
						specIP = ipLine.substring(0, comIndex);
						ipLine = ipLine.trim();
					}


					iplist.add(specIP);
				}
			
				buffer.close();
			}
			else
			{
				// log something
				ThreadCategory.getInstance(IpListFromUrl.class).warn("URL does not exist: " + url.toString());
			}
		}
		catch(MalformedURLException e)
		{
			ThreadCategory.getInstance(IpListFromUrl.class).error("Error reading URL: " + url.toString() + ": " + e.getLocalizedMessage());
		}
		catch(FileNotFoundException e)
		{
			ThreadCategory.getInstance(IpListFromUrl.class).error("Error reading URL: " + url.toString() + ": " + e.getLocalizedMessage());
		}
		catch(IOException e)
		{
			ThreadCategory.getInstance(IpListFromUrl.class).error("Error reading URL: " + url.toString() + ": " + e.getLocalizedMessage());
		}
		
		return iplist;
	}
}
