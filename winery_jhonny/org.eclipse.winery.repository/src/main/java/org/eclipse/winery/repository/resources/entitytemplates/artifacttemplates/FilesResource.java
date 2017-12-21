/*******************************************************************************
 * Copyright (c) 2012-2013,2015 University of Stuttgart.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and the Apache License 2.0 which both accompany this distribution,
 * and are available at http://www.eclipse.org/legal/epl-v10.html
 * and http://www.apache.org/licenses/LICENSE-2.0
 *
 * Contributors:
 *     Oliver Kopp - initial API and implementation
 *******************************************************************************/
package org.eclipse.winery.repository.resources.entitytemplates.artifacttemplates;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.winery.common.RepositoryFileReference;
import org.eclipse.winery.common.Util;
import org.eclipse.winery.repository.Constants;
import org.eclipse.winery.repository.Utils;
import org.eclipse.winery.repository.backend.BackendUtils;
import org.eclipse.winery.repository.backend.Repository;
import org.eclipse.winery.repository.datatypes.FileMeta;
import org.eclipse.winery.repository.datatypes.ids.elements.ArtifactTemplateDirectoryId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jersey.api.view.Viewable;
import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataBodyPart;
import com.sun.jersey.multipart.FormDataParam;

public class FilesResource {
	
	private static final Logger logger = LoggerFactory.getLogger(FilesResource.class);
	private final ArtifactTemplateDirectoryId fileDir;
	
	
	public FilesResource(ArtifactTemplateDirectoryId fileDir) {
		this.fileDir = fileDir;
	}
	
	private String getData4jqueryFileUpload(List<FileMeta> metas) {
		String data4jqueryFileUpload = Utils.Object2JSON(metas);
		data4jqueryFileUpload = "{\"files\":" + data4jqueryFileUpload + "}";
		return data4jqueryFileUpload;
	}
	
	/**
	 * Handles the upload of a <em>single</em> file. Adds the given file to the
	 * current artifact template.
	 * 
	 * If the file already exists, is it <em>overridden</em>
	 * 
	 * @return JSON with data required by JQuery-File-Upload (see
	 *         https://github.com/blueimp/jQuery-File-Upload/wiki/Setup)
	 */
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public Response onPost(@FormDataParam("files[]") InputStream uploadedInputStream, @FormDataParam("files[]") FormDataContentDisposition fileDetail, @FormDataParam("files[]") FormDataBodyPart body, @Context UriInfo uriInfo) {
		// existence check not required as instantiation of the resource ensures that the object only exists if the resource exists
		FilesResource.logger.debug("Beginning with file upload");
		
		String fileName = fileDetail.getFileName();
		if (StringUtils.isEmpty(fileName)) {
			return Response.status(Status.BAD_REQUEST).build();
		}
		RepositoryFileReference ref = this.fileName2fileRef(fileName, false);
		
		// TODO: instead of fixing the media type, we could overwrite the browser's mediatype by using some user configuration
		BufferedInputStream bis = new BufferedInputStream(uploadedInputStream);
		MediaType mediaType = Utils.getFixedMimeType(bis, fileName, body.getMediaType());
		
		Response response = BackendUtils.putContentToFile(ref, bis, mediaType);
		if (response.getStatus() == Status.INTERNAL_SERVER_ERROR.getStatusCode()) {
			return response;
		}
		
		// create FileMeta object
		String URL = Utils.getAbsoluteURL(this.fileDir) + Util.URLencode(fileName);
		String thumbnailURL = uriInfo.getBaseUriBuilder().path(Constants.PATH_MIMETYPEIMAGES).path(FilenameUtils.getExtension(fileName) + Constants.SUFFIX_MIMETYPEIMAGES).build().toString();
		long size;
		try {
			size = Repository.INSTANCE.getSize(ref);
		} catch (IOException e) {
			FilesResource.logger.error(e.getMessage(), e);
			return Response.serverError().entity(e.getMessage()).build();
		}
		FileMeta fileMeta = new FileMeta(fileName, size, URL, thumbnailURL);
		
		List<FileMeta> metas = new ArrayList<FileMeta>();
		metas.add(fileMeta);
		return Response.created(Utils.createURI(URL)).entity(this.getData4jqueryFileUpload(metas)).build();
	}
	
	/**
	 * Returns a list of file meta object
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public String getJSON() {
		return this.getData4jqueryFileUpload(this.getAllFileMetas());
	}
	
	private List<FileMeta> getAllFileMetas() {
		List<FileMeta> res = new ArrayList<FileMeta>();
		SortedSet<RepositoryFileReference> fileRefs = Repository.INSTANCE.getContainedFiles(this.fileDir);
		for (RepositoryFileReference ref : fileRefs) {
			res.add(new FileMeta(ref));
		}
		return res;
	}
	
	private RepositoryFileReference fileName2fileRef(String fileName, boolean encoded) {
		if (encoded) {
			fileName = Util.URLdecode(fileName);
		}
		RepositoryFileReference ref = new RepositoryFileReference(this.fileDir, fileName);
		return ref;
	}
	
	@GET
	@Produces(MediaType.TEXT_HTML)
	public Viewable getHTML() {
		return new Viewable("/jsp/entitytemplates/artifacttemplates/files.jsp");
	}
	
	@GET
	@Path("/{fileName}")
	public Response getFile(@PathParam("fileName") String fileName, @HeaderParam("If-Modified-Since") String modified) {
		RepositoryFileReference ref = this.fileName2fileRef(fileName, true);
		return BackendUtils.returnRepoPath(ref, modified);
	}
	
	@DELETE
	@Path("/{fileName}")
	public Response deleteFile(@PathParam("fileName") String fileName) {
		RepositoryFileReference ref = this.fileName2fileRef(fileName, true);
		return BackendUtils.delete(ref);
	}
	
}
