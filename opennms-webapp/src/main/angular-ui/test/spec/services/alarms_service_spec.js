describe('Shared Services Module - Alarms', function() {
  var rootScope;
  var AlarmService;

  var alarmsResponse;

  beforeEach(module('opennms.services.shared.alarms'));
  beforeEach(inject(function(_$rootScope_, _AlarmService_) {
    rootScope = _$rootScope_;
    AlarmService = _AlarmService_;

    alarmsResponse = [
      {
        _id: 30
      }
    ]
  }));

  describe('public functions', function() {
    describe('list', function() {
      it('should default the limit and offset', function() {
        var url = '';
        var handlerFn = function(alarmsUrl) {
          url = alarmsUrl;
        };
        AlarmService.internal.fetchAlarms = handlerFn;

        AlarmService.list();

        expect(url).toContain('limit=50');
        expect(url).toContain('offset=0');
      });
    });

    describe('getByNode', function() {
      it('should default the limit and offset', function() {
        var url = '';
        var handlerFn = function(alarmsUrl) {
          url = alarmsUrl;
        };
        AlarmService.internal.fetchAlarms = handlerFn;

        AlarmService.getByNode();

        expect(url).toContain('limit=50');
        expect(url).toContain('offset=0');
      });

      it('should add criteria for searching for the node', function() {
        var url = '';
        var handlerFn = function(alarmsUrl) {
          url = alarmsUrl;
        };
        AlarmService.internal.fetchAlarms = handlerFn;

        AlarmService.getByNode(1);
        expect(url).toContain('nodeId=1');
      });
    });

    describe('get', function() {
      it('should do something', function() {

      });
    });

    describe('summaries', function() {
      it('should retrieve the alarm URL', inject(function(_$httpBackend_) {
        _$httpBackend_.expectGET('/opennms/rest/alarms/summaries').respond(alarmsResponse);
        AlarmService.handlerFn = function() {};
        spyOn(AlarmService, 'handlerFn');
        AlarmService.internal.getAlarmSummarySuccessHandler = function(deferred) {
          return AlarmService.handlerFn;
        };


        AlarmService.summaries();
        _$httpBackend_.flush();
        expect(AlarmService.handlerFn).toHaveBeenCalled();
      }));
    });
  });


  describe('internal functions', function() {
    describe('fetchAlarms', function() {
      it('should retrieve the alarm URL', inject(function(_$httpBackend_) {
        _$httpBackend_.expectGET("/people/1234").respond(alarmsResponse);
        AlarmService.handlerFn = function() {};
        spyOn(AlarmService, 'handlerFn');
        AlarmService.internal.getAlarmListSuccessHandler = function() {
          return AlarmService.handlerFn;
        };


        AlarmService.internal.fetchAlarms("/people/1234");
        _$httpBackend_.flush();
        expect(AlarmService.handlerFn).toHaveBeenCalled();
      }));
    });

    describe('processAlarmListResults', function() {
      var alarms;
      beforeEach(function() { alarms = AlarmService.internal.processAlarmListResults(sampleAlarmsResults); })

      it('should return an array the same size as the results', function() {
        expect(alarms.length).toBe(sampleAlarmsResults.alarms.alarm.length);
      });

      it('should map the alarms to Alarm objects', function() {
        alarms.forEach(function(alarm) {
          expect(alarm.className).not.toBeUndefined();
          expect(alarm.className).toBe('Alarm');
        });
      });
    });
  });

});