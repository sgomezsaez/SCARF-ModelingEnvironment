/*******************************************************************************
 * Copyright (c) 2012-2013,2015 University of Stuttgart.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and the Apache License 2.0 which both accompany this distribution,
 * and are available at http://www.eclipse.org/legal/epl-v10.html
 * and http://www.apache.org/licenses/LICENSE-2.0
 *
 * Contributors:
 *     Kálmán Képes - initial API and implementation and/or initial documentation
 *     Oliver Kopp - adapted to new storage model and to TOSCA v1.0
 *******************************************************************************/
package org.eclipse.winery.repository.importing;

import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.FileVisitResult.SKIP_SUBTREE;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.ws.rs.core.MediaType;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.io.FilenameUtils;
import org.eclipse.winery.common.ModelUtilities;
import org.eclipse.winery.common.RepositoryFileReference;
import org.eclipse.winery.common.Util;
import org.eclipse.winery.common.constants.MimeTypes;
import org.eclipse.winery.common.constants.Namespaces;
import org.eclipse.winery.common.ids.XMLId;
import org.eclipse.winery.common.ids.definitions.ArtifactTemplateId;
import org.eclipse.winery.common.ids.definitions.EntityTypeId;
import org.eclipse.winery.common.ids.definitions.NodeTypeId;
import org.eclipse.winery.common.ids.definitions.RelationshipTypeId;
import org.eclipse.winery.common.ids.definitions.ServiceTemplateId;
import org.eclipse.winery.common.ids.definitions.TOSCAComponentId;
import org.eclipse.winery.common.ids.definitions.imports.GenericImportId;
import org.eclipse.winery.common.ids.definitions.imports.XSDImportId;
import org.eclipse.winery.common.ids.elements.PlanId;
import org.eclipse.winery.common.ids.elements.PlansId;
import org.eclipse.winery.common.propertydefinitionkv.WinerysPropertiesDefinition;
import org.eclipse.winery.model.csar.toscametafile.TOSCAMetaFile;
import org.eclipse.winery.model.csar.toscametafile.TOSCAMetaFileParser;
import org.eclipse.winery.model.tosca.Definitions;
import org.eclipse.winery.model.tosca.TArtifactReference;
import org.eclipse.winery.model.tosca.TArtifactReference.Exclude;
import org.eclipse.winery.model.tosca.TArtifactReference.Include;
import org.eclipse.winery.model.tosca.TArtifactTemplate;
import org.eclipse.winery.model.tosca.TArtifactTemplate.ArtifactReferences;
import org.eclipse.winery.model.tosca.TDefinitions;
import org.eclipse.winery.model.tosca.TDefinitions.Types;
import org.eclipse.winery.model.tosca.TEntityType;
import org.eclipse.winery.model.tosca.TEntityType.PropertiesDefinition;
import org.eclipse.winery.model.tosca.TExtensibleElements;
import org.eclipse.winery.model.tosca.TImport;
import org.eclipse.winery.model.tosca.TNodeType;
import org.eclipse.winery.model.tosca.TPlan;
import org.eclipse.winery.model.tosca.TPlan.PlanModelReference;
import org.eclipse.winery.model.tosca.TPlans;
import org.eclipse.winery.model.tosca.TRelationshipType;
import org.eclipse.winery.model.tosca.TServiceTemplate;
import org.eclipse.winery.repository.Constants;
import org.eclipse.winery.repository.JAXBSupport;
import org.eclipse.winery.repository.Utils;
import org.eclipse.winery.repository.backend.BackendUtils;
import org.eclipse.winery.repository.backend.Repository;
import org.eclipse.winery.repository.backend.constants.Filename;
import org.eclipse.winery.repository.backend.filebased.FileUtils;
import org.eclipse.winery.repository.datatypes.ids.elements.ArtifactTemplateDirectoryId;
import org.eclipse.winery.repository.datatypes.ids.elements.SelfServiceMetaDataId;
import org.eclipse.winery.repository.datatypes.ids.elements.VisualAppearanceId;
import org.eclipse.winery.repository.export.CSARExporter;
import org.eclipse.winery.repository.resources.admin.NamespacesResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

/**
 * Imports a CSAR into the storage. As the internal storage format does not have
 * CSARs as the topmost artifacts, but one TDefinition, the CSAR has to be split
 * up into several components.
 * 
 * Existing components are <em>not</em> replaced, but silently skipped
 * 
 * Minor errors are logged and not further propagated / notified. That means, a
 * user cannot see minor errors. Major errors are immediately thrown.
 * 
 * One instance for each import
 */
public class CSARImporter {
	
	private static final Logger logger = LoggerFactory.getLogger(CSARImporter.class);
	
	// ExecutorService for XSD schema initialization
	// Threads set to 1 to avoid testing for parallel processing of the same XSD file
	private static final ExecutorService xsdParsingService = Executors.newFixedThreadPool(1);
	
	private static final ExecutorService entityTypeAdjestmentService = Executors.newFixedThreadPool(10);
	
	
	/**
	 * Reads the CSAR from the given inputstream
	 * 
	 * @param in the inputstream to read from
	 * @param errorList the list of errors during the import. Has to be non-null
	 * @param overwrite if true: contents of the repo are overwritten
	 * 
	 * @throws InvalidCSARException if the CSAR is invalid
	 */
	public void readCSAR(InputStream in, List<String> errors, boolean overwrite, final boolean asyncWPDParsing) throws IOException {
		// we have to extract the file to a temporary directory as
		// the .definitions file does not necessarily have to be the first entry in the archive
		Path csarDir = Files.createTempDirectory("winery");
		
		try (ZipInputStream zis = new ZipInputStream(in)) {
			ZipEntry entry;
			while ((entry = zis.getNextEntry()) != null) {
				if (!entry.isDirectory()) {
					Path targetPath = csarDir.resolve(entry.getName());
					Files.createDirectories(targetPath.getParent());
					Files.copy(zis, targetPath);
				}
			}
			this.importFromDir(csarDir, errors, overwrite, asyncWPDParsing);
		} catch (Exception e) {
			CSARImporter.logger.debug("Could not import CSAR", e);
			throw e;
		} finally {
			// cleanup: delete all contents of the temporary directory
			FileUtils.forceDelete(csarDir);
		}
	}
	
