package de.uniluebeck.itm.tr.util.domobserver;

import javax.xml.namespace.QName;
import javax.xml.xpath.XPathExpressionException;

public interface DOMObserverListener {

	QName getQName();

	String getXPathExpression();

	void onDOMChanged(DOMTuple oldAndNew);

	void onDOMLoadFailure(Throwable cause);

	void onXPathEvaluationFailure(XPathExpressionException cause);

}
