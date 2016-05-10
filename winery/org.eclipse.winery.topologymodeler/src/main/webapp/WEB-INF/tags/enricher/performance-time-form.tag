<%@tag description="Form to input the metrics of time performance" pageEncoding="UTF-8"%>

<div class="modal fade" id="performanceTimeForm">
	<div class="modal-dialog">
	<div class="modal-content">
			<div class="modal-header">
				<h4 class="modal-title">Performance: Time Behaviour Requirements</h4>
			</div>
			
			<div class="model-body">
				<form id="addPerfTimeForm" enctype="multipart/form-data">
					<fieldset>
			
					<div class="cell col-xs-4">
						<label class="control-label">Response Time (ms)</label>
					    <input type="text" id="f_time_min" placeholder ="Minimum">
					    <input type="text" id="f_time_max" placeholder ="Maximum">
					    <input type="text" id="f_time_avg" placeholder ="Average">
					    <input type="text" id="f_time_st" placeholder ="Standard Dv">
						 		
					</div>
					
					<div class="cell col-xs-4">
						<label class="control-label">Throughput</label>
					    <input type="text" id="f_thr_min" placeholder ="Minimum">
					    <input type="text" id="f_thr_max" placeholder ="Maximum">
					    <input type="text" id="f_thr_avg" placeholder ="Average">
					    <input type="text" id="f_thr_st" placeholder ="Standard Dv">						 		
					</div>
					
					<div class="cell col-xs-4">
						<label class="control-label">Processing Time (ms)</label>
					    <input type="text" id="f_prt_min" placeholder ="Minimum">
					    <input type="text" id="f_prt_max" placeholder ="Maximum">
					    <input type="text" id="f_prt_avg" placeholder ="Average">
					    <input type="text" id="f_prt_st" placeholder ="Standard Deviation">						 		
					</div>
					
					<div class="cell col-xs-4">
						<label class="control-label"> Read Speed (revolutions/min)</label>
					    <input type="text" id="f_avgrs_min" placeholder ="Minimum">
					    <input type="text" id="f_avgrs_max" placeholder ="Maximum">
					    <input type="text" id="f_avgrs_avg" placeholder ="Average">
					    <input type="text" id="f_avgrs_st" placeholder ="Standard Deviation">						 		
					</div>
					
					<div class="cell col-xs-4">
						<label class="control-label">Write Speed (revolutions/min)</label>
					    <input type="text" id="f_avgws_min" placeholder ="Minimum">
					    <input type="text" id="f_avgws_max" placeholder ="Maximum">
					    <input type="text" id="f_avgws_avg" placeholder ="Average">
					    <input type="text" id="f_avgws_st" placeholder ="Standard Deviation">						 		
					</div>
					
					<div class="cell col-xs-4">
						<label class="control-label">Migration Time (s)</label>
					    <input type="text" id="f_avgmt_min" placeholder ="Minimum">
					    <input type="text" id="f_avgmt_max" placeholder ="Maximum">
					    <input type="text" id="f_avgmt_avg" placeholder ="Average">
					    <input type="text" id="f_avgmt_st" placeholder ="Stadard Deviation">						 		
					</div>
					
					<div class="cell col-xs-4">
						<label class="control-label">Latency (ms)</label>
					    <input type="text" id="f_lat_min" placeholder ="Minimum">
					    <input type="text" id="f_lat_max" placeholder ="Maximum">
					    <input type="text" id="f_lat_avg" placeholder ="Average">
					    <input type="text" id="f_lat_st" placeholder ="Standard Deviation">						 		
					</div>
					
					<div class="cell col-xs-4">
						<label class="control-label">Backup Time (s)</label>
					    <input type="text" id="f_back_min" placeholder ="Minimum">
					    <input type="text" id="f_back_max" placeholder ="Maximum">
					    <input type="text" id="f_back_avg" placeholder ="Average">
					    <input type="text" id="f_back_st" placeholder ="Standard Deviation">						 		
					</div>
					
					
					</fieldset>
				</form>
			</div>
			<div class="modal-footer">
					<button type="button" id ="addPerfTimebtn" class="btn btn-primary" onclick="addPerfTime()">
					Add</button>
					<button type="button" class="btn btn-default" data-dismiss="modal">Cancel</button>
			</div>			
			
		</div>
	</div>
</div>




<script>


function showPerfTimeForm() {
	$("#performanceAdded").modal("hide");
	$("#performanceTimeForm").modal("show");
	
}

/*
 * Adding the input values to hidden elements
 */
 
function addPerfTime(){
	$("#time_behaviour span").empty();
	var metrics= ['time','thr','prt','avgrs','avgws','avgmt','lat','back'];
	
	for (var k=0; k < metrics.length; k++){
		
		$("#"+metrics[k]+"_min").append($("#f_"+metrics[k]+"_min").val());
		$("#"+metrics[k]+"_max").append($("#f_"+metrics[k]+"_max").val());
		$("#"+metrics[k]+"_avg").append($("#f_"+metrics[k]+"_avg").val());
		$("#"+metrics[k]+"_st").append($("#f_"+metrics[k]+"_st").val());
		
	}
	
	vShowSuccess("Successfully Added");	
	$("#performanceTimeForm").modal("hide");
	showPerformance();
	
}
</script>
		