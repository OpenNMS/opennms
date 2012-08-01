/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2011 The OpenNMS Group, Inc.
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

package org.opennms.web.svclayer.support;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.opennms.netmgt.config.attrsummary.Attribute;
import org.opennms.netmgt.config.attrsummary.Resource;
import org.opennms.netmgt.config.attrsummary.Summary;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.dao.ResourceDao;
import org.opennms.netmgt.dao.RrdDao;
import org.opennms.netmgt.dao.support.FilterWalker;
import org.opennms.netmgt.dao.support.NodeSnmpResourceType;
import org.opennms.netmgt.filter.FilterDao;
import org.opennms.netmgt.model.AbstractEntityVisitor;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsResource;
import org.opennms.netmgt.model.RrdGraphAttribute;
import org.opennms.web.svclayer.RrdSummaryService;
import org.opennms.web.svclayer.SummarySpecification;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

/**
 * <p>DefaultRrdSummaryService class.</p>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 */
public class DefaultRrdSummaryService implements RrdSummaryService, InitializingBean {

    static class SummaryBuilder {
        private SummaryHolder m_root;
        private ResourceHolder m_currentResource;
        private Attribute m_currAttr;

        interface ResourceParent {
            public boolean isRoot();
            public void addResource(Resource resource);
            public void commit();
        }

        class SummaryHolder implements ResourceParent {
            Summary m_summary = new Summary();

            public void addResource(Resource resource) {
                m_summary.addResource(resource);
            }

            public void commit() {

            }

            public Summary getSummary() {
                return m_summary;
            }

            public boolean isRoot() {
                return true;
            }

            public String toString() {
                return "[root]";
            }

        }

        class ResourceHolder implements ResourceParent {
            ResourceParent m_parent;
            boolean m_commited = false;
            Resource m_resource;

            ResourceHolder(ResourceParent parent, String name) {
                Assert.notNull(parent, "parent must not be null");
                m_parent = parent;
                m_resource = new Resource();
                m_resource.setName(name);
            }

            public ResourceParent getParent() {
                return m_parent;
            }

            public boolean isCommited() {
                return m_commited;
            }

            public void commit() {
                if (isCommited()) return;
                if (m_parent != null) m_parent.commit();
                addSelf();
                m_commited = true;
            }

            public void addResource(Resource resource) {
                m_resource.addResource(resource);
            }

            protected Attribute addAttribute(String name) {
                Attribute attr = new Attribute();
                attr.setName(name);
                m_resource.addAttribute(attr);
                commit();
                return attr;
            }

            protected void addSelf() {
                if (getParent() == null) {
                    m_root.addResource(m_resource);
                } else {
                    getParent().addResource(m_resource);
                }
            }

            public String toString() {
                return (getParent() == null ? "[root]" : getParent().toString())+".["+m_resource.getName()+"]";
            }

            public boolean isRoot() {
                return false;
            }

        }

        SummaryBuilder() {
            m_root = new SummaryHolder();
        }


        Summary getSummary() {
            return m_root.getSummary();
        }

        public void addAttribute(String name) {
            Assert.state(m_currentResource != null, "addResource must be called before calling addAttribute");
            m_currAttr = m_currentResource.addAttribute(name);
        }

        public void setMin(double min){
            checkForCurrAttr();
            m_currAttr.setMin(min);

        }


        private void checkForCurrAttr() {
            Assert.state(m_currAttr != null, "addAttribute must be called before calling setMin,setMax or setAverage");
        }


        public void setAverage(double avg) {
            checkForCurrAttr();
            m_currAttr.setAverage(avg);
        }


        public void setMax(double max) {
            checkForCurrAttr();
            m_currAttr.setMax(max);
        }


        public void pushResource(String label) {
            ResourceParent parent = (m_currentResource == null ? m_root : m_currentResource);
            m_currentResource = new ResourceHolder(parent, label);
        }


        public void popResource() {
            Assert.state(m_currentResource != null, "you must push a resource before you can pop one");
            if (m_currentResource.getParent().isRoot()) {
                m_currentResource = null;
            } else {
                m_currentResource = (ResourceHolder)m_currentResource.getParent();
            }
        }
    }

    public FilterDao m_filterDao;
    public ResourceDao m_resourceDao;
    public RrdDao m_rrdDao;
    public NodeDao m_nodeDao;
    public Stats m_stats = new Stats();

    static class OpStats {
        private String m_name;
        private int m_count = 0;
        private long m_total = 0;
        private long m_lastStarted = -1;

        OpStats(String n) {
            m_name = n;
        }

        void begin() {
            m_count++;
            m_lastStarted = System.nanoTime();
        }

        void end() {
            long ended  = System.nanoTime();
            Assert.state(m_lastStarted >= 0, "must call begin before calling end");
            m_total += (ended - m_lastStarted);
            m_lastStarted = -1;
        }

        @Override
        public String toString() {
            double total = (double)m_total;
            return String.format("stats: %s: count=%d, totalTime=%f ms ( %f us/call )", m_name, m_count, total/1000000.0, total/(m_count*1000.0));
        }


    }

    static class Stats {
        Map<String, OpStats> map = new LinkedHashMap<String, OpStats>();
        public void begin(String operation) {
            if (!map.containsKey(operation)) {
                map.put(operation, new OpStats(operation));
            }
            map.get(operation).begin();
        }

        public void end(String operation) {
            map.get(operation).end();
        }

        @Override
        public String toString() {
            StringBuilder bldr = new StringBuilder(map.size()*50);
            for (OpStats opStat : map.values()) {
                bldr.append(opStat);
                bldr.append('\n');
            }
            return bldr.toString();
        }

    }

