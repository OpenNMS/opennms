/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2015 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.measurements.impl;

import org.apache.commons.jexl2.JexlContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

/**
 * Returns prior samples of given attribute from jexl context
 * 
 * @author cgallen
 *
 */
public class SampleArrayFunctions {
	private static final Logger LOG = LoggerFactory.getLogger(SampleArrayFunctions.class);
	
	private JexlContext m_context =null;
	
	/**
	 * The context will be populated by jexl prior to expression execution
	 * @param context
	 */
	public SampleArrayFunctions(JexlContext context){
		Preconditions.checkNotNull(context, "JexlContext context");
		this.m_context= context;
	}

	/**
	 * Returns prior sample value of attribute 'sampleName' y = sample[i - n]
	 * If selected sample is before start of retrieved samples return zero (0)
	 * @param sampleName name of attribute
	 * @param i number of samples prior to return
	 * @return sample n-i or Zero (0) if sample before start of received sequence. NaN if sampleName not in jexl context
	 */
	public Double arrayZero(String sampleName, Integer n ){

		double[] sample=null;
		Integer i=null;

		try{
			Preconditions.checkNotNull(sampleName, "sampleName");
			Preconditions.checkNotNull(n, "n");
			
			sample = (double[]) m_context.get("__"+sampleName);
			if(sample==null) {
				LOG.warn("fn:arrayZero attribute not in jexl context: __"+sampleName);
				return Double.NaN;
			}
			
			i = (Integer) m_context.get("__i");
			Preconditions.checkNotNull(i, "sample index __i"); // should not happen
			
			if(i-n < 0) return Double.valueOf(0);
			
			return sample[i-n];
			
		} catch (Exception ex){
			LOG.error("fn:arrayZero problem retrieving prior attribute sample  "+sampleName+" (i - n) ("+i+"-"+n+")",ex);
		}
		return Double.NaN;
	}

	/**
	 * Returns prior sample value of attribute 'sampleName' y = sample[i - n]
	 * If selected sample is before start of retrieved samples return first sample value sample[0] 
	 * @param sampleName name of attribute
	 * @param i number of samples prior to return
	 * @return sample n-i or sample[0] if sample before start of received sequence. NaN if sampleName not in jexl context
	 */
	public Double arrayFirst(String sampleName, int n ){

		double[] sample=null;
		Integer i=null;

		try{
			Preconditions.checkNotNull(sampleName, "sampleName");
			Preconditions.checkNotNull(n, "n");
			
			sample = (double[]) m_context.get("__"+sampleName);
			if(sample==null) {
				LOG.warn("fn:arrayFirst attribute not in jexl context: __"+sampleName);
				return Double.NaN;
			}
			
			i = (Integer) m_context.get("__i");
			Preconditions.checkNotNull(i, "sample index __i"); // should not happen
			
			if(i-n < 0) return Double.valueOf(sample[0]);
			
			return sample[i-n];
		}
		catch (Exception ex){
			LOG.error("fn:arrayFirst problem retrieving prior attribute sample "+sampleName+" (i - n) ("+i+"-"+n+")",ex);
		}
		return Double.NaN;
	}

	/**
	 * Returns prior sample value of attribute 'sampleName' y = sample[i - n]
	 * If selected sample is before start of retrieved samples return Not a Number (NaN)
	 * @param sampleName name of attribute
	 * @param i number of samples prior to return
	 * @return sample n-i or not a number (NaN) if sample before start of received sequence. NaN if sampleName not in jexl context
	 */
	public Double arrayNaN(String sampleName, int n ){

		double[] sample=null;
		Integer i=null;

		try{
			Preconditions.checkNotNull(sampleName, "sampleName");
			Preconditions.checkNotNull(n, "n");
			
			sample = (double[]) m_context.get("__"+sampleName);
			if(sample==null) {
				LOG.warn("fn:arrayNaN attribute not in jexl context: __"+sampleName);
				return Double.NaN;
			}
			
			i = (Integer) m_context.get("__i");
			Preconditions.checkNotNull(i, "sample index __i"); // should not happen
			
			if(i-n < 0) return Double.NaN;
			
			return sample[i-n];
		}
		catch (Exception ex){
			LOG.error("fn:arrayNaN problem retrieving prior attribute sample "+sampleName+" (i - n) ("+i+"-"+n+")",ex);
		}
		return Double.NaN;
	}
	
	/**
	 * Returns prior sample value of attribute 'sampleName' y = sample[i - n]
	 * If selected sample is before start of retrieved samples return substitute sample value
	 * @param sampleName name of attribute
	 * @param i number of samples prior to return
	 * @param start replace references before start of samples with this value
	 * @return sample n-i or substitute if sample before start of received sequence. NaN if sampleName not in jexl context
	 */
	public Double arrayStart(String sampleName, int n, double start ){

		double[] sample=null;
		Integer i=null;

		try{
			Preconditions.checkNotNull(sampleName, "sampleName");
			Preconditions.checkNotNull(n, "n");
			
			sample = (double[]) m_context.get("__"+sampleName);
			if(sample==null) {
				LOG.warn("fn:arrayStart attribute not in jexl context: __"+sampleName);
				return Double.NaN;
			}
			
			i = (Integer) m_context.get("__i");
			Preconditions.checkNotNull(i, "sample index __i"); // should not happen
			
			if(i-n < 0) return start;
			
			return sample[i-n];
		}
		catch (Exception ex){
			LOG.error("fn:arrayStart problem retrieving prior attribute sample  "+sampleName+" (i - n) ("+i+"-"+n+")",ex);
		}
		return Double.NaN;
	}
}
