<script lang="ts" setup>
import { computed, reactive, watch } from 'vue'
import { useStore } from 'vuex'
import { FeatherIcon } from '@featherds/icon'
import { FeatherButton } from '@featherds/button'
import Edit from '@featherds/icon/action/Edit'
import CheckCircle from '@featherds/icon/action/CheckCircle'
import Cancel from '@featherds/icon/navigation/Cancel'

const store = useStore()
const toastMessage = computed(() => store?.state?.configuration?.toast)
const editIcon = computed(() => Edit)
const successIcon = computed(() => CheckCircle)
const cancelIcon = computed(() => Cancel)
const toastState = reactive({ isOpen: false, toastTimeout: 0 })
const closeToast = () => {
  toastState.isOpen = false
  clearTimeout(toastState.toastTimeout)
}
watch(toastMessage, () => {
  toastState.isOpen = true
  clearTimeout(toastState.toastTimeout)
  toastState.toastTimeout = window.setTimeout(() => {
    toastState.isOpen = false
  }, 5000)
})
const getToastClass = () => {
  let classes = ''
  if (toastState.isOpen) {
    classes += 'config-toast-open '
  }
  if (toastMessage?.value?.hasErrors) {
    classes += 'config-toast-errors '
  }
  return classes
}
const getIconClass = () => {
  let classes = ''
  if (toastMessage?.value?.hasErrors) {
    classes += 'config-icon-errors '
  }
  return classes
}
</script>
<template>
  <div class="config-toast" :class="getToastClass()">
    <div class="message">
      <div class="icon">
        <FeatherIcon
          class="icon-inner"
          :icon="toastMessage?.hasError ? editIcon : successIcon"
          :class="getIconClass()"
        />
      </div>
      <div class="basic">
        {{toastMessage?.basic}}
      </div>
      <div class="detail">
        {{toastMessage?.detail}}
      </div>
      <div class="close">
        <FeatherButton icon="Cancel" text @click="closeToast">
          <FeatherIcon class="close-icon" :icon="cancelIcon" />
        </FeatherButton>
      </div>
    </div>
  </div>
</template>
<style lang="scss" scoped>
@import '@featherds/styles/mixins/typography';
@import '@featherds/styles/mixins/elevation';
.icon-inner {
  color: var(--feather-success);
  font-size: 32px;
}
.config-toast {
  @include elevation(6);
  color: var(--feather-surface);
  max-width: 1154px;
  height: 56px;
  background-color: var(--feather-secondary);
  position: fixed;
  bottom: 8px;
  left: 320px;
  width: 100%;
  border-radius: 5px;
  transform: translateY(76px);
  transition: transform ease-in-out 0.3s, opacity ease-in-out 0.2s;
  opacity: 0;
  z-index: 5;
}
.config-toast-open {
  opacity: 1;
  transform: translateY(0px);
}
.config-toast-errors {
  background-color: var(--feather-error);
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
  color: var(--feather-surface);
  margin-left: 8px;
}
.detail {
  color: var(--feather-background);
  margin-left: 8px;
}
.close {
  margin-left: auto;
}
.close-icon {
  color: var(--feather-surface);
}
.config-icon-errors {
  color: black;
}
</style>