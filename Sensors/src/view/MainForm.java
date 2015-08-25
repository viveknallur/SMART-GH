/**
 *
 * SensorSubmission - MainForm.java
 * @date 17 Aug 2015 - 2015
 * @author NicolasCardozo
 */
package view;

import general.Constants;
import general.RedisDataConnection;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.Collection;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.text.MaskFormatter;

import libellium.DewpointForm;
import libellium.HumidityForm;
import libellium.PressureForm;
import libellium.RainfalRateForm;
import libellium.TemperatureForm;
import noise.NoiseForm;

import org.geonames.Toponym;
import org.geonames.ToponymSearchCriteria;
import org.geonames.ToponymSearchResult;
import org.geonames.WebService;
import org.ini4j.Ini;
import org.ini4j.InvalidIniFormatException;

import accelerometer.AccelerometerForm;
import air.CO2Form;
import air.NH2Form;
import air.PollutantForm;

/**
 * 
 */
public class MainForm extends JFrame implements ActionListener {
	/**
	 * MainForm.java:long. Represents 
	 */
	private static final long serialVersionUID = -5847944744894218969L;

	private JComboBox<String> sensors = new JComboBox<String>();
	private JFormattedTextField ip;
	private JTextField port;
	private JPanel setupPanel;
	private JPanel sensorPanel;
	private JButton submitData;
	
	private RedisDataConnection db;
	
	public MainForm(String title) {
		String cityFile = this.loadCityFile();
		this.loadSensorsForCity(cityFile);
		
		this.setTitle(title);
		this.setSize(Constants.FORM_WIDTH, Constants.FORM_HEIGHT);
		this.setLayout(new GridLayout(1, 2, Constants.HGAP, Constants.VGAP));
		
		setupPanel = new JPanel(new GridLayout(4, 2, Constants.HGAP, Constants.VGAP));
		
		sensorPanel = new JPanel();
		
		submitData = new JButton("Data connection");
		
		submitData.addActionListener(this);
		submitData.setActionCommand(Constants.DATA_CONNECTION);
		
		setupPanel.add(new JLabel(" Database address"));
		setupPanel.add(new JLabel("Port Number"));
		
		MaskFormatter formatter;
		try {
			formatter = new MaskFormatter("###.###.###.###");
			ip = new JFormattedTextField(formatter);
			setupPanel.add(ip);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		port = new JTextField("6379");
		setupPanel.add(port);
		
		setupPanel.add(new JLabel());
		setupPanel.add(submitData);
		
		setupPanel.add(new JLabel(" Available Sensors"));
		sensors.addActionListener(this);
		sensors.setActionCommand(Constants.SENSOR_SELECTED);
		setupPanel.add(sensors);
		
		this.add(setupPanel);
		this.add(sensorPanel);
		
		this.setVisible(true);
	}
	
	
	/**
	 * Loads all sensors defined in a city
	 * @param cityFile
	 */
	private void loadSensorsForCity(String city) {
		try {
			Ini cityStream = new Ini(new FileReader(city+".config"));
			Collection<String> allSensors = cityStream.get("SensorsAvailable").values();
			for(String sensor : allSensors) {
				sensors.addItem(sensor);
			}
		} catch (InvalidIniFormatException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Loads the config file for each city
	 * @return city, Name of the city to load
	 */
	public String loadCityFile() {
		Properties prop = new Properties();
		InputStream inputStream = getClass().getClassLoader().getResourceAsStream(Constants.CONF_FILE);
		
		if(inputStream != null) {
			try {
				prop.load(inputStream);
				int numCities = Integer.parseInt(prop.getProperty("numCities"));
				for(int i=0; i<numCities; i++) {
					Constants.CITY = prop.getProperty("city") +  "_";
					return prop.getProperty("city");
				}
			} catch (IOException e) {
				JOptionPane.showMessageDialog(null, "Error reading properties file", "Error", JOptionPane.ERROR_MESSAGE);
				System.out.println(e.getMessage());
			}
		} else {
			JOptionPane.showMessageDialog(null, "Properties file " +  Constants.CONF_FILE + " not found", "Error", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
		return "";
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();
		if(command.equalsIgnoreCase(Constants.DATA_CONNECTION)) {
			db = new RedisDataConnection(ip.getText(), Integer.parseInt(port.getText()));
		}else if(command.equalsIgnoreCase(Constants.SENSOR_SELECTED)) {
			String sensor = (String)sensors.getSelectedItem();
			System.out.println(sensor);
			this.getContentPane().remove(sensorPanel);
			sensorPanel = null;
			
			if(sensor.equals("Accelerometer")) {
				sensorPanel = new AccelerometerForm(db);
			} else if(sensor.equals("Noisetube")) {
				sensorPanel = new NoiseForm(db);
			} else if(sensor.equals("Temperature")) {
				sensorPanel = new TemperatureForm(db);
			} else if(sensor.equals("Dewpoint")) {
				sensorPanel = new DewpointForm(db);
			} else if(sensor.equals("DublinCityCouncilAirPollution")) {
				sensorPanel = new PollutantForm(db);
			} else if(sensor.equals("Humidity")) {
				sensorPanel = new HumidityForm(db);
			} else if(sensor.equals("Pressure")) {
				sensorPanel = new PressureForm(db);
			} else if(sensor.equals("Rainfall")) {
				sensorPanel = new RainfalRateForm(db);
			} else if(sensor.equals("CO2")) {
				sensorPanel = new CO2Form(db);
			} else if(sensor.equals("NH2")) {
				sensorPanel = new NH2Form(db);
			} else if(sensor.equals("Pollutant")) {
				sensorPanel = new PollutantForm(db);
			} 
			else {
				sensorPanel = new JPanel();
			}
			
			this.getContentPane().add(sensorPanel);
			this.revalidate();
			this.repaint();
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		MainForm form = new MainForm("Sensor data submission platform");
		form.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
}
