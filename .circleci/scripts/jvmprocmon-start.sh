#!/bin/bash
TARGET="/tmp/jvmprocmon"

echo "Starting JVMProcMon writing to: ${TARGET}"
mkdir -p "$TARGET"

dump () {
  echo "$(date): Collecting details for JVM with PID $1 ..."
  cmdline=$(cat "/proc/$1/cmdline")
  timestamp_ms=$(date +%s%N | cut -b1-13)
  target="${TARGET}/$1/$timestamp_ms"
  mkdir -p "${target}"
  echo "${cmdline}" > "${target}/cmdline"
  jstack "$1" >> "${target}/jstack" 2>&1
  jmap -histo "$1" >> "${target}/jmap_histo" 2>&1
  echo "$(date): Done collection JVM details."
}

dump_all() {
  echo "$(date): Enumerating & collecting JVM details..."
  for PID in $(sudo pgrep --ns 1 java 2>/dev/null || pgrep --ns 1 java)
  do
    dump "$PID"
  done
  echo "$(date): Enumeration complete."
}

# Now and every 5 minutes capture a thread dump of all the JVMs
# in the primary namespace (excluding those in Docker containers)
while true
do 
    dump_all
    sleep 300
done
