/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.karaf.jaas.boot.principal;

import javax.security.auth.Subject;

/**
 * OPENNMS: the JAAS subject Karaf's shell runs as, inherited by the threads it starts.
 *
 * Karaf used to bind the subject to an {@code AccessControlContext}, which new threads
 * inherit, and read it back with {@code Subject.getSubject()}. That method always throws
 * on JDK 23+, and its replacement {@code Subject.current()} is backed by a
 * {@code ScopedValue} that plain threads (the console session thread, gogo's pipe
 * threads) do not inherit. This holder restores the inheritance.
 *
 * It lives in jaas.boot because that jar is exported once, by the system bundle, while
 * every Karaf bundle carries its own private copy of {@code JaasHelper}; a static there
 * would not be shared between them.
 */
public final class InheritableSubject {

    private static final InheritableThreadLocal<Subject> CURRENT = new InheritableThreadLocal<>();

    private InheritableSubject() {
    }

    public static Subject get() {
        return CURRENT.get();
    }

    /**
     * Makes {@code subject} the current one, {@code null} clearing it.
     *
     * @return the previous subject, to be passed back in once the action is done
     */
    public static Subject swap(final Subject subject) {
        final Subject previous = CURRENT.get();
        if (subject == null) {
            CURRENT.remove();
        } else {
            CURRENT.set(subject);
        }
        return previous;
    }
}
