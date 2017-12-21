<%@tag description="Workload Characteristics" pageEncoding="UTF-8"%>
<%@taglib prefix="o"  tagdir="/WEB-INF/tags/common/orioneditor"%>
<%@taglib prefix="w"  tagdir="/WEB-INF/tags"%>

<div class="modal fade" id="workloadAdded">
	<div class="modal-dialog">
	<div class="modal-content">
			<div class="modal-header">
				<h4 class="modal-title">Workload Characteristics: </h4>
			</div>
			
			<div class="model-body">			
				<o:orioneditorarea areaid="OrionWorkloadXML" withoutsavebutton="true" />	   
			 </div>
				
				<div class="modal-footer">
						<button type="button" class="btn btn-primary" id="addWorkload" onclick="showWorkloadForm();">
						Specify Workload Characteristics</button>
						<button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
					</div>
			</div>
			
	</div>
	
</div>

<script>
$("#workloadAdded").on("shown.bs.modal", function() {
	$("#addWorkload").focus();
});

var SIMILARITY_ENGINE_BASE_URL="http://192.168.99.100:8095";
var PERTOS_BASE_URL="http://192.168.99.100:8090";
var NEFOLOG_BASE_URL="http://192.168.99.100:8080";

/*
 * Builds the XML that is render when choosing Workload Characteristics from the menu
 * It uses the values of the hidden elements that were filled with the input data of
 * the respective forms
 */
function showWorkload() {	
	
	var elementsW = $("#workloadPropContainer").find("div");
		
	if (elementsW.length != 0) {
		var xmlWorkload = '<Workload>\n';
		var xmlWorkloadForCookie = '<Workload>';
		var i=1;
		$.each(elementsW, function(i,el) {
			xmlWorkload=xmlWorkload.concat('\t <Property id=\"',i+1,'\" name=\"',$(el).attr("id"),'\" value=\"',$(el).text(),'\"/>\n');
			xmlWorkloadForCookie=xmlWorkloadForCookie.concat('<Property id=\"',i+1,'\" name=\"',$(el).attr("id"),'\" value=\"',$(el).text(),'\"/>');
			i=i+1;			
		});		
		
		xmlWorkload = xmlWorkload.concat('</Workload>');
		xmlWorkloadForCookie = xmlWorkloadForCookie.concat('</Workload>');
		
		$("#workloadHidden").val(xmlWorkloadForCookie);
		//$("#workloadAdded").modal("show");
	   var diag = $("#workloadAdded");
		require(["winery-support-common"], function(w) {
			w.replaceDialogShownHookForOrionUpdate(diag, "OrionWorkloadXML", xmlWorkload);
			diag.modal("show");
		});
	}
	
	
	$("#workloadAdded").modal("show");
}

//TEST ONLY, can be deleted if needed
function getWorkloadSim(){
	// Assign handlers immediately after making the request,
	// and remember the jqxhr object for this request
	var jqxhr_wsim = $.get( SIMILARITY_ENGINE_BASE_URL + "/SimilarityEngine/get-workload-sim")
	.done(function(data) {
		//var obj = jQuery.parseJSON(data);
		alert("Topology ID:"+data[0].id+ " Similarity: "+data[0].simValue);
		alert("Topology ID:"+data[1].id+ " Similarity: "+data[1].simValue);
		
	})
	.fail(function() {
	alert( "error" );
	})
	.always(function() {
	alert( "finished" );
	});
	
}
</script>
		