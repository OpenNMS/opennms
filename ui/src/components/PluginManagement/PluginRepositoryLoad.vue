<template>
  <div class="repository-load" data-test="plugin-repository-load">
    <p class="tab-note">
      Pick a plugin and one of its released versions. The server downloads the KAR from the plugin's
      GitHub releases and runs the same checks as for an uploaded file; nothing is written until Load
      plugin is pressed.
    </p>
    <p v-if="store.catalog === undefined" class="placeholder" data-test="catalog-loading">Loading the plugin catalog…</p>
    <p v-else-if="store.catalog === null" class="error" data-test="catalog-error">
      The plugin catalog could not be read. Check that the server is up and that you are still logged in, then reload the page.
    </p>
    <template v-else>
      <div class="field-row">
        <FormField class="field" label="Plugin" for="plugin-catalog-entry" :hint="selectedEntry?.description || undefined">
          <OnmsSelect
            v-model="selectedId"
            inputId="plugin-catalog-entry"
            :options="pluginOptions"
            optionLabel="label"
            optionValue="value"
            placeholder="Choose a plugin"
            :disabled="disabled || busy"
            data-test="plugin-select"
          />
        </FormField>
        <a
          v-if="selectedEntry?.docsUrl"
          class="docs-link"
          :href="selectedEntry.docsUrl"
          target="_blank"
          rel="noopener noreferrer"
          data-test="plugin-docs-link"
        >Documentation</a>
      </div>
      <div v-if="isCustom" class="field-row">
        <FormField class="field" label="GitHub repository" for="plugin-custom-repository" :error="repositoryError" hint="owner/repository, as in the GitHub URL">
          <OnmsInputText
            id="plugin-custom-repository"
            v-model.trim="customRepository"
            placeholder="owner/repository"
            :invalid="!!repositoryError"
            :disabled="disabled || busy"
            data-test="custom-repository"
            @keyup.enter="lookUpReleases"
          />
        </FormField>
        <FormField reserveLabelSpace>
          <OnmsButton
            variant="outlined"
            label="Look up releases"
            :disabled="disabled || busy || !isRepository(customRepository)"
            :loading="loadingReleases"
            data-test="lookup-releases"
            @click="lookUpReleases"
          />
        </FormField>
      </div>
      <p v-if="loadingReleases && !isCustom" class="placeholder" data-test="releases-loading">Reading the releases…</p>
      <p v-if="releasesError" class="error" role="alert" data-test="releases-error">{{ releasesError }}</p>
      <template v-if="store.releases && query">
        <div class="field-row">
          <FormField class="field" label="Version" for="plugin-release-tag" :hint="releasesHint">
            <OnmsSelect
              v-model="selectedTag"
              inputId="plugin-release-tag"
              :options="releaseOptions"
              optionLabel="label"
              optionValue="value"
              placeholder="No releases found"
              :disabled="disabled || busy || releaseOptions.length === 0"
              data-test="release-select"
            />
          </FormField>
          <FormField v-if="assetOptions.length > 1" class="field" label="File" for="plugin-release-asset">
            <OnmsSelect
              v-model="selectedAsset"
              inputId="plugin-release-asset"
              :options="assetOptions"
              optionLabel="label"
              optionValue="value"
              :disabled="disabled || busy"
              data-test="asset-select"
            />
          </FormField>
          <FormField reserveLabelSpace>
            <OnmsButton
              variant="outlined"
              label="Refresh"
              :disabled="disabled || busy"
              :loading="loadingReleases"
              title="Read the releases from GitHub again"
              data-test="refresh-releases"
              @click="lookUpReleases"
            />
          </FormField>
        </div>
        <p v-if="selectedRelease && assetOptions.length === 0" class="error" data-test="no-kar-asset">
          Release {{ selectedRelease.tag }} has no .kar file among its assets, so it cannot be loaded from here.
        </p>
        <template v-if="selectedRelease?.notes">
          <div class="notes-title">Release notes</div>
          <pre class="release-notes" data-test="release-notes">{{ selectedRelease.notes }}</pre>
        </template>
      </template>
      <p v-if="fetchError" class="error" role="alert" data-test="fetch-error">{{ fetchError }}</p>
      <div v-if="fetching" class="progress" role="status" data-test="fetch-progress">
        <OnmsSpinner size="1.5rem" strokeWidth="6" />
        <span>Downloading {{ selectedAsset }} ({{ formatSize(selectedAssetSize) }})…</span>
      </div>
      <div class="actions">
        <OnmsButton
          label="Fetch and check"
          :disabled="!canFetch"
          :loading="fetching"
          :title="disabled ? CONTAINER_UNAVAILABLE : undefined"
          data-test="fetch-plugin"
          @click="fetchAndCheck"
        />
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { OnmsButton, OnmsInputText, OnmsSelect, OnmsSpinner } from '@opennms/onms-ui'

import FormField from '@/components/Common/FormField.vue'
import { usePluginManagementStore } from '@/stores/pluginManagementStore'
import { KarInspection, PluginFetchInput, PluginReleasesQuery } from '@/types/pluginManagement'
import { CONTAINER_UNAVAILABLE, defaultRelease, fetchedSourceLine, formatSize, isRepository, karAssets, releaseLabel, sortReleases } from './pluginDisplay'

const CUSTOM = '__custom__'

const props = defineProps<{
  // the container is unavailable: nothing can be fetched
  disabled: boolean
}>()

const emit = defineEmits<{
  (e: 'checked', inspection: KarInspection, source: string): void
  (e: 'reset'): void
}>()

