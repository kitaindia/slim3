/*
 * Copyright 2004-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.slim3.datastore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.slim3.util.ClassUtil;
import org.slim3.util.Cleanable;
import org.slim3.util.Cleaner;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.DatastoreTimeoutException;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.EntityTranslator;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.KeyRange;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Transaction;
import com.google.storage.onestore.v3.OnestoreEntity.EntityProto;
import com.google.storage.onestore.v3.OnestoreEntity.Reference;
import com.google.storage.onestore.v3.OnestoreEntity.Path.Element;

/**
 * A utility for {@link DatastoreService}.
 * 
 * @author higa
 * @since 3.0
 * 
 */
public final class DatastoreUtil {

    private static final int MAX_RETRY = 10;

    private static Logger logger =
        Logger.getLogger(DatastoreUtil.class.getName());

    private static ConcurrentHashMap<String, ModelMeta<?>> modelMetaCache =
        new ConcurrentHashMap<String, ModelMeta<?>>(87);

    private static volatile boolean initialized = false;

    static {
        initialize();
    }

    private static void initialize() {
        Cleaner.add(new Cleanable() {
            public void clean() {
                modelMetaCache.clear();
                initialized = false;
            }
        });
        initialized = true;
    }

    /**
     * Begins a transaction.
     * 
     * @return a begun transaction
     */
    public static Transaction beginTransaction() {
        DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
        try {
            return ds.beginTransaction();
        } catch (DatastoreTimeoutException e) {
            logger.log(Level.WARNING, e.getMessage(), e);
            for (int i = 0; i < MAX_RETRY; i++) {
                try {
                    return ds.beginTransaction();
                } catch (DatastoreTimeoutException e2) {
                    logger.log(Level.WARNING, "Retry("
                        + i
                        + "): "
                        + e2.getMessage(), e2);
                }
            }
            throw e;
        }
    }

    /**
     * Commits the transaction.
     * 
     * @param tx
     *            the transaction
     * @throws NullPointerException
     *             if the tx parameter is null
     * @throws IllegalArgumentException
     *             if the transaction is not active
     */
    public static void commit(Transaction tx) throws NullPointerException,
            IllegalArgumentException {
        if (tx == null) {
            throw new NullPointerException("The tx parameter must not be null.");
        }
        if (!tx.isActive()) {
            throw new IllegalArgumentException(
                "The transaction must be active.");
        }
        try {
            tx.commit();
        } catch (DatastoreTimeoutException e) {
            logger.log(Level.WARNING, e.getMessage(), e);
            for (int i = 0; i < MAX_RETRY; i++) {
                try {
                    tx.commit();
                    return;
                } catch (DatastoreTimeoutException e2) {
                    logger.log(Level.WARNING, "Retry("
                        + i
                        + "): "
                        + e2.getMessage(), e2);
                }
            }
            throw e;
        }
    }

    /**
     * Rolls back the transaction.
     * 
     * @param tx
     *            the transaction
     * @throws NullPointerException
     *             if the tx parameter is null
     * @throws IllegalArgumentException
     *             if the transaction is not active
     */
    public static void rollback(Transaction tx) throws NullPointerException,
            IllegalArgumentException {
        if (tx == null) {
            throw new NullPointerException("The tx parameter is null.");
        }
        if (!tx.isActive()) {
            throw new IllegalArgumentException(
                "The transaction must be active.");
        }
        try {
            tx.rollback();
        } catch (DatastoreTimeoutException e) {
            logger.log(Level.WARNING, e.getMessage(), e);
            for (int i = 0; i < MAX_RETRY; i++) {
                try {
                    tx.rollback();
                    return;
                } catch (DatastoreTimeoutException e2) {
                    logger.log(Level.WARNING, "Retry("
                        + i
                        + "): "
                        + e2.getMessage(), e2);
                }
            }
            throw e;
        }
    }

