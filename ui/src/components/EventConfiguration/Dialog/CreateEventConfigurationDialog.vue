<template>
  <FeatherDialog
    v-model="store.createEventConfigSourceDialogState.visible"
    :labels="labels"
    hide-close
    @hidden="store.hideCreateEventConfigSourceDialog()"
  >
    <div
      v-if="!successMessage"
      class="modal-body-form"
    >
      <div>
        <FeatherInput
          label="Event Configuration Source Name"
          v-model="configName"
          :error="error?.name"
        />
      </div>
      <div>
        <FeatherInput
          label="Vendor"
          v-model="vendor"
          :error="error?.vendor"
        />
      </div>
      <div>
        <FeatherTextarea
          v-model.trim="description"
          data-test="event-description"
          label=""
          hint="Provide a detailed description for the event configuration source (optional)."
          rows="10"
          auto
          clear="clear"
        >
        </FeatherTextarea>
      </div>
      <div>
        <p>
          Please note that this source will be created with 0 event configurations. You can add event configurations
          after creation.
        </p>
      </div>
    </div>
    <div
      v-else
      class="modal-body-success"
    >
      <p>The event configuration source has been created successfully.</p>
    </div>
    <template v-slot:footer>
      <FeatherButton @click="store.hideCreateEventConfigSourceDialog()"> Cancel </FeatherButton>
      <FeatherButton
        v-if="!successMessage"
        primary
        @click="handleSave"
        :disabled="Object.keys(error || {}).length > 0"
      >
        Create
      </FeatherButton>
      <FeatherButton
        v-else
        primary
        @click="visitCreatedEventConfigSource"
      >
        View Source
      </FeatherButton>
    </template>
  </FeatherDialog>
</template>

<script lang="ts" setup>
import useSnackbar from '@/composables/useSnackbar'
import { addEventConfigSource } from '@/services/eventConfigService'
import { useEventConfigStore } from '@/stores/eventConfigStore'
import { FeatherButton } from '@featherds/button'
import { FeatherDialog } from '@featherds/dialog'
import { FeatherInput } from '@featherds/input'
import { FeatherTextarea } from '@featherds/textarea'

const router = useRouter()
const configName = ref('')
const vendor = ref('')
const description = ref('')
const successMessage = ref(false)
const snackbar = useSnackbar()
const store = useEventConfigStore()
const newId: Ref<number> = ref(0)
const labels = {
  title: 'Create Event Configuration Source'
}
const error = computed(() => {
  let error: any = {}
  if (configName.value.trim() === '') {
    error.name = 'Configuration name is required.'
  }
  if (vendor.value.trim() === '') {
    error.vendor = 'Vendor is required.'
  }
  return Object.keys(error).length > 0 ? error : null
})

const resetForm = () => {
  configName.value = ''
  description.value = ''
  vendor.value = ''
}

const handleSave = async () => {
  if (configName.value.trim() === '') {
    return
  }

  try {
    const response = await addEventConfigSource(
      configName.value,
      vendor.value,
      description.value
    )
    if (response && response === 201) {
      resetForm()
      store.resetSourcesPagination()
      store.refreshSourcesfilters()
      store.fetchEventConfigs().then(() => {
        successMessage.value = true
        newId.value = store.sources[0].id // Assuming the newly created source is at the top
      })
    } else if (response && response === 409 ) {
      snackbar.showSnackBar({
        msg: 'An event configuration source with this name already exists.',
        error: true
      })
    } else {
      snackbar.showSnackBar({
        msg: 'Failed to create event configuration source. Please try again.',
        error: true
      })
    }
  } catch (error) {
    console.error('Error creating event configuration source:', error)
  }
}

const visitCreatedEventConfigSource = () => {
  if (newId.value !== 0) {
    router.push({
      name: 'Event Configuration Detail',
      params: { id: newId.value }
    })
  } else {
    console.error('No new event configuration source ID available.')
  }
  successMessage.value = false
  store.hideCreateEventConfigSourceDialog()
}
</script>

<style lang="scss" scoped></style>

