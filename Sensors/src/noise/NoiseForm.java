/**
 *
 * SensorSubmission - NoiseForm.java
 * @date 31 Jul 2015 - 2015
 * @author NicolasCardozo
 */
package noise;

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
public class NoiseForm extends JPanel implements ActionListener {
	
	/**
	 * NoiseForm.java:long. Represents 
	 */
	private static final long serialVersionUID = 1L;
	private static final String SENSOR = "noise";
	
	private RedisDataConnection db;
	
	private JTextField streetName;
	private JTextField noiseLevel;
	private JButton submit;
	private JButton clear;
	
	public NoiseForm(RedisDataConnection db) {
		this.db = db;
		
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
		
		this.add(new JLabel(" Noise Level:"));
		noiseLevel = new JTextField();
		noiseLevel.setToolTipText("Noise value in DB. A Number between 40 and 100");
		this.add(noiseLevel);
		
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
			if(!noiseLevel.getText().trim().matches("^-?\\d+$")) {
				JOptionPane.showMessageDialog(null, "Only numbers can be used in the Noise level field");
			} else {
				String dbs = noiseLevel.getText().trim();
				if(Double.parseDouble(dbs) < 40 || Double.parseDouble(dbs) > 100.0) {
					JOptionPane.showInternalMessageDialog(null, "Noise value in DB. A Number between 40 and 100");
					noiseLevel.setText("");
				}
				String street = streetName.getText().trim();
				System.out.println(" Submitting " + SENSOR+"_"+street + " - " + dbs);
				db.insertInDB(SENSOR+"_"+street, dbs);
			}
		} else if(actionCommand.equalsIgnoreCase(Constants.CLEAR_PRESSED)) {
			System.out.println("Clearing");
			streetName.setText("");
			noiseLevel.setText("");
		}
	}
}
