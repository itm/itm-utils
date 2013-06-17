package de.uniluebeck.itm.util.domobserver;

import com.google.inject.Inject;
import com.google.inject.Provider;
import org.w3c.dom.Node;

class DOMObserverFactoryImpl implements DOMObserverFactory {

	private final Provider<DOMObserverListenerManager> listenerManagerProvider;

	@Inject
	public DOMObserverFactoryImpl(final Provider<DOMObserverListenerManager> listenerManagerProvider) {
		this.listenerManagerProvider = listenerManagerProvider;
	}

	@Override
	public DOMObserver create(final Provider<Node> newDOMProvider) {
		return new DOMObserverImpl(listenerManagerProvider.get(), newDOMProvider);
	}
}