    /**
     * <p>getSummary</p>
     *
     * @param filterRule a {@link java.lang.String} object.
     * @param startTime a long.
     * @param endTime a long.
     * @param attributeSieve a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.config.attrsummary.Summary} object.
     */
    public Summary getSummary(String filterRule, final long startTime, final long endTime, final String attributeSieve) {
        m_stats.begin("getSummary");
        try {
            final SummaryBuilder bldr = new SummaryBuilder();


            FilterWalker walker = new FilterWalker();
            walker.setFilterDao(m_filterDao);
            walker.setNodeDao(m_nodeDao);
            walker.setFilter(filterRule);
            walker.setVisitor(new AbstractEntityVisitor() {
                public void visitNode(OnmsNode node) {

                    OnmsResource nodeResource = getResourceForNode(node);

                    bldr.pushResource(node.getLabel());

                    for(OnmsResource child : getChildResources1(nodeResource)) {
                        if (child.getResourceType() instanceof NodeSnmpResourceType) {
                            addAttributes(getResourceGraphAttributes(child));
                        } 
                    }

                    for(OnmsResource child : getChildResources2(nodeResource)) {
                        if (!(child.getResourceType() instanceof NodeSnmpResourceType)) {
                            addResource(child);
                        }
                    }

                    bldr.popResource();
                }

                private Collection<RrdGraphAttribute> getResourceGraphAttributes(OnmsResource child) {
                    String op = "getResourceGraphAttributes-"+child.getResourceType().getName();
                    m_stats.begin(op);
                    try {
                        return child.getRrdGraphAttributes().values();
                    } finally {
                        m_stats.end(op);
                    }
                }

                private List<OnmsResource> getChildResources1(OnmsResource nodeResource) {
                    m_stats.begin("getChildResources1");
                    try {
                        return nodeResource.getChildResources();
                    } finally {
                        m_stats.end("getChildResources1");
                    }
                }

                private List<OnmsResource> getChildResources2(OnmsResource nodeResource) {
                    m_stats.begin("getChildResources2");
                    try {
                        return nodeResource.getChildResources();
                    } finally {
                        m_stats.end("getChildResources2");
                    }
                }

                private OnmsResource getResourceForNode(OnmsNode node) {
                    m_stats.begin("getResourceForNode");
                    try {
                        return m_resourceDao.getResourceForNode(node);
                    } finally {
                        m_stats.end("getResourceForNode");
                    }
                }

                private void addResource(OnmsResource resource) {
                    addResource(resource, resource.getLabel());
                }

                private void addResource(OnmsResource resource, String label) {
                    Collection<RrdGraphAttribute> attrs = getResourceGraphAttributes(resource);
                    if (attrs.size() > 0) {
                        bldr.pushResource(label);
                        addAttributes(attrs);
                        bldr.popResource();
                    }
                }

                private void addAttributes(Collection<RrdGraphAttribute> attrs) {
                    m_stats.begin("addAttributes");
                    try {
                        for(RrdGraphAttribute attr : attrs) {
                            if (attr.getName().matches(attributeSieve)) {
                                bldr.addAttribute(attr.getName());
                                double[] values = getValues(attr);
                                bldr.setMin(values[0]);
                                bldr.setAverage(values[1]);
                                bldr.setMax(values[2]);
                            }
                        }
                    } finally {
                        m_stats.end("addAttributes");
                    }
                }

                private double[] getValues(RrdGraphAttribute attr) {
                    m_stats.begin("getValues");
                    try {
                        return m_rrdDao.getPrintValues(attr, "AVERAGE", startTime*1000, endTime*1000, "MIN", "AVERAGE", "MAX");
                    } finally {
                        m_stats.end("getValues");
                    }
                }

            });
            walker.walk();

            return bldr.getSummary();
        } finally {
            m_stats.end("getSummary");
        }
    }

    /**
     * <p>afterPropertiesSet</p>
     *
     * @throws java.lang.Exception if any.
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.state(m_filterDao != null, "filterDao property must be set");
        Assert.state(m_resourceDao != null, "resourceDao property must be set");
        Assert.state(m_rrdDao != null, "rrdDao property must be set");
        Assert.state(m_nodeDao != null, "nodeDao property must be set");
    }

    /**
     * <p>setFilterDao</p>
     *
     * @param filterDao a {@link org.opennms.netmgt.filter.FilterDao} object.
     */
    public void setFilterDao(FilterDao filterDao) {
        m_filterDao = filterDao;
    }

    /**
     * <p>setResourceDao</p>
     *
     * @param resourceDao a {@link org.opennms.netmgt.dao.ResourceDao} object.
     */
    public void setResourceDao(ResourceDao resourceDao) {
        m_resourceDao = resourceDao;
    }

    /**
     * <p>setRrdDao</p>
     *
     * @param rrdDao a {@link org.opennms.netmgt.dao.RrdDao} object.
     */
    public void setRrdDao(RrdDao rrdDao) {
        m_rrdDao = rrdDao;
    }

    /**
     * @return the nodeDao
     */
    public NodeDao getNodeDao() {
        return m_nodeDao;
    }

    /**
     * @param nodeDao the nodeDao to set
     */
    public void setNodeDao(NodeDao nodeDao) {
        this.m_nodeDao = nodeDao;
    }

    /** {@inheritDoc} */
    public Summary getSummary(SummarySpecification spec) {
        return getSummary(spec.getFilterRule(), spec.getStartTime(), spec.getEndTime(), spec.getAttributeSieve());
    }

}
