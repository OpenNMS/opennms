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
package org.opennms.features.mibcompiler.rest.internal;

import org.opennms.features.mibcompiler.api.MibParser;
import org.opennms.features.mibcompiler.rest.MibCompilerRestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;

public class MibCompilerRestServiceImpl implements MibCompilerRestService {

    private static final Logger LOG = LoggerFactory.getLogger(MibCompilerRestServiceImpl.class);

    private final MibParser mibParser;

    public MibCompilerRestServiceImpl(MibParser mibParser) {
        this.mibParser = mibParser;
    }

    @Override
    public Response uploadMib(byte[] mibContent, String filename) {
        // TODO: implement MIB upload to pending directory
        return Response.status(Response.Status.SERVICE_UNAVAILABLE)
                .entity("{\"error\": \"Not yet implemented\"}")
                .build();
    }

    @Override
    public Response compileMib(String name) {
        // TODO: implement MIB compilation
        return Response.status(Response.Status.SERVICE_UNAVAILABLE)
                .entity("{\"error\": \"Not yet implemented\"}")
                .build();
    }
}
