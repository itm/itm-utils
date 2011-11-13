package de.uniluebeck.itm.tr.util.domobserver;

import com.google.common.collect.ImmutableList;
import de.uniluebeck.itm.tr.util.Listenable;
import org.w3c.dom.Node;

interface DOMObserverListenerManager extends Listenable<DOMObserverListener> {

	Node getLastDOM(final DOMObserverListener listener);

	void updateLastDOM(final DOMObserverListener listener, final Node updatedDOM);

	ImmutableList<DOMObserverListener> getListeners();

}
