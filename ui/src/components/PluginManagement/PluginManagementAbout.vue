<template>
  <div class="help-section" data-test="plugin-about">
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
      <li>Writes a boot file under <code>featuresBoot.d</code> naming the features to start, and records
        the plugin so it appears in the table with its checksum and who loaded it.</li>
      <li>Moves the KAR into <code>{{ deployDir || 'deploy/' }}</code>. Unless its manifest sets
        <code>Karaf-Feature-Start: false</code>, the container picks it up within seconds and starts its
        features; the boot file makes them start again on every later boot.</li>
    </ol>
    <p>Nothing is written until the checks have run and Load plugin is pressed.</p>
    <div class="section-title">When a restart is needed</div>
    <p>
      A plugin whose manifest sets <code>Karaf-Feature-Start: false</code> is extracted right away but its
      features start on the next boot only; it is shown as <em>Load pending restart</em> and the banner at
      the top of the page stays on until the server has restarted. An unloaded plugin is likewise removed
      completely at the next boot and is shown as <em>Unload pending restart</em> until then. A plugin shown
      as <em>Failed to start</em> was loaded, but one or more of its features did not start after a restart;
      <code>karaf.log</code> has the reason. <em>Not managed here</em> marks a KAR found in the deploy
      directory that was not loaded through this page.
    </p>
  </div>
  <div class="help-section">
    <div class="section-title">What Unload does</div>
    <p>
      Removes the KAR from <code>{{ deployDir || 'deploy/' }}</code>, deletes its boot file and drops any
      other <code>featuresBoot.d</code> line that waits for the KAR. The container stops the plugin's
      features right away; a restart completes the removal, and until then the plugin is shown as
      <em>Unload pending restart</em>.
    </p>
    <div class="section-title">Restarting OpenNMS</div>
    <RestartCommands v-if="restartInstructions" :instructions="restartInstructions" />
    <p v-else data-test="about-no-instructions">
      The restart instructions could not be read from the server. Restart the OpenNMS service the way
      it was installed, then check that the web interface answers again.
    </p>
    <div class="section-title">Audit log and access</div>
    <p>
      Every check, load and unload is written to <code>plugin-management.log</code> with the user who
      requested it and the outcome; the Activity log card shows the most recent entries and can save the
      whole file. Only administrators can open this page and use its actions.
    </p>
  </div>
</template>

<script setup lang="ts">
import { RestartInstructions } from '@/types/pluginManagement'
import RestartCommands from './RestartCommands.vue'

defineProps<{
  restartInstructions: RestartInstructions | null
  deployDir?: string
}>()
</script>

<style lang="scss" scoped>
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
