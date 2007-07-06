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
		public String getCommand() {
			return command;
		}
		public void setCommand(String command) {
			this.command = command;
		}
		public String getLink() {
			return link;
		}
		public void setLink(String link) {
			this.link = link;
		}
		public String getParams() {
			return params;
		}
		public void setParams(String params) {
			this.params = params;
		}
		
	}
	
	public List<CMEntry> getEntries(){
		return entries;
	}
}
