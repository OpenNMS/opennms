#
# DON'T EDIT THIS FILE :: GENERATED WITH CONFD
#
{{if exists "/opennms/instance_id"}}
org.opennms.instance.id={{getv "/opennms/instance_id"}}{{end}}
{{$ipc_servers := getvs "/opennms/kafka/ipc/bootstrap_servers/*"}}
{{$rpc_servers := getvs "/opennms/kafka/rpc/bootstrap_servers/*"}}
{{$sink_servers := getvs "/opennms/kafka/sink/bootstrap_servers/*"}}
{{$twin_servers := getvs "/opennms/kafka/twin/bootstrap_servers/*"}}
{{$strategy := false}}
{{if $ipc_servers }}{{$strategy = true}}
org.opennms.core.ipc.kafka.bootstrap.servers={{join $ipc_servers ","}}{{end}}
{{range $custom := gets "/opennms/kafka/ipc/config/*"}}org.opennms.core.ipc.{{base .Key}}={{.Value}}{{end}}
{{if $rpc_servers }}{{$strategy = true}}
org.opennms.core.ipc.rpc.kafka.bootstrap.servers={{join $rpc_servers ","}}{{end}}
{{range $custom := gets "/opennms/kafka/rpc/config/*"}}org.opennms.core.ipc.rpc.{{base .Key}}={{.Value}}{{end}}
{{if $sink_servers }}{{$strategy = true}}
org.opennms.core.ipc.sink.kafka.bootstrap.servers={{join $sink_servers ","}}{{end}}
{{range $custom := gets "/opennms/kafka/sink/config/*"}}org.opennms.core.ipc.sink.{{base .Key}}={{.Value}}{{end}}
{{if $twin_servers }}{{$strategy = true}}
org.opennms.core.ipc.twin.kafka.bootstrap.servers={{join $twin_servers ","}}{{end}}
{{range $custom := gets "/opennms/kafka/twin/config/*"}}org.opennms.core.ipc.twin.{{base .Key}}={{.Value}}{{end}}
{{if $strategy}}
org.opennms.core.ipc.strategy=kafka
org.opennms.activemq.broker.disable=true
{{end}}
