<template>
  <TogglePanel
    :collapsed="collapsed"
    class="plugin-about-panel"
    data-test="plugin-about-panel"
    @update:collapsed="(value: boolean) => (collapsed = value)"
  >
    <template #header>
      <span class="panel-header">
        <i class="pi pi-question-circle" aria-hidden="true" />
        About plugin management
      </span>
    </template>
    <div class="help-columns">
      <div class="help-section">
        <div class="section-title">What a plugin is</div>
        <p>
          A plugin is a KAR file: an archive carrying Karaf features and the OSGi bundles they install,
          built against the OpenNMS Integration API. Loading it makes those features part of this server's
          container; the plugin then provides whatever it was built for, such as collectors, pollers,
          alarm consumers or user interface extensions.
        </p>
        <div class="section-title">What Load does</div>
        <ol>
          <li>Uploads the file and runs the checks: file structure, safe entries, the features file, the
            bundles, compatibility of the packages the bundles import with the packages this server exports,
            the Java version, and whether a plugin with the same name is already loaded. A failing check
            stops the load; a warning has to be acknowledged before loading.</li>
          <li>Stages the KAR into <code>{{ deployDir || 'deploy/' }}</code>, where the container picks it up.</li>
          <li>Writes a boot file under <code>featuresBoot.d</code> naming the features to start.</li>
          <li>Records the plugin, so it appears in the table below with its checksum and who loaded it.</li>
        </ol>
        <p>Nothing is written until the checks have run and Load plugin is pressed.</p>
        <div class="section-title">Why a restart is still required</div>
        <p>
          The boot file is read only at startup, so the features it names start on the next restart. Until
          then the plugin is shown as staged and the banner at the top of the page stays on.
        </p>
      </div>
      <div class="help-section">
        <div class="section-title">What Unload does</div>
        <p>
          Removes the KAR from <code>{{ deployDir || 'deploy/' }}</code> and deletes its boot file. The
          container stops the plugin's features right away; a restart completes the removal, and until
          then the plugin is shown as unloaded.
        </p>
        <div class="section-title">Restarting OpenNMS</div>
        <RestartCommands v-if="restartInstructions" :instructions="restartInstructions" />
        <p v-else data-test="about-no-instructions">
          The restart instructions could not be read from the server. Restart the OpenNMS service the way
          it was installed, then check that the web interface answers again.
        </p>
        <div class="section-title">Audit log and access</div>
        <p>
          Every load and unload is written to <code>plugin-management.log</code> with the user who requested
          it and the outcome; the Activity log card shows the most recent entries. Only administrators can
          open this page and use its actions.
        </p>
      </div>
    </div>
  </TogglePanel>
</template>

<script setup lang="ts">
import { ref } from 'vue'

import TogglePanel from '@/components/Common/TogglePanel.vue'
import { RestartInstructions } from '@/types/pluginManagement'
import RestartCommands from './RestartCommands.vue'

defineProps<{
  restartInstructions: RestartInstructions | null
  deployDir?: string
}>()

const collapsed = ref(true)
</script>

<style lang="scss" scoped>
.panel-header {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  font-weight: 600;

  .pi-question-circle {
    color: var(--p-primary-color);
  }
}

.help-columns {
  display: flex;
  gap: 2.5rem;
  flex-wrap: wrap;

  .help-section {
    flex: 1;
    min-width: 320px;
  }
}

.section-title {
  font-size: 1rem;
  font-weight: 600;
  margin: 0.75rem 0 0.5rem 0;

  &:first-child {
    margin-top: 0;
  }
}

p,
ol {
  margin: 0 0 0.75rem 0;
  font-size: 0.9rem;
  line-height: 1.5;
}

ol {
  padding-left: 1.25rem;
}
</style>
