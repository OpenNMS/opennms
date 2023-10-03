#
# DON'T EDIT THIS FILE :: GENERATED WITH CONFD
#
{{if exists "/opennms/instance_id"}}
org.opennms.instance.id={{getv "/opennms/instance_id"}}{{end}}
{{$ipc_servers := getvs "/ipc/kafka/bootstrap.servers/*"}}{{$strategy := false}}
{{$rpc_servers := getvs "/ipc/rpc/kafka/bootstrap.servers/*"}}
{{$sink_servers := getvs "/ipc/sink/kafka/bootstrap.servers/*"}}
{{$twin_servers := getvs "/ipc/twin/kafka/bootstrap.servers/*"}}
{{if $ipc_servers }}{{$strategy = true}}org.opennms.core.ipc.kafka.bootstrap.servers={{join $ipc_servers ","}}{{end}}
{{if $rpc_servers }}{{$strategy = true}}org.opennms.core.ipc.rpc.kafka.bootstrap.servers={{join $rpc_servers ","}}{{end}}
{{if $sink_servers }}{{$strategy = true}}org.opennms.core.ipc.sink.kafka.bootstrap.servers={{join $sink_servers ","}}{{end}}
{{if $twin_servers }}{{$strategy = true}}org.opennms.core.ipc.twin.kafka.bootstrap.servers={{join $twin_servers ","}}{{end}}
{{if exists "/ipc/kafka/bootstrap.servers"}}{{$strategy = true}}{{end}}
{{if exists "/ipc/rpc/kafka/bootstrap.servers"}}{{$strategy = true}}{{end}}
{{if exists "/ipc/sink/kafka/bootstrap.servers"}}{{$strategy = true}}{{end}}
{{if exists "/ipc/twin/kafka/bootstrap.servers"}}{{$strategy = true}}{{end}}
{{if exists "/ipc/sink/initialSleepTime"}}org.opennms.core.ipc.sink.initialSleepTime={{getv "/ipc/sink/initialSleepTime"}}{{end}}
{{range $custom := gets "/ipc/kafka/*"}}
org.opennms.core.ipc.kafka.{{base .Key}}={{.Value}}{{end}}
{{range $custom := gets "/ipc/rpc/kafka/*"}}
org.opennms.core.ipc.rpc.kafka.{{base .Key}}={{.Value}}{{end}}
{{range $custom := gets "/ipc/sink/kafka/*"}}
org.opennms.core.ipc.sink.kafka.{{base .Key}}={{.Value}}{{end}}
{{range $custom := gets "/ipc/twin/kafka/*"}}
org.opennms.core.ipc.twin.kafka.{{base .Key}}={{.Value}}{{end}}
{{if $strategy}}
org.opennms.core.ipc.strategy=kafka
org.opennms.activemq.broker.disable=true
{{end}}
