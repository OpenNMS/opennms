/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.dao.support;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.opennms.core.collections.LazySet;
import org.opennms.core.utils.PropertiesUtils;
import org.opennms.core.utils.PropertiesUtils.SymbolTable;
import org.opennms.netmgt.collection.api.StorageStrategy;
import org.opennms.netmgt.config.datacollection.ResourceType;
import org.opennms.netmgt.dao.api.ResourceStorageDao;
import org.opennms.netmgt.model.ExternalValueAttribute;
import org.opennms.netmgt.model.OnmsAttribute;
import org.opennms.netmgt.model.OnmsResource;
import org.opennms.netmgt.model.OnmsResourceType;
import org.opennms.netmgt.model.ResourcePath;
import org.opennms.netmgt.model.StringPropertyAttribute;
import org.springframework.orm.ObjectRetrievalFailureException;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Generic index resources are stored in paths like:
 *   snmp/1/${name}/${index}/ds.rrd
 *
 * The name and index elements depend on the implementation.
 *
 * Implementations are loaded from the data-collection configuration at run-time.
 */
public final class GenericIndexResourceType implements OnmsResourceType {
    private static final Pattern SUB_INDEX_PATTERN = Pattern.compile("^subIndex\\((.*)\\)$");
    private static final Pattern SUB_INDEX_ARGUMENTS_PATTERN = Pattern.compile("^(-?\\d+|n)(?:,\\s*(\\d+|n))?$");
    private static final Pattern HEX_PATTERN = Pattern.compile("^hex\\((.*)\\)$");
    private static final Pattern STRING_PATTERN = Pattern.compile("^string\\((.*)\\)$");

    private final String m_name;
    private final String m_label;
    private final String m_resourceLabelExpression;
    private final ResourceStorageDao m_resourceStorageDao;
    private final StorageStrategy m_storageStrategy;

    /**
     * <p>Constructor for GenericIndexResourceType.</p>
     *
     * @param resourceStorageDao a {@link org.opennms.netmgt.dao.api.ResourceStorageDao} object.
     * @param name a {@link java.lang.String} object.
     * @param label a {@link java.lang.String} object.
     * @param resourceLabelExpression a {@link java.lang.String} object.
     * @param storageStrategy a {@link org.opennms.netmgt.collection.api.StorageStrategy} object.
     */
    public GenericIndexResourceType(ResourceStorageDao resourceStorageDao, String name, String label, String resourceLabelExpression, StorageStrategy storageStrategy) {
        m_resourceStorageDao = resourceStorageDao;
        m_name = name;
        m_label = label;
        m_resourceLabelExpression = resourceLabelExpression;
        m_storageStrategy = storageStrategy;
    }

