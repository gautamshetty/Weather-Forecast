package client;

import java.io.FileInputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

public class XMLParser {

	public static void main(String[] args) {
		
		DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
		
		DocumentBuilder builder = null;
		
		try {
			
		    builder = builderFactory.newDocumentBuilder();
		    
		    Document document = builder.parse(new FileInputStream("/Users/gautamshetty/Documents/Workspace/Java/Dist_Syst_Lab_3/src/client/result.xml"));
//		    Document xmlDocument = builder.parse(new ByteArrayInputStream(xml.getBytes()));
		    XPath xPath =  XPathFactory.newInstance().newXPath();
		    
		    DateFormat dtf = new SimpleDateFormat("yyyy-MM-dd");
		    String currentDate = dtf.format(new Date());
		    
		    StringBuffer weatherConditions = new StringBuffer();
		    
		    String expression = "/dwml/data/time-layout[@time-coordinate='local']/start-valid-time";
			System.out.println(expression);
			NodeList nodeList = (NodeList) xPath.compile(expression).evaluate(document, XPathConstants.NODESET);
			int number = 0;
			for (int i = 0; i < nodeList.getLength(); i++) {
				if (currentDate.equals(nodeList.item(i).getFirstChild().getNodeValue().substring(0, 10)))
					break;
				number++;
			}
		    
		    expression = "/dwml/data/parameters/temperature[@type='maximum']/value";
			System.out.println(expression);
			nodeList = (NodeList) xPath.compile(expression).evaluate(document, XPathConstants.NODESET);
			if (nodeList.item(number).getFirstChild() != null)
				weatherConditions.append("Maximum Temperature = ").append(nodeList.item(number).getFirstChild().getNodeValue()).append("\n");
			
			expression = "/dwml/data/parameters/temperature[@type='minimum']/value";
			System.out.println(expression);
			nodeList = (NodeList) xPath.compile(expression).evaluate(document, XPathConstants.NODESET);
			if (nodeList.item(number).getFirstChild() != null)
				weatherConditions.append("Minimum Temperature = ").append(nodeList.item(number).getFirstChild().getNodeValue()).append("\n");
			
			expression = "/dwml/data/parameters/probability-of-precipitation[@type='12 hour']/value";
			System.out.println(expression);
			nodeList = (NodeList) xPath.compile(expression).evaluate(document, XPathConstants.NODESET);
			if (nodeList.item(number).getFirstChild() != null)
				weatherConditions.append("12 Hourly Probability of Precipitation = ").append(nodeList.item(number).getFirstChild().getNodeValue()).append("\n");
			
			expression = "/dwml/data/parameters/cloud-amount[@type='total']/value";
			System.out.println(expression);
			nodeList = (NodeList) xPath.compile(expression).evaluate(document, XPathConstants.NODESET);
			if (nodeList.item(number).getFirstChild() != null)
				weatherConditions.append("Cloud Cover Amount = ").append(nodeList.item(number).getFirstChild().getNodeValue()).append("\n");
			
			expression = "/dwml/data/parameters/wind-speed[@type='sustained']/value";
			System.out.println(expression);
			nodeList = (NodeList) xPath.compile(expression).evaluate(document, XPathConstants.NODESET);
			if (nodeList.item(number).getFirstChild() != null)
				weatherConditions.append("Wind Speed = ").append(nodeList.item(number).getFirstChild().getNodeValue()).append("\n");
			
			expression = "/dwml/data/parameters/direction[@type='wind']/value";
			System.out.println(expression);
			nodeList = (NodeList) xPath.compile(expression).evaluate(document, XPathConstants.NODESET);
			if (nodeList.item(number).getFirstChild() != null)
				weatherConditions.append("Wind Direction = ").append(nodeList.item(number).getFirstChild().getNodeValue()).append("\n");
			
			expression = "/dwml/data/parameters/water-state/waves[@type='significant']/value";
			System.out.println(expression);
			nodeList = (NodeList) xPath.compile(expression).evaluate(document, XPathConstants.NODESET);
			if (nodeList.item(number).getFirstChild() != null)
				weatherConditions.append("Wave Height = ").append(nodeList.item(number).getFirstChild().getNodeValue()).append("\n");
			
			System.out.println("Weather Conditions : \n\n" + weatherConditions.toString());
			
		} catch (Exception e) {
		    e.printStackTrace();
		}
		
	}
}
