package de.uniluebeck.itm.util.domobserver;

import com.google.inject.Guice;
import com.google.inject.Provider;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import static org.junit.Assert.assertNull;


@RunWith(MockitoJUnitRunner.class)
public class DOMObserverImplTest {

	private static final String CONFIG_1 = "de/uniluebeck/itm/util/domobserver/tr.iwsn-testbed.xml";

	private static final String CONFIG_2 = "de/uniluebeck/itm/util/domobserver/tr.iwsn-testbed2.xml";

	private static final String XPATH_EXPRESSION_ROOT_NODE = "/*";

	private static final String XPATH_EXPRESSION_APPLICATION_NODES = "//application";

	private DOMObserver domObserver;

	@Mock
	private Provider<Node> nodeProviderMock;

	@Mock
	private DOMObserverListener listenerMock;

	@Mock
	private DOMObserverListener listenerMock2;

	@Before
	public void setUp() throws Exception {
		DOMObserverFactory factory = Guice
				.createInjector(new DOMObserverModule())
				.getInstance(DOMObserverFactory.class);
		domObserver = factory.create(nodeProviderMock);
	}

	@Test
	public void testThatNoChangeIsDetectedWhenOldAndNewAreBothNull() throws Exception {

		// setup
		Mockito.when(nodeProviderMock.get()).thenReturn(null);

		// act
		Node oldDOM = (Node) domObserver.updateCurrentDOM().getFirst();

		// validate
		assertNull(domObserver.getScopedChanges(oldDOM, XPATH_EXPRESSION_ROOT_NODE, XPathConstants.NODE));
	}

	@Test
	public void testThatChangeIsDetectedWhenOldIsNullAndNewIsNotNull() throws Exception {

		Node node = createDOM(CONFIG_1);
		Mockito.when(nodeProviderMock.get()).thenReturn(node);

		Node oldDOM = (Node) domObserver.updateCurrentDOM().getFirst();

		DOMTuple lastScopedChanges = domObserver.getScopedChanges(
				oldDOM,
				XPATH_EXPRESSION_ROOT_NODE,
				XPathConstants.NODE
		);
		assertNull(lastScopedChanges.getFirst());
		Assert.assertTrue(node.isEqualNode((Node) lastScopedChanges.getSecond()));
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
		Mockito.when(nodeProviderMock.get()).thenReturn(node1).thenReturn(null);

		DOMTuple domTuple = domObserver.updateCurrentDOM();
		assertNull(domTuple.getFirst());
		Assert.assertNotNull(domTuple.getSecond());

		Node oldDOM = (Node) domObserver.updateCurrentDOM().getFirst(); // old is now node1 and current is null

		DOMTuple lastScopedChanges = domObserver.getScopedChanges(
				oldDOM,
				XPATH_EXPRESSION_ROOT_NODE,
				XPathConstants.NODE
		);

		Assert.assertNotNull(lastScopedChanges);
		Assert.assertNotNull(lastScopedChanges.getFirst());
		assertNull(lastScopedChanges.getSecond());
	}

	@Test
	public void testThatNoChangeIsDetectedWhenBotNotNullAndBothAreEqualUnscoped() throws Exception {
		Node node1 = createDOM(CONFIG_1);
		Node node2 = createDOM(CONFIG_1); // same config for both nodes
		Mockito.when(nodeProviderMock.get()).thenReturn(node1).thenReturn(node2);
		domObserver.updateCurrentDOM(); //new node is now node1
		Node oldDOM = (Node) domObserver.updateCurrentDOM().getFirst(); //new node is now node2 old node is node1
		DOMTuple lastScopedChanges = domObserver.getScopedChanges(
				oldDOM,
				XPATH_EXPRESSION_ROOT_NODE,
				XPathConstants.NODE
		);
		assertNull(lastScopedChanges);

	}

	@Test
	public void testThatChangeIsDetectedWhenBothNotNullAndChangeOccurredUnscoped() throws Exception {
		Node node1 = createDOM(CONFIG_1);
		Node node2 = createDOM(CONFIG_2); //now the configs differ
		Mockito.when(nodeProviderMock.get()).thenReturn(node1).thenReturn(node2);
		domObserver.updateCurrentDOM(); //new node is now node1
		Node oldDOM = (Node) domObserver.updateCurrentDOM().getFirst(); //new node is now node2 old node is node1
		DOMTuple lastScopedChanges = domObserver.getScopedChanges(
				oldDOM,
				XPATH_EXPRESSION_ROOT_NODE,
				XPathConstants.NODE
		);
		Assert.assertNotNull(lastScopedChanges);
		Assert.assertNotNull(lastScopedChanges.getFirst());
		Assert.assertFalse(((Node) lastScopedChanges.getFirst()).isEqualNode((Node) lastScopedChanges.getSecond()));
	}

