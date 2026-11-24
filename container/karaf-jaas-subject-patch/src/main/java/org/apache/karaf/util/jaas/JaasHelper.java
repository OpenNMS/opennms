/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */
package org.apache.karaf.util.jaas;

import java.security.Permission;
import java.security.Principal;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.security.ProtectionDomain;
import java.util.Set;
import java.util.concurrent.CompletionException;

import javax.security.auth.Subject;
import javax.security.auth.SubjectDomainCombiner;

import org.apache.karaf.jaas.boot.principal.InheritableSubject;
import org.apache.karaf.jaas.boot.principal.RolePrincipal;

public class JaasHelper {

    private static final String ROLE_WILDCARD = "*";

    public static boolean currentUserHasRole(String requestedRole) {
        if (ROLE_WILDCARD.equals(requestedRole)) {
            return true;
        }

        Subject subject = currentSubject();
        if (subject == null) {
            return false;
        }

        return currentUserHasRole(subject.getPrincipals(), requestedRole);
    }

    public static boolean currentUserHasRole(Set<Principal> principals, String requestedRole) {
        if (ROLE_WILDCARD.equals(requestedRole)) {
            return true;
        }

        String clazz;
        String role;
        int index = requestedRole.indexOf(':');
        if (index > 0) {
            clazz = requestedRole.substring(0, index);
            role = requestedRole.substring(index + 1);
            
            for (Principal p : principals) {
                if (clazz.equals(p.getClass().getName()) && role.equals(p.getName())) {
                    return true;
                }
            }
        } else {
            role = requestedRole;
            
            for (Principal p : principals) {
                if (RolePrincipal.class.isAssignableFrom(p.getClass()) && role.equals(p.getName())) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * OPENNMS: replacement for {@code Subject.getSubject(AccessController.getContext())},
     * which always throws UnsupportedOperationException on JDK 23+. A subject bound with
     * Subject.callAs/doAs (e.g. by the JDK's JMX connector) wins; otherwise it is the one
     * doAs() below bound on this thread or on the thread that started it.
     */
    public static Subject currentSubject() {
        final Subject subject = Subject.current();
        return subject != null ? subject : InheritableSubject.get();
    }

    public static void runAs(final Subject subject,
                             final Runnable action) {
        if (action == null) {
            throw new NullPointerException();
        }
        doAs(subject, (PrivilegedAction<Object>)(() -> { action.run(); return null; } ));
    }

    public static <T> T doAs(final Subject subject,
                             final PrivilegedAction<T> action) {
        if (action == null) {
            throw new NullPointerException();
        }
        // OPENNMS: an AccessControlContext carries no subject on JDK 23+ (see currentSubject())
        final Subject previous = InheritableSubject.swap(subject);
        try {
            return Subject.callAs(subject, action::run);
        } catch (CompletionException e) {
            // callAs wraps whatever the action throws; PrivilegedAction only throws unchecked
            throw unchecked(e);
        } finally {
            InheritableSubject.swap(previous);
        }
    }

    public static <T> T doAs(final Subject subject,
                             final PrivilegedExceptionAction<T> action) throws PrivilegedActionException {
        if (action == null) {
            throw new NullPointerException();
        }
        // OPENNMS: an AccessControlContext carries no subject on JDK 23+ (see currentSubject())
        final Subject previous = InheritableSubject.swap(subject);
        try {
            return Subject.callAs(subject, action::run);
        } catch (CompletionException e) {
            // same contract as AccessController.doPrivileged: checked exceptions wrapped
            // in PrivilegedActionException, unchecked ones thrown as they are
            if (e.getCause() instanceof Exception && !(e.getCause() instanceof RuntimeException)) {
                throw new PrivilegedActionException((Exception) e.getCause());
            }
            throw unchecked(e);
        } finally {
            InheritableSubject.swap(previous);
        }
    }

    private static RuntimeException unchecked(final CompletionException e) {
        final Throwable cause = e.getCause();
        if (cause instanceof RuntimeException) {
            return (RuntimeException) cause;
        }
        if (cause instanceof Error) {
            throw (Error) cause;
        }
        return e;
    }

    public static class OsgiSubjectDomainCombiner extends SubjectDomainCombiner {

        private final Subject subject;

        public OsgiSubjectDomainCombiner(Subject subject) {
            super(subject);
            this.subject = subject;
        }

        public ProtectionDomain[] combine(ProtectionDomain[] currentDomains,
                                          ProtectionDomain[] assignedDomains) {
            int cLen = (currentDomains == null ? 0 : currentDomains.length);
            int aLen = (assignedDomains == null ? 0 : assignedDomains.length);
            ProtectionDomain[] newDomains = new ProtectionDomain[cLen + aLen];
            Principal[] principals = subject.getPrincipals().toArray(new Principal[0]);
            for (int i = 0; i < cLen; i++) {
                newDomains[i] = new DelegatingProtectionDomain(currentDomains[i], principals);
            }
            if (assignedDomains != null) {
                System.arraycopy(assignedDomains, 0, newDomains, cLen, aLen);
            }
            return optimize(newDomains);
        }

        private ProtectionDomain[] optimize(ProtectionDomain[] domains) {
            if (domains == null || domains.length == 0) {
                return null;
            }
            ProtectionDomain[] optimized = new ProtectionDomain[domains.length];
            ProtectionDomain pd;
            int num = 0;
            for (ProtectionDomain domain : domains) {
                if ((pd = domain) != null) {
                    boolean found = false;
                    for (int j = 0; j < num && !found; j++) {
                        found = (optimized[j] == pd);
                    }
                    if (!found) {
                        optimized[num++] = pd;
                    }
                }
            }
            if (num > 0 && num < domains.length) {
                ProtectionDomain[] downSize = new ProtectionDomain[num];
                System.arraycopy(optimized, 0, downSize, 0, downSize.length);
                optimized = downSize;
            }
            return ((num == 0 || optimized.length == 0) ? null : optimized);
        }
    }

    public static class DelegatingProtectionDomain extends ProtectionDomain {

        private final ProtectionDomain delegate;

        DelegatingProtectionDomain(ProtectionDomain delegate, Principal[] principals) {
            super(delegate.getCodeSource(), delegate.getPermissions(), delegate.getClassLoader(), principals);
            this.delegate = delegate;
        }

        @Override
        public boolean implies(Permission permission) {
            return delegate.implies(permission);
        }

    }
}
