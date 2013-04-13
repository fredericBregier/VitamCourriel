/**
 * This file is part of Vitam Project.
 * 
 * Copyright 2010, Frederic Bregier, and individual contributors by the @author
 * tags. See the COPYRIGHT.txt in the distribution for a full listing of individual contributors.
 * 
 * All Vitam Project is free software: you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 * 
 * Vitam is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with Vitam. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package fr.gouv.culture.vitam.droid;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.apache.commons.io.FileUtils;
import org.dom4j.io.XMLWriter;

import de.schlichtherle.io.FileOutputStream;

import fr.gouv.culture.vitam.gui.VitamGui.RunnerLongTask;
import fr.gouv.culture.vitam.utils.StaticValues;
import fr.gouv.culture.vitam.utils.VitamArgument;

import uk.gov.nationalarchives.droid.command.ResultPrinter;
import uk.gov.nationalarchives.droid.command.action.CommandExecutionException;
import uk.gov.nationalarchives.droid.container.ContainerSignatureDefinitions;
import uk.gov.nationalarchives.droid.container.ContainerSignatureSaxParser;
import uk.gov.nationalarchives.droid.core.VitamBinarySignatureIdentifier;
import uk.gov.nationalarchives.droid.core.SignatureParseException;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationRequest;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationResultCollection;
import uk.gov.nationalarchives.droid.core.interfaces.RequestIdentifier;
import uk.gov.nationalarchives.droid.core.interfaces.resource.FileSystemIdentificationRequest;
import uk.gov.nationalarchives.droid.core.interfaces.resource.RequestMetaData;
import uk.gov.nationalarchives.droid.core.signature.FileFormat;

/**
 * DroidHandler for Vitam
 * 
 * @author "Frederic Bregier"
 * 
 */
public class DroidHandler {

	/**
	 * 
	 */
	public DroidHandler() {
	}

	private static final String FORWARD_SLASH = "/";
	private static final String BACKWARD_SLASH = "\\";
	private static VitamBinarySignatureIdentifier vitamBinarySignatureIdentifier;
	private static ContainerSignatureDefinitions containerSignatureDefinitions;

	/**
	 * Initialize Droid support through the 2 signature files and the limit of scan (default should
	 * be -1)
	 * 
	 * @param fileSignaturesFileName
	 * @param containerSignaturesFileName
	 * @param maxBytesToScan
	 * @throws CommandExecutionException
	 */
	public static void initialize(String fileSignaturesFileName,
			String containerSignaturesFileName, int maxBytesToScan)
			throws CommandExecutionException {
		File fileSignaturesFile = new File(fileSignaturesFileName);
		if (!fileSignaturesFile.exists()) {
			throw new CommandExecutionException("Signature file not found");
		}

		vitamBinarySignatureIdentifier = new VitamBinarySignatureIdentifier();

		vitamBinarySignatureIdentifier.setSignatureFile(fileSignaturesFileName);
		try {
			vitamBinarySignatureIdentifier.init();
		} catch (SignatureParseException e) {
			throw new CommandExecutionException("Can't parse signature file", e);
		}
		if (maxBytesToScan == 0) {
			maxBytesToScan = -1;
		}
		vitamBinarySignatureIdentifier.setMaxBytesToScan(maxBytesToScan);
		
		if (containerSignaturesFileName != null) {
			File containerSignaturesFile = new File(containerSignaturesFileName);
			if (!containerSignaturesFile.exists()) {
				throw new CommandExecutionException("Container signature file not found");
			}
			try {
				final InputStream in = new FileInputStream(containerSignaturesFileName);
				ContainerSignatureSaxParser parser = new ContainerSignatureSaxParser();
				containerSignatureDefinitions = parser.parse(in);
				in.close();
				parser = null;
			} catch (SignatureParseException e) {
				throw new CommandExecutionException("Can't parse container signature file", e);
			} catch (IOException ioe) {
				throw new CommandExecutionException(ioe);
			} catch (JAXBException jaxbe) {
				throw new CommandExecutionException(jaxbe);
			}
		}

	}

