<%@tag description="Nefolog Queries from Refinement Interface" pageEncoding="UTF-8"%>

<div class="modal fade" id="constraintsForm">
	<div class="modal-dialog">
	<div class="modal-content">
			<div class="modal-header">
				<h4 class="modal-title">App profile and Hard Constraints</h4>
			</div>
			
			<div class="model-body">
			   	<form id="addConstraintsForm" enctype="multipart/form-data">
					<fieldset>
						<div class="cell col-xs-4">
							<label for="configid" class="control-label">Candidate Offerings:</label>
					      	
							<select id="configid" class="form-control">
										  
						    </select> 
						</div>
					
						<div class="cell col-xs-4">
						<label for="location_zone" class="control-label">Location Zone</label>
				      	
						<select id="location_zone" class="form-control">
								 <option value="-">------</option>
								 <optgroup label="North America (US)">
									  <option value="NY">NY</option>
									  <option value="Oregon">Oregon</option>
									  <option value="N.California">N.California</option>
									  <option value="LA">LA</option>
									  <option value="Dules">Dulles</option>
									  <option value="Dallas">Dallas</option>
									  <option value="Chicago">Chicago</option>	
								  </optgroup>
								  <optgroup label="Latin America">
								  	<option value="Sao Paulo">Sao Paulo</option>
								  </optgroup>
								  <optgroup label="EU">
									  <option value="Ireland">Ireland</option>
									  <option value="London">London</option>
								  </optgroup>
								  <optgroup label="Asia Pacific">
									  <option value="Singapore">Singapore</option>	
									  <option value="Tokyp">Tokyo</option>
									  <option value="Sydney">Sydney</option>
									  <option value="HongKong">Hong Kong</option>	
								  </optgroup>		  
					    </select> 
					</div>
					<div class="cell col-xs-4" id="parameter_options">
						<label for="" class="control-label">Parameter</label>
					 	<select id="parameter" class="form-control">
					 		<option value="Queries">Queries per month</option>
					 		<option value="Account">Number of accounts</option>
					 		<option value="Channel">Opened channels</option>
					 		<option value="Server">Number of servers</option>
					 		<option value="Instance">Number of instances</option>
					 		<option value="Month">Number of months</option>
					 		<option value="ReportsPerHour">Reports per hour</option>
					 		<option value="App">Apps per month</option>
					 		<option value="Messages">Messages per month</option>
					 		<option value="NuberOfCaching">Caching operations per month</option>
					 		<option value="NuberOfVirtualIP">IPs per month</option>
					 		<option value="MetricsPerInstance">Metrics per instance</option>
					 		<option value="AlarmsPerInstance">Alarms per instance</option>
					 		<option value="SSLCapabilities">SSL capabilities per month</option>
					 		<option value="DomainNameRegistration">Domain name registration per month</option>
					 		<option value="NumberOfUsableIPaddresses">Usable IP addresses per month</option>
					 		<option value="NumberOfFirewalledIPaddresses">Firewalled IP address per month</option>
					 		
					 	</select>
					 	<input type="text" />
					 	<button id= "addNefologBtn" onclick="return addNefologParameters();">Add</button>
					</div>
							
					
					</fieldset>
				</form>
			</div>
			
			<div class="model-body">	
			<table class="table table-striped">
					 	<thead>
						      <tr>
						        <th>   </th>
						        <th>Parameter</th>
						        <th>Value</th>
						        <th></th>
						        
						      </tr>
						    </thead>
						    <tbody id="costparameter-list"></tbody>   					      
					 </table>		
				
			 </div>
			 
			<div class="modal-header">
				<h4 class="modal-title">Estimated Cost:</h4>
			</div>
			<div style="margin-left:20px" id="cost_info"> 
			</div>
			
			<div class="modal-footer">
					<button type="button"  class="btn btn-primary" onclick="calculateCost();">
					Estimate Cost</button>
					<button type="button" class="btn btn-default" data-dismiss="modal">Cancel</button>
			</div>			
			
		</div>
	</div>
</div>




<script>
var indexPar=0;
var SIMILARITY_ENGINE_BASE_URL="http://0.0.0.0:8095";
var PERTOS_BASE_URL="http://0.0.0.0:8090";
var NEFOLOG_BASE_URL="http://0.0.0.0:8010";
/*
 *Adding to the query string the parameters specified under the interface 
 */
