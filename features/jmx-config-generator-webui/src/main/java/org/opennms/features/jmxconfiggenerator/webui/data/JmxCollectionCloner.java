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

package org.opennms.features.jmxconfiggenerator.webui.data;

import org.opennms.xmlns.xsd.config.jmx_datacollection.*;

/**
 * Simple Helper to clone any member of <code>JmxDatacollectionConfig</code> or
 * the whole object itself. I used this way, because I do not like
 * implementing/overwriting clone().
 * 
 * @author Markus von Rüden
 */
public class JmxCollectionCloner {

	/**
	 * Clones a whole JmxCollectionConfig. Makes a deep copy!
	 * 
	 * @param input
	 * @return
	 */
	public static JmxDatacollectionConfig clone(JmxDatacollectionConfig input) {
		JmxDatacollectionConfig output = new JmxDatacollectionConfig();
		output.setRrdRepository(input.getRrdRepository());
		for (JmxCollection jmxCollection : input.getJmxCollection()) {
			output.getJmxCollection().add(clone(jmxCollection));
		}
		return output;
	}

	/**
	 * Clones a whole JmxCollection. Makes a deep copy!
	 * 
	 * @param input
	 * @return
	 */
	public static JmxCollection clone(JmxCollection input) {
		JmxCollection output = new JmxCollection();
		output.setMaxVarsPerPdu(input.getMaxVarsPerPdu());
		output.setName(input.getName());
		output.setRrd(clone(input.getRrd()));
		output.setMbeans(clone(input.getMbeans()));
		return output;
	}

	/**
	 * Clones a Rrd object. Makes a deep copy!
	 * 
	 * @param input
	 * @return
	 */
	public static Rrd clone(Rrd input) {
		Rrd output = new Rrd();
		output.setStep(input.getStep());
		output.getRra().addAll(input.getRra());
		return output;
	}

	/**
	 * Clones a Mbeans object. Makes a deep copy!
	 * 
	 * @param input
	 * @return
	 */
	public static Mbeans clone(Mbeans input) {
		Mbeans output = new Mbeans();
		for (Mbean inputBean : input.getMbean()) {
			output.getMbean().add(clone(inputBean));
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
		output.getIncludeMbean().addAll(input.getIncludeMbean());
		for (Attrib inputAttrib : input.getAttrib()) {
			output.getAttrib().add(clone(inputAttrib));
		}
		for (CompAttrib inputCombAttrib : input.getCompAttrib()) {
			output.getCompAttrib().add(clone(inputCombAttrib));
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
		for (CompMember inputMember : input.getCompMember()) {
			output.getCompMember().add(clone(inputMember));
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
