/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2021 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 2021-2021 The OpenNMS Group, Inc.
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

<<<<<<<< HEAD:features/config/service/api/src/main/java/org/opennms/features/config/exception/ConfigConversionException.java
package org.opennms.features.config.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This exception is mainly about config conversion
 */
public class ConfigConversionException extends RuntimeException {
    private static final Logger LOG = LoggerFactory.getLogger(ConfigConversionException.class);

    public ConfigConversionException(Exception e, Object object) {
        super(e);
        LOG.error("{} message: {}", this.getClass().getSimpleName(), (object != null) ? object.toString() : null);
========
package org.opennms.features.config.osgi.del;

import java.util.Set;

/**
 * We hard code here the migrated services.
 * This is necessary since we don't have access to CM when we need to make a decision about to whom to delegate to.
 * We can't use the start of the pid name ("org.opennms") since features might be started from fileinstall before
 * cm is available.
 * This class can be deleted once we have moved all opennms plugins to cm.
 */
public class MigratedServices {

    final public static Set<String> PIDS =  Set.of(
            "org.opennms.features.datachoices"
    );

    public static boolean isMigrated(final String pid) {
        return PIDS.contains(pid);
>>>>>>>> features/CM-Phase1:features/config/osgi/del/src/main/java/org/opennms/features/config/osgi/del/MigratedServices.java
    }
}