    /**
     * Allocates keys within a namespace defined by the kind.
     * 
     * @param kind
     *            the kind
     * @param num
     *            the number of allocated keys
     * @return keys within a namespace defined by the kind
     * @throws NullPointerException
     *             if the kind parameter is null
     */
    public static KeyRange allocateIds(String kind, long num)
            throws NullPointerException {
        if (kind == null) {
            throw new NullPointerException("The kind parameter is null.");
        }
        DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
        try {
            return ds.allocateIds(kind, num);
        } catch (DatastoreTimeoutException e) {
            logger.log(Level.WARNING, e.getMessage(), e);
            for (int i = 0; i < MAX_RETRY; i++) {
                try {
                    return ds.allocateIds(kind, num);
                } catch (DatastoreTimeoutException e2) {
                    logger.log(Level.WARNING, "Retry("
                        + i
                        + "): "
                        + e2.getMessage(), e2);
                }
            }
            throw e;
        }
    }

    /**
     * Allocates keys within a namespace defined by the parent key and the kind.
     * 
     * @param parentKey
     *            the parent key
     * @param kind
     *            the kind
     * @param num
     * @return keys within a namespace defined by the parent key and the kind
     * @throws NullPointerException
     *             if the parentKey parameter is null or if the kind parameter
     *             is null
     */
    public static KeyRange allocateIds(Key parentKey, String kind, int num)
            throws NullPointerException {
        if (parentKey == null) {
            throw new NullPointerException("The parentKey parameter is null.");
        }
        if (kind == null) {
            throw new NullPointerException("The kind parameter is null.");
        }
        DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
        try {
            return ds.allocateIds(parentKey, kind, num);
        } catch (DatastoreTimeoutException e) {
            logger.log(Level.WARNING, e.getMessage(), e);
            for (int i = 0; i < MAX_RETRY; i++) {
                try {
                    return ds.allocateIds(parentKey, kind, num);
                } catch (DatastoreTimeoutException e2) {
                    logger.log(Level.WARNING, "Retry("
                        + i
                        + "): "
                        + e2.getMessage(), e2);
                }
            }
            throw e;
        }
    }

    /**
     * Returns an entity specified by the key. If there is a current
     * transaction, this operation will execute within that transaction.
     * 
     * @param key
     *            the key
     * @return an entity specified by the key
     * @throws NullPointerException
     *             if the key parameter is null
     * @throws EntityNotFoundRuntimeException
     *             if no entity specified by the key could be found
     */
    public static Entity get(Key key) throws NullPointerException,
            EntityNotFoundRuntimeException {
        if (key == null) {
            throw new NullPointerException("The key parameter is null.");
        }
        DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
        try {
            try {
                return ds.get(key);
            } catch (DatastoreTimeoutException e) {
                logger.log(Level.WARNING, e.getMessage(), e);
                for (int i = 0; i < MAX_RETRY; i++) {
                    try {
                        return ds.get(key);
                    } catch (DatastoreTimeoutException e2) {
                        logger.log(Level.WARNING, "Retry("
                            + i
                            + "): "
                            + e2.getMessage(), e2);
                    }
                }
                throw e;
            }
        } catch (EntityNotFoundException cause) {
            throw new EntityNotFoundRuntimeException(key, cause);
        }
    }

    /**
     * Returns an entity specified by the key within the provided transaction.
     * 
     * @param tx
     *            the transaction
     * @param key
     *            the key
     * @return an entity specified by the key
     * @throws NullPointerException
     *             if the key parameter is null
     * @throws IllegalStateException
     *             if the transaction is not null and the transaction is not
     *             active
     * @throws EntityNotFoundRuntimeException
     *             if no entity specified by the key could be found
     */
    public static Entity get(Transaction tx, Key key)
            throws NullPointerException, IllegalStateException,
            EntityNotFoundRuntimeException {
        if (key == null) {
            throw new NullPointerException("The key parameter is null.");
        }
        if (tx != null && !tx.isActive()) {
            throw new IllegalStateException("The transaction must be active.");
        }
        DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
        try {
            try {
                return ds.get(tx, key);
            } catch (DatastoreTimeoutException e) {
                logger.log(Level.WARNING, e.getMessage(), e);
                for (int i = 0; i < MAX_RETRY; i++) {
                    try {
                        return ds.get(tx, key);
                    } catch (DatastoreTimeoutException e2) {
                        logger.log(Level.WARNING, "Retry("
                            + i
                            + "): "
                            + e2.getMessage(), e2);
                    }
                }
                throw e;
            }
        } catch (EntityNotFoundException cause) {
            throw new EntityNotFoundRuntimeException(key, cause);
        }
    }

