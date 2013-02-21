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

package org.opennms.netmgt.rrd.jrobin;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.jrobin.core.FetchData;
import org.jrobin.core.RrdDb;
import org.jrobin.core.RrdDef;
import org.jrobin.core.RrdException;
import org.jrobin.core.Sample;
import org.jrobin.data.DataProcessor;
import org.jrobin.data.Plottable;
import org.jrobin.graph.RrdGraph;
import org.jrobin.graph.RrdGraphDef;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.rrd.RrdDataSource;
import org.opennms.netmgt.rrd.RrdGraphDetails;
import org.opennms.netmgt.rrd.RrdStrategy;
import org.opennms.netmgt.rrd.RrdUtils;


/**
 * Provides a JRobin based implementation of RrdStrategy. It uses JRobin 1.4 in
 * FILE mode (NIO is too memory consuming for the large number of files that we
 * open)
 *
 * @author ranger
 * @version $Id: $
 */
public class JRobinRrdStrategy implements RrdStrategy<RrdDef,RrdDb> {
    private static final String BACKEND_FACTORY_PROPERTY = "org.jrobin.core.RrdBackendFactory";
    private static final String DEFAULT_BACKEND_FACTORY = "FILE";

    /*
     * Ensure that we only initialize certain things *once* per
     * Java VM, not once per instantiation of this class.
     */
    private static boolean s_initialized = false;

    private Properties m_configurationProperties;
    
    /**
     * An extremely simple Plottable for holding static datasources that
     * can't be represented with an SDEF -- currently used only for PERCENT
     * pseudo-VDEFs
     * 
     * @author jeffg
     *
     */
    class ConstantStaticDef extends Plottable {
        private double m_startTime = Double.NEGATIVE_INFINITY;
        private double m_endTime = Double.POSITIVE_INFINITY;
        private double m_value = Double.NaN;
        
        ConstantStaticDef(long startTime, long endTime, double value) {
            m_startTime = startTime;
            m_endTime = endTime;
            m_value = value;
        }
        
        public double getValue(long timestamp) {
            if (m_startTime <= timestamp && m_endTime >= timestamp) {
                return m_value;
            } else {
                return Double.NaN;
            }
        }
    }

    /**
     * <p>getConfigurationProperties</p>
     *
     * @return a {@link java.util.Properties} object.
     */
    public Properties getConfigurationProperties() {
        return m_configurationProperties;
    }

    /** {@inheritDoc} */
    public void setConfigurationProperties(final Properties configurationParameters) {
        m_configurationProperties = configurationParameters;
        if(!s_initialized) {
            String factory = null;
            if (m_configurationProperties == null) {
                factory = DEFAULT_BACKEND_FACTORY;
            } else {
                factory = (String)m_configurationProperties.get(BACKEND_FACTORY_PROPERTY);
            }
            try {
                RrdDb.setDefaultFactory(factory);
                s_initialized=true;
            } catch (RrdException e) {
                log().error("Could not set default JRobin RRD factory: " + e.getMessage(), e);
            }
        }
    }

    /**
     * Closes the JRobin RrdDb.
     *
     * @param rrdFile a {@link org.jrobin.core.RrdDb} object.
     * @throws java.lang.Exception if any.
     */
    public void closeFile(final RrdDb rrdFile) throws Exception {
        rrdFile.close();
    }

    /** {@inheritDoc} */
	public RrdDef createDefinition(final String creator,
			final String directory, final String rrdName, int step,
			final List<RrdDataSource> dataSources, final List<String> rraList) throws Exception {
        File f = new File(directory);
        f.mkdirs();

        String fileName = directory + File.separator + rrdName + RrdUtils.getExtension();
        
        if (new File(fileName).exists()) {
			log().debug(
					"createDefinition: filename [" + fileName
							+ "] already exists returning null as definition");
            return null;
        }

        RrdDef def = new RrdDef(fileName);

        // def.setStartTime(System.currentTimeMillis()/1000L - 2592000L);
        def.setStartTime(1000);
        def.setStep(step);
        
        for (RrdDataSource dataSource : dataSources) {
            String dsMin = dataSource.getMin();
            String dsMax = dataSource.getMax();
            double min = (dsMin == null || "U".equals(dsMin) ? Double.NaN : Double.parseDouble(dsMin));
            double max = (dsMax == null || "U".equals(dsMax) ? Double.NaN : Double.parseDouble(dsMax));
            def.addDatasource(dataSource.getName(), dataSource.getType(), dataSource.getHeartBeat(), min, max);
        }

        for (String rra : rraList) {
            def.addArchive(rra);
        }

        return def;
    }


