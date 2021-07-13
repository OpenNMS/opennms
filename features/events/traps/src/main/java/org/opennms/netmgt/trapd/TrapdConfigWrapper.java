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

package org.opennms.netmgt.trapd;

import org.opennms.core.twin.api.OnmsTwin;
import org.opennms.core.twin.publisher.api.OnmsTwinPublisher;
import org.opennms.netmgt.config.TrapdConfig;

public class TrapdConfigWrapper {

    private final TrapdConfig trapdConfig;

    private OnmsTwinPublisher twinPublisher;

    public TrapdConfigWrapper(TrapdConfig trapdConfig) {
        this.trapdConfig = trapdConfig;
    }

    public void init() {
        // Marshal trapdconfig and convert it to byte array.
        byte[] marshalledConfig = marshalTrapdConfig(trapdConfig);
        OnmsTwin onmsTwin = new OnmsTwin() {
            @Override
            public int getVersion() {
                return 0;
            }

            @Override
            public byte[] getObjectValue() {
                return marshalledConfig;
            }

            @Override
            public String getKey() {
                return "trapd-config";
            }

            @Override
            public String getLocation() {
                return null;
            }
        };
        OnmsTwinPublisher.Callback callback = twinPublisher.register(onmsTwin);
        // Register this callback that gets updates to TrapdConfig.
    }

    public void setTwinPublisher(OnmsTwinPublisher twinPublisher) {
        this.twinPublisher = twinPublisher;
    }

    public byte[] marshalTrapdConfig(TrapdConfig trapdConfig) {
        return null;
    }
}
