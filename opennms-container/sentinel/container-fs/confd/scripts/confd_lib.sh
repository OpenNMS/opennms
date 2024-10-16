SENTINEL_ETC_DIR="/opt/sentinel/etc"
STATIC_DIR="/opt/sentinel/confd/static"

# Checks if the given file is empty and if so deletes it.
removeIfEmpty() {
  local file="$1"

  if [ -z "$file" ]; then
    echo "No rendered template was specified"
    exit 1
  fi

  if [ -s "$file" ]; then
    return 1
  fi

  rm "$file"
  return 0
}
