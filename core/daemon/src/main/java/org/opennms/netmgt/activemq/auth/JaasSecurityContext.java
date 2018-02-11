/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.activemq.auth;

import java.security.Principal;
import java.util.Set;
import java.util.stream.Collectors;

import javax.security.auth.Subject;

import org.apache.activemq.jaas.GroupPrincipal;
import org.apache.activemq.security.SecurityContext;

public class JaasSecurityContext extends SecurityContext {

    private final Set<Principal> principals;

    public JaasSecurityContext(String userName, Subject subject) {
        super(userName);
        // Map all of the subject's principals to GroupPrincipals with the same name
        principals = subject.getPrincipals().stream()
                .map(Principal::getName)
                .map(GroupPrincipal::new)
                .collect(Collectors.toSet());
    }

    @Override
    public Set<Principal> getPrincipals() {
        return principals;
    }
}
