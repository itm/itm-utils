package de.uniluebeck.itm.tr.util;

import com.google.common.collect.ImmutableList;

public interface ListenerManager<T> extends Listenable<T> {

	ImmutableList<T> getListeners();
}
