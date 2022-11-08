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

package org.opennms.core.ipc.twin.jms.subscriber;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.opennms.core.utils.SystemInfoUtils;

public class JmsTwinRpcPreProcessor implements Processor {

    public static String JMS_QUEUE_NAME_HEADER = "JmsQueueName";
    private static final String TWIN_QUEUE_NAME_FORMAT = "%s.%s";

    @Override
    public void process(Exchange exchange) throws Exception {
        String queueName = String.format(TWIN_QUEUE_NAME_FORMAT, SystemInfoUtils.getInstanceId(), "Twin.RPC");
        exchange.getIn().setHeader(JMS_QUEUE_NAME_HEADER, queueName);
    }
}
