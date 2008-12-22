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
