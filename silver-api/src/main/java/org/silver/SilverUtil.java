/**
 * Copyright 2014 John Ericksen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.silver;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author John Ericksen
 */
public final class SilverUtil {

    public static final String SILVER_NAME = "Silver";
    public static final String SIlVER_REPOSITORY_NAME = "Silver$Repsitory";
    public static final String SIlVER_PACKAGE = "org.silver";
    public static final String IMPL_EXT = "SilverImpl";

    private static final ParcelCodeRepository REPOSITORY = new ParcelCodeRepository();

    private SilverUtil(){
        // private utility class constructor
    }

    /**
     * Gets the implementation of a `@Silver` annotated class
     *
     * @throws SilverRuntimeException if there was an error looking up the wrapped Silver$Repository class.
     * @param input Silver interface
     * @return Silver implementation
     */
    @SuppressWarnings("unchecked")
    public static <T> T get(Class<T> input) {
        SilverImplementation<T> parcelableFactory = REPOSITORY.get(input);

        return parcelableFactory.build();
    }

    /**
     * Factory class for building a `Parcelable` from the given input.
     */
    public interface SilverImplementation<T> {

        String BUILD = "build";

        /**
         * Build the corresponding `Parcelable` class.
         *
         * @return Parcelable instance
         */
        T build();
    }

    private static final class SilverImplReflectionProxy<T> implements SilverImplementation<T> {

        private final Constructor<T> constructor;

        public SilverImplReflectionProxy(Class<T> parcelWrapperClass) {
            try {
                this.constructor = parcelWrapperClass.getConstructor();
            } catch (NoSuchMethodException e) {
                throw new SilverRuntimeException("Unable to create ParcelFactory Type", e);
            }
        }

        @Override
        public T build() {
            try {
                return constructor.newInstance();
            } catch (InstantiationException e) {
                throw new SilverRuntimeException("Unable to create ParcelFactory Type", e);
            } catch (IllegalAccessException e) {
                throw new SilverRuntimeException("Unable to create ParcelFactory Type", e);
            } catch (InvocationTargetException e) {
                throw new SilverRuntimeException("Unable to create ParcelFactory Type", e);
            }
        }
    }

    private static final class ParcelCodeRepository {

        private ConcurrentMap<Class, SilverImplementation> generatedMap = new ConcurrentHashMap<Class, SilverImplementation>();

        public ParcelCodeRepository() {
            loadRepository(getClass().getClassLoader());
        }

        public <T> SilverImplementation<T> get(Class<T> clazz){
            SilverImplementation result = generatedMap.get(clazz);
            if (result == null) {
                SilverImplementation value = findClass(clazz);
                if(value == null){
                    throw new SilverRuntimeException("Unable to create SilverImplementation for " + clazz.getName());
                }
                result = generatedMap.putIfAbsent(clazz, value);
                if (result == null) {
                    result = value;
                }
            }

            return result;
        }

        @SuppressWarnings("unchecked")
        public SilverImplementation findClass(Class clazz){
            try {
                Class parcelWrapperClass = Class.forName(clazz.getName() + "$$" + IMPL_EXT);
                return new SilverImplReflectionProxy(parcelWrapperClass);
            } catch (ClassNotFoundException e) {
                return null;
            }
        }

        /**
         * Update the repository class from the given classloader.  If the given repository class cannot be instantiated
         * then this method will throw a SilverRuntimeException.
         *
         * @throws SilverRuntimeException
         * @param classLoader
         */
        @SuppressWarnings("unchecked")
        public void loadRepository(ClassLoader classLoader){
            try{
                Class repositoryClass = classLoader.loadClass(SIlVER_PACKAGE + "." + SIlVER_REPOSITORY_NAME);
                Repository instance = (Repository) repositoryClass.newInstance();
                generatedMap.putAll(instance.get());

            } catch (ClassNotFoundException e) {
                //nothing
            } catch (InstantiationException e) {
                throw new SilverRuntimeException("Unable to instantiate generated Repository", e);
            } catch (IllegalAccessException e) {
                throw new SilverRuntimeException("Unable to access generated Repository", e);
            }
        }
    }

    /**
     * @author John Ericksen
     */
    public interface Repository<T> {

        Map<Class, T> get();
    }

}
