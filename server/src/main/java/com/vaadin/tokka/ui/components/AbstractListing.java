/*
 * Copyright 2000-2014 Vaadin Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.tokka.ui.components;

import java.util.LinkedHashSet;
import java.util.Set;

import com.vaadin.server.AbstractExtension;
import com.vaadin.tokka.server.ListingExtension;
import com.vaadin.tokka.server.communication.data.DataCommunicator;
import com.vaadin.tokka.server.communication.data.SelectionModel;
import com.vaadin.tokka.server.communication.data.TypedDataGenerator;
import com.vaadin.ui.AbstractComponent;

/**
 * Base class for Listing components. Provides common handling for
 * {@link DataCommunicator}, {@link SelectionModel} and {@link TypedDataGenerator}s.
 * 
 * @param <T>
 *            listing data type
 */
public abstract class AbstractListing<T> extends AbstractComponent implements
        Listing<T> {

    /**
     * Helper base class for creating extensions for Listing components. This
     * class provides helpers for accessing the underlying parts of the
     * component and it's communicational mechanism.
     *
     * @param <T>
     *            listing data type
     */
    public abstract static class AbstractListingExtension<T> extends
            AbstractExtension implements ListingExtension<T>,
            TypedDataGenerator<T> {

        /**
         * {@inheritDoc}
         * <p>
         * Note: AbstractListingExtensions need parent to be of type
         * AbstractListing.
         * 
         * @throws IllegalArgument
         *             if parent is not an AbstractListing
         */
        @Override
        public void extend(Listing<T> listing) {
            if (listing instanceof AbstractListing) {
                AbstractListing<T> parent = (AbstractListing<T>) listing;
                super.extend(parent);
                parent.addDataGenerator(this);
            } else {
                throw new IllegalArgumentException(
                        "Parent needs to extend AbstractListing");
            }
        }

        @Override
        public void remove() {
            getParent().removeDataGenerator(this);

            super.remove();
        }

        /**
         * Gets a data object based on it's client-side identifier key.
         * 
         * @param key
         *            key for data object
         * @return the data object
         */
        protected T getData(String key) {
            DataCommunicator<T> dataProvider = getParent().getDataCommunicator();
            if (dataProvider != null) {
                return dataProvider.getKeyMapper().get(key);
            }
            return null;
        }

        @Override
        @SuppressWarnings("unchecked")
        public AbstractListing<T> getParent() {
            return (AbstractListing<T>) super.getParent();
        }

        /**
         * Helper method for refreshing a single data object.
         * 
         * @param data
         *            data object to refresh
         */
        protected void refresh(T data) {
            DataCommunicator<T> dataProvider = getParent().getDataCommunicator();
            if (dataProvider != null) {
                dataProvider.refresh(data);
            }
        }
    }

    /* DataProvider for this Listing component */
    private DataCommunicator<T> dataCommunicator;
    /* TypedDataGenerators used by this Listing */
    private Set<TypedDataGenerator<T>> generators = new LinkedHashSet<>();
    /* SelectionModel for this Listing */
    private SelectionModel<T> selectionModel;

    /**
     * Adds a {@link TypedDataGenerator} for the {@link DataCommunicator} of this
     * Listing component.
     * 
     * @param generator
     *            typed data generator
     */
    protected void addDataGenerator(TypedDataGenerator<T> generator) {
        generators.add(generator);

        if (dataCommunicator != null) {
            dataCommunicator.addDataGenerator(generator);
        }
    }

    /**
     * Removed a {@link TypedDataGenerator} from the {@link DataCommunicator} of
     * this Listing component.
     * 
     * @param generator
     *            typed data generator
     */
    protected void removeDataGenerator(TypedDataGenerator<T> generator) {
        generators.remove(generator);

        if (dataCommunicator != null) {
            dataCommunicator.removeDataGenerator(generator);
        }
    }

    /**
     * Extends this listing component with a data provider. This method
     * reapplies all data generators to the new data provider.
     * 
     * @param dataProvider
     *            new data provider
     */
    protected void setDataCommunicator(DataCommunicator<T> dataProvider) {
        if (this.dataCommunicator == dataProvider) {
            return;
        }

        if (this.dataCommunicator != null) {
            this.dataCommunicator.remove();
        }

        this.dataCommunicator = dataProvider;
        if (dataProvider != null) {
            addExtension(dataProvider);

            if (dataProvider != null) {
                // Reapply all data generators to the new data provider.
                for (TypedDataGenerator<T> generator : generators) {
                    dataProvider.addDataGenerator(generator);
                }
            }
        }
    }

    /**
     * Get the {@link DataCommunicator} of this Listing component.
     * 
     * @return data provider
     */
    protected DataCommunicator<T> getDataCommunicator() {
        return dataCommunicator;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setSelectionModel(SelectionModel<T> model) {
        if (selectionModel != null) {
            selectionModel.remove();
        }
        selectionModel = model;
        if (model != null) {
            model.extend(this);
        }
    }

    @Override
    public SelectionModel<T> getSelectionModel() {
        return selectionModel;
    }
}