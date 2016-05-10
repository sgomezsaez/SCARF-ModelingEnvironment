<%@tag description="Performance Requirements" pageEncoding="UTF-8"%>
<%@taglib prefix="o"  tagdir="/WEB-INF/tags/common/orioneditor"%>
<%@taglib prefix="w"  tagdir="/WEB-INF/tags"%>

<div class="modal fade" id="performanceAdded">
	<div class="modal-dialog modal-lg">
	<div class="modal-content">
			<div class="modal-header">
				<h4 class="modal-title">Performance Requirements: </h4>
			</div>
			
			<div class="model-body">			
				<o:orioneditorarea areaid="OrionPerformaceXML" withoutsavebutton="true" />	   
			 </div>
				
				<div class="modal-footer">
						<button type="button" class="btn btn-primary" id="addTime" onclick="showPerfTimeForm();">
						Specify Time Behaviour</button>
						<button type="button" class="btn btn-primary" id="addCapacity" onclick="showPerfCapacityForm();">
						Specify Capacity</button>
						<button type="button" class="btn btn-primary" id="addResource" onclick="showPerfResourceForm();">
						Specify Resource Utilization</button>
						<button type="button" class="btn btn-primary" id="addScalability" onclick="showPerfScalabilityForm();">
						Specify Scalability</button>
						<button type="button" class="btn btn-primary" id="addAvailabitiy" onclick="showPerfAvailabilityForm();">
						Specify Availability</button>
						
						<button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
					</div>
			</div>
			
	</div>
	
</div>

<script>
$("#performacneAdded").on("shown.bs.modal", function() {
	$("#addTime").focus();
});

/*
 * Builds the XML that is render when choosing Performance metrics from the menu
 * It uses the values of the hidden elements that were filled with the input data of
 * the respective forms
 */
function showPerformance() {		
	
	var metric_cats= ['Time Behaviour', 'Capacity', 'Resource utilization', 'Scalability', 'Availability'];
	var metric_ids=['time_behaviour', 'capacity', 'resource_utilization', 'scalability', 'availability'];
	
	var xmlPerformance= '<Performance>\n';
	var xmlPerformanceForCookie ='<Performance>';
	
	for (var l=0; l<metric_cats.length; l++){
		xmlPerformance=xmlPerformance.concat('\t<'+metric_ids[l]+'>\n');
		xmlPerformanceForCookie=xmlPerformanceForCookie.concat('<'+metric_ids[l]+'>');
		
		var elementsPT = $('#'+metric_ids[l]).find("div");
		var i=1;
		$.each(elementsPT, function(i,el) {
			xmlPerformance=xmlPerformance.concat('\t \t<'+$(el).attr("id")+'>\n');
			xmlPerformanceForCookie=xmlPerformanceForCookie.concat('<'+$(el).attr("id")+'>');
			
			var elementsSP= $('#'+$(el).attr("id")).find("span");
			var m=1;
			$.each(elementsSP, function(m,els) {
				xmlPerformance=xmlPerformance.concat('\t \t \t<Property name=\"',$(els).attr("id"),'\" value=\"',$(els).text(),'\" />\n');
				xmlPerformanceForCookie=xmlPerformanceForCookie.concat('<Property name=\"',$(els).attr("id"),'\" value=\"',$(els).text(),'\" />');
				
			});
			
			xmlPerformance=xmlPerformance.concat('\t \t</'+$(el).attr("id")+'> \n');
			xmlPerformanceForCookie=xmlPerformanceForCookie.concat('</'+$(el).attr("id")+'>');
			i=i+1;			
		});
		xmlPerformance=xmlPerformance.concat('\t</'+metric_ids[l]+'>\n');
		xmlPerformanceForCookie=xmlPerformanceForCookie.concat('</'+metric_ids[l]+'>');
		
	}
	xmlPerformance = xmlPerformance.concat('</Performance> \n');
	xmlPerformanceForCookie = xmlPerformanceForCookie.concat('</Performance>');
	$("#performanceHidden").val(xmlPerformanceForCookie);
	
	var diag = $("#performanceAdded");
	require(["winery-support-common"], function(w) {
		w.replaceDialogShownHookForOrionUpdate(diag, "OrionPerformaceXML", xmlPerformance);
		diag.modal("show");
	});
	
	
	//$("#performanceAdded").modal("show");
}

</script>
		