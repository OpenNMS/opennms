<template>
  <OnmsDialog
    :visible="visible"
    modal
    :header="isEditing ? `Edit Location: ${originalName}` : 'Add New Monitoring Location'"
    class="location-editor-dialog"
    width="min(560px, 95vw)"
    data-test="location-editor-dialog"
    @update:visible="(value: boolean) => emit('update:visible', value)"
  >
    <div class="form-column">
      <div
        v-if="errorText"
        class="dialog-error"
        role="alert"
        data-test="dialog-error"
      >{{ errorText }}</div>

      <FormField
        v-if="!isEditing"
        label="Location Name"
        for="location-name"
        required
        :error="nameProblem || undefined"
        hint="The name is the identifier and cannot be changed after creation."
      >
        <OnmsInputText
          id="location-name"
          v-model="locationName"
          :invalid="!!nameProblem"
          :maxlength="MAX_NAME_LENGTH"
          fluid
          data-test="location-name-input"
        />
      </FormField>

      <FormField
        label="Monitoring Area"
        for="monitoring-area"
        required
        :error="areaProblem || undefined"
      >
        <OnmsInputText
          id="monitoring-area"
          v-model="monitoringArea"
          :invalid="!!areaProblem"
          :maxlength="MAX_AREA_LENGTH"
          fluid
          data-test="monitoring-area-input"
        />
      </FormField>

      <FormField label="Geolocation (address)" for="geolocation" :error="geolocationProblem || undefined">
        <OnmsInputText
          id="geolocation"
          v-model="geolocation"
          :invalid="!!geolocationProblem"
          :maxlength="MAX_GEOLOCATION_LENGTH"
          fluid
          data-test="geolocation-input"
        />
      </FormField>

      <div class="lat-lng">
        <FormField label="Latitude" for="latitude" :error="latProblem || undefined">
          <OnmsInputNumber
            v-model="latitude"
            inputId="latitude"
            :maxFractionDigits="4"
            :min="-90"
            :max="90"
            :invalid="!!latProblem"
            fluid
            data-test="latitude-input"
          />
        </FormField>
        <FormField label="Longitude" for="longitude" :error="lngProblem || undefined">
          <OnmsInputNumber
            v-model="longitude"
            inputId="longitude"
            :maxFractionDigits="4"
            :min="-180"
            :max="180"
            :invalid="!!lngProblem"
            fluid
            data-test="longitude-input"
          />
        </FormField>
      </div>

      <FormField
        label="Priority"
        for="priority"
        :error="priorityProblem || undefined"
        hint="Lower numbers sort first (default 100)."
      >
        <OnmsInputNumber
          v-model="priority"
          inputId="priority"
          :useGrouping="false"
          :min="1"
          :max="MAX_PRIORITY"
          :invalid="!!priorityProblem"
          fluid
          data-test="priority-input"
        />
      </FormField>
    </div>

    <template #footer>
      <OnmsButton variant="ghost" label="Cancel" data-test="cancel-button" @click="emit('update:visible', false)" />
      <OnmsButton
        :label="isEditing ? 'Save Location' : 'Add Location'"
        :disabled="!isValid || saving"
        data-test="save-button"
        @click="save"
      />
    </template>
  </OnmsDialog>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'

import { OnmsButton, OnmsDialog, OnmsInputNumber, OnmsInputText, useOnmsToast } from '@opennms/onms-ui'

import FormField from '@/components/Common/FormField.vue'
import { isPathAddressable } from '@/lib/adminValidation'
import { useMonitoringLocationAdminStore } from '@/stores/monitoringLocationAdminStore'
import { MonitoringLocation } from '@/types'

const props = defineProps<{
  visible: boolean
  location: MonitoringLocation | null
}>()

const emit = defineEmits(['update:visible'])

const store = useMonitoringLocationAdminStore()
const { showToast } = useOnmsToast()

const locationName = ref('')
const monitoringArea = ref('')
const geolocation = ref('')
const latitude = ref<number | null>(null)
const longitude = ref<number | null>(null)
const priority = ref<number | null>(null)
const saving = ref(false)
const errorText = ref('')

const isEditing = computed(() => props.location !== null)
const originalName = computed(() => props.location?.['location-name'] ?? '')

// monitoringlocations.id and monitoringarea are varchar(256), geolocation varchar(2048)
const MAX_NAME_LENGTH = 256
const MAX_AREA_LENGTH = 256
const MAX_GEOLOCATION_LENGTH = 2048

