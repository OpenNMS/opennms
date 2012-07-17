#!/bin/bash
mvn clean && mvn install -P gwt-compile && mvn jetty:run -P jetty-run
