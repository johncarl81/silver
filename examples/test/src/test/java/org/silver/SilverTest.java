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

import org.junit.Before;
import org.junit.Test;
import org.silver.examples.BaseTwo;
import org.silver.examples.One;
import org.silver.examples.Three;
import org.silver.examples.Two;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;

/**
 * @author John Ericksen
 */
public class SilverTest {

    private Target target;

    @Before
    public void setUp() throws Exception {
        target = SilverUtil.get(Target.class);
    }

    @Test
    public void testAnnotatedBy(){
        Set<Class> annotated = target.getAnnotated();
        Set<Class> comparison = new HashSet<Class>(){{
            add(One.class);
            add(Three.class);
        }};

        assertEquals(comparison, annotated);
    }

    @Test
    public void testExtendsBase(){
        Set<Class> extendsBase = target.getExtendsBase();
        Set<Class> comparison = new HashSet<Class>(){{
            add(Two.class);
            add(Three.class);
        }};

        assertEquals(comparison, extendsBase);
    }

    @Test
    public void testAnnotatedExtendsBase(){
        Set<Class> extendsBase = target.getAnnotatedExtendsBase();
        Set<Class> comparison = new HashSet<Class>(){{
            add(Three.class);
        }};

        assertEquals(comparison, extendsBase);
    }

    @Test
    public void testImplementsPlugin(){
        Set<Class> extendsBase = target.getImplementsPlugin();
        Set<Class> comparison = new HashSet<Class>(){{
            add(One.class);
            add(Two.class);
            add(Three.class);
            add(BaseTwo.class);
        }};

        assertEquals(comparison, extendsBase);
    }


}
