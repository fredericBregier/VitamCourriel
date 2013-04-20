/**
 * Copyright (c) 2012, The National Archives <pronom@nationalarchives.gsi.gov.uk> All rights
 * reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted
 * provided that the following conditions are met:
 * 
 * * Redistributions of source code must retain the above copyright notice, this list of conditions
 * and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other materials provided with
 * the distribution.
 * 
 * * Neither the name of the The National Archives nor the names of its contributors may be used to
 * endorse or promote products derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY
 * WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package uk.gov.nationalarchives.droid.command;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import fr.gouv.culture.vitam.droid.DroidFileFormat;
import fr.gouv.culture.vitam.utils.StaticValues;

import uk.gov.nationalarchives.droid.command.action.CommandExecutionException;
import uk.gov.nationalarchives.droid.command.archive.Bzip2ArchiveContentIdentifier;
import uk.gov.nationalarchives.droid.command.archive.GZipArchiveContentIdentifier;
import uk.gov.nationalarchives.droid.command.archive.TarArchiveContentIdentifier;
import uk.gov.nationalarchives.droid.command.archive.ZipArchiveContentIdentifier;
import uk.gov.nationalarchives.droid.command.container.Ole2ContainerContentIdentifier;
import uk.gov.nationalarchives.droid.command.container.ZipContainerContentIdentifier;
import uk.gov.nationalarchives.droid.container.ContainerFileIdentificationRequestFactory;
import uk.gov.nationalarchives.droid.container.ContainerSignatureDefinitions;
import uk.gov.nationalarchives.droid.container.TriggerPuid;
import uk.gov.nationalarchives.droid.container.ole2.Ole2IdentifierEngine;
import uk.gov.nationalarchives.droid.container.zip.ZipIdentifierEngine;
import uk.gov.nationalarchives.droid.core.BinarySignatureIdentifier;
import uk.gov.nationalarchives.droid.core.VitamBinarySignatureIdentifier;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationRequest;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationResult;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationResultCollection;
import uk.gov.nationalarchives.droid.core.interfaces.archive.IdentificationRequestFactory;

/**
 * File identification results printer.
 * 
 * NB: This class is called recursively when archive files are opened
 * 
 * Extended for VITAM project
 * @author rbrennan
 * @author "Frederic Bregier"
 */
public class ResultPrinter {

	private static final String R_SLASH = "/";
	private static final String L_BRACKET = "(";
	private static final String R_BRACKET = ")";
	private static final String SPACE = " ";

	private VitamBinarySignatureIdentifier binarySignatureIdentifier;
	private ContainerSignatureDefinitions containerSignatureDefinitions;
	private List<TriggerPuid> triggerPuids;
	private IdentificationRequestFactory requestFactory;
	private String path;
	private String slash;
	private String slash1;
	private String wrongSlash;
	private boolean archives;
	private boolean subFormat;
	private final String OLE2_CONTAINER = "OLE2";
	private final String ZIP_CONTAINER = "ZIP";
	private final String ZIP_ARCHIVE = "x-fmt/263";
	private final String JIP_ARCHIVE = "x-fmt/412";
	private final String TAR_ARCHIVE = "x-fmt/265";
	private final String GZIP_ARCHIVE = "x-fmt/266";
	private final String BZIP2_ARCHIVE = "x-fmt/268";

	/**
	 * Store signature files.
	 * 
	 * @param binarySignatureIdentifier
	 *            binary signature identifier
	 * @param containerSignatureDefinitions
	 *            container signatures
	 * @param path
	 *            current file/container path
	 * @param slash
	 *            local path element delimiter
	 * @param slash1
	 *            local first container prefix delimiter
	 * @param archives
	 *            Should archives be examined?
	 */
	public ResultPrinter(final BinarySignatureIdentifier binarySignatureIdentifier,
			final ContainerSignatureDefinitions containerSignatureDefinitions,
			final String path, final String slash, final String slash1, boolean archives) {
		this(binarySignatureIdentifier, containerSignatureDefinitions,
				path, slash, slash1, archives, false);
	}

