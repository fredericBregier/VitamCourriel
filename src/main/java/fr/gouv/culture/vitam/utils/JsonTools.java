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
package fr.gouv.culture.vitam.utils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import de.odysseus.staxon.json.JsonXMLConfig;
import de.odysseus.staxon.json.JsonXMLConfigBuilder;
import de.odysseus.staxon.json.JsonXMLInputFactory;
import de.odysseus.staxon.json.JsonXMLOutputFactory;
import de.odysseus.staxon.xml.util.PrettyXMLEventWriter;

/**
 * @author "Frederic Bregier"
 *
 */
public class JsonTools {

	public static final void convertXmlFileToJsonFile(String xml, String json) {
		FileInputStream inputStream = null;
		FileOutputStream outputStream = null;
		XMLEventReader reader = null;
		XMLEventWriter writer = null;
		try {
			inputStream = new FileInputStream(xml);
			outputStream = new FileOutputStream(json);
			JsonXMLConfig config = new JsonXMLConfigBuilder()
				.autoArray(true).autoPrimitive(true).prettyPrint(true).build();
			/*
			 * Create reader (XML).
			 */
			reader = XMLInputFactory.newInstance().createXMLEventReader(inputStream);
	
			/*
			 * Create writer (JSON).
			 */
			writer = new JsonXMLOutputFactory(config).createXMLEventWriter(outputStream);
			
			/*
			 * Copy events from reader to writer.
			 */
			writer.add(reader);
			writer.flush();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (XMLStreamException e) {
			e.printStackTrace();
		} catch (FactoryConfigurationError e) {
			e.printStackTrace();
		} catch (TransformerFactoryConfigurationError e) {
			e.printStackTrace();
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
				}
			}
			if (outputStream != null) {
				try {
					outputStream.close();
				} catch (IOException e) {
				}
			}
			/*
			 * Close reader/writer.
			 */
			if (reader != null) {
				try {
					reader.close();
				} catch (XMLStreamException e) {
				}
			}
			if (writer != null) {
				try {
					writer.close();
				} catch (XMLStreamException e) {
				}
			}
		}
	}

	public static final void convertJsonFileToXmlFile(String json, String xml) {
		FileInputStream inputStream = null;
		FileOutputStream outputStream = null;
		XMLEventReader reader = null;
		XMLEventWriter writer = null;
		try {
			inputStream = new FileInputStream(json);
			outputStream = new FileOutputStream(xml);
			JsonXMLConfig config = new JsonXMLConfigBuilder().multiplePI(false).build();
			/*
			 * Create reader (JSON).
			 */
			reader = new JsonXMLInputFactory(config).createXMLEventReader(inputStream);
	
			/*
			 * Create writer (XML).
			 */
			writer = XMLOutputFactory.newInstance().createXMLEventWriter(outputStream);
			writer = new PrettyXMLEventWriter(writer); // format output
			/*
			 * Copy events from reader to writer.
			 */
			writer.add(reader);
			writer.flush();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (XMLStreamException e) {
			e.printStackTrace();
		} catch (FactoryConfigurationError e) {
			e.printStackTrace();
		} catch (TransformerFactoryConfigurationError e) {
			e.printStackTrace();
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
				}
			}
			if (outputStream != null) {
				try {
					outputStream.close();
				} catch (IOException e) {
				}
			}
			/*
			 * Close reader/writer.
			 */
			if (reader != null) {
				try {
					reader.close();
				} catch (XMLStreamException e) {
				}
			}
			if (writer != null) {
				try {
					writer.close();
				} catch (XMLStreamException e) {
				}
			}
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length < 1) {
			System.err.println("Need an XML as argument");
			return;
		}
		convertXmlFileToJsonFile(args[0], args[0]+".json");
		convertJsonFileToXmlFile(args[0]+".json", args[0]+".xml");
	}

}
