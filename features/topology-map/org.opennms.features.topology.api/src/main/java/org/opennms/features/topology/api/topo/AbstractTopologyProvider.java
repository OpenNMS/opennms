/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
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

package org.opennms.features.topology.api.topo;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.LoggerFactory;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;

public abstract class AbstractTopologyProvider extends DelegatingVertexEdgeProvider implements GraphProvider {    
    protected static final String SIMPLE_VERTEX_ID_PREFIX = "v";
	protected static final String SIMPLE_GROUP_ID_PREFIX = "g";
	protected static final String SIMPLE_EDGE_ID_PREFIX = "e";
	
	/**
	 * This class generates an unique id. 
	 * The generated id has the format '<prefix><counter>' (e.g. v100). 
	 * So the generator must be initialized with a prefix and the initial counter.
	 * 
	 * @author Markus von Rüden
	 *
	 */
    protected static abstract class IdGenerator {
        /** 
         * The topology provider. It is needed to initialize the counter.
         */
        private final AbstractTopologyProvider provider;
        /**
         * The prefix of the generated id. Must not be null.
         */
        private final String idPrefix;
        /**
         * The counter of the "next" (not current) id.
         */
        private int counter;
        /**
         * Defines if this generator is initialized or not.
         */
        private boolean initialized;

        protected IdGenerator(String idPrefix, AbstractTopologyProvider provider) {
            this.idPrefix = idPrefix;
            this.provider = provider;
        }

        /**
         * Returns the next id in format '<prefix><counter>' (e.g. v100). 
         * 
         * If an entry with the generated id (see {@link #createId()} 
         * already exists in {@link #provider} a new one is created. 
         * This process is done until a key is created which is not already in {@link #provider}
         * @return The next id in format '<prefix><counter>' (e.g. v100). 
         */
        public String getNextId() {
            try {
                initializeIfNeeded();
                while (!isValid(createId())) counter++;
                return createId();
            } finally {
                counter++;
            }
        }

        /**
         * Creates the id in format '<prefix><counter>' (e.g. v100)
         * @return the id in format '<prefix><counter>' (e.g. v100)
         */
        private String createId() {
            return idPrefix + counter;
        }

        /**
         * Returns the initial value of counter. 
         * 
         * Therefore the maximum number of each id from the {@link #getContent()} values are used.
         * A id can start with any prefix (or none) so only ids which starts with the same id as {@link #idPrefix} are considered.
         * If there is no matching content, 0 is returned.
         *   
         * @return The initial value of counter. 
         */
        private int getInitValue() {
            int max = 0;
            for (Ref ref : getContent()) {
                if (!ref.getId().startsWith(idPrefix)) continue;
                max = Math.max(max, extractIntegerFromId(ref.getId()));
            }
            return max;
        }

        /**
         * Returns true if the {@link #provider} does not contain a vertex id '<generatedId>', false otherwise. 
         * @param generatedId The generated id
         * @return true if the {@link #provider} does not contain a vertex id '<generatedId>', false otherwise.
         */
        @SuppressWarnings("deprecation")
        private boolean isValid(String generatedId) {
            return !provider.containsVertexId(new AbstractVertexRef(provider.getVertexNamespace(), generatedId));
        }

        public void reset() {
            counter = 0;
            initialized = false;
        }

        /**
         * Gets the integer value from the id. 
         * If the id does not match to this generator or the id cannot be parsed as an integer 0 is returned.
         * 
         * @param id the generated id. Should start with {@link #idPrefix}.
         * @return the integer value from the id. If the id does not match to this generator or the id cannot be parsed as an integer 0 is returned.
         */
        private int extractIntegerFromId(String id) {
            try {
                return Integer.parseInt(id.replaceAll(idPrefix, "").trim());
            } catch (NumberFormatException nfe) {
                return 0;
            } catch (IllegalArgumentException ilargex) {
                return 0;
            }
        }

        private void initializeIfNeeded() {
            if (!initialized) {
                counter = getInitValue();
                initialized = true;
            }
        }
        
        public abstract List<Ref> getContent();
    }

	private IdGenerator groupIdGenerator = new IdGenerator(SIMPLE_GROUP_ID_PREFIX, this) {
        @Override
        public List<Ref> getContent() {
            return new ArrayList<Ref>(getGroups());
        }
	};
	
	private IdGenerator edgeIdGenerator = new IdGenerator(SIMPLE_EDGE_ID_PREFIX, this) {
        @Override
        public List<Ref> getContent() {
            return new ArrayList<Ref>(getEdges());
        }
	};
	