    /**
     * Creates the JRobin RrdDb from the def by opening the file and then
     * closing.
     *
     * @param rrdDef a {@link org.jrobin.core.RrdDef} object.
     * @throws java.lang.Exception if any.
     */
    public void createFile(final RrdDef rrdDef,  Map<String, String> attributeMappings) throws Exception {
        if (rrdDef == null) {
        	log().debug("createRRD: skipping RRD file");
            return;
        }
        log().info("createRRD: creating RRD file " + rrdDef.getPath());
        
        RrdDb rrd = new RrdDb(rrdDef);
        rrd.close();
        
        String filenameWithoutExtension = rrdDef.getPath().replace(RrdUtils.getExtension(), "");
        int lastIndexOfSeparator = filenameWithoutExtension.lastIndexOf(File.separator);
        
		RrdUtils.createMetaDataFile(
				filenameWithoutExtension.substring(0, lastIndexOfSeparator),
				filenameWithoutExtension.substring(lastIndexOfSeparator),
				attributeMappings);
    }

    /**
     * {@inheritDoc}
     *
     * Opens the JRobin RrdDb by name and returns it.
     */
    public RrdDb openFile(final String fileName) throws Exception {
        RrdDb rrd = new RrdDb(fileName);
        return rrd;
    }

    /**
     * {@inheritDoc}
     *
     * Creates a sample from the JRobin RrdDb and passes in the data provided.
     */
    public void updateFile(final RrdDb rrdFile, final String owner, final String data) throws Exception {
        Sample sample = rrdFile.createSample();
        sample.setAndUpdate(data);
    }

    /**
     * Initialized the RrdDb to use the FILE factory because the NIO factory
     * uses too much memory for our implementation.
     *
     * @throws java.lang.Exception if any.
     */
    public JRobinRrdStrategy() throws Exception {
        String home = System.getProperty("opennms.home");
        System.setProperty("jrobin.fontdir", home + File.separator + "etc");
    }

    /**
     * {@inheritDoc}
     *
     * Fetch the last value from the JRobin RrdDb file.
     */
    public Double fetchLastValue(final String fileName, final String ds, final int interval) throws NumberFormatException, org.opennms.netmgt.rrd.RrdException {
        return fetchLastValue(fileName, ds, "AVERAGE", interval);
    }

    /** {@inheritDoc} */
    public Double fetchLastValue(final String fileName, final String ds, final String consolidationFunction, final int interval)
            throws org.opennms.netmgt.rrd.RrdException {
        RrdDb rrd = null;
        try {
            long now = System.currentTimeMillis();
            long collectTime = (now - (now % interval)) / 1000L;
            rrd = new RrdDb(fileName);
            FetchData data = rrd.createFetchRequest(consolidationFunction, collectTime, collectTime).fetchData();
            if(log().isDebugEnabled()) {
            	//The "toString" method of FetchData is quite computationally expensive; 
            	log().debug(data.toString());
            }
            double[] vals = data.getValues(ds);
            if (vals.length > 0) {
                return new Double(vals[vals.length - 1]);
            }
            return null;
        } catch (IOException e) {
            throw new org.opennms.netmgt.rrd.RrdException("Exception occurred fetching data from " + fileName, e);
        } catch (RrdException e) {
            throw new org.opennms.netmgt.rrd.RrdException("Exception occurred fetching data from " + fileName, e);
        } finally {
            if (rrd != null) {
                try {
                    rrd.close();
                } catch (IOException e) {
                    log().error("Failed to close rrd file: " + fileName, e);
                }
            }
        }
    }
    
    /** {@inheritDoc} */
    public Double fetchLastValueInRange(final String fileName, final String ds, final int interval, final int range) throws NumberFormatException, org.opennms.netmgt.rrd.RrdException {
        RrdDb rrd = null;
        try {
        	rrd = new RrdDb(fileName);
         	long now = System.currentTimeMillis();
            long latestUpdateTime = (now - (now % interval)) / 1000L;
            long earliestUpdateTime = ((now - (now % interval)) - range) / 1000L;
            if (log().isDebugEnabled()) {
            	log().debug("fetchInRange: fetching data from " + earliestUpdateTime + " to " + latestUpdateTime);
            }
            
            FetchData data = rrd.createFetchRequest("AVERAGE", earliestUpdateTime, latestUpdateTime).fetchData();
            
		    double[] vals = data.getValues(ds);
		    long[] times = data.getTimestamps();
		    
		    // step backwards through the array of values until we get something that's a number
            
		    for(int i = vals.length - 1; i >= 0; i--) {
            	if ( Double.isNaN(vals[i]) ) {
            		if (log().isDebugEnabled()) {
            			log().debug("fetchInRange: Got a NaN value at interval: " + times[i] + " continuing back in time");
            		}
            	} else {
               		if (log().isDebugEnabled()) {
               			log().debug("Got a non NaN value at interval: " + times[i] + " : " + vals[i] );
               		}
            		return new Double(vals[i]);
               	}
            }
            return null;
        } catch (IOException e) {
            throw new org.opennms.netmgt.rrd.RrdException("Exception occurred fetching data from " + fileName, e);
        } catch (RrdException e) {
            throw new org.opennms.netmgt.rrd.RrdException("Exception occurred fetching data from " + fileName, e);
        } finally {
            if (rrd != null) {
                try {
                    rrd.close();
                } catch (IOException e) {
                    log().error("Failed to close rrd file: " + fileName, e);
                }
            }
        }
    }

