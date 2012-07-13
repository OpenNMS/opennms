/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/
package org.opennms.features.vaadin.mibcompiler.services;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.percederberg.mibble.Mib;
import net.percederberg.mibble.MibLoader;
import net.percederberg.mibble.MibLoaderException;
import net.percederberg.mibble.MibLoaderLog;

import org.opennms.core.utils.LogUtils;
import org.opennms.features.vaadin.mibcompiler.api.MibParser;
import org.opennms.netmgt.config.datacollection.DatacollectionGroup;
import org.opennms.netmgt.mib2events.Mib2Events;
import org.opennms.netmgt.xml.eventconf.Events;

/**
 * Mibble implementation of the interface MibParser.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a> 
 */
public class MibbleMibParser implements MibParser {

    /** The errors. */
    private String errors;

    /** The loader. */
    private MibLoader loader;

    /** The MIB object. */
    private Mib mib;

    /** The pattern. */
    private Pattern pattern = Pattern.compile("couldn't find referenced MIB '(.+)'", Pattern.MULTILINE);

    /**
     * Instantiates a new Mibble MIB parser.
     */
    public MibbleMibParser() {
        loader = new MibLoader();
    }

    /* (non-Javadoc)
     * @see org.opennms.features.vaadin.mibcompiler.MibParser#addMibDirectory(java.io.File)
     */
    public void addMibDirectory(File mibDirectory) {
        LogUtils.debugf(this, "Adding MIB directory to %s", mibDirectory);
        loader.addDir(mibDirectory);
    }

    /* (non-Javadoc)
     * @see org.opennms.features.vaadin.mibcompiler.MibParser#parseMib(java.io.File)
     */
    public boolean parseMib(File mibFile) {
        try {
            LogUtils.debugf(this, "Parsing MIB file %s", mibFile);
            mib = loader.load(mibFile);
            if (mib.getLog().warningCount() > 0) {
                LogUtils.infof(this, "Warning found when processing %s", mibFile);
                handleMibError(mib.getLog());
                return false;
            }
            return true;
        } catch (IOException e) {
            errors = e.getMessage();
            LogUtils.infof(this, "IO error: " + errors);
        } catch (MibLoaderException e) {
            handleMibError(e.getLog());
            LogUtils.infof(this, "MIB error: " + errors);
        }
        return false;
    }

    /* (non-Javadoc)
     * @see org.opennms.features.vaadin.mibcompiler.MibParser#getFormattedErrors()
     */
    public String getFormattedErrors() {
        return errors;
    }

    /* (non-Javadoc)
     * @see org.opennms.features.vaadin.mibcompiler.MibParser#getMissingDependencies()
     */
    public List<String> getMissingDependencies() {
        List<String> dependencies = new ArrayList<String>();
        if (errors == null)
            return dependencies;
        Matcher m = pattern.matcher(errors);
        while (m.find()) {
            final String dep = m.group(1);
            if (!dependencies.contains(dep))
                dependencies.add(dep);
        }
        return dependencies;
    }

    /* (non-Javadoc)
     * @see org.opennms.features.vaadin.mibcompiler.services.MibParser#getEvents(java.lang.String)
     */
    public Events getEvents(String ueibase) {
        if (mib == null) {
            return null;
        }
        Mib2Events converter = new Mib2Events();
        return converter.convertMibToEvents(mib, ueibase);
    }

    /* (non-Javadoc)
     * @see org.opennms.features.vaadin.mibcompiler.api.MibParser#getDataCollection(java.lang.String)
     */
    public DatacollectionGroup getDataCollection(String groupname) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * Handle MIB error.
     *
     * @param log the log
     */
    private void handleMibError(MibLoaderLog log) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(os);
        log.printTo(ps);
        errors = os.toString();
    }

}
