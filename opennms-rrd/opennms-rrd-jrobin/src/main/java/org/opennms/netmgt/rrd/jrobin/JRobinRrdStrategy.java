/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2002-2008 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified 
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 *
 * 2008 Jun 16: Move RRD command-specific tokenizing methods here from StringUtils - jeffg@opennms.org
 * 2007 Aug 02: Organize imports. - dj@opennms.org
 * 2007 Apr 05: Java 5 generics and loops. - dj@opennms.org
 * 2007 Mar 19: Add createGraphReturnDetails and move assertion of a graph being created to JRobinRrdGraphDetails. - dj@opennms.org
 * 2007 Mar 19: Indent, add support for PRINT in graphs. - dj@opennms.org
 * 2007 Mar 02: Add support for --base and fix some log messages. - dj@opennms.org
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
package org.opennms.netmgt.rrd.jrobin;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.jrobin.core.FetchData;
import org.jrobin.core.RrdDb;
import org.jrobin.core.RrdDef;
import org.jrobin.core.RrdException;
import org.jrobin.core.Sample;
import org.jrobin.graph.RrdGraph;
import org.jrobin.graph.RrdGraphDef;
import org.opennms.core.utils.StringUtils;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.rrd.RrdConfig;
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
     * <p>getConfigurationProperties</p>
     *
     * @return a {@link java.util.Properties} object.
     */
    public Properties getConfigurationProperties() {
        return m_configurationProperties;
    }

    /** {@inheritDoc} */
    public void setConfigurationProperties(Properties configurationParameters) {
        m_configurationProperties = configurationParameters;
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
    public RrdDef createDefinition(final String creator, final String directory, final String rrdName, int step, final List<RrdDataSource> dataSources, final List<String> rraList) throws Exception {
        File f = new File(directory);
        f.mkdirs();

        String fileName = directory + File.separator + rrdName + RrdUtils.getExtension();
        
        if (new File(fileName).exists()) {
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
     * closing. TODO: Change the interface here to create the file and return it
     * opened.
     *
     * @param rrdDef a {@link org.jrobin.core.RrdDef} object.
     * @throws java.lang.Exception if any.
     */
    public void createFile(final RrdDef rrdDef) throws Exception {
        if (rrdDef == null) {
            return;
        }
        
        RrdDb rrd = new RrdDb(rrdDef);
        rrd.close();
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
        if (!s_initialized) {
            String factory;
            if (m_configurationProperties == null) {
                factory = DEFAULT_BACKEND_FACTORY;
            } else {
                factory = (String) m_configurationProperties.getProperty(BACKEND_FACTORY_PROPERTY, DEFAULT_BACKEND_FACTORY);
            }
            RrdDb.setDefaultFactory(factory);
            s_initialized = true;
        }
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
        int colorVal = Integer.parseInt(colorValue, 16);
        return new Color(colorVal);
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
        } catch (Exception e) {
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
    protected RrdGraphDef createGraphDef(final File workDir, final String[] commandArray) throws RrdException {
        RrdGraphDef graphDef = new RrdGraphDef();
        graphDef.setImageFormat("PNG");
        long start = 0;
        long end = 0;
        int height = 100;
        int width = 400;
        double lowerLimit = Double.NaN;
        double upperLimit = Double.NaN;
        boolean rigid = false;
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
                String[] def = definition.split(":");
                String[] ds = def[0].split("=");
                File dsFile = new File(workDir, ds[1]);
                graphDef.datasource(ds[0], dsFile.getAbsolutePath(), def[1], def[2]);
            
            } else if (arg.startsWith("CDEF:")) {
                String definition = arg.substring("CDEF:".length());
                String[] cdef = tokenize(definition, "=", true);
                graphDef.datasource(cdef[0], cdef[1]);
            
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
			} catch (Exception e) {
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
     * @param colorArg Should have the form COLORTAG#RRGGBB
     * @see http://www.jrobin.org/support/man/rrdgraph.html
     */
    private void parseGraphColor(final RrdGraphDef graphDef, final String colorArg) throws IllegalArgumentException {
        // Parse for format COLORTAG#RRGGBB
        String[] colorArgParts = tokenize(colorArg, "#", false);
        if (colorArgParts.length != 2) {
            throw new IllegalArgumentException("--color must be followed by value with format COLORTAG#RRGGBB");
        }

        String colorTag = colorArgParts[0].toUpperCase();
        String colorHex = colorArgParts[1].toUpperCase();

        // validate hex color input is actually an RGB hex color value
        if (colorHex.length() != 6) {
            throw new IllegalArgumentException("--color must be followed by value with format COLORTAG#RRGGBB");
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
        } catch (Exception e) {
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
        ThreadCategory log = ThreadCategory.getInstance(StringUtils.class);
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

}
