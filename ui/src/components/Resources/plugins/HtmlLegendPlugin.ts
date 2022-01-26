const getOrCreateLegendList = (id: string) => {
  const legendContainer = document.getElementById(id)

  if (!legendContainer) return

  let listContainer = legendContainer.querySelector('ul')

  if (!listContainer) {
    listContainer = document.createElement('ul')
    listContainer.style.display = 'flex'
    listContainer.style.flexDirection = 'column'
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
      li.style.display = 'flex'
      li.style.flexDirection = 'row'
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
      boxSpan.style.marginTop = '4px'
      boxSpan.style.width = '15px'

      // Text
      const textContainer = document.createElement('p')
      textContainer.style.color = item.fontColor
      textContainer.style.margin = '0'
      textContainer.style.padding = '0'
      textContainer.style.textDecoration = item.hidden ? 'line-through' : ''
      textContainer.innerHTML = item.text

      li.appendChild(boxSpan)
      li.appendChild(textContainer)
      ul.appendChild(li)
    })
  }
}

export default HtmlLegendPlugin
