echo "TCA alarm notification script started "
xml=$1
echo "Alarm xml being sent is " $xml
curl -v -u super:juniper123 -X POST -H "Content-type:application/xml" -d "$xml" "http://localhost:8080/serviceui/resteasy/tc-alarms"
