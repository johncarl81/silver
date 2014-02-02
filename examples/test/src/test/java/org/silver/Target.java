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

import org.silver.examples.BaseOne;
import org.silver.examples.BaseTwo;
import org.silver.examples.One;

import java.util.List;
import java.util.Set;

/**
 * @author John Ericksen
 */
@Silver
public interface Target {

    @AnnotatedBy(TestAnnotation.class)
    Set<Class> getAnnotated();

    @Inherits(BaseTwo.class)
    Set<Class> getExtendsBase();

    @Inherits(BaseOne.class)
    Set<Class> getImplementsPlugin();

    @Inherits(BaseTwo.class)
    @AnnotatedBy(TestAnnotation.class)
    Set<Class> getAnnotatedExtendsBase();
}
