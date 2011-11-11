package de.uniluebeck.itm.tr.util.domobserver;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;


import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathConstants;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Provider;
import com.google.inject.TypeLiteral;

import de.uniluebeck.itm.tr.util.ListenerManager;
import de.uniluebeck.itm.tr.util.ListenerManagerImpl;
import de.uniluebeck.itm.tr.util.Logging;


@RunWith(MockitoJUnitRunner.class)
public class DOMObserverImplTest {

        
	private static final String CONFIG_1 = "de/uniluebeck/itm/tr/util/domobserver/tr.iwsn-testbed.xml";
	private static final String CONFIG_2 = "de/uniluebeck/itm/tr/util/domobserver/tr.iwsn-testbed2.xml";

	private static final String X_PATH_EXPRESSION_ROOT_NODE = "/*";
	private static final String X_PATH_EXPRESSION_APPLICATION_NODES = "//application";
	
	private DOMObserver domObserver;

	@Mock
	private Provider<Node> nodeProvider;

	@Before
	public void setUp() throws Exception {
	    Logging.setLoggingDefaults();
		domObserver = Guice.createInjector(new AbstractModule() {
			@Override
			protected void configure() {
				bind(new TypeLiteral<ListenerManager<DOMObserverListener>>() {
				}
				).to(new TypeLiteral<ListenerManagerImpl<DOMObserverListener>>() {
				}
				);
				bind(Node.class).toProvider(nodeProvider);
				bind(DOMObserver.class).to(DOMObserverImpl.class);
			}
		}
		).getInstance(DOMObserver.class);
	}

	@Test
	public void testThatNoChangeIsDetectedWhenOldAndNewAreBothNull() throws Exception {

		// setup
		when(nodeProvider.get()).thenReturn(null);

		// act
		domObserver.updateCurrentDOM();

		// validate
		assertNull(domObserver.getLastScopedChanges(X_PATH_EXPRESSION_ROOT_NODE, XPathConstants.NODE));
	}

	@Test
	public void testThatChangeIsDetectedWhenOldIsNullAndNewIsNotNull() throws Exception {

		Node node = createDOM(CONFIG_1);
		when(nodeProvider.get()).thenReturn(node);

		domObserver.updateCurrentDOM();

		DOMTuple lastScopedChanges = domObserver.getLastScopedChanges(X_PATH_EXPRESSION_ROOT_NODE, XPathConstants.NODE);
		assertNull(lastScopedChanges.getFirst());
		assertTrue(node.isEqualNode((Node) lastScopedChanges.getSecond()));
	}

	private Node createDOM(final String fileName) throws Exception {

		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document document = dBuilder.parse(DOMObserverImplTest.class.getClassLoader().getResourceAsStream(fileName));
		return document.getDocumentElement();
	}

	@Test
	public void testThatChangeIsDetectedWhenOldIsNotNullButNewIsNull() throws Exception {
		Node node1 = createDOM(CONFIG_1);
		when(nodeProvider.get()).thenReturn(node1).thenReturn(null);
		domObserver.updateCurrentDOM(); //new node is now node1
		domObserver.updateCurrentDOM(); //new node is now null old node is node1
		
		DOMTuple lastScopedChanges = domObserver.getLastScopedChanges(X_PATH_EXPRESSION_ROOT_NODE, XPathConstants.NODE);
		assertNotNull(lastScopedChanges);
		assertNotNull(lastScopedChanges.getFirst());
		assertNull(lastScopedChanges.getSecond());
		
		
	}

	@Test
	public void testThatNoChangeIsDetectedWhenBotNotNullAndBothAreEqualUnscoped() throws Exception {
	    Node node1 = createDOM(CONFIG_1);
	    Node node2 = createDOM(CONFIG_1); // same config for both nodes
            when(nodeProvider.get()).thenReturn(node1).thenReturn(node2);
            domObserver.updateCurrentDOM(); //new node is now node1
            domObserver.updateCurrentDOM(); //new node is now node2 old node is node1
            DOMTuple lastScopedChanges = domObserver.getLastScopedChanges(X_PATH_EXPRESSION_ROOT_NODE, XPathConstants.NODE);
            assertNull(lastScopedChanges);
           
	}

