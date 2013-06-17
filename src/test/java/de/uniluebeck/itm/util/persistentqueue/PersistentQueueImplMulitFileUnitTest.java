package de.uniluebeck.itm.util.persistentqueue;

import java.io.IOException;


public class PersistentQueueImplMulitFileUnitTest extends PersistentQueueUnitTest{

    public PersistentQueueImplMulitFileUnitTest() throws IOException {
		super(new PersistentQueueImplMultiFile(System.getProperty("java.io.tmpdir"), 12));
	}

}
