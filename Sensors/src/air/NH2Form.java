/**
 *
 * SensorSubmission - NH2.java
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
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * 
 */
public class NH2Form extends JPanel implements ActionListener {
	
	/**
	 * NoiseForm.java:long. Represents 
	 */
	private static final long serialVersionUID = 1L;
	private static final String SENSOR = "nh2";
	
	private RedisDataConnection db;
	
	private JTextField streetName;
	private JTextField sensorLevel;
	private JButton submit;
	private JButton clear;
	
	public NH2Form(RedisDataConnection db2) {
		this.db = db2;
		
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
		
		this.add(new JLabel(" NH2 meassurement:"));
		sensorLevel = new JTextField();
		sensorLevel.setToolTipText("NH2 must be between 0 and 20 ppm");
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
				JOptionPane.showMessageDialog(null, "Only numbers can be used in the NH2 field");
			} else {
				String street = streetName.getText().trim();
				String meassurement = sensorLevel.getText().trim();
				if(Double.parseDouble(meassurement) < 0 || Double.parseDouble(meassurement) > 20) {
					JOptionPane.showMessageDialog(null, "NH2 must be between 0 and 20 ppm");
					sensorLevel.setText("");
				} else {
					System.out.println(" Submitting " + SENSOR+"_"+street + " - " + meassurement);
					db.insertInDB(SENSOR+"_"+street, "0." + meassurement);
				}
			}
		} else if(actionCommand.equalsIgnoreCase(Constants.CLEAR_PRESSED)) {
			System.out.println("Clearing");
			streetName.setText("");
			sensorLevel.setText("");
		}
	}

}
