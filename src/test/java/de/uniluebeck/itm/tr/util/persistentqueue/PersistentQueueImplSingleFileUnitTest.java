package de.uniluebeck.itm.tr.util.persistentqueue;

import de.uniluebeck.itm.tr.util.persistentqueue.impl.PersistentQueueImplSingleFile;

import java.io.File;
import java.io.IOException;


public class PersistentQueueImplSingleFileUnitTest extends PersistentQueueUnitTest{

    public PersistentQueueImplSingleFileUnitTest() throws IOException {
        super(new PersistentQueueImplSingleFile(
				File.createTempFile("PersistentQueueUnitTestSingleFile", "").getAbsolutePath().toString(),
				12)
		);
    }

}
