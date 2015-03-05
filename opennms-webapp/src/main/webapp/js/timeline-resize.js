$(document).ready(function() {
  var e = $('#availability-box');
  var getSize = function(element) {
    var relativeSize = 0.5
    var container = element.closest('div'); // This is the panel, not the cell that contains the IMG
    return Math.round(container.width() * relativeSize);
  }
  // Update the timeline headers
  var imgs = e.find('img');
  for (var i=0; i < imgs.length; i++) {
    var img = $(imgs[i]);
    var w = getSize(img);
    var imgsrc = img.data('imgsrc') + w;
    img.attr('src', imgsrc);
  }
  // Update the timeline html/images
  var spans = e.find('span');
  for (var i=0; i < spans.length; i++) {
    var span = $(spans[i]);
    var w = getSize(span);
    var htmlsrc = span.data('src') + w;
    span.load(htmlsrc);
  }
});