    private Color getColor(final String colorValue) {
        int rVal = Integer.parseInt(colorValue.substring(0, 2), 16);
        int gVal = Integer.parseInt(colorValue.substring(2, 4), 16);
        int bVal = Integer.parseInt(colorValue.substring(4, 6), 16);
        if (colorValue.length() == 6) {
            return new Color(rVal, gVal, bVal);
        }

        int aVal = Integer.parseInt(colorValue.substring(6, 8), 16);
        return new Color(rVal, gVal, bVal, aVal);
    }

    // For compatibility with RRDtool defs, the colour value for
    // LINE and AREA is optional. If it's missing, the line is rendered
    // invisibly.
    private Color getColorOrInvisible(final String[] array, final int index) {
        if (array.length > index) {
            return getColor(array[index]);
        }
        return new Color(1.0f, 1.0f, 1.0f, 0.0f);
    }

    /** {@inheritDoc} */
    public InputStream createGraph(final String command, final File workDir) throws IOException, org.opennms.netmgt.rrd.RrdException {
        return createGraphReturnDetails(command, workDir).getInputStream();
    }

    /**
     * {@inheritDoc}
     *
     * This constructs a graphDef by parsing the rrdtool style command and using
     * the values to create the JRobin graphDef. It does not understand the 'AT
     * style' time arguments however. Also there may be some rrdtool parameters
     * that it does not understand. These will be ignored. The graphDef will be
     * used to construct an RrdGraph and a PNG image will be created. An input
     * stream returning the bytes of the PNG image is returned.
     */
    public RrdGraphDetails createGraphReturnDetails(final String command, final File workDir) throws IOException, org.opennms.netmgt.rrd.RrdException {

        try {
            String[] commandArray = tokenize(command, " \t", false);

            RrdGraphDef graphDef = createGraphDef(workDir, commandArray);
            graphDef.setSignature("OpenNMS/JRobin");

            RrdGraph graph = new RrdGraph(graphDef);

            /*
             * We use a custom RrdGraphDetails object here instead of the
             * DefaultRrdGraphDetails because we won't have an InputStream
             * available if no graphing commands were used, e.g.: if we only
             * use PRINT or if the user goofs up a graph definition.
             * 
             * We want to throw an RrdException if the caller calls
             * RrdGraphDetails.getInputStream and no graphing commands were
             * used.  If they just call RrdGraphDetails.getPrintLines, though,
             * we don't want to throw an exception.
             */
            return new JRobinRrdGraphDetails(graph, command);
        } catch (Throwable e) {
            log().error("JRobin: exception occurred creating graph: " + e.getMessage(), e);
            throw new org.opennms.netmgt.rrd.RrdException("An exception occurred creating the graph: " + e.getMessage(), e);
        }
    }
    
    /** {@inheritDoc} */
    public void promoteEnqueuedFiles(Collection<String> rrdFiles) {
        // no need to do anything since this strategy doesn't queue
    }


