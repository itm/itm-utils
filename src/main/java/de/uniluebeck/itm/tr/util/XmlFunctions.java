package de.uniluebeck.itm.tr.util;

import com.google.common.base.Function;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.annotation.Nullable;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.*;
import java.io.File;

public final class XmlFunctions {

	private enum XPathToBooleanEvaluationFunction implements Function<Tuple<String, Node>, Boolean> {

		INSTANCE;

		@Override
		public Boolean apply(final Tuple<String, Node> input) {

			try {

				XPathFactory xPathFactory = XPathFactory.newInstance();
				XPath xPath = xPathFactory.newXPath();
				XPathExpression expression = xPath.compile(input.getFirst());

				return (Boolean) expression.evaluate(input.getSecond(), XPathConstants.BOOLEAN);

			} catch (XPathExpressionException e) {
				throw new RuntimeException(e);
			}
		}
	}

	private enum XPathToStringEvaluationFunction implements Function<Tuple<String, Node>, String> {

		INSTANCE;

		@Override
		public String apply(final Tuple<String, Node> input) {

			try {

				XPathFactory xPathFactory = XPathFactory.newInstance();
				XPath xPath = xPathFactory.newXPath();
				XPathExpression expression = xPath.compile(input.getFirst());

				return (String) expression.evaluate(input.getSecond(), XPathConstants.STRING);

			} catch (XPathExpressionException e) {
				throw new RuntimeException(e);
			}
		}
	}

	private enum XPathToNumberEvaluationFunction implements Function<Tuple<String, Node>, Number> {

		INSTANCE;

		@Override
		public Number apply(final Tuple<String, Node> input) {

			try {

				XPathFactory xPathFactory = XPathFactory.newInstance();
				XPath xPath = xPathFactory.newXPath();
				XPathExpression expression = xPath.compile(input.getFirst());

				return (Number) expression.evaluate(input.getSecond(), XPathConstants.NUMBER);

			} catch (XPathExpressionException e) {
				throw new RuntimeException(e);
			}
		}
	}

	private enum XPathToNodeEvaluationFunction implements Function<Tuple<String, Node>, Node> {

		INSTANCE;

		@Override
		public Node apply(final Tuple<String, Node> input) {

			try {

				XPathFactory xPathFactory = XPathFactory.newInstance();
				XPath xPath = xPathFactory.newXPath();
				XPathExpression expression = xPath.compile(input.getFirst());

				return (Node) expression.evaluate(input.getSecond(), XPathConstants.NODE);

			} catch (XPathExpressionException e) {
				throw new RuntimeException(e);
			}
		}
	}

	private enum XPathToNodeListEvaluationFunction implements Function<Tuple<String, Node>, NodeList> {

		INSTANCE;

		@Override
		public NodeList apply(final Tuple<String, Node> input) {

			try {

				XPathFactory xPathFactory = XPathFactory.newInstance();
				XPath xPath = xPathFactory.newXPath();
				XPathExpression expression = xPath.compile(input.getFirst());

				return (NodeList) expression.evaluate(input.getSecond(), XPathConstants.NODESET);

			} catch (XPathExpressionException e) {
				throw new RuntimeException(e);
			}
		}
	}

	private enum FileToRootElementTupleFunction implements Function<Tuple<String, File>, Tuple<String, Node>> {

		INSTANCE;

		@Override
		public Tuple<String, Node> apply(final Tuple<String, File> input) {

			try {

				DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
				Document document = dBuilder.parse(input.getSecond());
				return new Tuple<String, Node>(input.getFirst(), document.getDocumentElement());

			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	private enum FileToRootElementFunction implements Function<File, Node> {

		INSTANCE;

		@Override
		public Node apply(final File input) {

			try {

				DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
				Document document = dBuilder.parse(input);
				return document.getDocumentElement();

			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	private static class UnmarshalNodeToPojoFunction<T> implements Function<Tuple<Node, Class<T>>, T> {
		@Override
		public T apply(final Tuple<Node, Class<T>> input) {

			try {

				JAXBContext jaxbContext = JAXBContext.newInstance(input.getSecond().getPackage().getName());
				Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
				return unmarshaller.unmarshal(input.getFirst(), input.getSecond()).getValue();

			} catch (JAXBException e) {
				throw new RuntimeException(e);
			}
		}
	}

	public static Function<File, Node> fileToRootElementFunction() {
		return FileToRootElementFunction.INSTANCE;
	}

	public static Function<Tuple<String, File>, Tuple<String, Node>> fileToRootElementTupleFunction() {
		return FileToRootElementTupleFunction.INSTANCE;
	}

	public static Function<Tuple<String, Node>, Boolean> xPathToBooleanEvaluationFunction() {
		return XPathToBooleanEvaluationFunction.INSTANCE;
	}

	public static Function<Tuple<String, Node>, Node> xPathToNodeEvaluationFunction() {
		return XPathToNodeEvaluationFunction.INSTANCE;
	}

	public static Function<Tuple<String, Node>, NodeList> xPathToNodeListEvaluationFunction() {
		return XPathToNodeListEvaluationFunction.INSTANCE;
	}

	public static Function<Tuple<String, Node>, Number> xPathToNumberEvaluationFunction() {
		return XPathToNumberEvaluationFunction.INSTANCE;
	}

	public static Function<Tuple<String, Node>, String> xPathToStringEvaluationFunction() {
		return XPathToStringEvaluationFunction.INSTANCE;
	}

	public static <T> Function<Tuple<Node, Class<T>>, T> unmarshalNodeToPojoFunction() {
		return new UnmarshalNodeToPojoFunction<T>();
	}

}
