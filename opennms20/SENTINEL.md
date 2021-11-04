
# Sentinel

## Building

Build the tree from the root:
```
./compile.pl -DskipTests=true
```

(Re)build and start the Sentinel container:
```
cd features/sentinel
./runInPlace.sh
```

A fresh Sentinel container looks like:
```
admin@sentinel()> bundle:list 
START LEVEL 100 , List Threshold: 50
ID │ State    │ Lvl │ Version         │ Name
───┼──────────┼─────┼─────────────────┼──────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────
36 │ Active   │  80 │ 18.0.0          │ Guava: Google Core Libraries for Java
43 │ Active   │  80 │ 1.10.0          │ Apache Commons Codec
49 │ Resolved │  80 │ 4.3.2           │ Apache Karaf :: Diagnostic :: Boot
51 │ Active   │  80 │ 4.3.2           │ Apache Karaf :: OSGi Services :: Event
75 │ Active   │  80 │ 30.0.0.SNAPSHOT │ OpenNMS :: Container :: Extender
76 │ Active   │  80 │ 30.0.0.SNAPSHOT │ OpenNMS :: Features :: SCV :: API
77 │ Active   │  80 │ 30.0.0.SNAPSHOT │ OpenNMS :: Features :: SCV :: JCEKS Impl.
78 │ Active   │  80 │ 30.0.0.SNAPSHOT │ OpenNMS :: Features :: SCV :: Shell
```

Install the (Hibernate) persistence layer:
```
admin@sentinel()> feature:install sentinel-persistence 
admin@sentinel()> health-check 
Verifying the health of the container

Verifying installed bundles      [ Success  ]
Retrieving NodeDao               [ Success  ]
Connecting to OpenNMS ReST API   [ Success  ]

=> Everything is awesome
```

> This requires a running a database on the local host with the matching schema. Start an OpenNMS instance with the tree to initialize.


The `EventDao` is exposed and available:
```
admin@sentinel()> service:get org.opennms.netmgt.dao.api.EventDao
HibernateTemplate    org.springframework.orm.hibernate3.HibernateTemplate@74ece131
SessionFactory       org.hibernate.impl.SessionFactoryImpl@4a79795d
```

### How is the EventDao wired?

We can see that the `EventDao` is coming from our service registry (which acts as a bridge to Spring):
```
admin@sentinel()> service:list org.opennms.netmgt.dao.api.EventDao                                                                                                                            
[org.opennms.netmgt.dao.api.EventDao]
-------------------------------------
 registration.source = onms
 service.bundleid = 176
 service.id = 213
 service.scope = singleton
Provided by : 
 OpenNMS :: Core :: Service Registry (176)
Used by: 
 System Bundle (0)
```

Looking for Sprint related bundles, we find:
```
admin@sentinel()> bundle:list  | grep -i spring
177 x Active   x  80 x 30.0.0.SNAPSHOT         x OpenNMS :: Core :: Spring
215 x Active   x  80 x 30.0.0.SNAPSHOT         x OpenNMS :: Container :: Spring - Extender
```

