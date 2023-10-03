#!/bin/bash

source /usr/share/opennms/etc/confd.timeseries.cfg

function update_rras {
  if grep -q "[<]rrd" $1; then
    echo "Processing $1"
    sed -i -r "/[<]rra/d" $1
    sed -i -r "/[<]rrd/a $2" $1
  fi
}

# Configure RRAs
if [[ -v OPENNMS_RRAS ]]; then
  echo "Configuring RRAs..."
  IFS=';' read -a RRAS <<< ${OPENNMS_RRAS}
  RRACFG=""
  for RRA in ${RRAS[@]}; do
    RRACFG+="<rra>${RRA}</rra>"
  done
  echo ${RRACFG}
  for XML in $(find /usr/share/opennms/etc -name *datacollection*.xml -or -name *datacollection*.d); do
    if [ -d $XML ]; then
      for XML in $(find ${XML} -name *.xml); do
        update_rras ${XML} ${RRACFG}
      done
    else
      update_rras ${XML} ${RRACFG}
    fi
  done
fi
