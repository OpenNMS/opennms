/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017 The OpenNMS Group, Inc.
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

package org.opennms.core.ipc.common.aws.sqs;

/**
 * The Interface AmazonSQSConstants.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public interface AmazonSQSConstants {

    final String AWS_CONFIG_PID = "org.opennms.core.ipc.aws.sqs";

    final String AWS_CONFIG_SYS_PROP_PREFIX = AWS_CONFIG_PID + ".";

    final String SINK_QUEUE_PROP_PREFIX = "sink.";

    final String RPC_QUEUE_PROP_PREFIX = "rpc.";

    final String AWS_REGION = "aws_region";

    final String AWS_ACCESS_KEY_ID = "aws_access_key_id";

    final String AWS_SECRET_ACCESS_KEY = "aws_secret_access_key";

    final String AWS_QUEUE_NAME_PREFIX = "aws_queue_name_prefix";

    final String AWS_USE_HTTP = "aws_use_http";

}
