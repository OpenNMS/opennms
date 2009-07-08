/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 *
 * Copyright (C) 2008 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/

package org.opennms.core.utils;

public class IPLike {

   private IPLike() {
   }

   public static boolean matches(String address, String pattern) {
       String hostOctets[] = address.split("\\.", 0);
       String matchOctets[] = pattern.split("\\.", 0);
       for (int i = 0; i < 4; i++) {
           if (!matchNumericListOrRange(hostOctets[i], matchOctets[i])) {
               return false;
           }
       }
       return true;
   }
   
   /**
    * Use this method to match ranges, lists, and specific number strings
    * such as:
    * "200-300" or "200,300,501-700"
    * "*" matches any
    * This method is commonly used for matching IP octets or ports
    * 
    * @param value
    * @param patterns
    * @return
    */
    public static boolean matchNumericListOrRange(String value, String patterns) {
        
        String patternList[] = patterns.split(",", 0);
        for (String element : patternList) {
            if (matchRange(value, element)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Helper method in support of matchNumericListOrRange
     * @param value
     * @param pattern
     * @return
     */
     public static boolean matchRange(String value, String pattern) {
         int dashCount = countChar('-', pattern);
         
         if ("*".equals(pattern)) {
            return true;
        } else if (dashCount == 0) {
            return value.equals(pattern);
        } else if (dashCount > 1) {
            return false;
        } else if (dashCount == 1) {
             String ar[] = pattern.split("-");
             int rangeBegin = Integer.parseInt(ar[0]);
             int rangeEnd = Integer.parseInt(ar[1]);
             int ip = Integer.parseInt(value);
             return (ip >= rangeBegin && ip <= rangeEnd);
         }
         return false;
     }

     public static int countChar(char charIn, String stingIn) {
         
         int charCount = 0;
         int charIndex = 0;
         for (int i=0; i<stingIn.length(); i++) {
             charIndex = stingIn.indexOf(charIn, i);
             if (charIndex != -1) {
                 charCount++;
                 i = charIndex +1;
             }
         }
         return charCount;
     }

}
