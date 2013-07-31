  if (window.top != window.self) { 
    document.onclick = function(e) {
                        if (parent.resetIdle != null)
                            parent.resetIdle();
                     }

    document.onkeypress = function(e) {
                        if (parent.resetIdle != null)
                            parent.resetIdle();
                     }

    document.onmousemove = function(e) {
                        if (parent.resetIdle != null)
                            parent.resetIdle();
                     }
  }