    /**
     * Returns entities specified by the keys. If there is a current
     * transaction, this operation will execute within that transaction.
     * 
     * @param keys
     *            the keys
     * @return entities specified by the keys
     * @throws NullPointerException
     *             if the keys parameter is null
     */
    public static Map<Key, Entity> get(Iterable<Key> keys)
            throws NullPointerException {
        if (keys == null) {
            throw new NullPointerException("The keys parameter is null.");
        }
        DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
        try {
            return ds.get(keys);
        } catch (DatastoreTimeoutException e) {
            logger.log(Level.WARNING, e.getMessage(), e);
            for (int i = 0; i < MAX_RETRY; i++) {
                try {
                    return ds.get(keys);
                } catch (DatastoreTimeoutException e2) {
                    logger.log(Level.WARNING, "Retry("
                        + i
                        + "): "
                        + e2.getMessage(), e2);
                }
            }
            throw e;
        }
    }

    /**
     * Returns entities specified by the keys within the provided transaction.
     * 
     * @param tx
     *            the transaction
     * @param keys
     *            the keys
     * @return entities specified by the keys
     * @throws NullPointerException
     *             if the keys parameter is null
     * @throws IllegalStateException
     *             if the transaction is not null and the transaction is not
     *             active
     */
    public static Map<Key, Entity> get(Transaction tx, Iterable<Key> keys)
            throws NullPointerException, IllegalStateException {
        if (keys == null) {
            throw new NullPointerException("The keys parameter is null.");
        }
        if (tx != null && !tx.isActive()) {
            throw new IllegalStateException("The transaction must be active.");
        }
        DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
        try {
            return ds.get(tx, keys);
        } catch (DatastoreTimeoutException e) {
            logger.log(Level.WARNING, e.getMessage(), e);
            for (int i = 0; i < MAX_RETRY; i++) {
                try {
                    return ds.get(tx, keys);
                } catch (DatastoreTimeoutException e2) {
                    logger.log(Level.WARNING, "Retry("
                        + i
                        + "): "
                        + e2.getMessage(), e2);
                }
            }
            throw e;
        }
    }

    /**
     * Puts the entity to datastore. If there is a current transaction, this
     * operation will execute within that transaction.
     * 
     * @param entity
     *            the entity
     * @return a key
     * @throws NullPointerException
     *             if the entity parameter is null
     */
    public static Key put(Entity entity) throws NullPointerException {
        if (entity == null) {
            throw new NullPointerException("The entity parameter is null.");
        }
        DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
        try {
            return ds.put(entity);
        } catch (DatastoreTimeoutException e) {
            logger.log(Level.WARNING, e.getMessage(), e);
            for (int i = 0; i < MAX_RETRY; i++) {
                try {
                    return ds.put(entity);
                } catch (DatastoreTimeoutException e2) {
                    logger.log(Level.WARNING, "Retry("
                        + i
                        + "): "
                        + e2.getMessage(), e2);
                }
            }
            throw e;
        }
    }

    /**
     * Puts the entity to datastore within the provided transaction.
     * 
     * @param tx
     *            the transaction
     * @param entity
     *            the entity
     * @return a key
     * @throws NullPointerException
     *             if the entity parameter is null
     * @throws IllegalStateException
     *             if the transaction is not null and the transaction is not
     *             active
     */
    public static Key put(Transaction tx, Entity entity)
            throws NullPointerException, IllegalStateException {
        if (entity == null) {
            throw new NullPointerException("The entity parameter is null.");
        }
        if (tx != null && !tx.isActive()) {
            throw new IllegalStateException("The transaction must be active.");
        }
        DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
        try {
            return ds.put(tx, entity);
        } catch (DatastoreTimeoutException e) {
            logger.log(Level.WARNING, e.getMessage(), e);
            for (int i = 0; i < MAX_RETRY; i++) {
                try {
                    return ds.put(tx, entity);
                } catch (DatastoreTimeoutException e2) {
                    logger.log(Level.WARNING, "Retry("
                        + i
                        + "): "
                        + e2.getMessage(), e2);
                }
            }
            throw e;
        }
    }