	/**
	 * Reach check
	 * 
	 * @param file
	 * @param resultPrinter
	 * @throws CommandExecutionException
	 */
	private final void realCheck(File file, ResultPrinter resultPrinter)
			throws CommandExecutionException {
		String fileName;
		try {
			fileName = file.getCanonicalPath();
		} catch (IOException e) {
			throw new CommandExecutionException(e);
		}
		URI uri = file.toURI();
		RequestMetaData metaData =
				new RequestMetaData(file.length(), file.lastModified(), fileName);
		RequestIdentifier identifier = new RequestIdentifier(uri);
		identifier.setParentId(1L);

		InputStream in = null;
		IdentificationRequest request = new FileSystemIdentificationRequest(metaData, identifier);
		try {
			in = new FileInputStream(file);
			request.open(in);
			IdentificationResultCollection results =
					vitamBinarySignatureIdentifier.matchBinarySignatures(request);

			resultPrinter.print(results, request);
		} catch (IOException e) {
			throw new CommandExecutionException(e);
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					throw new CommandExecutionException(e);
				}
			}
			try {
				request.close();
			} catch (IOException e) {
			}
		}
	}

	/**
	 * Get the valid list of files
	 * 
	 * @param resources
	 * @param extensions
	 * @param recursive
	 * @return the valid list of files
	 * @throws CommandExecutionException
	 */
	public static final List<File> matchedFiled(String[] resources, String[] extensions,
			boolean recursive) throws CommandExecutionException {
		List<File> matchedFiles = new ArrayList<File>(1);
		if (resources == null || resources.length == 0) {
			throw new CommandExecutionException("Resources not specified");
		}
		File dirToSearch;
		for (int i = 0; i < resources.length; i++) {
			dirToSearch = new File(resources[i]);
			if (!dirToSearch.isDirectory()) {
				if (dirToSearch.canRead()) {
					matchedFiles.add(dirToSearch);
				} else {
					throw new CommandExecutionException("Resources not found");
				}
			} else {
				matchedFiles.addAll(FileUtils.listFiles(dirToSearch, extensions, recursive));
			}
		}
		return matchedFiles;
	}

	/**
	 * Get the valid list of files
	 * 
	 * @param resources
	 * @param extensions
	 * @param recursive
	 * @return the valid list of files
	 * @throws CommandExecutionException
	 */
	public static final List<File> matchedFiled(File[] resources, String[] extensions,
			boolean recursive) throws CommandExecutionException {
		List<File> matchedFiles = new ArrayList<File>(1);
		if (resources == null || resources.length == 0) {
			throw new CommandExecutionException("Resources not specified");
		}
		File dirToSearch;
		for (int i = 0; i < resources.length; i++) {
			dirToSearch = resources[i];
			if (!dirToSearch.isDirectory()) {
				if (dirToSearch.canRead()) {
					matchedFiles.add(dirToSearch);
				} else {
					throw new CommandExecutionException("Resources not found");
				}
			} else {
				matchedFiles.addAll(FileUtils.listFiles(dirToSearch, extensions, recursive));
			}
		}
		return matchedFiles;
	}

	/**
	 * Check multiples files, allowing recursive scanning
	 * 
	 * @param resources
	 * @param extensions
	 * @param argument
	 * @param task
	 *            optional
	 * @return a List of DroidFileFormat
	 * @throws CommandExecutionException
	 */
	public final List<DroidFileFormat> checkFilesFormat(File[] resources, String[] extensions,
			VitamArgument argument, RunnerLongTask task) throws CommandExecutionException {
		List<File> matchedFiles = matchedFiled(resources, extensions, argument.recursive);
		return checkFilesFormat(matchedFiles, argument, task);
	}

	/**
	 * Check multiples files specify exactly by the list
	 * 
	 * @param matchedFiles
	 * @param argument
	 * @param task
	 *            optional
	 * @return a List of DroidFileFormat
	 * @throws CommandExecutionException
	 */
	public final List<DroidFileFormat> checkFilesFormat(List<File> matchedFiles,
			VitamArgument argument, RunnerLongTask task) throws CommandExecutionException {
		if (matchedFiles.isEmpty()) {
			throw new CommandExecutionException("Resources not specified");
		}
		File dirToSearch = matchedFiles.get(0);
		String path = dirToSearch.getAbsolutePath();
		String slash = path.contains(FORWARD_SLASH) ? FORWARD_SLASH : BACKWARD_SLASH;
		String slash1 = slash;

		path = "";
		ResultPrinter resultPrinter =
				new ResultPrinter(vitamBinarySignatureIdentifier, containerSignatureDefinitions,
						path, slash, slash1, argument.archive, argument.checkSubFormat);

		// change System.out
		PrintStream oldOut = System.out;
		DroidFileFormatOutputStream out =
				new DroidFileFormatOutputStream(vitamBinarySignatureIdentifier.getSigFile(),
						argument);
		PrintStream newOut = new PrintStream(out, true);
		try {
			System.setOut(newOut);
			int currank = 0;
			for (File file : matchedFiles) {
				currank++;
				realCheck(file, resultPrinter);
				if (task != null) {
					float value = ((float) currank) / (float) matchedFiles.size();
					value *= 100;
					task.setProgressExternal((int) value);
				}
			}
		} finally {
			// reset System.out
			System.setOut(oldOut);
			newOut.close();
			newOut = null;
		}
		return out.getResult();
	}

	/**
	 * Check multiples files, allowing recursive scanning
	 * 
	 * @param resources
	 * @param extensions
	 * @param argument
	 * @param task
	 *            optional
	 * @return a List of DroidFileFormat
	 * @throws CommandExecutionException
	 */
	public final List<DroidFileFormat> checkFilesFormat(String[] resources, String[] extensions,
			VitamArgument argument, RunnerLongTask task) throws CommandExecutionException {
		List<File> matchedFiles = matchedFiled(resources, extensions, argument.recursive);
		return checkFilesFormat(matchedFiles, argument, task);
	}

	/**
	 * Check one file
	 * 
	 * @param filename
	 * @param argument
	 * @return a List of DroidFileFOrmat
	 * @throws CommandExecutionException
	 */
	public final List<DroidFileFormat> checkFileFormat(String filename, VitamArgument argument)
			throws CommandExecutionException {
		if (filename == null || filename.length() == 0) {
			throw new CommandExecutionException("File not specified");
		}
		File file = new File(filename);
		return checkFileFormat(file, argument);
	}

	/**
	 * Check one file
	 * 
	 * @param file
	 * @param argument
	 * @return a List of DroidFileFOrmat
	 * @throws CommandExecutionException
	 */
	public List<DroidFileFormat> checkFileFormat(File file, VitamArgument argument)
			throws CommandExecutionException {
		Collection<File> matchedFiles = new ArrayList<File>(1);

		String path = file.getAbsolutePath();
		String slash = path.contains(FORWARD_SLASH) ? FORWARD_SLASH : BACKWARD_SLASH;
		String slash1 = slash;

		matchedFiles.add(file);
		path = "";
		ResultPrinter resultPrinter =
				new ResultPrinter(vitamBinarySignatureIdentifier, containerSignatureDefinitions,
						path, slash, slash1, argument.archive, argument.checkSubFormat);

		// change System.out
		PrintStream oldOut = System.out;
		DroidFileFormatOutputStream out =
				new DroidFileFormatOutputStream(vitamBinarySignatureIdentifier.getSigFile(),
						argument);
		PrintStream newOut = new PrintStream(out, true);
		try {
			System.setOut(newOut);
			realCheck(file, resultPrinter);
		} finally {
			// reset System.out
			System.setOut(oldOut);
			newOut.close();
			newOut = null;
		}
		return out.getResult();
	}

	public static final void cleanTempFiles() {
		File tmpDir = new File(System.getProperty("java.io.tmpdir"));
		File [] todelete = tmpDir.listFiles(new FileFilter() {
			
			@Override
			public boolean accept(File arg0) {
				String name = arg0.getName();
				return (name.endsWith(".tmp") && (name.startsWith(StaticValues.PREFIX_TEMPFILE)|| 
						name.startsWith("droid-archive")));
			}
		});
		for (File file : todelete) {
			if (! file.delete()) {
				file.deleteOnExit();
			}
		}
	}
	/**
	 * Reach check but on InputStream
	 * 
	 * @param in
	 * @param resultPrinter
	 * @throws CommandExecutionException
	 */
	private final void realCheck(InputStream in, ResultPrinter resultPrinter)
			throws CommandExecutionException {
		File defaultFile = null;
		FileOutputStream out = null;
		try {
			defaultFile = File.createTempFile(StaticValues.PREFIX_TEMPFILE, ".tmp");
			out = new FileOutputStream(defaultFile);
			int len = 0;
			byte [] bytes = new byte[8192];
			while ( (len = in.read(bytes)) != -1) {
				out.write(bytes, 0, len);
			}
			out.flush();
			out.close();
			out = null;
			in.close();
			in = null;
			realCheck(defaultFile, resultPrinter);
		} catch (IOException e2) {
			System.err.println(StaticValues.LBL.error_error.get() + e2.toString());
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
				}
			}
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
				}
			}
			if (! defaultFile.delete()) {
				defaultFile.deleteOnExit();
			}
		}
	}

	/**
	 * Check one InputStream
	 * 
	 * @param in
	 * @param argument
	 * @return a List of DroidFileFOrmat
	 * @throws CommandExecutionException
	 */
	public List<DroidFileFormat> checkFileFormat(InputStream in, VitamArgument argument)
			throws CommandExecutionException {
		String path = "";
		String slash = File.separator;
		String slash1 = slash;

		path = "";
		ResultPrinter resultPrinter =
				new ResultPrinter(vitamBinarySignatureIdentifier, containerSignatureDefinitions,
						path, slash, slash1, argument.archive, argument.checkSubFormat);

		// change System.out
		PrintStream oldOut = System.out;
		DroidFileFormatOutputStream out =
				new DroidFileFormatOutputStream(vitamBinarySignatureIdentifier.getSigFile(),
						argument);
		PrintStream newOut = new PrintStream(out, true);
		try {
			System.setOut(newOut);
			realCheck(in, resultPrinter);
		} finally {
			// reset System.out
			System.setOut(oldOut);
			newOut.close();
			newOut = null;
		}
		return out.getResult();
	}

	/**
	 * 
	 * @param puid
	 * @param filename
	 * @return the associated DroidFileFormat from puid
	 */
	public DroidFileFormat getDroidFileFormatFromPuid(String puid, String filename) {
		FileFormat format = vitamBinarySignatureIdentifier.getSigFile().getFileFormat(puid);
		if (format == null) {
			System.err.println(StaticValues.LBL.error_notfound.get() + puid);
			return null;
		}
		return new DroidFileFormat(filename, format);
	}

	/**
	 * 
	 * @return the version of the signatures
	 */
	public String getVersionSignature() {
		return vitamBinarySignatureIdentifier.getSigFile().getVersion();
	}
	/**
	 * 
	 * @return the date of the signatures
	 */
	public String getDateSignature() {
		return vitamBinarySignatureIdentifier.getSigFile().getDateCreated();
	}
	/**
	 * Example only
	 * 
	 * @param args
	 * @throws CommandExecutionException
	 */
	public static void main(String[] args) throws CommandExecutionException {
		StaticValues.initialize();
		String signatureFile = StaticValues.resourceToFile(StaticValues.config.SIGNATURE_FILE);
		String containerSignatureFile = StaticValues
				.resourceToFile(StaticValues.config.CONTAINER_SIGNATURE_FILE);

		// Init Signature
		initialize(signatureFile, containerSignatureFile, -1);

		// Prepare command
		DroidHandler droid = new DroidHandler();

		String[] tocheck = new String[] { "J:\\Git\\SEDA\\droid-binary-6.1-bin\\doc",
				"J:\\Git\\SEDA\\droid-binary-6.1-bin\\doc\\vitam.xlsx" };

		// Execute and get result
		List<DroidFileFormat> list = null;
		try {
			list = droid.checkFilesFormat(tocheck, null, VitamArgument.NOFEATURE, null);
		} catch (CommandExecutionException e) {
			e.printStackTrace();
		}

		// for the example, print out the result as is
		XMLWriter writer = null;
		try {
			writer = new XMLWriter(System.out, StaticValues.defaultOutputFormat);
		} catch (UnsupportedEncodingException e1) {
		}
		try {
			for (DroidFileFormat droidFileFormat : list) {
				writer.write(droidFileFormat.toElement(true));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			list = droid.checkFileFormat(tocheck[1], VitamArgument.SHAALLONLY);
		} catch (CommandExecutionException e) {
			e.printStackTrace();
		}
		try {
			for (DroidFileFormat droidFileFormat : list) {
				writer.write(droidFileFormat.toElement(true));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			list = droid.checkFileFormat(tocheck[1], VitamArgument.SHAALLONLY);
			for (DroidFileFormat droidFileFormat : list) {
				System.out.println(droidFileFormat.toStringCsv());
			}
		} catch (CommandExecutionException e) {
			e.printStackTrace();
		}
	}
}
