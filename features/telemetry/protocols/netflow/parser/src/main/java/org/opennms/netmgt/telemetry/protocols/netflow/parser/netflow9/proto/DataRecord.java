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
package org.opennms.netmgt.telemetry.protocols.netflow.parser.netflow9.proto;

import static org.opennms.netmgt.telemetry.listeners.utils.BufferUtils.slice;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.opennms.netmgt.telemetry.protocols.netflow.parser.InvalidPacketException;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.MissingTemplateException;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.Value;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.session.Field;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.session.Session;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.session.Template;

import com.google.common.base.MoreObjects;

import io.netty.buffer.ByteBuf;

public final class DataRecord implements Record {

    /*
     +--------------------------------------------------+
     | Field Value                                      |
     +--------------------------------------------------+
     | Field Value                                      |
     +--------------------------------------------------+
      ...
     +--------------------------------------------------+
     | Field Value                                      |
     +--------------------------------------------------+
    */

    public final DataSet set;  // Enclosing set

    public final Template template;

    public final List<Value<?>> scopes;
    public final List<Value<?>> fields;
    public final List<Value<?>> options;

    public DataRecord(final DataSet set,
                      final Session.Resolver resolver,
                      final Template template,
                      final ByteBuf buffer) throws InvalidPacketException, MissingTemplateException {
        this.set = Objects.requireNonNull(set);

        this.template = Objects.requireNonNull(template);

        final List<Value<?>> scopes = new ArrayList<>(this.template.scopes.size());
        for (final Field scope : template.scopes) {
            scopes.add(scope.parse(resolver, slice(buffer, scope.length())));
        }

        final List<Value<?>> fields = new ArrayList<>(this.template.fields.size());
        for (final Field field : template.fields) {
            fields.add(field.parse(resolver, slice(buffer, field.length())));
        }

        this.scopes = Collections.unmodifiableList(scopes);
        this.fields = Collections.unmodifiableList(fields);

        // Expand the data record by appending values from
        this.options = resolver.lookupOptions(ScopeFieldSpecifier.buildScopeValues(this));
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("scopes", this.scopes)
                .add("fields", this.fields)
                .add("options", this.options)
                .toString();
    }
}