	@Test
	public void testThatChangeIsDetectedWhenBothNotNullAndChangeOccurredUnscoped() throws Exception {
	    Node node1 = createDOM(CONFIG_1);
            Node node2 = createDOM(CONFIG_2); //now the configs differ
            when(nodeProvider.get()).thenReturn(node1).thenReturn(node2);
            domObserver.updateCurrentDOM(); //new node is now node1
            domObserver.updateCurrentDOM(); //new node is now node2 old node is node1
            DOMTuple lastScopedChanges = domObserver.getLastScopedChanges(X_PATH_EXPRESSION_ROOT_NODE, XPathConstants.NODE);
            assertNotNull(lastScopedChanges);
            assertNotNull(lastScopedChanges.getFirst());
            assertFalse(((Node)lastScopedChanges.getFirst()).isEqualNode((Node) lastScopedChanges.getSecond()));
	}

	@Test
	public void testThatNoChangeIsDetectedWhenBotNotNullAndBothAreEqualScoped() throws Exception {
	    Node node1 = createDOM(CONFIG_1);
            Node node2 = createDOM(CONFIG_1); // same config for both nodes
            when(nodeProvider.get()).thenReturn(node1).thenReturn(node2);
            domObserver.updateCurrentDOM(); //new node is now node1
            domObserver.updateCurrentDOM(); //new node is now node2 old node is node1
            DOMTuple lastScopedChanges = domObserver.getLastScopedChanges(X_PATH_EXPRESSION_APPLICATION_NODES, XPathConstants.NODESET);
            assertNull(lastScopedChanges);
	}

	@Test
	public void testThatChangeIsDetectedWhenBothNotNullAndChangeOccurredScoped() throws Exception {
	    Node node1 = createDOM(CONFIG_1);
            Node node2 = createDOM(CONFIG_2); //now the configs differ
            when(nodeProvider.get()).thenReturn(node1).thenReturn(node2);
            domObserver.updateCurrentDOM(); //new node is now node1
            domObserver.updateCurrentDOM(); //new node is now node2 old node is node1
            DOMTuple lastScopedChanges = domObserver.getLastScopedChanges(X_PATH_EXPRESSION_APPLICATION_NODES, XPathConstants.NODESET);
            assertNotNull(lastScopedChanges);
            assertNotNull(lastScopedChanges.getFirst());
            assertNotNull(lastScopedChanges.getSecond());
	}

	@Test
	public void testThatNewIsNullIfInvalidXML() throws Exception {
	    Node node1 = createDOM(CONFIG_1);
	    when(nodeProvider.get()).thenReturn(node1).thenThrow(new RuntimeException("test xml is invalid"));
	    domObserver.updateCurrentDOM(); //new node is now node1
            domObserver.updateCurrentDOM(); //new node is now null old node is node1
            DOMTuple lastScopedChanges = domObserver.getLastScopedChanges(X_PATH_EXPRESSION_ROOT_NODE, XPathConstants.NODE);
            assertNotNull(lastScopedChanges);
            assertNotNull(lastScopedChanges.getFirst());
            assertNull(lastScopedChanges.getSecond());
	}

	@Test
	public void testThatNoChangesAreDetectedIfProviderDeliversSameInstanceAgain() throws Exception {
		Node config1DOM = createDOM(CONFIG_1);
		when(nodeProvider.get()).thenReturn(config1DOM);
		domObserver.updateCurrentDOM();
		domObserver.updateCurrentDOM();
		assertNull(domObserver.getLastScopedChanges(X_PATH_EXPRESSION_ROOT_NODE, XPathConstants.NODE));
	}

	@Test
	public void testThatCurrentIsNullIfProviderThrowsException() throws Exception {

		Node config1DOM = createDOM(CONFIG_1);

		when(nodeProvider.get())
				.thenReturn(config1DOM)
				.thenThrow(new RuntimeException("Test"));

		domObserver.updateCurrentDOM();
		domObserver.updateCurrentDOM();

		DOMTuple lastScopedChanges = domObserver.getLastScopedChanges(X_PATH_EXPRESSION_ROOT_NODE, XPathConstants.NODE);
		assertNotNull(lastScopedChanges.getFirst());
		assertNull(lastScopedChanges.getSecond());
	}
}
