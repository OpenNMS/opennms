//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2004 The OpenNMS Group, Inc.  All rights reserved.
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
//      http://www.blast.com/
//
// Tab Size = 8
//

package org.opennms.netmgt.rrd;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Category;
import org.apache.log4j.Priority;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.web.Util;




/**
 * Provides an rrdtool based implementation of RrdStrategy.  It uses the existing
 * JNI based single-threaded interface to write the rrdtool compatibile RRD files.
 * 
 * The JNI interface takes command-like arguments and doesn't provide open files so
 * the the Objects used to represent open files are really partial command strings
 * 
 * See the individual methods for more details
 */
class JniRrdStrategy implements RrdStrategy {
    
    boolean initialized = false;
    boolean graphicsInitialized = false;
    
    /**
     * The 'closes' the rrd file.  This is where the actual work of writing the RRD
     * files takes place.  The passed in rrd is actually an rrd command string containing
     * updates.  This method executes this command. 
     */
    public void closeFile(Object rrd) throws Exception {
        checkState("closeFile");
        String[] results = Interface.launch(rrd.toString());
        if (results[0] != null) { throw new Exception(results[0]); }
    }

    /**
     * Ensures that the initialize method has been called.
     * @param methodName the name of the method we are called from
     * @throws IllegalState exception of intialize has not been called.
     */
    private void checkState(String methodName) {
        if (!initialized) throw new IllegalStateException("the "+methodName+" method cannot be called before initialize");
    }

    /**
     * Constructs an rrdtool create command string that can be used to create the rrd file
     * and returns it as the rrdDefinition object.
     * 
     */
    public Object createDefinition(String creator, String directory, String dsName, int step, String dsType, int dsHeartbeat, String dsMin,
            String dsMax, List rraList) throws Exception {
        
        checkState("createDefinition");
        
        File f = new File(directory);
        f.mkdirs();
        
        String fileName = directory+File.separator+dsName+".rrd";

        StringBuffer createCmd = new StringBuffer("create");

        createCmd.append(' ' + fileName);
        
        createCmd.append(" --start="+(System.currentTimeMillis()/1000L - 10L));

        createCmd.append(" --step=" + step);

        createCmd.append(" DS:" + dsName + ":"+dsType+":" + dsHeartbeat + ':' + dsMin + ':' + dsMax);
        
        for (Iterator it = rraList.iterator(); it.hasNext();) {
            String rra = (String) it.next();
            createCmd.append(' ');
            createCmd.append(rra);
        }

        return createCmd.toString();
    }

    /**
     * Creates a the rrd file from the rrdDefinition.  Since this definition is really 
     * just the create command string it just executes it.
     */
    public void createFile(Object rrdDef) throws Exception {
        checkState("createFile");
        Interface.launch((String) rrdDef);
    }

    /**
     * The 'opens' the given rrd file.  In actuality since the JNI interface does not
     * provide files that may be open, this constructs the beginning portion of the rrd
     * command to update the file.
     */
    public Object openFile(String fileName) throws Exception {
        checkState("openFile");
        return new StringBuffer("update " + fileName);
    }

    /**
     * This 'updates' the given rrd file by providing data.  Since the JNI interface
     * does not provide files that can be open, this just appends the data to the command
     * string constructed so far.  The data is not immediately written to the file since
     * this would eliminate the possibility of getting performance benefit by doing more
     * than one write per open.  The updates are all performed at once in the closeFile
     * method. 
     */
    public void updateFile(Object rrd, String data) throws Exception {
        checkState("updateFile");
        StringBuffer cmd = (StringBuffer)rrd;
        cmd.append(' ');
        cmd.append(data);
    }

    /**
     * Initialized the JNI Interface
     */
    public void initialize() throws Exception {
        Interface.init();
        initialized = true;
    }
    
   