	/**
	 * Store signature files.
	 * 
	 * @param binarySignatureIdentifier
	 *            binary signature identifier
	 * @param containerSignatureDefinitions
	 *            container signatures
	 * @param path
	 *            current file/container path
	 * @param slash
	 *            local path element delimiter
	 * @param slash1
	 *            local first container prefix delimiter
	 * @param archives
	 *            Should archives be examined?
	 * @param subFormat
	 *            Should we print subFormats ?
	 */
	public ResultPrinter(final BinarySignatureIdentifier binarySignatureIdentifier,
			final ContainerSignatureDefinitions containerSignatureDefinitions,
			final String path, final String slash, final String slash1, boolean archives,
			boolean subFormat) {

		this.binarySignatureIdentifier = (VitamBinarySignatureIdentifier) binarySignatureIdentifier;
		this.containerSignatureDefinitions = containerSignatureDefinitions;
		this.path = path;
		this.slash = slash;
		this.slash1 = slash1;
		this.wrongSlash = this.slash.equals(R_SLASH) ? "\\" : R_SLASH;
		this.archives = archives;
		if (containerSignatureDefinitions != null) {
			triggerPuids = containerSignatureDefinitions.getTiggerPuids();
		}
		this.subFormat = subFormat;
	}

	/**
	 * Output identification for this file.
	 * 
	 * @param results
	 *            identification Results
	 * @param request
	 *            identification Request
	 * 
	 * @throws CommandExecutionException
	 *             if unexpected container type encountered
	 */
	public void print(final IdentificationResultCollection results,
			final IdentificationRequest request) throws CommandExecutionException {

		final String fileName = (path + request.getFileName()).replace(wrongSlash, slash);
		final IdentificationResultCollection containerResults =
				getContainerResults(results, request, fileName);

		IdentificationResultCollection finalResults = new IdentificationResultCollection(request);
		List<String> hash = null;
		if (subFormat) {
			hash = new ArrayList<String>();
			for (IdentificationResult identificationResult : containerResults.getResults()) {
				if (!hash.contains(identificationResult.getPuid())) {
					hash.add(identificationResult.getPuid());
				}
			}
			for (IdentificationResult identificationResult : results.getResults()) {
				if (!hash.contains(identificationResult.getPuid())) {
					hash.add(identificationResult.getPuid());
				}
			}
		} else {
			hash = new ArrayList<String>();
			for (IdentificationResult identificationResult : results.getResults()) {
				if (!hash.contains(identificationResult.getPuid())) {
					hash.add(identificationResult.getPuid());
				}
			}
			for (IdentificationResult identificationResult : containerResults.getResults()) {
				if (!hash.contains(identificationResult.getPuid())) {
					hash.add(identificationResult.getPuid());
				}
			}
		}
		boolean container = false;
		if (containerResults.getResults().size() > 0) {
			container = true;
			finalResults = containerResults;
		} else if (results.getResults().size() > 0) {
			finalResults = results;
		}
		if (finalResults.getResults().size() > 0) {
			binarySignatureIdentifier.removeLowerPriorityHits(finalResults);
		}
		if (finalResults.getResults().size() > 0) {
			// test if not x-fmt for seda compliance
			String realPuid = null;
			if (StaticValues.config.preventXfmt) {
				List<IdentificationResult> temp = finalResults.getResults();
				if (temp.size() > 0) {
					boolean foundCorrectFmt = false;
					for (IdentificationResult identResult : finalResults.getResults()) {
						String puid = identResult.getPuid();
						if (! foundCorrectFmt && ! puid.startsWith(StaticValues.FORMAT_XFMT)) {
							foundCorrectFmt = true;
							realPuid = puid;
							break;
						}
					}
					if (!foundCorrectFmt) {
						for (String puid : hash) {
							if (! foundCorrectFmt && ! puid.startsWith(StaticValues.FORMAT_XFMT)) {
								foundCorrectFmt = true;
								realPuid = puid;
								break;
							}
						}
					}
				}
			}
			for (IdentificationResult identResult : finalResults.getResults()) {
				String puid = identResult.getPuid();
				if (!container && JIP_ARCHIVE.equals(puid)) {
					puid = ZIP_ARCHIVE;
				}
				if (StaticValues.config.preventXfmt) {
					if (! puid.startsWith(StaticValues.FORMAT_XFMT)) {
						realPuid = puid;
					} else if (realPuid == null) {
						realPuid = puid;
					}
				} else {
					realPuid = puid;
				}
				if (subFormat) {
					String text = fileName + "," + realPuid;
					for (String string : hash) {
						text += "," + string;
					}
					System.out.println(text);

				} else {
					System.out.println(fileName + "," + realPuid);
				}
				if (archives && !container) {
					if (GZIP_ARCHIVE.equals(puid)) {
						GZipArchiveContentIdentifier gzipArchiveIdentifier =
								new GZipArchiveContentIdentifier(binarySignatureIdentifier,
										containerSignatureDefinitions, path, slash, slash1);
						gzipArchiveIdentifier.identify(results.getUri(), request);
					} else if (BZIP2_ARCHIVE.equals(puid)) {
						Bzip2ArchiveContentIdentifier bzip2ArchiveIdentifier =
								new Bzip2ArchiveContentIdentifier(binarySignatureIdentifier,
										containerSignatureDefinitions, path, slash, slash1);
						bzip2ArchiveIdentifier.identify(results.getUri(), request);
					} else if (TAR_ARCHIVE.equals(puid)) {
						TarArchiveContentIdentifier tarArchiveIdentifier =
								new TarArchiveContentIdentifier(binarySignatureIdentifier,
										containerSignatureDefinitions, path, slash, slash1);
						tarArchiveIdentifier.identify(results.getUri(), request);
					} else if (ZIP_ARCHIVE.equals(puid) || JIP_ARCHIVE.equals(puid)) {
						ZipArchiveContentIdentifier zipArchiveIdentifier =
								new ZipArchiveContentIdentifier(binarySignatureIdentifier,
										containerSignatureDefinitions, path, slash, slash1);
						zipArchiveIdentifier.identify(results.getUri(), request);
					}
				}
			}
		} else {
			System.out.println(fileName + "," + DroidFileFormat.Unknown);
		}
	}

