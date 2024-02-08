/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.netmgt.correlation.drools.config;

  //---------------------------------/
 //- Imported classes and packages -/
//---------------------------------/

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opennms.netmgt.correlation.CorrelationEngine;
import org.opennms.netmgt.events.api.EventIpcManager;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;

import com.codahale.metrics.MetricRegistry;

import io.swagger.v3.oas.annotations.Hidden;


/**
 * The top-level element of the drools-engine.xml configuration
 *  file.
 * 
 * @version $Revision$ $Date$
 */

@XmlRootElement(name="engine-configuration")
@XmlAccessorType(XmlAccessType.FIELD)
public class EngineConfiguration implements Serializable {
    private static final long serialVersionUID = 2358050053659695907L;


    private static final Logger LOG = LoggerFactory.getLogger(EngineConfiguration.class);


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

	/**
     * Field _ruleSetList.
     */
	@XmlElement(name="rule-set")
    private List<RuleSet> _ruleSetList;


      //----------------/
     //- Constructors -/
    //----------------/

    public EngineConfiguration() {
        this._ruleSetList = new ArrayList<>();
    }


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * 
     * 
     * @param vRuleSet
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addRuleSet(final RuleSet vRuleSet) throws IndexOutOfBoundsException {
        this._ruleSetList.add(vRuleSet);
    }

    /**
     * 
     * 
     * @param index
     * @param vRuleSet
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addRuleSet(final int index, final RuleSet vRuleSet) throws IndexOutOfBoundsException {
        this._ruleSetList.add(index, vRuleSet);
    }

    /**
     * Method enumerateRuleSet.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public Enumeration<RuleSet> enumerateRuleSet() {
        return Collections.enumeration(this._ruleSetList);
    }

    /**
     * Method getRuleSet.
     * 
     * @param index
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     * @return the value of the
     * RuleSet at the
     * given index
     */
    public RuleSet getRuleSet(final int index) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._ruleSetList.size()) {
            throw new IndexOutOfBoundsException("getRuleSet: Index value '" + index + "' not in range [0.." + (this._ruleSetList.size() - 1) + "]");
        }
        
        return (RuleSet) _ruleSetList.get(index);
    }

    /**
     * Method getRuleSet.Returns the contents of the collection in
     * an Array.  <p>Note:  Just in case the collection contents
     * are changing in another thread, we pass a 0-length Array of
     * the correct type into the API call.  This way we <i>know</i>
     * that the Array returned is of exactly the correct length.
     * 
     * @return this collection as an Array
     */
    public RuleSet[] getRuleSet() {
        RuleSet[] array = new RuleSet[0];
        return (RuleSet[]) this._ruleSetList.toArray(array);
    }

    /**
     * Method getRuleSetCollection.Returns a reference to
     * '_ruleSetList'. No type checking is performed on any
     * modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public List<RuleSet> getRuleSetCollection() {
        return this._ruleSetList;
    }

    /**
     * Method getRuleSetCount.
     * 
     * @return the size of this collection
     */
    public int getRuleSetCount() {
        return this._ruleSetList.size();
    }

    /**
     * Method iterateRuleSet.
     * 
     * @return an Iterator over all possible elements in this
     * collection
     */
    public Iterator<RuleSet> iterateRuleSet() {
        return this._ruleSetList.iterator();
    }

    /**
     */
    public void removeAllRuleSet() {
        this._ruleSetList.clear();
    }

    /**
     * Method removeRuleSet.
     * 
     * @param vRuleSet
     * @return true if the object was removed from the collection.
     */
    public boolean removeRuleSet(final RuleSet vRuleSet) {
        return _ruleSetList.remove(vRuleSet);
    }

    /**
     * Method removeRuleSetAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public RuleSet removeRuleSetAt(final int index) {
        return this._ruleSetList.remove(index);
    }

    /**
     * 
     * @deprecated
     * @param index
     * @param vRuleSet
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    @Hidden
    public void setRuleSet(final int index, final RuleSet vRuleSet) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._ruleSetList.size()) {
            throw new IndexOutOfBoundsException("setRuleSet: Index value '" + index + "' not in range [0.." + (this._ruleSetList.size() - 1) + "]");
        }
        
        this._ruleSetList.set(index, vRuleSet);
    }

    /**
     * 
     * @deprecated
     * @param vRuleSetArray
     */
    @Hidden
    public void setRuleSet(final RuleSet[] vRuleSetArray) {
    	this.setRuleSet(Arrays.asList(vRuleSetArray));
    }

    /**
     * Sets the value of '_ruleSetList' by copying the given
     * Vector. All elements will be checked for type safety.
     * 
     * @param vRuleSetList the Vector to copy.
     */
    public void setRuleSet(final List<RuleSet> vRuleSetList) {
        // copy vector
        this._ruleSetList.clear();
        
        this._ruleSetList.addAll(vRuleSetList);
    }

    /**
     * Sets the value of '_ruleSetList' by setting it to the given
     * Vector. No type checking is performed.
     * @deprecated
     * 
     * @param ruleSetList the Vector to set.
     */
    @Hidden
    public void setRuleSetCollection(final List<RuleSet> ruleSetList) {
        this._ruleSetList = ruleSetList;
    }

    @Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((_ruleSetList == null) ? 0 : _ruleSetList.hashCode());
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		EngineConfiguration other = (EngineConfiguration) obj;
		if (_ruleSetList == null) {
			if (other._ruleSetList != null)
				return false;
		} else if (!_ruleSetList.equals(other._ruleSetList))
			return false;
		return true;
	}


	public CorrelationEngine[] constructEngines(Resource basePath, ApplicationContext appContext, EventIpcManager eventIpcManager, MetricRegistry metricRegistry) {
		
		LOG.info("Creating drools engines for configuration {}.", basePath);

		List<CorrelationEngine> engineList = new ArrayList<>();
		for (final RuleSet ruleSet : getRuleSet()) {
			LOG.debug("Constucting engine for ruleset {} in configuration {}.", ruleSet.getName(), basePath);
			engineList.add(ruleSet.constructEngine(basePath, appContext, eventIpcManager, metricRegistry));
	    }
	    
	    return engineList.toArray(new CorrelationEngine[0]);
	}

	public CorrelationEngine constructEngine(Resource basePath, ApplicationContext appContext, EventIpcManager eventIpcManager, MetricRegistry metricRegistry, String engineName) {
            for (final RuleSet ruleSet : getRuleSet()) {
                if (ruleSet.getName().equals(engineName)) {
                    LOG.debug("Constucting engine for ruleset {} in configuration {}.", ruleSet.getName(), basePath);
                    return ruleSet.constructEngine(basePath, appContext, eventIpcManager, metricRegistry);
                }
            }
            return null;
        }

}
