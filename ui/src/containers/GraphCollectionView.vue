<template>
  <div class="onms-row">
    <div class="onms-col-12">
      <BreadCrumbs :items="breadcrumbs" />
    </div>
  </div>

  <div class="graph-collection-view">
    <TableCard>
      <div class="header">
        <div class="title-block">
          <div class="card-title" data-test="gc-view-title">{{ collection?.title ?? 'Graph Collection' }}</div>
          <div v-if="collection && !collection.graphs.length" class="subtitle" data-test="gc-no-graphs">
            There are no graphs in this collection.
          </div>
        </div>
        <div class="header-actions">
          <router-link v-if="isStored" to="/graph-collections">
            <OnmsButton variant="outlined" label="All collections" data-test="gc-back" />
          </router-link>
          <router-link v-if="isStored && canEdit" :to="`/graph-collections/${id}/edit`">
            <OnmsButton label="Customize" data-test="gc-customize" />
          </router-link>
          <router-link v-if="!isStored && canEdit && collection" :to="saveAsRoute">
            <OnmsButton label="Save as collection" data-test="gc-save-as" />
          </router-link>
        </div>
      </div>

      <p v-if="loadError" class="error" data-test="load-error">
        {{ loadError }}
        <OnmsButton variant="text" label="Retry" data-test="gc-retry" @click="load" />
      </p>
      <p v-else-if="refreshFailed" class="stale" data-test="gc-stale">
        The last refresh failed, so the graphs are from the previous load. The page tries again every five minutes.
      </p>

      <div v-if="collection && (collection.showTimespanButton || collection.showGraphtypeButton)" class="overrides">
        <FormField
          v-if="collection.showTimespanButton"
          label="Timespan for every graph"
          for="gc-timespan"
          class="override"
        >
          <OnmsSelect
            inputId="gc-timespan"
            :modelValue="timespanOverride"
            :options="timespanOptions"
            optionLabel="label"
            optionValue="id"
            data-test="gc-timespan"
            @update:modelValue="(value) => applyOverride('timespan', value as string)"
          />
        </FormField>
        <FormField
          v-if="collection.showGraphtypeButton"
          label="Graph type for every graph"
          for="gc-graphtype"
          class="override"
        >
          <OnmsSelect
            inputId="gc-graphtype"
            :modelValue="graphtypeOverride"
            :options="graphtypeOptions"
            optionLabel="label"
            optionValue="id"
            data-test="gc-graphtype"
            @update:modelValue="(value) => applyOverride('graphtype', value as string)"
          />
        </FormField>
      </div>

      <GraphCollectionGrid v-if="collection?.graphs.length" :collection="collection" />
    </TableCard>
  </div>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'

import { OnmsButton, OnmsSelect } from '@opennms/onms-ui'

import BreadCrumbs from '@/components/Layout/BreadCrumbs.vue'
import FormField from '@/components/Common/FormField.vue'
import TableCard from '@/components/Common/TableCard.vue'
import GraphCollectionGrid from '@/components/GraphCollections/GraphCollectionGrid.vue'
import useActionFeedback from '@/composables/useActionFeedback'
import useRole from '@/composables/useRole'
import {
  getGraphCollectionTimespans,
  getResolvedGraphCollection,
  getResolvedSyntheticGraphCollection
} from '@/services/graphCollectionsService'
import { useMenuStore } from '@/stores/menuStore'
import { BreadCrumb } from '@/types'
import {
  GraphCollectionTimespan,
  ResolveOptions,
  ResolvedGraphCollection,
  SyntheticGraphCollectionKind
} from '@/types/graphCollection'

/**
 * The view page of a graph collection: a stored one by `id`, or one built on the fly for a node,
 * node source or domain (`kind` plus `source`). The timespan and graph type overrides live in the
 * route query so a view with an override can be bookmarked.
 */
const props = defineProps<{
  id?: string
  kind?: SyntheticGraphCollectionKind
  source?: string
}>()

// The select needs a value for "no override"; the API spells it the same way.
const NONE = 'none'

const route = useRoute()
const router = useRouter()
const menuStore = useMenuStore()
const { withSpinner } = useActionFeedback()
const { readOnlyRole, rolesAreLoaded } = useRole()

const canEdit = computed(() => rolesAreLoaded.value && !readOnlyRole.value)

const collection = ref<ResolvedGraphCollection | null>(null)
const timespans = ref<GraphCollectionTimespan[]>([])
const loadError = ref('')
// set when a background refresh fails while a collection is on screen
const refreshFailed = ref(false)

const isStored = computed(() => !!props.id)

