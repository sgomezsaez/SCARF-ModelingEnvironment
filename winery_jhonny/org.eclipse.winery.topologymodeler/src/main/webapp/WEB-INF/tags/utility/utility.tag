<%@tag description="Application Utility Calculation" pageEncoding="UTF-8"%>
<%@taglib prefix="o"  tagdir="/WEB-INF/tags/common/orioneditor"%>
<%@taglib prefix="w"  tagdir="/WEB-INF/tags"%>

<div class="modal fade" id="displayUtility">
	<div class="modal-dialog">
	<div class="modal-content">
			<div class="modal-header">
				<h4 class="modal-title">Application Utility Calculation</h4>
				<div id="comp-cost" style ="display:none" >Requesting ...</div>
			</div>
			
			
			<div class="model-body">	
					<!-- <div class="container" style="width:100%;">					  
					  <div class="panel-group" id="accordion">
					  </div>
					</div> -->
					<form id="utilityCalculationForm" enctype="multipart/form-data">
						<fieldset>
							<div class="cell col-xs-4">
								<label for="f_application_type" class="control-label">Application Type:</label>
					 			<input type="text" id="application_type">
							</div>
							<div class="cell col-xs-4">
								<label for="f_application_name" class="control-label">Application Name:</label>
					 			<input type="text" id="application_name">
							</div>
							<div class="cell col-xs-4">
								<label for="f_distribution_name" class="control-label">Distribution Name:</label>
					 			<input type="text" id="distribution_name">
							</div>
							<div class="form-group">
								<label for="f_blank" class="control-label"></label>
							</div>
							<div class="form-group">
								<label for="f_nefolog_config" class="control-label">Nefolog Config (separated by ';'):</label>
					 			<input type="text" id="nefolog_config">
					 			(optional for cost calculation)
							</div>
							<div class="form-group">
								<h5>Utility Function</h5>
<!-- 								<label for="keretaSelectUf">Utility Function</label>
 -->					     
						 		<select class="form-control" id="keretaSelectUf" onclick="loadUtilityFunctions()">
								 	 
								</select> 		
							</div>
							<div class="form-group">
								<button type="button" id ="discoverSubfunctions" class="btn btn-primary" onclick="loadUtilitySubFunctions()">
									Display Sub-functions</button>		
							</div>
							<div class='form-group'>
								<h5>Utility Sub-functions </h5>
								<input type='hidden' value='' id='ufId' />
								<div class='form-group' id='utilitySubfunctions'>
									
<!-- 									<label for='fctAlias'>Alias:</label><input id='ufAlias' class='form-control' /> -->
								</div>
								<div class='form-group' id='utilityCalculationResult'>
									
<!-- 									<label for='fctAlias'>Alias:</label><input id='ufAlias' class='form-control' /> -->
								</div>
								
							</div>
							<!-- <div class='col-lg-12'>
								<h3>Utility Subfunctions</h3>
								<input type='hidden' value='' id='ufId' />
								<div class='form-group'>
									<label for='fctAlias'>Alias:</label><input id='ufAlias' class='form-control' />
								</div>
								<label for="keretaUsf" class="control-label">Utility Subfunctions</label>
					 			<table style="width:100%" id="keretaUsf">
					 			</table>
							</div> -->
						</fieldset>
						
					</form>
					
			   </div>
			<div class="modal-footer">						
			    <button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
			    <button type="button" id ="calculateUtility" class="btn btn-primary" onclick="calculateUtility()">
					Calculate Utility</button>
				<button type="button" id ="finishCalculation" class="btn btn-primary" onclick="finishCalculation()">
					Finish</button>
		     </div>	   
		</div>
				
		
	</div>
</div>
	

<script>
$("#displayUtility").on("shown.bs.modal", function() {
	$("#displayUtility").focus();
});

function showDiagUtility(){
	$("#displayUtility").modal("show");
}

/*
 * Performing the requests to Kereta
 */
 
var SIMILARITY_ENGINE_BASE_URL="http://0.0.0.0:8095";
var NEFOLOG_BASE_URL="http://0.0.0.0:8010";
var KERETA_BASE_URL="http://0.0.0.0:8075/Kereta";

function getUfObject(xml)
{
	var uf = new Object;
	uf.id = $(xml).find('id').text();
	uf.alias = $(xml).find('alias').text();
	uf.dstrId = $(xml).find('distributionId').text();
	uf.subFct = [];
	return uf;	
}

