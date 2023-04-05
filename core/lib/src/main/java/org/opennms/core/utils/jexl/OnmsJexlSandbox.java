/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2003-2021 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 2021 The OpenNMS Group, Inc.
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

package org.opennms.core.utils.jexl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * This class is taken and adapted from the jexl3 codebase. It allows default blacklisting of classes and methods.
 */
public final class OnmsJexlSandbox {
    private final Map<String, Permissions> sandbox;
    private final boolean white;

    public OnmsJexlSandbox() {
        this(false, new HashMap<String, Permissions>());
    }

    public OnmsJexlSandbox(boolean wb) {
        this(wb, new HashMap<String, Permissions>());
    }

    protected OnmsJexlSandbox(Map<String, Permissions> map) {
        this(true, map);
    }

    protected OnmsJexlSandbox(boolean wb, Map<String, Permissions> map) {
        white = wb;
        sandbox = map;
    }

    public OnmsJexlSandbox copy() {
        Map<String, Permissions> map = new HashMap<String, Permissions>();
        for (Map.Entry<String, Permissions> entry : sandbox.entrySet()) {
            map.put(entry.getKey(), entry.getValue().copy());
        }
        return new OnmsJexlSandbox(white, map);
    }

    public String read(Class<?> clazz, String name) {
        return read(clazz.getName(), name);
    }

    public String read(String clazz, String name) {
        Permissions permissions = sandbox.get(clazz);
        if (permissions == null) {
            return white? name : null;
        } else {
            return permissions.read().get(name);
        }
    }

    public String write(Class<?> clazz, String name) {
        return write(clazz.getName(), name);
    }

    public String write(String clazz, String name) {
        Permissions permissions = sandbox.get(clazz);
        if (permissions == null) {
            return white ? name : null;
        } else {
            return permissions.write().get(name);
        }
    }

    public String execute(Class<?> clazz, String name) {
        return execute(clazz.getName(), name);
    }

    public String execute(String clazz, String name) {
        Permissions permissions = sandbox.get(clazz);
        if (permissions == null) {
            return white ? name : null;
        } else {
            return permissions.execute().get(name);
        }
    }

    public abstract static class Names {

        public abstract boolean add(String name);

        public boolean alias(String name, String alias) {
            return false;
        }

        public String get(String name) {
            return name;
        }

        protected Names copy() {
            return this;
        }
    }

    private static final Names WHITE_NAMES = new Names() {
        @Override
        public boolean add(String name) {
            return false;
        }

        @Override
        protected Names copy() {
            return this;
        }
    };

    public static final class WhiteSet extends Names {
        private Map<String, String> names = null;

        @Override
        protected Names copy() {
            WhiteSet copy = new WhiteSet();
            copy.names = names == null ? null : new HashMap<String, String>(names);
            return copy;
        }

        @Override
        public boolean add(String name) {
            if (names == null) {
                names = new HashMap<String, String>();
            }
            return names.put(name, name) == null;
        }

        @Override
        public boolean alias(String name, String alias) {
            if (names == null) {
                names = new HashMap<String, String>();
            }
            return names.put(alias, name) == null;
        }

        @Override
        public String get(String name) {
            if (names == null) {
                return name;
            } else {
                return names.get(name);
            }
        }
    }

    public static final class BlackSet extends Names {
        private Set<String> names = null;

        @Override
        protected Names copy() {
            BlackSet copy = new BlackSet();
            copy.names = names == null ? null : new HashSet<String>(names);
            return copy;
        }

        @Override
        public boolean add(String name) {
            if (names == null) {
                names = new HashSet<String>();
            }
            return names.add(name);
        }

        @Override
        public String get(String name) {
            return names != null && !names.contains(name) ? name : null;
        }
    }

    public static final class Permissions {

        private final Names read;

        private final Names write;

        private final Names execute;

        Permissions(boolean readFlag, boolean writeFlag, boolean executeFlag) {
            this(readFlag ? new WhiteSet() : new BlackSet(),
                    writeFlag ? new WhiteSet() : new BlackSet(),
                    executeFlag ? new WhiteSet() : new BlackSet());
        }

        Permissions(Names nread, Names nwrite, Names nexecute) {
            this.read = nread != null ? nread : WHITE_NAMES;
            this.write = nwrite != null ? nwrite : WHITE_NAMES;
            this.execute = nexecute != null ? nexecute : WHITE_NAMES;
        }

        Permissions copy() {
            return new Permissions(read.copy(), write.copy(), execute.copy());
        }

        public Permissions read(String... pnames) {
            for (String pname : pnames) {
                read.add(pname);
            }
            return this;
        }

        public Permissions write(String... pnames) {
            for (String pname : pnames) {
                write.add(pname);
            }
            return this;
        }

        public Permissions execute(String... mnames) {
            for (String mname : mnames) {
                execute.add(mname);
            }
            return this;
        }

        public Names read() {
            return read;
        }

        public Names write() {
            return write;
        }

        public Names execute() {
            return execute;
        }
    }

    private static final Permissions ALL_WHITE = new Permissions(WHITE_NAMES, WHITE_NAMES, WHITE_NAMES);

    public Permissions permissions(String clazz, boolean readFlag, boolean writeFlag, boolean executeFlag) {
        Permissions box = new Permissions(readFlag, writeFlag, executeFlag);
        sandbox.put(clazz, box);
        return box;
    }

    public Permissions white(String clazz) {
        return permissions(clazz, true, true, true);
    }

    public Permissions black(String clazz) {
        return permissions(clazz, false, false, false);
    }

    public Permissions get(String clazz) {
        Permissions permissions = sandbox.get(clazz);
        if (permissions == null) {
            return ALL_WHITE;
        } else {
            return permissions;
        }
    }
}
