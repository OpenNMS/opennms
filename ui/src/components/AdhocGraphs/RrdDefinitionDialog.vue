<template>
  <OnmsDialog
    :visible="visible"
    header="Graph definition"
    width="60rem"
    @update:visible="value => !value && emit('close')"
  >
    <div
      v-if="!definition"
      class="definition-blocked"
      data-test="rrd-definition-blocked"
    >
      <p class="blocked-reason">{{ reason }}</p>
      <p class="blocked-help">
        A prefab graph definition is a template bound to one resource: the
        <code>{{ '{rrd1}' }}</code>&hellip;<code>{{ '{rrdN}' }}</code> placeholders are all
        resolved against the resource the graph is rendered for. Ad-hoc graphs have no
        such restriction, so only those drawing on a single resource can be expressed
        this way.
      </p>
    </div>

    <div
      v-else
      class="definition-body"
    >
      <p class="definition-intro">
        Save this as a new <code>.properties</code> file under
        <code>$OPENNMS_HOME/etc/snmp-graph.properties.d/</code> and reload the graph
        definitions &mdash; the <code>reports=</code> line is included, so it works as
        it stands. It will then appear for every <code>{{ definition.type }}</code>
        resource. To add it to an existing file instead, follow the note in the block.
      </p>
      <pre
        class="definition-text"
        data-test="rrd-definition-text"
      >{{ definition.properties }}</pre>
      <p class="definition-note">
        The time range is not part of a definition &mdash; the viewer chooses it.
        Consolidation, colors, styles and expressions are carried over as-is.
      </p>
    </div>

    <template #footer>
      <OnmsButton
        v-if="definition"
        variant="outlined"
        data-test="rrd-definition-copy"
        @click="copy"
      >Copy definition</OnmsButton>
      <OnmsButton
        variant="ghost"
        data-test="rrd-definition-close"
        @click="emit('close')"
      >Close</OnmsButton>
    </template>
  </OnmsDialog>
</template>

<script setup lang="ts">
import { OnmsButton, OnmsDialog } from '@opennms/onms-ui'
import { computed } from 'vue'

import { copyToClipboard } from '@/composables/useClipboard'
import useSnackbar from '@/composables/useSnackbar'
import { AdhocGraphConfig } from '@/types/adhocGraph'
import {
  buildRrdGraphDefinition,
  isRrdGraphDefinition,
  RrdGraphDefinition
} from './utils/rrdGraphDefinition'

const props = defineProps<{
  visible: boolean
  config: AdhocGraphConfig
}>()

const emit = defineEmits<{ close: [] }>()

const { showSnackBar } = useSnackbar()

const result = computed(() => buildRrdGraphDefinition(props.config))

const definition = computed<RrdGraphDefinition | null>(() =>
  (isRrdGraphDefinition(result.value) ? result.value : null))

const reason = computed<string>(() =>
  (isRrdGraphDefinition(result.value) ? '' : result.value.reason))

const copy = async () => {
  if (!definition.value) {
    return
  }

  // Synchronously, while the click's user activation still stands.
  const copied = copyToClipboard(definition.value.properties)

  try {
    await copied
    showSnackBar({ msg: 'Graph definition copied to the clipboard.' })
  } catch (_err) {
    showSnackBar({ msg: 'Could not copy automatically — select the text and copy it.', error: true })
  }
}
</script>

<style scoped lang="scss">
@import '@/styles/onms-typography';

.definition-intro,
.definition-note,
.blocked-help {
  @include onms-body-small;
  color: var(--p-text-muted-color);
}

.blocked-reason {
  color: var(--p-text-color);
}

.definition-text {
  max-height: 26rem;
  overflow: auto;
  padding: 0.75rem;
  border: 1px solid var(--p-content-border-color);
  border-radius: var(--p-content-border-radius);
  background: var(--p-content-background);
  font-family: monospace;
  font-size: 0.8125rem;
  // The command is one long value broken over continuation lines; keep those
  // breaks and let anything wider scroll rather than wrap mid-token.
  white-space: pre;
}
</style>
