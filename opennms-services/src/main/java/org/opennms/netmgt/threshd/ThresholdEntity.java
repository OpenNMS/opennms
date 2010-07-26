//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2009 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2009 Apr 09: Expose data source indexes in threshold-derived events  - jeffg@opennms.org
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
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.rrd.RrdException;
import org.opennms.netmgt.rrd.RrdUtils;
import org.opennms.netmgt.threshd.ThresholdEvaluatorState.Status;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Parm;
import org.opennms.netmgt.xml.event.Value;

/**
 * Wraps the castor created org.opennms.netmgt.config.threshd.Threshold class
 * and provides the ability to track threshold exceeded occurrences.
 *
 * @author ranger
 * @version $Id: $
 */
public final class ThresholdEntity implements Cloneable {
    
    private static List<ThresholdEvaluator> s_thresholdEvaluators;
    
    //Contains a list of evaluators for each used "instance".  Is populated with the list for the "default" instance (the "null" key)
    // in the Constructor.  Note that this means we must use a null-key capable map like HashMap
    private Map<String,List<ThresholdEvaluatorState>> m_thresholdEvaluatorStates = new HashMap<String,List<ThresholdEvaluatorState>>();

    // the commands for these need to be listed in ThresholdController as well
    static {
        s_thresholdEvaluators = new LinkedList<ThresholdEvaluator>();
        s_thresholdEvaluators.add(new ThresholdEvaluatorHighLow());
        s_thresholdEvaluators.add(new ThresholdEvaluatorRelativeChange());
        s_thresholdEvaluators.add(new ThresholdEvaluatorAbsoluteChange());
        s_thresholdEvaluators.add(new ThresholdEvaluatorRearmingAbsoluteChange());
    }

    /**
     * Constructor.
     */
    public ThresholdEntity() {
        //Put in a default list for the "null" key (the default evaluators)
        m_thresholdEvaluatorStates.put(null, new LinkedList<ThresholdEvaluatorState>());
    }

    /**
     * <p>getThresholdConfig</p>
     *
     * @return a {@link org.opennms.netmgt.threshd.BaseThresholdDefConfigWrapper} object.
     */
    public BaseThresholdDefConfigWrapper getThresholdConfig() {
        return m_thresholdEvaluatorStates.get(null).get(0).getThresholdConfig();
    }
    
    private boolean hasThresholds() {
        return m_thresholdEvaluatorStates.get(null).size()!=0;
    }
    /**
     * Get datasource name
     *
     * @return a {@link java.lang.String} object.
     */
    public String getDataSourceExpression() {
        if (hasThresholds()) {
            return getThresholdConfig().getDatasourceExpression();
        } else {
            throw new IllegalStateException("No thresholds have been added.");
        }
    }

    /**
     * Get datasource type
     *
     * @return a {@link java.lang.String} object.
     */
    public String getDatasourceType() {
        if (hasThresholds()) {
            return getThresholdConfig().getDsType();
        } else {
            throw new IllegalStateException("No thresholds have been added.");
        }
    }

    /**
     * Get datasource Label
     *
     * @return a {@link java.lang.String} object.
     */
    public String getDatasourceLabel() {
        if (hasThresholds()) {
            return getThresholdConfig().getDsLabel();
        } else {
            return null;
        }
    }

