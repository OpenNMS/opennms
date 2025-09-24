<template>
  <FeatherDrawer
    id="column-selection-drawer"
    data-test="column-selection-drawer"
    @shown="() => store.eventModificationDrawerState.visible"
    @hidden="() => store.eventModificationDrawerState.visible"
    v-model="store.eventModificationDrawerState.visible"
    :labels="{ close: 'close', title: 'Customize Columns' }"
    width="55em"
  >
    <div
      class="feather-drawer-custom-padding"
      v-if="store.eventModificationDrawerState.eventConfigEvent"
    >
      <section>
        <h3>
          {{ store.eventModificationDrawerState.isEditMode === CreateEditMode.Create ? 'Create New Event' : 'Edit Event Details' }}
        </h3>
      </section>
      <div class="spacer-large"></div>
      <div class="drawer-content">
        <FeatherInput
          label="Event UEI"
          type="search"
          data-test="event-label"
          v-model.trim="eventUei"
        ></FeatherInput>
        <div class="spacer-medium"></div>
        <div class="spacer-medium"></div>
        <FeatherInput
          label="Event Label"
          type="search"
          data-test="event-label"
          v-model.trim="eventLabel"
        >
        </FeatherInput>
        <div class="spacer-medium"></div>
        <div class="spacer-medium"></div>
        <FeatherTextarea
          v-model.trim="eventDescription"
          label="Event Description"
          placeholder="Type your event description here..."
          rows="10"
          cols="40"
          auto
          clear
        ></FeatherTextarea>
        <div class="spacer-medium"></div>
        <div class="spacer-medium"></div>
        <FeatherSelect
          label="Select Status"
          :options="statusOptions"
          v-model="selectedEventStatus"
        >
          <FeatherIcon :icon="MoreVert" />
        </FeatherSelect>
      </div>
      <div class="spacer-large"></div>
      <div>
        <FeatherButton
          primary
          @click="handleSave()"
        >
          Save Changes
        </FeatherButton>
        <FeatherButton
          secondary
          @click="store.closeEventModificationDrawer()"
        >
          Close
        </FeatherButton>
      </div>
    </div>
  </FeatherDrawer>
</template>

<script lang="ts" setup>
import useSnackbar from '@/composables/useSnackbar'
import { mapEventConfigEventToServer } from '@/mappers/eventConfig.mapper'
import { updateEventConfigEventById } from '@/services/eventConfigService'
import { useEventConfigDetailStore } from '@/stores/eventConfigDetailStore'
import { CreateEditMode } from '@/types'
import { EventConfigEvent } from '@/types/eventConfig'
import { FeatherButton } from '@featherds/button'
import { FeatherDrawer } from '@featherds/drawer'
import { FeatherIcon } from '@featherds/icon'
import MoreVert from '@featherds/icon/navigation/MoreVert'
import { FeatherInput } from '@featherds/input'
import { FeatherSelect, ISelectItemType } from '@featherds/select'
import { FeatherTextarea } from '@featherds/textarea'

const statusOptions: ISelectItemType[] = [
  { _text: 'Enable', _value: 'enable' },
  { _text: 'Disable', _value: 'disable' }
]
const snackbar = useSnackbar()
const store = useEventConfigDetailStore()
const eventDescription = ref(store.eventModificationDrawerState.eventConfigEvent?.description || '')
const eventLabel = ref(store.eventModificationDrawerState.eventConfigEvent?.eventLabel || '')
const eventUei = ref(store.eventModificationDrawerState.eventConfigEvent?.uei || '')
const selectedEventStatus = ref<ISelectItemType>({
  _text: store.eventModificationDrawerState.eventConfigEvent?.enabled ? 'Enable' : 'Disable',
  _value: store.eventModificationDrawerState.eventConfigEvent?.enabled ? 'enable' : 'disable'
})

const validateEventDetails = (): boolean => {
  if (!store.eventModificationDrawerState.eventConfigEvent) {
    snackbar.showSnackBar({ msg: 'No event selected', error: true })
    return false
  }
  if (!eventUei.value) {
    snackbar.showSnackBar({ msg: 'Event UEI is required', error: true })
    return false
  }
  if (!eventLabel.value) {
    snackbar.showSnackBar({ msg: 'Event label is required', error: true })
    return false
  }
  if (!eventDescription.value) {
    snackbar.showSnackBar({ msg: 'Event description is required', error: true })
    return false
  }
  if (selectedEventStatus.value === undefined) {
    snackbar.showSnackBar({ msg: 'Event status is required', error: true })
    return false
  }
  return true
}

const handleSave = async () => {
  if (!store.eventModificationDrawerState.eventConfigEvent) {
    return
  }

  if (!validateEventDetails()) {
    return
  }

  const event = {
    ...store.eventModificationDrawerState.eventConfigEvent,
    uei: eventUei.value,
    eventLabel: eventLabel.value,
    description: eventDescription.value,
    enabled: selectedEventStatus.value._value === 'enable'
  } as EventConfigEvent

  const newEvent = mapEventConfigEventToServer(event, store.eventModificationDrawerState.isEditMode)

  const response = await updateEventConfigEventById(newEvent)

  if (response) {
    snackbar.showSnackBar({ msg: 'Event Updated.', error: false })
    await store.fetchEventsBySourceId()
    store.closeEventModificationDrawer()
  } else {
    snackbar.showSnackBar({ msg: 'Something went wrong', error: true })
  }
}

watch(
  () => store.eventModificationDrawerState.eventConfigEvent,
  (newVal) => {
    if (newVal) {
      eventDescription.value = newVal.description || ''
      eventUei.value = newVal.uei || ''
      eventLabel.value = newVal.eventLabel || ''
      selectedEventStatus.value = {
        _text: newVal.enabled ? 'Enable' : 'Disable',
        _value: newVal.enabled ? 'enable' : 'disable'
      }
    } else {
      eventUei.value = ''
      eventLabel.value = ''
      eventDescription.value = ''
      selectedEventStatus.value = { _text: '', _value: '' }
    }
  }, { immediate: true, deep: true }
)
</script>

<style lang="scss" scoped>
@import "@featherds/table/scss/table";
@import "@featherds/styles/mixins/elevation";
@import "@featherds/styles/mixins/typography";
@import "@featherds/styles/themes/variables";

.feather-drawer-custom-padding {
  padding: 20px;
  height: 100%;
  overflow: auto;
}

.spacer-large {
  margin-bottom: 2rem;
}

.spacer-medium {
  margin-bottom: 0.5rem;
}

.footer {
  display: flex;
  padding-top: 20px;
}

button.primary {
  margin-top: 2rem;
  background-color: #1d2f75;
  color: white;
  padding: 0.5em 1.5em;
  border: none;
}

:deep(.feather-input-sub-text) {
  display: none;
}

.drawer-content {
  width: 70%;
  flex-direction: column;
  display: flex;
}

.textarea {
  padding: 5px;
}
</style>

