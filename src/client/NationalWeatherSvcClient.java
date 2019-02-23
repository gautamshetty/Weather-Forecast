package client;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.rmi.RemoteException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.axis2.AxisFault;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import gov.weather.graphical.xml.dwmlgen.wsdl.ndfdxml_wsdl.NdfdXMLStub;

/**
 * NationalWeatherSvcClient is a client for the National Weather Service web service defined by wsdl 
 * file located at https://graphical.weather.gov/xml/DWMLgen/wsdl/ndfdXML.wsdl
 * Includes a swing client to get the latitude and longitude information from the user and gets the weather information
 * for that latitude, longitude location and displays it to the user.   
 * @author Gautam Shetty
 * UTA ID : 1001446742
 */
public class NationalWeatherSvcClient implements ActionListener {
	
	/**
	 * Main function.
	 * @param args command parameters.
	 */
	public static void main(String[] args) {
		
		NationalWeatherSvcClient client = new NationalWeatherSvcClient();
		
		//Creates and launches the GUI.
		client.createGUI();
	}
	
	/**
	 * Sends a SOAP request to the web service on website http://w1.weather.gov and displays 
	 * the current weather conditions for the latitude and longitude location on the GUI.
	 * Stubs are generated for Web Service defined by the wsdl file at https://graphical.weather.gov/xml/DWMLgen/wsdl/ndfdXML.wsdl
	 * @param latitude latitude of the location
	 * @param longitude longitude of the location
	 * @return current weather conditions for the latitude and longitude location.
	 */
	private String getWeatherConditions(BigDecimal latitude, BigDecimal longitude) {
		
		String ndfdXmlResponse = new String();
		
		try {
			
			NdfdXMLStub stub = new NdfdXMLStub();
			
			stub._getServiceClient().getOptions().setProperty(org.apache.axis2.transport
																.http.HTTPConstants.CHUNKED, Boolean.FALSE);
			
			//Use the stub generated for the web services, set the latitude, longitude values.
			NdfdXMLStub.NDFDgen request = new NdfdXMLStub.NDFDgen();
			request.setLatitude(latitude);
			request.setLongitude(longitude);
			request.setProduct(NdfdXMLStub.ProductType.value1);
			
			//call the web service function and get the xml response.
			NdfdXMLStub.NDFDgenResponse response =  stub.nDFDgen(request);
			ndfdXmlResponse = response.getDwmlOut();
			
		} catch (AxisFault af) {
			af.printStackTrace();
		} catch (RemoteException re) {
			re.printStackTrace();
		}
		
		System.out.println(ndfdXmlResponse);
		
		return parseXMLResponse(ndfdXmlResponse);
	}
	
	/**
	 * Parse the xml response from the weather service.
	 * @param xmlResponse xml response
	 * @return values of the parameters.
	 */
	private String parseXMLResponse(String xmlResponse) {
		
		StringBuffer weatherConditions = new StringBuffer("Weather Conditions : \n\n");
		
		try {
			
		    Document document = DocumentBuilderFactory.newInstance()
		    											 .newDocumentBuilder()
		    											 .parse(new ByteArrayInputStream(xmlResponse.getBytes()));
		    
		    XPath xPath =  XPathFactory.newInstance().newXPath();
		    
		    DateFormat dtf = new SimpleDateFormat("yyyy-MM-dd");
		    String currentDate = dtf.format(new Date());
		    
		    String expression = "/dwml/data/time-layout[@time-coordinate='local']/start-valid-time";
			NodeList nodeList = (NodeList) xPath.compile(expression).evaluate(document, XPathConstants.NODESET);
			int number = 0;
			for (int i = 0; i < 7; i++) {
				if (currentDate.equals(nodeList.item(i).getFirstChild().getNodeValue().substring(0, 10))) {
					number = i;
					break;
				}
			}
		    
			//Parse the xml file using XPath and get the values of various parameters.
		    weatherConditions.append("Maximum Temperature = ")
		    					.append(getParameterValue(document, xPath, 
		    							"/dwml/data/parameters/temperature[@type='maximum']/value", number))
		    					.append("\n");
			
		    weatherConditions.append("Minimum Temperature = ")
							.append(getParameterValue(document, xPath, 
									"/dwml/data/parameters/temperature[@type='minimum']/value", number))
							.append("\n");
		    
		    weatherConditions.append("Dew Point Temperature = ")
							.append(getParameterValue(document, xPath, 
									"/dwml/data/parameters/temperature[@type='dew point']/value", number))
							.append("\n");
		    
		    weatherConditions.append("12 Hourly Probability of Precipitation = ")
							.append(getParameterValue(document, xPath, 
									"/dwml/data/parameters/probability-of-precipitation[@type='12 hour']/value", number))
							.append("\n");
		    
		    weatherConditions.append("Cloud Cover Amount = ")
							.append(getParameterValue(document, xPath, 
									"/dwml/data/parameters/cloud-amount[@type='total']/value", number))
							.append("\n");
		    
		    weatherConditions.append("Wind Speed = ")
							.append(getParameterValue(document, xPath, 
									"/dwml/data/parameters/wind-speed[@type='sustained']/value", number))
							.append("\n");
		    
		    weatherConditions.append("Wind Direction = ")
							.append(getParameterValue(document, xPath, 
									"/dwml/data/parameters/direction[@type='wind']/value", number))
							.append("\n");
		    
		    weatherConditions.append("Wave Height = ")
							.append(getParameterValue(document, xPath, 
									"/dwml/data/parameters/water-state/waves[@type='significant']/value", number))
							.append("\n");
		    	
		} catch (Exception e) {
		    e.printStackTrace();
		}
		
		return weatherConditions.toString();
	}
	