// the location name is a URL path segment on write; block what breaks addressing or markup
const nameProblem = computed(() => {
  if (isEditing.value) {
    return null
  }
  const trimmed = locationName.value.trim()
  if (!trimmed) {
    return null
  }
  if (trimmed.length > MAX_NAME_LENGTH) {
    return `The location name cannot be longer than ${MAX_NAME_LENGTH} characters.`
  }
  if (/[/\\%?#<>"'`]/.test(trimmed)) {
    return 'The location name must not contain the characters / \\ % ? # < > " \' `'
  }
  if (!isPathAddressable(trimmed)) {
    return 'The location name cannot be . or ..'
  }
  if (store.locations.some(existing => existing['location-name'] === trimmed)) {
    return `A location named '${trimmed}' already exists.`
  }
  return null
})
const areaProblem = computed(() =>
  monitoringArea.value.trim().length > MAX_AREA_LENGTH ? `The monitoring area cannot be longer than ${MAX_AREA_LENGTH} characters.` : null)
const geolocationProblem = computed(() =>
  geolocation.value.trim().length > MAX_GEOLOCATION_LENGTH ? `The geolocation cannot be longer than ${MAX_GEOLOCATION_LENGTH} characters.` : null)
const latProblem = computed(() =>
  latitude.value !== null && (latitude.value < -90 || latitude.value > 90) ? 'Latitude must be between -90 and 90.' : null)
const lngProblem = computed(() =>
  longitude.value !== null && (longitude.value < -180 || longitude.value > 180) ? 'Longitude must be between -180 and 180.' : null)

// priority is stored in a 32-bit int DB column; anything larger fails the save
const MAX_PRIORITY = 2147483647
const priorityProblem = computed(() => {
  if (priority.value === null || priority.value === undefined) {
    return null
  }
  if (!Number.isInteger(priority.value)) {
    return 'Priority must be a whole number.'
  }
  if (priority.value < 1) {
    return 'Priority must be at least 1 (1 = highest).'
  }
  if (priority.value > MAX_PRIORITY) {
    return `Priority must be ${MAX_PRIORITY} or less.`
  }
  return null
})

const isValid = computed(() =>
  (isEditing.value || !!locationName.value.trim())
  && !!monitoringArea.value.trim()
  && !nameProblem.value && !areaProblem.value && !geolocationProblem.value
  && !latProblem.value && !lngProblem.value && !priorityProblem.value)

watch(
  () => props.visible,
  (isVisible) => {
    if (!isVisible) {
      return
    }
    errorText.value = ''
    locationName.value = props.location?.['location-name'] ?? ''
    monitoringArea.value = props.location?.['monitoring-area'] ?? ''
    geolocation.value = props.location?.geolocation ?? ''
    latitude.value = props.location?.latitude ?? null
    longitude.value = props.location?.longitude ?? null
    priority.value = props.location?.priority ?? null
  }
)

const save = async () => {
  saving.value = true
  try {
    // spread the original so fields this form doesn't expose (tags) round-trip
    const base = props.location ?? {}
    const name = isEditing.value ? originalName.value : locationName.value.trim()
    const payload = {
      ...base,
      'location-name': name,
      'monitoring-area': monitoringArea.value.trim(),
      geolocation: geolocation.value.trim() || null,
      latitude: latitude.value,
      longitude: longitude.value,
      priority: priority.value
    } as MonitoringLocation
    const result = isEditing.value ? await store.updateLocation(payload) : await store.createLocation(payload)
    if (result.success) {
      showToast({ message: `Monitoring location '${name}' ${isEditing.value ? 'updated' : 'created'}.`, severity: 'success' })
      emit('update:visible', false)
    } else {
      errorText.value = result.message
    }
  } finally {
    saving.value = false
  }
}
</script>

<style lang="scss" scoped>
.form-column {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  padding-top: 0.5rem;

  :deep(input),
  :deep(.p-inputnumber) {
    width: 100%;
  }
}

.lat-lng {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
}

.dialog-error {
  padding: 0.5rem 0.75rem;
  border-radius: 6px;
  border: 1px solid var(--p-red-200, #fecaca);
  background: var(--p-red-50, #fef2f2);
  color: var(--p-red-700, #b91c1c);
  font-size: 0.9rem;
}
</style>
