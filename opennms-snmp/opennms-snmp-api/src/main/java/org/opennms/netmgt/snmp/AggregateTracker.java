//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2005 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2007 Jun 23: Use Java 5 generics and loops, format code. - dj@opennms.org
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
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
//
package org.opennms.netmgt.snmp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * <p>AggregateTracker class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class AggregateTracker extends CollectionTracker {

    private static class ChildTrackerPduBuilder extends PduBuilder {
        private List<SnmpObjId> m_oids = new ArrayList<SnmpObjId>();
        private int m_nonRepeaters = 0;
        private int m_maxRepititions = 0;
        private ResponseProcessor m_responseProcessor;
        private int m_nonRepeaterStartIndex;
        private int m_repeaterStartIndex;
        
        public ChildTrackerPduBuilder(int maxVarsPerPdu) {
            super(maxVarsPerPdu);
        }
        public void addOid(SnmpObjId snmpObjId) {
            m_oids.add(snmpObjId);
        }
    
        public void setNonRepeaters(int nonRepeaters) {
            m_nonRepeaters = nonRepeaters;
        }
        
        public int getNonRepeaters() {
            return m_nonRepeaters;
        }
        
        public int getRepeaters() {
            return size() - getNonRepeaters();
        }
    
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
                return canonicalIndex - getRepeaterStartIndex();
            }
            
            throw new IllegalArgumentException("index out of range for tracker "+this);
        }
    }

    private class ChildTrackerResponseProcessor implements ResponseProcessor {
        private final int m_repeaters;
    
        private final PduBuilder m_pduBuilder;
    
        private final int m_nonRepeaters;
    
        private final List<ChildTrackerPduBuilder> m_childPduBuilders;
        
        private int m_currResponseIndex = 0;
        
        public ChildTrackerResponseProcessor(PduBuilder pduBuilder, List<ChildTrackerPduBuilder> builders, int nonRepeaters, int repeaters) {
            this.m_repeaters = repeaters;
            this.m_pduBuilder = pduBuilder;
            this.m_nonRepeaters = nonRepeaters;
            this.m_childPduBuilders = builders;
        }
    
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
    
        public boolean processErrors(int errorStatus, int errorIndex) {
            if (errorStatus == TOO_BIG_ERR) {
                int maxVarsPerPdu = m_pduBuilder.getMaxVarsPerPdu();
                if (maxVarsPerPdu <= 1) {
                    throw new IllegalArgumentException("Unable to handle tooBigError when maxVarsPerPdu = "+maxVarsPerPdu);
                }
                m_pduBuilder.setMaxVarsPerPdu(maxVarsPerPdu/2);
                reportTooBigErr("Reducing maxVarsPerPdu for this request to "+m_pduBuilder.getMaxVarsPerPdu());
                return true;
            } else if (errorStatus == GEN_ERR) {
                return processChildError(errorStatus, errorIndex);
            } else if (errorStatus == NO_SUCH_NAME_ERR) {
                return processChildError(errorStatus, errorIndex);
            } else if (errorStatus != NO_ERR){
                throw new IllegalArgumentException("Unrecognized errorStatus "+errorStatus);
            } else {
                // contine on.. no need to retry
                return false;
            }
        }
    }

    private CollectionTracker[] m_children;
    
    /**
     * <p>Constructor for AggregateTracker.</p>
     *
     * @param children a {@link java.util.Collection} object.
     */
    public AggregateTracker(Collection<Collectable> children) {
        this(children, null);
    }

    /**
     * <p>Constructor for AggregateTracker.</p>
     *
     * @param children a {@link java.util.Collection} object.
     * @param parent a {@link org.opennms.netmgt.snmp.CollectionTracker} object.
     */
    public AggregateTracker(Collection<Collectable> children, CollectionTracker parent) {
        this(children.toArray(new Collectable[children.size()]), parent);
    }

    /**
     * <p>Constructor for AggregateTracker.</p>
     *
     * @param children an array of {@link org.opennms.netmgt.snmp.Collectable} objects.
     */
    public AggregateTracker(Collectable[] children) {
        this(children, null);
    }

    /**
     * <p>Constructor for AggregateTracker.</p>
     *
     * @param children an array of {@link org.opennms.netmgt.snmp.Collectable} objects.
     * @param parent a {@link org.opennms.netmgt.snmp.CollectionTracker} object.
     */
    public AggregateTracker(Collectable[] children, CollectionTracker parent) {
        super(parent);
        
        m_children = new CollectionTracker[children.length];
        for (int i = 0; i < m_children.length; i++) {
            m_children[i] = children[i].getCollectionTracker();
            m_children[i].setParent(this);
        }
    }
    
    /** {@inheritDoc} */
    public void setFailed(boolean failed) {
        super.setFailed(failed);
        for (CollectionTracker child : m_children) {
            child.setFailed(failed);
        }
    }

    /** {@inheritDoc} */
    public void setTimedOut(boolean timedOut) {
        super.setTimedOut(timedOut);
        for (CollectionTracker child : m_children) {
            child.setTimedOut(timedOut);
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public void setMaxRepititions(int maxRepititions) {
        for (CollectionTracker child : m_children) {
            child.setMaxRepititions(maxRepititions);
        }
    }

    /**
     * <p>isFinished</p>
     *
     * @return a boolean.
     */
    public boolean isFinished() {
        for (CollectionTracker child : m_children) {
            if (!child.isFinished()) {
                return false;
            }
        }
        return true;
    }

    /** {@inheritDoc} */
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
        
        // set the non repeaters and max repititions
        parentBuilder.setNonRepeaters(nonRepeaters);
        parentBuilder.setMaxRepetitions(maxRepititions == Integer.MAX_VALUE ? 1 : maxRepititions);
        
        // construct a response processor that tracks the changes and informs the response processors
        // for the child trackers
        return new ChildTrackerResponseProcessor(parentBuilder, builders, nonRepeaters, repeaters);
    }
}
