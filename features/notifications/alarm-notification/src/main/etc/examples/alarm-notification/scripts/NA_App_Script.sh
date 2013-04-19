echo "Script NAApplication started"
xml=$1
echo "Alarm xml being sent is " $xml
soapXml="<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:alar=\"http://alarms.sample\">   <soapenv:Header/>   <soapenv:Body>      <alar:AlarmXml>         <alar:alarmXml>              		<![CDATA[$xml]]>                  </alar:alarmXml>      </alar:AlarmXml>   </soapenv:Body></soapenv:Envelope>"

curl -H "SOAPAction: urn:alarmXml" -H "Content-Type:text/xml" -d "$soapXml" "http://localhost:8080/NAApplication/services/Alarms"
