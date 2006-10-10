package org.opennms.netmgt.dao.db;

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
	
	public BackupTablesFoundException(List<String> oldTables) {
		super(s_ourMessage);
		m_oldTables = oldTables;
	}
	
	public List<String> getOldTables() {
		return Collections.unmodifiableList(m_oldTables);
	}
	
	public String toString() {
		StringBuffer m = new StringBuffer(getMessage());
		m.append("\nBackup tables: \n\t");
                m.append(StringUtils.collectionToDelimitedString(m_oldTables, "\n\t"));
		return m.toString();
	}
}