function getSubFctObject(xml)
{
	var subFct = new Object;
	subFct.ufId = $(xml).find('utilityFunctionId').text();
	subFct.number = $(xml).find('number').text();
	subFct.fctId = $(xml).find('functionId').text();
	return subFct;	
}

function getSubFunctions(ufId)
{
	var subFcts = [];
	$.ajax({	
		type: "GET",
		crossDomain: true,
		dataType: "xml",
		crossDomain: true,
		url: KERETA_BASE_URL + "/UtilityFunction/" + ufId + "/SubFunction/",
		async: false,
		success: function(xml) 
		{
			$(xml).find('subFunction').each(function() {
					
				var subFct = getSubFctObject(this);
				subFcts.push(subFct);
			});
		},
		error: function(jqXHR, textStatus, errorThrown) {
			vShowAJAXError("Could not calculate utility. Check connection with Kereta Utility Calculation Service", jqXHR, errorThrown);
		}
	});
	return subFcts;
}

function getUtilityFunctions() {
	/*Invoking Kereta to obtain utility functions*/
	var ufs = [];
	$.ajax({
		type: "GET",
		crossDomain: true,
		dataType: "xml",
		crossDomain: true,
		url: KERETA_BASE_URL + "/UtilityFunction/",
		async: false,
		success: function(xml) 
		{
			$(xml).find('utilityFunction').each(function() {
				
				var uf = getUfObject(this);
				uf.subFct = getSubFunctions(uf.id);
				ufs.push(uf);
			});
		},
		error: function(jqXHR, textStatus, errorThrown) {
			
			vShowAJAXError("Errors contacting Kereta Service", jqXHR, errorThrown);
		}
	});
	return ufs;
}

function loadUtilityFunctions() {
	$("#keretaSelectUf").empty();
	var ufs = getUtilityFunctions();
	console.log("Utility Functions:");
	console.log(ufs);
	for (var i=0; i<ufs.length; i++)
	{
		var uf = ufs[i];
		$("#keretaSelectUf").append($.parseHTML("<option value='" + uf.id + "'>" + uf.alias + "</option>"));
	}
	//$("#keretaSelectUf").change(function() {
	//	loadUtilitySubFunctions($("#keretaSelectUf").val());
	//});
	//loadUtilitySubFunctions($("#keretaSelectUf").val());
}

function getFctObject(xml)
{
	var fct = new Object;
	fct.id = $(xml).find('id').text();
	fct.alias = $(xml).find('alias').text();
	fct.formula = $(xml).find('formula').text();
	fct.description = $(xml).find('description').text();
	fct.fctType = $(xml).find('functionType').text();
	fct.author = $(xml).find('author').text();
	fct.parameter = [];
	return fct;	
}

function getParmObject(xml)
{
	var parm = new Object;
	parm.fctId = $(xml).find('functionId').text();
	parm.name = $(xml).find('name').text();
	parm.description = $(xml).find('description').text();
	parm.dataType = $(xml).find('dataType').text();
	parm.author = $(xml).find('author').text();
	parm.def = $(xml).find('defaultValue').text();
	return parm;	
}

function getParameters(fctId)
{
	var parms = [];
	$.ajax({	
		type: "GET",
		crossDomain: true,
		dataType: "xml",
		crossDomain: true,
		url: KERETA_BASE_URL + "/Function/" + fctId + "/Parameter/",
		async: false,
		success: function(xml) 
		{
			$(xml).find('parameter').each(function() {
					
				var parm = getParmObject(this);
				parms.push(parm);
			});
		},
		error: function(jqXHR, textStatus, errorThrown) {
			vShowAJAXError("Could not calculate utility. Check connection with Kereta Utility Calculation Service", jqXHR, errorThrown);
		}
	});
	return parms;
}

function getFunction(fctId)
{
	var fct = null;
	$.ajax({	
		type: "GET",
		crossDomain: true,
		dataType: "xml",
		crossDomain: true,
		url: KERETA_BASE_URL + "/Function/" + fctId,
		async: false,
		success: function(xml) 
		{
			$(xml).find('function').each(function() {
					
				fct = getFctObject(this);
				fct.parameter = getParameters(fct.id);
			});
		},
		error: function(jqXHR, textStatus, errorThrown) {
			vShowAJAXError("Could not calculate utility. Check connection with Kereta Utility Calculation Service", jqXHR, errorThrown);
		}
	});
	return fct;
}

