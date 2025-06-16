<template>
  <Search />
  <div class="sidebar-relative-container">
    <div class="file-tools">
      <FeatherButton
        class="btn"
        :disabled="!selectedLog"
        icon="Scroll to selected log."
        @click="scrollToSelectedLog"
      >
        <FeatherIcon :icon="SupportCenter" />
      </FeatherButton>
    </div>
    <div class="logs-sidebar">
      <p
        :id="log === selectedLog ? 'selected' : ''"
        :class="{ 'selected': log === selectedLog }"
        class="pointer"
        v-for="(log, index) in logs"
        :key="log"
        @click="getLog(log)"
      >
        <span class="subtitle1">{{ Number(index) + 1 }}:&nbsp;</span>
        <span class="subtitle2">{{ log }}</span>
      </p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { FeatherIcon } from '@featherds/icon'
import { FeatherButton } from '@featherds/button'
import SupportCenter from '@featherds/icon/action/SupportCenter'
import Search from './Search.vue'
import { useLogStore } from '@/stores/logStore'

const logStore = useLogStore()
const selectedLog = computed(() => logStore.selectedLog)
const logs = computed(() => logStore.getFilteredLogs())
const getLog = (log: string) => logStore.getLog(log)

const scrollToSelectedLog = () => {
  const selected = document.getElementById('selected')
  if (selected) {
    selected.scrollIntoView({ behavior: 'smooth', block: 'center' })
  }
}
</script>

<style lang="scss" scoped>
@import "@featherds/styles/themes/variables";
.sidebar-relative-container {
  position: relative;

  .file-tools {
    position: sticky;
    width: 100%;
    height: 30px;
    background: var($shade-4);

    .btn {
      margin: 0px;
      float: right;
      height: 25px !important;
      width: 25px !important;
      min-width: 25px !important;
      margin-top: 2px;
      svg {
        font-size: 20px !important;
      }
    }
  }
  .logs-sidebar {
    overflow-y: scroll;
    overflow-x: hidden;
    height: calc(100vh - 212px);
    word-break: break-all;
    border: 1px solid var($border-on-surface);

    p {
      margin: 0px;
      padding: 5px;
      padding-left: 10px;
    }

    .selected {
      background: var($shade-3);
      span {
        color: var($primary);
      }
    }
  }
}
</style>