The 'Spring - Extender' defines a bundle activator that listens for Spring DM bundles here: [Activator.java#L49](https://github.com/OpenNMS/opennms/blob/opennms-28.1.1-1/container/spring-extender/src/main/java/org/opennms/netmgt/shared/bootstrap/Activator.java#L49)

Spring DM bundles contains a header in the manifest named [Spring-Context](https://github.com/OpenNMS/opennms/blob/opennms-28.1.1-1/features/distributed/dao/impl/pom.xml#L23).

The complete MANIFEST for the `org.opennms.features.distributed.dao-impl` bundle looks like:
```
Manifest-Version: 1.0
Bnd-LastModified: 1635984662874
Build-Jdk: 11.0.9.1
Built-By: jwhite
Bundle-Blueprint: OSGI-INF/blueprint/blueprint.xml
Bundle-Description: All features shared by distributed containers such
  as minion or sentinel
Bundle-License: http://www.gnu.org/licenses/agpl.html
Bundle-ManifestVersion: 2
Bundle-Name: OpenNMS :: Features :: Distributed :: DAO :: Impl
Bundle-RequiredExecutionEnvironment: JavaSE-1.8
Bundle-SymbolicName: org.opennms.features.distributed.dao-impl
Bundle-Version: 30.0.0.SNAPSHOT
Created-By: Apache Maven Bundle Plugin
Export-Package: org.opennms.netmgt.dao.support;version="30.0.0.SNAPSHO
 T";uses:="org.opennms.core.collections,org.opennms.netmgt.collection.
 api,org.opennms.netmgt.collection.support,org.opennms.netmgt.config.a
 pi,org.opennms.netmgt.config.datacollection,org.opennms.netmgt.dao.ap
 i,org.opennms.netmgt.events.api.annotations,org.opennms.netmgt.filter
 .api,org.opennms.netmgt.measurements.api,org.opennms.netmgt.model,org
 .opennms.netmgt.rrd,org.springframework.beans.factory,org.springframe
 work.core.io,org.springframework.dao,org.springframework.transaction,
 org.springframework.transaction.annotation",org.opennms.netmgt.dao.ut
 il;version="30.0.0.SNAPSHOT";uses:="org.opennms.netmgt.dao.api,org.op
 ennms.netmgt.model,org.opennms.netmgt.xml.event",org.opennms.netmgt.d
 ao.stats;version="30.0.0.SNAPSHOT";uses:="org.opennms.core.criteria,o
 rg.opennms.netmgt.dao.api,org.opennms.netmgt.model,org.springframewor
 k.beans.factory,org.springframework.transaction.annotation",org.openn
 ms.netmgt.dao.api;version="30.0.0.SNAPSHOT";uses:="org.hibernate,org.
 hibernate.criterion,org.opennms.core.criteria,org.opennms.features.re
 porting.model,org.opennms.netmgt.collection.api,org.opennms.netmgt.co
 nfig.ackd,org.opennms.netmgt.config.api,org.opennms.netmgt.config.jav
 amail,org.opennms.netmgt.config.microblog,org.opennms.netmgt.config.p
 rovisiond,org.opennms.netmgt.config.reportd,org.opennms.netmgt.config
 .reporting,org.opennms.netmgt.config.siteStatusViews,org.opennms.netm
 gt.config.statsd.model,org.opennms.netmgt.config.surveillanceViews,or
 g.opennms.netmgt.config.tl1d,org.opennms.netmgt.model,org.opennms.net
 mgt.model.alarm,org.opennms.netmgt.model.minion,org.opennms.netmgt.mo
 del.monitoringLocations,org.opennms.netmgt.model.outage,org.opennms.n
 etmgt.xml.event,org.springframework.dao,org.springframework.stereotyp
 e,org.springframework.transaction.annotation",org.opennms.netmgt.enli
 nkd.persistence.api;version="30.0.0.SNAPSHOT";uses:="org.opennms.core
 .utils,org.opennms.netmgt.dao.api,org.opennms.netmgt.model",org.openn
 ms.netmgt.enlinkd.service.api;version="30.0.0.SNAPSHOT";uses:="org.op
 ennms.netmgt.model",org.opennms.netmgt.topologies.service.api;version
 ="30.0.0.SNAPSHOT",org.opennms.netmgt.bsm.persistence.api;version="30
 .0.0.SNAPSHOT";uses:="javax.persistence,javax.validation.constraints,
 org.opennms.netmgt.bsm.persistence.api.functions.map,org.opennms.netm
 gt.bsm.persistence.api.functions.reduce,org.opennms.netmgt.dao.api,or
 g.opennms.netmgt.model",org.opennms.netmgt.bsm.persistence.api.functi
 ons.reduce;version="30.0.0.SNAPSHOT";uses:="javax.persistence,org.ope
 nnms.netmgt.dao.api",org.opennms.netmgt.bsm.persistence.api.functions
 .map;version="30.0.0.SNAPSHOT";uses:="javax.persistence,org.opennms.n
 etmgt.dao.api,org.opennms.netmgt.model",org.opennms.netmgt.endpoints.
 grafana.api;version="30.0.0.SNAPSHOT";uses:="javax.persistence",org.o
 pennms.netmgt.endpoints.grafana.persistence.api;version="30.0.0.SNAPS
 HOT";uses:="org.opennms.netmgt.dao.api,org.opennms.netmgt.endpoints.g
 rafana.api,org.springframework.stereotype",org.opennms.netmgt.flows.c
 lassification.persistence.api;version="30.0.0.SNAPSHOT";uses:="javax.
 persistence,org.opennms.netmgt.dao.api",org.opennms.netmgt.telemetry.
 protocols.bmp.persistence.api;version="30.0.0.SNAPSHOT";uses:="javax.
 persistence,org.opennms.netmgt.dao.api",org.opennms.netmgt.bsm.persis
 tence.api.functions;version="30.0.0"
Export-Service: org.opennms.netmgt.config.api.DatabaseSchemaConfig
Import-Package: com.google.common.base;version="[18.0,19)",com.google.
 common.cache;version="[18.0,19)",com.google.common.collect;version="[
 18.0,19)",javax.management,javax.persistence,javax.validation.constra
 ints,org.apache.commons.lang.builder;version="[2.6,3)",org.apache.com
 mons.io;version="[1.4,2)",org.apache.commons.lang;version="[2.6,3)",o
 rg.slf4j;version="[1.7,2)",org.hibernate,org.hibernate.classic,org.hi
 bernate.criterion,org.hibernate.metadata,org.hibernate.transform,org.
 hibernate.type,org.opennms.core.criteria;version="[30.0,31)",org.open
 nms.core.criteria.restrictions;version="[30.0,31)",org.opennms.core.u
 tils;version="[30.0,31)",org.opennms.core.xml;version="[30.0,31)",org
 .opennms.core.collections;version="[30.0,31)",org.opennms.core.spring
 ;version="[30.0,31)",org.opennms.core.sysprops;version="[30.0,31)",or
 g.opennms.features.reporting.model;version="[30.0,31)",org.opennms.ne
 tmgt.collection.api;version="[30.0,31)",org.opennms.netmgt.collection
 .support;version="[30.0,31)",org.opennms.netmgt.config;version="[30.0
 ,31)",org.opennms.netmgt.config.ackd;version="[30.0,31)",org.opennms.
 netmgt.config.api;version="[30.0,31)",org.opennms.netmgt.config.colle
 ctd;version="[30.0,31)",org.opennms.netmgt.config.datacollection;vers
 ion="[30.0,31)",org.opennms.netmgt.config.javamail;version="[30.0,31)
 ",org.opennms.netmgt.config.microblog;version="[30.0,31)",org.opennms
 .netmgt.config.provisiond;version="[30.0,31)",org.opennms.netmgt.conf
 ig.reportd;version="[30.0,31)",org.opennms.netmgt.config.reporting;ve
 rsion="[30.0,31)",org.opennms.netmgt.config.siteStatusViews;version="
 [30.0,31)",org.opennms.netmgt.config.statsd;version="[30.0,31)",org.o
 pennms.netmgt.config.statsd.model;version="[30.0,31)",org.opennms.net
 mgt.config.surveillanceViews;version="[30.0,31)",org.opennms.netmgt.c
 onfig.tl1d;version="[30.0,31)",org.opennms.netmgt.dao.api;version="[3
 0.0,31)",org.opennms.netmgt.dao.support;version="[30.0,31)",org.openn
 ms.netmgt.dao.util;version="[30.0,31)",org.opennms.netmgt.events.api;
 version="[30.0,31)",org.opennms.netmgt.events.api.annotations;version
 ="[30.0,31)",org.opennms.netmgt.filter.api;version="[30.0,31)",org.op
 ennms.netmgt.measurements.api;version="[30.0,31)",org.opennms.netmgt.
 measurements.model;version="[30.0,31)",org.opennms.netmgt.model;versi
 on="[30.0,31)",org.opennms.netmgt.model.alarm;version="[30.0,31)",org
 .opennms.netmgt.model.minion;version="[30.0,31)",org.opennms.netmgt.m
 odel.monitoringLocations;version="[30.0,31)",org.opennms.netmgt.model
 .outage;version="[30.0,31)",org.opennms.netmgt.rrd;version="[30.0,31)
 ",org.opennms.netmgt.xml.event;version="[30.0,31)",org.opennms.netmgt
 .enlinkd.persistence.api;version="[30.0,31)",org.opennms.netmgt.enlin
 kd.service.api;version="[30.0,31)",org.opennms.netmgt.topologies.serv
 ice.api;version="[30.0,31)",org.opennms.netmgt.bsm.persistence.api;ve
 rsion="[30.0,31)",org.opennms.netmgt.bsm.persistence.api.functions.ma
 p;version="[30.0,31)",org.opennms.netmgt.bsm.persistence.api.function
 s.reduce;version="[30.0,31)",org.opennms.netmgt.endpoints.grafana.api
 ;version="[30.0,31)",org.opennms.netmgt.endpoints.grafana.persistence
 .api;version="[30.0,31)",org.opennms.netmgt.flows.classification.pers
 istence.api;version="[30.0,31)",org.opennms.netmgt.telemetry.protocol
 s.bmp.persistence.api;version="[30.0,31)",org.springframework.stereot
 ype;version="[4.2,5)",org.springframework.transaction;version="[4.2,5
 )",org.springframework.transaction.annotation;version="[4.2,5)",org.s
 pringframework.transaction.support;version="[4.2,5)",org.springframew
 ork.beans;version="[4.2,5)",org.springframework.beans.factory;version
 ="[4.2,5)",org.springframework.beans.factory.annotation;version="[4.2
 ,5)",org.springframework.core.io;version="[4.2,5)",org.springframewor
 k.dao;version="[4.2,5)",org.springframework.dao.support;version="[4.2
 ,5)",org.springframework.jmx.access;version="[4.2,5)",org.springframe
 work.orm;version="[4.2,5)",org.springframework.orm.hibernate3;version
 ="[4.2,5)",org.springframework.orm.hibernate3.support;version="[4.2,5
 )",org.springframework.util;version="[4.2,5)",org.aopalliance.aop;ver
 sion="[1.0,2)",javassist.util.proxy;version="[3.18,4)",javax.sql,java
 x.xml.bind.annotation;version="[2.3,3)",javax.xml.bind.annotation.ada
 pters;version="[2.3,3)",org.codehaus.jackson.annotate;version="[1.9,2
 )",org.codehaus.jackson.map.annotate;version="[1.9,2)",org.hibernate.
 annotations,org.hibernate.proxy,org.hibernate.validator;version="[4.1
 ,4.2)",org.hibernate.validator.constraints;version="[4.1,4.2)",org.hi
 bernate.validator.messageinterpolation;version="[4.1,4.2)",org.hibern
 ate.validator.resourceloading;version="[4.1,4.2)",org.hibernate.valid
 ator.ap,org.hibernate.validator.ap.checks,org.hibernate.validator.ap.
 util,org.opennms.core.soa;version="[30.0,31)",org.opennms.netmgt.coll
 ection.core;version="[30.0,31)",org.opennms.netmgt.filter;version="[3
 0.0,31)",org.opennms.netmgt.poller;version="[30.0,31)",org.springfram
 ework.beans.factory.config;version="[4.2,5)",org.springframework.cach
 e.concurrent;version="[4.2,5)",org.springframework.cache.annotation;v
 ersion="[4.2,5)",org.springframework.cache.support;version="[4.2,5)",
 org.springframework.orm.hibernate3.annotation;version="[4.2,5)",org.s
 pringframework.aop;version="[4.2,5)",org.springframework.aop.aspectj;
 version="[4.2,5)",org.springframework.aop.aspectj.annotation;version=
 "[4.2,5)",org.springframework.aop.aspectj.autoproxy;version="[4.2,5)"
 ,org.springframework.aop.config;version="[4.2,5)",org.springframework
 .aop.framework;version="[4.2,5)",org.springframework.aop.framework.ad
 apter;version="[4.2,5)",org.springframework.aop.framework.autoproxy;v
 ersion="[4.2,5)",org.springframework.aop.framework.autoproxy.target;v
 ersion="[4.2,5)",org.springframework.aop.interceptor;version="[4.2,5)
 ",org.springframework.aop.scope;version="[4.2,5)",org.springframework
 .aop.support;version="[4.2,5)",org.springframework.aop.target;version
 ="[4.2,5)",org.springframework.context.annotation;version="[4.2,5)",o
 rg.springframework.jdbc.core;version="[4.2,5)"
Include-Resource: META-INF/services/javax.validation.spi.ValidationPro
 vider=src/main/resources/META-INF/services/javax.validation.spi.Valid
 ationProvider,OSGI-INF/blueprint/blueprint.xml=src/main/resources/OSG
 I-INF/blueprint/blueprint.xml
Private-Package: org.opennms.netmgt.enlinkd.persistence.impl,org.openn
 ms.netmgt.enlinkd.service.impl,org.opennms.netmgt.topologies.service.
 impl,org.opennms.netmgt.bsm.persistence.impl,org.opennms.netmgt.dao.j
 mx,org.opennms.netmgt.dao.hibernate,org.opennms.netmgt.dao.jaxb,org.o
 pennms.netmgt.dao.jaxb.collector,org.opennms.netmgt.endpoints.grafana
 .persistence.impl,org.opennms.netmgt.flows.classification.persistence
 .impl,org.opennms.netmgt.telemetry.protocols.bmp.persistence.impl
Require-Capability: osgi.ee;filter:="(&(osgi.ee=JavaSE)(version=11))"
Spring-Context: META-INF/opennms/applicationContext-osgi.xml,META-INF/
 opennms/applicationContext-shared.xml,META-INF/opennms/component-dao.
 xml
Tool: Bnd-5.1.1.202006162103
```

This is a very carefully wired bundle that includes classes related to:
* Spring + Spring Transactions
* Hibernate
* All DAO implementation classes i.e. `org.opennms.netmgt.dao.hibernate.EventDaoHibernate`

> I believe this was implemented as an Uber JAR due to class-loader challenges w/ Spring, Hibernate and generating proxy classes (AOP) for the DAOs.

This is the bundle that loads the Spring contexts and wires the Hibernate DAOs using mostly the same code as the core with the current bootstrap

