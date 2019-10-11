/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.threshd;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import org.opennms.core.rpc.utils.mate.EmptyScope;
import org.opennms.core.rpc.utils.mate.EntityScopeProvider;
import org.opennms.core.rpc.utils.mate.FallbackScope;
import org.opennms.core.rpc.utils.mate.Scope;
import org.opennms.netmgt.threshd.ThresholdEvaluatorState.Status;
import org.opennms.netmgt.threshd.api.ThresholdingEventProxy;
import org.opennms.netmgt.threshd.api.ThresholdingSession;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Parm;
import org.opennms.netmgt.xml.event.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.net.InetAddresses;

/**
 * Wraps the XML created org.opennms.netmgt.config.threshd.Threshold class
 * and provides the ability to track threshold exceeded occurrences.
 *
 * @author ranger
 * @version $Id: $
 */
public final class ThresholdEntity implements Cloneable {
    
    private static final Logger LOG = LoggerFactory.getLogger(ThresholdEntity.class);
    
    private static List<ThresholdEvaluator> s_thresholdEvaluators;
    
    //Contains a list of evaluators for each used "instance".  Is populated with the list for the "default" instance (the "null" key)
    // in the Constructor.  Note that this means we must use a null-key capable map like HashMap
    private Map<String,List<ThresholdEvaluatorState>> m_thresholdEvaluatorStates = new HashMap<String,List<ThresholdEvaluatorState>>();

    private ThresholdingEventProxy m_thresholdingEventProxy;

    // the commands for these need to be listed in ThresholdController as well
    static {
        s_thresholdEvaluators = new LinkedList<ThresholdEvaluator>();
        s_thresholdEvaluators.add(new ThresholdEvaluatorHighLow());
        s_thresholdEvaluators.add(new ThresholdEvaluatorRelativeChange());
        s_thresholdEvaluators.add(new ThresholdEvaluatorAbsoluteChange());
        s_thresholdEvaluators.add(new ThresholdEvaluatorRearmingAbsoluteChange());
    }

    private final EntityScopeProvider m_entityScopeProvider;

