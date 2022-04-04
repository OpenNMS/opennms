import { styles, icons } from './icons'

const ICON_PATHS: Record<string, string> = {}
const parser = new DOMParser()
const serializer = new XMLSerializer()

// get dom element reference to svg icons
const doc = parser.parseFromString(icons, 'image/svg+xml')
// get all icons with a unique id
const iconArray = doc.querySelectorAll('[id]')

iconArray.forEach(icon => {
  // serialize icon
  const iconStr = serializer.serializeToString(icon)
  // append styles and closing tag
  const svgStr = `${styles}${iconStr}</svg>`
  // create icon data path
  ICON_PATHS[icon.id] = 'data:image/svg+xml;base64,' + window.btoa(svgStr)
})

console.log(ICON_PATHS)

export default ICON_PATHS
