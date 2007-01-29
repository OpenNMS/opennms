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
// 2007 Jan 29: Indent; pull evaluation code out into ThresholdEvaluator(State) interface and implementations; improve exception messages; use Java 5 generics. - dj@opennms.org
// 2005 Nov 29: Added a method to allow for labels in Threshold events
// 2003 Jan 31: Cleaned up some unused imports.
// 2002 Oct 22: Added Threshold rearm event.
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

package org.opennms.netmgt.threshd;

import java.io.File;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.threshd.Threshold;
import org.opennms.netmgt.rrd.RrdException;
import org.opennms.netmgt.rrd.RrdUtils;
import org.opennms.netmgt.threshd.ThresholdEvaluatorState.Status;
import org.opennms.netmgt.xml.event.Event;

/**
 * Wraps the castor created org.opennms.netmgt.config.threshd.Threshold class
 * and provides the ability to track threshold exceeded occurrences.
 */
public final class ThresholdEntity implements Cloneable {
    private static List<ThresholdEvaluator> s_thresholdEvaluators;

    private List<ThresholdEvaluatorState> m_thresholdEvaluatorStates = new LinkedList<ThresholdEvaluatorState>();

    static {
        s_thresholdEvaluators = new LinkedList<ThresholdEvaluator>();
        s_thresholdEvaluators.add(new ThresholdEvaluatorHighLow());
        s_thresholdEvaluators.add(new ThresholdEvaluatorRelativeChange());
    }

    /**
     * Constructor.
     */
    public ThresholdEntity() {
    }

    /**
     * Get datasource name
     */
    public String getDatasourceName() {
        if (getThresholdEvaluatorStates().size() > 0) {
            return getThresholdEvaluatorStates().get(0).getThresholdConfig().getDsName();
        } else {
            throw new IllegalStateException("No thresholds have been added.");
        }
    }

    /**
     * Get datasource type
     */
    public String getDatasourceType() {
        if (getThresholdEvaluatorStates().size() > 0) {
            return getThresholdEvaluatorStates().get(0).getThresholdConfig().getDsType();
        } else {
            throw new IllegalStateException("No thresholds have been added.");
        }
    }

    /**
     * Get datasource Label
     */
    public String getDatasourceLabel() {
        if (getThresholdEvaluatorStates().size() > 0) {
            return getThresholdEvaluatorStates().get(0).getThresholdConfig().getDsLabel();
        } else {
            return null;
        }
    }

    /**
     * Returns a copy of this ThresholdEntity object.
     * 
     * NOTE: The m_lowThreshold and m_highThreshold member variables are not
     * actually cloned...the returned ThresholdEntity object will simply contain
     * references to the same castor Threshold objects as the original
     * ThresholdEntity object.
     */
    public ThresholdEntity clone() {
        ThresholdEntity clone = new ThresholdEntity();
        for (ThresholdEvaluatorState thresholdItem : getThresholdEvaluatorStates()) {
            clone.addThreshold(thresholdItem.getThresholdConfig());
        }

        return clone;
    }

    /**
     * This method is responsible for returning a String object which represents
     * the content of this ThresholdEntity. Primarily used for debugging
     * purposes.
     * 
     * @return String which represents the content of this ThresholdEntity
     */
    public String toString() {
        if (getThresholdEvaluatorStates().size() == 0) {
            return "";
        }

        StringBuffer buffer = new StringBuffer();

        buffer.append("dsName=").append(this.getDatasourceName());
        buffer.append(", dsType=").append(this.getDatasourceType());

        for (ThresholdEvaluatorState item : getThresholdEvaluatorStates()) {
            buffer.append(", ds=").append(item.getThresholdConfig().getDsName());
            buffer.append(", value=").append(item.getThresholdConfig().getValue());
            buffer.append(", rearm=").append(item.getThresholdConfig().getRearm());
            buffer.append(", trigger=").append(item.getThresholdConfig().getTrigger());
        }

        return buffer.toString();
    }

    /**
     * Evaluates the threshold in light of the provided datasource value and
     * create any events for thresholds.
     * 
     * @param dsValue
     *            Current value of datasource

     * @return List of events
     */
    public List<Event> evaluateAndCreateEvents(double dsValue, Date date) {
        if (log().isDebugEnabled()) {
            log().debug("evaluate: value= " + dsValue + " against threshold: " + this);
        }

        List<Event> events = new LinkedList<Event>();

        for (ThresholdEvaluatorState item : getThresholdEvaluatorStates()) {
            Status status = item.evaluate(dsValue);
            Event event = item.getEventForState(status, date, dsValue);
            if (event != null) {
                events.add(event);
            }
        }

        return events;
    }

    private final Category log() {
        return ThreadCategory.getInstance(ThresholdEntity.class);
    }

    public Double fetchLastValue(LatencyInterface latIface, LatencyParameters latParms) throws ThresholdingException {
        String datasource = getDatasourceName();

        // Use RRD strategy to "fetch" value of the datasource from the RRD file
        Double dsValue = null;
        try {
            if (getDatasourceType().equals("if")) {
                if (log().isDebugEnabled()) {
                    log().debug("Fetching last value from dataSource '" + datasource + "'");
                }

                File rrdFile = new  File(latIface.getLatencyDir(), datasource+RrdUtils.getExtension());
                if (!rrdFile.exists()) {
                    log().info("rrd file "+rrdFile+" does not exist");
                    return null;
                }

                if (!rrdFile.canRead()) {
                    log().error("Unable to read existing rrd file "+rrdFile);
                    return null;
                }

                if (latParms.getRange() == 0) {
                    dsValue = RrdUtils.fetchLastValue(rrdFile.getAbsolutePath(), latParms.getInterval());
                } else {
                    dsValue = RrdUtils.fetchLastValueInRange(rrdFile.getAbsolutePath(), latParms.getInterval(), latParms.getRange());
                }
            } else {
                throw new ThresholdingException("expr types not yet implemented", LatencyThresholder.THRESHOLDING_FAILED);
            }

            if (log().isDebugEnabled()) {
                log().debug("Last value from dataSource '" + datasource + "' was "+dsValue);
            }
        } catch (NumberFormatException nfe) {
            log().warn("Unable to convert retrieved value for datasource '" + datasource + "' to a double, skipping evaluation.");
        } catch (RrdException e) {
            log().error("An error occurred retriving the last value for datasource '" + datasource + "': " + e, e);
        }

        return dsValue;
    }

    public void addThreshold(Threshold threshold) {
        ThresholdEvaluator evaluator = getEvaluatorForThreshold(threshold);

        for (ThresholdEvaluatorState item : getThresholdEvaluatorStates()) {
            if (item.getThresholdConfig().getType() == threshold.getType()) {
                throw new IllegalStateException(threshold.getType().toString() + " threshold already set.");
            }
        }

        m_thresholdEvaluatorStates.add(evaluator.getThresholdEvaluatorState(threshold));
    }

    private ThresholdEvaluator getEvaluatorForThreshold(Threshold threshold) {
        for (ThresholdEvaluator evaluator : getThresholdEvaluators()) {
            if (evaluator.supportsType(threshold.getType())) {
                return evaluator;
            }
        }

        String message = "Threshold type '" + threshold.getType().toString() + "' for datasource " + threshold.getDsName() + " is not supported"; 
        log().warn(message);
        throw new IllegalArgumentException(message);
    }

    public List<ThresholdEvaluatorState> getThresholdEvaluatorStates() {
        return m_thresholdEvaluatorStates;
    }

    public static final List<ThresholdEvaluator> getThresholdEvaluators() {
        return s_thresholdEvaluators;
    }
}
