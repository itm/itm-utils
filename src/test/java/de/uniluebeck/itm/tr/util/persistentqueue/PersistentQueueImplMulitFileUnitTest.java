package de.uniluebeck.itm.tr.util.persistentqueue;

import de.uniluebeck.itm.tr.util.persistentqueue.impl.PersistentQueueImplMultiFile;

import java.io.IOException;


public class PersistentQueueImplMulitFileUnitTest extends PersistentQueueUnitTest{

    public PersistentQueueImplMulitFileUnitTest() throws IOException {
		super(new PersistentQueueImplMultiFile(System.getProperty("java.io.tmpdir"), 12));
	}

}