	private IdGenerator vertexIdGenerator = new IdGenerator(SIMPLE_VERTEX_ID_PREFIX, this) {
	    @Override
	    public List<Ref> getContent() {
	        return new ArrayList<Ref>(getVerticesWithoutGroups());
        }
	};
	
	protected String getNextVertexId() {
	    return vertexIdGenerator.getNextId();
	}

	protected String getNextGroupId() {
	    return groupIdGenerator.getNextId();
	}

	protected String getNextEdgeId() {
	    return edgeIdGenerator.getNextId();
	}
	
    protected AbstractTopologyProvider(String namespace) {
		super(namespace);
	}
    
    public List<Vertex> getVerticesWithoutGroups() {
        return new ArrayList<Vertex>(
                Collections2.filter(getVertices(), new Predicate<Vertex>() {
                    public boolean apply(Vertex input) {
                        return input != null && !input.isGroup();
                    };
                }));
    }
    
    public List<Vertex> getGroups() {
        return new ArrayList<Vertex>(
                Collections2.filter(getVertices(), new Predicate<Vertex>() {
                    public boolean apply(Vertex input) {
                        return input != null && input.isGroup();
                    };
                }));
    }

    @Override
    public final void removeVertex(VertexRef... vertexId) {
        for (VertexRef vertex : vertexId) {
            if (vertex == null) continue;
            
            getSimpleVertexProvider().remove(vertexId);
            
            removeEdges(getEdgeIdsForVertex(vertex));
        }
    }

    @Override
    public final void addVertices(Vertex... vertices) {
        getSimpleVertexProvider().add(vertices);
    }

    @Override
    public final AbstractVertex addVertex(int x, int y) {
        String id = getNextVertexId();
        return addVertex(id, x, y);
    }
    
    protected final AbstractVertex addVertex(String id, int x, int y) {
        LoggerFactory.getLogger(getClass()).debug("Adding vertex in {} with ID: {}", getClass().getSimpleName(), id);
        AbstractVertex vertex = new SimpleLeafVertex(getVertexNamespace(), id, x, y);
        getSimpleVertexProvider().add(vertex);
        return vertex;
    }

    @Override
    public final AbstractVertex addGroup(String groupName, String groupIconKey) {
        String nextGroupId = getNextGroupId();
        return addGroup(nextGroupId, groupIconKey, groupName);
    }

    protected final AbstractVertex addGroup(String groupId, String iconKey, String label) {
        AbstractVertex vertex = new SimpleGroup(getVertexNamespace(), groupId);
        if (containsVertexId(vertex)) {
            throw new IllegalArgumentException("A vertex or group with id " + groupId + " already exists!");
        }
        LoggerFactory.getLogger(this.getClass()).debug("Adding a group: {}", groupId);
        vertex.setLabel(label);
        vertex.setIconKey(iconKey);
        addVertices(vertex);
        return vertex;
    }

    @Override
    public final void addEdges(Edge... edges) {
        getSimpleEdgeProvider().add(edges);
    }

    @Override
    public final void removeEdges(EdgeRef... edge) {
        getSimpleEdgeProvider().remove(edge);
    }

    @Override
    public final EdgeRef[] getEdgeIdsForVertex(VertexRef vertex) {
        if (vertex == null) return new EdgeRef[0];
        List<EdgeRef> retval = new ArrayList<EdgeRef>();
        for (Edge edge : getEdges()) {
            // If the vertex is connected to the edge then add it
            if (new RefComparator().compare(edge.getSource().getVertex(), vertex) == 0 || new RefComparator().compare(edge.getTarget().getVertex(), vertex) == 0) {
                retval.add(edge);
            }
        }
        return retval.toArray(new EdgeRef[0]);
    }

    @Override
	public Edge connectVertices(VertexRef sourceVertextId, VertexRef targetVertextId) {
        String nextEdgeId = getNextEdgeId();
        return connectVertices(nextEdgeId, sourceVertextId, targetVertextId);
    }

    protected final AbstractEdge connectVertices(String id, VertexRef sourceId, VertexRef targetId) {
        SimpleConnector source = new SimpleConnector(getEdgeNamespace(), sourceId.getId()+"-"+id+"-connector", sourceId);
        SimpleConnector target = new SimpleConnector(getEdgeNamespace(), targetId.getId()+"-"+id+"-connector", targetId);

        AbstractEdge edge = new AbstractEdge(getEdgeNamespace(), id, source, target);

        addEdges(edge);
        
        return edge;
    }

    @Override
    public void resetContainer() {
        clearVertices();
        clearEdges();
        clearCounters();
    }

    protected void clearCounters() {
        vertexIdGenerator.reset();
        groupIdGenerator.reset();
        edgeIdGenerator.reset();
    }
}

