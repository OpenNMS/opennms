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
package org.opennms.features.vaadin.jmxconfiggenerator.data;

import org.opennms.netmgt.config.collectd.jmx.Attrib;
import org.opennms.netmgt.config.collectd.jmx.CompAttrib;
import org.opennms.netmgt.config.collectd.jmx.CompMember;
import org.opennms.netmgt.config.collectd.jmx.JmxCollection;
import org.opennms.netmgt.config.collectd.jmx.JmxDatacollectionConfig;
import org.opennms.netmgt.config.collectd.jmx.Mbean;
import org.opennms.netmgt.config.collectd.jmx.Rrd;

/**
 * Simple Helper to clone any member of <code>JmxDatacollectionConfig</code> or
 * the whole object itself. I used this way, because I do not like
 * implementing/overwriting clone().
 * 
 * @author Markus von Rüden
 */
public abstract class JmxCollectionCloner {

	/**
	 * Clones a whole JmxCollectionConfig. Makes a deep copy!
	 * 
	 * @param input
	 * @return
	 */
	public static JmxDatacollectionConfig clone(JmxDatacollectionConfig input) {
		JmxDatacollectionConfig output = new JmxDatacollectionConfig();
		output.setRrdRepository(input.getRrdRepository());
		for (JmxCollection jmxCollection : input.getJmxCollectionList()) {
			output.addJmxCollection(clone(jmxCollection));
		}
		return output;
	}

	/**
	 * Clones a whole JmxCollection. Makes a deep copy!
	 * 
	 * @param input
	 * @return
	 */
	private static JmxCollection clone(JmxCollection input) {
		JmxCollection output = new JmxCollection();
		output.setMaxVarsPerPdu(input.getMaxVarsPerPdu());
		output.setName(input.getName());
		output.setRrd(clone(input.getRrd()));
		for (final Mbean mbean : input.getMbeans()) {
		    output.addMbean(clone(mbean));
		}
		return output;
	}

	/**
	 * Clones a Rrd object. Makes a deep copy!
	 * 
	 * @param input
	 * @return
	 */
	private static Rrd clone(Rrd input) {
		Rrd output = new Rrd();
		output.setStep(input.getStep());
		for (final String rra : input.getRraCollection()) {
		    output.addRra(rra);
		}
		return output;
	}

	/**
	 * Clones a Mbean object. Makes a deep copy!
	 * 
	 * @param input
	 * @return
	 */
	public static Mbean clone(Mbean input) {
		Mbean output = new Mbean();
		output.setExclude(input.getExclude());
		output.setKeyAlias(input.getKeyAlias());
		output.setKeyfield(input.getKeyfield());
		output.setName(input.getName());
		output.setObjectname(input.getObjectname());
		output.getIncludeMbeanCollection().addAll(input.getIncludeMbeanCollection());
		for (Attrib inputAttrib : input.getAttribList()) {
			output.addAttrib(clone(inputAttrib));
		}
		for (CompAttrib inputCompAttrib : input.getCompAttribList()) {
			output.addCompAttrib(clone(inputCompAttrib));
		}
		return output;
	}

	/**
	 * Clones an Attrib object. Makes a deep copy!
	 * 
	 * @param input
	 * @return
	 */
	public static Attrib clone(Attrib input) {
		Attrib output = new Attrib();
		output.setAlias(input.getAlias());
		output.setMaxval(input.getMaxval());
		output.setMinval(input.getMinval());
		output.setName(input.getName());
		output.setType(input.getType());
		return output;
	}

	/**
	 * Clones a CompAttrib object. Makes a deep copy!
	 * 
	 * @param input
	 * @return
	 */
	public static CompAttrib clone(CompAttrib input) {
		CompAttrib output = new CompAttrib();
		output.setAlias(input.getAlias());
		output.setName(input.getName());
		output.setType(input.getType());
		for (CompMember inputMember : input.getCompMemberList()) {
			output.addCompMember(clone(inputMember));
		}
		return output;
	}

	/**
	 * Clones a CompMember object. Makes a deep copy!
	 * 
	 * @param input
	 * @return
	 */
	public static CompMember clone(CompMember input) {
		CompMember output = new CompMember();
		output.setAlias(input.getAlias());
		output.setMaxval(input.getMaxval());
		output.setMinval(input.getMinval());
		output.setName(input.getName());
		output.setType(input.getType());
		return output;
	}
}
