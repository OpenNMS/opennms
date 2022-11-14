<template>
  <div id="wrap">
    <FeatherCheckbox
      :modelValue="displayRawValues"
      @update:modelValue="valueDisplayHandler"
      class="raw-checkbox"
      >Raw values</FeatherCheckbox
    >
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
          v-for="(timestamp, index) in graphData.timestamps"
          :key="timestamp"
        >
          <td>{{ !displayRawValues ? graphData.formattedTimestamps[index] : timestamp }}</td>
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
import { FeatherCheckbox } from '@featherds/checkbox'
import { format } from 'd3'
import { PropType } from 'vue'

const displayRawValues = ref(false)
const d3format = format('.3s')
const formatColumnValue = (num: number) => {
  if (isNaN(num)) return 'N/A'
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
@import "@featherds/table/scss/table";
#wrap {
  height: calc(100% - 29px);
  overflow: auto;

  table {
    @include table();
    &.condensed {
      @include table-condensed();
    }
    margin-top: 0px;

    .time-column {
      width: 200px;
    }
  }

  .raw-checkbox {
    margin: 10px 0px -4px 18px;
  }
}
</style>

