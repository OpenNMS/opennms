DELETE FROM ifServices WHERE nodeID = 7;
DELETE FROM ipInterface WHERE nodeID = 7;
DELETE FROM node WHERE nodeID = 7;

INSERT INTO node (nodeID, dpName, nodeCreateTime, nodeType) VALUES (7, 'localhost', '30-Aug-2001 12:45:00', 'A');
INSERT INTO ipInterface (nodeID, ipAddr, ipHostname, isManaged) VALUES (7, '192.168.0.41', '192.168.0.41', 'A');
INSERT INTO ifServices (nodeID, ipAddr, serviceID, status, source, notify) VALUES (7, '192.168.0.41', 3, 'A', 'P', 'N');
INSERT INTO ipInterface (nodeID, ipAddr, ipHostname, isManaged) VALUES (7, '192.168.0.42', '192.168.0.42', 'A');
INSERT INTO ifServices (nodeID, ipAddr, serviceID, status, source, notify) VALUES (7, '192.168.0.42', 3, 'A', 'P', 'N');

