<%@tag description="Workload Characteristics Form" pageEncoding="UTF-8"%>

<div class="modal fade" id="workloadForm">
	<div class="modal-dialog">
	<div class="modal-content">
			<div class="modal-header">
				<h4 class="modal-title">Workload Characteristics</h4>
			</div>
			
			<div class="model-body">
				<form id="addWorkloadForm" enctype="multipart/form-data">
					<fieldset>
			
					<div class="form-group">
						<label for="f_pattern" class="control-label">Pattern</label>
					     
						 <select class="form-control" id="f_pattern">
						 	 <option value="-">------</option>
							  <option value="continouously changing">Continuously Changing</option>
							  <option value="once in a lifetime">Once in a Lifetime</option>
							  <option value="periodic">Periodic</option>
							  <option value="static">Static </option>
							  <option value="unpredictable">Unpredictable </option>
						</select> 		
					</div>
					<div class="form-group">
						<label for="f_arrival" class="control-label">Arrival Rate Distribution:</label>
					
						<select id="f_arrival" class="form-control" >
							 <option value="-">------</option>
							  <option value="normal">Normal</option>
							  <option value="logarithmic">Logarithmic</option>
							  <option value="gamma">Gamma</option>
							  <option value="uniform">Uniform</option>					  
						</select> 
					</div>
					<div class="cell col-xs-4">
						<label for="f_behavioral" class="control-label">Behavioral Model:</label>
					
						<select id="f_behavioral" class="form-control">
								 <option value="-">------</option>
								  <option value="normal">Normal</option>
								  <option value="logarithmic">Logarithmic</option>
								  <option value="gamma">Gamma</option>
								  <option value="uniform">Uniform</option>					  
					    </select> 
					</div>
					<div class="cell col-xs-4">
						<label for="f_avg_users" class="control-label">Avg. Number of Users/Month:</label>
					 	<input type="text" id="f_avg_users">
					</div>
					<div class="cell col-xs-4">
						 <label for="f_avg_transactions" class="control-label">Avg. Number of Transactions/Month</label>
						 <input type="text" id="f_avg_transactions">
					</div>
				
					</fieldset>
				</form>
			</div>
			<div class="modal-footer">
					<button type="button" id ="addWorkloadbtn" class="btn btn-primary" onclick="addWorkload()">
					Add</button>
					<button type="button" class="btn btn-default" data-dismiss="modal">Cancel</button>
			</div>			
			
		</div>
	</div>
</div>




<script>
$("#workloadForm").on("shown.bs.modal", function() {
	$("#testWorkload").focus();
});

function showWorkloadForm() {
	$("#workloadAdded").modal("hide");
	$("#workloadForm").modal("show");
	
}

/*
 * Adding the values of workload to hidden elements, they are used later to build
 * the different XML documents that are needed
 */

function addWorkload(){
	$("#pattern").empty();
	$("#pattern").append($("#f_pattern").val());
	$("#arrival").empty();
	$("#arrival").append($("#f_arrival").val());	
	$("#behavioral").empty();
	$("#behavioral").append($("#f_behavioral").val());
	$("#avg_users").empty();
	$("#avg_users").append($("#f_avg_users").val());	
	$("#avg_transactions").empty();
	$("#avg_transactions").append($("#f_avg_transactions").val());
	
	vShowSuccess("Successfully Added");	
	$("#workloadForm").modal("hide");
	showWorkload();
	
	
	
}
</script>
		