package de.uniluebeck.itm.util.persistentqueue;

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
