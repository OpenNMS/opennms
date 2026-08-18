<template>
  <div class="chart-toolbar">
    <div class="toolbar-row">
      <!--
        A visible label: the range button shows only the current selection ("Last
        day"), so without this there is nothing saying what it selects. Not wired up
        with aria-labelledby, because TimeControls' fallthrough target is a plain
        div and the attribute would be inert there — associating it properly means
        changing the shared component, which Resource Graphs needs too.
      -->
      <div class="time-range">
        <span class="time-range-label">Time Range:</span>
        <TimeControls
          data-test="toolbar-time-range"
          @updateTime="(value: StartEndTime) => emit('updateTime', value)"
        />
      </div>

      <div class="toolbar-actions">
        <OnmsIconButton
          variant="outlined"
          :icon="RefreshIcon"
          title="Refresh"
          v-onms-tooltip="'Redraw the graph with the latest data'"
          :disabled="!canQuery || loading"
          data-test="toolbar-refresh"
          @click="emit('refresh')"
        />
        <OnmsIconButton
          variant="outlined"
          :icon="DownloadFile"
          title="Download the graph data as CSV"
          v-onms-tooltip="'Download the graph data as CSV'"
          :disabled="!hasData"
          data-test="toolbar-csv"
          @click="emit('exportCsv')"
        />
        <OnmsIconButton
          variant="outlined"
          :icon="PdfIcon"
          title="Download this graph as a PDF"
          v-onms-tooltip="'Download this graph as a PDF'"
          :disabled="!hasData"
          data-test="toolbar-pdf"
          @click="emit('exportPdf')"
        />
        <OnmsIconButton
          variant="outlined"
          :icon="LinkIcon"
          title="Copy a link to this graph"
          v-onms-tooltip="'Copy a link to this graph'"
          :disabled="!canQuery"
          data-test="toolbar-share"
          @click="emit('share')"
        />
        <OnmsIconButton
          v-if="!viewOnly"
          variant="outlined"
          :icon="CodeIcon"
          title="Show this graph as an RRDtool graph definition"
          v-onms-tooltip="'Show this graph as an RRDtool graph definition'"
          :disabled="!canQuery"
          data-test="toolbar-definition"
          @click="emit('showDefinition')"
        />
        <OnmsIconButton
          v-if="!viewOnly"
          variant="outlined"
          :icon="expanded ? FullscreenExitIcon : FullscreenIcon"
          :title="expanded ? 'Show the selectors again' : 'Expand the graph, hiding the selectors'"
          v-onms-tooltip="expanded ? 'Show the selectors again' : 'Expand the graph, hiding the selectors'"
          data-test="toolbar-expand"
          @click="emit('toggleExpand')"
        />
        <OnmsIconButton
          v-if="!viewOnly"
          variant="outlined"
          :icon="PopOutIcon"
          title="Open this graph on its own, in a new tab"
          v-onms-tooltip="'Open this graph on its own, in a new tab'"
          :disabled="!canQuery"
          data-test="toolbar-popout"
          @click="emit('popOut')"
        />
        <OnmsIconButton
          v-if="!viewOnly"
          variant="outlined"
          :icon="CancelIcon"
          title="Clear all"
          v-onms-tooltip="'Clear every selection and start again'"
          data-test="toolbar-clear"
          @click="emit('clear')"
        />
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
          placeholder="Untitled custom performance graph"
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
import { OnmsIconButton, OnmsInputNumber, OnmsInputText, OnmsToggleSwitch } from '@opennms/onms-ui'

import CancelIcon from '@/components/icons/action/Cancel.vue'
import DownloadFile from '@/components/icons/action/DownloadFile.vue'
import RefreshIcon from '@/components/icons/navigation/Refresh.vue'
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

// Every control on this row is now the same height, so the label sits beside the
// range button rather than above it.
.time-range {
  display: flex;
  align-items: center;
  gap: 0.5rem;

  .time-range-label {
    font-weight: 700;
    white-space: nowrap;
  }
}

.toolbar-fields {
  justify-content: flex-start;
  // flex-start, not flex-end: every field is a label above a control, so they line
  // up at the top. Aligning to the bottom made a field whose hint wrapped onto a
  // second line drag its neighbors down with it.
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
