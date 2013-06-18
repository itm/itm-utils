package de.uniluebeck.itm.util.concurrent;

import java.util.Map;

public interface ProgressListenableFutureMap<K, V>
		extends Map<K, ProgressListenableFuture<V>>, ProgressListenableFuture<Map<K, V>> {

}
