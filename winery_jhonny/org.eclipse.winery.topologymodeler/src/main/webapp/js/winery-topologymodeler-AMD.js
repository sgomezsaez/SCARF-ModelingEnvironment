/*******************************************************************************
 * Copyright (c) 2012-2015 University of Stuttgart.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and the Apache License 2.0 which both accompany this distribution,
 * and are available at http://www.eclipse.org/legal/epl-v10.html
 * and http://www.apache.org/licenses/LICENSE-2.0
 *
 * Contributors:
 *    Oliver Kopp - initial API and implementation and/or initial documentation
 *******************************************************************************/

/**
 * This file contains supporting functions for the topoplogy modeler
 */
define(
	// although XMLWriter ist not an AMD module, requirejs does not complain when loading it
	["winery-support-common", "XMLWriter"],
	function (w) {
		// has to be consistent with {@link org.eclipse.winery.common.constants.Namespaces}
		var TOSCA_NAMESPACE = "http://docs.oasis-open.org/tosca/ns/2011/12";
		var TOSCA_WINERY_EXTENSIONS_NAMESPACE ="http://www.opentosca.org/winery/extensions/tosca/2013/02/12";
		var TOSCA_SELFSERVICE_NAMESPACE ="http://www.eclipse.org/winery/model/selfservice";

		var SIMILARITY_ENGINE_BASE_URL="http://192.168.99.100:8095";
		var PERTOS_BASE_URL="http://192.168.99.100:8090";
		var NEFOLOG_BASE_URL="http://192.168.99.100:8080";
		
		var topologyTemplateURL;
		var topologyTemplateURLMU;
		var repositoryURL;
		var nameMU;
		var URLViewMU;
		var URLDistMU;

		var module = {
			save: save,
			setTopologyTemplateURL: function(url) {
				topologyTemplateURL = url;
			},
			setTopologyTemplateURLMU: function(url) {
				topologyTemplateURLMU = url;
			},
			setRepositoryURL: function(url) {
				repositoryURL = url;
			},
			setNameMU: function(name) {
				nameMU = name;
			},
			setURLViewMU: function(url) {
				URLViewMU = url;
			},
			setURLDistMU: function(url) {
				URLDistMU = url;
			},
			
			getTopologyTemplateAsXML: getTopologyTemplateAsXML,
			getXml:getXml,
			persistKnowledge:persistKnowledge,
			saveNewMU:saveNewMU,

			TOSCA_NAMESPACE: TOSCA_NAMESPACE,
			TOSCA_WINERY_EXTENSIONS_NAMESPACE: TOSCA_WINERY_EXTENSIONS_NAMESPACE,
			SIMILARITY_ENGINE_BASE_URL: SIMILARITY_ENGINE_BASE_URL,
			PERTOS_BASE_URL: PERTOS_BASE_URL,
			NEFOLOG_BASE_URL: NEFOLOG_BASE_URL
		};
		return module;

		function writeReqOrCaps(elements, xmlw, globalWrapperElementName, singleElementWrapperName) {
			if (elements.length != 0) {
				xmlw.writeStartElement(globalWrapperElementName);

				$.each(elements, function(i,e) {
					xmlw.writeStartElement(singleElementWrapperName);
					e = $(e);
					xmlw.writeAttributeString("id",   e.children(".id").text());
					xmlw.writeAttributeString("name", e.children(".name").text());
					writeType(xmlw, e.children(".type").children("a").data("qname"));
					savePropertiesFromDivToXMLWriter(e.children("div.propertiesContainer"), xmlw);
					xmlw.writeEndElement();
				});

				xmlw.writeEndElement();
			}

		}
		/*
		 * This functions adds to the XML document the portion describing the workload.
		 * The elements variable corresponds to the child html nodes that contain the 
		 * depicted information
		 * */
		function writeWorkload(elements, xmlw) {
			if (elements.length != 0) {
				xmlw.writeStartElement("Workload");
                //var f_id=1;
				//go through all the children
				$.each(elements, function(i,e) {
					e = $(e);
					//The id contains the name of the tag (pattern, avg_users,etc)
					xmlw.writeStartElement(e.attr("id"));
					xmlw.writeString(e.text());
					xmlw.writeEndElement();
				});

				xmlw.writeEndElement();
			}

		}
		
		/*
		 * This functions adds to the given XML Document, the portion describing
		 * the URLS and name of the solution
		 * */
		function writeSolution(xmlw){
			xmlw.writeStartElement("Solution");
			//Specifying the name given when saving the solution
			xmlw.writeStartElement("name");
			//The value of the name is set when calling (firing) the function
			xmlw.writeString(nameMU);
			xmlw.writeEndElement();
			//The view URL of the Topology
			xmlw.writeStartElement("view_url");
			xmlw.writeString(URLViewMU);
			xmlw.writeEndElement();
			//The host of distribution
			xmlw.writeStartElement("dist_url_host");
			xmlw.writeString("http%3A%2F%2Flocalhost%3A8080%2Fwinery%2F");
			xmlw.writeEndElement();
			//The Namespace of the new topology
			xmlw.writeStartElement("dist_url_nsmu");
			xmlw.writeString(URLDistMU);
			xmlw.writeEndElement();
			//The ID (used by Winery) of the new topology
			xmlw.writeStartElement("dist_url_muid");
			xmlw.writeString(nameMU);
			xmlw.writeEndElement();
			xmlw.writeEndElement();
			
		}
		
		/*
		 * This function adds to the given XML document the characteristics of performance
		 * according to specifications given in the thesis
		 * */
		
		function writePerformance(xmlw) {
			//The metric categories that are being used
			var metric_cats= ['Time Behaviour', 'Capacity', 'Resource Utilization', 'Scalability', 'Availability'];
			//IDs of the divs containing the hidden the other divs that have the values of the metrics
			var metric_ids=['time_behaviour', 'capacity', 'resource_utilization', 'scalability', 'availability'];
			//The start element performance
			xmlw.writeStartElement("Performance");
			//We iterate over the list of categories
			for (var l=0; l<metric_cats.length; l++){
				//Add the elements of the list as starting tags
				xmlw.writeStartElement(metric_ids[l]);
				//getting the child nodes of the div with that ID
				var elementsPT = $('#'+metric_ids[l]).children("div");
				
				$.each(elementsPT, function(i,el) {
					el = $(el);
					//here we obtain the name of the metric
					xmlw.writeStartElement(el.attr("id"));
					
					var elementsSP= el.children("span");
					
					$.each(elementsSP, function(m,els) {
						els=$(els);
						//With the class we create the min, max, etc element
						xmlw.writeStartElement(els.attr("class"));
						xmlw.writeString(els.text());
						xmlw.writeEndElement();						
					});
					xmlw.writeEndElement();										
				});		
				xmlw.writeEndElement();	
			}
			xmlw.writeEndElement();
			

		}

		/**
		 * "doSave"
		 */
		function save() {
			$("#saveBtn").button('loading');
			
			$.ajax({
				url: topologyTemplateURL,
				type: "PUT",
				contentType: 'text/xml',
				data: getTopologyTemplateAsXML(false),
				success: function(data, textStatus, jqXHR) {
					$("#saveBtn").button('reset');
					vShowSuccess("successfully saved.");
				},
				error: function(jqXHR, textStatus, errorThrown) {
					$("#saveBtn").button('reset');
					vShowAJAXError("Could not save", jqXHR, errorThrown);
				}
			});
		}
		
		/*
		 *This function is use to  save the new refined topology in the Winery repository  
		 * */
		
		function saveNewMU() {
			
			$.ajax({
				url: topologyTemplateURLMU,
				type: "PUT",
				contentType: 'text/xml',
				data: getTopologyTemplateAsXML(false),
				success: function(data, textStatus, jqXHR) {
					//$("#saveBtn").button('reset');
					//vShowSuccess("successfully saved.");
					
				},
				error: function(jqXHR, textStatus, errorThrown) {
					$("#saveBtn").button('reset');
					vShowAJAXError("Could not save", jqXHR, errorThrown);
				}
			});
			
		}
		/*This is the function invoked when selecting Discover Similar Apps*/
		function getXml(){
						
			/*Invoking PERTOS Service*/
			var idAlpha;			
			var listSimilar=[];
			var index=0;
			//alert(alphaTopologyPertos());
			$.ajax({
				url: PERTOS_BASE_URL + "/pertos/topology/alphatopology",
				type: "POST",
				contentType: 'application/xml',
				data: alphaTopologyPertos(),
				beforeSend:function(){
					$("#comp-sim").show();
					$("#simapp-list").empty();
					
				},
				success: function(data, textStatus, jqXHR) {
					//Obtaining the ID assigned by PERTOS
				    idAlpha = $(data).find('id').text();
				    
				    //The Id can be set to a fixed one so no new topologies are created
				    //in PERTOS when testing
			 		//idAlpha=7;
				    
				    //Putting the ID in a hidden field to use it later
				    $("#newAlphaIDHidden").val(idAlpha);
							
				    //Invoking PERTOS to get the functional similar topologies
					$.ajax({
						url: PERTOS_BASE_URL + "/pertos/similartopology/"+idAlpha,
						type: "GET",
						success: function(dataSim, textStatus, jqXHR) {
							//Extracting from the XML just the IDs of applications that are sent
							//to SimilarityEngine 
							$(dataSim).find('specification').each(function(){
								listSimilar[index]=$(this).attr("alphaTopologyId");
								index=index+1;							
							});
							
								/*Invoking SimilarityEngine Service*/
								$.ajax({
									url: SIMILARITY_ENGINE_BASE_URL + "/SimilarityEngine/application-discoverability",
									type: "POST",
									contentType: 'text/xml',
									data: getSimilarityDataXMLQuerySE(listSimilar),							
									success: function(data, textStatus, jqXHR) {
										$("#comp-sim").hide();
										
										//Rendering the results in the panel
										showSimilarityData(data,repositoryURL);
										
									},
									error: function(jqXHR, textStatus, errorThrown) {
										
										vShowAJAXError("Errors contacting Similarity Service", jqXHR, errorThrown);
									}
								});				    
							
						},
						error: function(jqXHR, textStatus, errorThrown) {
							
							vShowAJAXError("Errors contacting Similarity Service", jqXHR, errorThrown);
						}
					});
					
				},
				error: function(jqXHR, textStatus, errorThrown) {
					
					vShowAJAXError("Errors contacting Similarity Service", jqXHR, errorThrown);
				}
			});
			
		}
		
		/*
		 * This functions performs the requirement to save the non functional characteristics
		 * of a depicted app 
		 */
		 
		function persistKnowledge(){
			// Assign handlers immediately after making the request,
			// and remember the jqxhr object for this request
			
			$.ajax({
						url: SIMILARITY_ENGINE_BASE_URL + "/SimilarityEngine/application-knowledge",
						type: "POST",
						contentType: 'text/xml',
						data: getSimilarityDataXML(),
						beforeSend:function(){
							//$("#persistBtn").button('loading');
							
						},
						success: function(data, textStatus, jqXHR) {
							
							vShowSuccess("Successfully stored in Knowledge Base");
							
						},
						error: function(jqXHR, textStatus, errorThrown) {
							
							vShowAJAXError("Errors contacting Similarity Service", jqXHR, errorThrown);
						}
					});
		}

		/*
		 * This function builds the XML document that is send as data to persist the 
		 * knowledge of an app
		 * */
        function getSimilarityDataXML(){
        	var xmlw = new XMLWriter("utf-8");
			xmlw.writeStartDocument();		
			//root element
        	xmlw.writeStartElement("SimilarityData");
        	xmlw.writeStartElement("appAlphaID");
        	//getting the ID from the hidden field (tis was obtained from PERTOS when
        	//requesting the similar functional apps)
			xmlw.writeString($("#idAlphaPertos").val())
			xmlw.writeEndElement();
			//Writing the workload,the id of the html input element is given as paraneter 
        	writeWorkload($("#workloadPropContainer").children(),xmlw);
        	//Writing the performance
        	writePerformance(xmlw);
        	//Writing the solution
        	writeSolution(xmlw);
        	//Closing the document
        	xmlw.writeEndElement();
        	return xmlw.flush();
        }
        
        /*
         * Similar to the previous function but this does not write an ID of the app
         * nor the solution, but writesthe list of similar application returned by PERTOS
         * Used to discover the Similar Apps
         * */
        function getSimilarityDataXMLQuerySE(lAlpha){
        	var xmlw = new XMLWriter("utf-8");
			xmlw.writeStartDocument();
			
        	xmlw.writeStartElement("SimilarityData");
        	//Writing the list of similar functional apps
        	writeSimilarAlpha(xmlw,lAlpha);
        	writeWorkload($("#workloadPropContainer").children(),xmlw);
        	writePerformance(xmlw);
        	//writeSolution(xmlw);
        	xmlw.writeEndElement();
        	return xmlw.flush();
        }
        
        /*
         * Write the list of similar functional apps in the XML document 
         * */
        function writeSimilarAlpha(xmlw,lAlpha){
        	xmlw.writeStartElement("SimilarApps");
        	for(var si=0; si<lAlpha.length; si++ ){
        		xmlw.writeStartElement("app_id");
        		xmlw.writeString(lAlpha[si]);
        		xmlw.writeEndElement();
        	}
        	xmlw.writeEndElement();
        	 
        }
        
        /*
         * Prepares the XML document that describes an alpha topology
         * according to the PERTOS specifications.
         * */
        function alphaTopologyPertos(){
        	var xmlw = new XMLWriter("utf-8");
        	xmlw.writeStartDocument();
        	xmlw.writeStartElement("AlphaTopologyTemplate");
        	xmlw.writeStartElement("specificationType");
        	xmlw.writeString("xml");
        	xmlw.writeEndElement();
        	xmlw.writeStartElement("specification");
        	//Getting the service template
        	getTopologyTemplateAsXMLNoRoot(true,xmlw);
        	xmlw.writeEndElement();
        	//Writing the node level according to PERTOS specifications
        	writeNodeLevel(xmlw);
        
        	xmlw.writeEndElement();
        	xmlw.writeEndDocument(); 
        	return xmlw.flush();
        }
        
        /*
         * Adds the nodel level to the XML document that describes the alpha topology
         * */
        function writeNodeLevel(xmlw){
        	var root=true;
        	xmlw.writeStartElement("nodelevel");
        	$("div.NodeTemplateShape").not(".hidden").each (function() {
				var headerContainer = $(this).children("div.headerContainer");				
				var typeQNameStr = headerContainer.children("span.typeQName").text();
				
				xmlw.writeStartElement("node");
				var qname = getQName(typeQNameStr);
	        	xmlw.writeAttributeString("id", qname.localName);
	        	xmlw.writeStartElement("level");
	        	if(root){
	        		xmlw.writeString("root");
	        		root =false;
	        	}else{
	        		xmlw.writeString("leaf");}
	        	xmlw.writeEndElement();
	        	xmlw.writeEndElement();
	        	
				
				
        	});
        	xmlw.writeEndElement();
        	
        }
		function getTopologyTemplateAsXMLNoRoot(needsDefinitionsTag,xmlw) {

			

			if (needsDefinitionsTag) {
				xmlw.writeStartElement("tosca:Definitions");
				xmlw.writeAttributeString("xmlns:tosca", TOSCA_NAMESPACE);
				xmlw.writeAttributeString("xmlns:ns0",TOSCA_SELFSERVICE_NAMESPACE);
				xmlw.writeAttributeString("xmlns:winery", TOSCA_WINERY_EXTENSIONS_NAMESPACE);

				xmlw.writeStartElement("tosca:ServiceTemplate");
				xmlw.writeAttributeString("id", $("#nameDistribution").val());
				xmlw.writeAttributeString("name", $("#nameDistribution").val());
				xmlw.writeAttributeString("targetNamespace",TOSCA_SELFSERVICE_NAMESPACE);
				//xmlw.writeAttributeString("xmlns", TOSCA_NAMESPACE);
				//xmlw.writeAttributeString("xmlns:winery", TOSCA_WINERY_EXTENSIONS_NAMESPACE);
			}
			xmlw.writeStartElement("tosca:TopologyTemplate");
			xmlw.writeAttributeString("xmlns", TOSCA_NAMESPACE);
			xmlw.writeAttributeString("xmlns:winery", TOSCA_WINERY_EXTENSIONS_NAMESPACE);
			
			/**Workload**/
			//writeWorkload($("#workloadPropContainer").children(),xmlw);
			
			$("div.NodeTemplateShape").not(".hidden").each (function() {
				xmlw.writeStartElement("tosca:NodeTemplate");

				var id = $(this).attr("id");

				var headerContainer = $(this).children("div.headerContainer");
				var name = headerContainer.children("div.name").text();
				var typeQNameStr = headerContainer.children("span.typeQName").text();
				var minmaxdiv = headerContainer.children("div.minMaxInstances");
				var min = minmaxdiv.children("span.minInstances").text();
				var max = minmaxdiv.children("span.maxInstances").text();
				if (max == "∞") {
					max = "unbounded";
				}
				var x = $(this).css("left");
				x = x.substring(0, x.indexOf("px"));
				var y = $(this).css("top");
				y = y.substring(0, y.indexOf("px"));

				xmlw.writeAttributeString("id", id);
				if (name != "") {
					xmlw.writeAttributeString("name", name);
				}
				//writeType(xmlw, typeQNameStr);
				//xmlw.writeAttributeString("ns0", typeQNameStr);
				writeQNameAttributeNoNS(xmlw, "ns0", typeQNameStr);
				if (min != "") {
					xmlw.writeAttributeString("minInstances", min);
				}
				if (max != "") {
					xmlw.writeAttributeString("maxInstances", max);
				}
				xmlw.writeAttributeString("winery:x", x);
				xmlw.writeAttributeString("winery:y", y);

				/** Properties **/
				savePropertiesFromDivToXMLWriter($(this).children("div.propertiesContainer"), xmlw);

				/** Requirements **/
				writeReqOrCaps(
					$(this).children("div.requirementsContainer").children("div.content").children("div.reqorcap"),
					xmlw,
					"Requirements",
					"Requirement");

				/** Capabilities **/
				writeReqOrCaps(
					$(this).children("div.capabilitiesContainer").children("div.content").children("div.reqorcap"),
					xmlw,
					"Capabilities",
					"Capability");
				
				

				/** Policies **/
				w.writeCollectionDefinedByATextArea(xmlw,
						$(this).children("div.policiesContainer").children("div.content").children("div.policy"),
						"Policies");

				/** Deployment Artifacts **/
				var das = $(this).children("div.deploymentArtifactsContainer").children("div.content").children("div.deploymentArtifact");
				if (das.length != 0) {
					xmlw.writeStartElement("DeploymentArtifacts");
					das.each(function(i,e) {
						// the textarea contains a valid deployment artifact xml
						var xml = $(e).children("textarea").val();
						xmlw.writeXML(xml);
					});
					xmlw.writeEndElement();
				}

				// End: Nodetemplate
				xmlw.writeEndElement();
			});
			jsPlumb.select().each(function(connection) {
				xmlw.writeStartElement("tosca:RelationshipTemplate");
				var id = connection.id;
				var typeQNameStr = connection.getType()[0];

				var connData = winery.connections[id];
				if (!connData) {
					vShowError("Error in the internal data structure: Id " + id + " not found");
					return;
				}

				xmlw.writeAttributeString("id", connData.id);
				if (connData.name != "") {
					xmlw.writeAttributeString("name", connData.name);
				}
				//xmlw.writeAttributeString("type", connData.name);
				//xmlw.writeAttributeString("winery", typeQNameStr);
				writeQNameAttributeNoNS(xmlw, "winery", typeQNameStr);
				//writeType(xmlw, typeQNameStr);

				if (typeof connData.propertiesContainer !== "undefined") {
					savePropertiesFromDivToXMLWriter(connData.propertiesContainer, xmlw);
				}

				xmlw.writeStartElement("tosca:SourceElement");
				if (connData.req) {
					// conn starts at a requirement
					xmlw.writeAttributeString("ref", connData.req);
				} else {
					// conn starts at a node template
					xmlw.writeAttributeString("ref", connection.sourceId);
				}
				xmlw.writeEndElement();
				xmlw.writeStartElement("tosca:TargetElement");
				if (connData.cap) {
					// conn ends at a capability
					xmlw.writeAttributeString("ref", connData.cap);
				} else {
					// conn ends at a node template
					xmlw.writeAttributeString("ref", connection.targetId);
				}
				xmlw.writeEndElement();

				xmlw.writeEndElement();
			});

			if (needsDefinitionsTag) {
				xmlw.writeEndElement();
				xmlw.writeEndElement();
			}

			

			return xmlw.flush();
		}
		
		/**
		 * Creates an XML String of the modelled topology template.
		 */
		function getTopologyTemplateAsXML(needsDefinitionsTag) {

			var xmlw = new XMLWriter("utf-8");
			xmlw.writeStartDocument();

			if (needsDefinitionsTag) {
				xmlw.writeStartElement("Definitions");
				xmlw.writeAttributeString("xmlns", TOSCA_NAMESPACE);
				xmlw.writeAttributeString("xmlns:winery", TOSCA_WINERY_EXTENSIONS_NAMESPACE);

				xmlw.writeStartElement("ServiceTemplate");
				xmlw.writeAttributeString("xmlns", TOSCA_NAMESPACE);
				xmlw.writeAttributeString("xmlns:winery", TOSCA_WINERY_EXTENSIONS_NAMESPACE);
			}
			xmlw.writeStartElement("TopologyTemplate");
			xmlw.writeAttributeString("xmlns", TOSCA_NAMESPACE);
			xmlw.writeAttributeString("xmlns:winery", TOSCA_WINERY_EXTENSIONS_NAMESPACE);
			
			/**Workload**/
			//writeWorkload($("#workloadPropContainer").children(),xmlw);
			
			$("div.NodeTemplateShape").not(".hidden").each (function() {
				xmlw.writeStartElement("NodeTemplate");

				var id = $(this).attr("id");

				var headerContainer = $(this).children("div.headerContainer");
				var name = headerContainer.children("div.name").text();
				var typeQNameStr = headerContainer.children("span.typeQName").text();
				var minmaxdiv = headerContainer.children("div.minMaxInstances");
				var min = minmaxdiv.children("span.minInstances").text();
				var max = minmaxdiv.children("span.maxInstances").text();
				if (max == "∞") {
					max = "unbounded";
				}
				var x = $(this).css("left");
				x = x.substring(0, x.indexOf("px"));
				var y = $(this).css("top");
				y = y.substring(0, y.indexOf("px"));

				xmlw.writeAttributeString("id", id);
				if (name != "") {
					xmlw.writeAttributeString("name", name);
				}
				writeType(xmlw, typeQNameStr);
				if (min != "") {
					xmlw.writeAttributeString("minInstances", min);
				}
				if (max != "") {
					xmlw.writeAttributeString("maxInstances", max);
				}
				xmlw.writeAttributeString("winery:x", x);
				xmlw.writeAttributeString("winery:y", y);

				/** Properties **/
				savePropertiesFromDivToXMLWriter($(this).children("div.propertiesContainer"), xmlw);

				/** Requirements **/
				writeReqOrCaps(
					$(this).children("div.requirementsContainer").children("div.content").children("div.reqorcap"),
					xmlw,
					"Requirements",
					"Requirement");

				/** Capabilities **/
				writeReqOrCaps(
					$(this).children("div.capabilitiesContainer").children("div.content").children("div.reqorcap"),
					xmlw,
					"Capabilities",
					"Capability");
				
				

				/** Policies **/
				w.writeCollectionDefinedByATextArea(xmlw,
						$(this).children("div.policiesContainer").children("div.content").children("div.policy"),
						"Policies");

				/** Deployment Artifacts **/
				var das = $(this).children("div.deploymentArtifactsContainer").children("div.content").children("div.deploymentArtifact");
				if (das.length != 0) {
					xmlw.writeStartElement("DeploymentArtifacts");
					das.each(function(i,e) {
						// the textarea contains a valid deployment artifact xml
						var xml = $(e).children("textarea").val();
						xmlw.writeXML(xml);
					});
					xmlw.writeEndElement();
				}

				// End: Nodetemplate
				xmlw.writeEndElement();
			});
			jsPlumb.select().each(function(connection) {
				xmlw.writeStartElement("RelationshipTemplate");
				var id = connection.id;
				var typeQNameStr = connection.getType()[0];

				var connData = winery.connections[id];
				if (!connData) {
					vShowError("Error in the internal data structure: Id " + id + " not found");
					return;
				}

				xmlw.writeAttributeString("id", connData.id);
				if (connData.name != "") {
					xmlw.writeAttributeString("name", connData.name);
				}
				writeType(xmlw, typeQNameStr);

				if (typeof connData.propertiesContainer !== "undefined") {
					savePropertiesFromDivToXMLWriter(connData.propertiesContainer, xmlw);
				}

				xmlw.writeStartElement("SourceElement");
				if (connData.req) {
					// conn starts at a requirement
					xmlw.writeAttributeString("ref", connData.req);
				} else {
					// conn starts at a node template
					xmlw.writeAttributeString("ref", connection.sourceId);
				}
				xmlw.writeEndElement();
				xmlw.writeStartElement("TargetElement");
				if (connData.cap) {
					// conn ends at a capability
					xmlw.writeAttributeString("ref", connData.cap);
				} else {
					// conn ends at a node template
					xmlw.writeAttributeString("ref", connection.targetId);
				}
				xmlw.writeEndElement();

				xmlw.writeEndElement();
			});

			if (needsDefinitionsTag) {
				xmlw.writeEndElement();
				xmlw.writeEndElement();
			}

			xmlw.writeEndDocument();

			return xmlw.flush();
		}

		function writeQNameAttribute(w, nsPrefix, qnameStr) {
			var qname = getQName(qnameStr);
			w.writeAttributeString("xmlns:" + nsPrefix, qname.namespace);
			w.writeAttributeString("type", nsPrefix + ":" + qname.localName);
		}
		
		function writeQNameAttributeNoNS(w, nsPrefix, qnameStr) {
			var qname = getQName(qnameStr);
			//w.writeAttributeString("xmlns:" + nsPrefix, qname.namespace);
			w.writeAttributeString("type", nsPrefix + ":" + qname.localName);
		}

		function writeType(w, typeQNameStr) {
			writeQNameAttribute(w, "ty", typeQNameStr);
		}
		
		
	}
);

