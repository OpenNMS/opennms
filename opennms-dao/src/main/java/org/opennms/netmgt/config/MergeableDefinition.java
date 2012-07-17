/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config;

import java.math.BigInteger;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ListIterator;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.snmp.Definition;
import org.opennms.netmgt.config.snmp.Range;
import org.opennms.netmgt.model.discovery.IPAddressRange;
import org.opennms.netmgt.model.discovery.IPAddressRangeSet;

/**
 * This is a wrapper class for the Definition class from the config package.  Has the logic for 
 * comparing definitions, sorting child elements, etc.
 * 
 * @author <a href="mailto:david@opennms.org>David Hustace</a>
 *
 */
final class MergeableDefinition {
    
    /**
     * This field should remaining encapsulated for there is
     * synchronization in the getter.
     * 
     */
    private final Definition m_snmpConfigDef;
    private IPAddressRangeSet m_configRanges = new IPAddressRangeSet();
    
    /**
     * <p>Constructor for MergeableDefinition.</p>
     *
     * @param def a {@link org.opennms.netmgt.config.snmp.Definition} object.
     */
    public MergeableDefinition(Definition def) {
        m_snmpConfigDef = def;
        
        for (Range r : def.getRangeCollection()) {
            m_configRanges.add(new IPAddressRange(r.getBegin(), r.getEnd()));
        }
        
        for(String s : def.getSpecificCollection()) {
            m_configRanges.add(new IPAddressRange(s));
        }
       
        
    }
    
    public IPAddressRangeSet getAddressRanges() {
        return m_configRanges;
    }

    /**
     * This method is called when a definition is found in the config and
     * that has the same attributes as the params in the configureSNMP event and
     * the IP specific/range needs to be merged into the definition.
     *
     * @param eventDefefinition a {@link org.opennms.netmgt.config.MergeableDefinition} object.
     */
    protected void mergeMatchingAttributeDef(MergeableDefinition eventDefinition)  {
        
        m_configRanges.addAll(eventDefinition.getAddressRanges());
        
        getConfigDef().removeAllRange();
        getConfigDef().removeAllSpecific();
        
        for(IPAddressRange range : m_configRanges) {
            if (range.isSingleton()) {
                getConfigDef().addSpecific(range.getBegin().toUserString());
            } else {
                Range xmlRange = new Range();
                xmlRange.setBegin(range.getBegin().toUserString());
                xmlRange.setEnd(range.getEnd().toUserString());
                getConfigDef().addRange(xmlRange);
            }
            
        }
        
        
//        if (eventDefinition.isSpecific()) {
//            handleSpecificMerge(eventDefinition.getConfigDef());
//        } else {
//            handleRangeMerge(eventDefinition.getConfigDef());
//        }
        
        
    }
    
    /**
     * Little helper method for determining if this definition
     * from a configureSNMP event containts a specific IP address vs.
     * a range.
     *
     * @return true if the number of ranges in the def is 0.
     */
    public boolean isSpecific() {
        
        boolean specific = false;
        if (getConfigDef().getRangeCount() == 0) {
            specific = true;
        }
        return specific;
    }
    
    /**
     * This method handles the updating of the SnmpConfig when
     * the definition to be merged contains a specific IP address.
     * 
     * @param eventDef a definition created to represent the values
     *  passed in the configureSNMP event params
     */
    @SuppressWarnings("unused")
	private void handleSpecificMerge(final Definition eventDef)  {
        
        if (hasRangeMatchingSpecific(eventDef.getSpecific(0))) {
            log().error("handleSpecificMerge: definition already contains a range that matches requested SNMP specific change: " + this);
        } else if (hasMatchingSpecific(eventDef.getSpecific(0))) {
            log().error("handleSpecificMerge: definition already contains a specific that matches requested SNMP specific change: " + this);
        } else {
            getConfigDef().addSpecific(eventDef.getSpecific(0));
            sortSpecifics();
        }
    }

    private ThreadCategory log() {
        return ThreadCategory.getInstance(getClass());
    }
    
