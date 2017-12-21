<%@tag description="Shows Knowledge of an App" pageEncoding="UTF-8"%>
<%@taglib prefix="o"  tagdir="/WEB-INF/tags/common/orioneditor"%>
<%@taglib prefix="w"  tagdir="/WEB-INF/tags"%>

<div class="modal fade" id="displayAppKnowledge">
	<div class="modal-dialog modal-lg">
	<div class="modal-content">
			<div class="modal-header">
				<h4 class="modal-title">App Knowledge </h4>
				<div id="comp-know" style ="display:none" >Requesting ...</div>
			</div>
			
			<div class="model-body">			
					
			      <div class="row">
			      	<div class="cell col-xs-1"></div>
			      	<div class="cell col-xs-9"><h4>Workload Time Interval <span id="idtimein"></span></h4></div>
			      </div>
			       <table class="table table-striped">
					 	<thead>
						      <tr>
						        <th>       </th>
						        <th>Pattern</th>
						        <th>Arrival Rate</th>
						        <th>Behavioral Model Max</th>
						        <th>Avg. Transac/Sec</th>
						        <th>Avg. User number</th>
						      </tr>
						    </thead>
						    <tbody id="knowapp-Worklist0"></tbody>   					      
					 </table>
			      
				<div>		   
				   <div class="row" >
			      		<div class="cell col-xs-1"></div>
			      		<div class="cell col-xs-3"><h4>Performance:</h4></div>
			      </div>
				 
					<div id="rowTB" style="display:none">
					<div class="row">
						      	<div class="cell col-xs-1"></div>
						      	<div class="cell col-xs-9"><b>- Time Behaviour -</b></div>
					 </div>
					 <table class="table table-striped">
					 	<thead>
						      <tr>
						        <th>       </th>
						        <th>Metric</th>
						        <th>Min</th>
						        <th>Max</th>
						        <th>Mean</th>
						        <th>St. Deviation</th>
						      </tr>
						    </thead>
						    <tbody id="rowknowPer1"></tbody>   					      
					 </table>
			      </div>
			     
			      
			      <div id="rowC" style="display:none">
				      <div class="row">
					      	<div class="cell col-xs-1"></div>
					      	<div class="cell col-xs-9"><b>- Capacity -</b></div>
				      </div>
				      
				    <table class="table table-striped">
					 	<thead>
						      <tr>
						        <th>       </th>
						        <th>Metric</th>
						        <th>Min</th>
						        <th>Max</th>
						        <th>Mean</th>
						        <th>St. Deviation</th>
						      </tr>
						    </thead>
						    <tbody id="rowknowPer2"></tbody>   					      
					 </table>
			      	
		      	</div>
			      
			      <div id="rowRU" style="display:none">
				      <div class="row" >
					      	<div class="cell col-xs-1"></div>
					      	<div class="cell col-xs-9"><b>- Resource Utilization -</b></div>
				      </div>
			      
				      <table class="table table-striped">
					 	<thead>
						      <tr>
						        <th>       </th>
						        <th>Metric</th>
						        <th>Min</th>
						        <th>Max</th>
						        <th>Mean</th>
						        <th>St. Deviation</th>
						      </tr>
						    </thead>
						    <tbody id="rowknowPer3"></tbody>   					      
					 </table>
				      	
			      </div>
			     
			      <div id="rowS" style="display:none">
				      <div class="row" >
				      	<div class="cell col-xs-1"></div>
				      	<div class="cell col-xs-9"><b>- Scalability -</b></div>
			      	  </div>
		      	  
			      	  <table class="table table-striped">
					 	<thead>
						      <tr>
						        <th>       </th>
						        <th>Metric</th>
						        <th>Min</th>
						        <th>Max</th>
						        <th>Mean</th>
						        <th>St. Deviation</th>
						      </tr>
						    </thead>
						    <tbody id="rowknowPer4"></tbody>   					      
					 </table>
				      	
			      </div>
			     
			      <div id="rowAV" style="display:none">
			      
			      	  <div class="row" >
				      	<div class="cell col-xs-1"></div>
				      	<div class="cell col-xs-9"><b> - Availability -</b></div>
			          </div>
		          
			          <table class="table table-striped">
					 	<thead>
						      <tr>
						        <th>       </th>
						        <th>Metric</th>
						        <th>Min</th>
						        <th>Max</th>
						        <th>Mean</th>
						        <th>St. Deviation</th>
						      </tr>
						    </thead>
						    <tbody id="rowknowPer5"></tbody>   					      
					 </table>
				      	
			      </div>
					
			 </div>
			   </div>
			<div class="modal-footer">						
			    <button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
		     </div>	   
		</div>
				
		
	</div>
</div>
	

<script>
$("#displayAppKnowledge").on("shown.bs.modal", function() {
	$("#displayAppKnowledge").focus();
});

function showDiagKnowledgeApp(){
	$("#displayAppKnowledge").modal("show");
}

