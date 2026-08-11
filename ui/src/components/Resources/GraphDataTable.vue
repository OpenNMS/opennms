<template>
  <div id="wrap">
    <div class="raw-checkbox">
      <OnmsCheckbox
        :inputId="`${id}-raw-values`"
        :modelValue="displayRawValues"
        @update:modelValue="valueDisplayHandler"
      />
      <label :for="`${id}-raw-values`">Raw values</label>
    </div>
    <table
      summary="Graph values"
      :id="`${id}-table`"
      @dblclick="highlightTableText"
    >
      <thead>
        <tr>
          <th
            class="time-column"
            scope="col"
          >
            Date/Time
          </th>
          <th
            v-for="metric of convertedGraphData.metrics"
            :key="metric.name"
            scope="col"
          >
            {{ getHeaderFromMetricName(metric.name as string) }}
          </th>
        </tr>
      </thead>
      <tbody>
        <tr
          v-for="index in rowIndices"
          :key="graphData.timestamps[index]"
        >
          <td class="time-cell">{{ displayRawValues ? graphData.timestamps[index] : displayTime(index) }}</td>
          <td
            v-for="metric of convertedGraphData.metrics"
            :key="metric.name"
          >
            {{
              !displayRawValues ?
                formatColumnValue(getColumnFromMetricName(metric.name as string)[index]) :
                getColumnFromMetricName(metric.name as string)[index]
            }}
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<script
  setup
  lang="ts"
>
import { ConvertedGraphData, GraphMetricsResponse } from '@/types'
import { OnmsCheckbox } from '@opennms/onms-ui'
import { format } from 'd3'
import { format as formatDate } from 'date-fns'
import { computed, PropType, ref } from 'vue'

/**
 * Full local date and time for a table row, matching what the legacy graph view
 * shows — Backshift renders its Date/Time column with d3's `%c`, which in the
 * default locale is exactly this: `Mon Aug 10 11:54:25 2026`.
 *
 * Deliberately NOT `graphData.formattedTimestamps`. Those are AXIS labels, chosen
 * for the granularity of the range, and they are wrong on a table: a week-long
 * range renders as `10/Aug 11:54` with no year, and a year-long one as `Aug/2026`
 * with no time at all, so rows stop being self-describing and can repeat.
 */
const DATETIME_FORMAT = 'EEE MMM d HH:mm:ss yyyy'

const displayRawValues = ref(false)
const d3format = format('.3s')
const formatColumnValue = (num: number) => {
  if (isNaN(num)) {
    return 'N/A'
  }
  return d3format(num)
}

const props = defineProps({
  graphData: {
    required: true,
    type: Object as PropType<GraphMetricsResponse>
  },
  convertedGraphData: {
    required: true,
    type: Object as PropType<ConvertedGraphData>
  },
  id: {
    required: true,
    type: String
  }
})

/** Timestamps arrive as epoch milliseconds. */
const displayTime = (index: number): string => {
  const timestamp = props.graphData.timestamps[index]
  return Number.isFinite(timestamp) ? formatDate(new Date(timestamp), DATETIME_FORMAT) : ''
}

/**
 * Row order as indices into the response arrays: most recent sample first.
 *
 * The measurements API returns timestamps ascending, but every graph data table in
 * OpenNMS reads newest-first — the legacy view walks its rows backwards
 * (`for (i = N-1; i >= 0; i--)` in jquery.flot.datatable), and this matches it.
 *
 * Reversing means reversing the INDICES, not the data. Timestamps and each metric's
 * values are separate arrays indexed in step, so mapping the values into a new
 * order instead would pair every timestamp with the wrong reading.
 */
const rowIndices = computed<number[]>(() =>
  props.graphData.timestamps.map((_timestamp, index) => index).reverse())

const getHeaderFromMetricName = (metricName: string): string => {
  for (const statement of props.convertedGraphData.printStatements) {
    if (statement.metric === metricName) {
      return statement.header as string
    }
  }
  return ''
}

const getColumnFromMetricName = (metricName: string): number[] => {
  for (const [index, label] of props.graphData.labels.entries()) {
    if (label === metricName) {
      return props.graphData.columns[index].values
    }
  }
  return []
}

const valueDisplayHandler = () => displayRawValues.value = !displayRawValues.value

const highlightTableText = () => {
  const table = document.getElementById(`${props.id}-table`)

  if (table) {
    const selection = window.getSelection()
    const range = document.createRange()
    range.selectNodeContents(table)
    if (selection) {
      selection.removeAllRanges()
      selection.addRange(range)
    }
  }
}
</script>

<style
  scoped
  lang="scss"
>
@import "@/styles/onms-table";
#wrap {
  height: calc(100% - 29px);
  overflow: auto;

  table {
    @include onms-table();
    &.condensed {
      @include onms-table-condensed();
    }
    margin-top: 0px;

    // Wide enough for the full date and time without wrapping.
    .time-column {
      width: 15rem;
    }

    .time-cell {
      white-space: nowrap;
    }
  }

  .raw-checkbox {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    margin: 10px 0px -4px 18px;

    label {
      cursor: pointer;
    }
  }
}
</style>
