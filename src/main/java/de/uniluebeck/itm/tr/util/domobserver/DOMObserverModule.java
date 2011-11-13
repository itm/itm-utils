package de.uniluebeck.itm.tr.util.domobserver;

import com.google.inject.AbstractModule;

public class DOMObserverModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(DOMObserverListenerManager.class).to(DOMObserverListenerManagerImpl.class);
		bind(DOMObserverFactory.class).to(DOMObserverFactoryImpl.class);
	}
}
