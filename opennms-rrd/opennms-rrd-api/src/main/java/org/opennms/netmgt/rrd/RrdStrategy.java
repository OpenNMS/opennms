/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2002-2005 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified 
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 *
 * 2007 Apr 05: Use Java 5 generics. - dj@opennms.org
 * 2007 Mar 19: Added createGraphReturnDetails. - dj@opennms.org
 * 2004 Jul 08: Created this file.
 *
 * Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *       
 * For more information contact: 
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.netmgt.rrd;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;

/**
 * Defines an abstract strategy for manipulating round robin database file. This
 * is used by the RrdUtils to implement the appropriate behavior
 */
public interface RrdStrategy<D,F> {

    /**
     * Initialize the appropriate round robin system
     * 
     * @throws Exception
     *             if an Error occurs
     */
    public void initialize() throws Exception;

    /**
     * This Initializes the graphics subsystem only. This is used by the web
     * application to avoid the need for the JNI infrastructure in the webapp.
     */
    public void graphicsInitialize() throws Exception;
    
    /**
     * Get the file extension appropriate for files of this type
     */
    public String getDefaultFileExtension();

    /**
     * Create a round robin database definition from the supplied parameters.
     * This definition is used in the createFile call to create the actual file.
     * 
     * @param creator -
     *            A string representing who is creating this file for use in log
     *            msgs
     * @param directory -
     *            The directory to create the file in
     * @param rrdName -
     *            The name to use for the round robin database
     * @param step -
     *            the step for the database
     * @param dataSources -
     *            the data sources to use for round robin database
     * @param rraList -
     *            a List of the round robin archives to create in the database
     * @return an object representing the definition of an round robin database
     * @throws Exception
     *             If an error occurs while creating the definition
     */
    public D createDefinition(String creator, String directory, String rrdName, int step, List<RrdDataSource> dataSources, List<String> rraList) throws Exception;

    /**
     * Creates the round robin database defined by the supplied definition.
     * 
     * @param rrdDef
     *            an round robin database definition created using the
     *            createDefinition call.
     * @throws Exception
     *             if an error occurs create the file
     */
    public void createFile(D rrdDef) throws Exception;

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
    public F openFile(String fileName) throws Exception;

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
    public void updateFile(F rrd, String owner, String data) throws Exception;

    /**
     * This closes the supplied round robin database
     * 
     * @param rrd
     *            an rrd object created using openFile
     * @throws Exception
     *             if an error occurs closing the file
     */
    public void closeFile(F rrd) throws Exception;

    /**
     * Fetches the last value from the round robin database with the given name.
     * The interval passed in should be the interval associated with the round
     * robin database.
     * 
     * @param rrdFile
     *            a name the represents a round robin database
     * @param ds
     *            a name the represents a data source to be used
     * @param interval
     *            a step interval of the round robin database
     * @return The last value as a Double (if the last value didn't exist
     *         returns a Double.NaN)
     * @throws NumberFormatException
     * @throws RrdException
     */
    public Double fetchLastValue(String rrdFile, String ds, int interval) throws NumberFormatException, RrdException;

    
    /**
     * Fetches the last value from the round robin database with the given name.
     * The interval passed in should be the interval associated with the round
     * robin database.
     * 
     * @param rrdFile
     *            a name the represents a round robin database
     * @param ds
     *            a name the represents a data source to be used
     * @param interval
     *            a step interval of the round robin database
     * @return The last value as a Double (if the last value didn't exist
     *         returns a Double.NaN)
     * @throws NumberFormatException
     * @throws RrdException
     */
    public Double fetchLastValue(String rrdFile, String ds, String consolidationFunction, int interval) throws NumberFormatException, RrdException;

    /**
     * Fetches the last value from the round robin database with the given name
     * within a time range. The interval passed in should be the interval 
     * associated with the round robin database. The range should be the amount of
     * "lag" acceptable for an update to be considered valid. Range must be a 
     * multiple of the RRD interval.
     * 
     * @param rrdFile
     *            a name the represents a round robin database
     * @param ds
     *            a name the represents a data source to be used
     * @param interval
     *            a step interval of the round robin database
     * @param range
     *            an acceptable range for which the last value will be returned
     * @return The last value as a Double (if the last value didn't exist
     *         returns a Double.NaN)
     * @throws NumberFormatException
     * @throws RrdException
     */
    public Double fetchLastValueInRange(String rrdFile, String ds, int interval, int range) throws NumberFormatException, RrdException;
    
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
     * Creates an RrdGraphDetails object representing the graph created from
     * round robin data. It accepts an rrdtool graph command. The underlying
     * implementation converts this command to a format appropriate for it .
     * 
     * @param command
     *            the command needed to create the graph
     * @param workDir
     *            the directory that all referenced files are relative to
     * @return details for the graph including an InputStream, any PRINTed
     *      lines, and graph dimensions.
     * @throws IOException
     *             if an IOError occurs
     * @throws RrdException
     *             if an RRD error occurs
     */
    public RrdGraphDetails createGraphReturnDetails(String command, File workDir) throws IOException, RrdException;
    
    
    /**
     * Returns the number of pixels that the leftt-hand side of the graph is
     * offset from the left side of the created image.  The offset should
     * always be positive.
     *  
     * @return offset in pixels.  Should always be positive.
     */
    public int getGraphLeftOffset();
    
    /**
     * Returns the number of pixels that the right-hand side of the graph is
     * offset from the right side of the created image.  The offset should
     * always be negative.
     *  
     * @return offset in pixels.  Should always be negative.
     */
    public int getGraphRightOffset();
    
    /**
     * Returns the number of pixels that the top of the graph is offset from
     * the top of the created image if there is single line of header text.
     * The offset should always be negative.
     *  
     * @return offset in pixels.  Should always be negative.
     */
    public int getGraphTopOffsetWithText();

    /**
     * Provides the round robin database an opportunity to contribute statistics
     * information to the logs file.
     * 
     * @return a non-null string representing any statistics to be included in
     *         the logs
     */
    public String getStats();

    /**
     * In the event that this is a queuing implementation of the RrdStrategy. This method
     * causes all queued but not yet written data to be to the rrd files as soon as possible.
     * 
     * @param files collection of files whose data should be written
     */
    public void promoteEnqueuedFiles(Collection<String> rrdFiles);
}
