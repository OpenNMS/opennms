/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.karaf.jaas.command;

import java.security.Principal;
import java.util.Set;

import javax.security.auth.Subject;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.apache.karaf.shell.support.table.Row;
import org.apache.karaf.shell.support.table.ShellTable;
import org.apache.karaf.util.jaas.JaasHelper;

@Command(scope = "jaas", name = "whoami", description = "List currently active principals according to JAAS.")
@Service
public class WhoamiCommand implements Action {
    private static final String USER_CLASS = "org.apache.karaf.jaas.boot.principal.UserPrincipal";
    private static final String GROUP_CLASS = "org.apache.karaf.jaas.boot.principal.GroupPrincipal";
    private static final String ROLE_CLASS = "org.apache.karaf.jaas.boot.principal.RolePrincipal";
    private static final String ALL_CLASS = "java.security.Principal";

	@Option(name = "-g", aliases = { "--groups" }, description = "Show groups instead of user.")
	boolean groups = false;
	
	@Option(name = "-r", aliases = { "--roles" }, description = "Show roles instead of user.")
	boolean roles = false;
	
    @Option(name = "-a", aliases = { "--all" }, description = "Show all JAAS principals regardless of type.")
	boolean all = false;

    @Option(name = "--no-format", description = "Disable table rendered output.")
	boolean noFormat = false;

    @Override
    public Object execute() throws Exception {
	ShellTable table = new ShellTable();

	// Get the currently-active JAAS Subject.
	// OPENNMS: Subject.getSubject() always throws on JDK 23+
	Subject subj = JaasHelper.currentSubject();

	String classString = USER_CLASS;
	if (groups) {
	    classString = GROUP_CLASS;
	} else if (roles) {
	    classString = ROLE_CLASS;
	} else if (all) {
	    classString = ALL_CLASS;
	}

	Class c = Class.forName(classString);

	Set<Principal> principals = subj.getPrincipals(c);

	
	table.column("Name");
	if (all) {
	    table.column("Class");
	}

	for (Principal p : principals) {
	    Row row = table.addRow();
	    row.addContent(p.getName());
	    if (all) {
		row.addContent(p.getClass().getCanonicalName());
	    }
	}

	table.print(System.out, !noFormat);

	return null;
    }
}