function processParameters(parametersInput) {
	parameters = "";
	console.log("Proessing parameters ");
	console.log(parametersInput);
	for (var i = 0; i < parametersInput.length; i++) {
		name = parametersInput[i].name;
		dataType = parametersInput[i].dataType;
		description = parametersInput[i].description;
		
		parameters = parameters + 
			"<div class='form-group'>" +
				"<label for='parameter_" + name + "' class='control-label'>" + name + "(" + dataType + "): " + "</label>" + 
				"<input type='text' id='parameter_" + name + "'>"
			"</div>";
			
	}
	return parameters;
}

function getNefologParameters(ufId, subfunctionId)
{
	var parms = [];
		
	$.ajax({	
		type: "GET",
		crossDomain: true,
		dataType: "xml",
		crossDomain: true,
		url: KERETA_BASE_URL + "/UtilityFunction/" + ufId + "/SubFunction/" + subfunctionId + "/NefologParameter/",
		async: false,
		success: function(xml) 
		{
			$(xml).find('parameter').each(function() {
				
				var parm = new Object;
				parm.fctId = "";
				parm.name = $(this).text();
				parm.description = "";
				parm.dataType = "see nefolog";
				parm.author = "nefolog";
				parm.def = "";
				
				parms.push(parm);
			});
		},
		error: function(jqXHR, textStatus, errorThrown) {
			vShowAJAXError("Could not calculate utility. Check connection with Kereta Utility Calculation Service", jqXHR, errorThrown);
			return [];
		}
	});
	return parms;
}

function createCostNefologFunction(ufId, nefologConfig, subfunctionNumber)
{
	var fct = null;
	$.ajax({	
		type: "POST",
		crossDomain: true,
		dataType: "xml",
		crossDomain: true,
		url: KERETA_BASE_URL + "/UtilityFunction/" + ufId + "/SubFunction/" + subfunctionNumber,
		async: false,
		success: function(xml) 
		{
			utilityId = $(xml).find('utilityFunctionId').text();
			subfunctionNumber = $(xml).find('number').text();
			fct = {
					ufId: utilityId,
					subfunctionNumber: subfunctionNumber
			};
		},
		error: function(jqXHR, textStatus, errorThrown) {
			vShowAJAXError("Could not create nefolog subfunction. Check connection with Kereta Utility Calculation Service", jqXHR, errorThrown);
		}
	});
	return fct;
}

function assignCostNefologFunction(ufId, nefologConfig, functionNumber)
{
	var res = null;
	var doc = $.parseXML("<subfunction/>");
	var functionId = doc.createElement("functionId");
	functionId.appendChild(doc.createTextNode(nefologConfig));
	doc.documentElement.appendChild(functionId);
	data = (new XMLSerializer()).serializeToString(doc);
	
	$.ajax({	
		type: "PUT",
		crossDomain: true,
		contentType: "application/xml",
		dataType: "xml",
		data: data,
		crossDomain: true,
		url: KERETA_BASE_URL + "/UtilityFunction/" + ufId + "/SubFunction/" + functionNumber,
		async: false,
		success: function(xml) 
		{
			utilityId = $(xml).find('utilityFunctionId').text();
			subfunctionId = $(xml).find('functionId').text();
			subfunctionNumber = $(xml).find('number').text();
			fct = {
					ufId: utilityId,
					subfunctionNumber: subfunctionNumber,
					subfunctionId: subfunctionId
			};
		},
		error: function(jqXHR, textStatus, errorThrown) {
			vShowAJAXError("Could not create nefolog subfunction. Check connection with Kereta Utility Calculation Service", jqXHR, errorThrown);
		}
	});
	return res;
}

var globalUtilitySubfunctionsDetail = [];
var globalUtilitySubfunctions = null;
var globalUtilityFunctionId = null;

