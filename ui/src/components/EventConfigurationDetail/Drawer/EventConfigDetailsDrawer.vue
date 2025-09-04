<template>
  <FeatherDrawer
    id="column-selection-drawer"
    data-test="column-selection-drawer"
    @shown="() => eventConfigStore.drawerState.visible = true"
    @hidden="() => eventConfigStore.drawerState.visible = false"
    v-model="eventConfigStore.drawerState.visible"
    :labels="{ close: 'close', title: 'Customize Columns' }"
    width="55em"
  > 
    <div class="feather-drawer-custom-padding">
      <section>
        <h3>Customize the Event Details</h3>
        <!-- <p>Select which columns you wish to showcase</p> -->
      </section>
      <div class="spacer-large"></div>
      <div class="drawer-content">
        <FeatherInput
          label="Event Label"
          type="search"
          data-test="event-label"
          v-model="eventLabel"
        >
        </FeatherInput>
        <div class="spacer-medium"></div>
        <h4>Description</h4>
        <div class="spacer-medium"></div>
        <textarea
          v-model="eventDescription"
          placeholder="Type your event description here..."
          rows="5"
          cols="40"
          class="textarea"
        ></textarea>
        <div class="spacer-medium"></div>
        <h4>Status</h4>
        <div class="spacer-medium"></div>
        <FeatherSelect
          label="Select Alert Trigger"
          :options="mapTriggerTypeOptions()"
          v-model="selectedEventStatus"
          clear="Clear Selection"
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
        <FeatherButton secondary>Close</FeatherButton>
      </div>
    </div>
  </FeatherDrawer>
</template>

<script lang="ts" setup>
import { useEventConfigDetailStore } from '@/stores/eventConfigDetailStore'
import { FeatherButton } from '@featherds/button'
import { FeatherDrawer } from '@featherds/drawer'
import { FeatherIcon } from '@featherds/icon'
import { FeatherInput } from '@featherds/input'
import { FeatherSelect, ISelectItemType } from '@featherds/select'
import MoreVert from '@featherds/icon/navigation/MoreVert'
import { EventConfigEvent } from '@/types/eventConfig'

const eventConfigStore = useEventConfigDetailStore()
const eventDescription = ref('')
const eventLabel = ref('')
const selectedEventStatus = ref<ISelectItemType | undefined>()
const props = defineProps<{
  event: EventConfigEvent | null
}>()

const categoryOptions = [
  { label: 'Enable', value: 'enable' },
  { label: 'Disable ', value: 'disable' }
]

const mapTriggerTypeOptions = (): ISelectItemType[] => {
  return (categoryOptions ?? []).map((trigger) => ({
    _text: trigger.label,
    _value: trigger.value
  }))
}

const handleSave = () => {
  eventConfigStore.closeEventDrawerModal()
}

const setIntialEventInfo = (val: EventConfigEvent) => {
  eventDescription.value = val.description
  eventLabel.value = val.eventLabel
  selectedEventStatus.value = {
    _text: val.enabled ? 'Enable' : 'Disable',
    _value: val.enabled ? 'enable' : 'disable'
  }
}

watch(
  () => props.event,
  (val) => {
    if (val) {
      setIntialEventInfo(val)
    }
  },
  { immediate: true, deep: true }
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
    margin-bottom: 0.25rem;
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

.drawer-content    {
    width: 70%;
    flex-direction: column;
    display: flex;
}
.textarea{
    padding: 5px;
}
</style>