/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

package org.opennms.smoketest.sentinel;

import org.junit.ClassRule;
import org.junit.Test;
import org.opennms.smoketest.stacks.IpcStrategy;
import org.opennms.smoketest.stacks.BlobStoreStrategy;
import org.opennms.smoketest.stacks.JsonStoreStrategy;
import org.opennms.smoketest.stacks.OpenNMSStack;
import org.opennms.smoketest.stacks.StackModel;
import org.opennms.smoketest.utils.KarafShell;

public class KeyValueStoresIT {
    @ClassRule
    public static final OpenNMSStack stack = OpenNMSStack.withModel(StackModel.newBuilder()
            .withSentinel()
            .withTelemetryProcessing()
            .withBlobStoreStrategy(BlobStoreStrategy.NEWTS_CASSANDRA)
            .withJsonStoreStrategy(JsonStoreStrategy.POSTGRES)
            .withIpcStrategy(IpcStrategy.JMS)
            .build());

    @Test
    public void canPutAndGetBlobsOnSentinel() {
        String value = "blob tubessssssssss!";
        KarafShell ks = new KarafShell(stack.sentinel().getSshAddress());
        ks.runCommand("opennms-kv-blob:put \"key\" \"context\" \"" + value + "\"");
        ks.runCommand("opennms-kv-blob:get \"key\" \"context\"", s -> s.contains(value));
    }

    @Test
    public void canPutAndGetJSONOnSentinel() {
        String value = "{\"label\": \"JSON tubessssssssss!\"}";
        KarafShell ks = new KarafShell(stack.sentinel().getSshAddress());
        ks.runCommand("opennms-kv-json:put \"key\" \"context\" '" + value + "'");
        ks.runCommand("opennms-kv-json:get \"key\" \"context\"", s -> s.contains(value));
    }

    @Test
    public void canPutAndGetJSONOnOpenNMS() {
        String value = "{\"label\": \"JSON tubessssssssss!\"}";
        KarafShell ks = new KarafShell(stack.opennms().getSshAddress());
        ks.runCommand("opennms-kv-json:put \"key\" \"context\" '" + value + "'");
        ks.runCommand("opennms-kv-json:get \"key\" \"context\"", s -> s.contains(value));
    }
}
