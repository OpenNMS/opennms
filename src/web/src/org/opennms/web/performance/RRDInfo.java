//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 Blast Internet Services, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of Blast Internet Services, Inc.
//
// Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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

package org.opennms.web.performance;

import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;


/**
 * A data structure encapsulating information about a single
 * round robin database (RRD).
 *
 * <p>This class holds all the data from the <code>rrdtool info</code>
 * command in a single, object-oriented data structure/JavaBean.  To 
 * create an instance of this object, call the static
 * {@link #createRRDInfo createRRDInfo} method with the output from
 * an <code> rrdtool info</code> command.</p>
 *
 * <p>This data usually looks something like:
 * <pre>
 *   filename = "129.1.1.208-localhost.rrd"
 *   rrd_version = "0001"
 *   step = 300
 *   last_update = 982079322
 *   ds[EXPANSION_COUNTER_1].type = "COUNTER"
 *   ds[EXPANSION_COUNTER_1].minimal_heartbeat = 600
 *   ds[EXPANSION_COUNTER_1].min = NaN
 *   ds[EXPANSION_COUNTER_1].max = NaN
 *   ds[EXPANSION_COUNTER_1].last_ds = "U"
 *   ds[EXPANSION_COUNTER_1].value = 0.0000000000e+00
 *   ds[EXPANSION_COUNTER_1].unknown_sec = 222
 * </pre>
 * but with many more <em>ds</em> (data source) lines and several
 * <em>rra</a> (round robin archive) lines.</p>
 *
 * <p>This class stores the general (filename, rrd version, etc) attributes
 * and the data source attributes in an object-oriented way, but the RRA 
 * information is stored only in the raw values properties object.  This class
 * could very simply be extended, however, to include the RRA information as 
 * well.</p>
 *
 * @author <a href="mailto:larry@opennms.org">Lawrence Karnowski</a>
 * @author <a href="http://www.opennms.org">OpenNMS</a>
 *
 * @see PerformanceModel#getRRDInfo
 */
public class RRDInfo extends Object
{
    /** 
     * Internal class to store information about a single RRD data source, 
     * which is similar to a column in a relational database.
     */
    public static class DataSource extends Object {
        protected String name;
        protected String type;
        protected double minimalHeartbeat;
        protected double min;
        protected double max;
        protected String lastDataSource;
        protected double value;
        protected double unknownSeconds;  //should be int instead?

        public String getName() {
            return( this.name );
        }

        public String getType() {
            return( this.type );
        }

        public double getMinimalHeartbeat() {
            return( this.minimalHeartbeat );
        }

        public double getMin() {
            return( this.min );
        }

        public double getMax() {
            return( this.max );
        }

        public String getLastDataSource() {
            return( this.lastDataSource );
        }

        public double getValue() {
            return( this.value );
        }

        public double getUnknownSeconds() {
            return( this.unknownSeconds );
        }

        public String toString() {
            return( this.name );
        }
    }


    /** The filname of the round robin database (RRD). */
    protected String filename;
    
    /** The version of <code>rrdtool</code> that created the round robin database. */
    protected String rrdVersion;
    
    /** 
     * The elapsed time in seconds that the RRD expects a new update.  For
     * example, if the step is 300, then the RRD is expecting a new update
     * every 5 minutes.
     */
    protected int step;
    
    /** The last time this RRD file was updated. */
    protected Date lastUpdate;
    
    /** 
     * The lines from the RRD info string as name-value pairs. Note this is 
     * currently the only way to retrieve the RRA information.
     */
    protected Properties rawValues;    
    
    /** The array of data sources (similar to relational database columns). */
    protected DataSource[] dataSources;
    
    /** A mapping of data source names to <code>DataSource</code> instances. */
    protected Map dataSourcesMap;
    

    public String getFilename() {
        return( this.filename );
    }


    public String getRRDVersion() {
        return( this.rrdVersion );
    }


    public int getStep() {
        return( this.step );
    }


    /** @deprecated Use getLastUpdate().getTime() instead. */
    public long getLastUpdateLong() {
        long lastUpdateLong = 0;
        
        if( this.lastUpdate != null ) {
            lastUpdateLong = this.lastUpdate.getTime();
        }
        
        return( lastUpdateLong );
    }


    public Date getLastUpdate() {
        return( this.lastUpdate );
    }


