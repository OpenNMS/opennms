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
package org.opennms.netmgt.capsd;

/**
 * <P>This class is designed to be used by the capabilities
 * daemon to test for the existance of an Lotus Notes HTTP server on 
 * remote interfaces. The class implements the CapsdPlugin
 * interface that allows it to be used along with other
 * plugins by the daemon.</P>
 *
 * @author <A HREF="mailto:jason@opennms.org">Jason</A>
 * @author <A HREF="http://www.opennsm.org">OpenNMS</A>
 *
 * @version 1.1.1.1
 *
 */
public class NotesHttpPlugin
	extends HttpPlugin
{
	public NotesHttpPlugin()
        {
                PROTOCOL_NAME	= "NotesHTTP";
                CHECK_RETURN_CODE = false;
                QUERY_STRING = "HEAD / HTTP/1.0\r\n\r\n";
                RESPONSE_STRING = "Lotus";
        }
}
