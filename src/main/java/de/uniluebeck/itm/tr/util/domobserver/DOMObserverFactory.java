package de.uniluebeck.itm.tr.util.domobserver;

import com.google.inject.Provider;
import org.w3c.dom.Node;

public interface DOMObserverFactory {

	DOMObserver create(final Provider<Node> newDOMProvider);

}
