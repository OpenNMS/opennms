describe('Directive - PagedTable', function() {
  var $q;
  var element;
  var compiled;
  var scope;

  beforeEach(module('opennms.directives.shared.pagedtable'));

  beforeEach(inject(function($rootScope, $compile) {
    scope = $rootScope.$new();
    element = angular.element(
      '<div class="test">' +
        '<table class="paged" model-name="outages">' +
          '<thead>' +
            '<th property="id">Id</th>' +
          '</thead>' +
        '</table>' +
      '</div>'
    );

    /*
<table>
  <thead>
    <th>Id</th>
  </thead>
  <tbody>
    <tr ng-repeat="outage in outages">
      <td>{{outage.id}}</td>
    </tr>
  </tbody>
</table>
    */

    compiled = $compile(element)(scope);
    element.scope().$digest();
  }));

  xdescribe('PagedTable.pt-th', function() {
    it('should replace the pt-th with a th', function() {
      var th = element.find('th');
      expect(th.length).toBe(1);
    });

    it('should store the model-property="id" in the scope', function() {
      var th = element.find('th');
      //console.log('modelProperties=',scope.modelProperties);
      expect(scope.modelProperties.length).toBe(1);
      expect(scope.modelProperties[0]).toBe('id');
    });
  });

  xdescribe('PagedTable.table', function() {
    it('should extract the model information from the table', function() {
      var pt = element.find('table');
      expect(pt.length).toBe(1);
      expect(scope.modelName).toBe('outages');
    });
  });
});
