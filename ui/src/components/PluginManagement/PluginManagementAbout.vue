<template>
  <div class="help-section" data-test="plugin-about">
    <div class="section-title">What a plugin is</div>
    <p>
      A plugin is a KAR file: an archive carrying Karaf features and the OSGi bundles they install,
      built against the OpenNMS Integration API. Loading it makes those features part of this server's
      container; the plugin then provides whatever it was built for, such as collectors, pollers,
      alarm consumers or user interface extensions.
    </p>
    <div class="section-title">Two ways to load</div>
    <p>
      <strong>From a repository</strong> offers a catalog of official plugins and reads the versions from the
      plugin's GitHub releases. The server downloads the chosen KAR itself and runs the same checks as for an
      uploaded file, so no file needs to pass through the browser. Downloads are only accepted from GitHub
      unless the server is configured otherwise; a GitHub token can be set as a server property to lift the
      unauthenticated rate limit, and the temporary downloads are cleaned up automatically.
    </p>
    <p>
      <strong>From a file</strong> uploads a KAR that was obtained elsewhere, for a plugin that is not in the
      catalog, a build of your own, or a server without access to GitHub.
    </p>
    <div class="section-title">What Load does</div>
    <ol>
      <li>Runs the checks on the downloaded or uploaded file: file structure, safe entries, the features file,
        the bundles, compatibility of the packages the bundles import with the packages this server exports,
        the Java version, and whether a plugin with the same name is already loaded. A failing check
        stops the load; a warning has to be acknowledged before loading.</li>
      <li>Writes a boot file under <code>featuresBoot.d</code> naming the features you ticked under
        <em>Features to start</em> (the catalog pre-selects the usual one for the plugins it lists; a KAR
        with several ways to run, such as standalone and distributed, needs you to pick), and records the
        plugin so it appears in the table with its checksum, its source and who loaded it.</li>
      <li>Moves the KAR into <code>{{ deployDir || 'deploy/' }}</code> and waits for the container to pick it up
        and start the features, for up to 30 seconds. The result dialog says whether they started; the boot file
        makes them start again on every later boot.</li>
    </ol>
    <p>Nothing is written until the checks have run and Load plugin is pressed.</p>
    <div class="section-title">When a plugin does not start</div>
    <p>
      A plugin shown as <em>Failed to start</em> was loaded, but one or more of its features did not start; the
      reason the container gave is in the status tag's tooltip and in the result dialog, and <code>karaf.log</code>
      has the details. Fix the cause, then use <em>Restart</em> on the plugin row, or unload the plugin.
      <em>Restart</em> stops and starts the plugin's features in the container: the plugin is unavailable for a few
      seconds and comes back in the same mode, and the dialog shows whether it started. No server restart is
      involved.
    </p>
    <div class="section-title">Events</div>
    <p>
      Every change of a plugin's state raises an OpenNMS event: <code>pluginStarted</code> when its features are
      running, <code>pluginFailed</code> when they did not start (this one raises an alarm that clears when the
      plugin starts), and <code>pluginStopped</code> when it is stopped or unloaded. Notifications and alarm
      handling apply to them as to any other event.
    </p>
    <div class="section-title">When a restart is needed</div>
    <p>
      A restart of OpenNMS is only needed when the container cannot start the plugin itself. A plugin whose
      manifest sets <code>Karaf-Feature-Start: false</code> is extracted right away but its features start on
      the next boot only; the same goes for a KAR loaded while the container was unavailable, or one the
      container did not pick up within 30 seconds. Such a plugin is shown as <em>Load pending restart</em> and the
      banner at the top of the page stays on until the server has restarted. An unloaded plugin is likewise
      removed completely at the next boot and is shown as <em>Unload pending restart</em> until then. A source of
      <em>Loaded by hand</em> marks a KAR installed outside this page (copied into the deploy directory or
      installed with <code>kar:install</code>); its features are read from the <code>featuresBoot.d</code> line
      that waits for it, or from the KAR itself when no boot file names it.
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
      Every check, load, restart and unload is written to <code>plugin-management.log</code> with the user who
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
