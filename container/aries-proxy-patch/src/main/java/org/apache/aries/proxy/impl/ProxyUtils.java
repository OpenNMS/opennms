/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.aries.proxy.impl;

import java.math.BigDecimal;

import org.objectweb.asm.Opcodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProxyUtils
{
  private static Logger LOGGER = LoggerFactory.getLogger(ProxyUtils.class);
  public static final int JAVA_CLASS_VERSION = new BigDecimal(System.getProperty("java.class.version")).intValue();
  private static int weavingJavaVersion = -1; // initialise an invalid number
  
  /**
   * Get the java version to be woven at.
   * @return
   */
  public static int getWeavingJavaVersion() {
    if (weavingJavaVersion == -1 ) {
      //In order to avoid an inconsistent stack error the version of the woven byte code needs to match
      //the level of byte codes in the original class
      switch(JAVA_CLASS_VERSION) {
        case Opcodes.V21:
          LOGGER.debug("Weaving to Java 21");
          weavingJavaVersion = Opcodes.V21;
          break;
        case Opcodes.V20:
          LOGGER.debug("Weaving to Java 20");
          weavingJavaVersion = Opcodes.V20;
          break;
        case Opcodes.V19:
          LOGGER.debug("Weaving to Java 19");
          weavingJavaVersion = Opcodes.V19;
          break;
        case Opcodes.V18:
          LOGGER.debug("Weaving to Java 18");
          weavingJavaVersion = Opcodes.V18;
          break;
        case Opcodes.V17:
          LOGGER.debug("Weaving to Java 17");
          weavingJavaVersion = Opcodes.V17;
          break;
        case Opcodes.V16:
          LOGGER.debug("Weaving to Java 16");
          weavingJavaVersion = Opcodes.V16;
          break;
        case Opcodes.V15:
          LOGGER.debug("Weaving to Java 15");
          weavingJavaVersion = Opcodes.V15;
          break;
        case Opcodes.V14:
          LOGGER.debug("Weaving to Java 14");
          weavingJavaVersion = Opcodes.V14;
          break;
        case Opcodes.V13:
          LOGGER.debug("Weaving to Java 13");
          weavingJavaVersion = Opcodes.V13;
          break;
        case Opcodes.V12:
          LOGGER.debug("Weaving to Java 12");
          weavingJavaVersion = Opcodes.V12;
          break;
        case Opcodes.V11:
          LOGGER.debug("Weaving to Java 11");
          weavingJavaVersion = Opcodes.V11;
          break;
        case Opcodes.V10:
          LOGGER.debug("Weaving to Java 10");
          weavingJavaVersion = Opcodes.V10;
          break;
        case Opcodes.V9:
          LOGGER.debug("Weaving to Java 9");
          weavingJavaVersion = Opcodes.V9;
          break;
        case Opcodes.V1_8:
          LOGGER.debug("Weaving to Java 8");
          weavingJavaVersion = Opcodes.V1_8;
          break;
        case Opcodes.V1_7:
          LOGGER.debug("Weaving to Java 7");
          weavingJavaVersion = Opcodes.V1_7;
          break;
        case Opcodes.V1_6:
          LOGGER.debug("Weaving to Java 6");
          weavingJavaVersion = Opcodes.V1_6;
          break;
        case Opcodes.V1_5:
          LOGGER.debug("Weaving to Java 5");
          weavingJavaVersion = Opcodes.V1_5;
          break;
        default:
          // OPENNMS: upstream throws IllegalArgumentException("Invalid Java version ...") for anything
          // newer than Java 21, so every blueprint <reference> proxy fails on JDK 22+. Proxies are
          // generated from scratch, so Java 21 byte code is valid on any newer JVM.
          if (JAVA_CLASS_VERSION > Opcodes.V21) {
            LOGGER.debug("Weaving to Java 21 on class file version {}", JAVA_CLASS_VERSION);
            weavingJavaVersion = Opcodes.V21;
            break;
          }
          //aries should work with Java 5 or above - also will highlight when a higher level (and unsupported) level of Java is released
          throw new IllegalArgumentException("Invalid Java version " + JAVA_CLASS_VERSION);
      }
    } 
    return weavingJavaVersion;
  } 
}
