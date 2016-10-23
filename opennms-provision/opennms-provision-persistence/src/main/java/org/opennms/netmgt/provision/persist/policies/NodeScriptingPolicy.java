/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision.persist.policies;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleBindings;

import org.apache.commons.jexl2.Expression;
import org.apache.commons.jexl2.JexlEngine;
import org.apache.commons.jexl2.MapContext;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.provision.BasePolicy;
import org.opennms.netmgt.provision.NodePolicy;
import org.opennms.netmgt.provision.annotations.Policy;
import org.opennms.netmgt.provision.annotations.Require;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Component;

@Component
/**
 * <p>NodeCategorySettingPolicy class.</p>
 *
 * @author Jeff Gehlbach <jeffg@opennms.org>
 * @version $Id: $
 */
@Scope("prototype")
@Policy("Scripted Node Actions")
public class NodeScriptingPolicy extends BasePolicy<OnmsNode> implements NodePolicy {
    private static final Logger LOG = LoggerFactory.getLogger(NodeScriptingPolicy.class);

    private String m_qualifier;
    private String m_engine;
    private String m_scriptUrl;

    /** {@inheritDoc} */
    @Override
    public OnmsNode act(final OnmsNode node) {
    	if (getQualifier() == null) {
    		LOG.error("Qualifier expression is not set. Returning unmodified node: {}", node);
    		return node;
    	}
    	if (! qualifies(node)) {
    		LOG.info("Qualifier expression '{}' returned false. Returning unmodified node: {}", m_qualifier, node);
    		return node;
    	}
    	if (m_engine == null || m_scriptUrl == null) {
    		LOG.error("The 'engine' and 'scriptUrl' parameters must be set. Returning unmodified node: {}", node);
    		return node;
    	}

    	UrlResource scriptResource;
    	ScriptEngine engine = new ScriptEngineManager().getEngineByName(m_engine);
    	if (engine == null) {
    		StringBuilder sb = new StringBuilder("[ ");
    		for (ScriptEngineFactory sef : new ScriptEngineManager().getEngineFactories()) {
    			sb.append(sef.getLanguageName()).append(" (").append(sef.getLanguageVersion()).append("), ");
    		}
    		sb.append("]");
    		LOG.error("Failed to construct script engine for name '{}'. Returning unmodified node: {}", m_engine, node);
    		LOG.error("Available script engines are: {}", sb.toString());
    		return node;
    	}
    	try {
    		scriptResource = new UrlResource(m_scriptUrl);
    		Reader scriptReader = new InputStreamReader(scriptResource.getInputStream());
    		Bindings bindings = new SimpleBindings();
    		bindings.put("node", node);
    		bindings.put("LOG", LOG);
    		LOG.debug("Attempting to eval script '{}' with engine '{}' for node {}", m_scriptUrl, m_engine, node);
    		engine.eval(scriptReader, bindings);
    		final OnmsNode newNode = (OnmsNode)engine.eval(scriptReader, bindings);
    	} catch (MalformedURLException mue) {
    		LOG.error("Malformed URL for scriptUrl '{}': {}", m_scriptUrl, mue.getMessage());
    	} catch (IOException ioe) {
    		LOG.error("IO exception while retrieving scriptURL '{}': {}", m_scriptUrl, ioe.getMessage());
    	} catch (ScriptException se) {
    		LOG.error("Script exception while running script at '{}': {}", m_scriptUrl, se.getMessage());
    	}
    	
    	LOG.debug("Returning updated node: {}", node);
    	return node;
    }


    /**
     * <p>getQualifier</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Require(value = { }) 
    public String getQualifier() {
        return m_qualifier;
    }

    /**
     * <p>setQualifier</p>
     *
     * @param qualifier a JEXL expression.
     * If the expression evaluates true, the script will be run.
     */
    public void setQualifier(String qualifier) {
        m_qualifier = qualifier;
    }
    
    /**
     * <p>getScriptEngine</p>
     * 
     * @return The name of the script engine for this policy
     */
    @Require(value = { })
    public String getScriptEngine() {
    	return m_engine;
    }
    
    /**
     * <p>setEngine</p>
     * 
     * @param engine The name of the script engine for this policy
     */
    public void setScriptEngine(String engine) {
    	m_engine = engine;
    }
    
    /**
     * <p>getScriptUrl</p>
     * 
     * @return A string containing the URL of the script to run
     */
    @Require(value = { })
    public String getScriptUrl() {
    	return m_scriptUrl;
    }
    
    /**
     * <p>setScriptUrl</p>
     * 
     * @param scriptUrl A string containing the URL of the script to run
     */
    public void setScriptUrl(String scriptUrl) {
    	m_scriptUrl = scriptUrl;
    }
    
    private boolean qualifies(OnmsNode node) {
        try {
            JexlEngine parser = new JexlEngine();
            Expression e = parser.createExpression(m_qualifier);
            final Map<String,Object> context = new HashMap<String,Object>();
            context.put("node", node);
            Boolean out = (Boolean) e.evaluate(new MapContext(context));
            return out;
        } catch (Exception e) {
            LOG.error("Can't process qualifier '{}' while checking node {} because {}", m_qualifier, node, e.getMessage());
            return false;
        }
    }

}