    /**
     * Puts the entities to datastore. If there is a current transaction, this
     * operation will execute within that transaction.
     * 
     * @param entities
     *            the entities
     * @return a list of keys
     * @throws NullPointerException
     *             if the entities parameter is null
     */
    public static List<Key> put(Iterable<Entity> entities)
            throws NullPointerException {
        if (entities == null) {
            throw new NullPointerException("The entities parameter is null.");
        }
        DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
        try {
            return ds.put(entities);
        } catch (DatastoreTimeoutException e) {
            logger.log(Level.WARNING, e.getMessage(), e);
            for (int i = 0; i < MAX_RETRY; i++) {
                try {
                    return ds.put(entities);
                } catch (DatastoreTimeoutException e2) {
                    logger.log(Level.WARNING, "Retry("
                        + i
                        + "): "
                        + e2.getMessage(), e2);
                }
            }
            throw e;
        }
    }

    /**
     * Puts the entities to datastore within the provided transaction.
     * 
     * @param tx
     *            the transaction
     * @param entities
     *            the entities
     * @return a list of keys
     * @throws NullPointerException
     *             if the entities parameter is null
     * @throws IllegalStateException
     *             if the transaction is not null and the transaction is not
     *             active
     */
    public static List<Key> put(Transaction tx, Iterable<Entity> entities)
            throws NullPointerException, IllegalStateException {
        if (entities == null) {
            throw new NullPointerException("The entities parameter is null.");
        }
        if (tx != null && !tx.isActive()) {
            throw new IllegalStateException("The transaction must be active.");
        }
        DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
        try {
            return ds.put(tx, entities);
        } catch (DatastoreTimeoutException e) {
            logger.log(Level.WARNING, e.getMessage(), e);
            for (int i = 0; i < MAX_RETRY; i++) {
                try {
                    return ds.put(tx, entities);
                } catch (DatastoreTimeoutException e2) {
                    logger.log(Level.WARNING, "Retry("
                        + i
                        + "): "
                        + e2.getMessage(), e2);
                }
            }
            throw e;
        }
    }

    /**
     * Deletes entities specified by the keys. If there is a current
     * transaction, this operation will execute within that transaction.
     * 
     * @param keys
     *            the keys
     * @throws NullPointerException
     *             if the keys parameter is null
     */
    public static void delete(Iterable<Key> keys) throws NullPointerException {
        if (keys == null) {
            throw new NullPointerException("The keys parameter is null.");
        }
        DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
        try {
            ds.delete(keys);
        } catch (DatastoreTimeoutException e) {
            logger.log(Level.WARNING, e.getMessage(), e);
            for (int i = 0; i < MAX_RETRY; i++) {
                try {
                    ds.delete(keys);
                    return;
                } catch (DatastoreTimeoutException e2) {
                    logger.log(Level.WARNING, "Retry("
                        + i
                        + "): "
                        + e2.getMessage(), e2);
                }
            }
            throw e;
        }
    }

    /**
     * Deletes entities specified by the keys within the provided transaction.
     * 
     * @param tx
     *            the transaction
     * @param keys
     *            the keys
     * @throws NullPointerException
     *             if the keys parameter is null
     * @throws IllegalStateException
     *             if the transaction is not null and the transaction is not
     *             active
     */
    public static void delete(Transaction tx, Iterable<Key> keys)
            throws NullPointerException, IllegalStateException {
        if (keys == null) {
            throw new NullPointerException("The keys parameter is null.");
        }
        if (tx != null && !tx.isActive()) {
            throw new IllegalStateException("The transaction must be active.");
        }
        DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
        try {
            ds.delete(tx, keys);
        } catch (DatastoreTimeoutException e) {
            logger.log(Level.WARNING, e.getMessage(), e);
            for (int i = 0; i < MAX_RETRY; i++) {
                try {
                    ds.delete(tx, keys);
                    return;
                } catch (DatastoreTimeoutException e2) {
                    logger.log(Level.WARNING, "Retry("
                        + i
                        + "): "
                        + e2.getMessage(), e2);
                }
            }
            throw e;
        }
    }

