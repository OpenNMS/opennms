/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.core.ipc.sink.kafka;

import java.util.Properties;

import org.junit.Test;
import org.opennms.core.test.camel.CamelBlueprintTest;

public class KafkaDispatcherBlueprintIT extends CamelBlueprintTest {

    @Override
    protected String getBlueprintDescriptor() {
        return "classpath:/OSGI-INF/blueprint/blueprint-ipc-client.xml,blueprint-empty-camel-context.xml";
    }

    @Override
    protected String setConfigAdminInitialConfiguration(final Properties props) {
        props.put("bootstrap.servers", "127.0.0.1:9092");
        return KafkaSinkConstants.KAFKA_CONFIG_PID;
    }

    @Test
    public void canBlueprintLoadSuccesfully() {
        
    }
}
