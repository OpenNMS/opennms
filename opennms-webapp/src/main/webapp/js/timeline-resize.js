$(document).ready(function() {
  var e = $('#availability-box');
  var imgs = e.find('img');
  for (var i=0; i < imgs.length; i++) {
    var img = $(imgs[i]);
    var container = img.closest('div'); // This is the panel, not the cell that contains the IMG
    var w = Math.round(container.width() * 0.5);
    var imgsrc = img.data('imgsrc') + w;
    img.attr('src', imgsrc);
  }
});
