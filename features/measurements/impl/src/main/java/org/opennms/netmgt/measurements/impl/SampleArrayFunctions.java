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
