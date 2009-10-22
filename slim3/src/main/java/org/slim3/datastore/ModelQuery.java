/*
 * Copyright the original author or authors.
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
import java.util.List;

import org.slim3.util.ConversionUtil;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.PreparedQuery;

/**
 * A query class for select.
 * 
 * @author higa
 * @param <M>
 *            the model type
 * @since 3.0
 * 
 */
public class ModelQuery<M> extends AbstractQuery<ModelQuery<M>> {

    /**
     * The meta data of model.
     */
    protected ModelMeta<M> modelMeta;

    /**
     * Constructor.
     * 
     * @param modelMeta
     *            the meta data of model
     * @throws NullPointerException
     *             if the modelMeta parameter is null
     */
    public ModelQuery(ModelMeta<M> modelMeta) throws NullPointerException {
        if (modelMeta == null) {
            throw new NullPointerException("The modelMeta parameter is null.");
        }
        this.modelMeta = modelMeta;
        setUpQuery(modelMeta.getKind());
    }

    /**
     * Constructor.
     * 
     * @param modelMeta
     *            the meta data of model
     * @param ancestorKey
     *            the ancestor key
     * @throws NullPointerException
     *             if the modelMeta parameter is null or if the ancestorKey
     *             parameter is null
     */
    public ModelQuery(ModelMeta<M> modelMeta, Key ancestorKey)
            throws NullPointerException {
        if (modelMeta == null) {
            throw new NullPointerException("The modelMeta parameter is null.");
        }
        if (ancestorKey == null) {
            throw new NullPointerException("The ancestorKey parameter is null.");
        }
        this.modelMeta = modelMeta;
        setUpQuery(modelMeta.getKind(), ancestorKey);
    }

    /**
     * Adds the filter criteria.
     * 
     * @param criteria
     *            the filter criteria
     * @return this instance
     * @throws IllegalArgumentException
     *             if the model of the criterion is different from the model of
     *             this query
     */
    public ModelQuery<M> filter(FilterCriterion... criteria)
            throws IllegalArgumentException {
        for (FilterCriterion c : criteria) {
            if (c != null) {
                if (c instanceof AbstractCriterion) {
                    ModelMeta<?> mm =
                        AbstractCriterion.class.cast(c).attributeMeta.modelMeta;
                    if (mm.getModelClass() != modelMeta.getModelClass()) {
                        throw new IllegalArgumentException("The model("
                            + mm.getModelClass().getName()
                            + ") of the criterion is different from the model("
                            + modelMeta.getModelClass().getName()
                            + ") of this query.");
                    }
                }
                c.apply(query);
            }
        }
        return this;
    }

    /**
     * Adds the sort criteria.
     * 
     * @param criteria
     *            the sort criteria
     * @return this instance
     */
    public ModelQuery<M> sort(SortCriterion... criteria) {
        for (SortCriterion c : criteria) {
            c.apply(query);
        }
        return this;
    }

    /**
     * Returns the result as a list.
     * 
     * @return the result as a list
     */
    public List<M> asList() {
        List<Entity> entityList = asEntityList();
        List<M> ret = new ArrayList<M>(entityList.size());
        for (Entity e : entityList) {
            ret.add(modelMeta.entityToModel(e));
        }
        return ret;
    }

    /**
     * Returns the single result or null if no entities match.
     * 
     * @return the single result
     * @throws PreparedQuery.TooManyResultsException
     *             if the results are multiple
     * 
     */
    public M asSingle() throws PreparedQuery.TooManyResultsException {
        Entity entity = asSingleEntity();
        if (entity == null) {
            return null;
        }
        return modelMeta.entityToModel(entity);
    }

    /**
     * Return a minimum value of the property. The value does not include null.
     * 
     * @param <A>
     *            the attribute type
     * @param attributeMeta
     *            the meta data of attribute
     * @return a minimum value of the property
     * @throws NullPointerException
     *             if the attributeMeta parameter is null
     */
    @SuppressWarnings("unchecked")
    public <A> A min(CoreAttributeMeta<M, A> attributeMeta)
            throws NullPointerException {
        if (attributeMeta == null) {
            throw new NullPointerException(
                "The attributeMeta parameter is null.");
        }
        Object value = super.min(attributeMeta.getName());
        return (A) ConversionUtil.convert(value, attributeMeta
            .getAttributeClass());
    }

    /**
     * Return a maximum value of the property.
     * 
     * @param <A>
     *            the attribute type
     * @param attributeMeta
     *            the meta data of attribute
     * @return a maximum value of the property
     * @throws NullPointerException
     *             if the attributeMeta parameter is null
     */
    @SuppressWarnings("unchecked")
    public <A> A max(CoreAttributeMeta<M, A> attributeMeta)
            throws NullPointerException {
        if (attributeMeta == null) {
            throw new NullPointerException(
                "The attributeMeta parameter is null.");
        }
        Object value = super.max(attributeMeta.getName());
        return (A) ConversionUtil.convert(value, attributeMeta
            .getAttributeClass());
    }

}