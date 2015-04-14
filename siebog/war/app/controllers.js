siebog.controller('AppCtrl', [function() {

}])

.controller('BodyCtrl', ['$scope', '$modal', '$http', function($scope, $modal, $http) {
        //$scope.agents = [{"module":"siebog","ejbName":"Annotator"},{"module":"siebog","ejbName":"Ant"},{"module":"siebog","ejbName":"ClientServerAgent"},{"module":"siebog","ejbName":"ConcurrentReceiver"},{"module":"siebog","ejbName":"Learner"},{"module":"siebog","ejbName":"LoadBalanced"},{"module":"siebog","ejbName":"Map"},{"module":"siebog","ejbName":"Particle"},{"module":"siebog","ejbName":"Ping"},{"module":"siebog","ejbName":"Pong"},{"module":"siebog","ejbName":"RadigostAgent"},{"module":"siebog","ejbName":"RadigostStub"},{"module":"siebog","ejbName":"Receiver"},{"module":"siebog","ejbName":"Resolver"},{"module":"siebog","ejbName":"Sender"},{"module":"siebog","ejbName":"Swarm"}];
		$scope.status = {'open':true, 'open2':true};
	
        $http.get('/siebog/rest/agents/classes').
            success(function(data) {
                $scope.agents = data;
            });
        //$scope.performatives = ["ACCEPT_PROPOSAL","AGREE","CANCEL","CALL_FOR_PROPOSAL","CONFIRM","DISCONFIRM","FAILURE","INFORM","INFORM_IF","INFORM_REF","NOT_UNDERSTOOD","PROPAGATE","PROPOSE","PROXY","QUERY_IF","QUERY_REF","REFUSE","REJECT_PROPOSAL","REQUEST","REQUEST_WHEN","REQUEST_WHENEVER","SUBSCRIBE"];
        
        $http.get('/siebog/rest/messages').
            success(function(data) {
                $scope.performatives = data;
            });
        $scope.createdAgents = [];
        $http.get('/siebog/rest/agents/running').
	        success(function(data) {
	        	if (data != '')
	        		$scope.createdAgents = data;
	        });
        $scope.request = {receivers: []};

        $scope.newAgent = function (agent) {
            var modalInstance = $modal.open({
                templateUrl: 'partials/modal.tpl.html',
                controller: 'ModalInstanceCtrl',
                size: 'sm',
                resolve: {
                    agent: function () {
                        return agent;
                    }
                }
            });

            modalInstance.result.then(function (selectedItem) {
            	var req = {
            			 method: 'PUT',
            			 url: '/siebog/rest/agents/running/'+selectedItem.agClass['module']+'$'+selectedItem.agClass['ejbName']+'/'+selectedItem['name'],
            			 headers: {
            			   'Content-Type': 'application/x-www-form-urlencoded'
            			 },
            			 data: {},
            			}

            	$http(req).success(function(data) {
            		var found = false;
            		for (var aid in $scope.createdAgents) {
            			var agent = $scope.createdAgents[aid];
            			if (agent.str == data.str) {
            				found = true;
            				break;
            			}
            		}
            		if (!found) {
            			$scope.createdAgents = $scope.createdAgents.concat(data);
            		}
                });
            });
        };

        $scope.sendMessage = function() {
        	var req = {
       			 method: 'POST',
       			 url: '/siebog/rest/messages',
       			 headers: {
       			   'Content-Type': 'application/json'
       			 },
       			 data: $scope.request
       			}
        	$http(req).success(function(data, status) {
        		console.log("USPEH");
        		console.log(data);
        	});
        };
}])

.controller('ModalInstanceCtrl', function ($scope, $modalInstance, $timeout, agent) {
        $scope.agent = agent;
        $scope.agentName = '';
        
        $timeout(function () {
        	var el = document.getElementById("agentName");
        	el.focus();
        }, 200);
        
        $scope.ok = function () {
            var agentInstance = {'name':$scope.agentName,'host':"xjaf", 'agClass':agent};
            $modalInstance.close(agentInstance);
        };

        $scope.cancel = function () {
            $modalInstance.dismiss('cancel');
        };
        
        $scope.keyFn = function (event) {
        	if (event.keyCode == 13) {
        		 var agentInstance = {'name':$scope.agentName,'host':"xjaf", 'agClass':agent};
                 $modalInstance.close(agentInstance);
        	}
        }

});