package de.uniluebeck.itm.tr.util;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.Map;

public interface ListenableFutureMap<K, V> extends Map<K, ListenableFuture<V>>, ListenableFuture<Map<K, V>> {

}