function loadUtilitySubFunctions() {
	ufId = $("#keretaSelectUf").val();
	console.log("loadUtilitySubFunctions: utility function Id " + ufId);
	
	var nefologConfig = document.getElementById("nefolog_config").value;
	 
	if (nefologConfig) {
		console.log("loadUtilitySubFunctions: Nefolog Config Provided:");
		var resCreateNefologFunction = createCostNefologFunction(ufId, nefologConfig, "3");
		console.log(resCreateNefologFunction);
		var resAssignNefologFunction = assignCostNefologFunction(
				ufId, nefologConfig, "3");
		console.log(resAssignNefologFunction);
	}
	
	usfs = getSubFunctions(ufId);
	console.log("loadUtilitySubFunctions: utility subfunctions:");
	console.log(usfs);
	globalUtilitySubfunctions = usfs;
	globalUtilityFunctionId = ufId;
	console.log("loadUtilitySubFunctions: Value:");
	console.log($("#keretaSelectUf").val());
	console.log("loadUtilitySubFunctions: Text:");
	console.log($("#keretaSelectUf :selected").text());
	 
	var subFunctions = [];
	
	var builtForm = "";
	for (var i=0; i<usfs.length; i++) {
		
		var fctAlias = "";
		var fctDescription = "";
		var fct = null;
		
		if (usfs[i].fctId && usfs[i].fctId.startsWith("nefolog")) {
			console.log("loadUtilitySubFunctions: nefolog function");
			fct = {
					id: usfs[i].fctId,
					fctAlias: usfs[i].fctId,
					fctDescription: "Nefolog configuration " + usfs[i].fctId.split("$")[1],
					parameter: getNefologParameters(ufId, i+1)
			};
			fctAlias = fct.fctAlias;
			fctDescription = fct.fctDescription;
		}
		else {
			console.log("loadUtilitySubFunctions: kereta function");
			fct = getFunction(usfs[i].fctId);
			
			fctAlias =  ((fct && fct.alias) ? fct.alias : usfs[i].fctId);
			fctDescription =  ((fct && fct.description) ? fct.description : usfs[i].fctId);
		}
		
		console.log("loadUtilitySubFunctions: processed function:");
		console.log(fct);
		subFunctions.push(fct);
		
		var fctParameters = processParameters(fct.parameter);
				
		builtForm = builtForm + 
			//"<div class='col-lg-12'>" +
			"<h5 align=\"center\"> Utility Subfunction "+ (i + 1) + ": " + fctAlias + "</h5>" + 
			"<div class='form-group'>" +
			"<label for='fct" + i + "Description'>Description:</label> " + fctDescription + 
			"<input type='hidden' value='' id='ufId' />" +
			"<div class='form-group'>" +
			"<label for='fctAlias'>Parameters:</label>" +
			fctParameters
			"</div>" +
			//"</div>" +
			"</div>";
	}
	var form = $.parseHTML(builtForm);
	$("#utilitySubfunctions").empty();
	$("#utilitySubfunctions").append(form);
		
	globalUtilitySubfunctionsDetail = subFunctions;
	
	console.log("loadUtilitySubFunctions: globalUtilitySubfunctionsDetail = ");
	console.log(globalUtilitySubfunctionsDetail);
}

function createApplicationType(appType)
{
	var result = false;
	$.ajax({	
		type: "POST",
		dataType: "xml",
		url: KERETA_BASE_URL + "/Type/ApplicationType/" + appType,
		async: false,
		success: function(xml) 
		{
			result = true;
		},
		error: function(jqXHR, textStatus, errorThrown) {
			vShowAJAXError("Could not calculate utility. Check connection with Kereta Utility Calculation Service", jqXHR, errorThrown);
		}
	});
	return result;
}

function getAppObject(xml)
{
	var app = new Object;
	app.id = $(xml).find('id').text();
	app.alias = $(xml).find('alias').text();
	app.name = $(xml).find('name').text();
	app.description = $(xml).find('description').text();
	app.appType = $(xml).find('applicationType').text();
	app.author = $(xml).find('author').text();
	return app;	
}

function createApplication()
{
	var app;
	$.ajax({
		url: KERETA_BASE_URL + "/Application/",
		crossDomain: true,
		type: "POST",
		dataType: "xml",
		async: false,
		success: function(xml) 
		{
			app = getAppObject(xml.documentElement);
		},
		error: function(jqXHR, textStatus, errorThrown) {
			vShowAJAXError("Could not calculate utility. Check connection with Kereta Utility Calculation Service", jqXHR, errorThrown);
		}
	});
	return app;
}

