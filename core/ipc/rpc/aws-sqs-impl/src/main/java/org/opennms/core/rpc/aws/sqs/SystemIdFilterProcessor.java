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

package org.opennms.core.rpc.aws.sqs;

import org.apache.camel.AsyncCallback;
import org.apache.camel.AsyncProcessor;
import org.apache.camel.Exchange;
import org.opennms.core.rpc.camel.CamelRpcConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class SystemIdFilterProcessor implements AsyncProcessor {
    private static final Logger LOG = LoggerFactory.getLogger(SystemIdFilterProcessor.class);

    private final String systemId;

    public SystemIdFilterProcessor(String systemId) {
        this.systemId = Objects.requireNonNull(systemId);
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        throw new UnsupportedOperationException("This processor must be invoked using the async interface.");
    }

    @Override
    public boolean process(Exchange exchange, AsyncCallback callback) {
        // Retrieve the system id header in the given request
        final String targettedSystemId = (String)exchange.getIn().getHeader(CamelRpcConstants.JMS_SYSTEM_ID_HEADER);
        if (targettedSystemId == null || systemId.equals(targettedSystemId)) {
            // Either no system was specified, or the requested system id matches our
            // Accept the request
        } else {
            // We should reject this request, but the SQS JMS connector doesn't provide
            // a way for us to reset the visibility timeout of the message at this stage
            // so for now we just log and process the message anyways
            LOG.info("Directed RPCs are not supported with SQS. The message targeted to %s, but our system id is %s." +
                    "Processing the message anyways.");
        }
        callback.done(false);
        return false;
    }
}
