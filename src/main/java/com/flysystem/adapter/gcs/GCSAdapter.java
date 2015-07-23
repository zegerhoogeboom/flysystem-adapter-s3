/*
 * Copyright (c) 2013-2015 Frank de Jonge
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is furnished
 * to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.flysystem.adapter.gcs;

import com.flysystem.adapter.gcs.exception.GCSConnectionException;
import com.flysystem.core.Config;
import com.flysystem.core.FileMetadata;
import com.flysystem.core.Visibility;
import com.flysystem.core.adapter.AbstractAdapter;
import com.flysystem.core.exception.FileExistsException;
import com.flysystem.core.exception.FileNotFoundException;
import com.flysystem.core.exception.FlysystemGenericException;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.storage.Storage;
import com.google.api.services.storage.StorageScopes;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

/**
 * @author Zeger Hoogeboom
 */
public class GCSAdapter extends AbstractAdapter
{
	private final static Logger logger = Logger.getLogger(GCSAdapter.class.getName());
	private JsonFactory jsonFactory;
	private HttpTransport httpTransport;

	private String applicationName;
	private String bucketName;
	private String serviceAccountEmail;
	private File p12Key;
	private boolean AUTH_LOCAL_WEBSERVER = true;

	private Storage client;

	private GCSAdapter(){}

	public static class Builder {
		GCSAdapter adapter;

		public Builder()
		{
			adapter = new GCSAdapter();
		}

		public Builder setJsonFactory(JsonFactory factory) {
			adapter.jsonFactory = factory;
			return this;
		}
		public Builder setHttpTransport(HttpTransport httpTransport) {
			adapter.httpTransport = httpTransport;
			return this;
		}
		public Builder setApplicationName(String applicationName) {
			adapter.applicationName = applicationName;
			return this;
		}
		public Builder setBucket(String bucket) {
			adapter.bucketName = bucket;
			return this;
		}
		public Builder setServiceAccountEmail(String serviceAccountEmail) {
			adapter.serviceAccountEmail = serviceAccountEmail;
			return this;
		}

		/**
		 * The expected path of your .p12 key is ${rootOfProject}/keyName.
	     * ${rootOfProject} is retrieved with the variable System.getProperty("user.dir").
		 * In case you want your .p12 key to reside elsewhere, please see the method {@code setP12Key(File key)}
		 * @param keyName The name of your key. e.g. "mykey.p12".
		 * @return GCSAdapter.Builder
		 */
		public Builder setP12Key(String keyName)
		{
			adapter.p12Key = new File(String.format("%s/%s",System.getProperty("user.dir"), keyName));
			return this;
		}

		public Builder setP12Key(File key)
		{
			adapter.p12Key = key;
			return this;
		}

		private void withDefaults()
		{
			try {
				if (adapter.httpTransport == null) setHttpTransport(GoogleNetHttpTransport.newTrustedTransport());
			} catch (GeneralSecurityException | IOException e) {
				throw new FlysystemGenericException(e);
			}
			if (adapter.jsonFactory == null) setJsonFactory(JacksonFactory.getDefaultInstance());
		}

		public GCSAdapter build()
		{
			withDefaults();
			if (adapter.bucketName == null) throw new GCSConnectionException("Bucket name has to be provided.");
			if (adapter.serviceAccountEmail == null) throw new GCSConnectionException("Service account email has to be provided.");
			if (adapter.p12Key == null) throw new GCSConnectionException("A .p12 key has to be provided.");
			if (adapter.applicationName == null) logger.warning("You didn't set your Application name. GCS will log warning messages because of this! Suggested format is \"MyCompany-ProductName/1.0\".");
			adapter.initialize();
			return adapter;
		}
	}

	private void initialize()
	{
		try {
			client = new Storage.Builder(httpTransport, jsonFactory, authorize()).setApplicationName(applicationName).build();
		} catch (IOException e) {
			throw new FlysystemGenericException(e);
		}
	}

	private Credential authorize() throws IOException
	{
		try {
			return new GoogleCredential.Builder().setTransport(httpTransport)
					.setJsonFactory(jsonFactory)
					.setServiceAccountId(serviceAccountEmail)
					.setServiceAccountScopes(Collections.singleton(StorageScopes.DEVSTORAGE_FULL_CONTROL))
					.setServiceAccountPrivateKeyFromP12File(p12Key)
					.build();
		} catch (GeneralSecurityException e) {
			throw new FlysystemGenericException(e);
		}
	}

	public boolean has(String path)
	{
		return false;
	}

	public String read(String path) throws FileNotFoundException
	{
		return null;
	}

	public List<FileMetadata> listContents(String directory, boolean recursive)
	{
		return null;
	}

	public FileMetadata getMetadata(String path)
	{
		return null;
	}

	public Long getSize(String path)
	{
		return null;
	}

	public String getMimetype(String path)
	{
		return null;
	}

	public Long getTimestamp(String path)
	{
		return null;
	}

	public Visibility getVisibility(String path)
	{
		return null;
	}

	public boolean write(String path, String contents, Config config)
	{
		return false;
	}

	public boolean write(String path, String contents)
	{
		return false;
	}

	public boolean update(String path, String contents)
	{
		return false;
	}

	public boolean update(String path, String contents, Config config)
	{
		return false;
	}

	public boolean rename(String from, String to) throws FileExistsException, FileNotFoundException
	{
		return false;
	}

	public boolean copy(String path, String newpath)
	{
		return false;
	}

	public boolean delete(String path)
	{
		return false;
	}

	public boolean deleteDir(String dirname)
	{
		return false;
	}

	public boolean createDir(String dirname, Config config)
	{
		return false;
	}

	public boolean createDir(String dirname)
	{
		return false;
	}

	public boolean setVisibility(String path, Visibility visibility)
	{
		return false;
	}
}
