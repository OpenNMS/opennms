const getOrCreateLegendList = (id: string) => {
  const legendContainer = document.getElementById(id)

  if (!legendContainer) return

  let listContainer = legendContainer.querySelector('ul')

  if (!listContainer) {
    listContainer = document.createElement('ul')
    listContainer.style.display = 'inline-block'
    listContainer.style.margin = '0'
    listContainer.style.padding = '0'

    legendContainer.appendChild(listContainer)
  }

  return listContainer
}

const HtmlLegendPlugin = {
  id: 'htmlLegend',
  afterUpdate(chart: any, args: any, options: any) {
    const ul = getOrCreateLegendList(options.containerID)

    if (!ul) return

    // Remove old legend items
    while (ul.firstChild) {
      ul.firstChild.remove()
    }

    // Reuse the built-in legendItems generator
    const items = chart.options.plugins.legend.labels.generateLabels(chart)

    items.forEach((item: any) => {
      const li = document.createElement('li')
      li.style.alignItems = 'left'
      li.style.cursor = 'pointer'
      li.style.display = 'inline-block'
      li.style.marginLeft = '10px'

      li.onclick = () => {
        chart.setDatasetVisibility(item.datasetIndex, !chart.isDatasetVisible(item.datasetIndex))
        chart.update()
      }

      // Color box
      const boxSpan = document.createElement('span')
      boxSpan.style.background = item.fillStyle
      boxSpan.style.borderColor = item.strokeStyle
      boxSpan.style.borderWidth = item.lineWidth + 'px'
      boxSpan.style.display = 'inline-block'
      boxSpan.style.height = '15px'
      boxSpan.style.marginRight = '10px'
      boxSpan.style.marginBottom = '-2px'
      boxSpan.style.width = '15px'

      // Text
      const textContainer = document.createElement('p')
      textContainer.style.color = item.fontColor
      textContainer.style.margin = '0'
      textContainer.style.padding = '0'
      textContainer.style.textDecoration = item.hidden ? 'line-through' : ''
      textContainer.style.display = 'inline-block'
      textContainer.style.whiteSpace = 'pre'
      textContainer.innerText = item.text

      const linebreak = document.createElement('br')

      li.appendChild(boxSpan)
      li.appendChild(textContainer)
      ul.appendChild(li)
      if (item.text.split('\n')[1] !== undefined) {
        ul.append(linebreak)
      }
    })
  }
}

export default HtmlLegendPlugin