    /**
     * Prepares the query.
     * 
     * @param ds
     *            the datastore
     * @param query
     *            the query
     * @return a prepared query.
     * @throws NullPointerException
     *             if the ds parameter is null or if the query parameter is null
     */
    public static PreparedQuery prepare(DatastoreService ds, Query query)
            throws NullPointerException {
        if (ds == null) {
            throw new NullPointerException("The ds parameter is null.");
        }
        if (query == null) {
            throw new NullPointerException("The query parameter is null.");
        }
        try {
            return ds.prepare(query);
        } catch (DatastoreTimeoutException e) {
            logger.log(Level.WARNING, e.getMessage(), e);
            for (int i = 0; i < MAX_RETRY; i++) {
                try {
                    return ds.prepare(query);
                } catch (DatastoreTimeoutException e2) {
                    logger.log(Level.WARNING, "Retry("
                        + i
                        + "): "
                        + e2.getMessage(), e2);
                }
            }
            throw e;
        }
    }

    /**
     * Prepares the query.
     * 
     * @param ds
     *            the datastore
     * @param tx
     *            the transaction
     * @param query
     *            the query
     * @return a prepared query.
     * @throws NullPointerException
     *             if the ds parameter is null or if tx parameter is null or if
     *             the query parameter is null
     */
    public static PreparedQuery prepare(DatastoreService ds, Transaction tx,
            Query query) throws NullPointerException {
        if (ds == null) {
            throw new NullPointerException("The ds parameter is null.");
        }
        if (query == null) {
            throw new NullPointerException("The query parameter is null.");
        }
        try {
            return ds.prepare(tx, query);
        } catch (DatastoreTimeoutException e) {
            logger.log(Level.WARNING, e.getMessage(), e);
            for (int i = 0; i < MAX_RETRY; i++) {
                try {
                    return ds.prepare(tx, query);
                } catch (DatastoreTimeoutException e2) {
                    logger.log(Level.WARNING, "Retry("
                        + i
                        + "): "
                        + e2.getMessage(), e2);
                }
            }
            throw e;
        }
    }

    /**
     * Returns a list of entities.
     * 
     * @param preparedQuery
     *            the prepared query
     * @param fetchOptions
     *            the fetch options
     * @return a list of entities
     * @throws NullPointerException
     *             if the preparedQuery parameter is null or if the fetchOptions
     *             parameter is null
     */
    public static List<Entity> asList(PreparedQuery preparedQuery,
            FetchOptions fetchOptions) throws NullPointerException {
        if (preparedQuery == null) {
            throw new NullPointerException(
                "The preparedQuery parameter is null.");
        }
        if (fetchOptions == null) {
            throw new NullPointerException(
                "The fetchOptions parameter is null.");
        }
        try {
            return preparedQuery.asList(fetchOptions);
        } catch (DatastoreTimeoutException e) {
            logger.log(Level.WARNING, e.getMessage(), e);
            for (int i = 0; i < MAX_RETRY; i++) {
                try {
                    return preparedQuery.asList(fetchOptions);
                } catch (DatastoreTimeoutException e2) {
                    logger.log(Level.WARNING, "Retry("
                        + i
                        + "): "
                        + e2.getMessage(), e2);
                }
            }
            throw e;
        }
    }

    /**
     * Returns a single entity.
     * 
     * @param preparedQuery
     *            the query
     * @return a single entity
     * @throws NullPointerException
     *             if the preparedQuery parameter is null
     */
    public static Entity asSingleEntity(PreparedQuery preparedQuery)
            throws NullPointerException {
        if (preparedQuery == null) {
            throw new NullPointerException(
                "The preparedQuery parameter is null.");
        }
        try {
            return preparedQuery.asSingleEntity();
        } catch (DatastoreTimeoutException e) {
            logger.log(Level.WARNING, e.getMessage(), e);
            for (int i = 0; i < MAX_RETRY; i++) {
                try {
                    return preparedQuery.asSingleEntity();
                } catch (DatastoreTimeoutException e2) {
                    logger.log(Level.WARNING, "Retry("
                        + i
                        + "): "
                        + e2.getMessage(), e2);
                }
            }
            throw e;
        }
    }

