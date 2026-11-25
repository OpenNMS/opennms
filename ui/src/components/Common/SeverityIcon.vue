<!--
  An alarm severity as an icon on a colored circle: Critical a diamond, Major and Minor a circle,
  Warning a triangle (each with a "!"), Normal an "i", and any other severity a "?". The colors are
  the --onms-severity-<key>-bg/-fg theme tokens: Indeterminate's "?" is blue, any other "?" grey.

  The icon is an image with a name ("Major severity"), as it is often the only sign of the
  severity; `label` names it otherwise.
-->
<template>
  <span
    class="severity-icon"
    :class="[`severity-icon--${key}`, `severity-icon--${size}`]"
    role="img"
    :aria-label="label ?? `${severityName(severity)} severity`"
  >
    <OnmsIcon :icon="ICONS[key]" />
  </span>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { OnmsIcon } from '@opennms/onms-ui'
import Critical from '@opennms/onms-ui/icons/severity/Critical.vue'
import Major from '@opennms/onms-ui/icons/severity/Major.vue'
import Minor from '@opennms/onms-ui/icons/severity/Minor.vue'
import None from '@opennms/onms-ui/icons/severity/None.vue'
import Normal from '@opennms/onms-ui/icons/severity/Normal.vue'
import Warning from '@opennms/onms-ui/icons/severity/Warning.vue'
import { severityKey, SeverityKey, severityName } from '@/lib/severity'

const ICONS: Record<SeverityKey, object> = {
  critical: Critical,
  major: Major,
  minor: Minor,
  warning: Warning,
  normal: Normal,
  indeterminate: None,
  none: None
}

const props = withDefaults(defineProps<{
  // An OnmsSeverity name, in any case.
  severity?: string | null
  size?: 'small' | 'medium' | 'large'
  label?: string
}>(), {
  severity: undefined,
  size: 'medium',
  label: undefined
})

const key = computed<SeverityKey>(() => severityKey(props.severity))
</script>

<style lang="scss" scoped>
.severity-icon {
  display: inline-flex;
  flex: none;
  align-items: center;
  justify-content: center;
  width: var(--severity-icon-size);
  height: var(--severity-icon-size);
  border-radius: 50%;
  vertical-align: middle;

  // OnmsIcon is 1em square. The sizes are in pixels, and each leaves a whole number of pixels on
  // every side of the icon: a fraction is rounded to the pixel grid, which puts the icon off center.
  :deep(.onms-icon) {
    font-size: var(--severity-icon-glyph);
    vertical-align: 0;
  }
}

.severity-icon--small {
  --severity-icon-size: 20px;
  --severity-icon-glyph: 18px;
}

.severity-icon--medium {
  --severity-icon-size: 24px;
  --severity-icon-glyph: 20px;
}

.severity-icon--large {
  --severity-icon-size: 32px;
  --severity-icon-glyph: 28px;
}

@each $key in critical, major, minor, warning, normal, indeterminate, none {
  .severity-icon--#{$key} {
    background: var(--onms-severity-#{$key}-bg);
    color: var(--onms-severity-#{$key}-fg);
  }
}
</style>
