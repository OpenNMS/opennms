DELETE FROM ifservices;
DELETE FROM ipinterface;
DELETE FROM snmpinterface;
DELETE FROM outages;
DELETE FROM node;
DELETE FROM notifications;
DELETE FROM usersNotified;
DELETE FROM events;
DELETE FROM service;

\i service.sql
\i node.sql
\i ipinterface.sql
\i ifservices.sql