    /**
     * <p>createGraphDef</p>
     *
     * @param workDir a {@link java.io.File} object.
     * @param commandArray an array of {@link java.lang.String} objects.
     * @return a {@link org.jrobin.graph.RrdGraphDef} object.
     * @throws org.jrobin.core.RrdException if any.
     */
    protected RrdGraphDef createGraphDef(final File workDir, final String[] inputArray) throws RrdException {
        RrdGraphDef graphDef = new RrdGraphDef();
        graphDef.setImageFormat("PNG");
        long start = 0;
        long end = 0;
        int height = 100;
        int width = 400;
        double lowerLimit = Double.NaN;
        double upperLimit = Double.NaN;
        boolean rigid = false;
        Map<String,List<String>> defs = new LinkedHashMap<String,List<String>>();
        // Map<String,List<String>> cdefs = new HashMap<String,List<String>>();
        
        final String[] commandArray;
        if (inputArray[0].contains("rrdtool") && inputArray[1].equals("graph") && inputArray[2].equals("-")) {
        	commandArray = Arrays.copyOfRange(inputArray, 3, inputArray.length);
        } else {
        	commandArray = inputArray;
        }
        
        log().debug("command array = " + Arrays.toString(commandArray));

        for (int i = 0; i < commandArray.length; i++) {
            String arg = commandArray[i];
            if (arg.startsWith("--start=")) {
                start = Long.parseLong(arg.substring("--start=".length()));
                log().debug("JRobin start time: " + start);
            } else if (arg.equals("--start")) {
                if (i + 1 < commandArray.length) {
                    start = Long.parseLong(commandArray[++i]);
                    log().debug("JRobin start time: " + start);
                } else {
                    throw new IllegalArgumentException("--start must be followed by a start time");
                }
                
            } else if (arg.startsWith("--end=")) {
                end = Long.parseLong(arg.substring("--end=".length()));
                log().debug("JRobin end time: " + end);
            } else if (arg.equals("--end")) {
                if (i + 1 < commandArray.length) {
                    end = Long.parseLong(commandArray[++i]);
                    log().debug("JRobin end time: " + end);
                } else {
                    throw new IllegalArgumentException("--end must be followed by an end time");
                }
                
            } else if (arg.startsWith("--title=")) {
                String[] title = tokenize(arg, "=", true);
                graphDef.setTitle(title[1]);
            } else if (arg.equals("--title")) {
                if (i + 1 < commandArray.length) {
                    graphDef.setTitle(commandArray[++i]);
                } else {
                    throw new IllegalArgumentException("--title must be followed by a title");
                }
                
            } else if (arg.startsWith("--color=")) {
                String[] color = tokenize(arg, "=", true);
                parseGraphColor(graphDef, color[1]);
            } else if (arg.equals("--color") || arg.equals("-c")) {
                if (i + 1 < commandArray.length) {
                    parseGraphColor(graphDef, commandArray[++i]);
                } else {
                    throw new IllegalArgumentException("--color must be followed by a color");
                }
                
            } else if (arg.startsWith("--vertical-label=")) {
                String[] label = tokenize(arg, "=", true);
                graphDef.setVerticalLabel(label[1]);
            } else if (arg.equals("--vertical-label")) {
                if (i + 1 < commandArray.length) {
                    graphDef.setVerticalLabel(commandArray[++i]);
                } else {
                    throw new IllegalArgumentException("--vertical-label must be followed by a label");
                }
                
            } else if (arg.startsWith("--height=")) {
                String[] argParm = tokenize(arg, "=", true);
                height = Integer.parseInt(argParm[1]);
                log().debug("JRobin height: "+height);
            } else if (arg.equals("--height")) {
                if (i + 1 < commandArray.length) {
                    height = Integer.parseInt(commandArray[++i]);
                    log().debug("JRobin height: "+height);
                } else {
                    throw new IllegalArgumentException("--height must be followed by a number");
                }
            
            } else if (arg.startsWith("--width=")) {
                String[] argParm = tokenize(arg, "=", true);
                width = Integer.parseInt(argParm[1]);
                log().debug("JRobin width: "+width);
            } else if (arg.equals("--width")) {
                if (i + 1 < commandArray.length) {
                    width = Integer.parseInt(commandArray[++i]);
                    log().debug("JRobin width: "+width);
                } else {
                    throw new IllegalArgumentException("--width must be followed by a number");
                }
            
            } else if (arg.startsWith("--units-exponent=")) {
                String[] argParm = tokenize(arg, "=", true);
                int exponent = Integer.parseInt(argParm[1]);
                log().debug("JRobin units exponent: "+exponent);
                graphDef.setUnitsExponent(exponent);
            } else if (arg.equals("--units-exponent")) {
                if (i + 1 < commandArray.length) {
                    int exponent = Integer.parseInt(commandArray[++i]);
                    log().debug("JRobin units exponent: "+exponent);
                    graphDef.setUnitsExponent(exponent);
                } else {
                    throw new IllegalArgumentException("--units-exponent must be followed by a number");
                }
            
            } else if (arg.startsWith("--lower-limit=")) {
                String[] argParm = tokenize(arg, "=", true);
                lowerLimit = Double.parseDouble(argParm[1]);
                log().debug("JRobin lower limit: "+lowerLimit);
            } else if (arg.equals("--lower-limit")) {
                if (i + 1 < commandArray.length) {
                    lowerLimit = Double.parseDouble(commandArray[++i]);
                    log().debug("JRobin lower limit: "+lowerLimit);
                } else {
                    throw new IllegalArgumentException("--lower-limit must be followed by a number");
                }
            
            } else if (arg.startsWith("--upper-limit=")) {
                String[] argParm = tokenize(arg, "=", true);
                upperLimit = Double.parseDouble(argParm[1]);
                log().debug("JRobin upp limit: "+upperLimit);
            } else if (arg.equals("--upper-limit")) {
                if (i + 1 < commandArray.length) {
                    upperLimit = Double.parseDouble(commandArray[++i]);
                    log().debug("JRobin upper limit: "+upperLimit);
                } else {
                    throw new IllegalArgumentException("--upper-limit must be followed by a number");
                }
            
            } else if (arg.startsWith("--base=")) {
                String[] argParm = tokenize(arg, "=", true);
                graphDef.setBase(Double.parseDouble(argParm[1]));
            } else if (arg.equals("--base")) {
                if (i + 1 < commandArray.length) {
                    graphDef.setBase(Double.parseDouble(commandArray[++i]));
                } else {
                    throw new IllegalArgumentException("--base must be followed by a number");
                }
            
            } else if (arg.startsWith("--font=")) {
            	String[] argParm = tokenize(arg, "=", true);
            	processRrdFontArgument(graphDef, argParm[1]);
            } else if (arg.equals("--font")) {
                if (i + 1 < commandArray.length) {
                	processRrdFontArgument(graphDef, commandArray[++i]);
                } else {
                    throw new IllegalArgumentException("--font must be followed by an argument");
                }
            } else if (arg.startsWith("--imgformat=")) {
            	String[] argParm = tokenize(arg, "=", true);
            	graphDef.setImageFormat(argParm[1]);
            } else if (arg.equals("--imgformat")) {
                if (i + 1 < commandArray.length) {
                	graphDef.setImageFormat(commandArray[++i]);
                } else {
                    throw new IllegalArgumentException("--imgformat must be followed by an argument");
                }
            
            } else if (arg.equals("--rigid")) {
                rigid = true;
            
            } else if (arg.startsWith("DEF:")) {
                String definition = arg.substring("DEF:".length());
                String[] def = splitDef(definition);
                String[] ds = def[0].split("=");
                // log().debug("ds = " + Arrays.toString(ds));
                final String replaced = ds[1].replaceAll("\\\\(.)", "$1");
                // log().debug("replaced = " + replaced);
                
                final File dsFile;
                File rawPathFile = new File(replaced);
                if (rawPathFile.isAbsolute()) {
                	dsFile = rawPathFile;
                } else {
                	dsFile = new File(workDir, replaced);
                }
                // log().debug("dsFile = " + dsFile + ", ds[1] = " + ds[1]);
                
                final String absolutePath = (File.separatorChar == '\\')? dsFile.getAbsolutePath().replace("\\", "\\\\") : dsFile.getAbsolutePath();
                // log().debug("absolutePath = " + absolutePath);
                graphDef.datasource(ds[0], absolutePath, def[1], def[2]);

                List<String> defBits = new ArrayList<String>();
                defBits.add(absolutePath);
                defBits.add(def[1]);
                defBits.add(def[2]);
                defs.put(ds[0], defBits);
            
            } else if (arg.startsWith("CDEF:")) {
                String definition = arg.substring("CDEF:".length());
                String[] cdef = tokenize(definition, "=", true);
                graphDef.datasource(cdef[0], cdef[1]);
                List<String> cdefBits = new ArrayList<String>();
                cdefBits.add(cdef[1]);
                defs.put(cdef[0], cdefBits);
            } else if (arg.startsWith("VDEF:")) {
                String definition = arg.substring("VDEF:".length());
                String[] vdef = tokenize(definition, "=", true);
                String[] expressionTokens = tokenize(vdef[1], ",", false);
                addVdefDs(graphDef, vdef[0], expressionTokens, start, end, defs);
            } else if (arg.startsWith("LINE1:")) {
                String definition = arg.substring("LINE1:".length());
                String[] line1 = tokenize(definition, ":", true);
                String[] color = tokenize(line1[0], "#", true);
                graphDef.line(color[0], getColorOrInvisible(color, 1), (line1.length > 1 ? line1[1] : ""));
            
            } else if (arg.startsWith("LINE2:")) {
                String definition = arg.substring("LINE2:".length());
                String[] line2 = tokenize(definition, ":", true);
                String[] color = tokenize(line2[0], "#", true);
                graphDef.line(color[0], getColorOrInvisible(color, 1), (line2.length > 1 ? line2[1] : ""), 2);

            } else if (arg.startsWith("LINE3:")) {
                String definition = arg.substring("LINE3:".length());
                String[] line3 = tokenize(definition, ":", true);
                String[] color = tokenize(line3[0], "#", true);
                graphDef.line(color[0], getColorOrInvisible(color, 1), (line3.length > 1 ? line3[1] : ""), 3);

            } else if (arg.startsWith("GPRINT:")) {
                String definition = arg.substring("GPRINT:".length());
                String gprint[] = tokenize(definition, ":", true);
                String format = gprint[2];
                //format = format.replaceAll("%(\\d*\\.\\d*)lf", "@$1");
                //format = format.replaceAll("%s", "@s");
                //format = format.replaceAll("%%", "%");
                //log.debug("gprint: oldformat = " + gprint[2] + " newformat = " + format);
                format = format.replaceAll("\\n", "\\\\l");
                graphDef.gprint(gprint[0], gprint[1], format);
                
            } else if (arg.startsWith("PRINT:")) {
                String definition = arg.substring("PRINT:".length());
                String print[] = tokenize(definition, ":", true);
                String format = print[2];
                //format = format.replaceAll("%(\\d*\\.\\d*)lf", "@$1");
                //format = format.replaceAll("%s", "@s");
                //format = format.replaceAll("%%", "%");
                //log.debug("gprint: oldformat = " + print[2] + " newformat = " + format);
                format = format.replaceAll("\\n", "\\\\l");
                graphDef.print(print[0], print[1], format);

            } else if (arg.startsWith("COMMENT:")) {
                String comments[] = tokenize(arg, ":", true);
                String format = comments[1].replaceAll("\\n", "\\\\l");
                graphDef.comment(format);
            } else if (arg.startsWith("AREA:")) {
                String definition = arg.substring("AREA:".length());
                String area[] = tokenize(definition, ":", true);
                String[] color = tokenize(area[0], "#", true);
                if (area.length > 1) {
                    graphDef.area(color[0], getColorOrInvisible(color, 1), area[1]);
                } else {
                    graphDef.area(color[0], getColorOrInvisible(color, 1));
                }

            } else if (arg.startsWith("STACK:")) {
                String definition = arg.substring("STACK:".length());
                String stack[] = tokenize(definition, ":", true);
                String[] color = tokenize(stack[0], "#", true);
                graphDef.stack(color[0], getColor(color[1]), (stack.length > 1 ? stack[1] : ""));

            } else if (arg.startsWith("HRULE:")) {
                String definition = arg.substring("HRULE:".length());
                String hrule[] = tokenize(definition, ":", true);
                String[] color = tokenize(hrule[0], "#", true);
                Double value = Double.valueOf(color[0]);
                graphDef.hrule(value, getColor(color[1]), hrule[1]);
            } else if (arg.endsWith("/rrdtool") || arg.equals("graph") || arg.equals("-")) {
            	// ignore, this is just a leftover from the rrdtool-specific options

            } else {
                log().warn("JRobin: Unrecognized graph argument: " + arg);
            }
        }
        
        graphDef.setTimeSpan(start, end);
        graphDef.setMinValue(lowerLimit);
        graphDef.setMaxValue(upperLimit);
        graphDef.setRigid(rigid);
        graphDef.setHeight(height);
        graphDef.setWidth(width);
        // graphDef.setSmallFont(new Font("Monospaced", Font.PLAIN, 10));
        // graphDef.setLargeFont(new Font("Monospaced", Font.PLAIN, 12));

        log().debug("JRobin Finished tokenizing checking: start time: " + start + "; end time: " + end);
        log().debug("large font = " + graphDef.getLargeFont() + ", small font = " + graphDef.getSmallFont());
        return graphDef;
    }

