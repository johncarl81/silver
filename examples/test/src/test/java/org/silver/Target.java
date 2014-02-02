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
