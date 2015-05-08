var module = angular.module('my.resources', ['ngResource']);

module.factory('Resource', ['$resource', function($resource) {
  return function(url, params, methods) {
    var defaults = {
      update: {
        method: 'put',
        isArray: false
      },
      create: {
        method: 'post'
      }
    };

    methods = angular.extend(defaults, methods);

    var resource = $resource(url, params, methods);

    resource.prototype.$save = function() {
      if (!this.id) {
        this.$create();
      } else {
        this.$update();
      }
    };

    return resource;
  };
}]);

var app =
// injects my.resources to support PUT updates
angular.module("app", ["my.resources"])
// creates the Person factory backed by our autosource
// Please remark the url person/:id which will use transparently our CRUD
// AutoSource endpoints
.factory('WeChatProfile', ["$resource", function($resource) {
  return $resource('/api/wechatprofile/:id', {
    "id": "@id"
  }, {
    update: {
      method: 'PUT'
    }
  });
}])
// creates a controller
.controller("WeChatProfileCtrl", ["$scope", "WeChatProfile", function($scope, QueryAPI) {

  function refresh() {
    $scope.createForm = {
      isActive: true,
      profileType: "WeChat"
    };
  }
  
  refresh();

  // retrieves all persons
  $scope.items = QueryAPI.query();

  // creates a person using createForm and refreshes list
  $scope.create = function() {
    var item = new QueryAPI($scope.createForm);
    item.$save(function() {
      refresh();
      // $scope.createForm = {};
      $scope.items = QueryAPI.query();
    });
  };

  // removes a person and refreshes list
  $scope.remove = function(item) {
    item.$remove(function() {
      $scope.persons = QueryAPI.query();
    });
  };

  // updates a person and refreshes list
  $scope.update = function(item) {
    item.$update(function() {
      $scope.items = QueryAPI.query();
    });
  };
}]);