    /**
     * This method handles the updating of the SnmpConfig when
     * the definition to be merged contains a range.
     * 
     * @param eventDef a definition created to represent the values
     *  passed in the configureSNMP event params
     */
    @SuppressWarnings("unused")
	private void handleRangeMerge(final Definition eventDef) {
        
        //first remove any specifics that would be eclipsed by this new range
        while (hasSpecificMatchingNewRange(eventDef.getRange(0))) {
            String spec = findSpecificMatchingNewRange(eventDef.getRange(0));
            getConfigDef().removeSpecific(spec);
        }
        mergeNewRangeIntoDef(eventDef.getRange(0));
    }
    
    /**
     * Enumerates over the list of specifics in a definition
     * looking for a matching specific element.  Assumes isSpecific()
     * returns true.
     *
     * @param specific IP address as a string
     * @return true if def has a matching specific IP address already
     */
    public boolean hasMatchingSpecific(final String specific) {
        boolean specificMatches = false;
        for (String defSpecific : getConfigDef().getSpecificCollection()) {
            if (defSpecific.equals(specific)) {
                specificMatches = true;
                break;
            }
        }
        return specificMatches;
    }
    
    /**
     * Simple method to add readability
     *
     * @param specific IP address
     * @return true if the definition has a range covering
     *  the specified specific IP address
     */
    public boolean hasRangeMatchingSpecific(final String specific) {
        return (findRangeMatchingSpecific(specific) != null);
    }
    
    /**
     * Analyzes the definition to determine if one of its ranges
     * matches the specified specific IP address
     *
     * @param specific a {@link java.lang.String} object.
     * @return the matching range
     */
    public Range findRangeMatchingSpecific(final String specific) {
        Range matchingRange = null;
        for (Range erange : getConfigDef().getRangeCollection()) {
            MergeableRange range = new MergeableRange(erange);
            if (range.coversSpecific(specific)) {
                matchingRange = range.getRange();
                break;
            }
        }
        return matchingRange;
    }
    
    /**
     * Responsible for merging new ranges in to this definition.
     *
     * @param newRange a {@link org.opennms.netmgt.config.common.Range} object.
     */
    public void mergeNewRangeIntoDef(final Range newRange) {
        purgeEclipsedRangesInDef(newRange);
        
        if (getConfigDef().getRangeCount() == 0) {
            getConfigDef().addRange(newRange);
        } else {
            boolean overlapped = mergeOverlappingRanges(newRange);
            if(!overlapped) {
                getConfigDef().addRange(newRange);
            }
        }
    }
    
    /**
     * Removes in ranges in the defintion that are eclipsed
     * by the new range.
     *
     * @param range a {@link org.opennms.netmgt.config.common.Range} object.
     */
    public void purgeEclipsedRangesInDef(final Range range) {
        Range[] ranges = getConfigDef().getRange();
        MergeableRange newRange = new MergeableRange(range);
        
        for (int i = 0; i < ranges.length; i++) {
            Range rng = ranges[i];
            
            if (newRange.eclipses(rng)) {
                getConfigDef().removeRange(rng);
            }
        }
    }
    
    /**
     * The passed range is evaluated against the existing ranges in the
     * definition and updates the def range if it overlaps or eclipses
     * a def range.
     *
     * @param range a {@link org.opennms.netmgt.config.common.Range} object.
     * @return the state of having updated any ranges in the definition
     *   due to being affected by the new range.
     */
    public boolean mergeOverlappingRanges(final Range range) {
        boolean overlapped = false;
        sortRanges();
        MergeableRange newRange = new MergeableRange(range);
        Range[] ranges = getConfigDef().getRange();
        for (int i = 0; i < ranges.length; i++) {
            Range defRange = ranges[i];
            
            if (newRange.equals(defRange)) {
                overlapped = true;
            } else if (newRange.overlapsBegin(defRange)) {
                defRange.setBegin(newRange.getRange().getBegin());
                overlapped = true;
            } else if (newRange.overlapsEnd(defRange)) {
                defRange.setEnd(newRange.getRange().getEnd());
                overlapped = true;
            } else if (newRange.eclipses(defRange)) {
                defRange.setBegin(newRange.getRange().getBegin());
                defRange.setEnd(newRange.getRange().getEnd());
                overlapped = true;
            }
        }
        sortRanges();
        return overlapped;
    }
    
