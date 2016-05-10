<%@tag description="Persist Topology" pageEncoding="UTF-8"%>


	
<div class="modal fade" id="ServiceTempEnrDiag">
	<div class="modal-dialog">
	<div class="modal-content">
			<div class="modal-header">
				<h4 class="modal-title">Add Service Template</h4>
			</div>
			
			<div class="model-body">
				<form id="addComponentInstanceFormEnr" enctype="multipart/form-data">
			<%-- we send namespace + name to server. There, the ID is generated out of the name --%>
			<fieldset>
				
				<div class="form-group">
					<label for="nameOfNewCI" class="control-label">Name</label>
					<input class="form-control" name="name" id="nameOfNewCI" type="text" required="required" />
				</div>
				<div class="form-group">
					<label for="namespace" class="control-label">Namespace</label>
					<input class="form-control" name="namespace" id="namespace" type="text" required="required" readonly  />
				</div>
					
			</fieldset>

		</form>
		<input type="hidden" id="namespaceCod" />
	</div>
	<div class="modal-footer">
		
		<button type="button" class="btn btn-primary" onclick="addComponentInstanceEnr();">Save Topology</button>		
		<button id="persistBtn" type="button" class="btn btn-primary" onclick="winery.events.fire(winery.events.name.command.PERSISTKNOWLEDGE);" disabled>Persist Knowledge</button>
		<button type="button" class="btn btn-default" data-dismiss="modal">Cancel</button>
	</div>
			
			
		</div>
	</div>
</div>




<script>
$("#ServiceTempEnrDiag").on("shown.bs.modal", function() {
	//$("#testWorkload").focus();
});

function showServiceTempEnrDiag() {
	
	$("#ServiceTempEnrDiag").modal("show");
	$("#namespace").val($("#namespaceNameDec").val());
	$("#namespaceCod").val($("#namespaceName").val());
	//$("#namespace").attr('disabled','disabled');
	
}

/*
 * Registering the function and the values of the URLs of Winery
 * The schema that Winery uses was followed
 */
function registerNewMUFunction(){
	var urlMuTopology="http://localhost:8080/winery/servicetemplates/";
	require(["winery-topologymodeler-AMD"], function(wt) {
		winery.events.register(winery.events.name.command.SAVENEWMU, wt.saveNewMU);
		wt.setTopologyTemplateURLMU(urlMuTopology+$("#namespaceCod").val()+"/"+$("#nameOfNewCI").val()+"/topologytemplate/");
		//wt.setTopologyTemplateURLMU(urlMuTopology+$("#namespace").val()+"/testigui/topologytemplate/");
	});
	return true;
}

/*
 * Registering the function and the values of the URLs of Winery
 * The schema that Winery uses was followed
 */
function registerURLSolutions(){
	var urlMuTopology="http://localhost:8080/winery/servicetemplates/";
	//var urlToRegisterIDMU= 'http://localhost:8080/winery/';
	require(["winery-topologymodeler-AMD"], function(wt) {
		winery.events.register(winery.events.name.command.PERSISTKNOWLEDGE, wt.persistKnowledge);	
		wt.setNameMU($("#nameOfNewCI").val());
		//wt.setURLViewMU(urlMuTopology+$("#namespaceCod").val()+"/"+$("#nameOfNewCI").val()+"/topologytemplate/?view");
		wt.setURLViewMU("/winery/servicetemplates/"+$("#namespaceCod").val()+"/"+$("#nameOfNewCI").val()+"/topologytemplate/?view");
		//alert("/winery/servicetemplates/"+$("#namespaceCod").val()+"/"+$("#nameOfNewCI").val()+"/topologytemplate/?view");
		//wt.setURLDistMU($("#namespaceCod").val());
		//alert(($("#namespaceCod").val()));
		wt.setURLDistMU($("#namespace").val());
	});
}

/*
 * Making the requirement that creates a new topology in the winery repo
 */
function addComponentInstanceEnr(){
	var dataToSend = $('#addComponentInstanceFormEnr').serialize();
	var urlMuTopology="http://localhost:8080/winery/servicetemplates/";
	var urlToRegisterID= 'http://localhost:8080/winery-topologymodeler/?repositoryURL=http%3A%2F%2Flocalhost%3A8080%2Fwinery%2F&ns=';
	registerNewMUFunction();
	registerURLSolutions();
	
	$.ajax({
		type: "POST",
		//async: false,
		"data": dataToSend,
		url:urlMuTopology,
		dataType: "text",
		error: function(jqXHR, textStatus, errorThrown) {
			vShowAJAXError("Could not add to repository", jqXHR, errorThrown);
		},
		success: function(resData, textStatus, jqXHR) {
			//var loc = jqXHR.getResponseHeader('Location');
			//window.open(loc, "_blank");
			$.ajax({
				type: "GET",
				url:urlToRegisterID+$("#namespaceCod").val()+'&id='+$("#nameOfNewCI").val(),				
				
				error: function(jqXHR, textStatus, errorThrown) {
					vShowAJAXError("Could not add to repository", jqXHR, errorThrown);
				},
				success: function(resData, textStatus, jqXHR) {
					winery.events.fire(winery.events.name.command.SAVENEWMU);					
					vShowSuccess("Successfully added to repository, check the topology under the name you have typed");	
					$('#persistBtn').removeAttr('disabled');	
				}
			});			
		}
	});	
}

function sleep(milliseconds) {
	  var start = new Date().getTime();
	  for (var i = 0; i < 1e7; i++) {
	    if ((new Date().getTime() - start) > milliseconds){
	      break;
	    }
	  }
	}
</script>
		