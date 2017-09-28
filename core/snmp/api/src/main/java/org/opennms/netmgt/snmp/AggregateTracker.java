/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.snmp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class AggregateTracker extends CollectionTracker {
    private static final class ChildTrackerPduBuilder extends PduBuilder {
        private List<SnmpObjId> m_oids = new ArrayList<SnmpObjId>();
        private int m_nonRepeaters = 0;
        private int m_maxRepititions = 0;
        private ResponseProcessor m_responseProcessor;
        private int m_nonRepeaterStartIndex;
        private int m_repeaterStartIndex;
        
        public ChildTrackerPduBuilder(int maxVarsPerPdu) {
            super(maxVarsPerPdu);
        }
        @Override
        public void addOid(SnmpObjId snmpObjId) {
            m_oids.add(snmpObjId);
        }
    
        @Override
        public void setNonRepeaters(int nonRepeaters) {
            m_nonRepeaters = nonRepeaters;
        }
        
        public int getNonRepeaters() {
            return m_nonRepeaters;
        }
        
        public int getRepeaters() {
            return size() - getNonRepeaters();
        }
    
        @Override
        public void setMaxRepetitions(int maxRepititions) {
            m_maxRepititions = maxRepititions;
        }
        
        public int getMaxRepititions() {
            return hasRepeaters() ? m_maxRepititions : Integer.MAX_VALUE;
        }
        
        public int size() {
            return m_oids.size();
        }
        
        public void setResponseProcessor(ResponseProcessor responseProcessor) {
            m_responseProcessor = responseProcessor;
        }
        
        public ResponseProcessor getResponseProcessor() {
            return m_responseProcessor;
        }
        
        public void addNonRepeaters(PduBuilder pduBuilder) {
            for (int i = 0; i < m_nonRepeaters; i++) {
                SnmpObjId oid = m_oids.get(i);
                pduBuilder.addOid(oid);
            }
        }
        
        public void addRepeaters(PduBuilder pduBuilder) {
            for (int i = m_nonRepeaters; i < m_oids.size(); i++) {
                SnmpObjId oid = m_oids.get(i);
                pduBuilder.addOid(oid);
            }
        }
        
        public boolean hasRepeaters() {
            return getNonRepeaters() < size();
        }
        
        public void setNonRepeaterStartIndex(int nonRepeaterStartIndex) {
            m_nonRepeaterStartIndex = nonRepeaterStartIndex;
        }
        
        public int getNonRepeaterStartIndex() {
            return m_nonRepeaterStartIndex;
        }
        
        public void setRepeaterStartIndex(int repeaterStartIndex) {
            m_repeaterStartIndex = repeaterStartIndex;
        }
        
        public int getRepeaterStartIndex() {
            return m_repeaterStartIndex;
        }
        
        boolean isNonRepeater(int canonicalIndex) {
            return getNonRepeaterStartIndex() <= canonicalIndex && canonicalIndex < getNonRepeaterStartIndex() + getNonRepeaters();
        }
        
        boolean isRepeater(int canonicalIndex) {
            return getRepeaterStartIndex() <= canonicalIndex && canonicalIndex < getRepeaterStartIndex()+getRepeaters();
        }
        
        public int getChildIndex(int canonicalIndex) {
            if (isNonRepeater(canonicalIndex)) {
                return canonicalIndex - getNonRepeaterStartIndex();
            }
            
            if (isRepeater(canonicalIndex)) {
                return canonicalIndex - getRepeaterStartIndex() + getNonRepeaters();
            }
            
            throw new IllegalArgumentException("index out of range for tracker "+this);
        }
    }

    private static class ChildTrackerResponseProcessor implements ResponseProcessor {
        private final CollectionTracker m_tracker;
        private final int m_repeaters;
        private final PduBuilder m_pduBuilder;
        private final int m_nonRepeaters;
        private final List<ChildTrackerPduBuilder> m_childPduBuilders;
        
        private int m_currResponseIndex = 0;
        
        public ChildTrackerResponseProcessor(final CollectionTracker tracker, final PduBuilder pduBuilder, final List<ChildTrackerPduBuilder> builders, final int nonRepeaters, final int repeaters) {
            m_tracker = tracker;
            m_repeaters = repeaters;
            m_pduBuilder = pduBuilder;
            m_nonRepeaters = nonRepeaters;
            m_childPduBuilders = builders;
        }
    
        @Override
        public void processResponse(SnmpObjId snmpObjId, SnmpValue val) {
            ChildTrackerPduBuilder childBuilder = getChildBuilder(m_currResponseIndex++);
            childBuilder.getResponseProcessor().processResponse(snmpObjId, val);
        }
    
        public boolean processChildError(int errorStatus, int errorIndex) {
            int canonicalIndex = getCanonicalIndex(errorIndex-1);
            ChildTrackerPduBuilder childBuilder = getChildBuilder(canonicalIndex);
            int childIndex = childBuilder.getChildIndex(canonicalIndex);
            return childBuilder.getResponseProcessor().processErrors(errorStatus, childIndex+1);
        }
    
        private ChildTrackerPduBuilder getChildBuilder(int zeroBasedIndex) {
            int canonicalIndex = getCanonicalIndex(zeroBasedIndex);
            for (ChildTrackerPduBuilder childBuilder : m_childPduBuilders) {
                if (childBuilder.isNonRepeater(canonicalIndex) || childBuilder.isRepeater(canonicalIndex)) {
                    return childBuilder;
                }
            }
    
            throw new IllegalStateException("Unable to find childBuilder for index "+zeroBasedIndex);
        }
    
        private int getCanonicalIndex(int zeroBasedIndex) {
            if (zeroBasedIndex <= 0) {
                return 0;
            }
            if (zeroBasedIndex < m_nonRepeaters) {
                return zeroBasedIndex;
            }
    
            // return the smallest index of the repeater this index refers to 
            return ((zeroBasedIndex - m_nonRepeaters) % m_repeaters) + m_nonRepeaters;
        }
    
        @Override
        public boolean processErrors(int errorStatus, int errorIndex) {
            //LOG.trace("processErrors: errorStatus={}, errorIndex={}", errorStatus, errorIndex);;

            final ErrorStatus status = ErrorStatus.fromStatus(errorStatus);

            // handle special cases first
            if (status == ErrorStatus.TOO_BIG) {
                int maxVarsPerPdu = m_pduBuilder.getMaxVarsPerPdu();
                if (maxVarsPerPdu <= 1) {
                    throw new IllegalArgumentException("Unable to handle tooBigError when maxVarsPerPdu = "+maxVarsPerPdu);
                }
                m_pduBuilder.setMaxVarsPerPdu(maxVarsPerPdu/2);
                m_tracker.reportTooBigErr("Reducing maxVarsPerPDU for this request.");
                return true;
            } else if (status.isFatal()) {
                final ErrorStatusException ex = new ErrorStatusException(status);
                m_tracker.reportFatalErr(ex);
                throw ex;
            } else {
                return processChildError(errorStatus, errorIndex);
            }
        }
    }

    private CollectionTracker[] m_children;
    
    public AggregateTracker(Collection<Collectable> children) {
        this(children, null);
    }

    public AggregateTracker(Collection<Collectable> children, CollectionTracker parent) {
        this(children.toArray(new Collectable[children.size()]), parent);
    }

    public AggregateTracker(Collectable[] children) {
        this(children, null);
    }

    public AggregateTracker(Collectable[] children, CollectionTracker parent) {
        super(parent);
        
        m_children = new CollectionTracker[children.length];
        for (int i = 0; i < m_children.length; i++) {
            m_children[i] = children[i].getCollectionTracker();
            m_children[i].setParent(this);
        }
    }
    
    @Override
    public void setFailed(boolean failed) {
        super.setFailed(failed);
        for (CollectionTracker child : m_children) {
            child.setFailed(failed);
        }
    }

    @Override
    public void setTimedOut(boolean timedOut) {
        super.setTimedOut(timedOut);
        for (CollectionTracker child : m_children) {
            child.setTimedOut(timedOut);
        }
    }
    
    @Override
    public void setMaxRepetitions(int maxRepititions) {
        for (CollectionTracker child : m_children) {
            child.setMaxRepetitions(maxRepititions);
        }
    }

    @Override
    public void setMaxRetries(final int maxRetries) {
        for (final CollectionTracker child : m_children) {
            child.setMaxRetries(maxRetries);
        }
    }

    @Override
    public boolean isFinished() {
        for (CollectionTracker child : m_children) {
            if (!child.isFinished()) {
                return false;
            }
        }
        return true;
    }
    
    @Override
    public ResponseProcessor buildNextPdu(final PduBuilder parentBuilder) {
        
        // first process the child trackers that aren't finished up to maxVars 
        int count = 0;
        int maxVars = parentBuilder.getMaxVarsPerPdu();
        final List<ChildTrackerPduBuilder> builders = new ArrayList<ChildTrackerPduBuilder>(m_children.length);
        for (int i = 0; i < m_children.length && count < maxVars; i++) {
            CollectionTracker childTracker = m_children[i];
            if (!childTracker.isFinished()) {
                ChildTrackerPduBuilder childBuilder = new ChildTrackerPduBuilder(maxVars-count);
                ResponseProcessor rp = childTracker.buildNextPdu(childBuilder);
                childBuilder.setResponseProcessor(rp);
                builders.add(childBuilder);
                count += childBuilder.size();
            }
        }
        
        // set the nonRepeaters in the passed in pduBuilder and store indices in the childTrackers
        int nonRepeaters = 0;
        for (ChildTrackerPduBuilder childBuilder : builders) {
            childBuilder.setNonRepeaterStartIndex(nonRepeaters);
            childBuilder.addNonRepeaters(parentBuilder);
            nonRepeaters += childBuilder.getNonRepeaters();
        }
        
        // set the repeaters in the passed in pduBuilder and store indices in the childTrackers
        int maxRepititions = Integer.MAX_VALUE;
        int repeaters = 0;
        for (ChildTrackerPduBuilder childBuilder : builders) {
            childBuilder.setRepeaterStartIndex(nonRepeaters+repeaters);
            childBuilder.addRepeaters(parentBuilder);
            maxRepititions = Math.min(maxRepititions, childBuilder.getMaxRepititions());
            repeaters += childBuilder.getRepeaters();
        }
        
        // set the non repeaters and max repetitions
        parentBuilder.setNonRepeaters(nonRepeaters);
        parentBuilder.setMaxRepetitions(maxRepititions == Integer.MAX_VALUE ? 1 : maxRepititions);
        
        // construct a response processor that tracks the changes and informs the response processors
        // for the child trackers
        return new ChildTrackerResponseProcessor(this, parentBuilder, builders, nonRepeaters, repeaters);
    }
}