    /**
     * Constructor.
     */
    public ThresholdEntity(EntityScopeProvider entityScopeProvider) {
        //Put in a default list for the "null" key (the default evaluators)
        m_thresholdEvaluatorStates.put(null, new LinkedList<ThresholdEvaluatorState>());
        m_entityScopeProvider = Objects.requireNonNull(entityScopeProvider);
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
            return getThresholdConfig().getDsLabel().orElse(null);
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
            final Set<String> dataSources = new HashSet<String>();
            dataSources.addAll(getThresholdConfig().getRequiredDatasources());
            dataSources.addAll(getThresholdConfig().getFilterDatasources());
            return dataSources;
        } else {
            throw new IllegalStateException("No thresholds have been added.");
        }
    }
    /**
     * Returns a copy of this ThresholdEntity object.
     *
     * NOTE: The m_lowThreshold and m_highThreshold member variables are not
     * actually cloned...the returned ThresholdEntity object will simply contain
     * references to the same Threshold objects as the original
     * ThresholdEntity object.
     *
     * All state will be lost, particularly instances, so it's not a true clone by any stretch of the imagination
     *
     * @return a {@link org.opennms.netmgt.threshd.ThresholdEntity} object.
     */
    @Override
    public ThresholdEntity clone() {
        ThresholdEntity clone = new ThresholdEntity(m_entityScopeProvider);
        for (ThresholdEvaluatorState thresholdItem : getThresholdEvaluatorStates(null)) {
            clone.addThreshold(thresholdItem.getThresholdConfig(), thresholdItem.getThresholdingSession());
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
    @Override
    public String toString() {
        if (!hasThresholds()) {
            return "";
        }

        final StringBuilder buffer = new StringBuilder("{");

        buffer.append("evaluator=").append(this.getThresholdConfig().getType());
        buffer.append(", dsName=").append(this.getDataSourceExpression());
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

        String instance = null;
        if (resource != null) {
            // NMS-9361: Use the instance label as the key for the thresholder's state. This allows us to uniquely
            // identify resources that share the same instance, but whose path
            // on disk may differ due to the use of a StorageStrategy implementation
            // such as the SiblingColumnStorageStrategy
            instance = resource.getInstanceLabel();
        }

        if (getThresholdEvaluatorStates(instance).isEmpty()) {
            throw new IllegalStateException("No thresholds have been added.");
        }

        // This reference contains the function that will be used by each evaluator to retrieve the status
        AtomicReference<EvaluateFunction> evaluateFunctionRef = new AtomicReference<>(null);

        // Depending on the type of threshold, we want to evaluate it differently
        // Specifically expression based thresholds are treated special because their behavior must change depending on
        // whether or not a given expression has been interpolated already
        getThresholdConfig().accept(new ThresholdDefVisitor() {
            @Override
            public void visit(ThresholdConfigWrapper thresholdConfigWrapper) {
                double computedValue = thresholdConfigWrapper.evaluate(values);
                evaluateFunctionRef.set(item -> new ThresholdEvaluatorState.ValueStatus(computedValue,
                        item.evaluate(computedValue, resource == null ? null : resource.getSequenceNumber())));
            }

            @Override
            public void visit(ExpressionConfigWrapper expressionConfigWrapper) {
                ExpressionThresholdValue expressionThresholdValue = new ExpressionThresholdValue() {
                    // This covers the case where an expression has not yet been interpolated, we want to evaluate and
                    // also retrieve the interpolated expression so we can persist it in our state going forward
                    @Override
                    public double get(Consumer<String> expressionConsumer) throws ThresholdExpressionException {
                        // Default to empty scopes and then attempt to populate each of node, interface, and service
                        // scopes below
                        Scope[] scopes = new Scope[]{EmptyScope.EMPTY, EmptyScope.EMPTY, EmptyScope.EMPTY};

                        if (resource != null) {
                            scopes[0] = m_entityScopeProvider.getScopeForNode(resource.getNodeId());
                            String interfaceIp = resource.getHostAddress();
                            if (interfaceIp != null) {
                                scopes[1] = m_entityScopeProvider.getScopeForInterface(resource.getNodeId(),
                                        interfaceIp);
                                scopes[2] = m_entityScopeProvider.getScopeForService(resource.getNodeId(),
                                        InetAddresses.forString(interfaceIp), resource.getServiceName());
                            }
                        }

                        FallbackScope fallbackScope = new FallbackScope(scopes);

                        ExpressionConfigWrapper.ExpressionValue expressionValue = expressionConfigWrapper
                                .interpolateAndEvaluate(values, fallbackScope);
                        expressionConsumer.accept(expressionValue.expression);

                        return expressionValue.value;
                    }

                    // This covers the case where an expression has already been interpolated and the evaluator is
                    // providing us that expression that it has persisted along with its state so that we do not need to
                    // perform interpolation again
                    @Override
                    public double get(String evaluatedExpression) throws ThresholdExpressionException {
                        return expressionConfigWrapper.evaluate(evaluatedExpression, values);
                    }
                };

                evaluateFunctionRef.set(item -> item.evaluate(expressionThresholdValue, resource == null ? null :
                        resource.getSequenceNumber()));
            }
        });

        EvaluateFunction evaluateFunction = evaluateFunctionRef.get();
        for (ThresholdEvaluatorState item : getThresholdEvaluatorStates(instance)) {
            ThresholdEvaluatorState.ValueStatus result;
            try {
                result = evaluateFunction.evaluate(item);
            } catch (ThresholdExpressionException e) {
                LOG.warn("Error evaluating: threshold: {} and evaluator: {}", this, item, e);
                continue;
            }
            Status status = result.status;
            Event event = item.getEventForState(status, date, result.value, resource);
            LOG.debug("evaluated: value= {} against threshold: {} and evaluator: {}", result.value, this, item);
            if (event != null) {
                events.add(event);
            }
        }

        return events;
    }

    /**
     * <p>addThreshold</p>
     *
     * @param threshold a {@link org.opennms.netmgt.threshd.BaseThresholdDefConfigWrapper} object.
     */
    public void addThreshold(BaseThresholdDefConfigWrapper threshold, ThresholdingSession thresholdingSession) {
        ThresholdEvaluator evaluator = getEvaluatorForThreshold(threshold);
        //Get the default list of evaluators (the null key)
        List<ThresholdEvaluatorState> defaultList=m_thresholdEvaluatorStates.get(null);

        for (ThresholdEvaluatorState item : defaultList) {
            if (threshold.getType().equals(item.getThresholdConfig().getType())) {
                throw new IllegalStateException(threshold.getType() + " threshold already set.");
            }
        }

        defaultList.add(evaluator.getThresholdEvaluatorState(threshold, thresholdingSession));
    }

    private ThresholdEvaluator getEvaluatorForThreshold(BaseThresholdDefConfigWrapper threshold) {
        for (ThresholdEvaluator evaluator : getThresholdEvaluators()) {
            if (evaluator.supportsType(threshold.getType())) {
                return evaluator;
            }
        }

 
        String message = "Threshold type '" + threshold.getType() + "' for "+ threshold.getDatasourceExpression() + " is not supported"; 
        LOG.warn(message);
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
                ThresholdEvaluatorState instancedState = state.getCleanClone();

                if (instance != null) {
                    // We need to set the instance on the evaluator state so that it can update its key to avoid
                    // collisions and this seems to be the only convenient spot to do that
                    instancedState.setInstance(instance);
                }

                result.add(instancedState);
            }
            
            //Store the new list with the instance as the key
            m_thresholdEvaluatorStates.put(instance == null ? null : instance.intern(), result);
        }
        return result;
    }
    
    /**
     * Merges the configuration and update states using parameter entity as a reference.
     *
     * @param entity a {@link org.opennms.netmgt.threshd.ThresholdEntity} object.
     */
    public void merge(ThresholdEntity entity) {
        if (getThresholdConfig().equals(entity.getThresholdConfig()) == false) {
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
                    e.addParm(p);
                    LOG.info("sendRearmForTriggeredStates: sending rearm for {}", e);
                    m_thresholdingEventProxy.sendEvent(e);
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

    public void setEventProxy(ThresholdingEventProxy eventProxy) {
        m_thresholdingEventProxy = eventProxy;
    }

    @FunctionalInterface
    private interface EvaluateFunction {
        ThresholdEvaluatorState.ValueStatus evaluate(ThresholdEvaluatorState thresholdEvaluatorState)
                throws ThresholdExpressionException;
    }
}