    /**
     * Returns a single entity.
     * 
     * @param preparedQuery
     *            the query
     * @param fetchOptions
     *            the fetch options
     * @return a single entity
     * @throws NullPointerException
     *             if the preparedQuery parameter is null or if the fetchOptions
     *             parameter is null
     */
    public static Iterable<Entity> asIterable(PreparedQuery preparedQuery,
            FetchOptions fetchOptions) throws NullPointerException {
        if (preparedQuery == null) {
            throw new NullPointerException(
                "The preparedQuery parameter is null.");
        }
        if (fetchOptions == null) {
            throw new NullPointerException(
                "The fetchOptions parameter is null.");
        }
        try {
            return preparedQuery.asIterable(fetchOptions);
        } catch (DatastoreTimeoutException e) {
            logger.log(Level.WARNING, e.getMessage(), e);
            for (int i = 0; i < MAX_RETRY; i++) {
                try {
                    return preparedQuery.asIterable(fetchOptions);
                } catch (DatastoreTimeoutException e2) {
                    logger.log(Level.WARNING, "Retry("
                        + i
                        + "): "
                        + e2.getMessage(), e2);
                }
            }
            throw e;
        }
    }

    /**
     * Returns a number of entities.
     * 
     * @param preparedQuery
     *            the prepared query
     * @return a number of entities
     * @throws NullPointerException
     *             if the preparedQuery parameter is null
     */
    public static int countEntities(PreparedQuery preparedQuery)
            throws NullPointerException {
        if (preparedQuery == null) {
            throw new NullPointerException(
                "The preparedQuery parameter is null.");
        }
        try {
            return preparedQuery.countEntities();
        } catch (DatastoreTimeoutException e) {
            logger.log(Level.WARNING, e.getMessage(), e);
            for (int i = 0; i < MAX_RETRY; i++) {
                try {
                    return preparedQuery.countEntities();
                } catch (DatastoreTimeoutException e2) {
                    logger.log(Level.WARNING, "Retry("
                        + i
                        + "): "
                        + e2.getMessage(), e2);
                }
            }
            throw e;
        }
    }

    /**
     * Filters the list in memory.
     * 
     * @param <M>
     *            the model type
     * @param list
     *            the model list
     * @param criteria
     *            the filter criteria
     * @return the filtered list.
     * @throws NullPointerException
     *             if the list parameter is null or if the criteria parameter is
     *             null or if the model of the list is null
     */
    public static <M> List<M> filterInMemory(List<M> list,
            List<? extends InMemoryFilterCriterion> criteria)
            throws NullPointerException {
        if (list == null) {
            throw new NullPointerException("The list parameter is null.");
        }
        if (criteria == null) {
            throw new NullPointerException("The criteria parameter is null.");
        }
        if (criteria.size() == 0) {
            return list;
        }
        List<M> newList = new ArrayList<M>(list.size());
        for (M model : list) {
            if (model == null) {
                throw new NullPointerException("The model is null.");
            }
            if (accept(model, criteria)) {
                newList.add(model);
            }
        }
        return newList;
    }

