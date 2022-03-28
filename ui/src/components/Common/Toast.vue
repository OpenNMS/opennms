<template>
  <div class="toast" :class="getToastClass()">
    <div class="message">
      <div class="icon">
        <FeatherIcon
          class="icon-inner"
          :icon="toastMessage.hasError ? Edit : CheckCircle"
          :class="getIconClass()"
        />
      </div>
      <div class="basic">{{ toastMessage.basic }}</div>
      <div class="detail">{{ toastMessage.detail }}</div>
      <div class="close">
        <FeatherButton icon="Cancel" text @click="closeToast">
          <FeatherIcon class="close-icon" :icon="Cancel" />
        </FeatherButton>
      </div>
    </div>
  </div>
</template>
<script lang="ts" setup>
import { useStore } from 'vuex'
import { FeatherIcon } from '@featherds/icon'
import { FeatherButton } from '@featherds/button'
import Edit from '@featherds/icon/action/Edit'
import CheckCircle from '@featherds/icon/action/CheckCircle'
import Cancel from '@featherds/icon/navigation/Cancel'
const store = useStore()
/**
 * Local State
 */
const toastMessage = computed(() => store.state.notificationModule.toast)
const toastState = reactive({ isOpen: false, toastTimeout: 0 })
watch(toastMessage, () => {
  toastState.isOpen = true
  clearTimeout(toastState.toastTimeout)
  toastState.toastTimeout = window.setTimeout(() => {
    toastState.isOpen = false
  }, 5000)
})
/**
 * Cloase the Toast Message
 */
const closeToast = () => {
  toastState.isOpen = false
  clearTimeout(toastState.toastTimeout)
}
/**
 * Helper method to get all the toast classes in a clean way
 */
const getToastClass = () => {
  let classes = ''
  if (toastState.isOpen) {
    classes += 'toast-open '
  }
  if (toastMessage.value.hasErrors) {
    classes += 'toast-errors '
  }
  return classes
}
/**
 * Helper method to get all the icon class in a clean way.
 */
const getIconClass = () => {
  return toastMessage.value.hasErrors ? 'icon-errors ' : ''
}
</script>
<style lang="scss" scoped>
@import "@featherds/styles/mixins/typography";
@import "@featherds/styles/mixins/elevation";
.icon-inner {
  color: var($success);
  font-size: 32px;
}
.toast {
  @include elevation(6);
  color: var($surface);
  max-width: 1154px;
  height: 56px;
  background-color: var($secondary);
  position: fixed;
  bottom: 8px;
  left: 320px;
  width: 50%;
  border-radius: 5px;
  transform: translateY(76px);
  transition: transform ease-in-out 0.3s, opacity ease-in-out 0.2s;
  opacity: 0;
  z-index: 5;
}
.toast-open {
  opacity: 1;
  transform: translateY(0px);
}
.toast-errors {
  background-color: var($error);
}
.message {
  @include body-large();
  display: flex;
  align-items: center;
  height: 100%;
  padding: 0 20px;
  width: 100%;
}
.basic {
  font-weight: 700;
  color: var($surface);
  margin-left: 8px;
}
.detail {
  color: var($background);
  margin-left: 8px;
}
.close {
  margin-right: 25px;
  margin-left: auto;
}
.close-icon {
  color: var($surface);
}
.icon-errors {
  color: black;
}
</style>
