package org.silver;

import org.junit.Before;
import org.junit.Test;
import org.silver.examples.BaseTwo;
import org.silver.examples.One;
import org.silver.examples.Three;
import org.silver.examples.Two;

import java.util.ArrayList;
import java.util.List;

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
        List<Class> annotated = target.getAnnotated();
        List<Class> comparison = new ArrayList<Class>(){{
            add(One.class);
            add(Three.class);
        }};

        assertEquals(comparison, annotated);
    }

    @Test
    public void testExtendsBase(){
        List<Class> extendsBase = target.getExtendsBase();
        List<Class> comparison = new ArrayList<Class>(){{
            add(Two.class);
            add(Three.class);
        }};

        assertEquals(comparison, extendsBase);
    }

    @Test
    public void testAnnotatedExtendsBase(){
        List<Class> extendsBase = target.getAnnotatedExtendsBase();
        List<Class> comparison = new ArrayList<Class>(){{
            add(Three.class);
        }};

        assertEquals(comparison, extendsBase);
    }

    @Test
    public void testImplementsPlugin(){
        List<Class> extendsBase = target.getImplementsPlugin();
        List<Class> comparison = new ArrayList<Class>(){{
            add(One.class);
            add(Two.class);
            add(Three.class);
            add(BaseTwo.class);
        }};

        assertEquals(comparison, extendsBase);
    }


}