	private final IdentificationResultCollection getContainerResults(
			final IdentificationResultCollection results,
			final IdentificationRequest request, final String fileName)
			throws CommandExecutionException {

		IdentificationResultCollection containerResults =
				new IdentificationResultCollection(request);

		if (results.getResults().size() > 0 && containerSignatureDefinitions != null) {
			for (IdentificationResult identResult : results.getResults()) {
				String filePuid = identResult.getPuid();
				if (filePuid != null) {
					TriggerPuid containerPuid = getTriggerPuidByPuid(filePuid);
					if (containerPuid != null) {

						requestFactory = new ContainerFileIdentificationRequestFactory();
						String containerType = containerPuid.getContainerType();

						if (OLE2_CONTAINER.equals(containerType)) {
							try {
								Ole2ContainerContentIdentifier ole2Identifier =
										new Ole2ContainerContentIdentifier();
								ole2Identifier.init(containerSignatureDefinitions, containerType);
								Ole2IdentifierEngine ole2IdentifierEngine = new Ole2IdentifierEngine();
								ole2IdentifierEngine.setRequestFactory(requestFactory);
								ole2Identifier.setIdentifierEngine(ole2IdentifierEngine);
								containerResults = ole2Identifier.process(
										request.getSourceInputStream(), containerResults);
							} catch (IOException e) { // carry on after container i/o problems
								System.err.println(e + SPACE + L_BRACKET + fileName + R_BRACKET);
							}
						} else if (ZIP_CONTAINER.equals(containerType)) {
							try {
								ZipContainerContentIdentifier zipIdentifier =
										new ZipContainerContentIdentifier();
								zipIdentifier.init(containerSignatureDefinitions, containerType);
								ZipIdentifierEngine zipIdentifierEngine = new ZipIdentifierEngine();
								zipIdentifierEngine.setRequestFactory(requestFactory);
								zipIdentifier.setIdentifierEngine(zipIdentifierEngine);
								containerResults = zipIdentifier.process(
										request.getSourceInputStream(), containerResults);
							} catch (IOException e) { // carry on after container i/o problems
								System.err.println(e + SPACE + L_BRACKET + fileName + R_BRACKET);
							}
						} else {
							throw new CommandExecutionException("Unknown container type: "
									+ containerPuid);
						}
					}
				}
			}
		}
		return containerResults;
	}

	private final TriggerPuid getTriggerPuidByPuid(final String puid) {
		for (final TriggerPuid tp : triggerPuids) {
			if (tp.getPuid().equals(puid)) {
				return tp;
			}
		}
		return null;
	}
}
