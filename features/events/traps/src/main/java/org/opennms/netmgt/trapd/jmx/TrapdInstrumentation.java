/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016-2017 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.trapd.jmx;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicLong;

public class TrapdInstrumentation {

    private static final Logger LOG = LoggerFactory.getLogger(TrapdInstrumentation.class);

    private final AtomicLong trapsReceived = new AtomicLong();
    private final AtomicLong v1TrapsReceived = new AtomicLong();
    private final AtomicLong v2cTrapsReceived = new AtomicLong();
    private final AtomicLong v3TrapsReceived = new AtomicLong();
    private final AtomicLong vUnknownTrapsReceived = new AtomicLong();
    private final AtomicLong trapsDiscarded = new AtomicLong();
    private final AtomicLong trapsErrored = new AtomicLong();

    public void incTrapsReceivedCount(String version) {
        trapsReceived.incrementAndGet();
        if ("v1".equals(version)) {
            v1TrapsReceived.incrementAndGet();
        } else if ("v2c".equals(version) || "v2".equals(version)) {
            v2cTrapsReceived.incrementAndGet();
        } else if ("v3".equals(version)) {
            v3TrapsReceived.incrementAndGet();
        } else {
            vUnknownTrapsReceived.incrementAndGet();
            LOG.warn("Received a trap with an unknown SNMP protocol version '{}'.", version);
        }
    }

    public void incDiscardCount() {
        trapsDiscarded.incrementAndGet();
    }

    public void incErrorCount() {
        trapsErrored.incrementAndGet();
    }

    public long getV1TrapsReceived() {
        return v1TrapsReceived.get();
    }

    public long getV2cTrapsReceived() {
        return v2cTrapsReceived.get();
    }

    public long getV3TrapsReceived() {
        return v3TrapsReceived.get();
    }

    public long getVUnknownTrapsReceived() {
        return vUnknownTrapsReceived.get();
    }

    public long getTrapsDiscarded() {
        return trapsDiscarded.get();
    }

    public long getTrapsErrored() {
        return trapsErrored.get();
    }

    public long getTrapsReceived() {
        return trapsReceived.get();
    }
}