var SIMILARITY_ENGINE_BASE_URL="http://192.168.99.100:8095";
var PERTOS_BASE_URL="http://192.168.99.100:8090";
var NEFOLOG_BASE_URL="http://192.168.99.100:8080";

function showAppKnowledge(id) {		
	var metric_names = ["Response Time", "Throughput", "Processing Time", "Avg. Read Speed", "Avg. Write Speed",
			"Avg. Migration Time", "Latency", "Backup Time", "Bandwith", "Processor Speed", "Storage Size",
			"Memory Allocation to VM", "Number VM", "Number Processors", "IO Operations", "Network Utilization",
			"Memory Utilization", "Disk Utilization", "CPU Utilization", "VM Utilization", "Number VM Perserver",
			"Resource Acquisition Time", "Resource Provisioning Time", "Deployment Time", "Resource Release Time",
			"VM Startup Time", "Cloud Service Uptime", "Cloud Resource Uptime", "Meantime Between Failures",
			"Meantime to Repair" ];
	$.ajax({
		url: SIMILARITY_ENGINE_BASE_URL + "/SimilarityEngine/application-knowledge/"+id,
		type: "GET",
		beforeSend:function(){
			$("#comp-know").show();
			$("#idtimein").empty();
			$("#knowapp-Worklist0").empty();			
			$(".rowsk").empty();	
			
		},
		success: function(data, textStatus, jqXHR) {
			$("#comp-know").hide();
			
			$("#knowapp-Worklist0").append('<tr id="rowknowWork0"></tr>');
			$('#rowknowWork0').append('<td>            </td>');
			$('#rowknowWork0').append('<td>'+data[0].workload.pattern+'</td>');
			$('#rowknowWork0').append('<td>'+data[0].workload.arrival_rate+'</td>');
			$('#rowknowWork0').append('<td>'+data[0].workload.behavioral_model+'</td>');
			$('#rowknowWork0').append('<td>'+data[0].workload.avg_transactions_second+'</td>');
			$('#rowknowWork0').append('<td>'+data[0].workload.avg_usr_number+'</td>');
			
			$("#idtimein").append(data[0].workload.time_interval);	
			$("#rowKnowPer1").empty();
			$("#rowKnowPer2").empty();
			$("#rowKnowPer3").empty();
			$("#rowKnowPer4").empty();
			$("#rowKnowPer5").empty();	
			var f1=true, f2= true, f3=true, f4 =true, f5=true;
			var indexMetric=0;
			for (var m=0; m<data[0].lmetricd.length; m++){
				var metricd=data[0].lmetricd[m];
				if( metricd.fk_metric_id >=1 && metricd.fk_metric_id <=8){
					if (f1){
						$("#rowTB").show();
						f1=false;
					}
					indexMetric=1;
					
				}else if (metricd.fk_metric_id >=9 && metricd.fk_metric_id <=15){
					if (f2){
						$("#rowC").show();
						f2=false;						
					}
					indexMetric=2;					
				}else if(metricd.fk_metric_id >=16 && metricd.fk_metric_id <=21){
					if (f3){
						$("#rowRU").show();
						f3=false;
					}	
					indexMetric=3;
				}else if (metricd.fk_metric_id >=22 && metricd.fk_metric_id <=26){
					if (f4){
						$("#rowS").show();
						f4=false;
					}
					indexMetric=4;
				}else if (metricd.fk_metric_id >=27 && metricd.fk_metric_id <=30){
					if (f5){
						$("#rowAV").show();
						f5=false;
					}
					indexMetric=5;
				}
				
				
				$('#rowknowPer'+indexMetric).append('<tr  class="rowsk"></tr>');
				$('#rowknowPer'+indexMetric+' tr:last-of-type').append('<td>           </td>');
				$('#rowknowPer'+indexMetric+' tr:last-of-type').append('<td>'+metric_names[metricd.fk_metric_id-1]+'</td>');
				$('#rowknowPer'+indexMetric+' tr:last-of-type').append('<td>'+metricd.min+'</td>');
				$('#rowknowPer'+indexMetric+' tr:last-of-type').append('<td>'+metricd.max+'</td>');
				$('#rowknowPer'+indexMetric+' tr:last-of-type').append('<td>'+metricd.mean+'</td>');
				$('#rowknowPer'+indexMetric+' tr:last-of-type').append('<td>'+metricd.st_deviation+'</td>');
									
			}
			
			
		},
		error: function(jqXHR, textStatus, errorThrown) {
			
			vShowAJAXError("Errors contacting Similarity Service", jqXHR, errorThrown);
		}
	});
	showDiagKnowledgeApp();
}


$("#displayAppKnowledge").on("hidden", function () {
	$("#rowKnowPer1").hide();
	
	$("#rowTB").hide();			
	$("#rowC").hide();
	$("#rowRU").hide();
	$("#rowS").hide();
	$("#rowAV").hide();
	});


</script>
		