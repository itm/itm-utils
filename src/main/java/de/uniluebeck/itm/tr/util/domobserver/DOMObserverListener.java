package de.uniluebeck.itm.tr.util.domobserver;

import javax.xml.namespace.QName;
import javax.xml.xpath.XPathExpressionException;

/**
 * An interface to be implemented by listeners that want to be notified of changes to (XML) DOM objects through the
 * {@link DOMObserver}.
 * <p/>
 * The listener must provide the DOMObserver with an XPath query to be executed on the DOM representation held by the
 * DOMObserver. This way the listener can further narrow his "region of interest" in the DOM, thereby not being called
 * when changes in other parts of the DOM happen.
 */
public interface DOMObserverListener {

	/**
	 * Returns the type of the expected result of the XPath query executed on the DOM. Typically one of the constants in
	 * {@link javax.xml.xpath.XPathConstants}.
	 *
	 * @return the type of the expected result of the XPath query
	 */
	QName getQName();

	/**
	 * The XPath query to be executed on the DOM structure the DOMObserver observers.
	 *
	 * @return a valid XPath query string
	 */
	String getXPathExpression();

	/**
	 * Invoked by the DOMObserver when a change occurred in the (potentially narrowed sub-region of the) DOM tree.
	 *
	 * @param oldAndNew
	 * 		a tuple of (old XPath query result, new XPath query result)
	 */
	void onDOMChanged(DOMTuple oldAndNew);

	/**
	 * Invoked by the DOMObserver if he fails to load the next DOM
	 *
	 * @param cause
	 * 		the Throwable that caused this failure
	 */
	void onDOMLoadFailure(Throwable cause);

	/**
	 * Invoked by the DOMObserver if the XPath expression is invalid or could not be executed on the DOM tree
	 *
	 * @param cause
	 * 		the exception that was raised by the XPath evaluation library
	 */
	void onXPathEvaluationFailure(XPathExpressionException cause);

}
