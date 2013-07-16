/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.statsd;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import org.opennms.core.utils.DefaultTimeKeeper;
import org.opennms.core.utils.TimeKeeper;

/**
 * <p>RelativeTime class.</p>
 *
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @version $Id: $
 */
public enum RelativeTime {

    THISYEAR {
        @Override
        public Date getStart() {
            final TimeZone timeZone = getTimeZone();
            final GregorianCalendar yearCalendar = new GregorianCalendar(timeZone);
            final GregorianCalendar januaryFirst = new GregorianCalendar(yearCalendar.get(Calendar.YEAR), 0, 1);
            januaryFirst.setTimeZone(timeZone);
            return new Date(januaryFirst.getTimeInMillis());
        }

        @Override
        public Date getEnd() {
            return getStartOfToday();
        }
        
    },
    
    LASTTHIRTYONEDAYS {
        @Override
        public Date getStart() {
            return getStartDate(31);
        }
        
        @Override
        public Date getEnd() {
                  return getStartOfToday();
        }
    },

    LASTSEVENDAYS {
        @Override
        public Date getStart() {
            return getStartDate(7);
        }
        
        @Override
        public Date getEnd() {
                  return getStartOfToday();
        }
    },

    YESTERDAY {
        @Override
        public Date getStart() {
            return getStartDate(1);
        }
        
        @Override
        public Date getEnd() {
            return getStartOfToday();
        }
    },
    
    LASTHOUR {
        @Override
        public Date getStart() {
            Calendar calendar = new GregorianCalendar(getTimeZone());
            calendar.setTimeInMillis(getCurrentTime());
            
            calendar.set(Calendar.MILLISECOND, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.add(Calendar.HOUR, -1);
            
            return calendar.getTime();
        }
        
        @Override
        public Date getEnd() {
            Calendar calendar = new GregorianCalendar(getTimeZone());
            calendar.setTimeInMillis(getCurrentTime());

            calendar.set(Calendar.MILLISECOND, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MINUTE, 0);
            
            return calendar.getTime();
        }
    },
    
    SLIDINGHOUR {
               @Override
       	public Date getStart() {
           Calendar calendar = new GregorianCalendar(getTimeZone());
           calendar.setTimeInMillis(getCurrentTime());
           
           calendar.set(Calendar.MILLISECOND, 0);
           calendar.set(Calendar.SECOND, 0);
           calendar.add(Calendar.HOUR, -1);
           
           return calendar.getTime();
       	}
        
               @Override
       	public Date getEnd() {
           Calendar calendar = new GregorianCalendar(getTimeZone());
           calendar.setTimeInMillis(getCurrentTime());
           
           calendar.set(Calendar.MILLISECOND, 0);
           calendar.set(Calendar.SECOND, 0);
           
           return calendar.getTime();
       	}
    },
    
    SLIDING4HOURS {
               @Override
       	public Date getStart() {
           Calendar calendar = new GregorianCalendar(getTimeZone());
           calendar.setTimeInMillis(getCurrentTime());
           
           calendar.set(Calendar.MILLISECOND, 0);
           calendar.set(Calendar.SECOND, 0);
           calendar.add(Calendar.HOUR, -4);
           
           return calendar.getTime();
       	}
        
               @Override
       	public Date getEnd() {
           Calendar calendar = new GregorianCalendar(getTimeZone());
           calendar.setTimeInMillis(getCurrentTime());
           
           calendar.set(Calendar.MILLISECOND, 0);
           calendar.set(Calendar.SECOND, 0);
           
           return calendar.getTime();
	}
    },
    
    SLIDING8HOURS {
        @Override
	public Date getStart() {
           Calendar calendar = new GregorianCalendar(getTimeZone());
           calendar.setTimeInMillis(getCurrentTime());
           
           calendar.set(Calendar.MILLISECOND, 0);
           calendar.set(Calendar.SECOND, 0);
           calendar.add(Calendar.HOUR, -8);
           
           return calendar.getTime();
       }
        
        @Override
       	public Date getEnd() {
           Calendar calendar = new GregorianCalendar(getTimeZone());
           calendar.setTimeInMillis(getCurrentTime());
           
           calendar.set(Calendar.MILLISECOND, 0);
           calendar.set(Calendar.SECOND, 0);
           
           return calendar.getTime();
	}
    },
    
    SLIDINGDAY {
               @Override
       	public Date getStart() {
           Calendar calendar = new GregorianCalendar(getTimeZone());
           calendar.setTimeInMillis(getCurrentTime());
           
           calendar.set(Calendar.MILLISECOND, 0);
           calendar.set(Calendar.SECOND, 0);
           calendar.add(Calendar.HOUR, -24);
           
           return calendar.getTime();
       	}
        
               @Override
       	public Date getEnd() {
           Calendar calendar = new GregorianCalendar(getTimeZone());
           calendar.setTimeInMillis(getCurrentTime());
       	   
           calendar.set(Calendar.MILLISECOND, 0);
           calendar.set(Calendar.SECOND, 0);
           
           return calendar.getTime();
	}
    },
    
    TODAY {
               @Override
       	public Date getStart() {
               return getStartOfToday();
       	}
        
               @Override
       	public Date getEnd() {
           Calendar calendar = new GregorianCalendar(getTimeZone());
           calendar.setTimeInMillis(getCurrentTime());
           
           calendar.set(Calendar.MILLISECOND, 0);
           calendar.set(Calendar.SECOND, 0);
           
           return calendar.getTime();
	}
    };

    /**
     * <p>getStartDate</p>
     *
     * @param offset a int.
     * @return a {@link java.util.Date} object.
     */
    protected Date getStartDate(int offset) {
        Calendar calendar = new GregorianCalendar(getTimeZone());
        calendar.setTimeInMillis(getCurrentTime());
        
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.HOUR, 0);
        calendar.add(Calendar.DAY_OF_YEAR, -offset);
        
        return calendar.getTime();
    }
    
    /**
     * <p>getStartOfToday</p>
     *
     * @return a {@link java.util.Date} object.
     */
    protected Date getStartOfToday() {
        Calendar calendar = new GregorianCalendar(getTimeZone());
        calendar.setTimeInMillis(getCurrentTime());

        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.HOUR, 0);
        
        return calendar.getTime();
    }
    
    /**
     * <p>getStart</p>
     *
     * @return a {@link java.util.Date} object.
     */
    public abstract Date getStart();
    /**
     * <p>getEnd</p>
     *
     * @return a {@link java.util.Date} object.
     */
    public abstract Date getEnd();
    
    private static TimeKeeper DEFAULT_TIME_KEEPER = new DefaultTimeKeeper();
    
    private TimeKeeper m_timeKeeper = null;
    
    /**
     * <p>getTimeKeeper</p>
     *
     * @return a {@link org.opennms.core.utils.TimeKeeper} object.
     */
    public TimeKeeper getTimeKeeper() {
        return m_timeKeeper;
    }
    
    /**
     * <p>setTimeKeeper</p>
     *
     * @param timeKeeper a {@link org.opennms.core.utils.TimeKeeper} object.
     */
    public void setTimeKeeper(TimeKeeper timeKeeper) {
        m_timeKeeper = timeKeeper;
    }
    
    /**
     * <p>getCurrentTime</p>
     *
     * @return a long.
     */
    protected long getCurrentTime() {
        return myTimeKeeper().getCurrentTime();
    }

    protected TimeZone getTimeZone() {
        return myTimeKeeper().getTimeZone();
    }

    protected TimeKeeper myTimeKeeper() {
        return getTimeKeeper() == null? DEFAULT_TIME_KEEPER : getTimeKeeper();
    }
}