// the legacy "customize" of a node report: start a new stored collection from these graphs
const saveAsRoute = computed(() => {
  const param = { node: 'fromNode', nodeSource: 'fromNodeSource', domain: 'fromDomain' }[props.kind ?? 'node']
  return { path: '/graph-collections/new', query: { [param]: props.source ?? '' }}
})

const breadcrumbs = computed<BreadCrumb[]>(() => [
  { label: 'Home', to: menuStore.mainMenu.homeUrl, isAbsoluteLink: true },
  { label: 'Graph Collections', to: '/graph-collections' },
  { label: collection.value?.title ?? 'View', to: '#', position: 'last' }
])

const queryValue = (name: string): string | null => {
  const value = route.query[name]
  const single = Array.isArray(value) ? value[0] : value
  return single && single !== NONE ? single : null
}

const options = computed<ResolveOptions>(() => ({
  timespan: queryValue('timespan'),
  graphtype: queryValue('graphtype')
}))

const timespanOverride = computed(() => collection.value?.timespan ?? options.value.timespan ?? NONE)
const graphtypeOverride = computed(() => collection.value?.graphtype ?? options.value.graphtype ?? NONE)

const timespanOptions = computed(() => [
  { id: NONE, label: 'Each graph\'s own' },
  ...timespans.value
])

const graphtypeOptions = computed(() => [
  { id: NONE, label: 'Each graph\'s own' },
  ...(collection.value?.graphTypes ?? []).map(id => ({ id, label: id }))
])

const applyOverride = (name: 'timespan' | 'graphtype', value: string) => {
  const query = { ...route.query }
  if (!value || value === NONE) {
    delete query[name]
  } else {
    query[name] = value
  }
  router.replace({ query })
}

// Each override change and navigation starts a load; a slow earlier one must not land on top of
// the latest.
let loadSeq = 0

const resolve = async (background = false) => {
  const current = ++loadSeq
  const result = props.id
    ? await getResolvedGraphCollection(Number(props.id), options.value)
    : await getResolvedSyntheticGraphCollection(props.kind ?? 'node', props.source ?? '', options.value)
  if (current !== loadSeq) {
    return
  }
  if (!result.success) {
    // A background refresh that fails keeps the last good collection on screen and says so; the
    // next tick tries again, so a wall display rides out a blip or a server restart on its own.
    if (background && collection.value) {
      refreshFailed.value = true
      return
    }
    // this component instance is reused across collection routes, so drop what the previous
    // route showed instead of leaving it under the error
    collection.value = null
    loadError.value = result.message
    return
  }
  loadError.value = ''
  refreshFailed.value = false
  const resolved = result.payload as ResolvedGraphCollection | { collection: ResolvedGraphCollection }
  collection.value = 'collection' in resolved ? resolved.collection : resolved
  if (collection.value.showTimespanButton && !timespans.value.length) {
    const spans = await getGraphCollectionTimespans()
    timespans.value = spans.payload ?? []
  }
}

const load = () => withSpinner(() => resolve())

// The legacy view page carried a Refresh header of five minutes, so a wall display showing a
// collection kept moving. Re-resolve on the same cadence, without the spinner, while the page is
// visible; a fresh resolve moves every graph's window, and the grid redraws the graphs it moves.
// The timer runs through failures too: a page left showing a load error recovers on its own as
// soon as the server answers again, as the header did.
const REFRESH_INTERVAL_MS = 5 * 60 * 1000
let refreshTimer: ReturnType<typeof setInterval> | undefined

onMounted(() => {
  load()
  refreshTimer = setInterval(() => {
    if (!document.hidden) {
      resolve(true)
    }
  }, REFRESH_INTERVAL_MS)
})

onBeforeUnmount(() => clearInterval(refreshTimer))

// a changed override, or a navigation from one collection to another, re-resolves the view
watch(() => [props.id, props.kind, props.source, route.query.timespan, route.query.graphtype], load)
</script>

<style scoped lang="scss">
.graph-collection-view {
  padding: 0 2px 2rem 2px;

  :deep(.table-card) {
    padding: 1.5rem;
  }

  .header {
    display: flex;
    align-items: flex-start;
    justify-content: space-between;
    gap: 1rem;
    margin-bottom: 0.5rem;
  }

  .card-title {
    font-size: 1.25rem;
    font-weight: 600;
    overflow-wrap: anywhere;
  }

  .subtitle {
    color: var(--p-text-muted-color);
  }

  .overrides {
    display: flex;
    flex-wrap: wrap;
    gap: 1.5rem;
    margin-top: 0.5rem;
  }

  .override {
    min-width: 16rem;
  }

  .error {
    color: var(--p-red-500, #d32f2f);
  }

  .stale {
    color: var(--p-text-muted-color);
  }
}
</style>
