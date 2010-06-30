/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2007 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created January 31, 2007
 *
 * Copyright (C) 2007 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */


package org.opennms.netmgt.correlation.drools;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.drools.RuleBase;
import org.drools.RuleBaseFactory;
import org.drools.WorkingMemory;
import org.drools.audit.WorkingMemoryFileLogger;
import org.drools.compiler.PackageBuilder;

/**
 * <p>CorrelationExample class.</p>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @version $Id: $
 */
public class CorrelationExample {

    /**
     * <p>main</p>
     *
     * @param args an array of {@link java.lang.String} objects.
     * @throws java.lang.Exception if any.
     */
    public static void main(final String[] args) throws Exception {

        final PackageBuilder builder = new PackageBuilder();
        builder.addPackageFromDrl( new InputStreamReader( CorrelationExample.class.getResourceAsStream( "CorrelationExample.drl" ) ) );

        final RuleBase ruleBase = RuleBaseFactory.newRuleBase();
        ruleBase.addPackage( builder.getPackage() );

        final WorkingMemory workingMemory = ruleBase.newStatefulSession();

        final WorkingMemoryFileLogger logger = new WorkingMemoryFileLogger( workingMemory );
        logger.setFileName( "log/correlation" );
        
        InputStream in = CorrelationExample.class.getResourceAsStream("simulation");
        try {
        	
        	Simulation simulation = new Simulation();
        	System.out.println("Loading Simulation");
        	simulation.load(in);
        	System.out.println("Executing Simulation");
        	simulation.simulate(workingMemory);
        	
        } finally {
        	if (in != null) in.close();
        }
        
        	
        logger.writeToDisk();
    }
    
    private static void sleep(int delay) {
    	try { Thread.sleep(delay); } catch (InterruptedException e) {}
		
	}
    
    public static class Simulation {
    	
    	public static class SimItem {
    		int m_delay;
    		EventBean m_event;
    		
    		public SimItem(int delay, EventBean event) {
    			m_delay = delay;
    			m_event = event;
    		}

			public void simulate(WorkingMemory memory) {
				sleep(m_delay);
    			System.out.println("Start simulation of "+this);
				memory.insert(m_event);
				memory.fireAllRules();
    			System.out.println("End simulation of "+this);
			}
    		
    	}
    	
    	Map<Integer, Node> m_nodes = new HashMap<Integer, Node>();
    	List<SimItem> m_eventSequence = new LinkedList<SimItem>();
    	
    	public void load(InputStream in) {
    		
            Scanner scanner = new Scanner(in);
            while(scanner.hasNext()) {
            	String lineType = scanner.next();
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
            		String nodeLabel = scanner.next();
            		Integer nodeId = scanner.nextInt();
            		assert m_nodes.get(nodeId) == null : "Already have a node with id "+nodeId;

            		Integer parentId = null;
            		if (scanner.hasNextInt()) {
            			parentId = scanner.nextInt();
            		}
            		
            		assert (parentId == null || m_nodes.containsKey(parentId)) : "Reference to parentId "+parentId+" that is not yet defined";
            		
            		Node parent = (parentId == null ? null : m_nodes.get(parentId));
            		
            		Node node = new Node(nodeId, nodeLabel, parent);
            		m_nodes.put(nodeId, node);
            		
            	} else if ("event".equals(lineType)) {
            		/*
            		 * expect line to be
            		 * event delay uei nodeid 
            		 */
            		int delay = scanner.nextInt();
            		String uei = scanner.next();
            		Integer nodeId = scanner.nextInt();
            		
            		assert m_nodes.containsKey(nodeId) : "Invalid nodeId "+nodeId;
            		
            		EventBean e = new EventBean(uei, m_nodes.get(nodeId));
            		
            		SimItem item = new SimItem(delay, e);
            		
            		m_eventSequence.add(item);
            		
            	}
            	
            }
    	}
    	
    	
    	public  void simulate(WorkingMemory memory) {
    		for( SimItem item : m_eventSequence ) {
    			item.simulate(memory);
    			System.out.println("Memory Size = " + getObjectCount(memory) );
    		}
    	}
    }

	public static class Outage {
    	private EventBean m_problem;
    	private EventBean m_resolution;
		private Node m_cause;
		private Node m_node;
    	
		public Outage(Node node, EventBean problem) {
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
		
		public void setResolution(EventBean resolution) {
			m_resolution = resolution;
		}
		
		public Node getCause() {
			return m_cause;
		}
		
		public void setCause(Node cause) {
			m_cause = cause;
		}
		
		public String toString() {
			return "Outage[ problem=" + m_problem + " , cause=" + m_cause + " , resolution="+m_resolution+" ]";
		}
		
    }

    public static class Node {
        private Integer m_id;
        private Node m_parent;
        private String m_label;

        public Node(Integer id, String label, Node parent) {
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
        
        public String toString( ) {
            return 
                "Node["
                + " id="+m_id
                + " , label="+m_label
                + " ]"
                ;
        }
    }
	public static class EventBean {
	    private String m_uei;
	    private Node m_node;

	    public EventBean(String uei, Node node) {
	        m_uei = uei;
	        m_node = node;
	    }

	    public String getUei() {
	        return m_uei;
	    }

	    public Node getNode() {
	        return m_node;
	    }

	    public String toString() {
	        return
	        "Event["
	               + " uei="+m_uei
	               + " , node="+m_node
	               + " ]";
	    }
	}


    
    public static class PossibleCause {
    	private Node m_node;
		private Outage m_outage;
		private boolean m_verified;
		
		public PossibleCause(Node node, Outage outage) {
			this(node, outage, false);
		}

		public PossibleCause(Node node, Outage outage, boolean verified) {
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
		
		public void setVerified(boolean verified) {
			m_verified = verified;
		}
		
		public String toString() {
			return "PossibleCause[ node=" + m_node + " , outage=" + m_outage + " ]";
		}
    }



    /**
     * <p>getObjectCount</p>
     *
     * @param memory a {@link org.drools.WorkingMemory} object.
     * @return a int.
     */
    public static int getObjectCount(WorkingMemory memory) {
        int count = 0;
        for(Iterator<?> it = memory.iterateObjects(); it.hasNext(); it.next()) {
            count++;
        }
        return count;
    }
    

}
