var getThenObject = function(response) {
  return { then: function(cb) { cb(response) } };
};

var getEmptyThenStub = function() {
  return getThenObject({});
};