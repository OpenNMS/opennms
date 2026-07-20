<template>
  <Drawer
    id="source-profiles-drawer"
    data-test="source-profiles-drawer"
    v-model:visible="isVisible"
    position="right"
    :header="`Edit Profiles for ${props.sourceName}`"
    :style="{ width: '40rem' }"
    @hide="close"
    class="source-profiles-drawer"
  >
    <div class="container">
      <div class="section-label">Assigned Profiles</div>
      <div class="chips-container">
        <PChip
          v-for="profile in localProfiles"
          :key="profile.id"
          :label="profile.name"
          removable
          @remove="removeProfile(profile)"
        />
        <span
          v-if="localProfiles.length === 0"
          class="empty-text"
        >No profiles assigned</span>
      </div>
      <div class="spacer" />
      <div class="section-label">Add Profile</div>
      <PAutoComplete
        v-model="autocompleteQuery"
        :suggestions="filteredSuggestions"
        optionLabel="name"
        @complete="onSearch"
        @option-select="addProfile($event.value)"
        placeholder="Search profiles..."
        :forceSelection="true"
        data-test="profile-autocomplete"
        dropdown
        completeOnFocus
      />
      <div class="button-row">
        <Button
          text
          label="Cancel"
          @click="close"
        />
        <Button
          data-test="save-profiles-button"
          label="Save"
          @click="save"
        />
      </div>
    </div>
  </Drawer>
</template>

<script lang="ts" setup>
import { computed, ref, watch } from 'vue'

import { useSnmpDataCollectionStore } from '@/stores/snmpDataCollectionStore'
import type { SnmpCollectionProfile } from '@/types/snmpDataCollection'
import AutoCompleteComponent from 'primevue/autocomplete'
import Button from 'primevue/button'
import ChipComponent from 'primevue/chip'
import Drawer from 'primevue/drawer'

const PChip = ChipComponent
const PAutoComplete = AutoCompleteComponent

const props = defineProps<{
  visible: boolean
  sourceName: string
  profiles: SnmpCollectionProfile[]
}>()

const emit = defineEmits<{
  (e: 'close'): void
  (e: 'saved', profiles: SnmpCollectionProfile[]): void
}>()

const snmpDataCollectionStore = useSnmpDataCollectionStore()

const isVisible = ref(false)
const localProfiles = ref<SnmpCollectionProfile[]>([])
const autocompleteQuery = ref<string | SnmpCollectionProfile>('')
const filteredSuggestions = ref<SnmpCollectionProfile[]>([])

const availableProfiles = computed(() =>
  snmpDataCollectionStore.profiles.filter(
    p => !localProfiles.value.some(lp => lp.id === p.id)
  )
)

const onOpen = async () => {
  await snmpDataCollectionStore.fetchSnmpCollectionProfiles()
  localProfiles.value = props.profiles.map((p: SnmpCollectionProfile) => ({ ...p, sourceNames: [...p.sourceNames] }))
  autocompleteQuery.value = ''
  filteredSuggestions.value = [...availableProfiles.value]
}

const onSearch = (event: { query: string }) => {
  const q = event.query.toLowerCase()
  if (q.length > 0) {
    filteredSuggestions.value = availableProfiles.value.filter(p =>
      p.name.toLowerCase().includes(q)
    )
  } else {
    filteredSuggestions.value = [...availableProfiles.value]
  }
}

const addProfile = (profile: SnmpCollectionProfile) => {
  localProfiles.value.push({ ...profile, sourceNames: [...profile.sourceNames] })
  autocompleteQuery.value = ''
}

const removeProfile = (profile: SnmpCollectionProfile) => {
  localProfiles.value = localProfiles.value.filter(p => p.id !== profile.id)
}

const close = () => {
  isVisible.value = false
  emit('close')
}

const save = () => {
  emit('saved', [...localProfiles.value])
  isVisible.value = false
}

watch(() => props.visible, async (visible) => {
  if (visible) {
    isVisible.value = true
    await onOpen()
  }
}, { immediate: true })
</script>

<style scoped lang="scss">
@import "@featherds/styles/mixins/typography";
@import "@featherds/styles/themes/variables";

.container {
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.section-label {
  @include headline4;
  color: var(--feather-secondary-text-on-surface);
}

.chips-container {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  min-height: 40px;
}

.empty-text {
  @include body-large;
  color: var(--feather-secondary-text-on-surface);
  font-style: italic;
}

.spacer {
  height: 8px;
}

.button-row {
  display: flex;
  gap: 8px;
  margin-top: 16px;

  :deep(.btn + .btn) {
    margin-left: 0 !important;
  }
}
</style>
