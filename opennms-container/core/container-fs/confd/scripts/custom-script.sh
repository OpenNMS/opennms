#!/bin/bash

source /usr/share/opennms/etc/confd.custom.cfg

if [[ ${ENABLE_SYSLOGD} == "true" ]]; then
  echo "Enabling Syslogd..."
  sed -i 'N;s/service.*\n\(.*Syslogd\)/service enabled="true">\n\1/;P;D' /usr/share/opennms/etc/service-configuration.xml
else
  echo "Disabling Syslogd..."
  sed -i 'N;s/service.*\n\(.*Syslogd\)/service enabled="false">\n\1/;P;D' /usr/share/opennms/etc/service-configuration.xml
fi


if [[ ${ENABLE_SNMPPOLLER} == "true" ]]; then
  echo "Enabling SNMP Interface Poller..."
  sed -i 'N;s/service.*\n\(.*SnmpPoller\)/service enabled="true">\n\1/;P;D' /usr/share/opennms/etc/service-configuration.xml
else
  echo "Disabling SNMP Interface Poller..."
  sed -i 'N;s/service.*\n\(.*SnmpPoller\)/service enabled="false">\n\1/;P;D' /usr/share/opennms/etc/service-configuration.xml
fi

if [[ ${ENABLE_TELEMETRYD} == "true" ]]; then
  echo "Enabling Telemetryd..."
  sed -i 'N;s/service.*\n\(.*Telemetryd\)/service enabled="true">\n\1/;P;D' /usr/share/opennms/etc/service-configuration.xml
else
  echo "Disabling Telemetryd..."
  sed -i 'N;s/service.*\n\(.*Telemetryd\)/service enabled="false">\n\1/;P;D' /usr/share/opennms/etc/service-configuration.xml
fi

if [[ ${ENABLE_CORRELATOR} == "true" ]]; then
  echo "Enabling Event Correlator..."
  sed -i 'N;s/service.*\n\(.*Correlator\)/service enabled="true">\n\1/;P;D' /usr/share/opennms/etc/service-configuration.xml
else
  echo "Disabling Event Correlator..."
  sed -i 'N;s/service.*\n\(.*Correlator\)/service enabled="false">\n\1/;P;D' /usr/share/opennms/etc/service-configuration.xml
fi

if [[ ${ENABLE_TICKETER} == "true" ]]; then
  echo "Enabling Ticketer..."
  sed -i 'N;s/service.*\n\(.*Ticketer\)/service enabled="true">\n\1/;P;D' /usr/share/opennms/etc/service-configuration.xml
else
  echo "Disabling Ticketer..."
  sed -i 'N;s/service.*\n\(.*Ticketer\)/service enabled="false">\n\1/;P;D' /usr/share/opennms/etc/service-configuration.xml
fi

if [[ ${ENABLE_TRAPD} == "true" ]]; then
  echo "Enabling Trapd..."
  sed -i 'N;s/service.*\n\(.*Trapd\)/service enabled="true">\n\1/;P;D' /usr/share/opennms/etc/service-configuration.xml
else
  echo "Disabling Trapd..."
  sed -i 'N;s/service.*\n\(.*Trapd\)/service enabled="false">\n\1/;P;D' /usr/share/opennms/etc/service-configuration.xml
fi