function persistApplication(app)
{
	var result = false;
	if (app == null) return result;
		
	var doc = $.parseXML("<application/>");
	var name = doc.createElement("name");
	var alias = doc.createElement("alias");
	var appType = doc.createElement("applicationType");
	var desc = doc.createElement("description");
	var author = doc.createElement("author");
	
	name.appendChild(doc.createTextNode(app.name));
	alias.appendChild(doc.createTextNode(app.alias));
	appType.appendChild(doc.createTextNode(app.appType));
	desc.appendChild(doc.createTextNode(app.description));
	author.appendChild(doc.createTextNode(app.author));
	
	doc.documentElement.appendChild(name);
	doc.documentElement.appendChild(alias);
	doc.documentElement.appendChild(appType);
	doc.documentElement.appendChild(desc);
	doc.documentElement.appendChild(author);
	
	data = (new XMLSerializer()).serializeToString(doc);
	
	$.ajax({	
		url: KERETA_BASE_URL + "/Application/" + app.id,
		crossDomain: true,
		data: data,
		type: "PUT",
		contentType: "application/xml",
		dataType: "xml",
		
		async: false,
		success: function(xml) 
		{
			result = true;
		},
		error: function(jqXHR, textStatus, errorThrown) {
			vShowAJAXError("Could not calculate utility. Check connection with Kereta Utility Calculation Service", jqXHR, errorThrown);
			result = false;
		}
	});
	return result;
}

function getDstrObject(xml)
{
	var dstr = new Object;
	dstr.id = $(xml).find('id').text();
	dstr.alias = $(xml).find('alias').text();
	dstr.appId = $(xml).find('applicationId').text();
	dstr.representation = $(xml).find('representation').text();
	dstr.lang = $(xml).find('language').text();
	dstr.langVersion = $(xml).find('langVersion').text();
	dstr.author = $(xml).find('author').text();
	dstr.namespace = $(xml).find('namespace').text();
	return dstr;	
}

function persistDistribution(dstr)
{
	var result = false;
	if (dstr == null) return result;
		
	var doc = $.parseXML("<distribution/>");
	var repr = doc.createElement("representation");
	var alias = doc.createElement("alias");
	var ns = doc.createElement("namespace");
	var lang = doc.createElement("language");
	var langV = doc.createElement("langVersion");
	var author = doc.createElement("author");
	
	repr.appendChild(doc.createCDATASection(dstr.representation));
	alias.appendChild(doc.createTextNode(dstr.alias));
	lang.appendChild(doc.createTextNode(dstr.lang));
	langV.appendChild(doc.createTextNode(dstr.langVersion));
	author.appendChild(doc.createTextNode(dstr.author));
	ns.appendChild(doc.createTextNode(dstr.namespace));
	
	doc.documentElement.appendChild(repr);
	doc.documentElement.appendChild(alias);
	doc.documentElement.appendChild(ns);
	doc.documentElement.appendChild(lang);
	doc.documentElement.appendChild(langV);
	doc.documentElement.appendChild(author);
	
	data = (new XMLSerializer()).serializeToString(doc);
	
	$.ajax({
		url: KERETA_BASE_URL + "/Distribution/" + dstr.id,
		crossDomain: true,
		data: data,
		type: "PUT",
		contentType: "application/xml",
		dataType: "xml",
		
		async: false,
		success: function(xml) 
		{
			result = true;
		},
		error: function(jqXHR, textStatus, errorThrown) {
			vShowAJAXError("Could not calculate utility. Check connection with Kereta Utility Calculation Service", jqXHR, errorThrown);
			result = false;
		}
	});
	return result;
}

function createDistribution(appId)
{
	var dstr;
	$.ajax({
		url: KERETA_BASE_URL + "/Application/" + appId + "/Distribution",
		crossDomain: true,
		type: "POST",
		dataType: "xml",
		async: false,
		success: function(xml) 
		{
			dstr = getDstrObject(xml.documentElement);
		},
		error: function(jqXHR, textStatus, errorThrown) {
			vShowAJAXError("Could not calculate utility. Check connection with Kereta Utility Calculation Service", jqXHR, errorThrown);
		}
	});
	return dstr;
}

function cloneUtilityFunctionDist(ufId, distId) {
	var newUfId = "-1";
	$.ajax({
		url: KERETA_BASE_URL + "/UtilityFunction/" + ufId + "/clone?distribution=" + distId,
		crossDomain: true,
		type: "GET",
		dataType: "xml",
		async: false,
		success: function(xml) 
		{
			newUfId = $(xml).find('id').text();
		},
		error: function(jqXHR, textStatus, errorThrown) {
			vShowAJAXError("Could not calculate utility. Check connection with Kereta Utility Calculation Service", jqXHR, errorThrown);
		}
	});
	return newUfId;
}

