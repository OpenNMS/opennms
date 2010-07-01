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
package org.opennms.netmgt.dao.db;

import java.util.Collections;
import java.util.List;

import org.springframework.util.StringUtils;

/**
 * <p>BackupTablesFoundException class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class BackupTablesFoundException extends Exception {
    /**
     * 
     */
    private static final long serialVersionUID = -3888915116741506735L;

    private List<String> m_oldTables;

	private static final String s_ourMessage =
		"One or more backup tables from a previous "
			+ "install still exists--aborting installation.  "
			+ "You either need to remove them or rename them "
			+ "so they do not contain the string '_old_'.";
	
	/**
	 * <p>Constructor for BackupTablesFoundException.</p>
	 *
	 * @param oldTables a {@link java.util.List} object.
	 */
	public BackupTablesFoundException(List<String> oldTables) {
		super(s_ourMessage);
		m_oldTables = oldTables;
	}
	
	/**
	 * <p>getOldTables</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	public List<String> getOldTables() {
		return Collections.unmodifiableList(m_oldTables);
	}
	
	/**
	 * <p>toString</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String toString() {
		StringBuffer m = new StringBuffer(getMessage());
		m.append("\nBackup tables: \n\t");
                m.append(StringUtils.collectionToDelimitedString(m_oldTables, "\n\t"));
		return m.toString();
	}
}
