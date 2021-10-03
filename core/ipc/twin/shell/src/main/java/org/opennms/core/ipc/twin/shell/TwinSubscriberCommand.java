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

import java.io.Closeable;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Command(scope = "opennms", name = "twin-subscribe", description = "Get current Twin Object for the key specified")
@Service
public class TwinSubscriberCommand implements Action {

    private AtomicBoolean objectReceived = new AtomicBoolean(false);

    @Reference
    private TwinSubscriber twinSubscriber;

    @Argument(index = 0, name = "consumer-key", description = "Consumer key", required = true)
    @Completion(TwinKeyCompleter.class)
    private String key;

    @Option(name = "-t", aliases = "--timeout in ms", description = "Session timeout before closing session")
    private long timeout = 15000;

    @Override
    public Object execute() throws Exception {
        Instant initialTime = Instant.now();
        Closeable closeable = twinSubscriber.subscribe(key, TwinKeyCompleter.twinClazzMap.get(key), this::accept);
        while (!objectReceived.get() &&
                Duration.between(initialTime, Instant.now()).compareTo(Duration.of(timeout, ChronoUnit.MILLIS)) < 0) {
            System.out.printf(".");
            Thread.sleep(1000);
        }
        closeable.close();
        return null;
    }

    private void accept(Object obj) {
        if (obj != null) {
            System.out.printf("Object returned for key '%s' = \n", key);
            System.out.println(obj);
            objectReceived.set(true);
        }
    }
}