function addNefologParameters(){
	$("#costparameter-list").append('<tr id="nefolog'+indexPar+'"></tr>');
	$("#costparameter-list tr:last-of-type").append('<td>   </td>');
	$("#costparameter-list tr:last-of-type").append('<td class="nefologParam">'+$("#parameter").val()+'</td>');
	$("#costparameter-list tr:last-of-type").append('<td class="nefologValue">'+$("#parameter_options input").val()+'</td>');
	$("#costparameter-list tr:last-of-type").append('<td><a href="javascript:void(0)" onclick="removeNefologParameter('+indexPar+')">remove</a></td>');
	indexPar=indexPar+1;
	return false; 
}

// Used when removing parameters
function removeNefologParameter(id){
	$("#nefolog"+id).remove();	
	 indexPar=indexPar-1;
}


function showConstraintsForm() {	
	showCostNefolog();
	$("#constraintsForm").modal("show");	
}

/*
 * Invoking the Nefolog service to render Candidate Offerings
 */
function showCostNefolog() {		
	// TODO: change to go through the service types in the topology and calculate the candidates
	var nefologURL= NEFOLOG_BASE_URL + "/nefolog/";
	//var qStringNefolog=nefologURL+"candidateSearch?servicetype=application&cpuCores=";
	var qStringNefolog=nefologURL+"candidateSearch?servicetype=application&io=moderate&media=json";
	//qStringNefolog=qStringNefolog+$("#np_avg").text()+'&cpuSpeed='+$("#psd_avg").text()+'&io=moderate';
	//qStringNefolog=qStringNefolog+'&memory='+$("#mav_avg").text()+'&storage='+$("#sts_avg").text();
	//qStringNefolog=qStringNefolog+'&bandwidth='+$("#band_avg").text()+'&transactions='+$("#avg_transactions").text()+'&media=json';
	
	$.ajax({
		url: qStringNefolog,
		type: "GET",
		crossDomain:true,	
		beforeSend:function(){
			$("#configid").empty();			
		},
		success: function(dataN, textStatus, jqXHR) {
			var lConfs= [];
			
			for(var k=0; k<dataN.candidates.length;k++){
				var indexSub=(dataN.candidates[k].configuration.uri).indexOf('_');
				lConfs[k]= (dataN.candidates[k].configuration.uri).substring(indexSub+1,dataN.candidates[k].configuration.uri.length);
				$("#configid").append('<option value="'+lConfs[k]+'">'+dataN.candidates[k].configuration.name+'</option>');				
			}
			
		},
		error: function(jqXHR, textStatus, errorThrown) {
			
			vShowAJAXError("Errors contacting Nefolog Service", jqXHR, errorThrown);
		}
	});

}

/*
 * Invoking Nefolog service to calculate distribution costs 
 */
function calculateCost(){
	var nefologURLCost= NEFOLOG_BASE_URL + "/nefolog/";
	var qStringNefologCost=nefologURLCost+'costCalculator?configid='+$("#configid").val();
	var listRows= $("#costparameter-list").find("tr");
	$.each(listRows, function(i,el) {
		qStringNefologCost=qStringNefologCost+'&'+($(el).find(".nefologParam")).text()+'=';
		qStringNefologCost=qStringNefologCost+($(el).find(".nefologValue")).text();
		
	});
		
	/*var qStringNefologCost=nefologURLCost+'costCalculator?configid='+$("#configid").val()+'&Queries='+$("#Queries").val();
	qStringNefologCost=qStringNefologCost+'&Account='+$("#Account").val()+'&Channel='+$("#Channel").val();
	qStringNefologCost=qStringNefologCost+'&Server='+$("#Server").val()+'&Instance='+$("#Instance").val();*/
	if($("#location_zone").val()!='-'){
		qStringNefologCost=qStringNefologCost+'&location_zone='+$("#location_zone").val();
	}
	
	//alert(qStringNefologCost);
	var flagcost=false;
	$.ajax({
		url: qStringNefologCost,
		type: "GET",				
		crossDomain:true,				
		beforeSend:function(){
			$("#cost_info").empty();	
										
		},						
		success: function(dataCost, textStatus, jqXHR) {
			
			$(dataCost).find('result').each(function(){
				
				$("#cost_info").append('<p style="margin-left:2%""><b>10-Month Total Cost: </b>'+ $(this).find("cost").first().contents().eq(0).text()+' <b> - Location: </b>'+$(this).find('location_zone').first().text()+' </p>');	
				flagcost=true;
				
			});							
			if(!flagcost){
				$("#cost_info").append("<p style='margin-left:2%'>No cost data.</p>");
			}
			
		},
		error: function(jqXHR, textStatus, errorThrown) {
			
			vShowAJAXError("Errors contacting Nefolog Service", jqXHR, errorThrown);
		}
	});
	
}

</script>
		