	@Test
	public void testThatNoChangeIsDetectedWhenBotNotNullAndBothAreEqualScoped() throws Exception {

		// twice the same document but different instances
		Mockito.when(nodeProviderMock.get()).thenReturn(createDOM(CONFIG_1)).thenReturn(createDOM(CONFIG_1));

		domObserver.updateCurrentDOM(); //new node is now node1
		Node oldDOM = (Node) domObserver.updateCurrentDOM().getFirst(); //new node is now node2 old node is node1

		DOMTuple scopedChanges = domObserver.getScopedChanges(
				oldDOM,
				XPATH_EXPRESSION_APPLICATION_NODES,
				XPathConstants.NODESET
		);
		assertNull(scopedChanges);
	}

	@Test
	public void testThatNoListenersAreNotifiedWhenBotNotNullAndBothAreEqualScoped() throws Exception {

		Mockito.when(nodeProviderMock.get()).thenReturn(createDOM(CONFIG_1)).thenReturn(createDOM(CONFIG_1));
		subscribeToRootNode(listenerMock);

		domObserver.updateCurrentDOM(); //new node is now node1
		domObserver.updateCurrentDOM(); //new node is now node2 old node is node1

		Mockito.verify(listenerMock, Mockito.never()).onDOMChanged(Matchers.<DOMTuple>any());
	}

	@Test
	public void testThatChangeIsDetectedWhenBothNotNullAndChangeOccurredScoped() throws Exception {
		Node node1 = createDOM(CONFIG_1);
		Node node2 = createDOM(CONFIG_2); //now the configs differ
		Mockito.when(nodeProviderMock.get()).thenReturn(node1).thenReturn(node2);
		domObserver.updateCurrentDOM(); //new node is now node1
		Node oldDOM = (Node) domObserver.updateCurrentDOM().getFirst(); //new node is now node2 old node is node1
		DOMTuple lastScopedChanges = domObserver.getScopedChanges(
				oldDOM,
				XPATH_EXPRESSION_APPLICATION_NODES,
				XPathConstants.NODESET
		);
		Assert.assertNotNull(lastScopedChanges);
		Assert.assertNotNull(lastScopedChanges.getFirst());
		Assert.assertNotNull(lastScopedChanges.getSecond());
	}

	@Test
	public void testThatExceptionIsThrownIfXmIsInvalid() throws Exception {
		Node node1 = createDOM(CONFIG_1);
		Mockito.when(nodeProviderMock.get()).thenReturn(node1).thenThrow(new RuntimeException("test xml is invalid"));
		domObserver.updateCurrentDOM(); // new node is now node1
		try {
			domObserver.updateCurrentDOM();
			Assert.fail("An exception should have been thrown");
		} catch (Exception expected) {
		}
	}

	@Test
	public void testThatNoChangesAreDetectedIfProviderDeliversSameInstanceAgain() throws Exception {
		Node config1DOM = createDOM(CONFIG_1);
		Mockito.when(nodeProviderMock.get()).thenReturn(config1DOM);
		domObserver.updateCurrentDOM();
		Node oldDOM = (Node) domObserver.updateCurrentDOM().getFirst();
		assertNull(domObserver.getScopedChanges(
				oldDOM,
				XPATH_EXPRESSION_ROOT_NODE,
				XPathConstants.NODE
		)
		);
	}

	@Test
	public void testThatNoListenersAreCalledIfProviderDeliversSameInstanceAgain() throws Exception {

		Node config1DOM = createDOM(CONFIG_1);
		Mockito.when(nodeProviderMock.get()).thenReturn(config1DOM);
		subscribeToRootNode(listenerMock);

		domObserver.run();
		Mockito.verify(listenerMock, Mockito.times(1)).onDOMChanged(Matchers.<DOMTuple>any());

		Mockito.reset(listenerMock);
		setRootNodeSubscriptionBehaviour(listenerMock);
		domObserver.run();
		Mockito.verify(listenerMock, Mockito.never()).onDOMChanged(Matchers.<DOMTuple>any());
	}

	@Test(expected = Exception.class)
	public void testThatExceptionIsThrownIfProviderThrowsException() throws Exception {

		Mockito.when(nodeProviderMock.get()).thenThrow(new RuntimeException("Test"));

		domObserver.updateCurrentDOM();
	}

