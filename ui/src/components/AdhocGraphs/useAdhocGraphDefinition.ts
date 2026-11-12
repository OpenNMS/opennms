///
/// Licensed to The OpenNMS Group, Inc (TOG) under one or more
/// contributor license agreements.  See the LICENSE.md file
/// distributed with this work for additional information
/// regarding copyright ownership.
///
/// TOG licenses this file to You under the GNU Affero General
/// Public License Version 3 (the "License") or (at your option)
/// any later version.  You may not use this file except in
/// compliance with the License.  You may obtain a copy of the
/// License at:
///
///      https://www.gnu.org/licenses/agpl-3.0.txt
///
/// Unless required by applicable law or agreed to in writing,
/// software distributed under the License is distributed on an
/// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
/// either express or implied.  See the License for the specific
/// language governing permissions and limitations under the
/// License.
///

import { computed, nextTick, onBeforeUnmount, reactive, watch } from 'vue'
import { DEFAULT_RANGE, resolveRelativeRange } from '@/components/Common/utils/timeRangeOptions'
import { AdhocGraphStore, MAX_GRAPH_NODES } from '@/stores/adhocGraphStore'
import { useAppStore } from '@/stores/appStore'
import { StartEndTime } from '@/types'
import {
  AdhocDatasourceOption,
  AdhocExpression,
  AdhocGraphConfig,
  AdhocSeries,
  AdhocSeriesOverride
} from '@/types/adhocGraph'
import { ConsolidationFunctionType } from '@/types/timeSeries'
import { restepColorForTheme, seriesColor } from './utils/adhocColors'
import {
  buildMeasurementsPayload,
  configIsQueryable,
  DEFAULT_RESOLUTION,
  expressionReferences,
  labelForDatasource,
  MAX_SERIES
} from './utils/adhocQuery'
import { AdhocLinkState, AdhocUrlState } from './utils/adhocUrlState'

/**
 * One ad-hoc graph's definition: the config, the user's per-series edits, the time
 * window, and the pipeline that turns what the store's filters match into the
 * series the chart draws and the query the server answers. The builder page owns
 * one of these on the default store; a graph collection cell owns one per graph
 * on a store of its own, so the two never meet.
 *
 * Call it from a component's setup: its watchers and teardown belong to that
 * component's scope.
 */