	private String[] splitDef(final String definition) {
		// log().debug("splitDef(" + definition + ")");
		final String[] def;
		if (File.separatorChar == '\\') {
			// log().debug("windows");
			// Windows, make sure the beginning isn't eg: C:\\foo\\bar
			if (definition.matches("[^=]*=[a-zA-Z]:.*")) {
				final String[] tempDef = definition.split("(?<!\\\\):");
				def = new String[tempDef.length - 1];
				def[0] = tempDef[0] + ':' + tempDef[1];
				if (tempDef.length > 2) {
					for (int i = 2; i < tempDef.length; i++) {
						def[i-1] = tempDef[i];
					}
				}
			} else {
				// log().debug("no match");
				def = definition.split("(?<!\\\\):");
			}
		} else {
			def = definition.split("(?<!\\\\):");
		}
		// log().debug("returning: " + Arrays.toString(def));
		return def;
	}

	private void processRrdFontArgument(RrdGraphDef graphDef, String argParm) {
		/*
		String[] argValue = tokenize(argParm, ":", true);
		if (argValue[0].equals("DEFAULT")) {
			int newPointSize = Integer.parseInt(argValue[1]);
			graphDef.setSmallFont(graphDef.getSmallFont().deriveFont(newPointSize));
		} else if (argValue[0].equals("TITLE")) {
			int newPointSize = Integer.parseInt(argValue[1]);
			graphDef.setLargeFont(graphDef.getLargeFont().deriveFont(newPointSize));
		} else {
			try {
				Font font = Font.createFont(Font.TRUETYPE_FONT, new File(argValue[0]));
			} catch (Throwable e) {
				// oh well, fall back to existing font stuff
				log().warn("unable to create font from font argument " + argParm, e);
			}
		}
		*/
	}
    
