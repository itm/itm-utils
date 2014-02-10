package de.uniluebeck.itm.util.jpa;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.persist.Transactional;
import org.hibernate.Session;
import org.hibernate.engine.spi.SessionImplementor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaQuery;
import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

/**
 * Extensions of this class provide functionality to manage
 * and persist entities in and from a data base by using an {@link EntityManager}
 * @author Sebastian Ebers
 *
 * @param <T> Type of entity to be persisted
 * @param <K> Type of the identifying primary key
 */
public abstract class GenericDaoImpl<T, K extends Serializable> implements GenericDao<T, K> {

	/** The instance which logs messages. */
	private static final Logger log = LoggerFactory.getLogger(GenericDaoImpl.class);

	/** The interface used to interact with the persistence context. */
	@Inject
	private Provider<EntityManager> entityManager;

	/** The class of the managed and persistent entities. */
	private Class<T> entityClass;
	
	
	/**
	 * Constructor
	 * 
	 * @param typeClass
	 *            The class of the managed and persistent entities.
	 */
	@SuppressWarnings("UnusedDeclaration")
	public GenericDaoImpl(final Class<T> typeClass) {
		this.entityClass = typeClass;
	}
	
	/**
	 * Constructor
	 * 
	 * @param entityManager
	 *            The instance used to interact with the persistence context.
	 */
	@Inject
	@SuppressWarnings("UnusedDeclaration")
	public GenericDaoImpl(final Provider<EntityManager> entityManager) {
		this.entityManager = entityManager;
	}
	
	/**
	 * Constructor
	 * 
	 * @param entityManager
	 *            The instance used to interact with the persistence context.
	 * @param typeClass
	 *            The class of the managed and persistent entities.
	 */
	@SuppressWarnings("UnusedDeclaration")
	public GenericDaoImpl(final Provider<EntityManager> entityManager, final Class<T> typeClass) {
		this.entityManager = entityManager;
		this.entityClass = typeClass;
	}


	/**
	 * Constructor inferring and setting the type class of the managed and persistent entities
	 */
	@SuppressWarnings({"unchecked", "UnusedDeclaration"})
	public GenericDaoImpl() {
		Type superclass = getClass().getGenericSuperclass();
		if (superclass instanceof Class) {
			throw new RuntimeException("Missing type parameter.");
		}
		Type type = ((ParameterizedType) superclass).getActualTypeArguments()[0];
		entityClass = (Class<T>) type;
	}

	/**
	 * Returns the interface used to interact with the persistence context.
	 * 
	 * @return the the interface used to interact with the persistence context.
	 */
	@SuppressWarnings("UnusedDeclaration")
	protected final EntityManager getEntityManager() {
		return entityManager.get();
	}

	/**
	 * Returns the class of the managed and persistent entities.
	 * 
	 * @return the class of the managed and persistent entities.
	 */
	@SuppressWarnings("UnusedDeclaration")
	protected final Class<T> getEntityClass() {
		return entityClass;
	}

	@Override
	@Transactional
	public T find(K id) {
		log.debug("Trying to fetch " + entityClass + " instance with id: " + id);
		return entityManager.get().find(entityClass, id);
	}

	@Override
	@Transactional
	public List<T> find() {
		log.debug("Trying to fetch all " + entityClass + " instances from persistence context...");
		final EntityManager em = entityManager.get();
		CriteriaQuery<T> createQuery = em.getEntityManagerFactory().getCriteriaBuilder().createQuery(entityClass);
		createQuery.from(entityClass);
		List<T> resultList = em.createQuery(createQuery).getResultList();
		log.debug(resultList.size()+" entities of class " + entityClass + " fetched from persistence context");
		return resultList;
	}

	@Override
	@Transactional
	public void save(T entity) {
		log.debug("Persisting " + entity);
		entityManager.get().persist(entity);
	}

	@Override
	@Transactional
	public void update(T entity) {
		log.debug("Persisting all local changes of " + entity);
		entityManager.get().merge(entity);
	}

	@Override
	@Transactional
	public void delete(T entity) {
		log.debug("Removing " + entity+" from persistence context");
		entityManager.get().remove(entity);
	}

	@SuppressWarnings("unchecked")
	@Override
	@Transactional
	public K getKey(T entity) {
		Session session = entityManager.get().unwrap(Session.class);
		return (K) session.getSessionFactory().getClassMetadata(entityClass).getIdentifier(entity, (SessionImplementor) session);
	}

	@Override
	@Transactional
	public void refresh(T entity) {
		log.debug("Resetting all properties of " + entity+" from persistence context");
		entityManager.get().refresh(entity);
	}

	@Override
	@Transactional
	public boolean contains(T entity) {
		log.debug("Checking whether " + entity+" consists in persistence context");
		return entityManager.get().contains(entity);
	}

}
