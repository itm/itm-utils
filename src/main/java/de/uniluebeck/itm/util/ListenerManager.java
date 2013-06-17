package de.uniluebeck.itm.util;

import com.google.common.collect.ImmutableList;

public interface ListenerManager<T> extends Listenable<T> {

	ImmutableList<T> getListeners();
}
