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
package org.opennms.core.ipc.twin.shell;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Completion;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.opennms.core.ipc.twin.api.TwinSubscriber;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Command(scope = "opennms", name = "twin-subscribe", description = "Get current Twin Object for the key specified")
@Service
public class TwinSubscriberCommand implements Action {

    @Reference
    private TwinSubscriber twinSubscriber;

    @Argument(index = 0, name = "consumer-key", description = "Consumer key", required = true)
    @Completion(TwinKeyCompleter.class)
    private String key;

    @Option(name = "-t", aliases = "--timeout in ms", description = "Session timeout before closing session")
    private long timeout = 15000;

    @Override
    public Object execute() throws Exception {
        final var clazz = TwinKeyCompleter.twinClazzMap.get(this.key);
        if (clazz == null) {
            System.err.println("Unknown key: " + this.key);
            return null;
        }

        final var received = new CompletableFuture<>();

        try(final var subscription = twinSubscriber.subscribe(this.key,
                                                              clazz,
                                                              received::complete)) {
            final var result = received.get(this.timeout, TimeUnit.MILLISECONDS);
            System.out.println("Object returned for key:" + this.key);
            System.out.println(result);

        } catch (final TimeoutException e) {
            System.err.println("Timeout");
        }

        return null;
    }
}
