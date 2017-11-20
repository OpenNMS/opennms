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
public class PriorSample {
	private static final Logger LOG = LoggerFactory.getLogger(PriorSample.class);

	/**
	 * Returns prior sample value of attribute 'sampleName' y = sample[i - n]
	 * if sample before start of retrieved samples return zero (0)
	 * @param sampleName name of attribute
	 * @param i number of samples prior to return
	 * @param context jexl context passed into function
	 * @return sample n-i or Zero (0) if sample before start of received sequence. NaN if sampleName not in context
	 */
	public static Double zStartZero(String sampleName, Integer n, JexlContext context){

		double[] sample=null;
		Integer i=null;

		try{
			Preconditions.checkNotNull(sampleName, "sampleName");
			Preconditions.checkNotNull(context, "JexlContext context");
			Preconditions.checkNotNull(n, "n");
			
			sample = (double[]) context.get("__"+sampleName);
			if(sample==null) throw new RuntimeException("not in context: __"+sampleName);
			i = (Integer) context.get("__i");
			if(i==null) throw new RuntimeException("not in context: __i");
			
			if(i-n < 0) return Double.valueOf(0);
			
			return sample[i-n];
		}
		catch (Exception ex){
			LOG.error("problem retreiving prior sample "+sampleName+" (i - n) ("+i+"-"+n+")",ex);
		}
		return Double.NaN;
	}

	/**
	 * Returns prior sample value of attribute 'sampleName' y = sample[i - n]
	 * if sample before start of retrieved samples return first sample value sample[0] 
	 * @param sampleName name of attribute
	 * @param i number of samples prior to return
	 * @param context jexl context passed into function
	 * @return sample n-i or sample[0] if sample before start of received sequence. NaN if sampleName not in context
	 */
	public static Double zStartFirst(String sampleName, int n, JexlContext context){

		double[] sample=null;
		Integer i=null;

		try{
			Preconditions.checkNotNull(sampleName, "sampleName");
			Preconditions.checkNotNull(context, "JexlContext context");
			Preconditions.checkNotNull(n, "n");
			
			sample = (double[]) context.get("__"+sampleName);
			if(sample==null) throw new RuntimeException("not in context: __"+sampleName);
			i = (Integer) context.get("__i");
			if(i==null) throw new RuntimeException("not in context: __i");
			
			if(i-n < 0) return Double.valueOf(sample[0]);
			
			return sample[i-n];
		}
		catch (Exception ex){
			LOG.error("problem retreiving prior sample "+sampleName+" (i - n) ("+i+"-"+n+")",ex);
		}
		return Double.NaN;
	}

	/**
	 * Returns prior sample value of attribute 'sampleName' y = sample[i - n]
	 * if sample before start of retrieved samples return not a number (NaN)
	 * @param sampleName name of attribute
	 * @param i number of samples prior to return
	 * @param context jexl context passed into function
	 * @return sample n-i or not a number (NaN) if sample before start of received sequence. NaN if sampleName not in context
	 */
	public static Double zStartNaN(String sampleName, int n, JexlContext context){

		double[] sample=null;
		Integer i=null;

		try{
			Preconditions.checkNotNull(sampleName, "sampleName");
			Preconditions.checkNotNull(context, "JexlContext context");
			Preconditions.checkNotNull(n, "n");
			
			sample = (double[]) context.get("__"+sampleName);
			if(sample==null) throw new RuntimeException("not in context: __"+sampleName);
			i = (Integer) context.get("__i");
			if(i==null) throw new RuntimeException("not in context: __i");
			
			if(i-n < 0) return Double.NaN;
			
			return sample[i-n];
		}
		catch (Exception ex){
			LOG.error("problem retreiving prior sample "+sampleName+" (i - n) ("+i+"-"+n+")",ex);
		}
		return Double.NaN;
	}
}