    /**
     * Sorts the specifics in the current wrapped definition.
     */
    public void sortSpecifics() {
        String[] specifics = getConfigDef().getSpecific();
        Arrays.sort(specifics, new SpecificComparator());
        getConfigDef().setSpecific(specifics);
    }
    
    /**
     * Sorts ranges with the current wrapped definition.
     */
    public void sortRanges() {
        Range[] ranges = getConfigDef().getRange();
        Arrays.sort(ranges, new RangeComparator());
        getConfigDef().setRange(ranges);
    }
    
    /**
     * <p>hasSpecificMatchingNewRange</p>
     *
     * @param eventRange a {@link org.opennms.netmgt.config.common.Range} object.
     * @return a boolean.
     */
    public boolean hasSpecificMatchingNewRange(final Range eventRange) {
        return (findSpecificMatchingNewRange(eventRange) != null);
    }
    
    /**
     * <p>findSpecificMatchingNewRange</p>
     *
     * @param eventRange a {@link org.opennms.netmgt.config.common.Range} object.
     * @return a {@link java.lang.String} object.
     */
    public String findSpecificMatchingNewRange(final Range eventRange) {
        String matchingSpecific = null;
        for (String spec : getConfigDef().getSpecificCollection()) {
            MergeableRange range = new MergeableRange(eventRange);
            
            if (range.coversSpecific(spec)) {
                matchingSpecific = spec;
                break;
            }
        }
        return matchingSpecific;
    }
    
    /**
     * <p>getConfigDef</p>
     *
     * @return a {@link org.opennms.netmgt.config.snmp.Definition} object.
     */
    final public Definition getConfigDef() {
        return m_snmpConfigDef;
    }
    
    final private boolean compareStrings(String str1, String str2) {
        boolean match = false;
        if (str1 == null && str2 == null) {
            match = true;
        } else if (str1 == null || str2 == null) {
            match = false;
        } else if (str1.equals(str2)) {
            match = true;
        }
        return match;
    }

    /**
     * Removes the specified specific from the wrapped definition.
     *
     * @param specific a {@link java.lang.String} object.
     */
    public void purgeSpecificFromDef(final String specific) {
        String[] specs = getConfigDef().getSpecific();
        for (int i = 0; i < specs.length; i++) {
            String spec = specs[i];

            if (spec.equals(specific)) {
                getConfigDef().removeSpecific(spec);
            }
        }
        
        Range[] ranges = getConfigDef().getRange();
        for (int i = 0; i < ranges.length; i++) {
            MergeableRange range = new MergeableRange(ranges[i]);
            
            if (range.coversSpecific(specific)) {
                Range newRange = range.removeSpecificFromRange(specific);
                if (newRange != null) {
                    getConfigDef().addRange(newRange);
                }
            }
        }
    }
    
    /**
     * Call this method to optimize the specifics in the wrapped definition.
     */
    public void optimizeSpecifics() {
        adjustRangesWithAdjacentSpecifics();
        removeSpecificsEclipsedByRanges();
        convertAdjacentSpecficsIntoRange();
    }
    
    /**
     * This method moves specifics that are adjacent to ranges in to the range.
     */
    private void adjustRangesWithAdjacentSpecifics() {
        sortSpecifics();
        sortRanges();
        String[] specifics = getConfigDef().getSpecific();
        
        for (int i = 0; i < specifics.length; i++) {
            MergeableSpecific specific = new MergeableSpecific(specifics[i]);
            for (Range range : getConfigDef().getRangeCollection()) {
                if (new BigInteger("-1").equals(InetAddressUtils.difference(specific.getSpecific(), range.getBegin()))) {
                    getConfigDef().removeSpecific(specific.getSpecific());
                    range.setBegin(specific.getSpecific());
                } else if (new BigInteger("1").equals(InetAddressUtils.difference(specific.getSpecific(), range.getEnd()))) {
                    getConfigDef().removeSpecific(specific.getSpecific());
                    range.setEnd(specific.getSpecific());
                }
            }
        }
        sortSpecifics();
        sortRanges();
    }
    
