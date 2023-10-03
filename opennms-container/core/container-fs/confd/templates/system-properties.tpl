#
# DON'T EDIT THIS FILE :: GENERATED WITH CONFD
#
{{range $custom := gets "/opennms/custom/*"}}{{base .Key}}={{.Value}}{{end}}
