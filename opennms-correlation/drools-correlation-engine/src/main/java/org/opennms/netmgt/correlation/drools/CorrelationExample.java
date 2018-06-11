/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.correlation.drools;

import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.drools.core.io.impl.ClassPathResource;
import org.kie.api.KieServices;
import org.kie.api.logger.KieRuntimeLogger;
import org.kie.api.runtime.KieSession;
import org.kie.internal.utils.KieHelper;

/**
 * <p>CorrelationExample class.</p>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @version $Id: $
 */
public abstract class CorrelationExample {

    /**
     * <p>main</p>
     *
     * @param args an array of {@link java.lang.String} objects.
     * @throws java.lang.Exception if any.
     */
    public static void main(final String[] args) throws Exception {
        final KieSession session = new KieHelper()
            .addResource(new ClassPathResource("CorrelationExample.drl"))
            .build()
            .newKieSession();

        KieRuntimeLogger logger = KieServices.Factory.get().getLoggers().newFileLogger(session, "log/correlation");
        try (InputStream in = CorrelationExample.class.getResourceAsStream("simulation")) {
        	final Simulation simulation = new Simulation();
        	System.out.println("Loading Simulation");
        	simulation.load(in);
        	System.out.println("Executing Simulation");
        	simulation.simulate(session);
        }
        logger.close();
    }
    
    private static void sleep(final int delay) {
    	try { Thread.sleep(delay); } catch (InterruptedException e) {}
	}

    public static class Simulation {
    	
    	public static class SimItem {
    		final int m_delay;
    		final EventBean m_event;

    		public SimItem(final int delay, final EventBean event) {
    			m_delay = delay;
    			m_event = event;
    		}

			public void simulate(final KieSession session) {
				sleep(m_delay);
    			System.out.println("Start simulation of "+this);
    			session.insert(m_event);
    			session.fireAllRules();
    			System.out.println("End simulation of "+this);
			}
    	}

    	final Map<Integer, Node> m_nodes = new HashMap<Integer, Node>();
    	final List<SimItem> m_eventSequence = new LinkedList<>();
    	
    	public void load(final InputStream in) {
    		
    		final Scanner scanner = new Scanner(in);
            while(scanner.hasNext()) {
            	final String lineType = scanner.next();
            	if ("#".equals(lineType)) {
            		scanner.nextLine();
            	}
            	else if ("node".equals(lineType)) {
            		/* expect line to be
            		 * node <nodeLabel> <nodeid> (<parentnodeid>?) 
            		 * 
            		 * Note: parent nodes need to be defined before their children
            		 * If the parentnodeid is missing then we assume that it has no parent
            		 */
            		final String nodeLabel = scanner.next();
            		final Integer nodeId = scanner.nextInt();
            		assert m_nodes.get(nodeId) == null : "Already have a node with id "+nodeId;

            		Integer parentId = null;
            		if (scanner.hasNextInt()) {
            			parentId = scanner.nextInt();
            		}
            		
            		assert (parentId == null || m_nodes.containsKey(parentId)) : "Reference to parentId "+parentId+" that is not yet defined";
            		
            		final Node parent = (parentId == null ? null : m_nodes.get(parentId));
            		final Node node = new Node(nodeId, nodeLabel, parent);
            		m_nodes.put(nodeId, node);
            		
            	} else if ("event".equals(lineType)) {
            		/*
            		 * expect line to be
            		 * event delay uei nodeid 
            		 */
            		final int delay = scanner.nextInt();
            		final String uei = scanner.next();
            		final Integer nodeId = scanner.nextInt();

            		assert m_nodes.containsKey(nodeId) : "Invalid nodeId "+nodeId;
            		
            		final EventBean e = new EventBean(uei, m_nodes.get(nodeId));
            		final SimItem item = new SimItem(delay, e);
            		m_eventSequence.add(item);
            		
            	}
            	scanner.close();
            }
    	}

    	public  void simulate(final KieSession session) {
    		for (final SimItem item : m_eventSequence) {
    			item.simulate(session);
    			System.out.println("Memory Size = " + session.getObjects().size() );
    		}
    	}
    }

	public static class Outage {
    	private EventBean m_problem;
    	private EventBean m_resolution;
		private Node m_cause;
		private Node m_node;
    	
		public Outage(final Node node, final EventBean problem) {
			m_node = node;
    		m_problem = problem;
    	}
		
		public Node getNode() {
			return m_node;
		}
		
		public EventBean getProblem() {
			return m_problem;
		}
		
		public EventBean getResolution() {
			return m_resolution;
		}
		
		public void setResolution(final EventBean resolution) {
			m_resolution = resolution;
		}
		
		public Node getCause() {
			return m_cause;
		}
		
		public void setCause(final Node cause) {
			m_cause = cause;
		}
		
            @Override
		public String toString() {
			return new ToStringBuilder(this)
				.append("problem", m_problem)
				.append("cause", m_cause)
				.append("resolution", m_resolution)
				.toString();
		}
		
    }

    public static class Node {
        private Integer m_id;
        private Node m_parent;
        private String m_label;

        public Node(final Integer id, final String label, final Node parent) {
            m_id = id;
            m_label = label;
            m_parent = parent;
        }
        
        public Integer getId() {
            return m_id;
        }
        
        public Node getParent() {
            return m_parent;
        }
        
        public String getLabel() {
            return m_label;
        }
        
        @Override
        public String toString() {
        	return new ToStringBuilder(this)
        		.append("id", m_id)
        		.append("label", m_label)
        		.toString();
        }
    }
	public static class EventBean {
	    private String m_uei;
	    private Node m_node;

	    public EventBean(final String uei, final Node node) {
	        m_uei = uei;
	        m_node = node;
	    }

	    public String getUei() {
	        return m_uei;
	    }

	    public Node getNode() {
	        return m_node;
	    }

            @Override
	    public String toString() {
	    	return new ToStringBuilder(this)
	    		.append("uei", m_uei)
	    		.append("node", m_node)
	    		.toString();
	    }
	}


    
    public static class PossibleCause {
    	private Node m_node;
		private Outage m_outage;
		private boolean m_verified;
		
		public PossibleCause(final Node node, final Outage outage) {
			this(node, outage, false);
		}

		public PossibleCause(final Node node, final Outage outage, final boolean verified) {
    		m_node = node;
    		m_outage = outage;
    		m_verified = verified;
    	}
		
		public Node getNode() {
			return m_node;
		}
		
		public Outage getOutage() {
			return m_outage;
		}
		
		public boolean isVerified() {
			return m_verified;
		}
		
		public void setVerified(final boolean verified) {
			m_verified = verified;
		}
		
            @Override
		public String toString() {
			return new ToStringBuilder(this)
				.append("node", m_node)
				.append("outage", m_outage)
				.toString();
		}
    }
}
