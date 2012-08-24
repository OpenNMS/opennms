/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

EMBEDDING KARAF IN A WEB APPLICATION
====================================

Purpose
-------
To embed Karaf in a web application.


Prerequisites for Running the Example
-------------------------------------
You must have the following installed on your machine:

  - JDK 1.5 or higher

  - Maven 2.0.9 or higher


Building and Deploying
----------------------
You can build and deploy this example in two ways:

- A. Using Jetty: Quick and Easy
  This option is useful if you want to see the example up and
  running quickly.
   
- B. Using Your Favorite Web Container
  This option is useful if you want to see Karaf running
  as a web application inside your favorite web container.


A. Using Jetty: Quick and Easy
------------------------------
To build the example and deploy to Jetty, complete the
following steps:

1. In a command prompt/shell, change to the directory
   that contains this README.txt file.

2. Enter the following Maven command:

     mvn package jetty:run

This Maven command builds the example web application, starts
Jetty and deploys the web application to Jetty. Once complete,
you should see the following printed to the console:

[INFO] Started Jetty Server
[INFO] Starting scanner at interval of 10 seconds.

Running a Client
----------------
To test the example, you can use the Apache Karaf client
to connect to the server and issue a Karaf command. For example,
try executing the "features:list" command as follows:

1. In a command prompt/shell, change to your product
   installation directory.

2. Run the following commands:

    Unix:
        ${KARAF_HOME}/bin/client features:list

    Windows:
        %KARAF_HOME%\bin\client.bat features:list

    Using JDK:
        java -jar lib/bin/karaf-client.jar features:list

In this case, you should see output similar to the following:

State         Version       Name       Repository
[uninstalled] [2.5.6.SEC01] spring     karaf-2.2.7
[uninstalled] [1.2.0      ] spring-dm  karaf-2.2.7
[uninstalled] [2.2.7] wrapper    karaf-2.2.7
[uninstalled] [2.2.7] obr        karaf-2.2.7
[uninstalled] [2.2.7] http       karaf-2.2.7
[uninstalled] [2.2.7] webconsole karaf-2.2.7
[installed  ] [2.2.7] ssh        karaf-2.2.7
[installed  ] [2.2.7] management karaf-2.2.7


B. Using Your Favorite Web Container
------------------------------------
You can deploy the web application to your favorite web
container, by completing the following steps:

1. In a command prompt/shell, change to the directory
   that contains this README.txt file.
   
2. Enter the following command:

     mvn package
     
Maven builds the web application, web-2.2.7.war, and
saves it in the target directory of this example. Deploy this
WAR file to your favorite web container. Once the application
is running, you can test it using the Apache Karaf client
as described in the "Running a Client" section above.
