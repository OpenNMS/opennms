<!--
  The availability timeline table.

  A native table with a fixed layout and a shared <colgroup> is what keeps the time axis in the
  header and the strip in every row on one x-origin and one width, across every interface group,
  without measuring anything or duplicating a grid definition.
-->
<template>
  <table
    class="availability-timeline"
    :aria-label="`Availability by service, ${rangeLabel}`"
    data-test="availability-timeline"
  >
    <colgroup>
      <col class="col-label">
      <col>
      <col class="col-pct">
    </colgroup>

    <thead>
      <tr>
        <th
          scope="col"
          class="col-label"
        >
          Service
        </th>
        <th scope="col">
          <div
            class="axis"
            data-test="timeline-axis"
          >
            <span
              v-for="tick of ticks"
              :key="tick.epoch"
              class="axis__tick"
              :class="tickEdgeClass(tick)"
              :style="{ left: `${tick.pct}%` }"
            >{{ tick.label }}</span>
          </div>
        </th>
        <th
          scope="col"
          class="col-pct"
        >
          Availability
        </th>
      </tr>
    </thead>

    <tbody
      v-for="group of model.interfaces"
      :key="group.ipInterfaceId"
    >
      <tr class="interface-row">
        <th
          scope="rowgroup"
          class="col-label"
        >
          <a
            :href="interfaceLink(baseHref, nodeId, group.ipAddress)"
            :title="group.ipAddress"
          >{{ group.ipAddress }}</a>
        </th>
        <!-- No strip: the axis above spans this column for every row beneath it. -->
        <td />
        <td class="col-pct">
          {{ interfaceAvailabilityText(group) }}
        </td>
      </tr>

      <AvailabilityTimelineRow
        v-for="row of group.services"
        :key="row.ifServiceId"
        :row="row"
        :ticks="ticks"
        :base-href="baseHref"
        :node-id="nodeId"
        :unmonitored-pct="model.unmonitoredPct"
      />
    </tbody>
  </table>
</template>

<script setup lang="ts">
import AvailabilityTimelineRow from './AvailabilityTimelineRow.vue'
import { interfaceLink } from '@/lib/linkUtils'
import { interfaceAvailabilityText, TimelineModel } from './availabilityTimelineModel'
import { tickEdgeClass, TimelineTick } from './availabilityTimelineAxis'

defineProps<{
  model: TimelineModel
  ticks: TimelineTick[]
  baseHref: string
  nodeId: string | number
  /** Names the window in the table's accessible name, e.g. 'Last day'. */
  rangeLabel: string
}>()
</script>

<style lang="scss">
@use '@/styles/onms-tokens' as variables;

// Not scoped: the strip geometry and colour tokens are declared here and consumed by
// AvailabilityTimelineRow, which is a child <tr> of this table.
.availability-timeline {
  width: 100%;
  // Honours the colgroup exactly, so a long IPv6 address cannot squeeze the strip column.
  table-layout: fixed;
  border-collapse: collapse;
  font-variant-numeric: tabular-nums;

  // One place owns the strip geometry, so the axis, the gridlines and the bars cannot drift apart.
  --timeline-strip-height: 1rem;
  --timeline-radius: 2px;
  // The interpolation is required. Sass treats a declaration whose name starts with -- as a custom
  // property and leaves its value as plain CSS, so `var(variables.$success)` would be emitted
  // verbatim -- a name Sass never resolves and the browser cannot match.
  //
  // The three fills and the gridline come from theme-independent tokens, so a strip looks the same
  // in both themes. The axis label is ordinary text on the card, so it stays themed.
  --timeline-up: var(#{variables.$timeline-available});
  --timeline-down: var(#{variables.$timeline-outage});
  --timeline-unmonitored: var(#{variables.$timeline-unmonitored});
  --timeline-gridline: var(#{variables.$timeline-gridline});
  --timeline-edge: var(#{variables.$timeline-edge});
  --timeline-axis-text: var(#{variables.$secondary-text-on-surface});

  th,
  td {
    padding: 0.25rem 0.5rem;
    vertical-align: middle;
  }

  .col-label {
    width: 10rem;
    text-align: left;
  }

  .col-pct {
    // Wide enough for 'Not Monitored' on one line: wrapping it made those rows taller than the
    // rest and broke the alignment of the strips down the panel.
    width: 7rem;
    text-align: right;
    white-space: nowrap;
  }

  thead th {
    font-weight: normal;
    font-size: 0.75rem;
    color: var(--timeline-axis-text);
  }

  .interface-row > th {
    font-weight: 600;
    overflow: hidden;

    a {
      display: block;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  }
}

.axis {
  position: relative;
  height: 1.25rem;
}

.axis__tick {
  position: absolute;
  bottom: 0;
  transform: translateX(-50%);
  white-space: nowrap;
  font-size: 0.75rem;
  color: var(--timeline-axis-text);

  // Centred on its tick, so the outermost labels would hang outside the column. Anchor them in
  // rather than let them be clipped.
  &--first {
    transform: none;
  }

  &--last {
    transform: translateX(-100%);
  }
}
</style>
