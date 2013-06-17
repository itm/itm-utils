package de.uniluebeck.itm.util.domobserver;

import com.google.inject.Provider;
import org.w3c.dom.Node;

public interface DOMObserverFactory {

	DOMObserver create(final Provider<Node> newDOMProvider);

}
