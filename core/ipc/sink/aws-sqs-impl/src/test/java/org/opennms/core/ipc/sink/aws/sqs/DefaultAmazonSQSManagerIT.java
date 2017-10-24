/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2017 The OpenNMS Group, Inc.
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

package org.opennms.core.ipc.sink.aws.sqs;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Properties;

import org.junit.Assert;
import org.junit.Test;
import org.opennms.core.ipc.sink.api.SinkModule;

/**
 * The Class DefaultAmazonSQSManagerIT.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class DefaultAmazonSQSManagerIT {

    /**
     * Test queue name.
     */
    @Test
    public void testQueueName() {
        DefaultAmazonSQSManager mgr = new DefaultAmazonSQSManager();
        Properties awsConfig = new Properties();
        SinkModule<?, ?> module = mock(SinkModule.class);
        when(module.getId()).thenReturn("Heartbeat");
        Assert.assertEquals("OpenNMS-Sink-Heartbeat", mgr.getQueueName(awsConfig, module));
        awsConfig.put(AmazonSQSManager.AWS_QUEUE_NAME_PREFIX, "PROD");
        Assert.assertEquals("PROD-OpenNMS-Sink-Heartbeat", mgr.getQueueName(awsConfig, module));
    }

}
