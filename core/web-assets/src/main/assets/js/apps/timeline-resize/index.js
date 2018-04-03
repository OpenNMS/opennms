const RELATIVE_SIZE = 0.5;

const getSize = function(element) {
  const container = element.closest('div'); // This is the panel, not the cell that contains the IMG
  return Math.round(container.width() * RELATIVE_SIZE);
}

$(document).ready(function() {
  const e = $('#availability-box');
  // Update the timeline headers
  const imgs = e.find('img');
  for (let i=0; i < imgs.length; i++) {
    const img = $(imgs[i]);
    const w = getSize(img);
    const imgsrc = img.data('imgsrc') + w;
    img.attr('src', imgsrc);
  }
  // Update the timeline html/images
  const spans = e.find('span');
  for (let i=0; i < spans.length; i++) {
    const span = $(spans[i]);
    const w = getSize(span);
    const htmlsrc = span.data('src') + w;
    span.load(htmlsrc);
  }
});
