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

package org.opennms.core.ipc.sink.aws.sqs;

import org.apache.camel.util.KeyValueHolder;
import org.junit.Test;
import org.opennms.core.ipc.common.aws.sqs.AmazonSQSConfig;
import org.opennms.core.ipc.common.aws.sqs.MapBasedSQSConfig;
import org.opennms.core.ipc.common.aws.sqs.AmazonSQSConstants;
import org.opennms.core.test.camel.CamelBlueprintTest;

import java.util.Dictionary;
import java.util.Map;
import java.util.Properties;

/**
 * The Class AwsDispatcherBlueprintIT.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class AmazonSQSDispatcherBlueprintIT extends CamelBlueprintTest {

    @SuppressWarnings( "rawtypes" )
    @Override
    protected void addServicesOnStartup(Map<String, KeyValueHolder<Object, Dictionary>> services) {
        services.put(AmazonSQSConfig.class.getName(),
                new KeyValueHolder<Object, Dictionary>(new MapBasedSQSConfig(),
                        new Properties()));
    }

    /* (non-Javadoc)
     * @see org.apache.camel.test.blueprint.CamelBlueprintTestSupport#getBlueprintDescriptor()
     */
    @Override
    protected String getBlueprintDescriptor() {
        return "classpath:/OSGI-INF/blueprint/blueprint-ipc-client.xml,blueprint-empty-camel-context.xml";
    }

    /* (non-Javadoc)
     * @see org.apache.camel.test.blueprint.CamelBlueprintTestSupport#setConfigAdminInitialConfiguration(java.util.Properties)
     */
    @Override
    protected String setConfigAdminInitialConfiguration(final Properties props) {
        return AmazonSQSConstants.AWS_CONFIG_PID;
    }

    /**
     * Can blueprint load successfully.
     */
    @Test
    public void canBlueprintLoadSuccessfully() {
    }

}