    private String[] tokenize(final String line, final String delimiters, final boolean processQuotes) {
        String passthroughTokens = "lcrjgsJ"; /* see org.jrobin.graph.RrdGraphConstants.MARKERS */
        return tokenizeWithQuotingAndEscapes(line, delimiters, processQuotes, passthroughTokens);
    }

    /**
     * @param colorArg Should have the form COLORTAG#RRGGBB[AA]
     * @see http://www.jrobin.org/support/man/rrdgraph.html
     */
    private void parseGraphColor(final RrdGraphDef graphDef, final String colorArg) throws IllegalArgumentException {
        // Parse for format COLORTAG#RRGGBB[AA]
        String[] colorArgParts = tokenize(colorArg, "#", false);
        if (colorArgParts.length != 2) {
            throw new IllegalArgumentException("--color must be followed by value with format COLORTAG#RRGGBB[AA]");
        }

        String colorTag = colorArgParts[0].toUpperCase();
        String colorHex = colorArgParts[1].toUpperCase();

        // validate hex color input is actually an RGB hex color value
        if (colorHex.length() != 6 && colorHex.length() != 8) {
            throw new IllegalArgumentException("--color must be followed by value with format COLORTAG#RRGGBB[AA]");
        }

        // this might throw NumberFormatException, but whoever wrote
        // createGraph didn't seem to care, so I guess I don't care either.
        // It'll get wrapped in an RrdException anyway.
        Color color = getColor(colorHex);

        // These are the documented RRD color tags
        try {
            if (colorTag.equals("BACK")) {
                graphDef.setColor("BACK", color);
            }
            else if (colorTag.equals("CANVAS")) {
                graphDef.setColor("CANVAS", color);
            }
            else if (colorTag.equals("SHADEA")) {
                graphDef.setColor("SHADEA", color);
            }
            else if (colorTag.equals("SHADEB")) {
                graphDef.setColor("SHADEB", color);
            }
            else if (colorTag.equals("GRID")) {
                graphDef.setColor("GRID", color);
            }
            else if (colorTag.equals("MGRID")) {
                graphDef.setColor("MGRID", color);
            }
            else if (colorTag.equals("FONT")) {
                graphDef.setColor("FONT", color);
            }
            else if (colorTag.equals("FRAME")) {
                graphDef.setColor("FRAME", color);
            }
            else if (colorTag.equals("ARROW")) {
                graphDef.setColor("ARROW", color);
            }
            else {
                throw new org.jrobin.core.RrdException("Unknown color tag " + colorTag);
            }
        } catch (Throwable e) {
            log().error("JRobin: exception occurred creating graph: " + e, e);
        }
    }

