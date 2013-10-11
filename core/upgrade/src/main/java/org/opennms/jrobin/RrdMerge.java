/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
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
package org.opennms.jrobin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.jrobin.core.RrdDb;
import org.jrobin.core.RrdDef;

/**
 * The Class RrdMerge.
 * 
 * @author Alejandro Galue <agalue@opennms.org>
 */
public class RrdMerge {

    /**
     * Merge JRBs.
     *
     * @param source the source JRB
     * @param dest the destination JRB
     * @return the file
     * @throws Exception the exception
     */
    public File mergeJrbs(File source, File dest) throws Exception {
        final List<RrdDatabase> rrds = new ArrayList<RrdDatabase>();
        rrds.add(new RrdDatabase(new RrdDb(source, true)));
        rrds.add(new RrdDatabase(new RrdDb(dest, true)));
        final TimeSeriesDataSource dataSource = new AggregateTimeSeriesDataSource(rrds);

        final File outputFile = createEmptyJrb(dest);
        final RrdDb outputRrd = new RrdDb(outputFile);
        final RrdDatabaseWriter writer = new RrdDatabaseWriter(outputRrd);
        final long endTime = dataSource.getEndTime();
        final long startTime = dataSource.getStartTime(); // FIXME Is this correct
        for (long time = startTime; time <= endTime; time += dataSource.getNativeStep()) {
            final RrdEntry entry = dataSource.getDataAt(time);
            writer.write(entry);
        }
        dataSource.close();
        outputRrd.close();
        return outputFile;
    }

    /**
     * Creates the empty JRB.
     *
     * @param dest the destination file
     * @return the new file
     * @throws Exception the exception
     */
    private File createEmptyJrb(File dest) throws Exception {
        final File outputFile = new File(dest.getCanonicalPath() + ".merged");
        RrdDb destJrb = new RrdDb(dest, true);
        RrdDef outputRef = destJrb.getRrdDef();
        outputRef.setPath(outputFile.getCanonicalPath());
        outputRef.setStartTime(0);
        final RrdDb newRrd = new RrdDb(outputRef);
        newRrd.close();
        destJrb.close();
        return outputFile;
    }
}
