/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
