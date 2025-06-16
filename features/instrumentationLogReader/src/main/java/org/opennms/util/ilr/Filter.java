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
package org.opennms.util.ilr;

import java.util.ArrayList;
import java.util.Collection;



public class Filter {

    private static String m_searchString = null;
    public static interface PropertyGetter<T> {
        T get(ServiceCollector c);
    }

    static PropertyGetter<String> serviceID() {
        return new PropertyGetter<String>() {

            @Override
            public String get(ServiceCollector c) {
                return c.getServiceID();
            }

        };

    }

    static PropertyGetter<Integer> collectionCount() {
        return new PropertyGetter<Integer>() {

            @Override
            public Integer get(ServiceCollector c) {
                return c.getCollectionCount();
            }

        };

    }

    static Predicate<ServiceCollector> and(final Predicate<ServiceCollector> a, final Predicate<ServiceCollector> b) {
        return new Predicate<ServiceCollector>() {

            @Override
            public boolean apply(ServiceCollector svcCollector) {
                return a.apply(svcCollector) && b.apply(svcCollector);
            }

        };
    }
    static Predicate<ServiceCollector> or(final Predicate<ServiceCollector> a, final Predicate<ServiceCollector> b) {
        return new Predicate<ServiceCollector>() {

            @Override
            public boolean apply(ServiceCollector svcCollector) {
                return a.apply(svcCollector) || b.apply(svcCollector);
            }

        };
    }

    static <T> Predicate<ServiceCollector> eq(final PropertyGetter<T> getter, final T val) {
        return new Predicate<ServiceCollector>() {

            @Override
            public boolean apply(ServiceCollector svcCollector) {
                return getter.get(svcCollector).equals(val);
            }

        };
    }

    static Predicate<ServiceCollector> greaterThan(final PropertyGetter<Integer> getter, final Integer val) {
        return new Predicate<ServiceCollector>() {

            @Override
            public boolean apply(ServiceCollector svcCollector) {
                return getter.get(svcCollector) > val;
            }

        };
    }

    static Predicate<ServiceCollector> lessThan(final PropertyGetter<Integer> getter, final Integer val) {
        return new Predicate<ServiceCollector>() {

            @Override
            public boolean apply(ServiceCollector svcCollector) {
                return getter.get(svcCollector) < val;
            }

        };
    }
    static Predicate<ServiceCollector> byServiceID(final String serviceID) {
        return new Predicate<ServiceCollector>() {

            @Override
            public boolean apply(ServiceCollector svcCollector) {
                return svcCollector.getServiceID().equals(serviceID);
            }

        };
    }
    
    static Predicate<ServiceCollector> byPartialServiceID(final String searchString) {
        return new Predicate<ServiceCollector>() {
            
            @Override
            public boolean apply(ServiceCollector svcCollector){
                return svcCollector.getServiceID().contains(searchString);
            }
        };
    }

    static Predicate<ServiceCollector> byTotalCollections(final long totalCollections) {
        return new Predicate<ServiceCollector>() {

            @Override
            public boolean apply(ServiceCollector svcCollector) {
                return svcCollector.getCollectionCount() == totalCollections;
            }

        };
    }
    
    static Predicate<ServiceCollector> byTotalCollectionTime(final long totalCollectionTime) {
        return new Predicate<ServiceCollector>() {

            @Override
            public boolean apply(ServiceCollector svcCollector) {
                return svcCollector.getCollectionCount() == totalCollectionTime;
            }

        };
    }
    
    static Predicate<ServiceCollector> byAverageCollectionTime(final long averageCollectionTime) {
        return new Predicate<ServiceCollector>() {

            @Override
            public boolean apply(ServiceCollector svcCollector) {
                return svcCollector.getCollectionCount() == averageCollectionTime;
            }

        };
    }
    
    static Predicate<ServiceCollector> byAverageTimeBetweenCollections(final long averageTimeBetweenCollections) {
        return new Predicate<ServiceCollector>() {

            @Override
            public boolean apply(ServiceCollector svcCollector) {
                return svcCollector.getCollectionCount() == averageTimeBetweenCollections;
            }

        };
    }
    
    static Predicate<ServiceCollector> byTotalSuccessfulCollections(final long totalSuccessfulCollections) {
        return new Predicate<ServiceCollector>() {

            @Override
            public boolean apply(ServiceCollector svcCollector) {
                return svcCollector.getCollectionCount() == totalSuccessfulCollections;
            }

        };
    }
    
    static Predicate<ServiceCollector> bySuccessfulPercentage(final double successfulPercentage) {
        return new Predicate<ServiceCollector>() {

            @Override
            public boolean apply(ServiceCollector svcCollector) {
                return svcCollector.getCollectionCount() == successfulPercentage;
            }

        };
    }
    
    static Predicate<ServiceCollector> byAverageSuccessfulCollectionTime(final long averageSuccessfulCollectionTime) {
        return new Predicate<ServiceCollector>() {

            @Override
            public boolean apply(ServiceCollector svcCollector) {
                return svcCollector.getCollectionCount() == averageSuccessfulCollectionTime;
            }

        };
    }
    
    static Predicate<ServiceCollector> byTotalUnsuccessfulCollections(final long totalUnsuccessfulCollections) {
        return new Predicate<ServiceCollector>() {

            @Override
            public boolean apply(ServiceCollector svcCollector) {
                return svcCollector.getCollectionCount() == totalUnsuccessfulCollections;
            }

        };
    }
    
    static Predicate<ServiceCollector> byUnsuccessfulPercentage(final double unsuccessfulPercentage) {
        return new Predicate<ServiceCollector>() {

            @Override
            public boolean apply(ServiceCollector svcCollector) {
                return svcCollector.getCollectionCount() == unsuccessfulPercentage;
            }

        };
    }
    
    static Predicate<ServiceCollector> byAverageUnsuccessfulCollectionTime(final long averageUnsuccessfulCollectionTime) {
        return new Predicate<ServiceCollector>() {

            @Override
            public boolean apply(ServiceCollector svcCollector) {
                return svcCollector.getCollectionCount() == averageUnsuccessfulCollectionTime;
            }

        };
    }
    
    static Predicate<ServiceCollector> byTotalPersistTime(final long totalPersistTime) {
        return new Predicate<ServiceCollector>() {

            @Override
            public boolean apply(ServiceCollector svcCollector) {
                return svcCollector.getCollectionCount() == totalPersistTime;
            }

        };
    }


    public Predicate<Integer> createIntegerBasedPredicate(final int j) {
        Predicate<Integer> predicate = new Predicate<Integer>() {
            @Override
            public boolean apply(Integer i) {
                if(i == j){
                    return true;
                }else{
                    return false;   
                }            
            }
        }; 
        return predicate;
    }
    public Predicate<String> createStringBasedPredicate(final String filterString) {
        Predicate<String> predicate = new Predicate<String>() {
            @Override
            public boolean apply(String s) {
                if(s.equals(filterString)){
                    return true;
                }else {
                    return false;
                }
            }
        };
        return predicate;
    }
    static public <T> Collection<T> filter(Collection<T> target, Predicate<T> predicate) {
        Collection<T> filteredCollection = new ArrayList<>();
        for (T t : target) {
            if (predicate.apply(t)) {
                filteredCollection.add(t);
            }
        }
        return filteredCollection;
    }

    public interface Predicate<T> {
        public boolean apply(T type);
    }

    public static void setSearchString(String searchString) {
         m_searchString = searchString;
    }

    public static String getSearchString() {
        return m_searchString;
    }
}