    /**
     * <p>getName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getName() {
        return m_name;
    }
    
    /**
     * <p>getLabel</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getLabel() {
        return m_label;
    }
    
    /**
     * <p>getStorageStrategy</p>
     *
     * @return a {@link org.opennms.netmgt.collection.api.StorageStrategy} object.
     */
    public StorageStrategy getStorageStrategy() {
        return m_storageStrategy;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isResourceTypeOnParent(OnmsResource parent) {
        if (parent == null) {
            return false;
        }
        return m_resourceStorageDao.exists(new ResourcePath(parent.getPath(), m_name), 1);
    }

    /** {@inheritDoc} */
    @Override
    public List<OnmsResource> getResourcesForParent(OnmsResource parent) {
        if (parent == null) {
            return Collections.emptyList();
        }

        List<OnmsResource> resources = Lists.newArrayList();

        List<String> indexes = getQueryableIndexes(new ResourcePath(parent.getPath(), m_name));
        for (String index : indexes) {
            resources.add(getResourceByPath(new ResourcePath(parent.getPath(), m_name, index), parent));
        }

        return OnmsResource.sortIntoResourceList(resources);
    }

    /** {@inheritDoc} */
    @Override
    public OnmsResource getChildByName(OnmsResource parent, String index) {
        if (parent == null) {
            return null;
        }

        final ResourcePath path = ResourcePath.get(parent.getPath(), getName(), index);
        if (!m_resourceStorageDao.exists(path, 0)) {
            throw new ObjectRetrievalFailureException(OnmsResource.class, path, "Generic resource with label " + m_label + " could not find resource at path: " + path, null);

        }

        return getResourceByPath(path, parent);
    }

    /** {@inheritDoc} */
    @Override
    public String getLinkForResource(OnmsResource resource) {
        return null;
    }

    /**
     * <p>getQueryableIndexesForNodeSource</p>
     *
     * @param nodeSource a String.
     * @return a {@link java.util.List} object.
     */
    private List<String> getQueryableIndexes(ResourcePath path) {
        return m_resourceStorageDao.children(path, 1).stream()
                .map(ResourcePath::getName)
                .collect(Collectors.toList());
    }

    public OnmsResource getResourceByPath(final ResourcePath path, final OnmsResource parent) {
        final Set<OnmsAttribute> set = new LazySet<OnmsAttribute>(new LazyResourceAttributeLoader(m_resourceStorageDao, path));
        final String index = path.getName();
        String label;
        if (m_resourceLabelExpression == null) {
            label = index;
        } else {
            SymbolTable symbolTable = new SymbolTable() {
                private int lastN;
                private boolean lastNSet = false;
                
                @Override
                public String getSymbolValue(String symbol) {
                    if (symbol.equals("index")) {
                        return index;
                    }
 
                    Matcher subIndexMatcher = SUB_INDEX_PATTERN.matcher(symbol);
                    if (subIndexMatcher.matches()) {
                        Matcher subIndexArgumentsMatcher = SUB_INDEX_ARGUMENTS_PATTERN.matcher(subIndexMatcher.group(1));
                        if (!subIndexArgumentsMatcher.matches()) {
                            // Invalid arguments
                            return null;
                        }
                        
                        List<String> indexElements = tokenizeIndex(index);
                        
                        int start;
                        int offset;
                        if ("n".equals(subIndexArgumentsMatcher.group(1)) && lastNSet) {
                            start = lastN;
                            lastNSet = false;
                        } else if ("n".equals(subIndexArgumentsMatcher.group(1))) {
                            // Invalid use of "n" when lastN is not set
                            return null;
                        } else {
                            offset = Integer.parseInt(subIndexArgumentsMatcher.group(1));
                            if (offset < 0) {
                                start = indexElements.size() + offset;
                            } else {
                                start = offset;
                            }
                        }

                        int end;
                        if ("n".equals(subIndexArgumentsMatcher.group(2))) {
                            end = start + Integer.parseInt(indexElements.get(start)) + 1;
                            start++;
                            lastN = end;
                            lastNSet = true;
                        } else {
                            if (subIndexArgumentsMatcher.group(2) == null) {
                                end = indexElements.size();
                            } else {                            
                                end = start + Integer.parseInt(subIndexArgumentsMatcher.group(2));
                            }
                        }
                        
                        if (start < 0 || start >= indexElements.size()) {
                            // Bogus index start
                            return null;
                        }
                        
                        if (end < 0 || end > indexElements.size()) {
                            // Bogus index end
                            return null;
                        }

                        final StringBuilder indexSubString = new StringBuilder();
                        for (int i = start; i < end; i++) {
                            if (indexSubString.length() != 0) {
                                indexSubString.append(".");
                            }
                            
                            indexSubString.append(indexElements.get(i));
                        }
                        
                        return indexSubString.toString();
                    }
                    
                    Matcher hexMatcher = HEX_PATTERN.matcher(symbol);
                    if (hexMatcher.matches()) {
                        String subSymbol = getSymbolValue(hexMatcher.group(1));
                        List<String> indexElements = tokenizeIndex(subSymbol);
                        
                        final StringBuilder hexString = new StringBuilder();
                        for (String indexElement : indexElements) {
                            if (hexString.length() > 0) {
                                hexString.append(":");
                            }
                            try {
                                hexString.append(String.format("%02X", Integer.parseInt(indexElement)));
                            } catch (NumberFormatException e) {
                                return null;
                            }
                        }
                        
                        return hexString.toString();
                    }
                    
                    Matcher stringMatcher = STRING_PATTERN.matcher(symbol);
                    if (stringMatcher.matches()) {
                        String subSymbol = getSymbolValue(stringMatcher.group(1));
                        List<String> indexElements = tokenizeIndex(subSymbol);
                        
                        StringBuffer stringString = new StringBuffer();
                        for (String indexElement : indexElements) {
                            stringString.append(String.format("%c", Integer.parseInt(indexElement)));
                        }
                        
                        return stringString.toString();
                    }
                    
                    for (OnmsAttribute attr : set) {
                        if (symbol.equals(attr.getName())) {
                            if (StringPropertyAttribute.class.isAssignableFrom(attr.getClass())) {
                                StringPropertyAttribute stringAttr = (StringPropertyAttribute) attr;
                                return stringAttr.getValue();
                            }
                            if (ExternalValueAttribute.class.isAssignableFrom(attr.getClass())) {
                                ExternalValueAttribute extAttr = (ExternalValueAttribute) attr;
                                return extAttr.getValue();
                            }
                        }
                    }
                    
                    return null;
                }

                private List<String> tokenizeIndex(final String index) {
                    List<String> indexElements = new ArrayList<>();
                    StringTokenizer t = new StringTokenizer(index, ".");
                    while (t.hasMoreTokens()) {
                        indexElements.add(t.nextToken());
                    }
                    return indexElements;
                }
            };
            
            label = PropertiesUtils.substitute(m_resourceLabelExpression, symbolTable);
        }

        final OnmsResource resource = new OnmsResource(index, label, this, set, path);
        resource.setParent(parent);
        return resource;
    }

    protected static Map<String, GenericIndexResourceType> createTypes(Map<String, ResourceType> configuredResourceTypes, ResourceStorageDao resourceStorageDao) {
        Map<String, GenericIndexResourceType> resourceTypes = Maps.newLinkedHashMap();
        List<ResourceType> resourceTypeList = new LinkedList<ResourceType>(configuredResourceTypes.values());
        Collections.sort(resourceTypeList, new Comparator<ResourceType>() {
            @Override
            public int compare(ResourceType r0, ResourceType r1) {
                // Sort by resource label, allowing the resource label to be null
                final Comparator<? super String> comparator = (a, b) -> a.compareTo(b);
                return Objects.compare(r0.getLabel(), r1.getLabel(), Comparator.nullsLast(comparator));
            }
        });
        for (ResourceType resourceType : resourceTypeList) {
            String className = resourceType.getStorageStrategy().getClazz();
            Class<?> cinst;
            try {
                cinst = Class.forName(className);
            } catch (ClassNotFoundException e) {
                throw new ObjectRetrievalFailureException(StorageStrategy.class, className,
                   "Could not load class '" + className + "' for resource type '" + resourceType.getName() + "'", e);
            }
            StorageStrategy storageStrategy;
            try {
                storageStrategy = (StorageStrategy) cinst.newInstance();
            } catch (InstantiationException e) {
                throw new ObjectRetrievalFailureException(StorageStrategy.class, className,
                    "Could not instantiate class '" + className + "' for resource type '" + resourceType.getName() + "'", e);
            } catch (IllegalAccessException e) {
                throw new ObjectRetrievalFailureException(StorageStrategy.class, className,
                    "Could not instantiate class '" + className + "' for resource type '" + resourceType.getName() + "'", e);
            }

            storageStrategy.setResourceTypeName(resourceType.getName());

            GenericIndexResourceType genericIndexResourceType =
                new GenericIndexResourceType(resourceStorageDao,
                                                  resourceType.getName(),
                                                  resourceType.getLabel(),
                                                  resourceType.getResourceLabel(),
                                                  storageStrategy);
            resourceTypes.put(genericIndexResourceType.getName(), genericIndexResourceType);
        }
        return resourceTypes;
    }

    @Override
    public String toString() {
        return "GenericIndexResourceType [name=" + m_name + ", label=" + m_label
                + ", resourceLabelExpression=" + m_resourceLabelExpression
                + ", resourceStorageDao=" + m_resourceStorageDao + ", storageStrategy="
                + m_storageStrategy + "]";
    }
}
