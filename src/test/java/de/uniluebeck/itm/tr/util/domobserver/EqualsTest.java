//package de.uniluebeck.itm.tr.util.domobserver;
//
//import org.w3c.dom.Document;
//import org.w3c.dom.NodeList;
//import org.xml.sax.SAXException;
//
//import javax.xml.bind.JAXBContext;
//import javax.xml.bind.JAXBException;
//import javax.xml.parsers.DocumentBuilder;
//import javax.xml.parsers.DocumentBuilderFactory;
//import javax.xml.parsers.ParserConfigurationException;
//import javax.xml.xpath.*;
//import java.io.IOException;
//
//public class EqualsTest {
//
//	public static void main(String[] args) throws JAXBException, IOException, SAXException,
//			ParserConfigurationException, XPathExpressionException {
//
//		/*
//		Testbed testbed = JAXB.unmarshal(EqualsTest.class.getClassLoader().getResourceAsStream("tr.iwsn-testbed.xml"),
//				Testbed.class
//		);
//		Testbed testbed2 = JAXB.unmarshal(EqualsTest.class.getClassLoader().getResourceAsStream("tr.iwsn-testbed.xml"),
//				Testbed.class
//		);
//
//		JAXBContext context = JAXBContext.newInstance(Testbed.class);
//		Marshaller marshaller = context.createMarshaller();
//		*/
//
//		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
//		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
//		Document doc = dBuilder.parse(EqualsTest.class.getClassLoader().getResourceAsStream("tr.iwsn-testbed.xml"));
//		Document doc2 = dBuilder.parse(EqualsTest.class.getClassLoader().getResourceAsStream("tr.iwsn-testbed2.xml"));
//
//		XPathFactory xPathFactory = XPathFactory.newInstance();
//		XPath xPath = xPathFactory.newXPath();
//		XPathExpression expression = xPath.compile("//testbed/nodes/names/nodename");
//
//		Object result = expression.evaluate(doc, XPathConstants.NODESET);
//
//		NodeList nodes = (NodeList) result;
//		for (int i = 0; i < nodes.getLength(); i++) {
//
//			JAXBContext jaxbContext = JAXBContext.newInstance(NodeName.class);
//			NodeName nodeName = jaxbContext.createUnmarshaller().unmarshal(nodes.item(i), NodeName.class).getValue();
//			System.out.println(nodeName.getName());
//		}
//
//		System.out.println(doc.isEqualNode(doc2));
//
//		/*Node node = marshaller.getNode(testbed);
//		Node node2 = marshaller.getNode(testbed2);
//
//		System.out.println(node.isEqualNode(node2));
//		System.out.println(testbed);
//
//		Node any = (Node) testbed.getNodes().get(0).getApplications().getApplication().get(0).getAny();
//		Node any2 = (Node) testbed.getNodes().get(0).getApplications().getApplication().get(1).getAny();
//
//		System.out.println(any);
//		System.out.println(any2);
//		System.out.println(any.isEqualNode(any2));
//		*/
//	}
//
//}
