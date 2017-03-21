/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2014 The OpenNMS Group, Inc.
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

package org.opennms.poller.remote;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.swing.UIDefaults;
import javax.swing.plaf.metal.MetalLookAndFeel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpenNMSLookAndFeel extends MetalLookAndFeel {
    private static final Logger LOG = LoggerFactory.getLogger(OpenNMSLookAndFeel.class);
    private static final long serialVersionUID = 1L;

    @Override
    public UIDefaults getDefaults() {
        final UIDefaults def = super.getDefaults();

        final String osName = System.getProperty("os.name");

        if (osName.contains("Windows")) {
            installKeybindingsIfPossible("sun.swing.plaf.WindowsKeybindings", def);
        } else if (osName.contains("Mac OS")) {
            MacKeybindings.installKeybindings(def);
        } else {
            installKeybindingsIfPossible("sun.swing.plaf.GTKKeybindings", def);
        }

        return def;
    }
    private void installKeybindingsIfPossible(final String className, final UIDefaults defaults) {
        try {
            final Class<?> c = Class.forName(className);
            final Method m = c.getMethod("installKeybindings", UIDefaults.class);
            m.invoke(null, new Object[] { defaults });
        } catch (final SecurityException | NoSuchMethodException | ClassNotFoundException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            LOG.warn("Unable to get {}, falling back to default Nimbus behavior.", className, e);
        }
    }
}
