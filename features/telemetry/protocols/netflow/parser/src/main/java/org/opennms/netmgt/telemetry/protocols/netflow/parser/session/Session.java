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
package org.opennms.netmgt.telemetry.protocols.netflow.parser.session;

import java.net.InetAddress;
import java.util.Collection;
import java.util.List;

import org.opennms.netmgt.telemetry.protocols.netflow.parser.MissingTemplateException;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.Value;

public interface Session {

    interface Resolver {
        Template lookupTemplate(final int templateId) throws MissingTemplateException;
        List<Value<?>> lookupOptions(final List<Value<?>> values);
    }

    void addTemplate(final long observationDomainId, final Template template);

    void removeTemplate(final long observationDomainId, final int templateId);

    void removeAllTemplate(final long observationDomainId, final Template.Type type);

    void addOptions(final long observationDomainId,
                    final int templateId,
                    final Collection<Value<?>> scopes,
                    final List<Value<?>> values);

    Resolver getResolver(final long observationDomainId);

    InetAddress getRemoteAddress();

    boolean verifySequenceNumber(final long observationDomainId,
                                 final long sequenceNumber);
}