    private static boolean accept(Object model,
            List<? extends InMemoryFilterCriterion> criteria) {
        for (InMemoryFilterCriterion c : criteria) {
            if (c == null) {
                continue;
            }
            if (!c.accept(model)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Sorts the list in memory.
     * 
     * @param <M>
     *            the model type
     * @param list
     *            the model list
     * @param criteria
     *            criteria to sort
     * @return the sorted list
     * @throws NullPointerException
     *             if the list parameter is null of if the criteria parameter is
     *             null
     */
    public static <M> List<M> sortInMemory(List<M> list,
            List<SortCriterion> criteria) throws NullPointerException {
        if (list == null) {
            throw new NullPointerException("The list parameter is null.");
        }
        if (criteria == null) {
            throw new NullPointerException("The criteria parameter is null.");
        }
        if (criteria.size() == 0) {
            return list;
        }
        Collections.sort(list, new AttributeComparator(criteria));
        return list;
    }

    /**
     * Returns a meta data of the model
     * 
     * @param <M>
     *            the model type
     * @param modelClass
     *            the model class
     * @return a meta data of the model
     * @throws NullPointerException
     *             if the modelClass parameter is null
     */
    @SuppressWarnings("unchecked")
    public static <M> ModelMeta<M> getModelMeta(Class<M> modelClass)
            throws NullPointerException {
        if (modelClass == null) {
            throw new NullPointerException("The modelClass parameter is null.");
        }
        if (!initialized) {
            initialize();
        }
        ModelMeta<M> modelMeta =
            (ModelMeta<M>) modelMetaCache.get(modelClass.getName());
        if (modelMeta != null) {
            return modelMeta;
        }
        modelMeta = createModelMeta(modelClass);
        ModelMeta<?> old =
            modelMetaCache.putIfAbsent(modelClass.getName(), modelMeta);
        return old != null ? (ModelMeta<M>) old : modelMeta;
    }

    /**
     * Returns a meta data of the model
     * 
     * @param <M>
     *            the model type
     * @param modelMeta
     *            the meta data of model
     * @param entity
     *            the entity
     * @return a meta data of the model
     * @throws NullPointerException
     *             if the modelMeta parameter is null or if the entity parameter
     *             is null
     * @throws IllegalArgumentException
     *             if the model class is not assignable from entity class
     */
    @SuppressWarnings("unchecked")
    public static <M> ModelMeta<M> getModelMeta(ModelMeta<M> modelMeta,
            Entity entity) throws NullPointerException,
            IllegalArgumentException {
        if (modelMeta == null) {
            throw new NullPointerException("The modelMeta parameter is null.");
        }
        if (entity == null) {
            throw new NullPointerException("The entity parameter is null.");
        }
        List<String> classHierarchyList =
            (List<String>) entity
                .getProperty(ModelMeta.CLASS_HIERARCHY_LIST_RESERVED_PROPERTY);
        if (classHierarchyList == null) {
            return modelMeta;
        }
        Class<M> subModelClass =
            ClassUtil.forName(classHierarchyList
                .get(classHierarchyList.size() - 1));
        if (!modelMeta.getModelClass().isAssignableFrom(subModelClass)) {
            throw new IllegalArgumentException("The model class("
                + modelMeta.getModelClass().getName()
                + ") is not assignable from entity class("
                + subModelClass.getName()
                + ").");
        }
        return getModelMeta(subModelClass);
    }

    /**
     * Creates a meta data of the model
     * 
     * @param <M>
     *            the model type
     * @param modelClass
     *            the model class
     * @return a meta data of the model
     */
    public static <M> ModelMeta<M> createModelMeta(Class<M> modelClass) {
        try {
            String metaClassName =
                modelClass.getName().replace(".model.", ".meta.").replace(
                    ".shared.",
                    ".server.")
                    + "Meta";
            return ClassUtil.newInstance(metaClassName, Thread
                .currentThread()
                .getContextClassLoader());
        } catch (Throwable cause) {
            throw new IllegalArgumentException("The meta data of the model("
                + modelClass.getName()
                + ") is not found.");
        }
    }

    /**
     * Converts the entity to an array of bytes.
     * 
     * @param entity
     *            the entity
     * @return an array of bytes
     * @throws NullPointerException
     *             if the entity parameter is null
     */
    public static byte[] entityToBytes(Entity entity)
            throws NullPointerException {
        if (entity == null) {
            throw new NullPointerException(
                "The entity parameter must not be null.");
        }
        EntityProto pb = EntityTranslator.convertToPb(entity);
        byte[] buf = new byte[pb.encodingSize()];
        pb.outputTo(buf, 0);
        return buf;
    }

    /**
     * Converts the array of bytes to an entity.
     * 
     * @param bytes
     *            the array of bytes
     * @return an entity
     * @throws NullPointerException
     *             if the bytes parameter is null
     */
    public static Entity bytesToEntity(byte[] bytes)
            throws NullPointerException {
        if (bytes == null) {
            throw new NullPointerException(
                "The bytes parameter must not be null.");
        }
        EntityProto pb = new EntityProto();
        pb.mergeFrom(bytes);
        return EntityTranslator.createFromPb(pb);
    }

    /**
     * Converts the reference to a key.
     * 
     * @param reference
     *            the reference object
     * @return a key
     * @throws NullPointerException
     *             if the reference parameter is null
     */
    public static Key referenceToKey(Reference reference)
            throws NullPointerException {
        if (reference == null) {
            throw new NullPointerException(
                "The reference parameter must not be null.");
        }
        Key key = null;
        for (Element e : reference.getPath().elements()) {
            String kind = e.getType();
            long id = e.getId();
            String name = e.getName();
            if (key == null) {
                if (id > 0) {
                    key = KeyFactory.createKey(kind, id);
                } else {
                    key = KeyFactory.createKey(kind, name);
                }
            } else {
                if (id > 0) {
                    key = KeyFactory.createKey(key, kind, id);
                } else {
                    key = KeyFactory.createKey(key, kind, name);
                }
            }
        }
        if (key == null) {
            throw new IllegalArgumentException("The reference("
                + reference
                + ") cannot be converted to Key.");
        }
        return key;
    }

    /**
     * Returns a list of {@link ModelMeta}s.
     * 
     * @param models
     *            the models
     * @return a list of {@link ModelMeta}s
     * @throws NullPointerException
     *             if the models parameter is null or if the element of models
     *             is null
     */
    public static List<ModelMeta<?>> getModelMetaList(Iterable<?> models)
            throws NullPointerException {
        if (models == null) {
            throw new NullPointerException(
                "The models parameter must not be null.");
        }
        List<ModelMeta<?>> list = new ArrayList<ModelMeta<?>>();
        for (Object model : models) {
            if (model == null) {
                throw new NullPointerException(
                    "The element of the models must not be null.");
            }
            if (model instanceof Entity) {
                list.add(null);
            } else {
                list.add(getModelMeta(model.getClass()));
            }
        }
        return list;
    }

    /**
     * Updates the properties of the model and convert it to an entity.
     * 
     * @param modelMeta
     *            the meta data of the model
     * @param model
     *            the model
     * @return an entity
     * @throws NullPointerException
     *             if the modelMeta parameter is null or if the model parameter
     *             is null
     */
    public static Entity updatePropertiesAndConvertToEntity(
            ModelMeta<?> modelMeta, Object model) throws NullPointerException {
        if (modelMeta == null) {
            throw new NullPointerException(
                "The modelMeta parameter must not be null.");
        }
        if (model == null) {
            throw new NullPointerException(
                "The model parameter must not be null.");
        }
        modelMeta.incrementVersion(model);
        return modelMeta.modelToEntity(model);
    }

    /**
     * Updates the properties of the models and convert them to entities.
     * 
     * @param modelMetaList
     *            the list of {@link ModelMeta}
     * @param models
     *            the models
     * @return a list of entities
     * @throws NullPointerException
     *             if the modelMetaList parameter is null or if the models
     *             parameter is null
     */
    public static List<Entity> updatePropertiesAndConvertToEntities(
            List<ModelMeta<?>> modelMetaList, Iterable<?> models)
            throws NullPointerException {
        if (modelMetaList == null) {
            throw new NullPointerException(
                "The modelMetaList parameter must not be null.");
        }
        if (models == null) {
            throw new NullPointerException(
                "The models parameter must not be null.");
        }
        List<Entity> entities = new ArrayList<Entity>(modelMetaList.size());
        int i = 0;
        for (Object model : models) {
            ModelMeta<?> modelMeta = modelMetaList.get(i);
            if (modelMeta == null) {
                entities.add((Entity) model);
            } else {
                Entity entity =
                    updatePropertiesAndConvertToEntity(modelMeta, model);
                entities.add(entity);
            }
            i++;
        }
        return entities;
    }

    /**
     * Sets the keys to the models.
     * 
     * @param modelMetaList
     *            the list of {@link ModelMeta}s
     * @param models
     *            the models
     * @param keys
     *            the keys
     */
    public static void setKeys(List<ModelMeta<?>> modelMetaList,
            Iterable<?> models, List<Key> keys) {
        int i = 0;
        for (Object model : models) {
            ModelMeta<?> modelMeta = modelMetaList.get(i);
            if (modelMeta != null) {
                modelMeta.setKey(model, keys.get(i));
            }
            i++;
        }
    }

    /**
     * Determines if the key is incomplete.
     * 
     * @param key
     *            the key
     * @return whether the key is incomplete
     * @throws NullPointerException
     *             if the key parameter is null
     */
    public static boolean isIncomplete(Key key) throws NullPointerException {
        if (key == null) {
            throw new NullPointerException(
                "The key parameter must not be null.");
        }
        return key.getName() == null && key.getId() <= 0;
    }

    private DatastoreUtil() {
    }
}