	/**
	 * Import an extracted CSAR from a directory
	 * 
	 * @param path the root path of an extracted CSAR file
	 * @param overwrite if true: contents of the repo are overwritten
	 * @param asyncWPDParsing true if WPD should be parsed asynchronously to
	 *            speed up the import. Required, because JUnit terminates the
	 *            used ExecutorService
	 * @throws InvalidCSARException
	 * @throws IOException
	 */
	void importFromDir(final Path path, final List<String> errors, final boolean overwrite, final boolean asyncWPDParsing) throws IOException {
		Path toscaMetaPath = path.resolve("TOSCA-Metadata/TOSCA.meta");
		if (!Files.exists(toscaMetaPath)) {
			errors.add("TOSCA.meta does not exist");
			return;
		}
		final TOSCAMetaFileParser tmfp = new TOSCAMetaFileParser();
		final TOSCAMetaFile tmf = tmfp.parse(toscaMetaPath);
		
		// we do NOT do any sanity checks, of TOSAC.meta
		// and just start parsing
		
		if (tmf.getEntryDefinitions() != null) {
			// we obey the entry definitions and "just" import that
			// imported definitions are added recursively
			Path defsPath = path.resolve(tmf.getEntryDefinitions());
			this.importDefinitions(tmf, defsPath, errors, overwrite, asyncWPDParsing);
			
			this.importSelfServiceMetaData(tmf, path, defsPath, errors);
		} else {
			// no explicit entry definitions found
			// we import all available definitions
			// The specification says (cos01, Section 16.1, line 2935) that all definitions are contained in the "Definitions" directory
			// The alternative is to go through all entries in the TOSCA Meta File, but there is no guarantee that this list is complete
			Path definitionsDir = path.resolve("Definitions");
			if (!Files.exists(definitionsDir)) {
				errors.add("No entry definitions defined and Definitions directory does not exist.");
				return;
			}
			final List<IOException> exceptions = new ArrayList<IOException>();
			Files.walkFileTree(definitionsDir, new SimpleFileVisitor<Path>() {
				
				@Override
				public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
					if (dir.endsWith("Definitions")) {
						return FileVisitResult.CONTINUE;
					} else {
						return FileVisitResult.SKIP_SUBTREE;
					}
				}
				
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
					try {
						CSARImporter.this.importDefinitions(tmf, file, errors, overwrite, asyncWPDParsing);
					} catch (IOException e) {
						exceptions.add(e);
						return FileVisitResult.TERMINATE;
					}
					return FileVisitResult.CONTINUE;
				}
			});
			
