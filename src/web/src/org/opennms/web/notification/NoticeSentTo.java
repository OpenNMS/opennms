//
// Copyright (C) 2000 Oculan Corp.
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
//	Brian Weaver   <weave@opennms.org>
//	http://www.opennms.org/
//
//
// Tab Size = 8
/*****************************************************************************/

package org.opennms.web.notification;

import java.util.*;

/**
 * NoticeSentTo Bean, containing data from the usersNotified table for a single user/notice pair.
 */
public class NoticeSentTo 
{	
	/**
         * User this notice was sent to
         */ 
	public String m_userId;
        
	/**
         * Time the notice was sent to the user in milliseconds.
	 */
        public long m_time;
        
        /**
         * Contact info.
	 */
        public String m_contactInfo;
        
	/**
         * The type of notification mechanism.
	 */
        public String m_media;
        
	/**
	 * Default Constructor
	 */
	public NoticeSentTo()
	{
	}
        
        /**
	 * 
	 */
        public void setUserId(String userid)
        {
                m_userId = userid;
        }
        
        /**
	 * 
	 */
        public String getUserId()
        {
                return m_userId;
        }
        
        /**
	 * 
	 */
        public void setTime(long time) //no see!
        {
                m_time = time;
        }
        
        /**
	 * 
	 */
        public Date getTime()
        {
                return new Date(m_time);
        }
        
        /**
	 * 
	 */
        public void setMedia(String media)
        {
                m_media = media;
        }
        
        /**
	 * 
	 */
        public String getMedia()
        {
                return m_media;
        }
        
        /**
	 * 
	 */
        public void setContactInfo(String contact)
        {
                m_contactInfo = contact;
        }
        
        /**
	 * 
	 */
        public String getContactInfo()
        {
                return m_contactInfo;
        }
}
