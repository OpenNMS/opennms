<!--
  One service row of the availability timeline: the service link, the strip, and the availability
  figure. A <tr> so the shared <colgroup> keeps the strip aligned with the axis in the header.
-->
<template>
  <tr data-test="timeline-service-row">
    <th
      scope="row"
      class="col-label"
    >
      <div class="service-label">
        <!-- Hidden from assistive technology: the link's name already says "down now". -->
        <SeverityIcon
          v-if="row.down"
          severity="critical"
          size="small"
          aria-hidden="true"
          data-test="service-down-icon"
        />
        <a
          :href="serviceHref"
          class="service-link"
          :class="{ 'service-link--down': row.down }"
          :title="serviceLinkLabel(row)"
          :aria-label="row.down ? serviceLinkLabel(row) : undefined"
          :data-down="row.down ? 'true' : undefined"
          data-test="timeline-service-link"
        >{{ row.serviceName }}</a>
      </div>
    </th>
    <td>
      <div
        class="strip"
        :class="{ 'strip--unmonitored': !monitored }"
        data-test="timeline-strip"
      >
        <span
          v-if="monitored && unmonitoredPct > 0"
          class="strip__unmonitored"
          aria-hidden="true"
          :style="{ width: `${unmonitoredPct}%` }"
        />
        <span
          v-for="tick of ticks"
          :key="tick.epoch"
          class="strip__gridline"
          aria-hidden="true"
          :style="{ left: `${tick.pct}%` }"
        />
        <a
          v-for="seg of row.segments"
          :key="seg.outageId"
          v-onms-tooltip.top="segmentDescription(row, seg)"
          class="strip__outage"
          :class="{
            'strip__outage--open': seg.openAtWindowEnd,
            'strip__outage--clipped': seg.startsBeforeWindow
          }"
          :style="{ left: `${seg.startPct}%`, width: `${seg.widthPct}%` }"
          :href="outageDetailLink(baseHref, seg.outageId)"
          :aria-label="segmentDescription(row, seg)"
          data-test="outage-segment"
        />
      </div>
    </td>
    <td class="col-pct">
      <AvailabilityBadge
        :text="serviceAvailabilityText(row)"
        :level="serviceAvailabilityClass(row, thresholds)"
      />
    </td>
  </tr>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { outageDetailLink, serviceLink } from '@/lib/linkUtils'
import AvailabilityBadge from './AvailabilityBadge.vue'
import SeverityIcon from '@/components/Common/SeverityIcon.vue'
import {
  AvailabilityThresholds,
  isServiceMonitored,
  segmentDescription,
  serviceAvailabilityClass,
  serviceAvailabilityText,
  serviceLinkLabel,
  TimelineServiceRow
} from '@/components/Nodes/availabilityTimelineModel'
import { TimelineTick } from './availabilityTimelineAxis'

const props = defineProps<{
  row: TimelineServiceRow
  ticks: TimelineTick[]
  baseHref: string
  nodeId: string | number
  /** Width of the leading band in which the node did not yet exist, 0-100. */
  unmonitoredPct: number
  thresholds?: AvailabilityThresholds
}>()

const serviceHref = computed(() =>
  serviceLink(props.baseHref, props.nodeId, props.row.ipAddress, props.row.serviceId))

// The same test as the text and the badge, so a service that is not managed draws no green strip
// beside its status label.
const monitored = computed(() => isServiceMonitored(props.row))
</script>

<style lang="scss" scoped>
@use '@/styles/onms-tokens' as variables;

.col-label {
  text-align: left;
  font-weight: normal;
  overflow: hidden;
}

// Indented under its interface's heading. A margin, not padding, so the indent is not part of the
// link; the name still truncates within the narrower width, after the icon of a service that is down.
.service-label {
  display: flex;
  align-items: center;
  gap: 0.25rem;
  margin-left: 1em;
  min-width: 0;
}

.service-link {
  display: block;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

// Down now: the red the legacy page gave the name (severity-Critical), as the critical icon before
// it. The token is a red in both themes; --onms-error is a pink in the dark one.
.service-link--down {
  color: var(--onms-severity-critical-bg);
}

// The small icon is 20px. A negative block margin lets it overhang the line by 2px at top and
// bottom rather than push the row taller than the 1rem strip, so a down service's row keeps the
// height of the others. Its root takes this component's scope, so no :deep() is needed.
.service-label > .severity-icon {
  margin-block: -2px;
}

.col-pct {
  text-align: right;
}

.strip {
  position: relative;
  box-sizing: border-box;
  // Keeps the bar's extent legible on a light card, where the fills sit under the 3:1 WCAG asks of
  // a meaningful graphic. The absolutely positioned fills are laid out against the padding box, so
  // they sit inside this frame rather than covering it.
  border: 1px solid var(--timeline-edge);
  height: var(--timeline-strip-height);
  // Clips an outage held open by min-width at the right edge, and the unmonitored band's corners.
  overflow: hidden;
  border-radius: var(--timeline-radius);
  // "Monitored" is the ground; only outages are drawn on it. This is what the server-rendered
  // strip did, and it means two adjacent outages cannot leave a seam between them.
  background: var(--timeline-up);
}

// Nothing was watching this service over the window, so the strip claims nothing. Green here would
// read as a full window of uptime; the legacy page left the same rows empty.
.strip--unmonitored {
  background: var(--timeline-unmonitored);
}

.strip__unmonitored {
  position: absolute;
  top: 0;
  bottom: 0;
  left: 0;
  background: var(--timeline-unmonitored);
}

.strip__gridline {
  position: absolute;
  top: 0;
  bottom: 0;
  width: 1px;
  pointer-events: none;
  // A fixed dark hairline rather than the panel background: the three strip fills are
  // theme-independent and all light, so a gridline that followed the theme would disappear over
  // them in the light theme.
  background: var(--timeline-gridline);
}

.strip__outage {
  position: absolute;
  top: 0;
  bottom: 0;
  display: block;
  box-sizing: border-box;
  // The CSS equivalent of the one-pixel floor the server used, at two pixels: one is invisible at
  // a device pixel ratio of 1 and is not a pointer target.
  min-width: 2px;
  background: var(--timeline-down);
  // Not colour alone: success and error are a green/red pair, so the hatch is what distinguishes
  // an outage without relying on hue.
  background-image: repeating-linear-gradient(
    45deg,
    rgba(255, 255, 255, 0.28) 0 2px,
    transparent 2px 4px
  );

  // An inset ring rather than an outline. A segment spans the full height of the strip, and the
  // strip clips its overflow -- it has to, to cut a min-width bar at the right edge and to keep the
  // corners rounded -- so an outline, which is drawn outside the border box, lost its top and
  // bottom edges. A shadow drawn inward cannot be clipped. Dark, because all three strip fills are
  // light in both themes.
  &:focus-visible {
    outline: none;
    box-shadow: inset 0 0 0 2px var(--timeline-focus);
  }
}

// Square off the edge that is a cut rather than a real start or end, so the bar reads as
// continuing past the window the way the server-drawn bar did when it ran to the edge.
.strip__outage--open {
  border-top-right-radius: 0;
  border-bottom-right-radius: 0;
}

.strip__outage--clipped {
  border-top-left-radius: 0;
  border-bottom-left-radius: 0;
}
</style>
