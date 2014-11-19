/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.web.map.config;

import java.util.ArrayList;
import java.util.List;


/**
 * <p>ContextMenu class.</p>
 *
 * @author <a href="mailto:antonio@opennms.it">Antonio Russo</a>
 * @version $Id: $
 * @since 1.8.1
 */
public class ContextMenu {
	List<CMEntry> entries = new ArrayList<CMEntry>();

	/**
	 * <p>addEntry</p>
	 *
	 * @param command a {@link java.lang.String} object.
	 * @param link a {@link java.lang.String} object.
	 * @param params a {@link java.lang.String} object.
	 */
	public void addEntry(String command, String link, String params){
		CMEntry entry = new CMEntry(command,link,params);
		entries.add(entry);
	}
	
	public static class CMEntry{
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
	
	/**
	 * <p>Getter for the field <code>entries</code>.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	public List<CMEntry> getEntries(){
		return entries;
	}
}
