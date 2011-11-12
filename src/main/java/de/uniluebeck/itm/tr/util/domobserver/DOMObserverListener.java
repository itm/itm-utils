package de.uniluebeck.itm.tr.util.domobserver;

import javax.xml.namespace.QName;

public interface DOMObserverListener {

	QName getQName();

	String getXPathExpression();

	void onDOMChanged(DOMTuple oldAndNew);

}