    /**
     * This implementation does not track any stats.
     *
     * @return a {@link java.lang.String} object.
     */
    public String getStats() {
        return "";
    }

    /*
     * These offsets work for ranger@ with Safari and JRobin 1.5.8.
     */
    /**
     * <p>getGraphLeftOffset</p>
     *
     * @return a int.
     */
    public int getGraphLeftOffset() {
        return 74;
    }
    
    /**
     * <p>getGraphRightOffset</p>
     *
     * @return a int.
     */
    public int getGraphRightOffset() {
        return -15;
    }

    /**
     * <p>getGraphTopOffsetWithText</p>
     *
     * @return a int.
     */
    public int getGraphTopOffsetWithText() {
        return -61;
    }

    /**
     * <p>getDefaultFileExtension</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getDefaultFileExtension() {
        return ".jrb";
    }

    private final ThreadCategory log() {
        return ThreadCategory.getInstance(getClass());
    }

    /**
     * <p>tokenizeWithQuotingAndEscapes</p>
     *
     * @param line a {@link java.lang.String} object.
     * @param delims a {@link java.lang.String} object.
     * @param processQuoted a boolean.
     * @return an array of {@link java.lang.String} objects.
     */
    public static String[] tokenizeWithQuotingAndEscapes(String line, String delims, boolean processQuoted) {
        return tokenizeWithQuotingAndEscapes(line, delims, processQuoted, "");
    }
    
