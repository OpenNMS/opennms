//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2007 Jul 23: Use Java 5 generics to eliminate warnings. - dj@opennms.org
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
//      http://www.opennms.com/
//
/**
 * <p>TmpInterface class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.6.12
 */
package org.opennms.secret.web;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
public class TmpInterface {
	
	static String[] ifDSNames =  { "ifInOctets", "ifOutOctets", "ifInErrors", "ifInDiscards" };
	static String[] ifDSLabels = { "In Octets", "Out Octets", "In Errors", "In Discards" };
	static String[] svcNames = { "ICMP", "HTTP", "DNS", "SSH" };

	String m_label;
	/**
	 * <p>Constructor for TmpInterface.</p>
	 *
	 * @param label a {@link java.lang.String} object.
	 */
	public TmpInterface(String label) {
		m_label = label;
	}
	/**
	 * <p>getIfDescr</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getIfDescr() {
		return m_label;
	}
	/**
	 * <p>getServices</p>
	 *
	 * @return a {@link java.util.Collection} object.
	 */
	public Collection<TmpService> getServices() {
		List<TmpService> list = new LinkedList<TmpService>();
		for (int i = 0; i < svcNames.length; i++) {
			String svcName = svcNames[i];
			list.add(new TmpService(svcName));
		}
		return list;
	}
	/**
	 * <p>getDataSources</p>
	 *
	 * @return a {@link java.util.Collection} object.
	 */
	public Collection<TmpDataSource> getDataSources() {
		List<TmpDataSource> list = new LinkedList<TmpDataSource>();
		for (int i = 0; i < ifDSNames.length; i++) {
			String name = ifDSNames[i];
			String label = ifDSLabels[i];
			list.add(new TmpDataSource(name, label));
		}
		return list;
	}
	
}
