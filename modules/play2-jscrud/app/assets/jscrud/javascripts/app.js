var app = angular.module('jscrud', ['schemaForm', 'ngRoute', 'ngResource', 'jsonFormatter', 'datatables', 'datatables.bootstrap', 'flash', 'oitozero.ngSweetAlert']);

app.constant('cfg', {
  jsonSchemaUrl: window.jsonSchemaUrl,
  restUrl: window.restUrl,
  columns: window.columns,
  dictionary: window.dictionary,
  baseAssetUrl: "../../jscrud/assets/"
});

app.factory('RestAPI', ['$resource', 'cfg', function($resource, cfg) {
  return $resource(cfg.restUrl, null, {
    'update': {
      method: 'PUT',
      params: {
        id: '@id'
      }
    }
  });
}]);

app.config(['$routeProvider', '$resourceProvider', 'cfg', function($routeProvider, $resourceProvider, cfg) {
//  var baseUrl = cfg.baseAssetUrl;

  $routeProvider.when('/', {
    templateUrl: '/itemList.html',
    controller: 'ItemListController'
  }).when('', {
    templateUrl: '/itemList.html',
    controller: 'ItemListController'
  }).when('/edit/:id', {
    templateUrl: '/itemDetail.html',
    controller: 'ItemDetailController'
  }).when('/new', {
    templateUrl: '/itemDetail.html',
    controller: 'ItemDetailController'
  }).otherwise({
    redirectTo: '/'
  });
  $resourceProvider.defaults.stripTrailingSlashes = false;

}]);

app.controller('ItemListController', ['$scope', 'cfg', 'RestAPI', 'DTOptionsBuilder', 'DTColumnBuilder', 'DTInstances', 'Flash', function($scope, cfg, RestAPI, DTOptionsBuilder, DTColumnBuilder, DTInstances, Flash) {
  $scope.items = RestAPI.query();

  function newPromiseFn() {
    return RestAPI.query().$promise;
  }

  $scope.dtOptions = DTOptionsBuilder.fromFnPromise(newPromiseFn).withOption('processing', true).withPaginationType('full_numbers').withBootstrap();

  var cols = [];

  var renderFunction =  function(dataId, type, full) {
        if (dataId)
          return cfg.dictionary[key][dataId];
        else
          return null;
      };

  for ( var key in cfg.columns) {
    if (cfg.dictionary[key] === null)
      cols.push(DTColumnBuilder.newColumn(key).withTitle(cfg.columns[key].title).withOption('defaultContent', '-'));
    else
      cols.push(DTColumnBuilder.newColumn(key).withTitle(cfg.columns[key].title).withOption('defaultContent', '-').renderWith(renderFunction));
  }

  cols.push(DTColumnBuilder.newColumn("action").withClass("width-xs").withTitle(" ").renderWith(function(data, type, full) {
    return '<a href="#/edit/' + full.id + '"><i class="fa fa-edit"></i></a>';
  }));
  $scope.dtColumns = cols;

  // $scope.prettyJs = function(js) {
  // return JSON.stringify(js, undefined, 1).trim();
  // }
}]);

app.controller('ItemDetailController', ['$scope', '$http', '$routeParams', 'RestAPI', '$location', 'cfg', 'Flash', 'SweetAlert', function($scope, $http, $routeParams, RestAPI, $location, cfg, Flash, SweetAlert) {
  $http.get(cfg.jsonSchemaUrl).success(function(data) {
    $scope.schema = data.schema;
    $scope.form = data.schemaForm;
  });

  $scope.schema = {};

  $scope.form = [];

  var itemId = $routeParams.id;

  if (itemId) {
    $scope.sectionTitle = "Edit Item";
    $scope.model = RestAPI.get({
      id: itemId
    }, function() {
      // good code
    }, function(response) {
      // 404 or bad
      if (response.status === 404) {
        $location.path('/');
      }
    });
  } else {
    $scope.sectionTitle = "Add Item";
    $scope.model = {};
  }

  $scope.onSave = function(form, model) {
    $scope.$broadcast('schemaFormValidate');

    if (form.$valid) {
      if ($scope.model.id) {
        RestAPI.update($scope.model, function(result) {
          Flash.create("success", "item updated!");
        });
      } else {
        RestAPI.save($scope.model, function(result) {
          Flash.create("success", "item saved!");
          $location.path('/edit/' + result.id);
        });
      }
    }
  };

  $scope.onDelete = function() {
    SweetAlert.swal({
      title: "Are you sure?",
      text: "Your will not be able to recover this item!",
      type: "warning",
      showCancelButton: true,
      confirmButtonColor: "#DD6B55",
      confirmButtonText: "Yes, delete it!",
      closeOnConfirm: true
    }, function(confirmDelete) {
      if (confirmDelete) {
        if ($scope.model.id) {
          RestAPI.remove($scope.model);
        }
        $location.path('/');
        Flash.create("danger", "item removed!");
      }
    });

  };

}]);
