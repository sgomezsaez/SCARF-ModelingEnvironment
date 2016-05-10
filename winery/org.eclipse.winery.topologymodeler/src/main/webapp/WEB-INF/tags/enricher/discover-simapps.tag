<%@tag description="Renders the list of similar apps" pageEncoding="UTF-8"%>
<%@taglib prefix="o"  tagdir="/WEB-INF/tags/common/orioneditor"%>
<%@taglib prefix="w"  tagdir="/WEB-INF/tags"%>

<div class="modal fade" id="displaySimilarApps">
	<div class="modal-dialog modal-lg">
	<div class="modal-content">
			<div class="modal-header">
				<h4 class="modal-title">Similar applications: </h4>
				<div id="comp-sim" style ="display:none" >Computing ...</div>
			</div>
			
			<div class="model-body">	
			<table class="table table-striped">
					 	<thead>
						      <tr>
						        <th>   </th>
						        <th>ID</th>
						        <th>Workload</th>
						        <th>Performance</th>
						        <th>Global Similarity</th>
						        <th>Knowledge</th>
						        <th>Mu-Topology</th>
						        <th>Distribution Cost</th>
						      </tr>
						    </thead>
						    <tbody id="simapp-list"></tbody>   					      
					 </table>		
				
			 </div>
				
				<div class="modal-footer">
						<button type="button" class="btn btn-primary" id="discoverApps" onclick="winery.events.fire(winery.events.name.command.GETXML);">
						Discover Similar Apps</button>
						<button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
					</div>
			</div>
			
	</div>
	
</div>

<script>
$("#displaySimilarApps").on("shown.bs.modal", function() {
	$("#displaySimilarApps").focus();
});

function showDiagSimilarApps() {		
	
	$("#displaySimilarApps").modal("show");
}

/*
 * This function renders the list of similar apps and the different options (view knowledge, cost, refine)
 * Here the functions that make requests to the Similarity Engine and Nefolog are invoked
 */
function showSimilarityData(data,topologyTemplateURL){
	if(data.length == 0){
		alert('No similar applications found');		
	}
	
	for (var i=0; i<data.length; i++){
		
		$("#simapp-list").append('<tr id="rowsim'+i+'"></tr>');
		$('#rowsim'+i).append('<td>   </td>');
		$('#rowsim'+i).append('<td><a href='+data[i].url+' target="_blank">'+data[i].appId+'<a></td>');
		$('#rowsim'+i).append('<td>'+data[i].workloadSimilarity+'</td>');
		$('#rowsim'+i).append('<td>'+data[i].performanceSimilarity+'</td>');
		$('#rowsim'+i).append('<td>'+data[i].globalSimilarity+'</td>')
		$('#rowsim'+i).append('<td><a href="javascript:void(0)" onclick="showAppKnowledge('+data[i].appId+')">View</a></td>');
	    //$('#rowsim'+i).append('<td><a target="_blank" href="http://localhost:8080/winery-topologymodeler/redistributeIndex.jsp?'+topologyTemplateURL+'&mu='+data[i].dist_url+'">Adapt</a></td>');
	    
	    var encodedTemplateURL= "'"+encodeURIComponent(topologyTemplateURL)+"'";
	    var encodedDistURL= "'"+encodeURIComponent(data[i].dist_url)+"'";
	    //alert(encodedDistURL);
	    $('#rowsim'+i).append('<td><a href="javascript:void(0)" onclick="return redirectAdapt('+encodedTemplateURL+','+encodedDistURL+')">Refine</a></td>');
	    $('#rowsim'+i).append('<td><a href="javascript:void(0)" onclick="showCostNefolog('+data[i].appId+')">View</a></td>');
	    
	    //$('#rowsim'+i).append('<td><a href="javascript:void(0)" onclick="showAppKnowledge('+data[i].appId+')"><img class= "icon" src="view-icon.png" /></a></td>');
	    //('#optionsSim'+i).append('<a href="javascript:void(0)" onclick="showAppKnowledge('+data[i].appId+')"><img src="../images/redist-icon.png" /></a>');
		//$('#optionsSim'+i).append('<a href="javascript:void(0)" onclick="showAppKnowledge('+data[i].appId+')"><img src="../images/cost-icon.png" /></a>');
		
	}
}



</script>
		