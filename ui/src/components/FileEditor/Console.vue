<template>
  <div :class="{ 'console': isConsoleOpen, 'console-minimized': !isConsoleOpen }">
    <div
      class="console-header"
      @click="setIsConsoleOpen(true)"
    >
      <div :class="{ 'icon-err': logErrors.length }">
        Console
        <FeatherIcon :icon="Error" />
      </div>
      <div
        class="btns"
        v-if="isConsoleOpen"
      >
        <div
          class="clear pointer"
          @click="clear"
        >
          Clear
        </div>
        &nbsp;
        <div
          class="min pointer"
          @click.stop="setIsConsoleOpen(false)"
        >
          Minimize
        </div>
      </div>
    </div>
    <div
      class="console-text"
      v-for="(log, index) in logs"
      :key="String(log.success) + String(index)"
    >
      <div :class="{ 'log-err': !log.success }">{{ log.msg }}</div>
    </div>
  </div>
</template>

<script
  setup
  lang="ts"
>
import { FeatherIcon } from '@featherds/icon'
import Error from '@featherds/icon/notification/Error'
import { useFileEditorStore } from '@/stores/fileEditorStore'
import { FileEditorResponseLog } from '@/types'

const fileEditorStore = useFileEditorStore()
const logs = computed(() => fileEditorStore.logs)
const logErrors = computed(() => logs.value.filter((log: FileEditorResponseLog) => !log.success))
const isConsoleOpen = computed(() => fileEditorStore.isConsoleOpen)

const setIsConsoleOpen = (isOpen: boolean) => fileEditorStore.setIsConsoleOpen(isOpen)
const clear = () => fileEditorStore.clearLogs()
</script>

<style
  scoped
  lang="scss"
>
@import "@featherds/styles/themes/variables";
@import url("https://fonts.googleapis.com/css2?family=Ubuntu+Mono&display=swap");

@mixin console {
  font-family: "Ubuntu Mono", monospace;
  border: 1px solid var($border-on-surface);
  border-radius: 1px;
  background: var($shade-4);
  color: var($primary-text-on-surface);
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
  // height: 20px;
  border-bottom: 1px solid var($secondary-variant);
  padding: 7px;
  .btns {
    display: flex;
    justify-content: flex-end;
    .clear,
    .min {
      margin-right: 10px;
    }
  }
  .icon-err {
    .feather-icon {
      color: var($error);
    }
  }
}
.console-text {
  margin-top: 5px;
  margin-left: 10px;
  .log-err {
    color: var($error);
  }
}
</style>
