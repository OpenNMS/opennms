//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2003 Jan 31: Cleaned up some unused imports.
// 2002 Oct 22: Added Threshold rearm event.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.                                                            
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//       
// For more information contact: 
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//

package org.opennms.netmgt.threshd;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.threshd.Threshold;

/**
 * Wraps the castor created org.opennms.netmgt.config.threshd.Threshold class
 * and provides the ability to track threshold exceeded occurrences.
 */
final class ThresholdEntity
	implements Cloneable
{
	static final int NONE_TRIGGERED = 0;
	static final int HIGH_TRIGGERED = 1;
	static final int LOW_TRIGGERED = 2;
	static final int HIGH_AND_LOW_TRIGGERED = 3;
	static final int HIGH_REARMED = 4;
	static final int LOW_REARMED = 5;
	static final int HIGH_AND_LOW_REARMED = 6;
	
	static final String HIGH_THRESHOLD = "high";
	static final String LOW_THRESHOLD = "low";
	
	/**
	 * Castor Threshold object containing threshold
	 * configuration data.
	 */
	private Threshold	m_highThreshold;
	private Threshold	m_lowThreshold;
	
	/** 
	 * Threshold exceeded count
	 */
	private int m_highCount;
	private int m_lowCount;

	/** 
	 * Threshold armed flag 
	 * 
	 * This flag must be true before evaluate() will return 
	 * true (indicating that the threshold has been triggered).
	 * This flag is initialized to true by the constructor and
	 * is set to false each time the threshold is triggered.
	 * It can only be reset by the current value of the datasource
	 * falling below (for high threshold) or rising above (for 
	 * low threshold) the rearm value.
	 */
	private boolean m_highArmed;
	private boolean m_lowArmed;
	 
	/**
	 * Constructor.
	 */
	ThresholdEntity()
	{
		m_highThreshold = null;
		m_lowThreshold = null;
	}
	
	void setHighThreshold(Threshold threshold)
	{
		if (m_highThreshold != null)
			throw new IllegalStateException("High threshold already set.");
			
		m_highThreshold = threshold;
		m_highCount = 0;
		m_highArmed = true;
	}
	
	void setLowThreshold(Threshold threshold)
	{
		if (m_lowThreshold != null)
			throw new IllegalStateException("Low threshold already set.");
			
		m_lowThreshold = threshold;
		m_lowCount = 0;
		m_lowArmed = true;
	}
	
	boolean hasHighThreshold()
	{
		return m_highThreshold != null;
	}
	
	boolean hasLowThreshold()
	{
		return m_lowThreshold != null;
	}
	
	Threshold getHighThreshold()
	{
		return m_highThreshold;
	}
	
	Threshold getLowThreshold()
	{
		return m_lowThreshold;
	}
	
	/**
	 * Get datasource name
	 */
	String getDatasourceName()
	{
		if (this.hasHighThreshold())
			return m_highThreshold.getDsName();
		else if (this.hasLowThreshold())
			return m_lowThreshold.getDsName();
		else
			throw new IllegalStateException("Neither high nor low threshold set.");
	}
	
	/**
	 * Get datasource type
	 */
	String getDatasourceType()
	{
		if (this.hasHighThreshold())
			return m_highThreshold.getDsType();
		else if (this.hasLowThreshold())
			return m_lowThreshold.getDsType();
		else
			throw new IllegalStateException("Neither high nor low threshold set.");
	}
	
	/**
	 * Get high threshold value
	 */
	double getHighValue()
	{
		if (m_highThreshold == null)
			throw new IllegalStateException("High threshold not set.");
		
		return m_highThreshold.getValue();
	}
	
	/**
	 * Get low threshold value
	 */
	double getLowValue()
	{
		if (m_lowThreshold == null)
			throw new IllegalStateException("Low threshold not set.");
			
		return m_lowThreshold.getValue();
	}
	
	/** 
	 * Get high threshold re-arm
	 */
	double getHighRearm()
	{
		if (m_highThreshold == null)
			throw new IllegalStateException("High threshold not set.");
			
		return m_highThreshold.getRearm();
	}
	
	/** 
	 * Get low threshold re-arm
	 */
	double getLowRearm()
	{
		if (m_lowThreshold == null)
			throw new IllegalStateException("Low threshold not set.");
			
		return m_lowThreshold.getRearm();
	}
	
	/** 
	 * Get high threshold trigger
	 */
	int getHighTrigger()
	{
		if (m_highThreshold == null)
			throw new IllegalStateException("High threshold not set.");
			
		return m_highThreshold.getTrigger();
	}
	
	/** 
	 * Get low threshold trigger
	 */
	int getLowTrigger()
	{
		if (m_lowThreshold == null)
			throw new IllegalStateException("Low threshold not set.");
		
		return m_lowThreshold.getTrigger();
	}
	
	/**
	 * Returns a copy of this ThresholdEntity object.
	 * 
	 * NOTE:  The m_lowThreshold and m_highThreshold member 
	 * 	  variables are not actually cloned...the returned 
	 *  	  ThresholdEntity object will simply contain
	 * 	  references to the same castor Threshold objects
	 * 	  as the original ThresholdEntity object.
	 */
	public Object clone()
	{
		ThresholdEntity clone = new ThresholdEntity();
		if (this.hasHighThreshold())
			clone.setHighThreshold(this.m_highThreshold);
		if (this.hasLowThreshold())
			clone.setLowThreshold(this.m_lowThreshold);
			
		return clone;
	}
	
	/** 
	 * This method is responsible for returning a String object which represents
	 * the content of this ThresholdEntity.  Primarily used for debugging purposes.
	 * 
	 * @return String which represents the content of this ThresholdEntity
	 */
	public String toString()
	{
		StringBuffer buffer = new StringBuffer();
		
		// Build the buffer
		//
		if (!this.hasHighThreshold() && !this.hasLowThreshold())
			return buffer.toString();
		
		buffer.append("dsName=").append(this.getDatasourceName());
		buffer.append(",dsType=").append(this.getDatasourceType()).append(":");
		// High Threshold 
		//
		if (this.hasHighThreshold())
		{
			buffer.append(" highVal=").append(m_highThreshold.getValue());
			buffer.append(",highRearm=").append(m_highThreshold.getRearm());
			buffer.append(",highTrigger=").append(m_highThreshold.getTrigger());
		}
		
		// Low Threshold
		//
		if (this.hasLowThreshold())
		{
			buffer.append(",lowVal=").append(m_lowThreshold.getValue());
			buffer.append(",lowRearm=").append(m_lowThreshold.getRearm());
			buffer.append(",lowTrigger=").append(m_lowThreshold.getTrigger());
		}
		
		return buffer.toString();
	}
	
	/**
	 * Evaluates the threshold in light of the provided
	 * datasource value.  
	 * 
	 * @param dsValue	Current value of datasource
	 * 
	 * @return integer value indicating which threshold types 
	 * 	(if any) were exceeded indicating than an event 
	 *	should be generated. false otherwise.
	 */
	int evaluate(double dsValue)
	{
		Category log = ThreadCategory.getInstance(getClass());
		
		boolean highTriggered = false;
		boolean lowTriggered = false;
		boolean highRearmed = false;
		boolean lowRearmed = false;
		
		if (log.isDebugEnabled())
			log.debug("evaluate: value= " + dsValue + " against threshold: " + this);
		
		// Check high threshold
		//
		if (hasHighThreshold())
		{
			// threshold exceeded?
			if (dsValue >= m_highThreshold.getValue())
			{
				// Is threshold armed?
				if (m_highArmed)
				{
					// increment count
					m_highCount++;
					
					if (log.isDebugEnabled())
						log.debug("evaluate: high threshold exceeded, count=" + m_highCount);
					
					// trigger exceeded?
					if (m_highCount >= m_highThreshold.getTrigger())
					{
						if (log.isDebugEnabled())
							log.debug("evaluate: high threshold triggered!");
						highTriggered = true;
						m_highCount = 0;
						m_highArmed = false;
					}
				}
			}
			// rearm threshold?
			else if (dsValue <= m_highThreshold.getRearm())
			{
				if (!m_highArmed)
				{
					if (log.isDebugEnabled())
						log.debug("evaluate: high threshold rearmed!");
					m_highArmed = true;
					highRearmed = true;
					m_highCount = 0;
				}
			}
			// reset count
			else 
			{
				if (log.isDebugEnabled())
					log.debug("evaluate: resetting high threshold count to 0");
				m_highCount = 0;
			}
		}
		
		// Check low threshold
		//
		if (hasLowThreshold())
		{
			// threshold exceeded?
			if (dsValue <= m_lowThreshold.getValue())
			{
				// Is threshold armed?
				if (m_lowArmed)
				{
					// increment count
					m_lowCount++;
					
					if (log.isDebugEnabled())
						log.debug("evaluate: low threshold exceeded, count=" + m_lowCount);
					
					// trigger exceeded?
					if (m_lowCount >= m_lowThreshold.getTrigger())
					{
						if (log.isDebugEnabled())
							log.debug("evaluate: low threshold triggered!");
						lowTriggered = true;
						m_lowCount = 0;
						m_lowArmed = false;
					}
				}
			}
			// rearm threshold?
			else if (dsValue >= m_lowThreshold.getRearm())
			{
				if (!m_lowArmed)
				{
					if (log.isDebugEnabled())
						log.debug("evaluate: low threshold rearmed!");
					m_lowArmed = true;
					lowRearmed = true;
					m_lowCount = 0;
				}
			}
			// reset count
			else 
			{
				if (log.isDebugEnabled())
					log.debug("evaluate: resetting low threshold count to 0");
				m_lowCount = 0;
			}
		}
		// Return integer value indicating which 
		// threshold configurations have been triggered.
		//
		if (lowTriggered && highTriggered)
			return HIGH_AND_LOW_TRIGGERED;
		else if (lowTriggered)
			return LOW_TRIGGERED;
		else if (highTriggered)
			return HIGH_TRIGGERED;
                else if (lowRearmed && highRearmed)
                        return HIGH_AND_LOW_REARMED;
                else if (lowRearmed)
                        return LOW_REARMED;
                else if (highRearmed)
                        return HIGH_REARMED;

		else
			return NONE_TRIGGERED;
	}
}
