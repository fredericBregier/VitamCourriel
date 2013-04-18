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

import java.awt.BorderLayout;

import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JButton;
import javax.swing.JLabel;

import fr.gouv.culture.vitam.utils.FileExtensionFilter;
import fr.gouv.culture.vitam.utils.StaticValues;
import fr.gouv.culture.vitam.utils.VitamArgument.VitamOutputModel;

import javax.swing.JTextField;

import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import javax.swing.JCheckBox;
import javax.swing.JRadioButton;
import javax.swing.ButtonGroup;
import javax.swing.SwingConstants;
import javax.swing.ImageIcon;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.File;
import java.text.NumberFormat;

import javax.swing.border.CompoundBorder;

/**
 * Dialog to handle configuration change in the GUI
 * 
 * @author "Frederic Bregier"
 * 
 */
public class VitamConfigDialog extends JPanel {
	private static final long serialVersionUID = 5129887729538501977L;
	JFrame frame;
	private VitamGui vitamGui;
	private static boolean fromMain = false;
	private JTextField xsdroot;
	private JTextField namespace;
	private JTextField filefield;
	private JTextField fileattr;
	private JTextField digestfield;
	private JTextField algoattr;
	private JTextField mimeattr;
	private JTextField formatattr;
	private JTextField signature;
	private JTextField container;
	private final ButtonGroup buttonGroup = new ButtonGroup();
	private JTextField fitshome;
	private JRadioButton rdbtnCsvOutput;
	private JRadioButton rdbtnMultipleXmlOutput;
	private JRadioButton rdbtnSingleXmlOutput;
	private JCheckBox chckbxArchives;
	private JCheckBox chckbxRecursiveChecking;
	private JCheckBox chckbxShaDigest1;
	private JCheckBox chckbxShaDigest256;
	private JCheckBox chckbxShaDigest512;
	private JTextField docField;
	private JCheckBox chckbxProposeFileSave;
	private JCheckBox chckbxWarnXfmt;
	private JTextField textLibreOffice;
	private JTextField textUnoconv;
	private JFormattedTextField msperkb;
	private JFormattedTextField lowlimitms;
	private JFormattedTextField ranklimit;
	private JFormattedTextField wordlimit;
	private JCheckBox chckbxUpdateConfigurationFile;
	private JCheckBox chckbxExtractKeywords;
	private JTextField outputDirField;
	private JCheckBox chckbxExtractEmlBody;
	/**
	 * @param frame the parent frame
	 * @param vitamGui the VitamGui associated
	 */
	public VitamConfigDialog(JFrame frame, VitamGui vitamGui) {
		super(new BorderLayout());
		this.vitamGui = vitamGui;
		this.frame = frame;
		setBorder(new CompoundBorder());

		JPanel buttonPanel = new JPanel();
		GridBagLayout buttons = new GridBagLayout();
		buttons.columnWidths = new int[] { 194, 124, 0, 0, 0 };
		buttons.rowHeights = new int[] { 0, 0 };
		buttons.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		buttons.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
		buttonPanel.setLayout(buttons);
		add(buttonPanel, BorderLayout.SOUTH);

		JButton btnSaveConfig = new JButton(StaticValues.LBL.button_save.get());
		btnSaveConfig.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveConfig();
			}
		});
		GridBagConstraints gbc_btnSaveConfig = new GridBagConstraints();
		gbc_btnSaveConfig.insets = new Insets(0, 0, 0, 5);
		gbc_btnSaveConfig.gridx = 0;
		gbc_btnSaveConfig.gridy = 0;
		buttonPanel.add(btnSaveConfig, gbc_btnSaveConfig);

		String text = StaticValues.LBL.button_cancel.get();
		if (fromMain) {
			text += " " + StaticValues.LBL.button_exit.get();
		}
		JButton btnCancel = new JButton(text);
		btnCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				cancel();
			}
		});
		GridBagConstraints gbc_btnCancel = new GridBagConstraints();
		gbc_btnCancel.insets = new Insets(0, 0, 0, 5);
		gbc_btnCancel.gridx = 1;
		gbc_btnCancel.gridy = 0;
		buttonPanel.add(btnCancel, gbc_btnCancel);
		
		chckbxUpdateConfigurationFile = new JCheckBox(StaticValues.LBL.button_update.get());
		GridBagConstraints gbc_chckbxUpdateConfigurationFile = new GridBagConstraints();
		gbc_chckbxUpdateConfigurationFile.insets = new Insets(0, 0, 0, 5);
		gbc_chckbxUpdateConfigurationFile.gridx = 2;
		gbc_chckbxUpdateConfigurationFile.gridy = 0;
		chckbxUpdateConfigurationFile.setSelected(true);
		buttonPanel.add(chckbxUpdateConfigurationFile, gbc_chckbxUpdateConfigurationFile);

		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		add(tabbedPane, BorderLayout.CENTER);
		xmlPanel(tabbedPane);
		toolPanel(tabbedPane);
		outputPanel(tabbedPane);
	}
	
	private void xmlPanel(JTabbedPane tabbedPane) {
		JPanel xmlFilePanel = new JPanel();
		tabbedPane.addTab("XML Context", null, xmlFilePanel, null);
		GridBagLayout gbl_xmlFilePanel = new GridBagLayout();
		gbl_xmlFilePanel.columnWidths = new int[] { 21, 38, 86, 0, 45, 86, 72, 34, 0 };
		gbl_xmlFilePanel.rowHeights = new int[] { 0, 20, 0, 0, 0, 0, 0, 0, 0 };
		gbl_xmlFilePanel.columnWeights = new double[] { 0.0, 0.0, 1.0, 0.0, 0.0, 1.0, 0.0, 1.0,
				Double.MIN_VALUE };
		gbl_xmlFilePanel.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 };
		xmlFilePanel.setLayout(gbl_xmlFilePanel);

		JLabel lblXsdRoot = new JLabel("XSD Root");
		GridBagConstraints gbc_lblXsdRoot = new GridBagConstraints();
		gbc_lblXsdRoot.anchor = GridBagConstraints.EAST;
		gbc_lblXsdRoot.insets = new Insets(0, 0, 5, 5);
		gbc_lblXsdRoot.gridx = 1;
		gbc_lblXsdRoot.gridy = 1;
		xmlFilePanel.add(lblXsdRoot, gbc_lblXsdRoot);

		xsdroot = new JTextField();
		GridBagConstraints gbc_xsdroot = new GridBagConstraints();
		gbc_xsdroot.gridwidth = 2;
		gbc_xsdroot.insets = new Insets(0, 0, 5, 5);
		gbc_xsdroot.fill = GridBagConstraints.HORIZONTAL;
		gbc_xsdroot.gridx = 2;
		gbc_xsdroot.gridy = 1;
		xmlFilePanel.add(xsdroot, gbc_xsdroot);
		xsdroot.setColumns(10);

		JLabel lblNamespace = new JLabel("Namespace");
		GridBagConstraints gbc_lblNamespace = new GridBagConstraints();
		gbc_lblNamespace.anchor = GridBagConstraints.EAST;
		gbc_lblNamespace.insets = new Insets(0, 0, 5, 5);
		gbc_lblNamespace.gridx = 4;
		gbc_lblNamespace.gridy = 1;
		xmlFilePanel.add(lblNamespace, gbc_lblNamespace);

		namespace = new JTextField();
		GridBagConstraints gbc_namespace = new GridBagConstraints();
		gbc_namespace.gridwidth = 2;
		gbc_namespace.insets = new Insets(0, 0, 5, 5);
		gbc_namespace.fill = GridBagConstraints.HORIZONTAL;
		gbc_namespace.gridx = 5;
		gbc_namespace.gridy = 1;
		xmlFilePanel.add(namespace, gbc_namespace);
		namespace.setColumns(10);

		JLabel lblDocumentField = new JLabel("Document field");
		GridBagConstraints gbc_lblDocumentField = new GridBagConstraints();
		gbc_lblDocumentField.anchor = GridBagConstraints.EAST;
		gbc_lblDocumentField.insets = new Insets(0, 0, 5, 5);
		gbc_lblDocumentField.gridx = 1;
		gbc_lblDocumentField.gridy = 2;
		xmlFilePanel.add(lblDocumentField, gbc_lblDocumentField);

		docField = new JTextField();
		GridBagConstraints gbc_textLibreOffice = new GridBagConstraints();
		gbc_textLibreOffice.gridwidth = 2;
		gbc_textLibreOffice.insets = new Insets(0, 0, 5, 5);
		gbc_textLibreOffice.fill = GridBagConstraints.HORIZONTAL;
		gbc_textLibreOffice.gridx = 2;
		gbc_textLibreOffice.gridy = 2;
		xmlFilePanel.add(docField, gbc_textLibreOffice);
		docField.setColumns(10);

		JButton btnHelp = new JButton("?");
		btnHelp.setToolTipText("Xpath resolution");
		btnHelp.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				showHelp(arg0);
			}
		});
		GridBagConstraints gbc_btnHelp = new GridBagConstraints();
		gbc_btnHelp.insets = new Insets(0, 0, 5, 5);
		gbc_btnHelp.gridx = 5;
		gbc_btnHelp.gridy = 2;
		xmlFilePanel.add(btnHelp, gbc_btnHelp);

		JLabel lblFileField = new JLabel("File field");
		GridBagConstraints gbc_lblFileField = new GridBagConstraints();
		gbc_lblFileField.anchor = GridBagConstraints.EAST;
		gbc_lblFileField.insets = new Insets(0, 0, 5, 5);
		gbc_lblFileField.gridx = 1;
		gbc_lblFileField.gridy = 3;
		xmlFilePanel.add(lblFileField, gbc_lblFileField);

		filefield = new JTextField();
		GridBagConstraints gbc_filefield = new GridBagConstraints();
		gbc_filefield.gridwidth = 2;
		gbc_filefield.insets = new Insets(0, 0, 5, 5);
		gbc_filefield.fill = GridBagConstraints.HORIZONTAL;
		gbc_filefield.gridx = 2;
		gbc_filefield.gridy = 3;
		xmlFilePanel.add(filefield, gbc_filefield);
		filefield.setColumns(10);

		JLabel lblFileAttribut = new JLabel("File attribut");
		GridBagConstraints gbc_lblFileAttribut = new GridBagConstraints();
		gbc_lblFileAttribut.anchor = GridBagConstraints.EAST;
		gbc_lblFileAttribut.insets = new Insets(0, 0, 5, 5);
		gbc_lblFileAttribut.gridx = 4;
		gbc_lblFileAttribut.gridy = 3;
		xmlFilePanel.add(lblFileAttribut, gbc_lblFileAttribut);

		fileattr = new JTextField();
		GridBagConstraints gbc_fileattr = new GridBagConstraints();
		gbc_fileattr.insets = new Insets(0, 0, 5, 5);
		gbc_fileattr.fill = GridBagConstraints.HORIZONTAL;
		gbc_fileattr.gridx = 5;
		gbc_fileattr.gridy = 3;
		xmlFilePanel.add(fileattr, gbc_fileattr);
		fileattr.setColumns(10);

		JLabel lblMimetype = new JLabel("MimeType attribute");
		GridBagConstraints gbc_lblMimetype = new GridBagConstraints();
		gbc_lblMimetype.anchor = GridBagConstraints.EAST;
		gbc_lblMimetype.insets = new Insets(0, 0, 5, 5);
		gbc_lblMimetype.gridx = 1;
		gbc_lblMimetype.gridy = 4;
		xmlFilePanel.add(lblMimetype, gbc_lblMimetype);

		mimeattr = new JTextField();
		GridBagConstraints gbc_mimeattr = new GridBagConstraints();
		gbc_mimeattr.gridwidth = 2;
		gbc_mimeattr.insets = new Insets(0, 0, 5, 5);
		gbc_mimeattr.fill = GridBagConstraints.HORIZONTAL;
		gbc_mimeattr.gridx = 2;
		gbc_mimeattr.gridy = 4;
		xmlFilePanel.add(mimeattr, gbc_mimeattr);
		mimeattr.setColumns(10);

		JLabel lblFormat = new JLabel("Format attribute");
		GridBagConstraints gbc_lblFormat = new GridBagConstraints();
		gbc_lblFormat.anchor = GridBagConstraints.EAST;
		gbc_lblFormat.insets = new Insets(0, 0, 5, 5);
		gbc_lblFormat.gridx = 4;
		gbc_lblFormat.gridy = 4;
		xmlFilePanel.add(lblFormat, gbc_lblFormat);

		formatattr = new JTextField();
		GridBagConstraints gbc_formatattr = new GridBagConstraints();
		gbc_formatattr.insets = new Insets(0, 0, 5, 5);
		gbc_formatattr.fill = GridBagConstraints.HORIZONTAL;
		gbc_formatattr.gridx = 5;
		gbc_formatattr.gridy = 4;
		xmlFilePanel.add(formatattr, gbc_formatattr);
		formatattr.setColumns(10);

		JLabel lblDigestField = new JLabel("Digest field");
		GridBagConstraints gbc_lblDigestField = new GridBagConstraints();
		gbc_lblDigestField.anchor = GridBagConstraints.EAST;
		gbc_lblDigestField.insets = new Insets(0, 0, 5, 5);
		gbc_lblDigestField.gridx = 1;
		gbc_lblDigestField.gridy = 5;
		xmlFilePanel.add(lblDigestField, gbc_lblDigestField);

		digestfield = new JTextField();
		GridBagConstraints gbc_digestfield = new GridBagConstraints();
		gbc_digestfield.gridwidth = 2;
		gbc_digestfield.insets = new Insets(0, 0, 5, 5);
		gbc_digestfield.fill = GridBagConstraints.HORIZONTAL;
		gbc_digestfield.gridx = 2;
		gbc_digestfield.gridy = 5;
		xmlFilePanel.add(digestfield, gbc_digestfield);
		digestfield.setColumns(10);

		JLabel lblAlgorithmAttribut = new JLabel("Algorithm attribut");
		GridBagConstraints gbc_lblAlgorithmAttribut = new GridBagConstraints();
		gbc_lblAlgorithmAttribut.anchor = GridBagConstraints.EAST;
		gbc_lblAlgorithmAttribut.insets = new Insets(0, 0, 5, 5);
		gbc_lblAlgorithmAttribut.gridx = 4;
		gbc_lblAlgorithmAttribut.gridy = 5;
		xmlFilePanel.add(lblAlgorithmAttribut, gbc_lblAlgorithmAttribut);

		algoattr = new JTextField();
		GridBagConstraints gbc_algoattr = new GridBagConstraints();
		gbc_algoattr.insets = new Insets(0, 0, 5, 5);
		gbc_algoattr.fill = GridBagConstraints.HORIZONTAL;
		gbc_algoattr.gridx = 5;
		gbc_algoattr.gridy = 5;
		xmlFilePanel.add(algoattr, gbc_algoattr);
		algoattr.setColumns(10);

	}
	private void toolPanel(JTabbedPane tabbedPane) {
		JPanel toolsPanel = new JPanel();
		tabbedPane.addTab("Tools", null, toolsPanel, null);
		GridBagLayout gbl_toolsPanel = new GridBagLayout();
		gbl_toolsPanel.columnWidths = new int[] { 0, 0, 0, 0, 0, 0, 0 };
		gbl_toolsPanel.rowHeights = new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		gbl_toolsPanel.columnWeights = new double[] { 0.0, 0.0, 1.0, 1.0 };
		gbl_toolsPanel.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 };
		toolsPanel.setLayout(gbl_toolsPanel);

		JLabel lblDroidSignature = new JLabel("Droid Signature");
		GridBagConstraints gbc_lblDroidSignature = new GridBagConstraints();
		gbc_lblDroidSignature.insets = new Insets(0, 0, 5, 5);
		gbc_lblDroidSignature.anchor = GridBagConstraints.EAST;
		gbc_lblDroidSignature.gridx = 1;
		gbc_lblDroidSignature.gridy = 0;
		toolsPanel.add(lblDroidSignature, gbc_lblDroidSignature);

		signature = new JTextField();
		GridBagConstraints gbc_signature = new GridBagConstraints();
		gbc_signature.gridwidth = 2;
		gbc_signature.insets = new Insets(0, 0, 5, 5);
		gbc_signature.fill = GridBagConstraints.HORIZONTAL;
		gbc_signature.gridx = 3;
		gbc_signature.gridy = 0;
		toolsPanel.add(signature, gbc_signature);
		signature.setColumns(10);

		JButton btnFilesign = new JButton();
		btnFilesign.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				File file = openFile(signature.getText(), "Droid Signature", "xml");
				if (file != null) {
					signature.setText(file.getAbsolutePath());
				}
			}
		});
		btnFilesign.setMargin(new Insets(2, 2, 2, 2));
		btnFilesign.setIcon(new ImageIcon(VitamConfigDialog.class
				.getResource(VitamGui.RESOURCES_IMG_CHECKFILES_PNG)));
		GridBagConstraints gbc_btnFilesign = new GridBagConstraints();
		gbc_btnFilesign.insets = new Insets(0, 0, 5, 5);
		gbc_btnFilesign.gridx = 5;
		gbc_btnFilesign.gridy = 0;
		toolsPanel.add(btnFilesign, gbc_btnFilesign);

		JLabel lblDroidContainer = new JLabel("Droid Container");
		GridBagConstraints gbc_lblDroidContainer = new GridBagConstraints();
		gbc_lblDroidContainer.anchor = GridBagConstraints.EAST;
		gbc_lblDroidContainer.insets = new Insets(0, 0, 5, 5);
		gbc_lblDroidContainer.gridx = 1;
		gbc_lblDroidContainer.gridy = 1;
		toolsPanel.add(lblDroidContainer, gbc_lblDroidContainer);

		container = new JTextField();
		GridBagConstraints gbc_container = new GridBagConstraints();
		gbc_container.gridwidth = 2;
		gbc_container.insets = new Insets(0, 0, 5, 5);
		gbc_container.fill = GridBagConstraints.HORIZONTAL;
		gbc_container.gridx = 3;
		gbc_container.gridy = 1;
		toolsPanel.add(container, gbc_container);
		container.setColumns(10);

		JButton btnFilecontain = new JButton();
		btnFilecontain.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				File file = openFile(container.getText(), "Droid Container Signature", "xml");
				if (file != null) {
					container.setText(file.getAbsolutePath());
				}
			}
		});
		btnFilecontain.setMargin(new Insets(2, 2, 2, 2));
		btnFilecontain.setIcon(new ImageIcon(VitamConfigDialog.class
				.getResource(VitamGui.RESOURCES_IMG_CHECKFILES_PNG)));
		GridBagConstraints gbc_btnFilecontain = new GridBagConstraints();
		gbc_btnFilecontain.insets = new Insets(0, 0, 5, 5);
		gbc_btnFilecontain.gridx = 5;
		gbc_btnFilecontain.gridy = 1;
		toolsPanel.add(btnFilecontain, gbc_btnFilecontain);

		JLabel lblFitsHome = new JLabel("Fits Home");
		GridBagConstraints gbc_lblFitsHome = new GridBagConstraints();
		gbc_lblFitsHome.anchor = GridBagConstraints.EAST;
		gbc_lblFitsHome.insets = new Insets(0, 0, 5, 5);
		gbc_lblFitsHome.gridx = 1;
		gbc_lblFitsHome.gridy = 2;
		toolsPanel.add(lblFitsHome, gbc_lblFitsHome);

		fitshome = new JTextField();
		fitshome.setColumns(10);
		GridBagConstraints gbc_fitshome = new GridBagConstraints();
		gbc_fitshome.gridwidth = 2;
		gbc_fitshome.insets = new Insets(0, 0, 5, 5);
		gbc_fitshome.fill = GridBagConstraints.HORIZONTAL;
		gbc_fitshome.gridx = 3;
		gbc_fitshome.gridy = 2;
		toolsPanel.add(fitshome, gbc_fitshome);

		JButton btnFitshome = new JButton();
		btnFitshome.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				File file = openFile(fitshome.getText(), "FITS home directory", null);
				if (file != null) {
					fitshome.setText(file.getAbsolutePath());
				}
			}
		});
		btnFitshome.setMargin(new Insets(2, 2, 2, 2));
		btnFitshome.setIcon(new ImageIcon(VitamConfigDialog.class
				.getResource(VitamGui.RESOURCES_IMG_CHECKFILES_PNG)));
		GridBagConstraints gbc_btnFitshome = new GridBagConstraints();
		gbc_btnFitshome.insets = new Insets(0, 0, 5, 5);
		gbc_btnFitshome.gridx = 5;
		gbc_btnFitshome.gridy = 2;
		toolsPanel.add(btnFitshome, gbc_btnFitshome);

		JLabel lblCheck = new JLabel("Check");
		GridBagConstraints gbc_lblCheck = new GridBagConstraints();
		gbc_lblCheck.anchor = GridBagConstraints.EAST;
		gbc_lblCheck.insets = new Insets(0, 0, 5, 5);
		gbc_lblCheck.gridx = 1;
		gbc_lblCheck.gridy = 3;
		toolsPanel.add(lblCheck, gbc_lblCheck);

		chckbxArchives = new JCheckBox("Archive checking");
		GridBagConstraints gbc_chckbxArchives = new GridBagConstraints();
		gbc_chckbxArchives.anchor = GridBagConstraints.WEST;
		gbc_chckbxArchives.insets = new Insets(0, 0, 5, 5);
		gbc_chckbxArchives.gridx = 3;
		gbc_chckbxArchives.gridy = 3;
		toolsPanel.add(chckbxArchives, gbc_chckbxArchives);

		chckbxRecursiveChecking = new JCheckBox("Recursive checking");
		GridBagConstraints gbc_chckbxRecursiveChecking = new GridBagConstraints();
		gbc_chckbxRecursiveChecking.anchor = GridBagConstraints.WEST;
		gbc_chckbxRecursiveChecking.insets = new Insets(0, 0, 5, 5);
		gbc_chckbxRecursiveChecking.gridx = 4;
		gbc_chckbxRecursiveChecking.gridy = 3;
		toolsPanel.add(chckbxRecursiveChecking, gbc_chckbxRecursiveChecking);
		
		chckbxWarnXfmt = new JCheckBox("Warn x-fmt");
		GridBagConstraints gbc_chckbxWarnXfmt = new GridBagConstraints();
		gbc_chckbxWarnXfmt.anchor = GridBagConstraints.WEST;
		gbc_chckbxWarnXfmt.insets = new Insets(0, 0, 5, 5);
		gbc_chckbxWarnXfmt.gridx = 5;
		gbc_chckbxWarnXfmt.gridy = 3;
		toolsPanel.add(chckbxWarnXfmt, gbc_chckbxWarnXfmt);

		chckbxShaDigest1 = new JCheckBox("SHA-1 Digest");
		GridBagConstraints gbc_chckbxShaDigest = new GridBagConstraints();
		gbc_chckbxShaDigest.anchor = GridBagConstraints.WEST;
		gbc_chckbxShaDigest.insets = new Insets(0, 0, 5, 5);
		gbc_chckbxShaDigest.gridx = 3;
		gbc_chckbxShaDigest.gridy = 5;
		toolsPanel.add(chckbxShaDigest1, gbc_chckbxShaDigest);

		chckbxShaDigest256 = new JCheckBox("SHA-256 Digest");
		GridBagConstraints gbc_chckbxShaDigest_1 = new GridBagConstraints();
		gbc_chckbxShaDigest_1.anchor = GridBagConstraints.WEST;
		gbc_chckbxShaDigest_1.insets = new Insets(0, 0, 5, 5);
		gbc_chckbxShaDigest_1.gridx = 4;
		gbc_chckbxShaDigest_1.gridy = 5;
		toolsPanel.add(chckbxShaDigest256, gbc_chckbxShaDigest_1);

		chckbxShaDigest512 = new JCheckBox("SHA-512 Digest");
		GridBagConstraints gbc_chckbxShaDigest_2 = new GridBagConstraints();
		gbc_chckbxShaDigest_2.insets = new Insets(0, 0, 5, 5);
		gbc_chckbxShaDigest_2.anchor = GridBagConstraints.WEST;
		gbc_chckbxShaDigest_2.gridx = 5;
		gbc_chckbxShaDigest_2.gridy = 5;
		toolsPanel.add(chckbxShaDigest512, gbc_chckbxShaDigest_2);
		
		JLabel lblLibreofficeopenoffice = new JLabel("LibreOffice/OpenOffice");
		GridBagConstraints gbc_lblLibreofficeopenoffice = new GridBagConstraints();
		gbc_lblLibreofficeopenoffice.anchor = GridBagConstraints.EAST;
		gbc_lblLibreofficeopenoffice.insets = new Insets(0, 0, 5, 5);
		gbc_lblLibreofficeopenoffice.gridx = 1;
		gbc_lblLibreofficeopenoffice.gridy = 6;
		toolsPanel.add(lblLibreofficeopenoffice, gbc_lblLibreofficeopenoffice);
		
		textLibreOffice = new JTextField();
		GridBagConstraints gbc_textLibreOffice1 = new GridBagConstraints();
		gbc_textLibreOffice1.gridwidth = 2;
		gbc_textLibreOffice1.insets = new Insets(0, 0, 5, 5);
		gbc_textLibreOffice1.fill = GridBagConstraints.HORIZONTAL;
		gbc_textLibreOffice1.gridx = 3;
		gbc_textLibreOffice1.gridy = 6;
		toolsPanel.add(textLibreOffice, gbc_textLibreOffice1);
		textLibreOffice.setColumns(10);
		
		JButton btnLibreoffice = new JButton();
		btnLibreoffice.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				File file = openFile(textLibreOffice.getText(), "LibreOffice/OpenOffice home directory", null);
				if (file != null) {
					textLibreOffice.setText(file.getAbsolutePath());
				}
			}
		});
		btnLibreoffice.setMargin(new Insets(2, 2, 2, 2));
		btnLibreoffice.setIcon(new ImageIcon(VitamConfigDialog.class
				.getResource(VitamGui.RESOURCES_IMG_CHECKFILES_PNG)));
		GridBagConstraints gbc_btnLibreoffice = new GridBagConstraints();
		gbc_btnLibreoffice.insets = new Insets(0, 0, 5, 5);
		gbc_btnLibreoffice.gridx = 5;
		gbc_btnLibreoffice.gridy = 6;
		toolsPanel.add(btnLibreoffice, gbc_btnLibreoffice);
		
		JLabel lblUnoconv = new JLabel("Unoconv");
		GridBagConstraints gbc_lblUnoconv = new GridBagConstraints();
		gbc_lblUnoconv.anchor = GridBagConstraints.EAST;
		gbc_lblUnoconv.insets = new Insets(0, 0, 5, 5);
		gbc_lblUnoconv.gridx = 1;
		gbc_lblUnoconv.gridy = 7;
		toolsPanel.add(lblUnoconv, gbc_lblUnoconv);
		
		textUnoconv = new JTextField();
		GridBagConstraints gbc_textUnoconv = new GridBagConstraints();
		gbc_textUnoconv.gridwidth = 2;
		gbc_textUnoconv.insets = new Insets(0, 0, 5, 5);
		gbc_textUnoconv.fill = GridBagConstraints.HORIZONTAL;
		gbc_textUnoconv.gridx = 3;
		gbc_textUnoconv.gridy = 7;
		toolsPanel.add(textUnoconv, gbc_textUnoconv);
		textUnoconv.setColumns(10);
		
		JButton btnUnoconv = new JButton();
		btnUnoconv.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				File file = openFile(textUnoconv.getText(), "Unoconv.py script", "py");
				if (file != null) {
					textUnoconv.setText(file.getAbsolutePath());
				}
			}
		});
		btnUnoconv.setMargin(new Insets(2, 2, 2, 2));
		btnUnoconv.setIcon(new ImageIcon(VitamConfigDialog.class
				.getResource(VitamGui.RESOURCES_IMG_CHECKFILES_PNG)));
		GridBagConstraints gbc_btnUnoconv = new GridBagConstraints();
		gbc_btnUnoconv.insets = new Insets(0, 0, 5, 5);
		gbc_btnUnoconv.gridx = 5;
		gbc_btnUnoconv.gridy = 7;
		toolsPanel.add(btnUnoconv, gbc_btnUnoconv);
		
		chckbxExtractKeywords = new JCheckBox(StaticValues.LBL.config_keywords.get());
		GridBagConstraints gbc_chckbxExtractKeywords = new GridBagConstraints();
		gbc_chckbxExtractKeywords.anchor = GridBagConstraints.EAST;
		gbc_chckbxExtractKeywords.insets = new Insets(0, 0, 5, 5);
		gbc_chckbxExtractKeywords.gridx = 5;
		gbc_chckbxExtractKeywords.gridy = 8;
		toolsPanel.add(chckbxExtractKeywords, gbc_chckbxExtractKeywords);
		
		JLabel lblMillisecondsPerKb = new JLabel(StaticValues.LBL.config_limitkb.get());
		GridBagConstraints gbc_lblMillisecondsPerKb = new GridBagConstraints();
		gbc_lblMillisecondsPerKb.anchor = GridBagConstraints.EAST;
		gbc_lblMillisecondsPerKb.insets = new Insets(0, 0, 5, 5);
		gbc_lblMillisecondsPerKb.gridx = 1;
		gbc_lblMillisecondsPerKb.gridy = 9;
		toolsPanel.add(lblMillisecondsPerKb, gbc_lblMillisecondsPerKb);
		
		msperkb = new JFormattedTextField(NumberFormat.getIntegerInstance());
		GridBagConstraints gbc_msperkb = new GridBagConstraints();
		gbc_msperkb.insets = new Insets(0, 0, 5, 5);
		gbc_msperkb.fill = GridBagConstraints.HORIZONTAL;
		gbc_msperkb.gridx = 3;
		gbc_msperkb.gridy = 9;
		toolsPanel.add(msperkb, gbc_msperkb);
		msperkb.setColumns(10);

		JLabel lblLowLimit = new JLabel(StaticValues.LBL.config_limitlow.get());
		GridBagConstraints gbc_lblLowLimit = new GridBagConstraints();
		gbc_lblLowLimit.anchor = GridBagConstraints.EAST;
		gbc_lblLowLimit.insets = new Insets(0, 0, 0, 5);
		gbc_lblLowLimit.gridx = 1;
		gbc_lblLowLimit.gridy = 10;
		toolsPanel.add(lblLowLimit, gbc_lblLowLimit);
		
		lowlimitms = new JFormattedTextField(NumberFormat.getIntegerInstance());
		GridBagConstraints gbc_lowlimit = new GridBagConstraints();
		gbc_lowlimit.insets = new Insets(0, 0, 0, 5);
		gbc_lowlimit.fill = GridBagConstraints.HORIZONTAL;
		gbc_lowlimit.gridx = 3;
		gbc_lowlimit.gridy = 10;
		toolsPanel.add(lowlimitms, gbc_lowlimit);
		lowlimitms.setColumns(10);
		
		JLabel lblLimitOccurence = new JLabel(StaticValues.LBL.config_keywordsrk.get());
		GridBagConstraints gbc_lblLimitOccurence = new GridBagConstraints();
		gbc_lblLimitOccurence.anchor = GridBagConstraints.EAST;
		gbc_lblLimitOccurence.insets = new Insets(0, 0, 5, 5);
		gbc_lblLimitOccurence.gridx = 4;
		gbc_lblLimitOccurence.gridy = 9;
		toolsPanel.add(lblLimitOccurence, gbc_lblLimitOccurence);

		ranklimit = new JFormattedTextField(NumberFormat.getIntegerInstance());
		GridBagConstraints gbc_limit = new GridBagConstraints();
		gbc_limit.insets = new Insets(0, 0, 5, 5);
		gbc_limit.fill = GridBagConstraints.HORIZONTAL;
		gbc_limit.gridx = 5;
		gbc_limit.gridy = 9;
		toolsPanel.add(ranklimit, gbc_limit);
		ranklimit.setColumns(10);

		JLabel lblLimitWord = new JLabel(StaticValues.LBL.config_keywordsnb.get());
		GridBagConstraints gbc_lblLimitWord = new GridBagConstraints();
		gbc_lblLimitWord.anchor = GridBagConstraints.EAST;
		gbc_lblLimitWord.insets = new Insets(0, 0, 0, 5);
		gbc_lblLimitWord.gridx = 4;
		gbc_lblLimitWord.gridy = 10;
		toolsPanel.add(lblLimitWord, gbc_lblLimitWord);

		wordlimit = new JFormattedTextField(NumberFormat.getIntegerInstance());
		GridBagConstraints gbc_wordlimit = new GridBagConstraints();
		gbc_wordlimit.insets = new Insets(0, 0, 0, 5);
		gbc_wordlimit.fill = GridBagConstraints.HORIZONTAL;
		gbc_wordlimit.gridx = 5;
		gbc_wordlimit.gridy = 10;
		toolsPanel.add(wordlimit, gbc_wordlimit);
		wordlimit.setColumns(10);
	}
	private void outputPanel(JTabbedPane tabbedPane) {
		JPanel outputPanel = new JPanel();
		tabbedPane.addTab("Output", null, outputPanel, null);
		GridBagLayout gbl_outputPanel = new GridBagLayout();
		gbl_outputPanel.columnWidths = new int[] { 0, 0, 0, 0 };
		gbl_outputPanel.rowHeights = new int[] { 0, 0, 0, 0, 0, 0, 0, 0 };
		gbl_outputPanel.columnWeights = new double[] { 0.0, 0.0, 1.0, Double.MIN_VALUE };
		gbl_outputPanel.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		outputPanel.setLayout(gbl_outputPanel);

		JLabel lblFormatOutput = new JLabel("Format Output");
		GridBagConstraints gbc_lblFormatOutput = new GridBagConstraints();
		gbc_lblFormatOutput.insets = new Insets(0, 0, 5, 5);
		gbc_lblFormatOutput.gridx = 1;
		gbc_lblFormatOutput.gridy = 0;
		outputPanel.add(lblFormatOutput, gbc_lblFormatOutput);

		rdbtnCsvOutput = new JRadioButton("TXT output");
		buttonGroup.add(rdbtnCsvOutput);
		GridBagConstraints gbc_rdbtnCsvOutput = new GridBagConstraints();
		gbc_rdbtnCsvOutput.anchor = GridBagConstraints.WEST;
		gbc_rdbtnCsvOutput.insets = new Insets(0, 0, 5, 0);
		gbc_rdbtnCsvOutput.gridx = 2;
		gbc_rdbtnCsvOutput.gridy = 1;
		outputPanel.add(rdbtnCsvOutput, gbc_rdbtnCsvOutput);

		rdbtnMultipleXmlOutput = new JRadioButton("Multiple XML output");
		buttonGroup.add(rdbtnMultipleXmlOutput);
		GridBagConstraints gbc_rdbtnMultipleXmlOutput = new GridBagConstraints();
		gbc_rdbtnMultipleXmlOutput.anchor = GridBagConstraints.WEST;
		gbc_rdbtnMultipleXmlOutput.insets = new Insets(0, 0, 5, 0);
		gbc_rdbtnMultipleXmlOutput.gridx = 2;
		gbc_rdbtnMultipleXmlOutput.gridy = 2;
		outputPanel.add(rdbtnMultipleXmlOutput, gbc_rdbtnMultipleXmlOutput);

		rdbtnSingleXmlOutput = new JRadioButton("Single XML output");
		buttonGroup.add(rdbtnSingleXmlOutput);
		GridBagConstraints gbc_rdbtnSingleXmlOutput = new GridBagConstraints();
		gbc_rdbtnSingleXmlOutput.anchor = GridBagConstraints.WEST;
		gbc_rdbtnSingleXmlOutput.insets = new Insets(0, 0, 5, 0);
		gbc_rdbtnSingleXmlOutput.gridx = 2;
		gbc_rdbtnSingleXmlOutput.gridy = 3;
		outputPanel.add(rdbtnSingleXmlOutput, gbc_rdbtnSingleXmlOutput);
		
		chckbxProposeFileSave = new JCheckBox("Propose File Save (XML only and best on Single XML)");
		GridBagConstraints gbc_chckbxProposeFileSave = new GridBagConstraints();
		gbc_chckbxProposeFileSave.insets = new Insets(0, 0, 5, 0);
		gbc_chckbxProposeFileSave.gridwidth = 2;
		gbc_chckbxProposeFileSave.gridx = 1;
		gbc_chckbxProposeFileSave.gridy = 4;
		outputPanel.add(chckbxProposeFileSave, gbc_chckbxProposeFileSave);
		
		chckbxExtractEmlBody = new JCheckBox("Extract Eml Body and Attachments");
		GridBagConstraints gbc_chckbxExtractEmlBody = new GridBagConstraints();
		gbc_chckbxExtractEmlBody.insets = new Insets(0, 0, 5, 5);
		gbc_chckbxExtractEmlBody.gridx = 1;
		gbc_chckbxExtractEmlBody.gridy = 5;
		outputPanel.add(chckbxExtractEmlBody, gbc_chckbxExtractEmlBody);
		
		outputDirField = new JTextField();
		GridBagConstraints gbc_outputDirField = new GridBagConstraints();
		gbc_outputDirField.insets = new Insets(0, 0, 5, 0);
		gbc_outputDirField.fill = GridBagConstraints.HORIZONTAL;
		gbc_outputDirField.gridx = 2;
		gbc_outputDirField.gridy = 5;
		outputPanel.add(outputDirField, gbc_outputDirField);
		outputDirField.setColumns(10);
		
		JButton btnSelectOutputDirectory = new JButton("Select output directory");
		btnSelectOutputDirectory.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				File file = openFile(outputDirField.getText(), "Select Output directory", null);
				if (file != null) {
					outputDirField.setText(file.getAbsolutePath());
				}
			}
		});
		GridBagConstraints gbc_btnSelectOutputDirectory = new GridBagConstraints();
		gbc_btnSelectOutputDirectory.gridx = 2;
		gbc_btnSelectOutputDirectory.gridy = 6;
		outputPanel.add(btnSelectOutputDirectory, gbc_btnSelectOutputDirectory);

		JLabel lblTitle = new JLabel("Vitam Configuration");
		lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
		add(lblTitle, BorderLayout.NORTH);
		initValue();
	}

	public void initValue() {
		xsdroot.setText(StaticValues.config.CURRENT_XSD_ROOT);
		namespace.setText(StaticValues.config.DEFAULT_LOCATION);
		docField.setText(StaticValues.config.DOCUMENT_FIELD);
		filefield.setText(StaticValues.config.ATTACHMENT_FIELD);
		fileattr.setText(StaticValues.config.FILENAME_ATTRIBUTE);
		digestfield.setText(StaticValues.config.INTEGRITY_FIELD);
		algoattr.setText(StaticValues.config.ALGORITHME_ATTRIBUTE);
		mimeattr.setText(StaticValues.config.MIMECODE_ATTRIBUTE);
		formatattr.setText(StaticValues.config.FORMAT_ATTRIBUTE);
		signature.setText(StaticValues.config.SIGNATURE_FILE);
		container.setText(StaticValues.config.CONTAINER_SIGNATURE_FILE);
		fitshome.setText(StaticValues.config.FITS_HOME);
		textLibreOffice.setText(StaticValues.config.LIBREOFFICE_HOME);
		textUnoconv.setText(StaticValues.config.UNOCONV);
		msperkb.setText(Long.toString(StaticValues.config.msPerKB));
		lowlimitms.setText(Long.toString(StaticValues.config.lowLimitMs));
		ranklimit.setText(Integer.toString(StaticValues.config.rankLimit));
		wordlimit.setText(Integer.toString(StaticValues.config.wordLimit));
		chckbxArchives.setSelected(StaticValues.config.argument.archive);
		chckbxRecursiveChecking.setSelected(StaticValues.config.argument.recursive);
		chckbxShaDigest1.setSelected(StaticValues.config.argument.sha1);
		chckbxShaDigest256.setSelected(StaticValues.config.argument.sha256);
		chckbxShaDigest512.setSelected(StaticValues.config.argument.sha512);
		chckbxExtractKeywords.setSelected(StaticValues.config.argument.extractKeyword);
		chckbxProposeFileSave.setSelected(StaticValues.config.guiProposeFileSaving);
		chckbxWarnXfmt.setSelected(StaticValues.config.preventXfmt);
		rdbtnCsvOutput
				.setSelected(StaticValues.config.argument.outputModel == VitamOutputModel.TXT);
		rdbtnMultipleXmlOutput
				.setSelected(StaticValues.config.argument.outputModel == VitamOutputModel.MultipleXML);
		rdbtnSingleXmlOutput
				.setSelected(StaticValues.config.argument.outputModel == VitamOutputModel.OneXML);
		chckbxExtractEmlBody.setSelected(StaticValues.config.extractFile);
		outputDirField.setText(StaticValues.config.outputDir);
	}

	public void saveConfig() {
		if (!StaticValues.config.CURRENT_XSD_ROOT.equals(xsdroot.getText())) {
			StaticValues.config.CURRENT_XSD_ROOT = xsdroot.getText();
		}
		if (!StaticValues.config.DEFAULT_LOCATION.equals(namespace.getText())) {
			StaticValues.config.DEFAULT_LOCATION = namespace.getText();
		}
		if (!StaticValues.config.DOCUMENT_FIELD.equals(docField.getText())) {
			StaticValues.config.DOCUMENT_FIELD = docField.getText();
		}
		if (!StaticValues.config.ATTACHMENT_FIELD.equals(filefield.getText())) {
			StaticValues.config.ATTACHMENT_FIELD = filefield.getText();
		}
		if (!StaticValues.config.FILENAME_ATTRIBUTE.equals(fileattr.getText())) {
			StaticValues.config.FILENAME_ATTRIBUTE = fileattr.getText();
		}
		if (!StaticValues.config.INTEGRITY_FIELD.equals(digestfield.getText())) {
			StaticValues.config.INTEGRITY_FIELD = digestfield.getText();
		}
		if (!StaticValues.config.ALGORITHME_ATTRIBUTE.equals(algoattr.getText())) {
			StaticValues.config.ALGORITHME_ATTRIBUTE = algoattr.getText();
		}
		if (!StaticValues.config.MIMECODE_ATTRIBUTE.equals(mimeattr.getText())) {
			StaticValues.config.MIMECODE_ATTRIBUTE = mimeattr.getText();
		}
		if (!StaticValues.config.FORMAT_ATTRIBUTE.equals(formatattr.getText())) {
			StaticValues.config.FORMAT_ATTRIBUTE = formatattr.getText();
		}
		if (!StaticValues.config.SIGNATURE_FILE.equals(signature.getText())) {
			StaticValues.config.SIGNATURE_FILE = signature.getText();
			StaticValues.config.droidHandler = null;
		}
		if (!StaticValues.config.CONTAINER_SIGNATURE_FILE.equals(container.getText())) {
			StaticValues.config.CONTAINER_SIGNATURE_FILE = container.getText();
			StaticValues.config.droidHandler = null;
		}
		if (!StaticValues.config.FITS_HOME.equals(fitshome.getText())) {
			StaticValues.config.FITS_HOME = fitshome.getText();
			StaticValues.config.fits = null;
			StaticValues.config.exif = null;
			StaticValues.config.jhove = null;
		}
		if (!StaticValues.config.LIBREOFFICE_HOME.equals(textLibreOffice.getText())) {
			StaticValues.config.LIBREOFFICE_HOME = textLibreOffice.getText();
		}
		if (!StaticValues.config.UNOCONV.equals(textUnoconv.getText())) {
			StaticValues.config.UNOCONV = textUnoconv.getText();
		}
		try {
			long value = Long.parseLong(msperkb.getText());
			if (value > 50) {
				StaticValues.config.msPerKB = value;
			}
		} catch (NumberFormatException e) {
		}
		try {
			long value = Long.parseLong(lowlimitms.getText());
			if (value > 10000) {
				StaticValues.config.lowLimitMs = value;
			}
		} catch (NumberFormatException e) {
		}
		try {
			int value = Integer.parseInt(ranklimit.getText());
			if (value >= 0) {
				StaticValues.config.rankLimit = value;
			}
		} catch (NumberFormatException e) {
		}
		try {
			int value = Integer.parseInt(wordlimit.getText());
			if (value >= 0) {
				StaticValues.config.wordLimit = value;
			}
		} catch (NumberFormatException e) {
		}
		StaticValues.config.argument.archive = chckbxArchives.isSelected();
		StaticValues.config.argument.recursive = chckbxRecursiveChecking.isSelected();
		StaticValues.config.argument.sha1 = chckbxShaDigest1.isSelected();
		StaticValues.config.argument.sha256 = chckbxShaDigest256.isSelected();
		StaticValues.config.argument.sha512 = chckbxShaDigest512.isSelected();
		StaticValues.config.guiProposeFileSaving = chckbxProposeFileSave.isSelected();
		StaticValues.config.preventXfmt = chckbxWarnXfmt.isSelected();
		StaticValues.config.argument.extractKeyword = chckbxExtractKeywords.isSelected();
		if (rdbtnCsvOutput.isSelected()) {
			StaticValues.config.argument.outputModel = VitamOutputModel.TXT;
		} else if (rdbtnMultipleXmlOutput.isSelected()) {
			StaticValues.config.argument.outputModel = VitamOutputModel.MultipleXML;
		} else if (rdbtnSingleXmlOutput.isSelected()) {
			StaticValues.config.argument.outputModel = VitamOutputModel.OneXML;
		}
		if (!StaticValues.config.outputDir.equals(outputDirField.getText())) {
			StaticValues.config.outputDir = outputDirField.getText();
		}
		StaticValues.config.extractFile = chckbxExtractEmlBody.isSelected();
		if (chckbxUpdateConfigurationFile.isSelected()) {
			StaticValues.config.saveConfig();
		}

		if (fromMain) {

		} else {
			this.vitamGui.setEnabled(true);
			this.vitamGui.requestFocus();
			this.frame.setVisible(false);
		}
	}

	public void cancel() {
		if (fromMain) {
			this.frame.dispose();
			System.exit(0);
		} else {
			this.vitamGui.setEnabled(true);
			this.vitamGui.requestFocus();
			this.frame.setVisible(false);
		}
	}

	public File openFile(String currentValue, String text, String extension) {
		JFileChooser chooser = null;
		if (currentValue != null) {
			String file = StaticValues.resourceToFile(currentValue);
			if (file != null) {
				File ffile = new File(file).getParentFile();
				chooser = new JFileChooser(ffile);
			}
		}
		if (chooser == null) {
			chooser = new JFileChooser(System.getProperty("user.dir"));
		}
		if (extension == null) {
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		} else {
			FileExtensionFilter filter = new FileExtensionFilter(extension, text);
			chooser.setFileFilter(filter);
		}
		chooser.setDialogTitle(text);
		if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			return chooser.getSelectedFile();
		}
		return null;
	}

	/**
	 * @param arg0
	 */
	public void showHelp(ActionEvent arg0) {
		JPanel panel = new JPanel(new BorderLayout());
		String help = "<html><p><center>Logique des chemins</center></p>"
				+
				"<p><table><tr><td>XPATH //DOCUMENT_FIELD</td><td>ex: Document</td><td>=> //Document</td></tr>"
				+
				"<tr><td>XPATH //DOCUMENT_FIELD/ATTACHMENT_FIELD</td><td>ex: Attachment</td><td>=> //Document/Attachment</td></tr>"
				+
				"<tr><td>XPATH //DOCUMENT_FIELD/ATTACHMENT_FIELD/FILENAME_ATTRIBUTE</td><td>ex: @filename</td><td>=> //Document/Attachment/@filename</td></tr>"
				+
				"<tr><td>XPATH //DOCUMENT_FIELD/ATTACHMENT_FIELD/MIMECODE_ATTRIBUTE</td><td>ex: @mimeCode</td><td>=> //Document/Attachment/@mimeCode</td></tr>"
				+
				"<tr><td>XPATH //DOCUMENT_FIELD/ATTACHMENT_FIELD/FORMAT_ATTRIBUTE</td><td>ex: @format</td><td>=> //Document/Attachment/@format</td></tr>"
				+
				"<tr><td>XPATH //DOCUMENT_FIELD/INTEGRITY_FIELD</td><td>ex: Integrity</td><td>=> //Document/Integrity</td></tr>"
				+
				"<tr><td>XPATH //DOCUMENT_FIELD/INTEGRITY_FIELD/ALGORITHME_ATTRIBUTE</td><td>ex: @algorithme</td><td>=> //Document/Integrity/@algorithme</td></tr>"
				+
				"</table></p></html>";
		panel.add(new JLabel(help), BorderLayout.CENTER);
		JOptionPane.showConfirmDialog(((JButton) arg0.getSource()).getTopLevelAncestor(), panel,
				"Help",
				JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE);
	}

	/**
	 * Create the GUI and show it. For thread safety, this method should be invoked from the
	 * event-dispatching thread.
	 */
	private static void createAndShowGUI() {
		// Create and set up the window.
		JFrame frame = new JFrame("Vitam Configuration");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		// Create and set up the content pane.
		fromMain = true;
		VitamConfigDialog newContentPane = new VitamConfigDialog(frame, null);
		newContentPane.setOpaque(true); // content panes must be opaque
		frame.setContentPane(newContentPane);

		// Display the window.
		frame.pack();
		frame.setVisible(true);
	}

	public static void main(String[] args) {
		StaticValues.initialize();
		// Schedule a job for the event-dispatching thread:
		// creating and showing this application's GUI.
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createAndShowGUI();
			}
		});
	}

}
