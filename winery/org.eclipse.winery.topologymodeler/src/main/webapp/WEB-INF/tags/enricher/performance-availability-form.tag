<%@tag description="Form to input the metrics of availability" pageEncoding="UTF-8"%>

<div class="modal fade" id="performanceAvailabilityForm">
	<div class="modal-dialog">
	<div class="modal-content">
			<div class="modal-header">
				<h4 class="modal-title">Performance: Availability Requirements</h4>
			</div>
			
			<div class="model-body">
				<form id="addPerfAvalForm" enctype="multipart/form-data">
					<fieldset>
			
					<div class="cell col-xs-4">
						<label class="control-label">Cloud Service Uptime (s)</label>
					    <input type="text" id="f_csu_min" placeholder ="Minimum">
					    <input type="text" id="f_csu_max" placeholder ="Maximum">
					    <input type="text" id="f_csu_avg" placeholder ="Average">
					    <input type="text" id="f_csu_st" placeholder ="Standard Dv">
						 		
					</div>
					
					<div class="cell col-xs-4">
						<label class="control-label">Cloud Resources Uptime (s)</label>
					    <input type="text" id="f_cru_min" placeholder ="Minimum">
					    <input type="text" id="f_cru_max" placeholder ="Maximum">
					    <input type="text" id="f_cru_avg" placeholder ="Average">
					    <input type="text" id="f_cru_st" placeholder ="Standard Dv">						 		
					</div>
					
					<div class="cell col-xs-4">
						<label class="control-label">Mean Time Between Failures (ms)</label>
					    <input type="text" id="f_mtbf_min" placeholder ="Minimum">
					    <input type="text" id="f_mtbf_max" placeholder ="Maximum">
					    <input type="text" id="f_mtbf_avg" placeholder ="Average">
					    <input type="text" id="f_mtbf_st" placeholder ="Standard Deviation">						 		
					</div>
					
					<div class="cell col-xs-4">
						<label class="control-label">Mean Time to Repair(ms)</label>
					    <input type="text" id="f_mtr_min" placeholder ="Minimum">
					    <input type="text" id="f_mtr_max" placeholder ="Maximum">
					    <input type="text" id="f_mtr_avg" placeholder ="Average">
					    <input type="text" id="f_mtr_st" placeholder ="Standard Deviation">						 		
					</div>
					
					
					
					</fieldset>
				</form>
			</div>
			<div class="modal-footer">
					<button type="button" id ="addPerfAvalbtn" class="btn btn-primary" onclick="addPerfAvailability()">
					Add</button>
					<button type="button" class="btn btn-default" data-dismiss="modal">Cancel</button>
			</div>			
			
		</div>
	</div>
</div>




<script>


function showPerfAvailabilityForm() {
	$("#performanceAdded").modal("hide");
	$("#performanceAvailabilityForm").modal("show");
	
}

/*
 * Adding the input values to hidden elements
 */
function addPerfAvailability(){
	$("#availability span").empty();
	var metrics= ['csu','cru','mtbf','mtr'];
	
	for (var k=0; k < metrics.length; k++){
		
		$("#"+metrics[k]+"_min").append($("#f_"+metrics[k]+"_min").val());
		$("#"+metrics[k]+"_max").append($("#f_"+metrics[k]+"_max").val());
		$("#"+metrics[k]+"_avg").append($("#f_"+metrics[k]+"_avg").val());
		$("#"+metrics[k]+"_st").append($("#f_"+metrics[k]+"_st").val());
		
	}
	
	vShowSuccess("Successfully Added");	
	$("#performanceAvailabilityForm").modal("hide");
	showPerformance();
	
}
</script>
		