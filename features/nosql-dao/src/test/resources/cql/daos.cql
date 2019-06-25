/*
CREATE KEYSPACE opennms
  WITH REPLICATION = {
   'class' : 'SimpleStrategy',
   'replication_factor' : 1
  };
*/

CREATE TABLE alarms (id uuid PRIMARY KEY,
  eventuei varchar,
  nodeid int,
  ipaddr varchar,
  serviceid int,
  reductionkey varchar,
  type int,
  counter int,
  severity int,
  lasteventid int,
  firsteventtime timestamp,
  lasteventtime timestamp,
  firstautomationtime timestamp,
  lastautomationtime timestamp,
  description varchar,
  logmsg varchar,
  operinstruct varchar,
  tticketid varchar,
  tticketstate int,
  mouseovertext varchar,
  suppresseduntil timestamp,
  suppresseduser varchar,
  suppressedtime timestamp,
  alarmackuser varchar,
  alarmacktime timestamp,
  managedobjectinstance varchar,
  managedobjecttype varchar,
  applicationdn varchar,
  ossprimarykey varchar,
  x733alarmtype varchar,
  x733probablecause int,
  qosalarmstate varchar,
  clearkey varchar,
  ifindex int,
  stickymemo int,
  systemid varchar);
