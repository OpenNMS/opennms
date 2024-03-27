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
package org.opennms.netmgt.flows.classification;

import org.opennms.netmgt.flows.classification.persistence.api.Protocol;

public class ClassificationRequestBuilder {

    private final ClassificationRequest request = new ClassificationRequest();

    public ClassificationRequestBuilder withSrcAddress(String srcAddress) {
        request.setSrcAddress(srcAddress);
        return this;
    }

    public ClassificationRequestBuilder withSrcPort(final Integer srcPort) {
        request.setSrcPort(srcPort);
        return this;
    }

    public ClassificationRequestBuilder withDstPort(final Integer dstPort) {
        request.setDstPort(dstPort);
        return this;
    }

    public ClassificationRequestBuilder withDstAddress(String dstAddress) {
        request.setDstAddress(dstAddress);
        return this;
    }

    public ClassificationRequestBuilder withProtocol(Protocol protocol) {
        request.setProtocol(protocol);
        return this;
    }

    public ClassificationRequestBuilder withExporterAddress(String exporterAddress) {
        request.setExporterAddress(exporterAddress);
        return this;
    }

    public ClassificationRequestBuilder withLocation(String location) {
        request.setLocation(location);
        return this;
    }

    public ClassificationRequest build() {
        return request;
    }
}
