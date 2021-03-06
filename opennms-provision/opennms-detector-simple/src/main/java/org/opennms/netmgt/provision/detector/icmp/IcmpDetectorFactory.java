/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision.detector.icmp;

import java.util.Map;

import org.opennms.netmgt.icmp.PingerFactory;
import org.opennms.netmgt.provision.support.GenericServiceDetectorFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class IcmpDetectorFactory extends GenericServiceDetectorFactory<IcmpDetector> {

    @Autowired(required=false)
    private PingerFactory pingerFactory;

    public IcmpDetectorFactory() {
        super(IcmpDetector.class);
    }

    @Override
    public IcmpDetector createDetector(Map<String, String> properties) {
        final IcmpDetector detector = new IcmpDetector();
        setBeanProperties(detector, properties);
        detector.setPingerFactory(pingerFactory);
        return detector;
    }

    public void setPingerFactory(PingerFactory pingerFactory) {
        this.pingerFactory = pingerFactory;
    }

}