    /**
     * Tokenize a {@link String} into an array of {@link String}s.
     *
     * @param line
     *          the string to tokenize
     * @param delims
     *          a string containing zero or more characters to treat as a delimiter
     * @param processQuoted
     *          whether or not to process escaped values inside quotes
     * @param tokens
     *          custom escaped tokens to pass through, escaped.  For example, if tokens contains "lsg", then \l, \s, and \g
     *          will be passed through unescaped.
     * @return an array of {@link java.lang.String} objects.
     */
    public static String[] tokenizeWithQuotingAndEscapes(final String line, final String delims, final boolean processQuoted, final String tokens) {
        ThreadCategory log = ThreadCategory.getInstance(JRobinRrdStrategy.class);
        List<String> tokenList = new LinkedList<String>();
    
        StringBuffer currToken = new StringBuffer();
        boolean quoting = false;
        boolean escaping = false;
        boolean debugTokens = Boolean.getBoolean("org.opennms.netmgt.rrd.debugTokens");
        if (!log.isDebugEnabled())
            debugTokens = false;
        
        if (debugTokens)
            log.debug("tokenize: line=" + line + " delims=" + delims);
        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (debugTokens)
                log.debug("tokenize: checking char: " + ch);
            if (escaping) {
                if (ch == 'n') {
                    currToken.append(escapeIfNotPathSepInDEF(ch, '\n', currToken));
                } else if (ch == 'r') {
                    currToken.append(escapeIfNotPathSepInDEF(ch, '\r', currToken));
                } else if (ch == 't') {
                    currToken.append(escapeIfNotPathSepInDEF(ch, '\t', currToken));
                } else {
                    if (tokens.indexOf(ch) >= 0) {
                        currToken.append('\\').append(ch);
                    } else if (currToken.toString().startsWith("DEF:")) {
                        currToken.append('\\').append(ch);
                    } else {
                        // silently pass through the character *without* the \ in front of it
                        currToken.append(ch);
                    }
                }
                escaping = false;
                if (debugTokens)
                    log.debug("tokenize: escaped. appended to " + currToken);
            } else if (ch == '\\') {
                if (debugTokens)
                    log.debug("tokenize: found a backslash... escaping currToken = " + currToken);
                if (quoting && !processQuoted)
                    currToken.append(ch);
                else
                    escaping = true;
            } else if (ch == '\"') {
                if (!processQuoted)
                    currToken.append(ch);
                if (quoting) {
                    if (debugTokens)
                        log.debug("tokenize: found a quote ending quotation currToken = " + currToken);
                    quoting = false;
                } else {
                    if (debugTokens)
                        log.debug("tokenize: found a quote beginning quotation  currToken =" + currToken);
                    quoting = true;
                }
            } else if (!quoting && delims.indexOf(ch) >= 0) {
                if (debugTokens)
                    log.debug("tokenize: found a token: " + ch + " ending token [" + currToken + "] and starting a new one");
                tokenList.add(currToken.toString());
                currToken = new StringBuffer();
            } else {
                if (debugTokens)
                    log.debug("tokenize: appending " + ch + " to token: " + currToken);
                currToken.append(ch);
            }
    
        }
    
        if (escaping || quoting) {
            if (debugTokens)
                log.debug("tokenize: ended string but escaping = " + escaping + " and quoting = " + quoting);
            throw new IllegalArgumentException("unable to tokenize string " + line + " with token chars " + delims);
        }
    
        if (debugTokens)
            log.debug("tokenize: reached end of string.  completing token " + currToken);
        tokenList.add(currToken.toString());
    
        return (String[]) tokenList.toArray(new String[tokenList.size()]);
    }
    
    /**
     * <p>escapeIfNotPathSepInDEF</p>
     *
     * @param encountered a char.
     * @param escaped a char.
     * @param currToken a {@link java.lang.StringBuffer} object.
     * @return an array of char.
     */
    public static char[] escapeIfNotPathSepInDEF(final char encountered, final char escaped, final StringBuffer currToken) {
    	if ( ('\\' != File.separatorChar) || (! currToken.toString().startsWith("DEF:")) ) {
    		return new char[] { escaped };
    	} else {
    		return new char[] { '\\', encountered };
    	}
    }
    
    protected void addVdefDs(RrdGraphDef graphDef, String sourceName, String[] rhs, double start, double end, Map<String,List<String>> defs) throws RrdException {
        if (rhs.length == 2) {
            graphDef.datasource(sourceName, rhs[0], rhs[1]);
        } else if (rhs.length == 3 && "PERCENT".equals(rhs[2])) {
            // Is there a better way to do this than with a separate DataProcessor?
            double pctRank = Double.valueOf(rhs[1]);
            DataProcessor dataProcessor = new DataProcessor((int)start, (int)end);
            for (String dsName : defs.keySet()) {
                List<String> thisDef = defs.get(dsName);
                if (thisDef.size() == 3) {
                    dataProcessor.addDatasource(dsName, thisDef.get(0), thisDef.get(1), thisDef.get(2));
                } else if (thisDef.size() == 1) {
                    dataProcessor.addDatasource(dsName, thisDef.get(0));
                }
            }
            try {
                dataProcessor.processData();
            } catch (IOException e) {
                throw new RrdException("Caught IOException: " + e.getMessage());
            }
            
            double result = dataProcessor.getPercentile(rhs[0], pctRank);
            ConstantStaticDef csDef = new ConstantStaticDef((long)start, (long)end, result);
            graphDef.datasource(sourceName, csDef);
        } 
    }

}
