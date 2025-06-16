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
package org.opennms.netmgt.provision.service.requisition;

import java.io.File;
import java.util.Map;

import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.provision.persist.AbstractRequisitionProvider;
import org.opennms.netmgt.provision.persist.requisition.Requisition;

public class FileRequisitionProvider extends AbstractRequisitionProvider<FileRequisitionRequest> {

    public static final String TYPE_NAME = "file";

    public FileRequisitionProvider() {
        super(FileRequisitionRequest.class);
    }

    @Override
    public String getType() {
        return TYPE_NAME;
    }

    @Override
    public FileRequisitionRequest getRequest(Map<String, String> parameters) {
        final FileRequisitionRequest request = new FileRequisitionRequest();
        request.setPath(parameters.get("path"));
        if (request.getPath() == null || request.getPath().isEmpty()) {
            throw new IllegalArgumentException("Path arguments is required.");
        }
        return request;
    }

    @Override
    public Requisition getRequisitionFor(FileRequisitionRequest request) {
        final File file = new File(request.getPath());
        return JaxbUtils.unmarshal(Requisition.class, file);
    }

}
