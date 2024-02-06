{{if exists "/opennms/rrd/rras"}}
{{$rras := getvs "/opennms/rrd/rras/*"}}
OPENNMS_RRAS="{{join $rras ";"}}"
{{end}}
