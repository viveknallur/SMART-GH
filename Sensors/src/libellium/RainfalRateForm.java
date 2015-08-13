/**
 *
 * SensorSubmission - RainfallRateForm.java
 * @date 31 Jul 2015 - 2015
 * @author NicolasCardozo
 */
package libellium;

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
public class RainfalRateForm extends JFrame implements ActionListener {
	
	/**
	 * TemperatureForm.java:long. Represents 
	 */
	private static final long serialVersionUID = 1L;
	private static final String SENSOR = "RainfallRate";
	
	private RedisDataConnection db;
	
	private JTextField streetName;
	private JTextField sensorLevel;
	private JButton submit;
	private JButton clear;
	
	public RainfalRateForm(String title) {
		this.setTitle(title);
		this.setSize(Constants.FORM_WIDTH, Constants.FORM_HEIGHT);
		this.setLayout(new GridLayout(3, 2, Constants.HGAP, Constants.VGAP));
		
		clear = new JButton("Clear");
		submit = new JButton("Submit");
		
		submit.addActionListener(this);
		submit.setActionCommand(Constants.SUBMIT_PRESSED);
		
		clear.addActionListener(this);
		clear.setActionCommand(Constants.CLEAR_PRESSED);
		
		this.add(new JLabel(" Street Name:"));
		streetName = new JTextField();
		this.add(streetName);
		
		this.add(new JLabel(" Temperature meassurement:"));
		sensorLevel = new JTextField();
		sensorLevel.setToolTipText("positive number");
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
				JOptionPane.showMessageDialog(null, "Only numbers can be used in the RainfallRate field");
			} else {
				String dbs = sensorLevel.getText().trim();
				if(Double.parseDouble(dbs) < 0.0 ) {
					JOptionPane.showMessageDialog(null, "RainfallRate must be a positive number");
					sensorLevel.setText("");
				} else {
					String street = streetName.getText().trim();
					System.out.println(" Submitting " + SENSOR+"_"+street + " - " + dbs);
					db.insertInDB(SENSOR+"_"+street, dbs);
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
		RainfalRateForm form = new RainfalRateForm("RainFall Data Submission Form");
		form.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
}