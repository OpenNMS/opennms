--
-- PostgreSQL database dump
--

SET client_encoding = 'SQL_ASCII';
SET check_function_bodies = false;
SET client_min_messages = warning;

SET search_path = public, pg_catalog;

--
-- Name: alarmsnxtid; Type: SEQUENCE SET; Schema: public; Owner: opennms
--

SELECT pg_catalog.setval('alarmsnxtid', 1, false);


--
-- Name: catnxtid; Type: SEQUENCE SET; Schema: public; Owner: opennms
--

SELECT pg_catalog.setval('catnxtid', 1, false);


--
-- Name: demandpollnxtid; Type: SEQUENCE SET; Schema: public; Owner: opennms
--

SELECT pg_catalog.setval('demandpollnxtid', 1, false);


--
-- Name: eventsnxtid; Type: SEQUENCE SET; Schema: public; Owner: opennms
--

SELECT pg_catalog.setval('eventsnxtid', 1, true);


--
-- Name: mapnxtid; Type: SEQUENCE SET; Schema: public; Owner: opennms
--

SELECT pg_catalog.setval('mapnxtid', 1, false);


--
-- Name: nodenxtid; Type: SEQUENCE SET; Schema: public; Owner: opennms
--

SELECT pg_catalog.setval('nodenxtid', 6, true);


--
-- Name: notifynxtid; Type: SEQUENCE SET; Schema: public; Owner: opennms
--

SELECT pg_catalog.setval('notifynxtid', 1, false);


--
-- Name: outagenxtid; Type: SEQUENCE SET; Schema: public; Owner: opennms
--

SELECT pg_catalog.setval('outagenxtid', 2, true);


--
-- Name: pollresultnxtid; Type: SEQUENCE SET; Schema: public; Owner: opennms
--

SELECT pg_catalog.setval('pollresultnxtid', 1, false);


--
-- Name: servicenxtid; Type: SEQUENCE SET; Schema: public; Owner: opennms
--

SELECT pg_catalog.setval('servicenxtid', 3, true);


--
-- Name: usernotifnxtid; Type: SEQUENCE SET; Schema: public; Owner: opennms
--

SELECT pg_catalog.setval('usernotifnxtid', 1, false);


--
-- Data for Name: alarms; Type: TABLE DATA; Schema: public; Owner: opennms
--



--
-- Data for Name: assets; Type: TABLE DATA; Schema: public; Owner: opennms
--

