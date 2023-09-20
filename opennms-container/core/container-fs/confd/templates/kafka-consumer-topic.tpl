#
# DON'T EDIT THIS FILE :: GENERATED WITH CONFD
#
{{if exists "/opennms/kafka/consumer/topic" }}
eventsTopic={{getv "/opennms/kafka/consumer/topic"}}
{{end}}
