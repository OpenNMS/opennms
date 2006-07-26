package org.opennms.install;

import java.util.Collections;
import java.util.List;

public class BackupTablesFoundException extends Exception {
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
		m.append(Installer.join("\n\t", (String[]) m_oldTables.toArray(new String[0])));
		return m.toString();
	}
}
