<template>
  <div class="chart-toolbar">
    <div class="toolbar-row">
      <TimeControls @updateTime="(value: StartEndTime) => emit('updateTime', value)" />

      <div class="toolbar-actions">
        <OnmsButton
          variant="outlined"
          :disabled="!canQuery"
          :loading="loading"
          data-test="toolbar-refresh"
          @click="emit('refresh')"
        >Refresh</OnmsButton>
        <OnmsIconButton
          variant="outlined"
          :icon="DownloadFile"
          title="Download this graph's data as CSV"
          :disabled="!hasData"
          data-test="toolbar-csv"
          @click="emit('exportCsv')"
        />
        <OnmsIconButton
          variant="outlined"
          :icon="PdfIcon"
          title="Download this graph as a PDF"
          :disabled="!hasData"
          data-test="toolbar-pdf"
          @click="emit('exportPdf')"
        />
        <OnmsIconButton
          variant="outlined"
          :icon="LinkIcon"
          title="Copy a link to this graph"
          :disabled="!canQuery"
          data-test="toolbar-share"
          @click="emit('share')"
        />
        <OnmsIconButton
          v-if="!viewOnly"
          variant="outlined"
          :icon="CodeIcon"
          title="Show this graph as an RRDtool graph definition"
          :disabled="!canQuery"
          data-test="toolbar-definition"
          @click="emit('showDefinition')"
        />
        <OnmsIconButton
          v-if="!viewOnly"
          variant="outlined"
          :icon="expanded ? FullscreenExitIcon : FullscreenIcon"
          :title="expanded ? 'Show the selectors again' : 'Expand the graph, hiding the selectors'"
          data-test="toolbar-expand"
          @click="emit('toggleExpand')"
        />
        <OnmsIconButton
          v-if="!viewOnly"
          variant="outlined"
          :icon="PopOutIcon"
          title="Open this graph on its own, in a new tab"
          :disabled="!canQuery"
          data-test="toolbar-popout"
          @click="emit('popOut')"
        />
        <OnmsButton
          v-if="!viewOnly"
          variant="ghost"
          data-test="toolbar-clear"
          @click="emit('clear')"
        >Clear all</OnmsButton>
      </div>
    </div>

    <div
      v-if="!viewOnly && !expanded"
      class="toolbar-row toolbar-fields"
    >
      <FormField
        label="Graph title"
        for="adhoc-title"
        class="field-title"
      >
        <OnmsInputText
          id="adhoc-title"
          :modelValue="config.title"
          placeholder="Untitled ad-hoc graph"
          data-test="toolbar-title"
          @update:modelValue="value => emit('update', { title: (value ?? '') as string })"
        />
      </FormField>

      <FormField
        label="Vertical label"
        for="adhoc-vlabel"
        class="field-vlabel"
      >
        <OnmsInputText
          id="adhoc-vlabel"
          :modelValue="config.verticalLabel"
          placeholder="e.g. bits per second"
          data-test="toolbar-vlabel"
          @update:modelValue="value => emit('update', { verticalLabel: (value ?? '') as string })"
        />
      </FormField>

      <FormField
        label="Data points"
        for="adhoc-resolution"
        class="field-resolution"
        hint="Samples across the range."
      >
        <OnmsInputNumber
          inputId="adhoc-resolution"
          :modelValue="config.resolution"
          :min="10"
          :max="MAX_RESOLUTION"
          data-test="toolbar-resolution"
          @update:modelValue="value => emit('update', { resolution: Number(value) || DEFAULT_RESOLUTION })"
        />
      </FormField>

      <FormField
        label="Stack series"
        for="adhoc-stacked"
        class="field-stacked"
      >
        <OnmsToggleSwitch
          :modelValue="config.stacked"
          inputId="adhoc-stacked"
          data-test="toolbar-stacked"
          @update:modelValue="value => emit('update', { stacked: value })"
        />
      </FormField>
    </div>
  </div>
</template>

<script setup lang="ts">
import { OnmsButton, OnmsIconButton, OnmsInputNumber, OnmsInputText, OnmsToggleSwitch } from '@opennms/onms-ui'

import DownloadFile from '@/components/icons/action/DownloadFile.vue'
import CodeIcon from '@/components/icons/action/Code.vue'
import PopOutIcon from '@/components/icons/action/Expand.vue'
import FullscreenIcon from '@/components/icons/navigation/Fullscreen.vue'
import FullscreenExitIcon from '@/components/icons/navigation/FullscreenExit.vue'
import LinkIcon from '@/components/icons/action/Link.vue'
import PdfIcon from '@/components/icons/file/Pdf.vue'
import FormField from '@/components/Common/FormField.vue'
import TimeControls from '@/components/Resources/TimeControls.vue'
import { StartEndTime } from '@/types'
import { AdhocGraphConfig } from '@/types/adhocGraph'
import { DEFAULT_RESOLUTION, MAX_RESOLUTION } from './utils/adhocQuery'

withDefaults(defineProps<{
  config: AdhocGraphConfig
  canQuery: boolean
  hasData: boolean
  loading: boolean
  /** True while the graph is filling the page with the selectors hidden. */
  expanded?: boolean
  /** Graph-only route: no editing controls, no expand/pop-out of its own. */
  viewOnly?: boolean
}>(), {
  expanded: false,
  viewOnly: false
})

const emit = defineEmits<{
  update: [patch: Partial<AdhocGraphConfig>]
  updateTime: [time: StartEndTime]
  refresh: []
  clear: []
  toggleExpand: []
  popOut: []
  showDefinition: []
  share: []
  exportCsv: []
  exportPdf: []
}>()
</script>

<style scoped lang="scss">
.chart-toolbar {
  display: flex;
  flex-direction: column;
}

.toolbar-row {
  display: flex;
  flex-wrap: wrap;
  align-items: flex-start;
  justify-content: space-between;
  gap: 0.75rem;
}

.toolbar-actions {
  display: flex;
  align-items: flex-start;
  gap: 0.5rem;
}

.toolbar-fields {
  justify-content: flex-start;
  // flex-start, not flex-end: every field is a label above a control, so they line
  // up at the top. Aligning to the bottom made a field whose hint wrapped onto a
  // second line drag its neighbours down with it.
  align-items: flex-start;
  margin-bottom: 0.75rem;

  .field-title {
    flex: 1 1 18rem;
  }

  .field-vlabel {
    flex: 1 1 14rem;
  }

  // Wide enough that the hint sits on one line at this font size.
  .field-resolution {
    flex: 0 0 13rem;
  }

  .field-stacked {
    flex: 0 0 auto;
  }
}
</style>