    /**
     * This method removes any specifics from the wrapped definition that are covered one of its ranges.
     *
     */
    private void removeSpecificsEclipsedByRanges() {
        sortSpecifics();
        String[] specifics = getConfigDef().getSpecific();
        for (int i = 0; i < specifics.length; i++) {
            String specific = specifics[i];
            if (hasRangeMatchingSpecific(specific)) {
                getConfigDef().removeSpecific(specific);
            }
        }
        sortSpecifics();
    }
    
    /**
     * Converts specifics into ranges when their diffs are abs() 1
     */
    private void convertAdjacentSpecficsIntoRange() {
        sortSpecifics();
        List<String> specificList = new ArrayList<String>(new LinkedHashSet<String>(getConfigDef().getSpecificCollection()));
        List<Range> definedRanges = Arrays.asList(getConfigDef().getRange());
        ArrayList<Range> newRanges = new ArrayList<Range>();
        
        if (specificList.size() > 1) {
            for (ListIterator<String> it = specificList.listIterator(); it.hasNext();) {
                final MergeableSpecific specific = new MergeableSpecific(it.next());
                final Range newRange = new Range();
                newRange.setBegin(specific.getSpecific());
                while (it.hasNext()) {
                    String nextSpecific = (String)it.next();
                    if (new BigInteger("-1").equals(InetAddressUtils.difference(specific.getSpecific(), nextSpecific))) {
                        newRange.setEnd(nextSpecific);
                        getConfigDef().removeSpecific(specific.getSpecific());
                        getConfigDef().removeSpecific(nextSpecific);
                        specific.setSpecific(nextSpecific);
                    } else {
                        it.previous();
                        break;
                    }
                }
                
                if (newRange.getEnd() != null) {
                    newRanges.add(newRange);
                }
            }
            newRanges.addAll(definedRanges);
            getConfigDef().setRange(newRanges);
            sortRanges();
        }
    }
    
    /**
     * Removes the specifics and ranges covered by the range specified in the parameter from the current wrapped definition.
     *
     * @param eventRange a {@link org.opennms.netmgt.config.common.Range} object.
     */
    public void purgeRangeFromDef(final Range eventRange) {
        MergeableRange range = new MergeableRange(eventRange);
        
        sortSpecifics();
        String[] specs = getConfigDef().getSpecific();
        
        for (int i = 0; i < specs.length; i++) {
            String spec = specs[i];
            if (range.coversSpecific(spec)) {
                getConfigDef().removeSpecific(spec);
            }
        }
        
        sortRanges();
        Range[] ranges = getConfigDef().getRange();
        for (int i = 0; i < ranges.length; i++) {
            Range defRng = ranges[i];

            try {
                if (range.eclipses(defRng)) {
                    getConfigDef().removeRange(defRng);
                } else if (range.withInRange(defRng)) {
                    Range newRange = new Range();
                    newRange.setBegin(InetAddressUtils.incr(range.getLast().getSpecific()));
                    newRange.setEnd(defRng.getEnd());
                    getConfigDef().addRange(newRange);
                    defRng.setEnd(InetAddressUtils.decr(range.getFirst().getSpecific()));
                } else if (range.overlapsBegin(defRng)) {
                    defRng.setBegin(InetAddressUtils.incr(range.getLast().getSpecific()));
                } else if (range.overlapsEnd(defRng)) {
                    defRng.setEnd(InetAddressUtils.decr(range.getFirst().getSpecific()));
                }
            } catch (UnknownHostException e) {
                ThreadCategory.getInstance(getClass()).error("Error converting string to IP address: " + e.getMessage(), e);
            }
        }
    }
    