function createKeretaResources(applicationType, applicationName, distributionName, ufId) {
	console.log("createKeretaResources: start");
	//createApplicationType(applicationType);
	console.log("createKeretaResources: utility function id: " + ufId);
	var app = createApplication();
	app.name = applicationName;
	app.appType = applicationType;
	app.alias = applicationName;
	console.log("createKeretaResources: application type");
	console.log(app.appType);
	persistApplication(app);
	appId = app.id;
	console.log("createKeretaResources: application id " + appId);
	console.log("createKeretaResources: application: ");
	console.log(app);
	
	//creating distribution
	var dist = createDistribution(appId);
	dist.alias = distributionName;
	console.log("createKeretaResources: distribution: ");
	console.log(dist);
	var res = persistDistribution(dist);
	console.log(res);
	
	//cloning utility function for distribution
	console.log("createKeretaResources: cloning utility function: " + ufId);
	var newUfId = cloneUtilityFunctionDist(ufId, dist.id);
	console.log("createKeretaResources: cloned utility function: " + newUfId);
	dist.ufId = newUfId;
	
	console.log("createKeretaResources: created resources: ");
	console.log(dist);
	return dist;
}

function uuidv4() {
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
    var r = Math.random() * 16 | 0, v = c == 'x' ? r : (r & 0x3 | 0x8);
    return v.toString(16);
  });
}



function assignSubfunctionParameters(key, utilityId, subfunctionId, subfunctionNumber, parameters) {
	console.log("assignSubfunctionParameters: utilityid " + utilityId + " subfunctionId " + subfunctionId);
	query = "";
	query += "key=" + key;
	
	for (var param=0; param<parameters.length; param++) {
		if (parameters[param].value != "") {
			parameter = parameters[param].name + "=" + parameters[param].value;
			query += "&" + parameter;
		}
	}
	
	console.log("assignSubfunctionParameters: query: " + query);
	
	var result = -1;
	$.ajax({	
		type: "GET",
		crossDomain: true,
		dataType: "xml",
		crossDomain: true,
		url: KERETA_BASE_URL + "/UtilityFunction/" + utilityId + "/SubFunction/" + subfunctionNumber + "/assign?" + query,
		async: false,
		success: function(xml) 
		{
			console.log("assignSubfunctionParameters: xml received: ");
			console.log(xml);
			$(xml).find('key').each(function() {
				result = $(this).text();
			});
		},
		error: function(jqXHR, textStatus, errorThrown) {
			result = -1;
			vShowAJAXError("Could not calculate utility. Check connection with Kereta Utility Calculation Service", jqXHR, errorThrown);
		}
	});
	return result;
}

function assignParametersValues(subfunctions, resources) {
	var key = "-1";
	var ufId = resources.ufId;
	generatedkey = uuidv4();
	for (var i=0; i<subfunctions.length; i++) {
		console.log("assignParametersValues: processing subfunction " + subfunctions[i].subfunctionId);
		var key = assignSubfunctionParameters(generatedkey, ufId, 
				subfunctions[i].subfunctionId, i+1, subfunctions[i].parameters);
		console.log("assignParametersValues: result assignment key " + key);
		if (key == "-1") {
			return "-1";
		}
	}
	return key;
}

function processUtilityCalculation(xml) {
	console.log("processUtilityCalculation: init");
	var utilityCalculation = new Object;
	utilityCalculation.distributionId = $(xml).find('distributionId').text();
	console.log("processUtilityCalculation: distributionId: ");
	console.log($(xml).find('distributionId').text());
	utilityCalculation.result = $(xml).find('result').first().text();
	
	console.log("processUtilityCalculation: subfunction calculations: ");
	//console.log($(xml).find('result').text());
	var subfunctionCalculations = [];
	var subcalculations = $(xml).find('subCalculations');
	$(subcalculations).find('calculation').each(function() {
		var subfunctionCalculation = new Object;
		subfunctionCalculation.number = $(this).attr('number');
		subfunctionCalculation.type = $(this).attr('type');
		subfunctionCalculation.result = $(this).find('result').text();
		subfunctionParameters = [];
		$(this).find('parameter').each(function() {
			var parameter = new Object;
			parameter.value = $(this).text();
			parameter.name = $(this).attr('name');
			subfunctionParameters.push(parameter);
		});
		subfunctionCalculation.parameters = subfunctionParameters;
		subfunctionCalculations.push(subfunctionCalculation);
	});
	console.log("processUtilityCalculation: subfunction calculations result: ");
	console.log(subfunctionCalculations);
	utilityCalculation.subfunctionCalculations = subfunctionCalculations;

	console.log("processUtilityCalculation: utility calculation: ");
	console.log(utilityCalculation);
	return utilityCalculation;	
}

