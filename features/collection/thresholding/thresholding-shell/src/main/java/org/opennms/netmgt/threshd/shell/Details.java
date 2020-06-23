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

package org.opennms.netmgt.threshd.shell;

import static org.opennms.netmgt.threshd.AbstractThresholdEvaluatorState.fst;

import java.util.Optional;

import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;

@Command(scope = "opennms-threshold-states", name = "details", description = "Prints the details of a specific " +
        "threshold state")
@Service
public class Details extends AbstractKeyOrIndexCommand {
    @Override
    public Object execute() {
        String key = getKey();
        Optional<byte[]> value = blobStore.get(key, THRESHOLDING_KV_CONTEXT);

        if (value.isPresent()) {
            System.out.println(fst.asObject(value.get()).toString());
        } else {
            System.out.printf("Could not find a state for key '%s'\n", key);
        }

        return null;
    }
}
