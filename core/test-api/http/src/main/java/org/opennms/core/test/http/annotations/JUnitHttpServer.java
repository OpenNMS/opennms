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
package org.opennms.core.test.http.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>JUnitHttpServer class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD,ElementType.TYPE})
public @interface JUnitHttpServer {

    /** the directory from which to serve test files **/
    String resource() default "target/test-classes";
    
    /** the port to listen on **/
    int port() default 9162;

    /** the list of virtual hosts to respond to, defaults to "localhost" **/
    String[] vhosts() default { "localhost", "127.0.0.1", "::1", "[0000:0000:0000:0000:0000:0000:0000:0001]" };

    /** whether or not to use HTTPS (defaults to HTTP) **/
    boolean https() default false;

    /** whether or not to use basic auth **/
    boolean basicAuth() default false;

    /** the basic auth property file (defaults to target/test-classes/realm.properties) **/
    String basicAuthFile() default "target/test-classes/realm.properties";

    /** the location of the keystore if using HTTPS (defaults to target/test-classes/JUnitHttpServer.keystore) **/
    String keystore() default "target/test-classes/JUnitHttpServer.keystore";
    
    /** the keystore password **/
    String keystorePassword() default "opennms";
    
    /** the key password **/
    String keyPassword() default "opennms";

    /** zero or more webapps to include, with contexts **/
    Webapp[] webapps() default { };
    
}