export const useAdhocGraphDefinition = (store: AdhocGraphStore) => {
  const appStore = useAppStore()

  /**
   * Set on teardown. The query is debounced by its callers, so one can still be
   * pending when the component is gone.
   */
  let disposed = false
  let expressionSeq = 0

  const config = reactive<AdhocGraphConfig>({
    series: [],
    expressions: [],
    title: '',
    verticalLabel: '',
    stacked: false,
    resolution: DEFAULT_RESOLUTION
  })

  /**
   * Per-series edits, keyed by series. The series list is generated from the
   * filters every time they change, so an edited label or color has to live apart
   * from the generated row and be re-applied when the row is rebuilt.
   */
  const overrides = reactive<Record<string, AdhocSeriesOverride>>({})

  /**
   * The generated label each series was given, by key. A generated label is a
   * JEXL identifier that an expression may reference, so once a series has one it
   * keeps it, whatever order later builds happen in; only a label made from a
   * link's stand-in, before the node and resource names were known, is replaced.
   */
  const generatedLabels = new Map<string, { label: string, placeholder: boolean }>()

  // Relative by default, so an unbookmarked page and a bookmarked one behave alike.
  const time = reactive<StartEndTime>(resolveRelativeRange(DEFAULT_RANGE))

  /**
   * Filters compose multiplicatively, so a loose combination can name far more
   * series than a plot can show or the measurements API should be asked for. Past
   * the cap the page explains rather than tries.
   */
  const tooManySeriesMessage = computed<string>(() => {
    if (store.nodeLimitExceeded) {
      return `The node filter matches ${store.nodeLimitExceeded} nodes; a graph can be built from at most ${MAX_GRAPH_NODES}. Narrow the rule, or select nodes.`
    }

    const count = store.effectiveDatasources.length

    if (count <= MAX_SERIES) {
      return ''
    }

    return `These filters match ${count} series; the limit is ${MAX_SERIES}. Narrow a filter, or select items in a column.`
  })

  const canQuery = computed<boolean>(() => !tooManySeriesMessage.value && configIsQueryable(config))

  /**
   * The first palette color no current series or expression is using, so a series
   * the filter adds later cannot land on an expression's color, and so the colors
   * come out the same when a link is reopened: expressions travel with theirs, and
   * the series fill in around them in order, as they did for the sender.
   */
  const freeColor = (used: Set<string>): string => {
    for (let slot = 0; slot < 64; slot++) {
      const candidate = seriesColor(slot, appStore.theme)

      if (!used.has(candidate)) {
        return candidate
      }
    }

    return seriesColor(used.size, appStore.theme)
  }

  const claimColor = (used: Set<string>): string => {
    const color = freeColor(used)
    used.add(color)
    return color
  }

  /**
   * Rebuild `config.series` from what the filters currently match, re-applying the
   * user's edits. Colors are carried over from the previous build where the series
   * survived, so adding a series does not recolor its neighbors; a new series takes
   * the palette slot for its position. A generated label is remembered per series
   * (see `generatedLabels`) so that a filter matching one more node cannot renumber
   * `foo` to `foo_2` under an expression; the one label not kept is a placeholder
   * made from a link's stand-in before its node and resource names were known.
   */
  const reconcileSeries = (datasources: AdhocDatasourceOption[]) => {
    if (datasources.length > MAX_SERIES) {
      config.series = []
      return
    }

    const existing = new Map(config.series.map(entry => [entry.key, entry]))
    const taken = new Set<string>()
    const next: AdhocSeries[] = []
    const usedColors = new Set(config.expressions.map(expression => expression.color))

    // Colors held by series that survive this build are claimed before any new
    // series picks one, whatever order the two come in.
    for (const datasource of datasources) {
      const color = overrides[datasource.key]?.color ?? existing.get(datasource.key)?.color

      if (color) {
        usedColors.add(color)
      }
    }

    // Edited labels are claimed first so a generated one can never collide with
    // them; then the labels series already hold, so a newcomer takes the suffix.
    for (const datasource of datasources) {
      const label = overrides[datasource.key]?.label

      if (label) {
        taken.add(label)
      }
    }

    const kept = new Map<string, string>()

    for (const datasource of datasources) {
      const memory = generatedLabels.get(datasource.key)
      const stale = !memory || (memory.placeholder && Boolean(datasource.nodeLabel))

      if (!overrides[datasource.key]?.label && !stale && !taken.has(memory.label)) {
        kept.set(datasource.key, memory.label)
        taken.add(memory.label)
      }
    }

    for (const datasource of datasources) {
      const override = overrides[datasource.key] ?? {}
      const previous = existing.get(datasource.key)
      let label = override.label ?? kept.get(datasource.key)

      if (!label) {
        label = labelForDatasource(datasource, taken)
        generatedLabels.set(datasource.key, { label, placeholder: !datasource.nodeLabel })
      }

      taken.add(label)

      next.push({
        key: datasource.key,
        label,
        resourceId: datasource.resourceId,
        attribute: datasource.attribute,
        aggregation: override.aggregation ?? ConsolidationFunctionType.AVERAGE,
        color: override.color ?? previous?.color ?? claimColor(usedColors),
        style: override.style ?? 'line',
        hidden: override.hidden ?? false
      })
    }

    config.series = next
  }

  const updateSeries = (key: string, patch: Partial<AdhocSeries>) => {
    overrides[key] = { ...overrides[key], ...patch }
    config.series = config.series.map(entry => (entry.key === key ? { ...entry, ...patch } : entry))
  }

  const addExpression = () => {
    config.expressions = [...config.expressions, {
      id: `expr-${++expressionSeq}`,
      label: `expression_${config.expressions.length + 1}`,
      value: '',
      color: freeColor(new Set([...config.series, ...config.expressions].map(entry => entry.color))),
      style: 'line'
    }]
  }

  const updateExpression = (id: string, patch: Partial<AdhocExpression>) => {
    config.expressions = config.expressions.map(entry => (entry.id === id ? { ...entry, ...patch } : entry))
  }

  const removeExpression = (id: string) => {
    config.expressions = config.expressions.filter(entry => entry.id !== id)
  }

  const updateTime = (value: StartEndTime) => {
    time.startTime = value.startTime
    time.endTime = value.endTime
    time.format = value.format
    // Deleted rather than left stale: a custom range carries no `range`, and keeping
    // the previous one would make an absolute window silently start sliding.
    if (value.range) {
      time.range = value.range
    } else {
      delete time.range
    }
  }

  /**
   * Slide a relative window up to the present. A no-op for a custom range, which is
   * absolute by definition.
   */
  const refreshRelativeWindow = () => {
    if (!time.range) {
      return
    }

    const resolved = resolveRelativeRange(time.range)
    time.startTime = resolved.startTime
    time.endTime = resolved.endTime
  }

  const runQuery = () => {
    if (disposed || !canQuery.value) {
      return
    }

    // "Last hour" must mean the hour ending now, not the hour that ended whenever the
    // range was chosen, which may have been a long time ago on a page left open.
    refreshRelativeWindow()
    store.runQuery(buildMeasurementsPayload(config, time))
  }

  /** Back to an empty graph: the config, the edits and the label memory. The store is the caller's. */
  const resetDefinition = () => {
    generatedLabels.clear()
    config.series = []
    config.expressions = []
    config.title = ''
    config.verticalLabel = ''
    config.stacked = false
    config.resolution = DEFAULT_RESOLUTION

    for (const key of Object.keys(overrides)) {
      delete overrides[key]
    }
  }

  /** What a link carries besides the config: the picker, the edits, and the series count for comparison. */
  const linkState = (): AdhocLinkState => {
    // Every edit travels, plotted or not: a series the filters set aside for the
    // moment, or the whole set while it is over the cap, keeps its edits for when
    // it is back. Edits are few and deliberate; Clear all is what forgets them.
    const carried: Record<string, AdhocSeriesOverride> = Object.fromEntries(
      Object.entries(overrides).map(([key, override]) => [key, { ...override }])
    )

    // A generated label an expression refers to is part of the graph's meaning:
    // write it into the link, or the reopened graph may generate it differently.
    for (const series of config.series) {
      const referenced = config.expressions.some(expression => expressionReferences(expression.value, series.label))

      if (referenced && !carried[series.key]?.label) {
        carried[series.key] = { ...carried[series.key], label: series.label }
      }
    }

    return { selection: store.selectionState, overrides: carried, seriesCount: config.series.length }
  }

  /**
   * Take a decoded link: its config, edits and time, then evaluate its picker
   * against the server as it stands now. The series follow through the watcher
   * below, so this waits a tick for them before returning; the caller decides
   * what to say about the result and when to query.
   */
  const restore = async (restored: AdhocUrlState) => {
    Object.assign(config, restored.config)
    Object.assign(overrides, restored.link.overrides)

    if (restored.time.range) {
      // Resolved against the clock now, so the link shows current data however old
      // the bookmark is.
      updateTime(resolveRelativeRange(restored.time.range))
    } else if (restored.time.startTime && restored.time.endTime) {
      updateTime(restored.time)
    }

    // An expression id is regenerated rather than carried; a color is optional.
    const expressionColors = new Set(config.expressions.map(entry => entry.color).filter(Boolean))
    config.expressions = config.expressions.map(entry => ({
      ...entry,
      id: `expr-${++expressionSeq}`,
      color: entry.color || claimColor(expressionColors)
    }))

    await store.restore(restored.link.selection)
    await nextTick()
  }

  // The series are whatever the filters and picks currently produce.
  watch(() => store.effectiveDatasources, value => reconcileSeries([...value]))

  // Edits and generated labels for series no node seen this session can produce
  // are forgotten, so neither the page nor the link grows for as long as it is
  // open. Judged only after a load that had nothing missing (see the store): after
  // a failed rule, an overflow or a node that did not answer, "not seen" says
  // nothing about the series, and a link's edits must survive until it is fixed.
  watch(() => store.cleanResourceLoads, () => {
    for (const key of Object.keys(overrides)) {
      if (!store.isKnownKey(key)) {
        delete overrides[key]
      }
    }

    for (const key of [...generatedLabels.keys()]) {
      if (!store.isKnownKey(key)) {
        generatedLabels.delete(key)
      }
    }
  })

  // A shared link carries the colors of the theme it was built in; move any color
  // still sitting on a palette slot to that slot's step for the current theme.
  watch(() => appStore.theme, (theme) => {
    config.series = config.series.map(entry => ({ ...entry, color: restepColorForTheme(entry.color, theme) }))
    config.expressions = config.expressions.map(entry => ({ ...entry, color: restepColorForTheme(entry.color, theme) }))
  })

  onBeforeUnmount(() => {
    disposed = true
  })

  return {
    config,
    time,
    tooManySeriesMessage,
    canQuery,
    updateSeries,
    addExpression,
    updateExpression,
    removeExpression,
    updateTime,
    refreshRelativeWindow,
    runQuery,
    resetDefinition,
    linkState,
    restore
  }
}
