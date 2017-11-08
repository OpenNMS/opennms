/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016-2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.trapd;

import java.util.Objects;

import org.opennms.core.ipc.sink.api.Message;
import org.opennms.netmgt.snmp.SnmpException;
import org.opennms.netmgt.snmp.TrapInformation;

/**
 * Wrapper to make the {@link TrapInformation} object Sink API compatible, without adding the dependency to the sink-api module.
 *
 * @author mvrueden
 */
public class TrapInformationWrapper implements Message {

    private final TrapInformation trapInformation;

    public TrapInformationWrapper(TrapInformation trapInformation) throws SnmpException {
        this.trapInformation = Objects.requireNonNull(trapInformation);
        trapInformation.validate(); // Before this was at ProcessQueueProcessor which does not exist anymore
    }

    public TrapInformation getTrapInformation() {
        return trapInformation;
    }
}
