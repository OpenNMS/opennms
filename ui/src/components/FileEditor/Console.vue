<template>
  <div :class="{'console': isConsoleOpen, 'console-minimized': !isConsoleOpen}">
    <div class="console-header" @click="setIsConsoleOpen(true)">
      <div>
        Console
        <FeatherIcon :icon="Error" />
      </div>
      <div class="btns" v-if="isConsoleOpen">
        <div class="clear pointer" @click="clear">Clear</div>&nbsp;
        <div class="min pointer" @click.stop="setIsConsoleOpen(false)">Minimize</div>
      </div>
    </div>
    <div class="console-text" v-for="log of logs" :key="log">{{ log }}</div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useStore } from 'vuex'
import { FeatherIcon } from '@featherds/icon'
import Error from "@featherds/icon/alert/Error"

const store = useStore()
const logs = computed(() => store.state.fileEditorModule.logs)
const isConsoleOpen = computed(() => store.state.fileEditorModule.isConsoleOpen)

const setIsConsoleOpen = (isOpen: boolean) => store.dispatch('fileEditorModule/setIsConsoleOpen', isOpen)
const clear = () => store.dispatch('fileEditorModule/clearLogs')
</script>

<style scoped lang="scss">
@import url('https://fonts.googleapis.com/css2?family=Ubuntu+Mono&display=swap');

@mixin console {
  font-family: 'Ubuntu Mono', monospace;
  border: 1px solid gray;
  border-radius: 1px;
  background: var(--feather-secondary-text-on-warning);
  color: var(--feather-primary-text-on-color);
  height: 250px;
  overflow-x: auto;
  transition: height 0.5s;
}
.console {
  @include console;
}
.console-minimized {
  @include console;
  height: 35px;
  overflow: hidden;
  cursor: pointer;
  transition: height 0.5s;
}
.console-header {
  display: flex;
  justify-content: space-between;
  height: 20px;
  border-bottom: 1px solid var(--feather-secondary-variant);
  padding: 7px;
  .btns {
    display: flex;
    justify-content: flex-end;
    .clear, .min {
      margin-right: 10px;
    }
  }
}

.console-text {
  margin-top: 5px;
  margin-left: 10px;
}
</style>
