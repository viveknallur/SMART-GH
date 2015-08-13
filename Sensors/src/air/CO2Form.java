/**
 *
 * SensorSubmission - CO2.java
 * @date 31 Jul 2015 - 2015
 * @author NicolasCardozo
 */
package air;

import general.Constants;
import general.RedisDataConnection;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

/**
 * 
 */
public class CO2Form extends JFrame implements ActionListener {
	
	/**
	 * NoiseForm.java:long. Represents 
	 */
	private static final long serialVersionUID = 1L;
	private static final String SENSOR = "co2";
	
	private RedisDataConnection db;
	
	private JTextField streetName;
	private JTextField sensorLevel;
	private JButton submit;
	private JButton clear;
	
	public CO2Form(String title) {
		this.setTitle(title);
		this.setSize(Constants.FORM_WIDTH, Constants.FORM_HEIGHT);
		this.setLayout(new GridLayout(3, 2, Constants.HGAP, Constants.VGAP));
		
		clear = new JButton("Clear");
		submit = new JButton("Submit");
		
		submit.addActionListener(this);
		submit.setActionCommand(Constants.SUBMIT_PRESSED);
		
		clear.addActionListener(this);
		clear.setActionCommand(Constants.CLEAR_PRESSED);
		
		this.add(new JLabel(" Bar Name:"));
		streetName = new JTextField();
		this.add(streetName);
		
		this.add(new JLabel(" CO2 meassurement:"));
		sensorLevel = new JTextField();
		sensorLevel.setToolTipText("CO2 is between 0 and 50000 ppm");
		this.add(sensorLevel);
		
		this.add(clear);
		this.add(submit);

			
		db = new RedisDataConnection();
		
		this.setVisible(true);
	}
	
	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent event) {
		String actionCommand = event.getActionCommand();
		if(actionCommand.equalsIgnoreCase(Constants.SUBMIT_PRESSED)) {
			if(!sensorLevel.getText().trim().matches("^-?\\d+$")) {
				JOptionPane.showMessageDialog(null, "Only numbers can be used in the CO2 field");
			} else {
				String street = streetName.getText().trim();
				String meassurement = sensorLevel.getText().trim();
				if(Double.parseDouble(meassurement) < 0 || Double.parseDouble(meassurement) > 50000) {
					JOptionPane.showMessageDialog(null, "CO2 must be between 0 and 50000 ppm");
					sensorLevel.setText("");
				} else {
					System.out.println(" Submitting " + SENSOR+"_"+street + " - " + meassurement);
					db.insertInDB(SENSOR+"_"+street, meassurement);
				}
			}
		} else if(actionCommand.equalsIgnoreCase(Constants.CLEAR_PRESSED)) {
			System.out.println("Clearing");
			streetName.setText("");
			sensorLevel.setText("");
		}
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		CO2Form form = new CO2Form("CO2 Data Submission Form");
		form.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
}