    /**
     * Returns the names of the dataousrces required to evaluate this threshold entity
     *
     * @return Collection of the names of datasources
     */
    public Collection<String> getRequiredDatasources() {
        if (hasThresholds()) {
            return getThresholdConfig().getRequiredDatasources();
        } else {
            throw new IllegalStateException("No thresholds have been added.");
        }
    }
    /**
     * Returns a copy of this ThresholdEntity object.
     *
     * NOTE: The m_lowThreshold and m_highThreshold member variables are not
     * actually cloned...the returned ThresholdEntity object will simply contain
     * references to the same castor Threshold objects as the original
     * ThresholdEntity object.
     *
     * All state will be lost, particularly instances, so it's not a true clone by any stretch of the imagination
     *
     * @return a {@link org.opennms.netmgt.threshd.ThresholdEntity} object.
     */
    public ThresholdEntity clone() {
        ThresholdEntity clone = new ThresholdEntity();
        for (ThresholdEvaluatorState thresholdItem : getThresholdEvaluatorStates(null)) {
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
        if (!hasThresholds()) {
            return "";
        }

        StringBuffer buffer = new StringBuffer("{");

        buffer.append("dsName=").append(this.getDataSourceExpression());
        buffer.append(", dsType=").append(this.getDatasourceType());
        buffer.append(", evaluators=[");
        for (ThresholdEvaluatorState item : getThresholdEvaluatorStates(null)) {
            buffer.append("{ds=").append(item.getThresholdConfig().getDatasourceExpression());
            buffer.append(", value=").append(item.getThresholdConfig().getValue());
            buffer.append(", rearm=").append(item.getThresholdConfig().getRearm());
            buffer.append(", trigger=").append(item.getThresholdConfig().getTrigger());
            buffer.append("}");
        }
        buffer.append("]}");

        return buffer.toString();
    }

    
    /**
     * Evaluates the threshold in light of the provided datasource value and
     * create any events for thresholds.
     *
     * Semi-deprecated method; only used for old Thresholding code (threshd and friends)
     * Implemented in terms of the other method with the same name and the extra param
     *
     * @param values
     *          map of values (by datasource name) to evaluate against the threshold (might be an expression)
     * @param date
     *          Date to use in created events
     * @return List of events
     */
    public  List<Event> evaluateAndCreateEvents(Map<String, Double> values, Date date) {
           return evaluateAndCreateEvents(null, values, date);
    }

    /**
     * Evaluates the threshold in light of the provided datasource value, for
     * the named instance (or the generic instance if instance is null) and
     * create any events for thresholds.
     *
     * @param values
     *          map of values (by datasource name) to evaluate against the threshold (might be an expression)
     * @param date
     *          Date to use in created events
     * @return List of events
     * @param resource a {@link org.opennms.netmgt.threshd.CollectionResourceWrapper} object.
     */
    public List<Event> evaluateAndCreateEvents(CollectionResourceWrapper resource, Map<String, Double> values, Date date) {
        List<Event> events = new LinkedList<Event>();
        double dsValue=0.0;
        String instance = resource != null ? resource.getInstance() : null;
        try {
            if (getThresholdEvaluatorStates(instance).size() > 0) {
                dsValue=getThresholdConfig().evaluate(values);
            } else {
                throw new IllegalStateException("No thresholds have been added.");
            }
        } catch (ThresholdExpressionException e) {
            log().warn("Failed to evaluate: ", e);
            return events; //No events to report
        }
        
        if (log().isDebugEnabled()) {
            log().debug("evaluate: value= " + dsValue + " against threshold: " + this);
        }        

        for (ThresholdEvaluatorState item : getThresholdEvaluatorStates(instance)) {
            Status status = item.evaluate(dsValue);
            Event event = item.getEventForState(status, date, dsValue, resource);
            if (event != null) {
                events.add(event);
            }
        }

        return events;
    }

    private final ThreadCategory log() {
        return ThreadCategory.getInstance(ThresholdEntity.class);
    }

    /**
     * <p>fetchLastValue</p>
     *
     * @param latIface a {@link org.opennms.netmgt.threshd.LatencyInterface} object.
     * @param latParms a {@link org.opennms.netmgt.threshd.LatencyParameters} object.
     * @return a {@link java.lang.Double} object.
     * @throws org.opennms.netmgt.threshd.ThresholdingException if any.
     */
    public Double fetchLastValue(LatencyInterface latIface, LatencyParameters latParms) throws ThresholdingException {
        //Assume that this only happens on a simple "Threshold", not an "Expression"
        //If it is an Expression, then we don't yet know what to do - this will likely just fail with some sort of exception.
        //perhaps we should figure out how to expand it (or at least use code elsewhere to do so sensibly)
        String datasource=getDataSourceExpression();
  

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
                    dsValue = RrdUtils.fetchLastValue(rrdFile.getAbsolutePath(), datasource, latParms.getInterval());
                } else {
                    dsValue = RrdUtils.fetchLastValueInRange(rrdFile.getAbsolutePath(), datasource, latParms.getInterval(), latParms.getRange());
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

    /**
     * <p>addThreshold</p>
     *
     * @param threshold a {@link org.opennms.netmgt.threshd.BaseThresholdDefConfigWrapper} object.
     */
    public void addThreshold(BaseThresholdDefConfigWrapper threshold) {
        ThresholdEvaluator evaluator = getEvaluatorForThreshold(threshold);
        //Get the default list of evaluators (the null key)
        List<ThresholdEvaluatorState> defaultList=m_thresholdEvaluatorStates.get(null);

        for (ThresholdEvaluatorState item : defaultList) {
            if (threshold.getType().equals(item.getThresholdConfig().getType())) {
                throw new IllegalStateException(threshold.getType().toString() + " threshold already set.");
            }
        }

        defaultList.add(evaluator.getThresholdEvaluatorState(threshold));
    }

    private ThresholdEvaluator getEvaluatorForThreshold(BaseThresholdDefConfigWrapper threshold) {
        for (ThresholdEvaluator evaluator : getThresholdEvaluators()) {
            if (evaluator.supportsType(threshold.getType())) {
                return evaluator;
            }
        }

 
        String message = "Threshold type '" + threshold.getType().toString() + "' for "+ threshold.getDatasourceExpression() + " is not supported"; 
        log().warn(message);
        throw new IllegalArgumentException(message);
    }

    /**
     * Returns the evaluator states *for the given instance.
     *
     * @param instance The key to use to identify the instance to get states for. Can be null to get the default instance
     * @return a {@link java.util.List} object.
     */
    public List<ThresholdEvaluatorState> getThresholdEvaluatorStates(String instance) {
        List<ThresholdEvaluatorState> result= m_thresholdEvaluatorStates.get(instance);
        if(result==null) {
            //There is no set of evaluators for this instance; create a list by copying the base ones
            List<ThresholdEvaluatorState> defaultList=m_thresholdEvaluatorStates.get(null);
          
            //Create the new list
            result=new LinkedList<ThresholdEvaluatorState>();
            for(ThresholdEvaluatorState state: defaultList) {
                result.add(state.getCleanClone());
            }
            
            //Store the new list with the instance as the key
            m_thresholdEvaluatorStates.put(instance,result);
        }
        return result;
    }
    
    /**
     * Merges the configuration and update states using parameter entity as a reference.
     *
     * @param entity a {@link org.opennms.netmgt.threshd.ThresholdEntity} object.
     */
    public void merge(ThresholdEntity entity) {
        if (getThresholdConfig().identical(entity.getThresholdConfig()) == false) {
            sendRearmForTriggeredStates();
            getThresholdConfig().merge(entity.getThresholdConfig());
        }
    }

    /**
     * Delete this will check states and will send rearm for all triggered.
     */
    public void delete() {
        sendRearmForTriggeredStates();
    }
    
    private void sendRearmForTriggeredStates() {
        for (String instance : m_thresholdEvaluatorStates.keySet()) {
            for (ThresholdEvaluatorState state : m_thresholdEvaluatorStates.get(instance)) {
                if (state.isTriggered()) {
                    Event e = state.getEventForState(Status.RE_ARMED, new Date(), Double.NaN, null);
                    Parm p = new Parm();
                    p.setParmName("reason");
                    Value v = new Value();
                    v.setContent("Configuration has been changed");
                    p.setValue(v);
                    e.getParms().addParm(p);
                    log().info("sendRearmForTriggeredStates: sending rearm for " + e);
                    ThresholdingEventProxyFactory.getFactory().getProxy().add(e);
                    state.clearState();
                }
            }
        }
    }

    /**
     * <p>getThresholdEvaluators</p>
     *
     * @return a {@link java.util.List} object.
     */
    public static final List<ThresholdEvaluator> getThresholdEvaluators() {
        return s_thresholdEvaluators;
    }

}
