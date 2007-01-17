package org.opennms.web.map.config;

import java.util.ArrayList;
import java.util.List;

public class ContextMenu {
	List<CMEntry> entries = new ArrayList<CMEntry>();

	public void addEntry(String command, String link, String params){
		CMEntry entry = new CMEntry(command,link,params);
		entries.add(entry);
	}
	
	public class CMEntry{
		public String command;
		public String link;
		public String params;
		CMEntry(String command, String link, String params) {
			super();
			this.command = command;
			this.link = link;
			this.params = params;
		}
		
	}
	
	public List<CMEntry> getEntries(){
		return entries;
	}
}
