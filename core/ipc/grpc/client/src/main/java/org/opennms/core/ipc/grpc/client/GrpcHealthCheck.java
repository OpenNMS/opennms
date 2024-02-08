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
package org.opennms.core.ipc.grpc.client;

import org.opennms.core.health.api.Context;
import org.opennms.core.health.api.HealthCheck;
import org.opennms.core.health.api.Response;
import org.opennms.core.health.api.Status;

import io.grpc.ConnectivityState;

import java.util.Arrays;
import java.util.List;

import static org.opennms.core.health.api.HealthCheckConstants.BROKER;

public class GrpcHealthCheck implements HealthCheck {

    private final MinionGrpcClient minionGrpcClient;

    public GrpcHealthCheck(MinionGrpcClient minionGrpcClient) {
        this.minionGrpcClient = minionGrpcClient;
    }

    @Override
    public String getDescription() {
        return "Connecting to gRPC IPC Server";
    }

    @Override
    public List<String> getTags() {
        return Arrays.asList(BROKER);
    }

    @Override
    public Response perform(Context context) throws Exception {
        if (minionGrpcClient.getChannelState().equals(ConnectivityState.READY)) {
            return new Response(Status.Success);
        } else if (minionGrpcClient.getChannelState().equals(ConnectivityState.CONNECTING)) {
            return new Response(Status.Starting);
        }
        return new Response(Status.Failure);
    }
}