function calculateKeretaUtility(utilityId, key) {
	var utilityCalculationRes = null;
	$.ajax({	
		type: "GET",
		crossDomain: true,
		dataType: "xml",
		crossDomain: true,
		url: KERETA_BASE_URL + "/UtilityFunction/" + utilityId + "/calc?key=" + key,
		async: false,
		success: function(xml) 
		{
			console.log("calculateKeretaUtility: xml received: ");
			console.log(xml);
			utilityCalculationRes = processUtilityCalculation(xml);
			utilityCalculationRes.ufId = utilityId;
		},
		error: function(jqXHR, textStatus, errorThrown) {
			utilityCalculationRes = "-1";
			vShowAJAXError("Could not calculate utility. Check connection with Kereta Utility Calculation Service", jqXHR, errorThrown);
		}
	});
	return utilityCalculationRes;
}

function deleteUtilityFunction(ufId) {
	var res = false;
	$.ajax({	
		type: "DELETE",
		crossDomain: true,
		dataType: "xml",
		crossDomain: true,
		url: KERETA_BASE_URL + "/UtilityFunction/" + ufId,
		async: false,
		success: function(xml) 
		{
			res = true;
		},
		error: function(jqXHR, textStatus, errorThrown) {
			vShowAJAXError("Could not calculate utility. Check connection with Kereta Utility Calculation Service", jqXHR, errorThrown);
			res = false;
		}
	});
	return res;
}

function deleteCostFunction(ufId, subfunctionNumber) {
	var res = false;
	$.ajax({	
		type: "DELETE",
		crossDomain: true,
		dataType: "xml",
		crossDomain: true,
		url: KERETA_BASE_URL + "/UtilityFunction/" + ufId + "/SubFunction/" + subfunctionNumber,
		async: false,
		success: function(xml) 
		{
			res = true;
		},
		error: function(jqXHR, textStatus, errorThrown) {
			vShowAJAXError("Could not calculate utility. Check connection with Kereta Utility Calculation Service", jqXHR, errorThrown);
			res = false;
		}
	});
	return res;
}
 
function displayUtilityCalculationResult(res) {
	console.log("displayUtilityCalculationResult: init");
	var result = res.result;
	var subfunctions = res.subfunctionCalculations;
	
	var resultForm = "<div class='form-group'>" + 
					"<h5 align=\"center\"> Utilty Calculation Result (Monetary) </h5>" +
					"<div class='form-group'>" +
					"<label for='utility_value' class='control-label'> Utility (Monetary Value): </label>" + result +
					"</div>";

	
	for (var i=0; i<subfunctions.length;i++) {
		var subfunctionName = subfunctions[i].type;
		var subfunctionResult = subfunctions[i].result;
		
		resultForm = resultForm + 
					"<div class='form-group'>" +
					"<label for='subfunction_" + subfunctionName + "' class='control-label'>" + subfunctionName + ": </label>" + subfunctionResult
				"</div>";
	}
	
	resultForm = resultForm + 
				"</div>";
	
	var form = $.parseHTML(resultForm);
	$("#utilityCalculationResult").empty();
	$("#utilityCalculationResult").append(form);
} 

