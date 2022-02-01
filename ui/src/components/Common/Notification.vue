<template>
  <transition name="fade">
    <div class="positioning-div" v-if="notification.msg">
      <div class="notification" :class="notification.severity">
        {{ notification.msg }}
        <FeatherButton
          icon="Close message."
          @click="closeMessage"
          class="close-btn"
          :class="notification.severity"
        >
          <FeatherIcon :icon="Close" />
        </FeatherButton>
      </div>
    </div>
  </transition>
</template>

<script setup lang=ts>
import { computed } from 'vue'
import { useStore } from 'vuex'
import { FeatherIcon } from '@featherds/icon'
import { FeatherButton } from '@featherds/button'
import Close from '@featherds/icon/navigation/Cancel'
import { NotificationSeverity } from '@/types'
const store = useStore()
const notification = computed(() => store.state.notificationModule.notification)
const closeMessage = () => store.dispatch('notificationModule/setNotification', { msg: '', severity: NotificationSeverity.ERROR }) // default
</script>

<style lang="scss" scoped>
@import "@featherds/styles/mixins/elevation";
@import "@featherds/styles/mixins/typography";
.positioning-div {
  position: absolute;
  left: 50%;
  top: 20px;
  z-index: 1030;
}
.notification {
  @include subtitle1;
  position: relative;
  left: -50%;
  min-width: 460px;
  max-width: 1000px;
  padding: 10px;
  padding-left: 20px;
  text-align: center;
}
.error {
  background: var($error);
  color: var($primary-text-on-color) !important;
}
.warning {
  background: var($warning);
  color: var($primary-text-on-warning) !important;
}
.success {
  background: var($success);
  color: var($primary-text-on-color) !important;
}
.close-btn {
  margin: 0;
}
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.5s;
}
.fade-enter,
.fade-leave-to {
  opacity: 0;
}
</style>