INSERT INTO assets (nodeid, category, manufacturer, vendor, modelnumber, serialnumber, description, circuitid, assetnumber, operatingsystem, rack, slot, port, region, division, department, address1, address2, city, state, zip, building, floor, room, vendorphone, vendorfax, vendorassetnumber, userlastmodified, lastmodifieddate, dateinstalled, lease, leaseexpires, supportphone, maintcontract, maintcontractexpires, displaycategory, notifycategory, pollercategory, thresholdcategory, "comment") VALUES (1, 'Unspecified', NULL, NULL, NULL, NULL, NULL, NULL, 'imported:1', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '                    ', '2006-07-28 16:48:07.844', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO assets (nodeid, category, manufacturer, vendor, modelnumber, serialnumber, description, circuitid, assetnumber, operatingsystem, rack, slot, port, region, division, department, address1, address2, city, state, zip, building, floor, room, vendorphone, vendorfax, vendorassetnumber, userlastmodified, lastmodifieddate, dateinstalled, lease, leaseexpires, supportphone, maintcontract, maintcontractexpires, displaycategory, notifycategory, pollercategory, thresholdcategory, "comment") VALUES (2, 'Unspecified', NULL, NULL, NULL, NULL, NULL, NULL, 'imported:2', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '                    ', '2006-07-28 16:48:07.871', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO assets (nodeid, category, manufacturer, vendor, modelnumber, serialnumber, description, circuitid, assetnumber, operatingsystem, rack, slot, port, region, division, department, address1, address2, city, state, zip, building, floor, room, vendorphone, vendorfax, vendorassetnumber, userlastmodified, lastmodifieddate, dateinstalled, lease, leaseexpires, supportphone, maintcontract, maintcontractexpires, displaycategory, notifycategory, pollercategory, thresholdcategory, "comment") VALUES (3, 'Unspecified', NULL, NULL, NULL, NULL, NULL, NULL, 'imported:3', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '                    ', '2006-07-28 16:48:07.891', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO assets (nodeid, category, manufacturer, vendor, modelnumber, serialnumber, description, circuitid, assetnumber, operatingsystem, rack, slot, port, region, division, department, address1, address2, city, state, zip, building, floor, room, vendorphone, vendorfax, vendorassetnumber, userlastmodified, lastmodifieddate, dateinstalled, lease, leaseexpires, supportphone, maintcontract, maintcontractexpires, displaycategory, notifycategory, pollercategory, thresholdcategory, "comment") VALUES (4, 'Unspecified', NULL, NULL, NULL, NULL, NULL, NULL, 'imported:4', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '                    ', '2006-07-28 16:48:07.906', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO assets (nodeid, category, manufacturer, vendor, modelnumber, serialnumber, description, circuitid, assetnumber, operatingsystem, rack, slot, port, region, division, department, address1, address2, city, state, zip, building, floor, room, vendorphone, vendorfax, vendorassetnumber, userlastmodified, lastmodifieddate, dateinstalled, lease, leaseexpires, supportphone, maintcontract, maintcontractexpires, displaycategory, notifycategory, pollercategory, thresholdcategory, "comment") VALUES (5, 'Unspecified', NULL, NULL, NULL, NULL, NULL, NULL, '5', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '                    ', '2006-07-28 16:48:07.925', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO assets (nodeid, category, manufacturer, vendor, modelnumber, serialnumber, description, circuitid, assetnumber, operatingsystem, rack, slot, port, region, division, department, address1, address2, city, state, zip, building, floor, room, vendorphone, vendorfax, vendorassetnumber, userlastmodified, lastmodifieddate, dateinstalled, lease, leaseexpires, supportphone, maintcontract, maintcontractexpires, displaycategory, notifycategory, pollercategory, thresholdcategory, "comment") VALUES (6, 'Unspecified', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '                    ', '2006-07-28 16:48:07.942', NULL, NULL, NULL, NULL, NULL, NULL, 'category1', NULL, NULL, NULL, NULL);


--
-- Data for Name: atinterface; Type: TABLE DATA; Schema: public; Owner: opennms
--



--
-- Data for Name: categories; Type: TABLE DATA; Schema: public; Owner: opennms
--



--
-- Data for Name: category_node; Type: TABLE DATA; Schema: public; Owner: opennms
--



--
-- Data for Name: datalinkinterface; Type: TABLE DATA; Schema: public; Owner: opennms
--



--
-- Data for Name: demandpolls; Type: TABLE DATA; Schema: public; Owner: opennms
--



--
-- Data for Name: distpoller; Type: TABLE DATA; Schema: public; Owner: opennms
--

INSERT INTO distpoller (dpname, dpip, dpcomment, dpdisclimit, dplastnodepull, dplasteventpull, dplastpackagepush, dpadminstate, dprunstate) VALUES ('localhost', '127.0.0.1', NULL, NULL, NULL, NULL, NULL, NULL, NULL);


--
-- Data for Name: element; Type: TABLE DATA; Schema: public; Owner: opennms
--



--
-- Data for Name: events; Type: TABLE DATA; Schema: public; Owner: opennms
--

INSERT INTO events (eventid, eventuei, nodeid, eventtime, eventhost, eventsource, ipaddr, eventdpname, eventsnmphost, serviceid, eventsnmp, eventparms, eventcreatetime, eventdescr, eventloggroup, eventlogmsg, eventseverity, eventpathoutage, eventcorrelation, eventsuppressedcount, eventoperinstruct, eventautoaction, eventoperaction, eventoperactionmenutext, eventnotification, eventtticket, eventtticketstate, eventforward, eventmouseovertext, eventlog, eventdisplay, eventackuser, eventacktime, alarmid) VALUES (1, 'uei.opennms.org/test', NULL, '2006-07-28 16:48:07.957', NULL, 'test', NULL, 'localhost', NULL, NULL, NULL, NULL, '2006-07-28 16:48:07.957', NULL, NULL, NULL, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'Y', 'Y', NULL, NULL, NULL);


--
-- Data for Name: ifservices; Type: TABLE DATA; Schema: public; Owner: opennms
--

INSERT INTO ifservices (nodeid, ipaddr, ifindex, serviceid, lastgood, lastfail, qualifier, status, source, "notify") VALUES (1, '192.168.1.1', 1, 2, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO ifservices (nodeid, ipaddr, ifindex, serviceid, lastgood, lastfail, qualifier, status, source, "notify") VALUES (1, '192.168.1.1', 1, 1, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO ifservices (nodeid, ipaddr, ifindex, serviceid, lastgood, lastfail, qualifier, status, source, "notify") VALUES (1, '192.168.1.2', 2, 1, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO ifservices (nodeid, ipaddr, ifindex, serviceid, lastgood, lastfail, qualifier, status, source, "notify") VALUES (1, '192.168.1.2', 2, 3, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO ifservices (nodeid, ipaddr, ifindex, serviceid, lastgood, lastfail, qualifier, status, source, "notify") VALUES (1, '192.168.1.3', 3, 1, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO ifservices (nodeid, ipaddr, ifindex, serviceid, lastgood, lastfail, qualifier, status, source, "notify") VALUES (2, '192.168.2.1', -1, 2, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO ifservices (nodeid, ipaddr, ifindex, serviceid, lastgood, lastfail, qualifier, status, source, "notify") VALUES (2, '192.168.2.1', -1, 1, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO ifservices (nodeid, ipaddr, ifindex, serviceid, lastgood, lastfail, qualifier, status, source, "notify") VALUES (2, '192.168.2.2', -1, 3, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO ifservices (nodeid, ipaddr, ifindex, serviceid, lastgood, lastfail, qualifier, status, source, "notify") VALUES (2, '192.168.2.2', -1, 1, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO ifservices (nodeid, ipaddr, ifindex, serviceid, lastgood, lastfail, qualifier, status, source, "notify") VALUES (2, '192.168.2.3', -1, 1, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO ifservices (nodeid, ipaddr, ifindex, serviceid, lastgood, lastfail, qualifier, status, source, "notify") VALUES (3, '192.168.3.1', -1, 2, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO ifservices (nodeid, ipaddr, ifindex, serviceid, lastgood, lastfail, qualifier, status, source, "notify") VALUES (3, '192.168.3.1', -1, 1, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO ifservices (nodeid, ipaddr, ifindex, serviceid, lastgood, lastfail, qualifier, status, source, "notify") VALUES (3, '192.168.3.2', -1, 1, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO ifservices (nodeid, ipaddr, ifindex, serviceid, lastgood, lastfail, qualifier, status, source, "notify") VALUES (3, '192.168.3.2', -1, 3, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO ifservices (nodeid, ipaddr, ifindex, serviceid, lastgood, lastfail, qualifier, status, source, "notify") VALUES (3, '192.168.3.3', -1, 1, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO ifservices (nodeid, ipaddr, ifindex, serviceid, lastgood, lastfail, qualifier, status, source, "notify") VALUES (4, '192.168.4.1', -1, 2, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO ifservices (nodeid, ipaddr, ifindex, serviceid, lastgood, lastfail, qualifier, status, source, "notify") VALUES (4, '192.168.4.1', -1, 1, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO ifservices (nodeid, ipaddr, ifindex, serviceid, lastgood, lastfail, qualifier, status, source, "notify") VALUES (4, '192.168.4.2', -1, 1, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO ifservices (nodeid, ipaddr, ifindex, serviceid, lastgood, lastfail, qualifier, status, source, "notify") VALUES (4, '192.168.4.2', -1, 3, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO ifservices (nodeid, ipaddr, ifindex, serviceid, lastgood, lastfail, qualifier, status, source, "notify") VALUES (4, '192.168.4.3', -1, 1, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO ifservices (nodeid, ipaddr, ifindex, serviceid, lastgood, lastfail, qualifier, status, source, "notify") VALUES (5, '10.1.1.1', -1, 2, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO ifservices (nodeid, ipaddr, ifindex, serviceid, lastgood, lastfail, qualifier, status, source, "notify") VALUES (5, '10.1.1.1', -1, 1, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO ifservices (nodeid, ipaddr, ifindex, serviceid, lastgood, lastfail, qualifier, status, source, "notify") VALUES (5, '10.1.1.2', -1, 1, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO ifservices (nodeid, ipaddr, ifindex, serviceid, lastgood, lastfail, qualifier, status, source, "notify") VALUES (5, '10.1.1.2', -1, 3, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO ifservices (nodeid, ipaddr, ifindex, serviceid, lastgood, lastfail, qualifier, status, source, "notify") VALUES (5, '10.1.1.3', -1, 1, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO ifservices (nodeid, ipaddr, ifindex, serviceid, lastgood, lastfail, qualifier, status, source, "notify") VALUES (6, '10.1.2.1', -1, 2, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO ifservices (nodeid, ipaddr, ifindex, serviceid, lastgood, lastfail, qualifier, status, source, "notify") VALUES (6, '10.1.2.1', -1, 1, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO ifservices (nodeid, ipaddr, ifindex, serviceid, lastgood, lastfail, qualifier, status, source, "notify") VALUES (6, '10.1.2.2', -1, 1, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO ifservices (nodeid, ipaddr, ifindex, serviceid, lastgood, lastfail, qualifier, status, source, "notify") VALUES (6, '10.1.2.2', -1, 3, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO ifservices (nodeid, ipaddr, ifindex, serviceid, lastgood, lastfail, qualifier, status, source, "notify") VALUES (6, '10.1.2.3', -1, 1, NULL, NULL, NULL, NULL, NULL, NULL);


--
-- Data for Name: inventory; Type: TABLE DATA; Schema: public; Owner: opennms
--



--
-- Data for Name: ipinterface; Type: TABLE DATA; Schema: public; Owner: opennms
--

INSERT INTO ipinterface (nodeid, ipaddr, ifindex, iphostname, ismanaged, ipstatus, iplastcapsdpoll, issnmpprimary) VALUES (1, '192.168.1.1', 1, NULL, 'M', 1, NULL, 'P');
INSERT INTO ipinterface (nodeid, ipaddr, ifindex, iphostname, ismanaged, ipstatus, iplastcapsdpoll, issnmpprimary) VALUES (1, '192.168.1.2', 2, NULL, 'M', 1, NULL, 'S');
INSERT INTO ipinterface (nodeid, ipaddr, ifindex, iphostname, ismanaged, ipstatus, iplastcapsdpoll, issnmpprimary) VALUES (1, '192.168.1.3', 3, NULL, 'M', 1, NULL, 'N');
INSERT INTO ipinterface (nodeid, ipaddr, ifindex, iphostname, ismanaged, ipstatus, iplastcapsdpoll, issnmpprimary) VALUES (2, '192.168.2.1', -1, NULL, 'M', 1, NULL, 'P');
INSERT INTO ipinterface (nodeid, ipaddr, ifindex, iphostname, ismanaged, ipstatus, iplastcapsdpoll, issnmpprimary) VALUES (2, '192.168.2.2', -1, NULL, 'M', 1, NULL, 'S');
INSERT INTO ipinterface (nodeid, ipaddr, ifindex, iphostname, ismanaged, ipstatus, iplastcapsdpoll, issnmpprimary) VALUES (2, '192.168.2.3', -1, NULL, 'M', 1, NULL, 'N');
INSERT INTO ipinterface (nodeid, ipaddr, ifindex, iphostname, ismanaged, ipstatus, iplastcapsdpoll, issnmpprimary) VALUES (3, '192.168.3.1', -1, NULL, 'M', 1, NULL, 'P');
INSERT INTO ipinterface (nodeid, ipaddr, ifindex, iphostname, ismanaged, ipstatus, iplastcapsdpoll, issnmpprimary) VALUES (3, '192.168.3.2', -1, NULL, 'M', 1, NULL, 'S');
INSERT INTO ipinterface (nodeid, ipaddr, ifindex, iphostname, ismanaged, ipstatus, iplastcapsdpoll, issnmpprimary) VALUES (3, '192.168.3.3', -1, NULL, 'M', 1, NULL, 'N');
INSERT INTO ipinterface (nodeid, ipaddr, ifindex, iphostname, ismanaged, ipstatus, iplastcapsdpoll, issnmpprimary) VALUES (4, '192.168.4.1', -1, NULL, 'M', 1, NULL, 'P');
INSERT INTO ipinterface (nodeid, ipaddr, ifindex, iphostname, ismanaged, ipstatus, iplastcapsdpoll, issnmpprimary) VALUES (4, '192.168.4.2', -1, NULL, 'M', 1, NULL, 'S');
INSERT INTO ipinterface (nodeid, ipaddr, ifindex, iphostname, ismanaged, ipstatus, iplastcapsdpoll, issnmpprimary) VALUES (4, '192.168.4.3', -1, NULL, 'M', 1, NULL, 'N');
INSERT INTO ipinterface (nodeid, ipaddr, ifindex, iphostname, ismanaged, ipstatus, iplastcapsdpoll, issnmpprimary) VALUES (5, '10.1.1.1', -1, NULL, 'M', 1, NULL, 'P');
INSERT INTO ipinterface (nodeid, ipaddr, ifindex, iphostname, ismanaged, ipstatus, iplastcapsdpoll, issnmpprimary) VALUES (5, '10.1.1.2', -1, NULL, 'M', 1, NULL, 'S');
INSERT INTO ipinterface (nodeid, ipaddr, ifindex, iphostname, ismanaged, ipstatus, iplastcapsdpoll, issnmpprimary) VALUES (5, '10.1.1.3', -1, NULL, 'M', 1, NULL, 'N');
INSERT INTO ipinterface (nodeid, ipaddr, ifindex, iphostname, ismanaged, ipstatus, iplastcapsdpoll, issnmpprimary) VALUES (6, '10.1.2.1', -1, NULL, 'M', 1, NULL, 'P');
INSERT INTO ipinterface (nodeid, ipaddr, ifindex, iphostname, ismanaged, ipstatus, iplastcapsdpoll, issnmpprimary) VALUES (6, '10.1.2.2', -1, NULL, 'M', 1, NULL, 'S');
INSERT INTO ipinterface (nodeid, ipaddr, ifindex, iphostname, ismanaged, ipstatus, iplastcapsdpoll, issnmpprimary) VALUES (6, '10.1.2.3', -1, NULL, 'M', 1, NULL, 'N');


--
-- Data for Name: iprouteinterface; Type: TABLE DATA; Schema: public; Owner: opennms
--



--
-- Data for Name: map; Type: TABLE DATA; Schema: public; Owner: opennms
--



--
-- Data for Name: node; Type: TABLE DATA; Schema: public; Owner: opennms
--

INSERT INTO node (nodeid, dpname, nodecreatetime, nodeparentid, nodetype, nodesysoid, nodesysname, nodesysdescription, nodesyslocation, nodesyscontact, nodelabel, nodelabelsource, nodenetbiosname, nodedomainname, operatingsystem, lastcapsdpoll) VALUES (1, 'localhost', '2006-07-28 16:48:07.844', NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'node1', NULL, NULL, NULL, NULL, NULL);
INSERT INTO node (nodeid, dpname, nodecreatetime, nodeparentid, nodetype, nodesysoid, nodesysname, nodesysdescription, nodesyslocation, nodesyscontact, nodelabel, nodelabelsource, nodenetbiosname, nodedomainname, operatingsystem, lastcapsdpoll) VALUES (2, 'localhost', '2006-07-28 16:48:07.871', NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'node2', NULL, NULL, NULL, NULL, NULL);
INSERT INTO node (nodeid, dpname, nodecreatetime, nodeparentid, nodetype, nodesysoid, nodesysname, nodesysdescription, nodesyslocation, nodesyscontact, nodelabel, nodelabelsource, nodenetbiosname, nodedomainname, operatingsystem, lastcapsdpoll) VALUES (3, 'localhost', '2006-07-28 16:48:07.891', NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'node3', NULL, NULL, NULL, NULL, NULL);
INSERT INTO node (nodeid, dpname, nodecreatetime, nodeparentid, nodetype, nodesysoid, nodesysname, nodesysdescription, nodesyslocation, nodesyscontact, nodelabel, nodelabelsource, nodenetbiosname, nodedomainname, operatingsystem, lastcapsdpoll) VALUES (4, 'localhost', '2006-07-28 16:48:07.906', NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'node4', NULL, NULL, NULL, NULL, NULL);
INSERT INTO node (nodeid, dpname, nodecreatetime, nodeparentid, nodetype, nodesysoid, nodesysname, nodesysdescription, nodesyslocation, nodesyscontact, nodelabel, nodelabelsource, nodenetbiosname, nodedomainname, operatingsystem, lastcapsdpoll) VALUES (5, 'localhost', '2006-07-28 16:48:07.925', NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'alternate-node1', NULL, NULL, NULL, NULL, NULL);
INSERT INTO node (nodeid, dpname, nodecreatetime, nodeparentid, nodetype, nodesysoid, nodesysname, nodesysdescription, nodesyslocation, nodesyscontact, nodelabel, nodelabelsource, nodenetbiosname, nodedomainname, operatingsystem, lastcapsdpoll) VALUES (6, 'localhost', '2006-07-28 16:48:07.942', NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'alternate-node2', NULL, NULL, NULL, NULL, NULL);


--
-- Data for Name: notifications; Type: TABLE DATA; Schema: public; Owner: opennms
--



--
-- Data for Name: outages; Type: TABLE DATA; Schema: public; Owner: opennms
--

INSERT INTO outages (outageid, svclosteventid, svcregainedeventid, nodeid, ipaddr, serviceid, iflostservice, ifregainedservice, suppresstime, suppressedby) VALUES (1, 1, 1, 1, '192.168.1.1', 2, '2006-07-28 16:48:07.967', '2006-07-28 16:48:07.967', NULL, NULL);
INSERT INTO outages (outageid, svclosteventid, svcregainedeventid, nodeid, ipaddr, serviceid, iflostservice, ifregainedservice, suppresstime, suppressedby) VALUES (2, 1, NULL, 1, '192.168.1.1', 2, '2006-07-28 16:48:07.973', NULL, NULL, NULL);


--
-- Data for Name: pathoutage; Type: TABLE DATA; Schema: public; Owner: opennms
--



--
-- Data for Name: pollresults; Type: TABLE DATA; Schema: public; Owner: opennms
--



--
-- Data for Name: service; Type: TABLE DATA; Schema: public; Owner: opennms
--

INSERT INTO service (serviceid, servicename) VALUES (1, 'ICMP');
INSERT INTO service (serviceid, servicename) VALUES (2, 'SNMP');
INSERT INTO service (serviceid, servicename) VALUES (3, 'HTTP');


--
-- Data for Name: snmpinterface; Type: TABLE DATA; Schema: public; Owner: opennms
--

INSERT INTO snmpinterface (nodeid, ipaddr, snmpphysaddr, snmpifindex, snmpifdescr, snmpiftype, snmpifname, snmpifspeed, snmpifadminstatus, snmpifoperstatus, snmpifalias) VALUES (1, '192.168.1.1', NULL, 1, NULL, NULL, NULL, 10000000, NULL, NULL, NULL);
INSERT INTO snmpinterface (nodeid, ipaddr, snmpphysaddr, snmpifindex, snmpifdescr, snmpiftype, snmpifname, snmpifspeed, snmpifadminstatus, snmpifoperstatus, snmpifalias) VALUES (1, '192.168.1.2', NULL, 2, NULL, NULL, NULL, 10000000, NULL, NULL, NULL);
INSERT INTO snmpinterface (nodeid, ipaddr, snmpphysaddr, snmpifindex, snmpifdescr, snmpiftype, snmpifname, snmpifspeed, snmpifadminstatus, snmpifoperstatus, snmpifalias) VALUES (1, '192.168.1.3', NULL, 3, NULL, NULL, NULL, 10000000, NULL, NULL, NULL);


--
-- Data for Name: stpinterface; Type: TABLE DATA; Schema: public; Owner: opennms
--



--
-- Data for Name: stpnode; Type: TABLE DATA; Schema: public; Owner: opennms
--



--
-- Data for Name: usersnotified; Type: TABLE DATA; Schema: public; Owner: opennms
--



--
-- PostgreSQL database dump complete
--