	@Test
	public void testThatListenersAreNotifiedIfProviderThrowsException() throws Exception {

		Mockito.when(nodeProviderMock.get()).thenThrow(new RuntimeException("Test"));
		subscribeToRootNode(listenerMock);

		domObserver.run();

		Mockito.verify(listenerMock).onDOMLoadFailure(Matchers.<Throwable>any());
	}

	@Test(expected = XPathExpressionException.class)
	public void testThatExceptionIsThrownIfXPathExpressionIsInvalid() throws Exception {

		Mockito.when(nodeProviderMock.get()).thenReturn(createDOM(CONFIG_1));

		Node oldDOM = (Node) domObserver.updateCurrentDOM().getFirst();
		domObserver.getScopedChanges(oldDOM, "hello, world", XPathConstants.NODE);
	}

	@Test
	public void testThatListenersAreNotifiedIfXPathExpressionIsInvalid() throws Exception {

		Mockito.when(nodeProviderMock.get()).thenReturn(createDOM(CONFIG_1));

		Mockito.when(listenerMock.getXPathExpression()).thenReturn("hello, world");
		Mockito.when(listenerMock.getQName()).thenReturn(XPathConstants.NODE);
		domObserver.addListener(listenerMock);

		domObserver.run();

		Mockito.verify(listenerMock).onXPathEvaluationFailure(Matchers.<XPathExpressionException>any());
	}

	/**
	 * https://github.com/itm/itm-utils/issues/26
	 *
	 * @throws Exception
	 * 		if the test fails
	 */
	@Test
	public void testNoLoadingAndEvaluationWillBeDoneUnlessAtLeastOneListenerIsRegistered() throws Exception {

		domObserver.run();
		Mockito.verify(nodeProviderMock, Mockito.never()).get();

		domObserver.addListener(listenerMock);
		domObserver.run();
		Mockito.verify(nodeProviderMock).get();
	}

	/**
	 * https://github.com/itm/itm-utils/issues/27
	 *
	 * @throws Exception
	 * 		if the test fails
	 */
	@Test
	public void testIfListenerGetsFullDiffToNullStateEvenIfNoChangesOccurred() throws Exception {

		Mockito.when(nodeProviderMock.get()).thenReturn(createDOM(CONFIG_1));
		Node oldDOM = (Node) domObserver.updateCurrentDOM().getFirst();
		domObserver.getScopedChanges(
				oldDOM,
				XPATH_EXPRESSION_ROOT_NODE,
				XPathConstants.NODE
		);

		subscribeToRootNode(listenerMock);
		domObserver.run();

		ArgumentCaptor<DOMTuple> argumentCaptor = ArgumentCaptor.forClass(DOMTuple.class);
		Mockito.verify(listenerMock).onDOMChanged(argumentCaptor.capture());
		assertNull(argumentCaptor.getValue().getFirst());
		Assert.assertNotNull(argumentCaptor.getValue().getSecond());
	}

	/**
	 * https://github.com/itm/itm-utils/issues/27
	 *
	 * @throws Exception
	 * 		if the test fails
	 */
	@Test
	public void testIfSecondListenerGetsFullDiffAlthoughFirstListenerGetsNone() throws Exception {

		Mockito.when(nodeProviderMock.get()).thenReturn(createDOM(CONFIG_1));

		subscribeToRootNode(listenerMock);
		domObserver.run();

		subscribeToRootNode(listenerMock2);
		domObserver.run();

		// assert listenerMock1 still has only one invocation (the one from before)
		Mockito.verify(listenerMock).onDOMChanged(Matchers.<DOMTuple>any());

		// check if listenerMock2 has been notified
		ArgumentCaptor<DOMTuple> argumentCaptorListener2 = ArgumentCaptor.forClass(DOMTuple.class);
		Mockito.verify(listenerMock2).onDOMChanged(argumentCaptorListener2.capture());
		assertNull(argumentCaptorListener2.getValue().getFirst());
		Assert.assertNotNull(argumentCaptorListener2.getValue().getSecond());
	}

	private void subscribeToRootNode(final DOMObserverListener listenerMock) {
		setRootNodeSubscriptionBehaviour(listenerMock);
		domObserver.addListener(listenerMock);
	}

	private void setRootNodeSubscriptionBehaviour(final DOMObserverListener listenerMock) {
		Mockito.when(listenerMock.getXPathExpression()).thenReturn(XPATH_EXPRESSION_ROOT_NODE);
		Mockito.when(listenerMock.getQName()).thenReturn(XPathConstants.NODE);
	}
}
