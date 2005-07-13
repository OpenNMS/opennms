//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2005 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// Jul 8, 2004: Created this file.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.                                                            
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//       
// For more information contact: 
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
// Tab Size = 8
//

package org.opennms.netmgt.rrd;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Defines an abstract strategy for manipulating round robin database file. This
 * is used by the RrdUtils to implement the appropriate behaviour
 */
public interface RrdStrategy {

    /**
     * Initialize the appropriate round robin system
     * 
     * @throws Exception
     *             if an Error occurs
     */
    void initialize() throws Exception;

    /**
     * This Initializes the graphics subsystem only. This is used by the web
     * application to avoid the need for the JNI infrastructure in the webapp.
     */
    void graphicsInitialize() throws Exception;

    /**
     * Create a round robin database definition from the supplied parameters.
     * This definition is used in the createFile call to create the actual file.
     * 
     * @param creator -
     *            A string representing who is creating this file for use in log
     *            msgs
     * @param directory -
     *            The directory to create the file in
     * @param dsName -
     *            The datasource name for use in the round robin database
     * @param step -
     *            the step for the database
     * @param dsType -
     *            the type for the datasource
     * @param dsHeartbeat -
     *            the heartbeat for the datasouce
     * @param dsMin -
     *            the minimum allowable value for the datasource
     * @param dsMax -
     *            the maximum allowable value for the datasouce
     * @param rraList -
     *            a List of the round robin archives to create in the database
     * @return an object representing the definition of an round robin database
     * @throws Exception
     *             If an error occurs while creating the definition
     */
    Object createDefinition(String creator, String directory, String dsName, int step, String dsType, int dsHeartbeat, String dsMin, String dsMax, List rraList) throws Exception;

    /**
     * Creates the round robin database defined by the supplied definition.
     * 
     * @param rrdDef
     *            an round robin database definition created using the
     *            createDefinition call.
     * @throws Exception
     *             if an error occurs create the file
     */
    void createFile(Object rrdDef) throws Exception;

    /**
     * Opens the round robin database with the supplied name. It is assumed the
     * name refers to a round robin database appropriate for this strategy
     * implementation
     * 
     * @param fileName
     *            the name of the associated rrd file
     * @return an open rrd reference that can by used in calls to updateFile and
     *         closeFile
     * @throws Exception
     *             if an error occurs opening the file
     */
    Object openFile(String fileName) throws Exception;

    /**
     * Updates the supplied round robin database with the given timestamp:value
     * point
     * 
     * @param rrd
     *            an rrd object created using openFile
     * @param owner
     *            the owner of the rrd
     * @param data
     *            a string of the form <timestamp>: <datavalue>
     * @throws Exception
     *             if an error occurs updating the file
     */
    void updateFile(Object rrd, String owner, String data) throws Exception;

    /**
     * This closes the supplied round robin database
     * 
     * @param rrd
     *            an rrd object created using openFile
     * @throws Exception
     *             if an error occurs closing the file
     */
    void closeFile(Object rrd) throws Exception;

    /**
     * Fetches the last value from the round robin database with the given name.
     * The interval passed in should be the interval associated with the round
     * robin database.
     * 
     * @param rrdFile
     *            a name the represents a round robin database
     * @param interval
     *            a step interval of the round robin database
     * @return The last value as a Double (if the last value didn't exist
     *         returns a Double.NaN)
     * @throws NumberFormatException
     * @throws RrdException
     */
    public Double fetchLastValue(String rrdFile, int interval) throws NumberFormatException, RrdException;

    /**
     * Creates an InputStream representing the bytes of a graph created from
     * round robin data. It accepts an rrdtool graph command. The underlying
     * implementation converts this command to a format appropriate for it .
     * 
     * @param command
     *            the command needed to create the graph
     * @param workDir
     *            the directory that all referenced files are relative to
     * @return an input stream representing the bytes of a graph image as a PNG
     *         file
     * @throws IOException
     *             if an IOError occurs
     * @throws RrdException
     *             if an RRD error occurs
     */
    public InputStream createGraph(String command, File workDir) throws IOException, RrdException;

    /**
     * Provides the round robin database an opportunity to contribute statistics
     * information to the logs file.
     * 
     * @return a non-null string representing any staticstics to be included in
     *         the logs
     */
    public String getStats();
}