    /**
     * Optimizes the ranges in the wrapped definition by making the definition as small and
     * as ordered as possible.
     */
    public void optimizeRanges() {
        sortRanges();
        Range[] ranges = getConfigDef().getRange();
        
        for (int i = 0; i < ranges.length-1; i++) {
            Range firstRange = ranges[i];
            Range nextRange = ranges[i+1];
            optimizeAdjacentRanges(firstRange, nextRange);
        }
        optimizeZeroLengthRanges();
    }
    
    /**
     * Converts ranges in the wrapped defintion that have equal begin and end addresses.
     */
    private void optimizeZeroLengthRanges() {
        Range[] ranges = getConfigDef().getRange();
        
        for (int i = 0; i < ranges.length; i++) {
            Range range = ranges[i];
            
            if (range.getBegin().equals(range.getEnd())) {
                if (!hasMatchingSpecific(range.getBegin())) {
                    getConfigDef().addSpecific(range.getBegin());
                }
                getConfigDef().removeRange(range);
            }
        }
    }
    
    /**
     * Adjusts 2 ordered ranges in the wrapped definition by determining if the nextRange is
     * either eclipsed, or overlapped by its previousRange.
     * 
     * @param previousRange
     * @param nextRange
     */
    private void optimizeAdjacentRanges(final Range previousRange, final Range nextRange) {
        MergeableRange range = new MergeableRange(previousRange);
        
        if (range.equals(nextRange)) {
            getConfigDef().removeRange(previousRange);
            
        } else if (range.eclipses(nextRange)) {
            
            //We have to do this because of the side effects of object references
            nextRange.setBegin(previousRange.getBegin());
            nextRange.setEnd(previousRange.getEnd());
            getConfigDef().removeRange(previousRange);
            
        } else if (range.isAdjacentToBegin(nextRange) || range.overlapsBegin(nextRange)){
            nextRange.setBegin(previousRange.getBegin());
            getConfigDef().removeRange(previousRange);
            
        } else if (range.isAdjacentToEnd(nextRange) || range.overlapsEnd(nextRange)) {
            
            //this "probably" should never happen
            nextRange.setEnd(previousRange.getEnd());
            getConfigDef().removeRange(previousRange);
        }
    }

    boolean matches(MergeableDefinition other) {
        boolean compares = compareStrings(getConfigDef().getReadCommunity(), other.getConfigDef().getReadCommunity())
                && getConfigDef().getPort() == other.getConfigDef().getPort() 
                && getConfigDef().getRetry() == other.getConfigDef().getRetry()
                && getConfigDef().getTimeout() == other.getConfigDef().getTimeout()
                && compareStrings(getConfigDef().getVersion(), other.getConfigDef().getVersion());
        return compares;
    }
    
    boolean isEmpty(String s) {
        return s == null || "".equals(s.trim());
    }
    
    boolean isTrivial() {
        return isEmpty(getConfigDef().getReadCommunity()) 
        && isEmpty(getConfigDef().getVersion())
        && !getConfigDef().hasPort()
        && !getConfigDef().hasRetry()
        && !getConfigDef().hasTimeout();
    }


    void removeRanges(MergeableDefinition eventDefinition) {
        
        m_configRanges.removeAll(eventDefinition.getAddressRanges());

        getConfigDef().removeAllRange();
        getConfigDef().removeAllSpecific();
        
        for(IPAddressRange r : m_configRanges) {
            if (r.isSingleton()) {
                getConfigDef().addSpecific(r.getBegin().toUserString());
            } else {
                Range xmlRange = new Range();
                xmlRange.setBegin(r.getBegin().toUserString());
                xmlRange.setEnd(r.getEnd().toUserString());
                getConfigDef().addRange(xmlRange);
            }
            
        }

//        if (eventDefinition.isSpecific()) {
//            purgeSpecificFromDef(eventDefinition.getConfigDef().getSpecific(0));
//        } else {
//            purgeRangeFromDef(eventDefinition.getConfigDef().getRange(0));
//        }
    }

    boolean isEmpty() {
        return getConfigDef().getRangeCount() < 1 && getConfigDef().getSpecificCount() < 1;
    }

}