function calculateUtility() {
	console.log("calculate utility: start");
	console.log("calculate utility: global utility subfunctions");
	console.log(globalUtilitySubfunctions);
	console.log("calculate utility: global utility subfunctions detail");
	console.log(globalUtilitySubfunctionsDetail);
	console.log("calculate utility: global utility function");
	console.log(globalUtilityFunctionId);
	
	console.log(document.getElementById("application_type"));
	console.log(document.getElementById("application_type").indexOf);
	console.log(document.getElementById("application_type").value);
	
	if (document.getElementById("application_type").value && 
			document.getElementById("application_name").value && 
			document.getElementById("distribution_name").value &&
			globalUtilityFunctionId != null ) {
		
		var applicationType = document.getElementById("application_type").value;
		var applicationName = document.getElementById("application_name").value;
		var distributionName = document.getElementById("distribution_name").value;
		
		var createdResources = createKeretaResources(applicationType, applicationName, distributionName, globalUtilityFunctionId);
		console.log("calculate utility: created kereta resources: ");
		console.log(createdResources);
		
		// Reading Parameters
		subFunctionsAssignment = [];
		for (var i=0; i<globalUtilitySubfunctionsDetail.length; i++) {
			subFunctionAssignment = {};
			if (globalUtilitySubfunctionsDetail[i] != null && globalUtilitySubfunctionsDetail[i].parameter && globalUtilitySubfunctionsDetail[i].id) {
				subFunctionAssignment.subfunctionId = globalUtilitySubfunctionsDetail[i].id;
				subFunctionAssignment.utilityId = createdResources.ufId;
				subFunctionAssignment.parameters = [];
				
				for (var j=0; j<globalUtilitySubfunctionsDetail[i].parameter.length; j++) {
					subFunctionAssignment.parameters.push(
						{
							name: globalUtilitySubfunctionsDetail[i].parameter[j].name,
							value: document.getElementById("parameter_" + globalUtilitySubfunctionsDetail[i].parameter[j].name).value
						}		
					);
				} 	
			}
			else {
				// nefolog function
				subFunctionAssignment.subfunctionId = globalUtilitySubfunctions[i].fctId;
				subFunctionAssignment.utilityId = createdResources.ufId;
				
			}
			subFunctionsAssignment.push(subFunctionAssignment);
		}
		console.log("calculate utility: subfunctions assignment");
		console.log(subFunctionsAssignment);
		
		var key = assignParametersValues(subFunctionsAssignment, createdResources);
		if (key != "-1") {
			var utilityCalculationRes = calculateKeretaUtility(createdResources.ufId, key);
			console.log("calculateUtility: utility calculation result: ");
			console.log(utilityCalculationRes);
			// Deleting preliminary utility function
			var resultUfCostDeletion = deleteCostFunction(utilityCalculationRes.ufId, "3");
			console.log("calculateUtility: removing utility subfunction cost: " + resultUfCostDeletion);
			var resultUfDeletion = deleteUtilityFunction(utilityCalculationRes.ufId);
			console.log("calculateUtility: removing utility function: " + resultUfDeletion);
			displayUtilityCalculationResult(utilityCalculationRes);
		}
	}
	else {
		alert("Please fill mandatory fields Application Type, Application Name, Distribution Name, and Utility Function");
	}
} 

function finishCalculation() {
	var resultUfCostDeletionOriginal = deleteCostFunction(globalUtilityFunctionId, "3");
	console.log("calculateUtility: removing original utility subfunction cost: " + resultUfCostDeletionOriginal);
}

function startUtilityCalculation(id, url) {
	
	console.log("UUUUUUUUUUUUUUUUU");
	console.log(id);
	console.log(url);
	
	showDiagUtility();
}

/*
 * THIS IS JUST A TEST TO PROVE THAT A SERVER RESPONDS TO CORS REQUESTS
 *
 */
//Create the XHR object.
function createCORSRequest(method, url) {
  var xhr = new XMLHttpRequest();
  if ("withCredentials" in xhr) {
    // XHR for Chrome/Firefox/Opera/Safari.
    xhr.open(method, url, true);
  } else if (typeof XDomainRequest != "undefined") {
    // XDomainRequest for IE.
    xhr = new XDomainRequest();
    xhr.open(method, url);
  } else {
    // CORS not supported.
    xhr = null;
  }
  return xhr;
}

// Helper method to parse the title tag from the response.
function getTitle(text) {
  return text.match('<title>(.*)?</title>')[1];
}

// Make the actual CORS request.
function makeCorsRequest() {
  // All HTML5 Rocks properties support CORS.
  var url = NEFOLOG_BASE_URL + '/nefolog/candidateSearch?servicetype=application&cpuCores=9&cpuSpeed=1500&io=moderate&memory=15&storage=600&bandwidth=400&transactions=5000&media=json';

  var xhr = createCORSRequest('GET', url);
  if (!xhr) {
    alert('CORS not supported');
    return;
  }

  // Response handlers.
  xhr.onload = function() {
   // var text = xhr.responseText;
    //var title = getTitle(text);
    alert('Response from CORS request to ' + url + ': ' + title);
  };

  xhr.onerror = function() {
    alert('Woops, there was an error making the request.');
  };

  xhr.send();
}

</script>
		