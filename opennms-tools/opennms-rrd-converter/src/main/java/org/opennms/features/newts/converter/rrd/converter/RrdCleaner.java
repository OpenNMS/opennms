/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
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

package org.opennms.features.newts.converter.rrd.converter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.jrobin.core.RrdDb;

public class RrdCleaner {

    public void execute(final CommandLine cmd) {
        for (final Object arg : cmd.getArgList()) {
            LogUtils.infof(this, "Scanning %s for single-metric JRBs and temporal files.", arg);
            final File f = new File((String)arg);
            if (f.exists() && f.isDirectory()) {
                for (final File rrdFile : findRrds(f)) {
                    cleanRrd(rrdFile);
                }
            } else {
                LogUtils.warnf(this, "File or directory %s doesn't exist", f);
            }
        }
    }

    public List<File> findRrds(final File topDirectory) {
        final List<File> files = new ArrayList<>();
        findRrds(topDirectory, files);
        return files;
    }

    private void cleanRrd(File f) {
        LogUtils.infof(this, "Removing %s", f);
        try {
            if (!f.delete()) {
                LogUtils.warnf(this, "Couldn't delete the file %s", f);
            }
        } catch (Exception e) {
            LogUtils.warnf(this, "Can't delete the file %s", f);
        }
    }

    private void findRrds(final File directory, final List<File> files) {
        for (final File f : directory.listFiles()) {
            if (f.isDirectory()) {
                findRrds(f, files);
            } else {
                if (f.getName().endsWith(".finished") || isSingleMetricDs(f)) {
                    files.add(f);
                }
            }
        }
    }

    private boolean isSingleMetricDs(final File rrdFile) {
        if (! (rrdFile.getName().endsWith(".rrd") || rrdFile.getName().endsWith(".jrb"))) {
            return false;
        }
        try {
            final RrdDb db = new RrdDb(rrdFile.getAbsolutePath(), true);
            String[] dsNames = db.getDsNames();
            return dsNames.length == 1 && rrdFile.getName().startsWith(dsNames[0] + '.');
        } catch (final Exception e) {
            LogUtils.warnf(JRobinConverter.class, e, "error reading file %s", rrdFile);
            return false;
        }
    }

}
