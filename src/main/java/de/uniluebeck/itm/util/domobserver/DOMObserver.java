package de.uniluebeck.itm.util.domobserver;

import de.uniluebeck.itm.util.Listenable;
import org.w3c.dom.Node;

import javax.annotation.Nullable;
import javax.xml.namespace.QName;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

/**
 * An observable class that checks for changes to a DOM {@link org.w3c.dom.Node}. It is intended to track changes of
 * one document over time. Listeners can scope which sub-tree of the DOM should be observed.
 * <p/>
 * The {@code oldDOM} instance is considered an old version of the current DOM instance held by the DOM observer. By calling
 * {@link DOMObserver#updateCurrentDOM()} the observer "advances in time" by
 * retrieving a new DOM (e.g. from an XML file stored on disk). The decision if a change has occurred is made by
 * comparing either the {@code oldDOM} argument of {@link DOMObserver#getScopedChanges(org.w3c.dom.Node, String,
 * javax.xml.namespace.QName)} to the DOMObservers current DOM or by looking at the last state that an individual
 * listener saw. Therefore, the DOMObserver always keeps in memory the current version of the DOM plus as many
 * old instances of the DOM as listener instances still refer to. So, for most of the time this will be two instances
 * in total.
 */
public interface DOMObserver extends Runnable, Listenable<DOMObserverListener> {

	/**
	 * Returns the changes since last calling {@link DOMObserver#updateCurrentDOM()}
	 *
	 * @param oldDOM
	 * 		the DOM against which the changes should be calculated. This may be the DOM as it was before calling {@link
	 * 		DOMObserver#updateCurrentDOM()}, a totally different state or {@code null}
	 * 		if the changes should be "calculated" against an empty state (i.e. the changes calculated will then be identical
	 * 		to the current DOM
	 * @param xPathExpression
	 * 		the scope to inspect
	 * @param qName
	 * 		type of expected result QName see also {@link XPathConstants}
	 *
	 * @return {@code null} if no changes occurred or a {@link DOMTuple} instance holding the old (scoped) DOM and the new
	 *         (scoped) DOM otherwise
	 *
	 * @throws javax.xml.xpath.XPathExpressionException
	 * 		if either the XPath expression is invalid or its evaluation results in an exception being thrown
	 */
	DOMTuple getScopedChanges(@Nullable final Node oldDOM, final String xPathExpression, final QName qName)
			throws XPathExpressionException;

	/**
	 * Lets the observer "advance in time" by retrieving a new state (e.g. from an XML file stored on disk).
	 *
	 * @return a tuple type containing the old and current DOM (instances of {@link Node} or {@code null})
	 *
	 * @throws Exception
	 * 		if loading the "next step in time" fails. In this case the DOMObserver will keep its state and not "advance in
	 * 		time"
	 */
	DOMTuple updateCurrentDOM() throws Exception;

}