    /* (non-Javadoc)
     * @see org.opennms.netmgt.rrd.RrdStrategy#graphicsInitialize()
     */
    public void graphicsInitialize() throws Exception {
        // nothing to do here
    }
    /**
     * Fetches the last value directly from the rrd file using the JNI Interface.
     */
    public Double fetchLastValue(String rrdFile, int interval) throws NumberFormatException, RrdException {
        checkState("fetchLastValue");
        // Log4j category
        //
        Category log = ThreadCategory.getInstance(getClass());

        // Generate rrd_fetch() command through jrrd JNI interface in order to retrieve
        // LAST pdp for the datasource stored in the specified RRD file
        //
        // String array returned from launch() native method format:
        //  String[0] - If success is null, otherwise contains reason for failure
        //  String[1] - All data source names contained in the RRD (space delimited)
        //  String[2]...String[n] - RRD fetch data in the following format:
        //      <timestamp> <value1> <value2> ... <valueX> where X is
        //          the total number of data sources
        //
        // NOTE: Specifying start time of 'now-<interval>' and
        //        end time of 'now-<interval>' where <interval> is the
        //    configured thresholding interval (and should be the
        //    same as the RRD step size) in order to guarantee that
        //        we don't get a 'NaN' value from the fetch command. This
        //    is necessary because the collection is being done by collectd
        //    and there is nothing keeping us in sync.
        // 
        //    interval argument is in milliseconds so must convert to seconds
        //
        String fetchCmd = "fetch " + rrdFile + " AVERAGE -s now-" + interval / 1000 + " -e now-" + interval / 1000;

        if (log.isDebugEnabled()) log.debug("fetch: Issuing RRD command: " + fetchCmd);

        String[] fetchStrings = Interface.launch(fetchCmd);

        // Sanity check the returned string array
        if (fetchStrings == null) {
            if (log.isEnabledFor(Priority.ERROR)) {
                log.error("fetch: Unexpected error issuing RRD 'fetch' command, no error text available.");
            }
            return null;
        }

        // Check error string at index 0, will be null if 'fetch' was successful
        if (fetchStrings[0] != null) {
            if (log.isEnabledFor(Priority.ERROR)) {
                log.error("fetch: RRD database 'fetch' failed, reason: " + fetchStrings[0]);
            }
            return null;
        }

        // Sanity check
        if (fetchStrings[1] == null || fetchStrings[2] == null) {
            if (log.isEnabledFor(Priority.ERROR)) {
                log.error("fetch: RRD database 'fetch' failed, no data retrieved.");
            }
            return null;
        }

        // String at index 1 contains the RRDs datasource names
        //
        String dsName = fetchStrings[1].trim();

        // String at index 2 contains fetched values for the current time
        // Convert value string into a Double
        //
        Double dsValue = null;
        if (fetchStrings[2].trim().equalsIgnoreCase("nan")) {
            dsValue = new Double(Double.NaN);
        } else {
            try {
                dsValue = new Double(fetchStrings[2].trim());
            } catch (NumberFormatException nfe) {
                if (log.isEnabledFor(Priority.WARN))
                        log.warn("fetch: Unable to convert fetched value (" + fetchStrings[2].trim() + ") to Double for data source " + dsName);
                throw nfe;
            }
        }

        if (log.isDebugEnabled()) log.debug("fetch: fetch successful: " + dsName + "= " + dsValue);

        return dsValue;
    }
    
    /**
     * Executes the given graph comnmand as process with workDir as the current directory.  The
     * output stream of the command (a PNG image) is copied to a the InputStream returned
     * from the method.
     */
    public InputStream createGraph(String command, File workDir) throws IOException, RrdException {
        InputStream tempIn;
        String[] commandArray = Util.createCommandArray( command, '@' );
        Process process = Runtime.getRuntime().exec( commandArray, null, workDir );
        
        ByteArrayOutputStream tempOut = new ByteArrayOutputStream();
        BufferedInputStream in = new BufferedInputStream( process.getInputStream() );
        
        Util.streamToStream( in, tempOut );
        
        in.close();
        tempOut.close();
        
        BufferedReader err = new BufferedReader( new InputStreamReader( process.getErrorStream() ));
        String line = err.readLine();
        StringBuffer buffer = new StringBuffer();
        
        while( line != null ) {
            buffer.append( line );
            line = err.readLine();
        }
        
        if (buffer.length() > 0) {
            throw new RrdException(buffer.toString());
        }
        
        byte[] byteArray = tempOut.toByteArray();
        tempIn = new ByteArrayInputStream( byteArray );
        return tempIn;
    }


    /**
     * No stats are kept for this implementation.
     */
    public String getStats() {
        return "";
    }
}