    public DataSource[] getDataSources() {
        return( this.dataSources );
    }


    public DataSource getDataSource( String ds ) {
        return( (DataSource)this.dataSourcesMap.get( ds ));
    }


    public String getRawValue( String property ) {
        return( this.rawValues.getProperty( property ));
    }


    public Properties getRawValues() {
        return( (Properties)this.rawValues.clone() );
    }


    /**
     * Parses the output from a <code>rrdtool info</code> command
     * to create the <code>RRDInfo</code> data structure.
     *
     * @return An <code>RRDInfo</code> object that encapsulates the data
     * given in the <code>infoString</code> parameter.  If the <code>infoString</code>
     * parameter does not contain a parseable info string, this method will return null.
     */
    public static RRDInfo createRRDInfo( int nodeId, String infoString ) {
        if( infoString == null ) {
            throw new IllegalArgumentException( "Cannot take null parameters." );
        }

        Properties props = new Properties();

        //load the infoString as a properties list
        try {
            props.load( new ByteArrayInputStream( infoString.getBytes() ));
        }
        catch( IOException e ) {
            //since we are using a string buffer, this exception should never
            //be thrown, but just in case convert it to a runtime exception
            throw new IllegalStateException( "Could not load the rrd info string into a properties list" );
        }

        //start filling in some values
        RRDInfo info = new RRDInfo();
        info.rawValues = props;
        info.filename = String.valueOf(nodeId) + java.io.File.separator + props.getProperty( "filename" );
        info.rrdVersion = props.getProperty( "rrd_version" );

        //if it doesn't have the filename or the rrdversion, this wasn't an rrdtool info string
        if( info.filename == null || info.rrdVersion == null ) {
            return null;
        }

        //since we're pretty sure we're really looking at an rrdtool info string,
        //these values should now be safe to populate too
        info.step = Integer.parseInt( props.getProperty( "step" ) );
        long lastUpdateLong = Long.parseLong( props.getProperty( "last_update" ) );
        info.lastUpdate = new Date( lastUpdateLong*1000 );

        //find the unique names of the data sources
        ArrayList dsNames = new ArrayList();
        StringTokenizer tokenizer = new StringTokenizer( infoString, System.getProperty( "line.separator" ));

        while( tokenizer.hasMoreTokens() ) {
            String line = tokenizer.nextToken();
            
            if( line.startsWith( "ds" ) ) {
                String dsname = line.substring( line.indexOf("[")+1, line.indexOf("]") );
                if( !dsNames.contains( dsname ) ) {
                    dsNames.add( dsname );
                }
            }
        }

        //populate the data sources list and map
        DataSource[] dslist = new DataSource[dsNames.size()];
        HashMap dsmap = new HashMap();        
        for( int i=0; i < dslist.length; i++ ) {
            String ds = (String)dsNames.get(i);

            dslist[i] = new DataSource();
            dslist[i].name = ds;
            dslist[i].type = props.getProperty( "ds[" + ds + "].type" );
            dslist[i].minimalHeartbeat = parseDouble( props.getProperty( "ds[" + ds + "].minimal_heartbeat" ));
            dslist[i].min = parseDouble( props.getProperty( "ds[" + ds + "].min" ));
            dslist[i].max = parseDouble( props.getProperty( "ds[" + ds + "].max" ));
            dslist[i].lastDataSource = props.getProperty( "ds[" + ds + "].last_ds" );
            dslist[i].value = parseDouble( props.getProperty( "ds[" + ds + "].value" ));
            dslist[i].unknownSeconds = parseDouble( props.getProperty( "ds[" + ds + "].unknown_sec" ));

            dsmap.put( ds, dslist[i] );
        }
        
        //fill the datasources into the rrdinfo object
        info.dataSources = dslist;
        info.dataSourcesMap = dsmap;

        return( info );
    }


    /** 
     * Convenience method that parses a double or returns a 
     * {@link Double#NaN NaN} (not a number) double value if
     * the string cannot be parsed to a double.
     */
    protected static double parseDouble( String s ) {
        double value = Double.NaN;

        try {
            value = Double.parseDouble( s );
        }
        catch( NumberFormatException e ) {
            //if we catch a number format exception, just
            //leave the value as NaN (not a number)
        }

        return value;
    }

}