			if (!exceptions.isEmpty()) {
				// something went wrong during parsing
				// we rethrow the exception
				throw exceptions.get(0);
			}
		}
		
		this.importNamespacePrefixes(path);
	}
	
	
	private static final Pattern GENERATED_PREFIX_PATTERN = Pattern.compile("^ns\\d+$");
	
	
	/**
	 * Import namespace prefixes. This is kind of a quick hack. TODO: during the
	 * import, the prefixes should be extracted using JAXB and stored in the
	 * NamespacesResource
	 * 
	 * @param rootPath the root path of the extracted CSAR
	 */
	private void importNamespacePrefixes(Path rootPath) {
		Path properties = rootPath.resolve(CSARExporter.PATH_TO_NAMESPACES_PROPERTIES);
		if (Files.exists(properties)) {
			PropertiesConfiguration pconf;
			try {
				pconf = new PropertiesConfiguration(properties.toFile());
			} catch (ConfigurationException e) {
				CSARImporter.logger.debug(e.getMessage(), e);
				return;
			}
			Iterator<String> namespaces = pconf.getKeys();
			while (namespaces.hasNext()) {
				boolean addToStorage = false;
				String namespace = namespaces.next();
				if (NamespacesResource.INSTANCE.getIsPrefixKnownForNamespace(namespace)) {
					String storedPrefix = NamespacesResource.getPrefix(namespace);
					// QUICK HACK to check whether the prefix is a generated one
					// We assume we know the internal generation routine
					Matcher m = CSARImporter.GENERATED_PREFIX_PATTERN.matcher(storedPrefix);
					if (m.matches()) {
						// the stored prefix is a generated one
						// replace it by the one stored in the exported properties
						addToStorage = true;
					}
				} else {
					addToStorage = true;
				}
				if (addToStorage) {
					String prefix = pconf.getString(namespace);
					NamespacesResource.INSTANCE.addNamespace(namespace, prefix);
				}
			}
		}
	}
	
	/**
	 * Imports a self-service meta data description (if available)
	 * 
	 * The first service template in the provided entry definitions is taken
	 * 
	 * @param tmf
	 * 
	 * @param errors
	 */
	private void importSelfServiceMetaData(final TOSCAMetaFile tmf, final Path rootPath, Path entryDefinitions, final List<String> errors) {
		final Path selfServiceDir = rootPath.resolve(Constants.DIRNAME_SELF_SERVICE_METADATA);
		if (!Files.exists(selfServiceDir)) {
			CSARImporter.logger.debug("Self-service Portal directory does not exist in CSAR");
			return;
		}
		if (!Files.exists(entryDefinitions)) {
			CSARImporter.logger.debug("Entry definitions does not exist.");
			return;
		}
		
		Unmarshaller um = JAXBSupport.createUnmarshaller();
		TDefinitions defs;
		try {
			defs = (TDefinitions) um.unmarshal(entryDefinitions.toFile());
		} catch (JAXBException e) {
			errors.add("Could not unmarshal definitions " + entryDefinitions.getFileName() + " " + e.getMessage());
			return;
		} catch (ClassCastException e) {
			errors.add("Definitions " + entryDefinitions.getFileName() + " is not a TDefinitions " + e.getMessage());
			return;
		}
		
		final int cutLength = selfServiceDir.toString().length() + 1;
		Iterator<TExtensibleElements> iterator = defs.getServiceTemplateOrNodeTypeOrNodeTypeImplementation().iterator();
		boolean found = false;
		TExtensibleElements next = null;
		while (iterator.hasNext() && !found) {
			next = iterator.next();
			if (next instanceof TServiceTemplate) {
				found = true;
			}
		}
		
		if (found) {
			TServiceTemplate serviceTemplate = (TServiceTemplate) next;
			String namespace = serviceTemplate.getTargetNamespace();
			if (namespace == null) {
				namespace = defs.getTargetNamespace();
			}
			ServiceTemplateId stId = new ServiceTemplateId(namespace, serviceTemplate.getId(), false);
			final SelfServiceMetaDataId id = new SelfServiceMetaDataId(stId);
			
			// QUICK HACK: We just import all data without any validation
			// Reason: the metadata resource can deal with nearly arbitrary formats of the data, therefore we do not do any checking here
			
			try {
				Files.walkFileTree(selfServiceDir, new SimpleFileVisitor<Path>() {
					
					@Override
					public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
						String name = file.toString().substring(cutLength);
						// check: if name contains "/", this could lead to exceptions
						RepositoryFileReference ref = new RepositoryFileReference(id, name);
						
						if (name.equals("data.xml")) {
							// we have to check whether the data.xml contains
							// (uri:"http://opentosca.org/self-service", local:"application")
							// instead of
							// (uri:"http://www.eclipse.org/winery/model/selfservice", local:"Application"
							// We quickly replace it via String replacement instead of XSLT
							try {
								String oldContent = org.apache.commons.io.FileUtils.readFileToString(file.toFile(), "UTF-8");
								String newContent = oldContent.replace("http://opentosca.org/self-service", "http://www.eclipse.org/winery/model/selfservice");
								newContent = newContent.replace(":application", ":Application");
								if (!oldContent.equals(newContent)) {
									// we replaced something -> write new content to old file
									org.apache.commons.io.FileUtils.writeStringToFile(file.toFile(), newContent, "UTF-8");
								}
							} catch (IOException e) {
								CSARImporter.logger.debug("Could not replace content in data.xml", e);
							}
						}
						CSARImporter.this.importFile(file, ref, tmf, rootPath, errors);
						return FileVisitResult.CONTINUE;
					}
				});
			} catch (IOException e) {
				CSARImporter.logger.debug(e.getMessage(), e);
				errors.add("Self-service Meta Data: " + e.getMessage());
			}
		}
		
	}
	
	/**
	 * Recursively imports the given definitions
	 * 
	 * @param tmf the TOSCAMetaFile object holding the parsed content of a TOSCA
	 *            meta file. If null, no files must be referenced from the given
	 *            definitions
	 * @param overwrite true: existing contents are overwritten
	 * @param asyncWPDParsing
	 * @param definitions the path to the definitions to import
	 * 
	 * @throws IOException
	 */
	public void importDefinitions(TOSCAMetaFile tmf, Path defsPath, final List<String> errors, boolean overwrite, boolean asyncWPDParsing) throws IOException {
		if (defsPath == null) {
			throw new IllegalStateException("path to definitions must not be null");
		}
		if (!Files.exists(defsPath)) {
			errors.add(String.format("Definitions %1$s does not exist", defsPath.getFileName()));
			return;
		}
		
		Unmarshaller um = JAXBSupport.createUnmarshaller();
		TDefinitions defs;
		try {
			defs = (TDefinitions) um.unmarshal(defsPath.toFile());
		} catch (JAXBException e) {
			Throwable cause = e;
			String eMsg = "";
			do {
				String msg = cause.getMessage();
				if (msg != null) {
					eMsg = eMsg + msg + "; ";
				}
				cause = cause.getCause();
			} while (cause != null);
			errors.add("Could not unmarshal definitions " + defsPath.getFileName() + " " + eMsg);
			CSARImporter.logger.debug("Unmarshalling error", e);
			return;
		} catch (ClassCastException e) {
			errors.add("Definitions " + defsPath.getFileName() + " is not a TDefinitions " + e.getMessage());
			return;
		}
		
		List<TImport> imports = defs.getImport();
		this.importImports(defsPath.getParent(), tmf, imports, errors, overwrite, asyncWPDParsing);
		// imports has been modified to contain necessary imports only
		
		// this method adds new imports to defs which may not be imported using "importImports".
		// Therefore, "importTypes" has to be called *after* importImports
		this.importTypes(defs, errors);
		
		String defaultNamespace = defs.getTargetNamespace();
		List<TExtensibleElements> componentInstanceList = defs.getServiceTemplateOrNodeTypeOrNodeTypeImplementation();
		for (final TExtensibleElements ci : componentInstanceList) {
			// Determine namespace
			String namespace = this.getNamespace(ci, defaultNamespace);
			// Ensure that element has the namespace
			this.setNamespace(ci, namespace);
			
			// Determine id
			String id = ModelUtilities.getId(ci);
			
			// Determine WineryId
			Class<? extends TOSCAComponentId> widClass = org.eclipse.winery.repository.Utils.getComponentIdClassForTExtensibleElements(ci.getClass());
			final TOSCAComponentId wid = BackendUtils.getTOSCAcomponentId(widClass, namespace, id, false);
			
			if (Repository.INSTANCE.exists(wid)) {
				if (overwrite) {
					Repository.INSTANCE.forceDelete(wid);
					String msg = String.format("Deleted %1$s %2$s to enable replacement", ci.getClass().getName(), wid.getQName().toString());
					CSARImporter.logger.debug(msg);
				} else {
					String msg = String.format("Skipped %1$s %2$s, because it already exists", ci.getClass().getName(), wid.getQName().toString());
					CSARImporter.logger.debug(msg);
					// this is not displayed in the UI as we currently do not distinguish between pre-existing types and types created during the import.
					continue;
				}
			}
			
			// Create a fresh definitions object without the other data.
			final Definitions newDefs = BackendUtils.createWrapperDefinitions(wid);
			
			// copy over the inputs determined by this.importImports
			newDefs.getImport().addAll(imports);
			
			// add the current TExtensibleElements as the only content to it
			newDefs.getServiceTemplateOrNodeTypeOrNodeTypeImplementation().add(ci);
			
			if (ci instanceof TArtifactTemplate) {
				// convention: Definitions are stored in the "Definitions" directory, therefore going to levels up (Definitions dir -> root dir) resolves to the root dir
				// COS01, line 2663 states that the path has to be resolved from the *root* of the CSAR
				this.adjustArtifactTemplate(defsPath.getParent().getParent(), tmf, (ArtifactTemplateId) wid, (TArtifactTemplate) ci, errors);
			} else if (ci instanceof TNodeType) {
				this.adjustNodeType(defsPath.getParent().getParent(), (TNodeType) ci, (NodeTypeId) wid, tmf, errors);
			} else if (ci instanceof TRelationshipType) {
				this.adjustRelationshipType(defsPath.getParent().getParent(), (TRelationshipType) ci, (RelationshipTypeId) wid, tmf, errors);
			} else if (ci instanceof TServiceTemplate) {
				this.adjustServiceTemplate(defsPath.getParent().getParent(), tmf, (ServiceTemplateId) wid, (TServiceTemplate) ci, errors);
			}
			
			// node types and relationship types are subclasses of TEntityType
			// Therefore, we check the entity type separately here
			if (ci instanceof TEntityType) {
				if (asyncWPDParsing) {
					// Adjusting takes a long time
					// Therefore, we first save the type as is and convert to Winery-Property-Definitions in the background
					CSARImporter.storeDefinitions(wid, newDefs);
					CSARImporter.entityTypeAdjestmentService.submit(new Runnable() {
						
						@Override
						public void run() {
							CSARImporter.adjustEntityType((TEntityType) ci, (EntityTypeId) wid, newDefs, errors);
							CSARImporter.storeDefinitions(wid, newDefs);
						}
					});
				} else {
					CSARImporter.adjustEntityType((TEntityType) ci, (EntityTypeId) wid, newDefs, errors);
					CSARImporter.storeDefinitions(wid, newDefs);
				}
			} else {
				CSARImporter.storeDefinitions(wid, newDefs);
			}
		}
	}
	
	/**
	 * Imports the specified types into the repository. The types are converted
	 * to an import statement
	 * 
	 * @param errors Container for error messages
	 */
	private void importTypes(TDefinitions defs, final List<String> errors) {
		Types typesContainer = defs.getTypes();
		if (typesContainer != null) {
			List<Object> types = typesContainer.getAny();
			for (Object type : types) {
				if (type instanceof Element) {
					Element element = (Element) type;
					
					// generate id part of ImportId out of definitions' id
					// we do not use the name as the name has to be URLencoded again and we have issues with the interplay with org.eclipse.winery.common.ids.definitions.imports.GenericImportId.getId(TImport) then.
					String id = defs.getId();
					// try to  make the id unique by hashing the "content" of the definition
					id = id + "-" + Integer.toHexString(element.hashCode());
					
					// set importId
					TOSCAComponentId importId;
					String ns;
					if (element.getNamespaceURI().equals(XMLConstants.W3C_XML_SCHEMA_NS_URI)) {
						ns = element.getAttribute("targetNamespace");
						importId = new XSDImportId(ns, id, false);
					} else {
						// Quick hack for non-XML-Schema-definitions
						ns = "unknown";
						importId = new GenericImportId(ns, id, false, element.getNamespaceURI());
					}
					
					// Following code is adapted from importOtherImports
					
					TDefinitions wrapperDefs = BackendUtils.createWrapperDefinitions(importId);
					TImport imp = new TImport();
					String fileName = id + ".xsd";
					imp.setLocation(fileName);
					imp.setImportType(XMLConstants.W3C_XML_SCHEMA_NS_URI);
					imp.setNamespace(ns);
					wrapperDefs.getImport().add(imp);
					CSARImporter.storeDefinitions(importId, wrapperDefs);
					
					// put the file itself to the repo
					// ref is required to generate fileRef
					RepositoryFileReference ref = BackendUtils.getRefOfDefinitions(importId);
					RepositoryFileReference fileRef = new RepositoryFileReference(ref.getParent(), fileName);
					// convert element to document
					// QUICK HACK. Alternative: Add new method Repository.INSTANCE.getOutputStream and transform DOM node to OuptputStream
					String content = Util.getXMLAsString(element);
					try {
						Repository.INSTANCE.putContentToFile(fileRef, content, MediaType.APPLICATION_XML_TYPE);
					} catch (IOException e) {
						CSARImporter.logger.debug("Could not put XML Schema definition to file " + fileRef.toString(), e);
						errors.add("Could not put XML Schema definition to file " + fileRef.toString());
					}
					
					// add import to definitions
					
					// adapt path - similar to importOtherImport
					String newLoc = "../" + Utils.getURLforPathInsideRepo(BackendUtils.getPathInsideRepo(fileRef));
					imp.setLocation(newLoc);
					defs.getImport().add(imp);
				} else {
					// This is a known type. Otherwise JAX-B would render it as Element
					errors.add("There is a Type of class " + type.getClass().toString() + " which is unknown to Winery. The type element is imported as is");
				}
			}
		}
	}
	
	/**
	 * All EntityTypes may contain properties definition. In case a winery
	 * properties definition is found, the TOSCA conforming properties
	 * definition is removed
	 * 
	 * @param ci the entity type
	 * @param wid the Winery id of the entitytype
	 * @param newDefs the definitions, the entiy type is contained in. The
	 *            imports might be adjusted here
	 * @param errors
	 */
	private static void adjustEntityType(TEntityType ci, EntityTypeId wid, Definitions newDefs, final List<String> errors) {
		PropertiesDefinition propertiesDefinition = ci.getPropertiesDefinition();
		if (propertiesDefinition != null) {
			WinerysPropertiesDefinition winerysPropertiesDefinition = ModelUtilities.getWinerysPropertiesDefinition(ci);
			boolean deriveWPD;
			if (winerysPropertiesDefinition == null) {
				deriveWPD = true;
			} else {
				if (winerysPropertiesDefinition.getIsDerivedFromXSD() == null) {
					// if the winery's properties are defined by Winery itself,
					// remove the TOSCA conforming properties definition as a Winery properties definition exists (and which takes precedence)
					ci.setPropertiesDefinition(null);
					
					// no derivation from properties required as the properties are generated by Winery
					deriveWPD = false;
					
					// we have to remove the import, too
					// Determine the location
					String elementName = winerysPropertiesDefinition.getElementName();
					String loc = BackendUtils.getImportLocationForWinerysPropertiesDefinitionXSD(wid, null, elementName);
					// remove the import matching that location
					List<TImport> imports = newDefs.getImport();
					boolean found = false;
					if (imports != null) {
						Iterator<TImport> iterator = imports.iterator();
						TImport imp;
						while (iterator.hasNext()) {
							imp = iterator.next();
							// TODO: add check for QNames.QNAME_WINERYS_PROPERTIES_DEFINITION_ATTRIBUTE instead of import location. The current routine, however, works, too.
							if (imp.getLocation().equals(loc)) {
								found = true;
								break;
							}
						}
						if (found) {
							// imp with Winery's k/v location found
							iterator.remove();
							// the XSD has been imported in importOtherImport
							// it was too difficult to do the location check there, therefore we just remove the XSD from the repository here
							XSDImportId importId = new XSDImportId(winerysPropertiesDefinition.getNamespace(), elementName, false);
							try {
								Repository.INSTANCE.forceDelete(importId);
							} catch (IOException e) {
								CSARImporter.logger.debug("Could not delete Winery's generated XSD definition", e);
								errors.add("Could not delete Winery's generated XSD definition");
							}
						} else {
							// K/V properties definition was incomplete
						}
					}
				} else {
					// winery's properties are derived from an XSD
					// The export does NOT add an imports statement: only the wpd exists
					// We remove that as
					ModelUtilities.removeWinerysPropertiesDefinition(ci);
					// derive the WPDs again from the properties definition
					deriveWPD = true;
				}
			}
			if (deriveWPD) {
				BackendUtils.deriveWPD(ci, errors);
			}
		}
	}
	
	/**
	 * In case plans are provided, the plans are imported into Winery's storage
	 * 
	 * @param rootPath the root path of the extracted csar
	 * @param tmf the TOSCAMetaFile object used to determine the mime type of
	 *            the plan
	 * @param wid Winery's internal id of the service template
	 * @param st the the service template to be imported {@inheritDoc}
	 * 
	 * @throws InvalidCSARException
	 */
	private void adjustServiceTemplate(Path rootPath, TOSCAMetaFile tmf, ServiceTemplateId wid, TServiceTemplate st, final List<String> errors) {
		TPlans plans = st.getPlans();
		if (plans != null) {
			for (TPlan plan : plans.getPlan()) {
				PlanModelReference refContainer = plan.getPlanModelReference();
				if (refContainer != null) {
					String ref = refContainer.getReference();
					if (ref != null) {
						// URLs are stored encoded -> undo the encoding
						ref = Util.URLdecode(ref);
						URI refURI;
						try {
							refURI = new URI(ref);
						} catch (URISyntaxException e) {
							errors.add(String.format("Invalid URI %1$s", ref));
							continue;
						}
						if (refURI.isAbsolute()) {
							// Points to somewhere external
							// This is a linked plan
							// We have to do nothing
							continue;
						}
						Path path = rootPath.resolve(ref);
						if (!Files.exists(path)) {
							// possibly, the reference is relative to the Definitions subfolder
							// COS01 does not make any explicit statement how to resolve the reference here
							path = rootPath.resolve("Definitions").resolve(ref);
							if (!Files.exists(path)) {
								errors.add(String.format("Plan reference %1$s not found", ref));
								// we quickly remove the reference to reflect the not-found in the data
								refContainer.setReference(null);
								continue;
							}
						}
						PlansId plansId = new PlansId(wid);
						PlanId pid = new PlanId(plansId, new XMLId(plan.getId(), false));
						if (Files.isDirectory(path)) {
							errors.add(String.format("Reference %1$s is a directory and Winery currently does not support importing directories", ref));
							continue;
						}
						RepositoryFileReference fref = new RepositoryFileReference(pid, path.getFileName().toString());
						this.importFile(path, fref, tmf, rootPath, errors);
						
						// file is imported
						// Adjust the reference
						refContainer.setReference("../" + Utils.getURLforPathInsideRepo(BackendUtils.getPathInsideRepo(fref)));
					}
				}
			}
		}
	}
	
	/**
	 * Adds a color to the given relationship type
	 */
	private void adjustRelationshipType(Path rootPath, TRelationshipType ci, RelationshipTypeId wid, TOSCAMetaFile tmf, final List<String> errors) {
		VisualAppearanceId visId = new VisualAppearanceId(wid);
		this.importIcons(rootPath, visId, tmf, errors);
	}
	
	private void adjustNodeType(Path rootPath, TNodeType ci, NodeTypeId wid, TOSCAMetaFile tmf, final List<String> errors) {
		VisualAppearanceId visId = new VisualAppearanceId(wid);
		this.importIcons(rootPath, visId, tmf, errors);
	}
	
	private void importIcons(Path rootPath, VisualAppearanceId visId, TOSCAMetaFile tmf, final List<String> errors) {
		String pathInsideRepo = BackendUtils.getPathInsideRepo(visId);
		Path visPath = rootPath.resolve(pathInsideRepo);
		this.importIcon(visId, visPath, Filename.FILENAME_BIG_ICON, tmf, rootPath, errors);
	}
	
	private void importIcon(VisualAppearanceId visId, Path visPath, String fileName, TOSCAMetaFile tmf, Path rootPath, final List<String> errors) {
		Path file = visPath.resolve(fileName);
		if (Files.exists(file)) {
			RepositoryFileReference ref = new RepositoryFileReference(visId, fileName);
			this.importFile(file, ref, tmf, rootPath, errors);
		}
	}
	
	/**
	 * Adjusts the given artifact template to conform with the repository format
	 * 
	 * We import the files given at the artifact references
	 * 
	 * @throws InvalidCSARException
	 * @throws IOException
	 */
	private void adjustArtifactTemplate(Path rootPath, TOSCAMetaFile tmf, ArtifactTemplateId atid, TArtifactTemplate ci, final List<String> errors) throws IOException {
		ArtifactReferences refs = ci.getArtifactReferences();
		if (refs == null) {
			// no references stored - break
			return;
		}
		List<TArtifactReference> refList = refs.getArtifactReference();
		Iterator<TArtifactReference> iterator = refList.iterator();
		while (iterator.hasNext()) {
			TArtifactReference ref = iterator.next();
			String reference = ref.getReference();
			// URLs are stored encoded -> undo the encoding
			reference = Util.URLdecode(reference);
			
			URI refURI;
			try {
				refURI = new URI(reference);
			} catch (URISyntaxException e) {
				errors.add(String.format("Invalid URI %1$s", ref));
				continue;
			}
			if (refURI.isAbsolute()) {
				// Points to somewhere external
				// We have to do nothing
				continue;
			}
			
			// we remove the current element as it will be handled during the export
			iterator.remove();
			
			Path path = rootPath.resolve(reference);
			if (!Files.exists(path)) {
				errors.add(String.format("Reference %1$s not found", reference));
				return;
			}
			Set<Path> allFiles;
			if (Files.isRegularFile(path)) {
				allFiles = new HashSet<Path>();
				allFiles.add(path);
			} else {
				assert (Files.isDirectory(path));
				Path localRoot = rootPath.resolve(path);
				List<Object> includeOrExclude = ref.getIncludeOrExclude();
				
				if (includeOrExclude.get(0) instanceof TArtifactReference.Exclude) {
					// Implicit semantics of an exclude listed first:
					// include all files and then exclude the files matched by the pattern
					allFiles = this.getAllFiles(localRoot);
				} else {
					// semantics if include lited as first:
					// same as listed at other places
					allFiles = new HashSet<>();
				}
				
				for (Object object : includeOrExclude) {
					if (object instanceof TArtifactReference.Include) {
						this.handleInclude((TArtifactReference.Include) object, localRoot, allFiles);
					} else {
						assert (object instanceof TArtifactReference.Exclude);
						this.handleExclude((TArtifactReference.Exclude) object, localRoot, allFiles);
					}
				}
			}
			this.importAllFiles(allFiles, atid, tmf, rootPath, errors);
		}
		
		if (refList.isEmpty()) {
			// everything is imported and is a file stored locally
			// we don't need the references stored locally: they are generated on the fly when exporting
			ci.setArtifactReferences(null);
		}
	}
	
	/**
	 * Imports a file from the filesystem to the repository
	 * 
	 * @param p the file to read from
	 * @param fref the "file" to put the content to
	 * @param tmf the TOSCAMetaFile object used to determine the mimetype. Must
	 *            not be null.
	 * @param rootPath used to relativize p to determine the mime type
	 * @throws InvalidCSARException
	 */
	private void importFile(Path p, RepositoryFileReference fref, TOSCAMetaFile tmf, Path rootPath, final List<String> errors) {
		if (tmf == null) {
			throw new IllegalStateException("tmf must not be null");
		}
		try (InputStream is = Files.newInputStream(p);
				BufferedInputStream bis = new BufferedInputStream(is)) {
			String mediaType = tmf.getMimeType(p.relativize(rootPath).toString());
			if (mediaType == null) {
				// Manually find out mime type
				try {
					mediaType = Utils.getMimeType(bis, p.getFileName().toString());
				} catch (IOException e) {
					errors.add(String.format("No MimeType given for %1$s (%2$s)", p.getFileName(), e.getMessage()));
					return;
				}
				if (mediaType == null) {
					errors.add(String.format("No MimeType given for %1$s", p.getFileName()));
					return;
				}
			}
			try {
				Repository.INSTANCE.putContentToFile(fref, bis, MediaType.valueOf(mediaType));
			} catch (IllegalArgumentException | IOException e) {
				throw new IllegalStateException(e);
			}
		} catch (IOException e1) {
			throw new IllegalStateException("Could not work on generated temporary files", e1);
		}
	}
	
	private void importAllFiles(Collection<Path> allFiles, ArtifactTemplateId atid, TOSCAMetaFile tmf, Path rootPath, final List<String> errors) {
		// import all files to repository
		ArtifactTemplateDirectoryId fileDir = new ArtifactTemplateDirectoryId(atid);
		for (Path p : allFiles) {
			if (!Files.exists(p)) {
				errors.add(String.format("File %1$s does not exist", p.toString()));
				return;
			}
			RepositoryFileReference fref = new RepositoryFileReference(fileDir, p.getFileName().toString());
			this.importFile(p, fref, tmf, rootPath, errors);
		}
		
	}
	
	/**
	 * Modifies given allFiles object to exclude all files given by the excl
	 * pattern
	 * 
	 * Semantics: Remove all files from the set, which match the given pattern
	 */
	private void handleExclude(Exclude excl, Path localRoot, Set<Path> allFiles) {
		PathMatcher pathMatcher = localRoot.getFileSystem().getPathMatcher("glob:" + excl.getPattern());
		Iterator<Path> it = allFiles.iterator();
		while (it.hasNext()) {
			Path curPath = it.next();
			if (pathMatcher.matches(curPath)) {
				it.remove();
			}
		}
	}
	
	/**
	 * Modifies given allFiles object to include all files given by the incl
	 * pattern
	 * 
	 * Semantics: Add all files from localRoot to allFiles matching the pattern
	 */
	private void handleInclude(final Include incl, final Path localRoot, final Set<Path> allFiles) {
		final PathMatcher pathMatcher = localRoot.getFileSystem().getPathMatcher("glob:" + incl.getPattern());
		try {
			Files.walkFileTree(localRoot, new SimpleFileVisitor<Path>() {
				
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					Path relFile = localRoot.relativize(file);
					if (pathMatcher.matches(relFile)) {
						allFiles.add(file);
					}
					return CONTINUE;
				}
				
				@Override
				public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
					if (pathMatcher.matches(dir)) {
						Set<Path> filesToAdd = CSARImporter.this.getAllFiles(dir);
						allFiles.addAll(filesToAdd);
						return SKIP_SUBTREE;
					} else {
						return CONTINUE;
					}
				}
			});
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}
	
	/**
	 * Lists all files contained in the given path
	 */
	private Set<Path> getAllFiles(Path startPath) {
		final Set<Path> res = new HashSet<>();
		try {
			Files.walkFileTree(startPath, new SimpleFileVisitor<Path>() {
				
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					res.add(file);
					return CONTINUE;
				}
			});
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
		return res;
	}
	
	/**
	 * Sets the namespace on the CI if CI offers the method "setTargetNamespace"
	 * 
	 * @param ci the component instance to set the namespace
	 * @param namespace the namespace to set
	 */
	private void setNamespace(TExtensibleElements ci, String namespace) {
		Method method;
		try {
			method = ci.getClass().getMethod("setTargetNamespace", String.class);
			method.invoke(ci, namespace);
		} catch (NoSuchMethodException ne) {
			// this is OK, because we do not check, whether the method really exists
			// Special case for TArtifactTemplate not offering setTargetNamespace
			// just ignore it
		} catch (Exception e) {
			throw new IllegalStateException("Could not set target namespace", e);
		}
	}
	
	/**
	 * @param ci the component instance to get the namespace from
	 * @param defaultNamespace the namespace to use if the TExtensibleElements
	 *            has no targetNamespace
	 */
	private String getNamespace(TExtensibleElements ci, String defaultNamespace) {
		Method method;
		Object res;
		try {
			method = ci.getClass().getMethod("getTargetNamespace");
			res = method.invoke(ci);
		} catch (Exception e) {
			// we are at TArtifactTemplate, which does not offer getTargetNamespace
			res = null;
		}
		String ns = (String) res;
		if (ns == null) {
			ns = defaultNamespace;
		}
		return ns;
	}
	
	/**
	 * @param basePath the base path where to resolve files from. This is the
	 *            directory of the Definitions
	 * @param imports the list of imports to import. SIDE EFFECT: this list is
	 *            modified. After this method has run, the list contains the
	 *            imports to be put into the wrapper element
	 */
	private void importImports(Path basePath, TOSCAMetaFile tmf, List<TImport> imports, final List<String> errors, boolean overwrite, final boolean asyncWPDParsing) throws IOException {
		for (Iterator<TImport> iterator = imports.iterator(); iterator.hasNext();) {
			TImport imp = iterator.next();
			String importType = imp.getImportType();
			String namespace = imp.getNamespace();
			String loc = imp.getLocation();
			
			if (namespace == null) {
				errors.add("not namespace-qualified imports are not supported.");
				continue;
			}
			
			if (loc == null) {
				errors.add("Empty location imports are not supported.");
			} else {
				if (importType.equals(Namespaces.TOSCA_NAMESPACE)) {
					if (!Util.isRelativeURI(loc)) {
						errors.add("Absolute URIs for definitions import not supported.");
						continue;
					}
					
					// URIs are encoded
					loc = Util.URLdecode(loc);
					
					Path defsPath = basePath.resolve(loc);
					// fallback for older CSARs, where the location is given from the root
					if (!Files.exists(defsPath)) {
						defsPath = basePath.getParent().resolve(loc);
						// the real existence check is done in importDefinitions
					}
					this.importDefinitions(tmf, defsPath, errors, overwrite, asyncWPDParsing);
					// imports of definitions don't have to be kept as these are managed by Winery
					iterator.remove();
				} else {
					this.importOtherImport(basePath, imp, errors, importType, overwrite);
				}
			}
		}
	}
	
	/**
	 * SIDE EFFECT: modifies the location of imp to point to the correct
	 * relative location (when read from the exported CSAR)
	 * 
	 * @param rootPath the absolute path where to resolve files from
	 */
	private void importOtherImport(Path rootPath, TImport imp, final List<String> errors, String type, boolean overwrite) {
		assert (!type.equals(Namespaces.TOSCA_NAMESPACE));
		String loc = imp.getLocation();
		
		if (!Util.isRelativeURI(loc)) {
			// This is just an information message
			errors.add("Absolute URIs are not resolved by Winery (" + loc + ")");
			return;
		}
		
		// location URLs are encoded: http://www.w3.org/TR/2001/WD-charmod-20010126/#sec-URIs, RFC http://www.ietf.org/rfc/rfc2396.txt
		loc = Util.URLdecode(loc);
		Path path;
		try {
			path = rootPath.resolve(loc);
		} catch (Exception e) {
			// java.nio.file.InvalidPathException could be thrown which is a RuntimeException
			errors.add(e.getMessage());
			return;
		}
		if (!Files.exists(path)) {
			// fallback for older CSARs, where the location is given from the root
			path = rootPath.getParent().resolve(loc);
			if (!Files.exists(path)) {
				errors.add(String.format("File %1$s does not exist", loc));
				return;
			}
		}
		String namespace = imp.getNamespace();
		String fileName = path.getFileName().toString();
		String id = fileName;
		id = FilenameUtils.removeExtension(id);
		// Convention: id of import is filename without extension
		
		GenericImportId rid;
		if (type.equals(XMLConstants.W3C_XML_SCHEMA_NS_URI)) {
			rid = new XSDImportId(namespace, id, false);
		} else {
			rid = new GenericImportId(namespace, id, false, type);
		}
		
		boolean importDataExistsInRepo = Repository.INSTANCE.exists(rid);
		
		if (!importDataExistsInRepo) {
			// We have to
			//  a) create a .definitions file
			//  b) put the file itself in the repo
			
			// Create the definitions file
			TDefinitions defs = BackendUtils.createWrapperDefinitions(rid);
			defs.getImport().add(imp);
			// QUICK HACK: We change the imp object's location here and below again
			// This is "OK" as "storeDefinitions" serializes the current state and not the future state of the imp object
			// change the location to point to the file in the folder of the .definitions file
			imp.setLocation(fileName);
			
			// put the definitions file to the repository
			CSARImporter.storeDefinitions(rid, defs);
		}
		
		// put the file itself to the repo
		// ref is required to generate fileRef
		RepositoryFileReference ref = BackendUtils.getRefOfDefinitions(rid);
		RepositoryFileReference fileRef = new RepositoryFileReference(ref.getParent(), fileName);
		
		// location is relative to Definitions/
		// even if the import already exists, we have to adapt the path
		// URIs are encoded
		String newLoc = "../" + Utils.getURLforPathInsideRepo(BackendUtils.getPathInsideRepo(fileRef));
		imp.setLocation(newLoc);
		
		if (!importDataExistsInRepo || overwrite) {
			// finally write the file to the storage
			try (InputStream is = Files.newInputStream(path);
					BufferedInputStream bis = new BufferedInputStream(is)) {
				MediaType mediaType;
				if (type.equals(XMLConstants.W3C_XML_SCHEMA_NS_URI)) {
					mediaType = MediaType.valueOf(MimeTypes.MIMETYPE_XSD);
				} else {
					String mimeType = Utils.getMimeType(bis, path.getFileName().toString());
					mediaType = MediaType.valueOf(mimeType);
				}
				Repository.INSTANCE.putContentToFile(fileRef, bis, mediaType);
			} catch (IllegalArgumentException | IOException e) {
				throw new IllegalStateException(e);
			}
			
			// we have to update the cache in case of a new XSD to speedup usage of winery
			if (rid instanceof XSDImportId) {
				// We do the initialization asynchronously
				// We do not check whether the XSD has already been checked
				// We cannot just checck whether an XSD already has been handled since the XSD could change over time
				// Synchronization at org.eclipse.winery.repository.resources.imports.xsdimports.XSDImportResource.getAllDefinedLocalNames(short) also isn't feasible as the backend doesn't support locks
				CSARImporter.xsdParsingService.submit(new Runnable() {
					
					@Override
					public void run() {
						CSARImporter.logger.debug("Updating XSD import cache data");
						// We call the queries without storing the result:
						// We use the SIDEEFFECT that a cache is created
						Utils.getAllXSDElementDefinitionsForTypeAheadSelection();
						Utils.getAllXSDTypeDefinitionsForTypeAheadSelection();
						CSARImporter.logger.debug("Updated XSD import cache data");
					}
				});
			}
		}
	}
	
	private static void storeDefinitions(TOSCAComponentId id, TDefinitions defs) {
		RepositoryFileReference ref = BackendUtils.getRefOfDefinitions(id);
		String s = Utils.getXMLAsString(defs, true);
		try {
			Repository.INSTANCE.putContentToFile(ref, s, MediaType.valueOf(MimeTypes.MIMETYPE_TOSCA_DEFINITIONS));
		} catch (IllegalArgumentException | IOException e) {
			throw new IllegalStateException(e);
		}
	}
}
