<%@tag description="Form to input the metrics of availability" pageEncoding="UTF-8"%>

<div class="modal fade" id="performanceCapacityForm">
	<div class="modal-dialog">
	<div class="modal-content">
			<div class="modal-header">
				<h4 class="modal-title">Performance: Capacity Requirements</h4>
			</div>
			
			<div class="model-body">
				<form id="addPerfCapForm" enctype="multipart/form-data">
					<fieldset>
			
					<div class="cell col-xs-4">
						<label class="control-label">Bandwith (MBps)</label>
					    <input type="text" id="f_band_min" placeholder ="Minimum">
					    <input type="text" id="f_band_max" placeholder ="Maximum">
					    <input type="text" id="f_band_avg" placeholder ="Average">
					    <input type="text" id="f_band_st" placeholder ="Standard Dv">
						 		
					</div>
					
					<div class="cell col-xs-4">
						<label class="control-label">Processor Speed (GHz)</label>
					    <input type="text" id="f_psd_min" placeholder ="Minimum">
					    <input type="text" id="f_psd_max" placeholder ="Maximum">
					    <input type="text" id="f_psd_avg" placeholder ="Average">
					    <input type="text" id="f_psd_st" placeholder ="Standard Dv">						 		
					</div>
					
					<div class="cell col-xs-4">
						<label class="control-label">Storage Size (GB)</label>
					    <input type="text" id="f_sts_min" placeholder ="Minimum">
					    <input type="text" id="f_sts_max" placeholder ="Maximum">
					    <input type="text" id="f_sts_avg" placeholder ="Average">
					    <input type="text" id="f_sts_st" placeholder ="Standard Deviation">						 		
					</div>
					
					<div class="cell col-xs-4"">
						<label class="control-label">Memory Allocation to VM (GB)</label>
					    <input type="text" id="f_mav_min" placeholder ="Minimum">
					    <input type="text" id="f_mav_max" placeholder ="Maximum">
					    <input type="text" id="f_mav_avg" placeholder ="Average">
					    <input type="text" id="f_mav_st" placeholder ="Standard Deviation">						 		
					</div>
					
					<div class="cell col-xs-4">
						<label class="control-label">Number of VM</label>
					    <input type="text" id="f_nv_min" placeholder ="Minimum">
					    <input type="text" id="f_nv_max" placeholder ="Maximum">
					    <input type="text" id="f_nv_avg" placeholder ="Average">
					    <input type="text" id="f_nv_st" placeholder ="Standard Deviation">						 		
					</div>
					
					<div class="cell col-xs-4">
						<label class="control-label">Number of Processors</label>
					    <input type="text" id="f_np_min" placeholder ="Minimum">
					    <input type="text" id="f_np_max" placeholder ="Maximum">
					    <input type="text" id="f_np_avg" placeholder ="Average">
					    <input type="text" id="f_np_st" placeholder ="Stadard Deviation">						 		
					</div>
					
					<div class="cell col-xs-4">
						<label class="control-label">I/O Operations</label>
					    <input type="text" id="f_ios_min" placeholder ="Minimum">
					    <input type="text" id="f_ios_max" placeholder ="Maximum">
					    <input type="text" id="f_ios_avg" placeholder ="Average">
					    <input type="text" id="f_ios_st" placeholder ="Standard Deviation">						 		
					</div>
				
					
					</fieldset>
				</form>
			</div>
			<div class="modal-footer">
					<button type="button" id ="addPerfCapbtn" class="btn btn-primary" onclick="addPerfCapacity()">
					Add</button>
					<button type="button" class="btn btn-default" data-dismiss="modal">Cancel</button>
			</div>			
			
		</div>
	</div>
</div>




<script>


function showPerfCapacityForm() {
	$("#performanceAdded").modal("hide");
	$("#performanceCapacityForm").modal("show");
	
}

/*
 * Adding the input values to hidden elements
 */
function addPerfCapacity(){
	$("#capacity span").empty();
	var metrics= ['band','psd','sts','mav','nv','np','ios'];
	
	for (var k=0; k < metrics.length; k++){
		$("#"+metrics[k]+"_min").append($("#f_"+metrics[k]+"_min").val());
		$("#"+metrics[k]+"_max").append($("#f_"+metrics[k]+"_max").val());
		$("#"+metrics[k]+"_avg").append($("#f_"+metrics[k]+"_avg").val());
		$("#"+metrics[k]+"_st").append($("#f_"+metrics[k]+"_st").val());
		
	}
	
	vShowSuccess("Successfully Added");	
	$("#performanceCapacityForm").modal("hide");
	showPerformance();
	
}
</script>
		