<%@tag description="Form to input the metrics of resource utilization" pageEncoding="UTF-8"%>

<div class="modal fade" id="performanceResourceForm">
	<div class="modal-dialog">
	<div class="modal-content">
			<div class="modal-header">
				<h4 class="modal-title">Performance: Resource Utilization Requirements</h4>
			</div>
			
			<div class="model-body">
				<form id="addPerfResForm" enctype="multipart/form-data">
					<fieldset>
			
					<div class="cell col-xs-4">
						<label class="control-label">Network Utilization (%)</label>
					    <input type="text" id="f_nu_min" placeholder ="Minimum">
					    <input type="text" id="f_nu_max" placeholder ="Maximum">
					    <input type="text" id="f_nu_avg" placeholder ="Average">
					    <input type="text" id="f_nu_st" placeholder ="Standard Dv">
						 		
					</div>
					
					<div class="cell col-xs-4">
						<label class="control-label">Memory Utilization (%)</label>
					    <input type="text" id="f_mu_min" placeholder ="Minimum">
					    <input type="text" id="f_mu_max" placeholder ="Maximum">
					    <input type="text" id="f_mu_avg" placeholder ="Average">
					    <input type="text" id="f_mu_st" placeholder ="Standard Dv">						 		
					</div>
					
					<div class="cell col-xs-4">
						<label class="control-label">Disk Utilization (%)</label>
					    <input type="text" id="f_du_min" placeholder ="Minimum">
					    <input type="text" id="f_du_max" placeholder ="Maximum">
					    <input type="text" id="f_du_avg" placeholder ="Average">
					    <input type="text" id="f_du_st" placeholder ="Standard Deviation">						 		
					</div>
					
					<div class="cell col-xs-4">
						<label class="control-label">CPU Utilization (%)</label>
					    <input type="text" id="f_cu_min" placeholder ="Minimum">
					    <input type="text" id="f_cu_max" placeholder ="Maximum">
					    <input type="text" id="f_cu_avg" placeholder ="Average">
					    <input type="text" id="f_cu_st" placeholder ="Standard Deviation">						 		
					</div>
					
					<div class="cell col-xs-4">
						<label class="control-label">VM Utilization (%)</label>
					    <input type="text" id="f_vu_min" placeholder ="Minimum">
					    <input type="text" id="f_vu_max" placeholder ="Maximum">
					    <input type="text" id="f_vu_avg" placeholder ="Average">
					    <input type="text" id="f_vu_st" placeholder ="Standard Deviation">						 		
					</div>
					
					<div class="cell col-xs-4">
						<label class="control-label">VM per Physical Server</label>
					    <input type="text" id="f_vps_min" placeholder ="Minimum">
					    <input type="text" id="f_vps_max" placeholder ="Maximum">
					    <input type="text" id="f_vps_avg" placeholder ="Average">
					    <input type="text" id="f_vps_st" placeholder ="Stadard Deviation">						 		
					</div>
					</fieldset>
				</form>
			</div>
			<div class="modal-footer">
					<button type="button" id ="addPerfResbtn" class="btn btn-primary" onclick="addPerfResource()">
					Add</button>
					<button type="button" class="btn btn-default" data-dismiss="modal">Cancel</button>
			</div>			
			
		</div>
	</div>
</div>




<script>


function showPerfResourceForm() {
	$("#performanceAdded").modal("hide");
	$("#performanceResourceForm").modal("show");
	
}

/*
 * Adding the input values to hidden elements
 */
function addPerfResource(){
	$("#resource_utilization span").empty();
	var metrics= ['nu','mu','du','cu','vu','vps'];
	
	for (var k=0; k < metrics.length; k++){
		$("#"+metrics[k]+"_min").append($("#f_"+metrics[k]+"_min").val());
		$("#"+metrics[k]+"_max").append($("#f_"+metrics[k]+"_max").val());
		$("#"+metrics[k]+"_avg").append($("#f_"+metrics[k]+"_avg").val());
		$("#"+metrics[k]+"_st").append($("#f_"+metrics[k]+"_st").val());
		
	}
	
	vShowSuccess("Successfully Added");	
	$("#performanceResourceForm").modal("hide");
	showPerformance();
	
}
</script>
		