/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
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
 * OpenNMS(R) Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *******************************************************************************/

package org.opennms.minion.provisiond.shell;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Completion;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.opennms.netmgt.icmp.Pinger;
import org.opennms.netmgt.icmp.PingerFactory;
import org.opennms.netmgt.provision.DetectorFactory;
import org.opennms.netmgt.provision.SyncServiceDetector;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyAccessorFactory;

@Command(scope = "minion", name = "detect", description = "Detects services at given ipAddress")
@Service
public class MinionDetector implements Action {

    @Argument(index = 0, name = "detectorType", description = "Type of detector", required = true, multiValued = false, valueToShowInHelp = "icmp")
    @Completion(DetectorTypeCompleter.class)
    String detectorType = null;

    @Argument(index = 1, name = "address", description = "The address to be detected", required = true, multiValued = false)
    String address = null;

    @Argument(index = 2, name = "attributes", description = "Attributes to be set in key=value  form", multiValued = true)
    List<String> attributes;

    @SuppressWarnings("rawtypes")
    @Reference
    List<DetectorFactory> detectorFactoryList;

    @Reference
    Pinger pinger;

    @Override
    public Object execute() throws Exception {

        PingerFactory.setInstance(pinger);
        InetAddress ipAddress = InetAddress.getByName(address);
        boolean isServiceDetected = false;
        Map<String, String> properties = parse(attributes);

        for (@SuppressWarnings("rawtypes")
        DetectorFactory detectorFactory : detectorFactoryList) {

            SyncServiceDetector detector = detectorFactory.createDetector();

            if ((detector.getServiceName().equals(detectorType))) {
                System.out.println("Trying to detect   " + detectorType
                        + "   service at    " + address);
                try {
                    setAttributes(properties, detector);
                    isServiceDetected = detector.isServiceDetected(ipAddress);
                } catch (Exception e) {
                    System.out.println(" Exception caused for detectorType "
                            + detectorType);
                }
                System.out.println(detectorType + "   service detected at "
                        + ipAddress + "   is   " + isServiceDetected);

            }
        }

        return null;

    }

    private void setAttributes(Map<String, String> properties,
            SyncServiceDetector detector) {

        BeanWrapper wrapper = PropertyAccessorFactory.forBeanPropertyAccess(detector);

        try {
            wrapper.setPropertyValues(properties);
        } catch (BeansException e) {
            System.out.println("Could not set properties on detector "
                    + e.getMessage());
        }
    }

    private Map<String, String> parse(List<String> attributeList) {
        Map<String, String> properties = new HashMap<>();
        if (attributeList != null) {
            for (String keyValue : attributeList) {
                int splitAt = keyValue.indexOf("=");
                if (splitAt <= 0) {
                    throw new IllegalArgumentException("Invalid property "
                            + keyValue);
                } else {
                    String key = keyValue.substring(0, splitAt);
                    String value = keyValue.substring(splitAt + 1,
                                                      keyValue.length());
                    properties.put(key, value);
                }
            }
        }
        return properties;
    }

    public void setAddress(String address) {
        this.address = address;
    }

}
