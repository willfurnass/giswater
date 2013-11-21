/*
 * This file is part of Giswater
 * Copyright (C) 2013 Tecnics Associats
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 * 
 * Author:
 *   David Erill <daviderill79@gmail.com>
 */
package org.giswater.gui.dialog;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Vector;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import org.giswater.controller.RaingageController;
import org.giswater.util.Utils;

import net.miginfocom.swing.MigLayout;


@SuppressWarnings("rawtypes")
public class RaingageDialog extends JDialog implements ActionListener{

	private static final long serialVersionUID = -6349825417550216902L;
	private RaingageController controller;
	public HashMap<String, JComboBox> comboMap;
	public HashMap<String, JTextField> textMap;
	private JTextField textField_18;
	private JTextField textField_1;
	private JTextField textField_2;
	private JTextField textField_3;
	private JTextField textField_4;
	private JButton btnPrevious;
	private JButton btnNext;
	private JButton btnSave;
	
	
	public RaingageDialog() {
		initConfig();
		createComponentMap();
	}
	
	
	public void setControl(RaingageController controller) {
		this.controller = controller;
	}		

	
	public void setTextField(JTextField textField, Object value) {
		if (value!=null){
			textField.setText(value.toString());
		}
	}	
	
	
	public void setComboModel(JComboBox<String> combo, Vector<String> items) {
		if (items != null){
			ComboBoxModel<String> cbm = new DefaultComboBoxModel<String>(items);
			combo.setModel(cbm);
		}
	}	
	
	
	public void setComboSelectedItem(JComboBox combo, String item){
		combo.setSelectedItem(item);
	}
	
	
	private void createComponentMap() {
		
        comboMap = new HashMap<String, JComboBox>();
        textMap = new HashMap<String, JTextField>();
        Component[] components = getContentPane().getComponents();

        for (int j=0; j<components.length; j++) {
        	if (components[j] instanceof JPanel){
        		JPanel panel = (JPanel) components[j];            
	            Component[] comp = panel.getComponents();        
	            for (int i=0; i < comp.length; i++) {        
	            	if (comp[i] instanceof JComboBox) {         	
	            		comboMap.put(comp[i].getName(), (JComboBox) comp[i]);
	            	}
	            	else if (comp[i] instanceof JTextField) {      
	            		textMap.put(comp[i].getName(), (JTextField) comp[i]);
	            	}
	            }
        	}
        }

	}


