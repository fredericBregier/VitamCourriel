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

import java.io.IOException;
import java.io.PipedInputStream;

import javax.swing.text.Document;
import javax.swing.text.JTextComponent;

/**
 * Thread used to read STDOUT STDERR and to place to the correct output
 * 
 * @author "Frederic Bregier"
 * 
 */
public class ReaderThread extends Thread {
	PipedInputStream pi;
	JTextComponent texte;

	ReaderThread(PipedInputStream pi, JTextComponent texte) {
		super();
		this.pi = pi;
		this.texte = texte;
	}

	public void run() {
		try {
			while (true) {
				final byte[] buf = new byte[2048];
				final int len = pi.read(buf, 0, 2048);
				if (len == -1) {
					break;
				}
				Document doc = texte.getDocument();
				try {
					doc.insertString(doc.getLength(), new String(buf, 0, len), null);
					texte.setCaretPosition(texte.getDocument().getLength());
				} catch (Exception e) {
				}
			}
		} catch (IOException e) {
		}
	}
}
