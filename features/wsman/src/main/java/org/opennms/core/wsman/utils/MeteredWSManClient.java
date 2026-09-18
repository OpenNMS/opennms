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
package org.opennms.core.wsman.utils;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

import org.opennms.core.wsman.Identity;
import org.opennms.core.wsman.WSManClient;
import org.opennms.core.wsman.shell.CommandResult;
import org.opennms.core.wsman.shell.ShellOptions;
import org.w3c.dom.Node;

/** Counts every call on the wrapped client in {@link WsManMetrics}; a thrown exception counts as a failure. */
public final class MeteredWSManClient implements WSManClient {

    private final WSManClient delegate;
    private final WsManMetrics metrics;

    public MeteredWSManClient(WSManClient delegate) {
        this(delegate, WsManMetrics.INSTANCE);
    }

    MeteredWSManClient(WSManClient delegate, WsManMetrics metrics) {
        this.delegate = Objects.requireNonNull(delegate);
        this.metrics = Objects.requireNonNull(metrics);
    }

    public WSManClient getDelegate() {
        return delegate;
    }

    private <T> T metered(Supplier<T> call) {
        metrics.request();
        try {
            return call.get();
        } catch (RuntimeException e) {
            metrics.requestFailed();
            throw e;
        }
    }

    @Override
    public Identity identify() {
        return metered(delegate::identify);
    }

    @Override
    public Node get(String resourceUri, Map<String, String> selectors) {
        return metered(() -> delegate.get(resourceUri, selectors));
    }

    @Override
    public String enumerate(String resourceUri) {
        return metered(() -> delegate.enumerate(resourceUri));
    }

    @Override
    public String enumerateWithFilter(String resourceUri, String dialect, String filter) {
        return metered(() -> delegate.enumerateWithFilter(resourceUri, dialect, filter));
    }

    @Override
    public String pull(String contextId, String resourceUri, List<Node> nodes, boolean recursive) {
        return metered(() -> delegate.pull(contextId, resourceUri, nodes, recursive));
    }

    @Override
    public String enumerateAndPull(String resourceUri, List<Node> nodes, boolean recursive) {
        return metered(() -> delegate.enumerateAndPull(resourceUri, nodes, recursive));
    }

    @Override
    public String enumerateAndPullUsingFilter(String resourceUri, String dialect, String filter, List<Node> nodes, boolean recursive) {
        return metered(() -> delegate.enumerateAndPullUsingFilter(resourceUri, dialect, filter, nodes, recursive));
    }

    @Override
    public CommandResult runCommand(String command, String[] args, Duration timeout, ShellOptions options) {
        metrics.command();
        return metered(() -> delegate.runCommand(command, args, timeout, options));
    }

    @Override
    public CommandResult runCommand(String command, String[] args, Duration timeout) {
        metrics.command();
        return metered(() -> delegate.runCommand(command, args, timeout));
    }

    @Override
    public void close() {
        delegate.close();
    }
}
