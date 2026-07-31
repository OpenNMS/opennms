<template>
  <Dialog
    :visible="visible"
    modal
    :header="isEditing ? `Edit Location: ${originalName}` : 'Add New Monitoring Location'"
    class="location-editor-dialog"
    :style="{ width: '560px', maxWidth: '95vw' }"
    data-test="location-editor-dialog"
    @update:visible="(value: boolean) => emit('update:visible', value)"
  >
    <div class="form-column">
      <Message
        v-if="errorText"
        severity="error"
        :closable="false"
        data-test="dialog-error"
      >{{ errorText }}</Message>

      <FormField v-if="!isEditing">
        <IftaLabel>
          <InputText
            id="location-name"
            v-model="locationName"
            :invalid="!!nameProblem"
            data-test="location-name-input"
          />
          <label for="location-name">Location Name *</label>
        </IftaLabel>
        <small v-if="nameProblem" class="field-error" data-test="name-error">{{ nameProblem }}</small>
      </FormField>

      <FormField>
        <IftaLabel>
          <InputText
            id="monitoring-area"
            v-model="monitoringArea"
            :invalid="!!areaProblem"
            data-test="monitoring-area-input"
          />
          <label for="monitoring-area">Monitoring Area *</label>
        </IftaLabel>
        <small v-if="areaProblem" class="field-error" data-test="area-error">{{ areaProblem }}</small>
      </FormField>

      <FormField>
        <IftaLabel>
          <InputText id="geolocation" v-model="geolocation" data-test="geolocation-input" />
          <label for="geolocation">Geolocation (address)</label>
        </IftaLabel>
      </FormField>

      <div class="lat-lng">
        <FormField>
          <IftaLabel>
            <InputNumber
              v-model="latitude"
              inputId="latitude"
              :minFractionDigits="0"
              :maxFractionDigits="6"
              :min="-90"
              :max="90"
              :invalid="!!latProblem"
              data-test="latitude-input"
            />
            <label for="latitude">Latitude</label>
          </IftaLabel>
          <small v-if="latProblem" class="field-error" data-test="lat-error">{{ latProblem }}</small>
        </FormField>
        <FormField>
          <IftaLabel>
            <InputNumber
              v-model="longitude"
              inputId="longitude"
              :minFractionDigits="0"
              :maxFractionDigits="6"
              :min="-180"
              :max="180"
              :invalid="!!lngProblem"
              data-test="longitude-input"
            />
            <label for="longitude">Longitude</label>
          </IftaLabel>
          <small v-if="lngProblem" class="field-error" data-test="lng-error">{{ lngProblem }}</small>
        </FormField>
      </div>

      <FormField>
        <IftaLabel>
          <InputNumber v-model="priority" inputId="priority" :useGrouping="false" :min="0" data-test="priority-input" />
          <label for="priority">Priority</label>
        </IftaLabel>
        <small class="hint">Lower numbers sort first; the default location is 100.</small>
      </FormField>
    </div>

    <template #footer>
      <Button text label="Cancel" data-test="cancel-button" @click="emit('update:visible', false)" />
      <Button
        :label="isEditing ? 'Save Location' : 'Add Location'"
        :disabled="!isValid || saving"
        data-test="save-button"
        @click="save"
      />
    </template>
  </Dialog>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'

import Button from 'primevue/button'
import Dialog from 'primevue/dialog'
import IftaLabel from 'primevue/iftalabel'
import InputNumber from 'primevue/inputnumber'
import InputText from 'primevue/inputtext'
import Message from 'primevue/message'

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

const isValid = computed(() =>
  (isEditing.value || !!locationName.value.trim())
  && !nameProblem.value && !areaProblem.value && !latProblem.value && !lngProblem.value)

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