const store = usePluginManagementStore()

const selectedId = ref<string | null>(null)
const customRepository = ref('')
const lookedUpRepository = ref('')
const loadingReleases = ref(false)
const releasesError = ref('')
const selectedTag = ref<string | null>(null)
const selectedAsset = ref<string | null>(null)
const fetching = ref(false)
const fetchError = ref('')

const busy = computed(() => loadingReleases.value || fetching.value)
const isCustom = computed(() => selectedId.value === CUSTOM)
const selectedEntry = computed(() => store.catalog?.entries.find(e => e.id === selectedId.value) ?? null)

const pluginOptions = computed(() => {
  const options = (store.catalog?.entries ?? []).map(e => ({ label: e.name, value: e.id }))
  return store.catalog?.customAllowed ? [...options, { label: 'Other GitHub repository', value: CUSTOM }] : options
})

const repositoryError = computed(() =>
  customRepository.value && !isRepository(customRepository.value) ? 'Enter the repository as owner/repository.' : undefined)

// what the current selection would look up; null until a plugin is chosen
const query = computed<PluginReleasesQuery | null>(() => {
  if (selectedEntry.value) {
    return { catalogId: selectedEntry.value.id }
  }
  if (isCustom.value && lookedUpRepository.value) {
    return { repository: lookedUpRepository.value }
  }
  return null
})

const sortedReleases = computed(() => sortReleases(store.releases?.releases ?? []))
const releaseOptions = computed(() => sortedReleases.value.map(r => ({ label: releaseLabel(r), value: r.tag })))
const selectedRelease = computed(() => sortedReleases.value.find(r => r.tag === selectedTag.value) ?? null)
const assetOptions = computed(() => karAssets(selectedRelease.value).map(a => ({ label: `${a.name} (${formatSize(a.size)})`, value: a.name })))
const selectedAssetSize = computed(() => karAssets(selectedRelease.value).find(a => a.name === selectedAsset.value)?.size)

const releasesHint = computed(() => {
  if (!store.releases) {
    return undefined
  }
  const count = store.releases.releases.length
  return `${count} release${count === 1 ? '' : 's'} on ${store.releases.repository}${store.releases.cached ? ' (cached)' : ''}`
})

const canFetch = computed(() =>
  !props.disabled && !busy.value && query.value !== null && selectedTag.value !== null && selectedAsset.value !== null)

const clearReleases = () => {
  store.releases = null
  releasesError.value = ''
  selectedTag.value = null
  selectedAsset.value = null
}

const loadReleases = async (target: PluginReleasesQuery) => {
  clearReleases()
  fetchError.value = ''
  loadingReleases.value = true
  try {
    const result = await store.loadReleases(target)
    if (result.success && result.payload) {
      selectedTag.value = defaultRelease(result.payload.releases)?.tag ?? null
    } else {
      releasesError.value = result.message
    }
  } finally {
    loadingReleases.value = false
  }
}

const lookUpReleases = async () => {
  if (isCustom.value) {
    if (!isRepository(customRepository.value)) {
      return
    }
    lookedUpRepository.value = customRepository.value
  }
  if (query.value) {
    await loadReleases(query.value)
  }
}

watch(selectedId, async () => {
  emit('reset')
  fetchError.value = ''
  lookedUpRepository.value = ''
  clearReleases()
  if (selectedEntry.value) {
    await loadReleases({ catalogId: selectedEntry.value.id })
  }
})

watch(selectedRelease, (release) => {
  selectedAsset.value = karAssets(release)[0]?.name ?? null
})

const fetchAndCheck = async () => {
  if (!canFetch.value || !query.value || !selectedTag.value || !selectedAsset.value) {
    return
  }
  const input: PluginFetchInput = { ...query.value, tag: selectedTag.value, assetName: selectedAsset.value }
  emit('reset')
  fetchError.value = ''
  fetching.value = true
  try {
    const result = await store.fetchFromRepository(input)
    if (result.success && result.payload) {
      const inspection = result.payload
      const source = inspection.source ?? { repository: store.releases?.repository ?? '', tag: input.tag, assetName: input.assetName, url: '' }
      emit('checked', inspection, fetchedSourceLine(source, inspection.size))
    } else {
      fetchError.value = result.message
    }
  } finally {
    fetching.value = false
  }
}

onMounted(async () => {
  if (store.catalog === undefined) {
    await store.loadCatalog()
  }
})
</script>

<style lang="scss" scoped>
.repository-load {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.tab-note,
.placeholder {
  margin: 0;
  font-size: 0.9rem;
  color: var(--p-text-muted-color);
}

.field-row {
  display: flex;
  align-items: flex-start;
  flex-wrap: wrap;
  gap: 0.75rem 1rem;
}

.field {
  flex: 1 1 260px;
  max-width: 480px;
}

.docs-link {
  align-self: center;
  font-size: 0.9rem;
  white-space: nowrap;
}

.notes-title {
  font-size: 0.9rem;
  font-weight: 600;
}

.release-notes {
  margin: 0;
  padding: 0.75rem;
  max-height: 16rem;
  overflow: auto;
  font-size: 0.85rem;
  white-space: pre-wrap;
  word-break: break-word;
  border-radius: 6px;
  background: var(--p-surface-100, #f4f4f4);
}

.progress {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  font-size: 0.9rem;
}

.error {
  color: var(--p-red-500, #c62828);
  margin: 0;
  font-size: 0.9rem;
}

.actions {
  display: flex;
  justify-content: flex-end;
}
</style>
