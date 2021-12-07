/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2021 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2021 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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
