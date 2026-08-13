<template>
  <OnmsDrawer
    id="source-profiles-drawer"
    data-test="source-profiles-drawer"
    v-model:visible="isVisible"
    :header="`Edit Profiles for ${props.sourceName}`"
    width="40rem"
    @hide="close"
    class="source-profiles-drawer"
  >
    <div class="container">
      <div class="section-label">Assigned Profiles</div>
      <div class="chips-container">
        <OnmsChip
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
      <FormField label="Add Profile">
        <OnmsAutoComplete
          v-model="autocompleteQuery"
          :suggestions="filteredSuggestions"
          optionLabel="name"
          @complete="onSearch"
          @optionSelect="(value) => addProfile(value as SnmpCollectionProfile)"
          placeholder="Search profiles..."
          :forceSelection="true"
          data-test="profile-autocomplete"
          dropdown
          completeOnFocus
          fluid
        />
      </FormField>
      <div class="button-row">
        <OnmsButton
          variant="ghost"
          label="Cancel"
          @click="close"
        />
        <OnmsButton
          data-test="save-profiles-button"
          label="Save"
          @click="save"
        />
      </div>
    </div>
  </OnmsDrawer>
</template>

<script lang="ts" setup>
import { computed, ref, watch } from 'vue'

import { useSnmpDataCollectionStore } from '@/stores/snmpDataCollectionStore'
import type { SnmpCollectionProfile } from '@/types/snmpDataCollection'
import { OnmsAutoComplete, OnmsButton, OnmsChip, OnmsDrawer } from '@opennms/onms-ui'
import FormField from '@/components/Common/FormField.vue'

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

const onSearch = (query: string) => {
  const q = query.toLowerCase()
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
@import '@/styles/onms-typography';
@import "@/styles/onms-tokens";

.container {
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.section-label {
  @include onms-headline4;
  color: var(--onms-secondary-text-on-surface);
}

.chips-container {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  min-height: 40px;
}

.empty-text {
  @include onms-body-large;
  color: var(--onms-secondary-text-on-surface);
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
