/**
 *
 * SensorSubmission - NoiseForm.java
 * @date 31 Jul 2015 - 2015
 * @author NicolasCardozo
 */
package accelerometer;

import general.Constants;
import general.RedisDataConnection;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * 
 */
public class AccelerometerForm extends JPanel implements ActionListener {
	
	/**
	 * NoiseForm.java:long. Represents 
	 */
	private static final long serialVersionUID = 1L;
	private static final String SENSOR = "accelerometer";
	
	private RedisDataConnection db;
	
	private JTextField streetName;
	private JTextField sensorLevel;
	private JButton submit;
	private JButton clear;
	
	public AccelerometerForm(RedisDataConnection db) {
		this.db = db;
		
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
		
		this.add(new JLabel(" Movement:"));
		sensorLevel = new JTextField();
		sensorLevel.setToolTipText("acceleration is a number greater than 0");
		this.add(sensorLevel);
		
		this.add(clear);
		this.add(submit);
		
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
				JOptionPane.showMessageDialog(null, "Only numbers can be used in the accelerometer field");
			} else {
				String street = streetName.getText().trim();
				String dbs = sensorLevel.getText().trim();
				System.out.println(" Submitting " + SENSOR+"_"+street + " - " + dbs);
				db.insertInDB(SENSOR+"_"+street, dbs);
			}
		} else if(actionCommand.equalsIgnoreCase(Constants.CLEAR_PRESSED)) {
			System.out.println("Clearing");
			streetName.setText("");
			sensorLevel.setText("");
		}
	}
	
	
}
