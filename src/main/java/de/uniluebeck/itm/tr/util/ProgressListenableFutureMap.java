package de.uniluebeck.itm.tr.util;

import java.util.Map;

public interface ProgressListenableFutureMap<K, V>
		extends Map<K, ProgressListenableFuture<V>>,
		ProgressListenableFuture<Map<K, V>> {

}
