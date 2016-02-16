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