	private void initConfig(){

		setTitle("Raingage Table");
		setBounds(100, 100, 633, 339);
		getContentPane().setLayout(new MigLayout("", "[90.00,grow][200px]", "[80.00][60.00px][80.00][10px][36.00]"));
		
		JPanel panelGeneral = new JPanel();
		panelGeneral.setFont(new Font("Tahoma", Font.BOLD, 14));
		panelGeneral.setBorder(new TitledBorder(new LineBorder(new Color(0, 0, 0)), "GENERAL", TitledBorder.CENTER, TitledBorder.TOP, null, null));
		getContentPane().add(panelGeneral, "cell 0 0 2 1,grow");
		panelGeneral.setLayout(new MigLayout("", "[75.00][200.00][10px][80px][200px,grow]", "[][][][][][]"));

		JLabel lblFlowUnits = new JLabel("Form type:");
		panelGeneral.add(lblFlowUnits, "cell 0 0,alignx trailing");

		JComboBox flow_units = new JComboBox();
		panelGeneral.add(flow_units, "cell 1 0,growx");
		flow_units.setName("form_type");
		
		JLabel lblNewLabel_1 = new JLabel("Intvl:");
		panelGeneral.add(lblNewLabel_1, "cell 3 0,alignx trailing");
		
		textField_1 = new JTextField();
		textField_1.setName("intvl");
		textField_1.setColumns(10);
		panelGeneral.add(textField_1, "cell 4 0,growx");

		JLabel lblInfiltration = new JLabel("Scf:");
		panelGeneral.add(lblInfiltration, "cell 0 1,alignx trailing");
		
		textField_2 = new JTextField();
		textField_2.setName("scf");
		textField_2.setColumns(10);
		panelGeneral.add(textField_2, "cell 1 1,growx");
		
		JLabel lblIgnoreSnowmelt = new JLabel("Raingage Type:");
		panelGeneral.add(lblIgnoreSnowmelt, "cell 3 1,alignx trailing");
		
		JComboBox comboBox_3 = new JComboBox();
		panelGeneral.add(comboBox_3, "cell 4 1,growx");
		comboBox_3.setName("rgage_type");
		
		ImageIcon image = new ImageIcon("images/imago.png");        
		super.setIconImage(image.getImage());		
		
		btnPrevious = new JButton("<");
		btnPrevious.setActionCommand("movePrevious");
		getContentPane().add(btnPrevious, "flowx,cell 1 4");
		
		btnNext = new JButton(">");
		btnNext.setActionCommand("moveNext");
		getContentPane().add(btnNext, "cell 1 4");
		
		btnSave = new JButton(Utils.getBundleString("save"));
		btnSave.setActionCommand("saveData");
		getContentPane().add(btnSave, "cell 1 4,alignx right");
		
		JPanel panelTimeseries = new JPanel();
		panelTimeseries.setFont(new Font("Tahoma", Font.BOLD, 14));
		panelTimeseries.setBorder(new TitledBorder(new LineBorder(new Color(0, 0, 0)), "TIMESERIES", TitledBorder.CENTER, TitledBorder.TOP, null, null));
		getContentPane().add(panelTimeseries, "cell 0 1 2 1,grow");
		panelTimeseries.setLayout(new MigLayout("", "[75px][200px][10.00][80][200]", "[]"));
				
		JLabel lblFlowRouting = new JLabel("Timeseries id:");
		panelTimeseries.add(lblFlowRouting, "cell 0 0,alignx right");
								
		JComboBox flow_routing = new JComboBox();
		panelTimeseries.add(flow_routing, "cell 1 0,growx");
		flow_routing.setName("timser_id");
		
		JPanel panelFile = new JPanel();
		panelFile.setFont(new Font("Tahoma", Font.BOLD, 14));
		panelFile.setBorder(new TitledBorder(new LineBorder(new Color(0, 0, 0)), "FILE", TitledBorder.CENTER, TitledBorder.TOP, null, null));
		getContentPane().add(panelFile, "cell 0 2 2 1,grow");
		panelFile.setLayout(new MigLayout("", "[75px][200px][10px][80px][200px,grow]", "[][]"));
		
		JLabel lblIgnoreGroundwater = new JLabel("Fname:");
		panelFile.add(lblIgnoreGroundwater, "cell 0 0,alignx right");
		
		textField_3 = new JTextField();
		panelFile.add(textField_3, "cell 1 0,growx");
		textField_3.setName("fname");
		textField_3.setColumns(10);
		
		JLabel lblMinSlope = new JLabel("Sta:");
		panelFile.add(lblMinSlope, "cell 3 0,alignx right");
		lblMinSlope.setName("");
		
		textField_18 = new JTextField();
		panelFile.add(textField_18, "cell 4 0,growx");
		textField_18.setName("sta");
		textField_18.setColumns(10);
		
		JLabel lblIgnoreRouting = new JLabel("Units:");
		panelFile.add(lblIgnoreRouting, "cell 0 1,alignx right");
		
		textField_4 = new JTextField();
		panelFile.add(textField_4, "cell 1 1,growx");
		textField_4.setName("units");
		textField_4.setColumns(10);
		
		setupListeners();
		
	}

	
	// Setup component's listener
	private void setupListeners() {
		btnSave.addActionListener(this);	
		btnPrevious.addActionListener(this);
		btnNext.addActionListener(this);
	}
	

	@Override
	public void actionPerformed(ActionEvent e) {
		controller.action(e.getActionCommand());
	}
	

}