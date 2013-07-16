/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.scriptd.helper;

import java.util.regex.Pattern;

import org.opennms.netmgt.xml.event.Event;

public class UeiEventMatch implements EventMatch {

	private String ueimatch;
	
	public UeiEventMatch() {
		super();
		this.ueimatch = null;
	}
	
	
	public UeiEventMatch(String ueimatch) {
		super();
		this.ueimatch = ueimatch;
	}

	
        @Override
	public boolean match(Event event) {
		if (event == null) return false;
		if (event.getUei() == null ) return false;
		if (this.ueimatch == null) return false;
		if (this.ueimatch.startsWith("~"))
			return rematch(event.getUei(), this.ueimatch.substring(1));
		else
			return (event.getUei().equals(this.ueimatch));
		
	}

	private boolean rematch(String text, String regex) {
		Pattern p = Pattern.compile(regex);
		return p.matcher(text).matches();
	}
}
