//
// Copyright (C) 2002 Oculan Corp.  All rights reserved.
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
//
package org.opennms.netmgt.vulnscand;

import org.opennms.netmgt.eventd.db.Constants;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;

/**
* Class that holds the return values
* when parsing a port/protocol field from
* Nessus.
*/
public class PortValues
{
	int port;
	String protocol;

	public PortValues()
	{
		port = -1;
		protocol = null;
	}

	public void useDefaults()
	{
		protocol = "unknown";
	}

	public boolean isValid()
	{
		if (
			(protocol != null)
		)
			return true;
		else
			return false;
	}

	public String toString()
	{
		return (
			"port: " + port +
			"\nprotocol: " + protocol + "\n"
		);
	}
}
