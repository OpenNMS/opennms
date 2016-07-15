/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
<<<<<<< ccb274e4dd4064f27f4c8ad6fd2ecd4eb96a046b
 * Copyright (C) 2008-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
=======
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
>>>>>>> hzn-838: Add jmx, web, ssh detectors to console
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
<<<<<<< ccb274e4dd4064f27f4c8ad6fd2ecd4eb96a046b
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
=======
 * OpenNMS(R) Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
>>>>>>> hzn-838: Add jmx, web, ssh detectors to console
 *******************************************************************************/

package org.opennms.netmgt.provision.detector.ssh;

import org.opennms.netmgt.provision.AbstractServiceDetectorFactory;
import org.springframework.stereotype.Component;

@Component
public class SshDetectorFactory extends AbstractServiceDetectorFactory<SshDetector> {

    @Override
    public SshDetector createDetector() {
        return new SshDetector();
    }
}
