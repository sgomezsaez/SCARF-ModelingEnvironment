<%@tag description="Form to input the metrics of scalability"  pageEncoding="UTF-8"%>

<div class="modal fade" id="performanceScalabilityForm">
	<div class="modal-dialog">
	<div class="modal-content">
			<div class="modal-header">
				<h4 class="modal-title">Performance: Scalability Requirements</h4>
			</div>
			
			<div class="model-body">
				<form id="addPerfScaForm" enctype="multipart/form-data">
					<fieldset>
			
					<div class="cell col-xs-4">
						<label class="control-label">Resource Acquisition Time (s)</label>
					    <input type="text" id="f_rat_min" placeholder ="Minimum">
					    <input type="text" id="f_rat_max" placeholder ="Maximum">
					    <input type="text" id="f_rat_avg" placeholder ="Average">
					    <input type="text" id="f_rat_st" placeholder ="Standard Dv">
						 		
					</div>
					
					<div class="cell col-xs-4">
						<label class="control-label">Resource Provisioning Time (s)</label>
					    <input type="text" id="f_rpt_min" placeholder ="Minimum">
					    <input type="text" id="f_rpt_max" placeholder ="Maximum">
					    <input type="text" id="f_rpt_avg" placeholder ="Average">
					    <input type="text" id="f_rpt_st" placeholder ="Standard Dv">						 		
					</div>
					
					<div class="cell col-xs-4">
						<label class="control-label">Deployment Time (s)</label>
					    <input type="text" id="f_dt_min" placeholder ="Minimum">
					    <input type="text" id="f_dt_max" placeholder ="Maximum">
					    <input type="text" id="f_dt_avg" placeholder ="Average">
					    <input type="text" id="f_dt_st" placeholder ="Standard Deviation">						 		
					</div>
					
					<div class="cell col-xs-4">
						<label class="control-label">Resource Release Time (s)</label>
					    <input type="text" id="f_rrt_min" placeholder ="Minimum">
					    <input type="text" id="f_rrt_max" placeholder ="Maximum">
					    <input type="text" id="f_rrt_avg" placeholder ="Average">
					    <input type="text" id="f_rrt_st" placeholder ="Standard Deviation">						 		
					</div>
					
					<div class="cell col-xs-4">
						<label class="control-label">VM Startup Time (s)</label>
					    <input type="text" id="f_vst_min" placeholder ="Minimum">
					    <input type="text" id="f_vst_max" placeholder ="Maximum">
					    <input type="text" id="f_vst_avg" placeholder ="Average">
					    <input type="text" id="f_vst_st" placeholder ="Standard Deviation">						 		
					</div>							
					
					</fieldset>
				</form>
			</div>
			<div class="modal-footer">
					<button type="button" id ="addPerfScabtn" class="btn btn-primary" onclick="addPerfScalability()">
					Add</button>
					<button type="button" class="btn btn-default" data-dismiss="modal">Cancel</button>
			</div>			
			
		</div>
	</div>
</div>




<script>


function showPerfScalabilityForm() {
	$("#performanceAdded").modal("hide");
	$("#performanceScalabilityForm").modal("show");
	
}

/*
 * Adding the input values to hidden elements
 */
function addPerfScalability(){
	$("#scalability span").empty();
	var metrics= ['rat','rpt','dt','rrt','vst'];
	
	for (var k=0; k < metrics.length; k++){
		$("#"+metrics[k]+"_min").append($("#f_"+metrics[k]+"_min").val());
		$("#"+metrics[k]+"_max").append($("#f_"+metrics[k]+"_max").val());
		$("#"+metrics[k]+"_avg").append($("#f_"+metrics[k]+"_avg").val());
		$("#"+metrics[k]+"_st").append($("#f_"+metrics[k]+"_st").val());
	}
	
	vShowSuccess("Successfully Added");	
	$("#performanceScalabilityForm").modal("hide");
	showPerformance();
	
}
</script>
		