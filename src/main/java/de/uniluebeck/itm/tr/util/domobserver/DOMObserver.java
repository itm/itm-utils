package de.uniluebeck.itm.tr.util.domobserver;

import de.uniluebeck.itm.tr.util.Listenable;

import javax.xml.namespace.QName;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

/**
 * A class that observes two instances of a DOM {@link org.w3c.dom.Node} for changes. It is intended to track changes
 * of one document over time. Listeners can scope which sub-tree of the DOM should be observed. The first DOM instance
 * is considered as an old version of the second DOM instance. By calling {@link de.uniluebeck.itm.tr.util.domobserver.DOMObserver#updateCurrentDOM()}
 * the observer "advances in time" by retrieving a new state (e.g. from an XML file stored on disk) and remembering the
 * current state as the next old state.
 */
public interface DOMObserver extends Runnable, Listenable<DOMObserverListener> {

	/**
	 * Returns the changes since last calling {@link DOMObserver#updateCurrentDOM()}
	 * @param xPathExpression the scope to inspect
	 * @param qName type of expected result QName see also {@link XPathConstants}
	 * @return {@code null} if no changes occurred or a {@link DOMTuple} instance holding the old (scoped) DOM and the new
	 *         (scoped) DOM otherwise
	 */
	DOMTuple getLastScopedChanges(final String xPathExpression, final QName qName) throws XPathExpressionException;

	/**
	 * Lets the observer "advance in time" by retrieving a new state (e.g. from an XML file stored on disk) and remembering
	 * the current state as the next old state.
	 * 
	 */
	void updateCurrentDOM();

}
