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
* when parsing a description field from
* Nessus.
*/
public class DescrValues
{
	String descr;
	String cveEntry;
	int severity;

	public DescrValues()
	{
		descr = null;
		cveEntry = null;
		severity = -1;
	}

	public void useDefaults()
	{
		descr = "";
		severity = Constants.SEV_INDETERMINATE;
	}

	public boolean isValid()
	{
		if (
			(descr != null) &&
			(severity > 0)
		)
			return true;
		else
			return false;
	}

	public String toString()
	{
		return (
			"descr: " + descr +
			"\ncveEntry: " + cveEntry +
			"\nseverity: " + severity + "\n"
		);
	}
}


