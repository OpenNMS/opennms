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
package org.opennms.core.health.api;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A health check that does no action by its own but relies on being informed about healthiness.
 */
public class DefaultPassiveHealthCheck implements CachingHealthCheck {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultPassiveHealthCheck.class);

    private final String description;
    private final List<String> tags;

    private Response cachedResponse = null;
    private Instant cachedResponseTimestamp = null;
    private boolean cachedExpires = true;

    public DefaultPassiveHealthCheck(
            String description,
            List<String> tags,
            Status initialStatus
    ) {
        this.description = description;
        this.tags = tags;
        setResponse(new Response(initialStatus));
    }

    @Override
    public synchronized void setResponse(Response response) {
        setResponse(response, true);
    }

    public synchronized void setResponse(Response response, boolean expires) {
        LOG.debug("Cache response - healthCheck: {}; status: {}; msg: {}", description, response.getStatus(), response.getMessage());
        cachedResponse = response;
        cachedResponseTimestamp = Instant.now();
        cachedExpires = expires;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public List<String> getTags() {
        return tags;
    }

    @Override
    public synchronized Response perform(Context context) throws Exception {
        if (!cachedExpires || Duration.between(cachedResponseTimestamp, Instant.now()).compareTo(context.getMaxAge()) < 0) {
            return cachedResponse;
        } else {
            return new Response(Status.Failure, String.format("did not receive a recent response - maxAge: %s", context.getMaxAge()));
        }
    }

}
