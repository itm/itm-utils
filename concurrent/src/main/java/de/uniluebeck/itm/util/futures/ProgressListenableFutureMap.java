package de.uniluebeck.itm.util.futures;

import java.util.Map;

public interface ProgressListenableFutureMap<K, V>
		extends Map<K, ProgressListenableFuture<V>>, ProgressListenableFuture<Map<K, V>> {

}
