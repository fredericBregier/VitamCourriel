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
package fr.gouv.culture.vitam.pdfa;

import java.io.File;
import java.util.ArrayList;

import org.dom4j.Attribute;
import org.dom4j.Element;

import fr.gouv.culture.vitam.utils.ConfigLoader;
import fr.gouv.culture.vitam.utils.Executor;
import fr.gouv.culture.vitam.utils.StaticValues;
import fr.gouv.culture.vitam.utils.SystemPropertyUtil;
import fr.gouv.culture.vitam.utils.XmlDom;

/**
 * PDF/A-1B converter
 * 
 * @author Frederic Bregier
 *
 */
public class PdfaConverter {

	/**
	 * Convert the file rootdir/basepath/basename into Pdf/A-1B in outdir/basepath/basename <br>
	 * <br>
	 * Note: Any LibreOffice/OpenOffice running instance will be killed!
	 * 
	 * @param basepath could be as simple as "/"
	 * @param basename
	 * @param rootdir
	 * @param outdir Must not contains space in the path !
	 * @param config
	 * @return the Element as result
	 */
	public static Element convertPdfA(String basepath, String basename, 
			File rootdir, File outdir, ConfigLoader config) {
		String temppdfname = basename.substring(0, basename.lastIndexOf('.')) + ".pdf";
		String pdfname = basename + ".pdf";
		if (! basepath.endsWith(File.separator)) {
			basepath += File.separator;
		}
		File sourceFile = new File(rootdir, basepath + basename);
		File targetDir = new File(outdir, basepath);
		targetDir.mkdirs();
		File targetFile = new File(targetDir, pdfname);
		File targetTempFile = new File(targetDir, temppdfname);
		if (targetFile.exists()) {
			System.err.println(StaticValues.LBL.error_warning.get() + " destination exist: " +
					targetFile.getAbsolutePath());
		}
		if (targetTempFile.exists()) {
			System.err.println(StaticValues.LBL.error_warning.get() + " temp destination exist: " +
					targetTempFile.getAbsolutePath());
		}

		Element root = XmlDom.factory.createElement("convert");
		Element newElt = XmlDom.factory.createElement("file");
		newElt.addAttribute("filename", basepath + basename);
		root.add(newElt);
		newElt = XmlDom.factory.createElement("pdfa");
		Attribute targetName = XmlDom.factory.createAttribute(newElt, "filename", 
				basepath + pdfname);
		newElt.add(targetName);
		root.add(newElt);
		
		// Check Office installation first
		String osName = SystemPropertyUtil.get("os.name").toLowerCase();
		String python = null;
		if (osName.indexOf("win") >= 0) {
			python = config.LIBREOFFICE_HOME+"\\program\\python.exe";
		} else {
			python  = config.LIBREOFFICE_HOME+"/program/python.bin";
		}
		File fpython = new File(python);
		if (! fpython.exists()) {
			System.err.println(StaticValues.LBL.error_filenotfound.get() + " LibreOffice");
			root.addAttribute("status", "Error LibreOffice not found");
			return root;
		}
		// convertion
		boolean done = false;
		ArrayList<String> command = new ArrayList<String>();
		command.add(fpython.getAbsolutePath());
		command.add(config.UNOCONV);
		//command.add("-v");
		command.add("-f");
		command.add("pdf");
		command.add("-eSelectPdfVersion=1");
		command.add("--output=" + targetDir.getAbsolutePath());
		command.add(sourceFile.getAbsolutePath());
		long wait = sourceFile.length() / 1024 * config.msPerKB;
		if (wait < config.lowLimitMs) {
			wait = config.lowLimitMs;
		}
		int status = Executor.exec(command, wait, 
				new int[] { 0 }, false, "soffice.bin");
		done = (status == 0 || status == 1);
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
		}
		Executor.killProcess("soffice.bin");
	
		if (done) {
			if (!targetTempFile.renameTo(targetFile)) {
				targetName.setText(temppdfname);
			}
			root.addAttribute("status", "ok");
		} else {
			root.addAttribute("status", "Error during convertion");
		}
		return root;
	}

}
