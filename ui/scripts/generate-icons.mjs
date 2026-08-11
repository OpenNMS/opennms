/*
 * ONE-TIME icon vendoring generator (NMS-19981, Phase 6).
 *
 * Reads every compiled icon component from `@featherds/icon/<category>/<Name>.js`,
 * renders it to a static <svg> via Vue's server renderer, and writes a local,
 * self-contained template-only SFC to src/components/icons/<category>/<Name>.vue.
 *
 * This exists so the app can drop the `@featherds/icon` dependency entirely while
 * keeping pixel-identical icons (see src/components/icons/OnmsIcon.vue wrapper).
 *
 * NOTE: this is a one-shot script. Once `@featherds/icon` is removed from
 * package.json it can no longer run without a temporary reinstall. It is kept in
 * the repo only for provenance / regeneration if the dependency is restored.
 *
 * Run from the `ui/` directory:  node scripts/generate-icons.mjs
 */
import { readdirSync, mkdirSync, writeFileSync, existsSync } from 'fs'
import { createRequire } from 'module'
import { pathToFileURL } from 'url'
import { resolve, join, dirname } from 'path'

const req = createRequire(process.cwd() + '/')

// Resolve vue + @vue/server-renderer through the installed vue (pnpm-safe: the
// renderer lives under vue's own dependency scope, not the top level).
const vuePath = req.resolve('vue')
const { h } = await import(pathToFileURL(vuePath).href)
const srPath = createRequire(vuePath).resolve('@vue/server-renderer')
const { renderToString } = await import(pathToFileURL(srPath).href)

const iconBase = resolve('node_modules/@featherds/icon')
const outBase = resolve('src/components/icons')

// Category folders only (skip package internals).
const SKIP = new Set(['dist', 'src', 'scripts', 'node_modules'])

const categories = readdirSync(iconBase, { withFileTypes: true })
  .filter((d) => d.isDirectory() && !SKIP.has(d.name))
  .map((d) => d.name)
  .sort()

let total = 0
const perCategory = {}

for (const cat of categories) {
  const catDir = join(iconBase, cat)
  const files = readdirSync(catDir)
    .filter((f) => f.endsWith('.js') && !f.endsWith('.d.ts'))
    .sort()

  if (files.length === 0) {
    continue
  }

  mkdirSync(join(outBase, cat), { recursive: true })
  perCategory[cat] = 0

  for (const file of files) {
    const name = file.slice(0, -'.js'.length)
    const mod = await import(pathToFileURL(join(catDir, file)).href)
    const cmp = mod.default

    if (!cmp) {
      console.warn(`  skip ${cat}/${name}: no default export`)
      continue
    }

    const svg = (await renderToString(h(cmp))).trim()

    if (!svg.startsWith('<svg')) {
      console.warn(`  WARN ${cat}/${name}: rendered output does not start with <svg>: ${svg.slice(0, 40)}`)
    }

    const sfc = `<template>\n  ${svg}\n</template>\n`
    writeFileSync(join(outBase, cat, `${name}.vue`), sfc, 'utf8')
    total++
    perCategory[cat]++
  }
}

console.log('Generated icons by category:')
for (const cat of Object.keys(perCategory).sort()) {
  console.log(`  ${cat}: ${perCategory[cat]}`)
}
console.log(`Total: ${total} icons -> ${outBase}`)
