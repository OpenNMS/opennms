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
      >
        <OnmsInputText
          id="location-name"
          v-model="locationName"
          :invalid="!!nameProblem"
          fluid
          data-test="location-name-input"
        />
        <small v-if="nameProblem" class="field-error" data-test="name-error">{{ nameProblem }}</small>
      </FormField>

      <FormField
        label="Monitoring Area"
        for="monitoring-area"
        required
      >
        <OnmsInputText
          id="monitoring-area"
          v-model="monitoringArea"
          :invalid="!!areaProblem"
          fluid
          data-test="monitoring-area-input"
        />
        <small v-if="areaProblem" class="field-error" data-test="area-error">{{ areaProblem }}</small>
      </FormField>

      <FormField label="Geolocation (address)" for="geolocation">
        <OnmsInputText
          id="geolocation"
          v-model="geolocation"
          fluid
          data-test="geolocation-input"
        />
      </FormField>

      <div class="lat-lng">
        <FormField label="Latitude" for="latitude">
          <OnmsInputNumber
            v-model="latitude"
            inputId="latitude"
            :maxFractionDigits="6"
            :min="-90"
            :max="90"
            :invalid="!!latProblem"
            fluid
            data-test="latitude-input"
          />
          <small v-if="latProblem" class="field-error" data-test="lat-error">{{ latProblem }}</small>
        </FormField>
        <FormField label="Longitude" for="longitude">
          <OnmsInputNumber
            v-model="longitude"
            inputId="longitude"
            :maxFractionDigits="6"
            :min="-180"
            :max="180"
            :invalid="!!lngProblem"
            fluid
            data-test="longitude-input"
          />
          <small v-if="lngProblem" class="field-error" data-test="lng-error">{{ lngProblem }}</small>
        </FormField>
      </div>

      <FormField label="Priority" for="priority">
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
        <small v-if="priorityProblem" class="field-error" data-test="priority-error">{{ priorityProblem }}</small>
        <small v-else class="hint">Lower numbers sort first (1 = highest); the default location is 100.</small>
      </FormField>
    </div>

    <template #footer>
      <OnmsButton variant="text" label="Cancel" data-test="cancel-button" @click="emit('update:visible', false)" />
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

import { OnmsButton, OnmsDialog, OnmsInputNumber, OnmsInputText } from '@opennms/onms-ui'

import FormField from '@/components/Common/FormField.vue'
import { useMonitoringLocationAdminStore } from '@/stores/monitoringLocationAdminStore'
import { MonitoringLocation } from '@/types'

const props = defineProps<{
  visible: boolean
  location: MonitoringLocation | null
}>()

const emit = defineEmits(['update:visible'])

const store = useMonitoringLocationAdminStore()

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

// the location-name is a URL path segment on write; block characters that
// would break addressing or the FIQL/path encoding
const nameProblem = computed(() => {
  if (isEditing.value) {
    return null
  }
  const trimmed = locationName.value.trim()
  if (!trimmed) {
    return null
  }
  // / \ % ? # break URL/path addressing; , ; = ( ) break FIQL filter queries
  // that later reference the location by name
  if (/[/\\%?#\s,;=()]/.test(trimmed)) {
    return 'The location name must not contain whitespace or the characters / \\ % ? # , ; = ( )'
  }
  return null
})
const areaProblem = computed(() => (monitoringArea.value.trim() ? null : 'A monitoring area is required.'))
const latProblem = computed(() =>
  latitude.value !== null && (latitude.value < -90 || latitude.value > 90) ? 'Latitude must be between -90 and 90.' : null)
const lngProblem = computed(() =>
  longitude.value !== null && (longitude.value < -180 || longitude.value > 180) ? 'Longitude must be between -180 and 180.' : null)

// priority is stored in a 32-bit int DB column; anything larger fails the save
const MAX_PRIORITY = 2147483647
const priorityProblem = computed(() => {
  if (priority.value === null || priority.value === undefined) {
    return null // blank is allowed; the server defaults it to 100
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
  && !nameProblem.value && !areaProblem.value && !latProblem.value && !lngProblem.value && !priorityProblem.value)

watch(
  () => props.visible,
  (isVisible) => {
    if (!isVisible) {
      return
    }
    errorText.value = ''
    if (props.location) {
      locationName.value = props.location['location-name']
      monitoringArea.value = props.location['monitoring-area'] ?? ''
      geolocation.value = props.location.geolocation ?? ''
      latitude.value = props.location.latitude ?? null
      longitude.value = props.location.longitude ?? null
      priority.value = props.location.priority ?? null
    } else {
      locationName.value = ''
      monitoringArea.value = ''
      geolocation.value = ''
      latitude.value = null
      longitude.value = null
      priority.value = null
    }
  }
)

const save = async () => {
  saving.value = true
  try {
    // spread the original so fields this form doesn't expose (tags) round-trip
    const base = props.location ?? {}
    const payload = {
      ...base,
      'location-name': isEditing.value ? originalName.value : locationName.value.trim(),
      'monitoring-area': monitoringArea.value.trim(),
      geolocation: geolocation.value.trim() || null,
      latitude: latitude.value,
      longitude: longitude.value,
      priority: priority.value
    } as MonitoringLocation
    const error = isEditing.value ? await store.updateLocation(payload) : await store.createLocation(payload)
    if (error === null) {
      emit('update:visible', false)
    } else {
      errorText.value = error
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

.field-error {
  display: block;
  margin-top: 0.25rem;
  color: var(--p-red-500, #e24c4c);
}

.hint {
  display: block;
  margin-top: 0.25rem;
  color: var(--p-text-muted-color);
}
</style>
