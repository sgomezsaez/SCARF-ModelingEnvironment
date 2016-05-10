<%@tag description="Queries the costs of a distribution of an app returned by the Similarity Engine" pageEncoding="UTF-8"%>
<%@taglib prefix="o"  tagdir="/WEB-INF/tags/common/orioneditor"%>
<%@taglib prefix="w"  tagdir="/WEB-INF/tags"%>

<div class="modal fade" id="displayCost">
	<div class="modal-dialog">
	<div class="modal-content">
			<div class="modal-header">
				<h4 class="modal-title">Candidate Offerings and Costs</h4>
				<div id="comp-cost" style ="display:none" >Requesting ...</div>
			</div>
			
			<div class="model-body">	
					<div class="container" style="width:80%;">					  
					  <div class="panel-group" id="accordion">
					  </div>
					</div>
					
			   </div>
			<div class="modal-footer">						
			    <button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
		     </div>	   
		</div>
				
		
	</div>
</div>
	

<script>
$("#displayCost").on("shown.bs.modal", function() {
	$("#displayCost").focus();
});

function showDiagCostNefolog(){
	$("#displayCost").modal("show");
}

/*
 * Performing the requests to Nefolog
 */
function showCostNefolog(id) {		
	var nefologURL="http://ec2-52-31-0-129.eu-west-1.compute.amazonaws.com:8080/nefolog/";
	
	/*Obtaining characteristics of the app to add them as parameters to Nefolog*/
	$.ajax({
		url: "/SimilarityEngine/application-knowledge/"+id,
		type: "GET",
		beforeSend:function(){
			$("#comp-cost").show();
			$("#idtimein").empty();
			$("#knowapp-Worklist0").empty();			
			$(".rowsk").empty();	
			
		},
		success: function(data, textStatus, jqXHR) {
			/*Building Nefolog URL with the data obtained from the SimEngineAPI*/
			var qStringNefolog=nefologURL+"candidateSearch?servicetype=application&io=moderate&media=json";
			
			//qStringNefolog=qStringNefolog+data[0].lmetricd[13].mean+'&cpuSpeed='+data[0].lmetricd[9].mean+'&io=moderate';
			//qStringNefolog=qStringNefolog+'&memory='+data[0].lmetricd[11].mean+'&storage='+data[0].lmetricd[10].mean;
			//qStringNefolog=qStringNefolog+'&bandwidth='+data[0].lmetricd[8].mean+'&transactions='+data[0].workload.avg_transactions_second+'&media=json';
			
			//var qStringCost="&GB="+data[0].lmetricd[10].mean+"&Transactions="+data[0].workload.avg_transactions_second+"&Server="+data[0].lmetricd[12].mean;
			var qStringCost="&Server=3";
			
			/*Invoking Nefolog  to obtain the candidate offerings and rendering the results*/
			$.ajax({
				url: qStringNefolog,
				type: "GET",
				crossDomain:true,	
				beforeSend:function(){
					$("#comp-cost").show();		
					$("#accordion").empty();
					
				},
				success: function(dataN, textStatus, jqXHR) {
					var lConfs= [];
					$("#comp-cost").hide();
					for(var k=0; k<dataN.candidates.length;k++){
						var indexSub=(dataN.candidates[k].configuration.uri).indexOf('_');
						lConfs[k]= (dataN.candidates[k].configuration.uri).substring(indexSub+1,dataN.candidates[k].configuration.uri.length);
						$("#accordion").append('<div class="panel panel-default" id="acco-inner-'+k+'"></div>');
						var uriConf= "'"+encodeURIComponent(dataN.candidates[k].configuration.uri)+"'";
						var uriCost= "'"+qStringCost+"'";
						$("#acco-inner-"+k).append('<div class="panel-heading"><a data-toggle="collapse" data-parent="#accordion" onclick="viewOffering('+uriConf+','+k+','+uriCost+','+lConfs[k]+');" href="#collap'+k+'">'+dataN.candidates[k].configuration.name+'</a></div>');
						$("#acco-inner-"+k).append('<div id="collap'+k+'" class="panel-collapse collapse"><div class="panel-body"></div></div>');
					}
					
				},
				error: function(jqXHR, textStatus, errorThrown) {
					
					vShowAJAXError("Errors contacting Nefolog Service", jqXHR, errorThrown);
				}
			});
			
						
			
		},
		error: function(jqXHR, textStatus, errorThrown) {
			
			vShowAJAXError("Errors contacting Nefolog Service", jqXHR, errorThrown);
		}
	});
	
	
	showDiagCostNefolog();
}

/**
 * Requirement performed when querying details of certain offering
 */
function viewOffering(offerURL,k,qofferCost,confid){
	
	var nefologURL="http://ec2-52-31-0-129.eu-west-1.compute.amazonaws.com:8080/nefolog";
	var nefologCostURL="http://ec2-52-31-0-129.eu-west-1.compute.amazonaws.com:8080/nefolog/costCalculator?configid="+confid+qofferCost;
				
			$.ajax({
				url: nefologURL+decodeURIComponent(offerURL),
				type: "GET",				
				crossDomain:true,				
				beforeSend:function(){
					$("#comp-cost").show();		
					$('#collap'+k).empty();
					
				},
				success: function(dataOf, textStatus, jqXHR) {
					//Rendering results
					var flag=false;
					$("#comp-cost").hide();
					$("#collap"+k).append("<p style='margin-left:2%'> <b>Performance Data:</b></p>")
					$("#collap"+k).append("<ul style='margin-left:2%'></ul>");
					$(dataOf).find('performance').each(function(){
					     	flag=true;
							$('#collap'+k+' ul').append('<li>'+$(this).find('name').text()+': '+$(this).find('value').text()+'</li>');
					});
					if(!flag){
						$("#collap"+k).append("<p style='margin-left:2%'>No Performance data.</p>");
					}
					var flagcost=false;
					$.ajax({
						url: nefologCostURL,
						type: "GET",				
						crossDomain:true,				
						beforeSend:function(){
							$("#comp-cost").show();	
														
						},						
						success: function(dataCost, textStatus, jqXHR) {
							$("#comp-cost").hide();	
							$(dataCost).find('result').each(function(){
								$("#comp-cost").hide();
								$("#collap"+k).append('<p style="margin-left:2%""><b>10-Month Total Cost: </b>'+ $(this).find("cost").first().contents().eq(0).text()+' <b> - Location: </b>'+$(this).find('location_zone').first().text()+' </p>');	
								flagcost=true;
								
							});							
							if(!flagcost){
								$("#collap"+k).append("<p style='margin-left:2%'>No cost data.</p>");
							}
							
						},
						error: function(jqXHR, textStatus, errorThrown) {
							
							vShowAJAXError("Errors contacting Nefolog Service", jqXHR, errorThrown);
						}
					});
					
								
				},
				error: function(jqXHR, textStatus, errorThrown) {
					
					vShowAJAXError("Errors contacting Nefolog Service", jqXHR, errorThrown);
				}
			});
			
		
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
  var url = 'http://ec2-52-31-0-129.eu-west-1.compute.amazonaws.com:8080/nefolog/candidateSearch?servicetype=application&cpuCores=9&cpuSpeed=1500&io=moderate&memory=15&storage=600&bandwidth=400&transactions=5000&media=json';

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
		