#!/usr/bin/env python

"""send-event.py: Send events to OpenNMS via the REST API."""

import datetime
from lxml import etree as ET
import urllib2
import base64

class EventParameter:
    """An OpenNMS parameter"""
    def __init__(self, name, value):
        self.name = name
        self.value = value

class Event:
    """An OpenNMS event"""
    def __init__(self, uei):
        self.uei = uei
        self.interface = None
        self.source = "python_send_event"
        self.host = "localhost"
        self.time = None
        self.parameters = []

    def add_parameter(self, name, value):
        self.parameters.append(EventParameter(name, value))
    
    def to_xml(self):
        # Create the Event XML
        root = ET.Element('event')

        # UEI
        ET.SubElement(root, "uei").text = self.uei

        # Source
        ET.SubElement(root, "source").text = self.source

        # Time, expected format: "Monday, February 18, 2002 3:01:58 PM EST"
        time = self.time if self.time is not None else datetime.datetime.utcnow()
        ET.SubElement(root, "time").text = time.strftime("%A, %B %d, %Y %I:%M:%S %p UTC")

        # Host
        ET.SubElement(root, "host").text = self.host

        # Parameters
        if len(self.parameters) > 0:
            parms_el = ET.SubElement(root, "parms")
            for parameter in self.parameters:
                parm_el = ET.SubElement(parms_el, "parm")
                ET.SubElement(parm_el, "parmName").text = ET.CDATA(parameter.name)
                ET.SubElement(parm_el, "value", type="string", encoding="text").text = ET.CDATA(parameter.value)

        return ET.tostring(root, pretty_print=True)

def send_event(event):
    r = urllib2.Request("http://localhost:8980/opennms/rest/events", data=event.to_xml(), headers={'Content-Type': 'application/xml'})
    b64 = base64.encodestring("{}:{}".format("admin", "admin")).rstrip()
    r.add_header("Authorization", "Basic {}".format(b64))
    u = urllib2.urlopen(r)
    response = u.read()

e = Event("uei.opennms.org/internal/discovery/newSuspect")
e.add_parameter("key", "value")
print "Sending event: {}".format(e.to_xml())
send_event(e)

