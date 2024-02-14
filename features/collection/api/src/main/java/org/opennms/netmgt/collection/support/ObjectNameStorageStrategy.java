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
package org.opennms.netmgt.collection.support;

import javax.management.ObjectName;

import org.apache.commons.jexl2.JexlContext;
import org.opennms.netmgt.collection.api.CollectionResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ObjectNameStorageStrategy extends JexlIndexStorageStrategy {

    private static final Logger LOG = LoggerFactory.getLogger(ObjectNameStorageStrategy.class);
    private static final String QUOTE = "\"";

    public ObjectNameStorageStrategy() {
        super();
        jexlEngine.white(ObjectName.class.getName());
    }

    @Override
    public void updateContext(JexlContext context, CollectionResource resource) throws IllegalArgumentException {
        try {
            ObjectName oname = new ObjectName(resource.getUnmodifiedInstance());
            context.set("ObjectName", oname);
            context.set("domain", oname.getDomain() == null ? "" : oname.getDomain());
            oname.getKeyPropertyList().entrySet().forEach((entry) -> {
                final String value = entry.getValue();
                if (value.startsWith(QUOTE) && value.endsWith(QUOTE)) {
                    context.set(entry.getKey(), ObjectName.unquote(entry.getValue()));
                } else {
                    context.set(entry.getKey(), entry.getValue());
                }
            });
        } catch (javax.management.MalformedObjectNameException e) {
            final String msg = "Malformed ObjectName: " + resource.getUnmodifiedInstance();
            LOG.error("getResourceNameFromIndex(): {}", msg, e);
            throw new IllegalArgumentException(msg);
        }
    }
}