	/**
	 * Gets the value of the parameter represented by the expression. 
	 * @param document xml document
	 * @param xPath XPath object
	 * @param expression expression for the parameter whose value is to be retrieved.
	 * @param number the index of the data.
	 * @return the value for the parameter expression.
	 * @throws XPathExpressionException
	 */
	private String getParameterValue(Document document, XPath xPath, String expression, int number) 
																		throws XPathExpressionException {
		
		String parameterValue = "";
		NodeList nodeList = (NodeList) xPath.compile(expression).evaluate(document, XPathConstants.NODESET);
		if (nodeList != null && nodeList.item(number) != null && nodeList.item(number).getFirstChild() != null)
			parameterValue = nodeList.item(number).getFirstChild().getNodeValue();
		
		return parameterValue;
	}
	
	/**
	 * Text field to input the latitude.
	 */
	private JTextField latitudeTextField = new JTextField(10);
	
	/**
	 * Text field to input the longitude.
	 */
	private JTextField longitudeTextField = new JTextField(10);
	
	/**
	 * Text Area to display the weather conditions.
	 */
	private JTextArea weatherCondTextArea = new JTextArea(25, 28);
	
	/**
	 * Creates the client GUI which takes input of latitude and longitude of the location from the user and 
	 * displays the current weather conditions for the location in the text area on the GUI.
	 */
	private void createGUI() {
		
		JFrame frame = new JFrame();
		frame.setTitle("Lab 3 - National Weather Service");
		Container contentPane = frame.getContentPane();
		SpringLayout paneLayout = new SpringLayout();
		contentPane.setLayout(paneLayout);
		
		//Creates the label for location.
		JLabel locationLabel = new JLabel("Location: ", JLabel.TRAILING);
		contentPane.add(locationLabel);
		
		//Creates the label for textfield to entering the latitude.
		JLabel latitudeLabel = new JLabel("Latitude: ", JLabel.TRAILING);
		contentPane.add(latitudeLabel);
		
		//Creates the textfield for entering the latitude.
		latitudeLabel.setLabelFor(latitudeTextField);
		contentPane.add(latitudeTextField);
		
		//Creates the label for textfield to entering the longitude.
		JLabel longitudeLabel = new JLabel("Longitude: ", JLabel.TRAILING);
		contentPane.add(longitudeLabel);
		
		//Creates the textfield for entering the longitude.
		longitudeLabel.setLabelFor(longitudeTextField);
		contentPane.add(longitudeTextField);
		
		//Refresh button, on clicking sends a soap request to the server and 
		//refreshes the data on GUI.
		JButton refreshButton = new JButton("REFRESH");
		refreshButton.setVerticalTextPosition(AbstractButton.CENTER);
		refreshButton.setHorizontalTextPosition(AbstractButton.CENTER);
		contentPane.add(refreshButton);
		refreshButton.addActionListener(this);
		
		//Exit button to close the client window. 
		JButton exitButton = new JButton("EXIT");
		exitButton.setVerticalTextPosition(AbstractButton.CENTER);
		exitButton.setHorizontalTextPosition(AbstractButton.CENTER);
		contentPane.add(exitButton);
		exitButton.addActionListener(this);
		
		//Label for the weather conditions textArea.
		JLabel weatherCondLabel = new JLabel("Weather Conditions: ", JLabel.TRAILING);
		contentPane.add(weatherCondLabel);
		
		//Weather Conditions textArea used to display the response received from server. 
		JScrollPane weatherCondTextAScroll = new JScrollPane(weatherCondTextArea);
		weatherCondTextArea.setEditable(false);
		weatherCondTextArea.setLineWrap(true);
		contentPane.add(weatherCondTextAScroll);
		
		//Layout all the components above on the frame.
		paneLayout.putConstraint(SpringLayout.WEST, locationLabel, 25, SpringLayout.WEST, contentPane);
		paneLayout.putConstraint(SpringLayout.NORTH, locationLabel, 20, SpringLayout.NORTH, contentPane);
		
		paneLayout.putConstraint(SpringLayout.WEST, latitudeLabel, 25, SpringLayout.WEST, locationLabel);
		paneLayout.putConstraint(SpringLayout.NORTH, latitudeLabel, 27, SpringLayout.NORTH, locationLabel);
		
		paneLayout.putConstraint(SpringLayout.WEST, latitudeTextField, 5, SpringLayout.EAST, latitudeLabel);
		paneLayout.putConstraint(SpringLayout.NORTH, latitudeTextField, 25, SpringLayout.NORTH, locationLabel);
		
		paneLayout.putConstraint(SpringLayout.WEST, longitudeLabel, 25, SpringLayout.EAST, latitudeTextField);
		paneLayout.putConstraint(SpringLayout.NORTH, longitudeLabel, 27, SpringLayout.NORTH, locationLabel);
		
		paneLayout.putConstraint(SpringLayout.WEST, longitudeTextField, 5, SpringLayout.EAST, longitudeLabel);
		paneLayout.putConstraint(SpringLayout.NORTH, longitudeTextField, 25, SpringLayout.NORTH, locationLabel);
		
		paneLayout.putConstraint(SpringLayout.WEST, refreshButton, 100, SpringLayout.WEST, contentPane);
		paneLayout.putConstraint(SpringLayout.NORTH, refreshButton, 10, SpringLayout.SOUTH, longitudeTextField);
		
		paneLayout.putConstraint(SpringLayout.WEST, exitButton, 10, SpringLayout.EAST, refreshButton);
		paneLayout.putConstraint(SpringLayout.NORTH, exitButton, 10, SpringLayout.SOUTH, longitudeTextField);
		
		paneLayout.putConstraint(SpringLayout.WEST, weatherCondLabel, 25, SpringLayout.WEST, contentPane);
		paneLayout.putConstraint(SpringLayout.NORTH, weatherCondLabel, 15, SpringLayout.SOUTH, exitButton);
		
		paneLayout.putConstraint(SpringLayout.WEST, weatherCondTextAScroll, 10, SpringLayout.EAST, weatherCondLabel);
		paneLayout.putConstraint(SpringLayout.NORTH, weatherCondTextAScroll, 15, SpringLayout.SOUTH, exitButton);
		
		paneLayout.putConstraint(SpringLayout.EAST, contentPane, 15, SpringLayout.EAST, weatherCondTextAScroll);
		paneLayout.putConstraint(SpringLayout.SOUTH, contentPane, 15, SpringLayout.SOUTH, weatherCondTextAScroll);
		
		contentPane.setVisible(true);
		
		frame.pack();
		frame.setVisible(true);
	}
	
	/**
	 * Handles the events created by clicking the buttons REFRESH and EXIT on the GUI.
	 * On clicking REFRESH sends the inputed latitude and longitude of location using SOAP request to the web service 
	 * on website http://w1.weather.gov and displays the current weather conditions in the text area on GUI. 
	 * On clicking EXIT, shutdowns the client.
	 * @param actEvent The click REFRESH and EXIT events object.
	 */
	public void actionPerformed(ActionEvent actEvent) {
		
		//Handles the event generated on clicking REFRESH button on the GUI.
		if ("REFRESH".equals(actEvent.getActionCommand())) {
			
			//On refresh, get the data again from the web service and display on UI.
			String weatherConditions = getWeatherConditions(new BigDecimal(latitudeTextField.getText()), 
														   new BigDecimal(longitudeTextField.getText()));
			
			weatherCondTextArea.setText(weatherConditions);
			
			//Handles the event generated on clicking the EXIT button on GUI.
		} else if ("EXIT".equals(actEvent.getActionCommand().trim())) {
			
			System.exit(0);
		}
		
	}
}
