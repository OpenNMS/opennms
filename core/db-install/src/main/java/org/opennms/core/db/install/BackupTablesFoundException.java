/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
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

package org.opennms.core.db.install;

import java.util.Collections;
import java.util.List;

import org.springframework.util.StringUtils;

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
    @Override
	public String toString() {
		StringBuffer m = new StringBuffer(getMessage());
		m.append("\nBackup tables: \n\t");
                m.append(StringUtils.collectionToDelimitedString(m_oldTables, "\n\t"));
		return m.toString();
	}
}
