/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.karaf.shell.util;

import static org.apache.karaf.shell.commands.ansi.SimpleAnsi.COLOR_DEFAULT;
import static org.apache.karaf.shell.commands.ansi.SimpleAnsi.COLOR_RED;
import static org.apache.karaf.shell.commands.ansi.SimpleAnsi.INTENSITY_BOLD;
import static org.apache.karaf.shell.commands.ansi.SimpleAnsi.INTENSITY_NORMAL;

import java.util.Arrays;

import javax.security.auth.Subject;

import org.apache.felix.service.command.CommandSession;
import org.apache.karaf.jaas.boot.principal.UserPrincipal;
import org.apache.karaf.shell.commands.CommandException;
import org.apache.karaf.shell.console.CloseShellException;
import org.apache.karaf.shell.console.SessionProperties;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.startlevel.BundleStartLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.karaf.util.jaas.JaasHelper;

@Deprecated
public class ShellUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(ShellUtil.class);

    public static String getBundleName(Bundle bundle) {
        if (bundle != null) {
            String name = bundle.getHeaders().get(Constants.BUNDLE_NAME);
            return (name == null)
                    ? "Bundle " + bundle.getBundleId()
                    : name + " (" + bundle.getBundleId() + ")";
        }
        return "[STALE BUNDLE]";
    }

    public static String getUnderlineString(String s) {
        StringBuilder sb = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            sb.append('-');
        }
        return sb.toString();
    }

    public static String getValueString(Object obj) {
        if (obj == null) {
            return "null";
        } else if (obj instanceof boolean[]) {
            return Arrays.toString((boolean[]) obj);
        } else if (obj instanceof byte[]) {
            return Arrays.toString((byte[]) obj);
        } else if (obj instanceof char[]) {
            return Arrays.toString((char[]) obj);
        } else if (obj instanceof double[]) {
            return Arrays.toString((double[]) obj);
        } else if (obj instanceof float[]) {
            return Arrays.toString((float[]) obj);
        } else if (obj instanceof int[]) {
            return Arrays.toString((int[]) obj);
        } else if (obj instanceof long[]) {
            return Arrays.toString((long[]) obj);
        } else if (obj instanceof short[]) {
            return Arrays.toString((short[]) obj);
        } else if (obj.getClass().isArray()) {
            Object[] array = (Object[]) obj;
            StringBuilder sb = new StringBuilder();
            sb.append("[");
            for (int i = 0; i < array.length; i++) {
                if (i != 0) {
                    sb.append(", ");
                }
                sb.append(getValueString(array[i]));
            }
            sb.append("]");
            return sb.toString();
        } else {
            return obj.toString();
        }
    }

    /**
     * Check if a bundle is a system bundle (start level minor than 50)
     *
     * @param bundleContext The bundle context.
     * @param bundle The bundle to check.
     * @return True if the bundle has start level minor than 50, false else.
     */
    public static boolean isASystemBundle(BundleContext bundleContext, Bundle bundle) {
        int level = bundle.adapt(BundleStartLevel.class).getStartLevel();
        int sbsl = 49;
        final String sbslProp = bundleContext.getProperty("karaf.systemBundlesStartLevel");
        if (sbslProp != null) {
            try {
                sbsl = Integer.parseInt(sbslProp);
            } catch (Exception ignore) {
                // ignore
            }
        }
        return level <= sbsl;
    }

    public static boolean getBoolean(CommandSession session, String name) {
        Object s = session.get(name);
        if (s == null) {
            s = System.getProperty(name);
        }
        if (s == null) {
            return false;
        }
        if (s instanceof Boolean) {
            return (Boolean) s;
        }
        return Boolean.parseBoolean(s.toString());
    }

    public static void logException(CommandSession session, Throwable t) {
        try {
            boolean isCommandNotFound = "org.apache.felix.gogo.runtime.CommandNotFoundException".equals(t.getClass().getName());
            if (isCommandNotFound) {
                LOGGER.debug("Unknown command entered", t);
            } else if (t instanceof CommandException) {
                LOGGER.debug("Command exception (Undefined option, ...)", t);
            } else if (!(t instanceof CloseShellException)) {
                LOGGER.error("Exception caught while executing command", t);
            }
            session.put(SessionProperties.LAST_EXCEPTION, t);
            if (t instanceof CommandException) {
                session.getConsole().println(((CommandException) t).getNiceHelp());
            } else if (isCommandNotFound) {
                String str = COLOR_RED + "Command not found: "
                         + INTENSITY_BOLD + t.getClass().getMethod("getCommand").invoke(t) + INTENSITY_NORMAL
                         + COLOR_DEFAULT;
                session.getConsole().println(str);
            }
            if (getBoolean(session, SessionProperties.PRINT_STACK_TRACES)) {
                session.getConsole().print(COLOR_RED);
                t.printStackTrace(session.getConsole());
                session.getConsole().print(COLOR_DEFAULT);
            } else if (!(t instanceof CloseShellException) && !(t instanceof CommandException) && !isCommandNotFound) {
                session.getConsole().print(COLOR_RED);
                session.getConsole().println("Error executing command: "
                        + (t.getMessage() != null ? t.getMessage() : t.getClass().getName()));
                session.getConsole().print(COLOR_DEFAULT);
            }
        } catch (Exception ignore) {
            // ignore
        }
    }

    public static String getCurrentUserName() {
        // OPENNMS: Subject.getSubject() always throws on JDK 23+
        final Subject subject = JaasHelper.currentSubject();
        if (subject != null && subject.getPrincipals(UserPrincipal.class).iterator().hasNext()) {
            return subject.getPrincipals(UserPrincipal.class).iterator().next().getName();
        } else {
            return null;
        }
    }

}
