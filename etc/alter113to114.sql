DROP INDEX node_id_idx;

ALTER TABLE ipinterface DROP CONSTRAINT fk_nodeID1;
ALTER TABLE ipinterface ADD CONSTRAINT fk_nodeID1 FOREIGN KEY (nodeID) REFERENCES node (nodeID) ON DELETE CASCADE;

ALTER TABLE snmpinterface DROP CONSTRAINT fk_nodeID2;
ALTER TABLE snmpinterface ADD CONSTRAINT fk_nodeID2 FOREIGN KEY (nodeID) REFERENCES node (nodeID) ON DELETE CASCADE;

ALTER TABLE ifservices DROP CONSTRAINT fk_nodeID3;
ALTER TABLE ifservices ADD CONSTRAINT fk_nodeID3 FOREIGN KEY (nodeID) REFERENCES node (nodeID) ON DELETE CASCADE;
ALTER TABLE ifservices DROP CONSTRAINT fk_serviceID1;
ALTER TABLE ifservices ADD CONSTRAINT fk_serviceID1 FOREIGN KEY (serviceID) REFERENCES service (serviceID) ON DELETE CASCADE;
CREATE INDEX ifservices_nodeid_serviceid_idx ON ifservices(nodeID, serviceID);

DROP INDEX events_id_idx;
ALTER TABLE events ADD CONSTRAINT fk_nodeID6 FOREIGN KEY (nodeID) REFERENCES node (nodeID) ON DELETE CASCADE;

DROP INDEX outages_id_idx;
ALTER TABLE outages DROP CONSTRAINT fk_eventID1;
ALTER TABLE outages ADD CONSTRAINT fk_eventID1 FOREIGN KEY (svcLostEventID) REFERENCES events (eventID) ON DELETE CASCADE;
ALTER TABLE outages DROP CONSTRAINT fk_eventID2;
ALTER TABLE outages ADD CONSTRAINT fk_eventID2 FOREIGN KEY (svcRegainedEventID) REFERENCES events (eventID) ON DELETE CASCADE;
ALTER TABLE outages DROP CONSTRAINT fk_serviceID2;
ALTER TABLE outages ADD CONSTRAINT fk_serviceID2 FOREIGN KEY (serviceID) REFERENCES service (serviceID) ON DELETE CASCADE;

DROP INDEX vulnerabilities_id_idx;

DROP INDEX vulnplugins_plugin_idx;
ALTER TABLE vulnPlugins ADD CONSTRAINT pk_vulnplugins PRIMARY KEY (pluginID, pluginSubID);

DROP INDEX notifications_id_idx;
DROP INDEX notifications_nodeid_idx;
ALTER TABLE notifications ADD CONSTRAINT fk_nodeID7 FOREIGN KEY (nodeID) REFERENCES node (nodeID) ON DELETE CASCADE;
ALTER TABLE notifications DROP CONSTRAINT fk_eventID3;
ALTER TABLE notifications ADD CONSTRAINT fk_eventID3 FOREIGN KEY (eventID) REFERENCES events (eventID) ON DELETE CASCADE;

ALTER TABLE usersnotified ADD CONSTRAINT fk_notifID2 FOREIGN KEY (notifyID) REFERENCES notifications (notifyID);
--ALTER TABLE usersnotified ADD CONSTRAINT pk_usersNotified PRIMARY KEY (userID, notifyID);
CREATE INDEX userid_notifyid_idx ON usersNotified(userID, notifyID);