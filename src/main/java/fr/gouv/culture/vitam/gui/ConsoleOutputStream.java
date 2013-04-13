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
package fr.gouv.culture.vitam.gui;

import java.awt.Color;
import java.io.ByteArrayOutputStream;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

/**
 * Used to output console System.out and System.err
 * 
 * @author "Frederic Bregier"
 * 
 */
public class ConsoleOutputStream extends ByteArrayOutputStream {

	private SimpleAttributeSet attributes;
	private JTextComponent texte;
	private Document document;

	/**
	 * 
	 * @param texte
	 * @param textColor
	 *            optional (maybe null)
	 */
	public ConsoleOutputStream(JTextComponent texte, Color textColor) {
		if (textColor != null) {
			attributes = new SimpleAttributeSet();
			StyleConstants.setForeground(attributes, textColor);
		}
		this.texte = texte;
		this.document = texte.getDocument();
	}

	/**
	 * Override this method to intercept the output text. Each line of text output will actually
	 * involve invoking this method twice:
	 * 
	 * a) for the actual text message b) for the newLine string
	 */
	public void flush() {
		try {
			int offset = document.getLength();
			document.insertString(offset, new String(this.toByteArray(), 0, this.size()),
					attributes);
			reset();
			texte.setCaretPosition(document.getLength());
		} catch (BadLocationException ble) {
		}
	}

}
