package org.silver;

import org.silver.examples.BaseOne;
import org.silver.examples.BaseTwo;

import java.util.List;

/**
 * @author John Ericksen
 */
@Silver
public interface Target {

    @AnnotatedBy(TestAnnotation.class)
    List<Class> getAnnotated();

    @Extends(BaseTwo.class)
    List<Class> getExtendsBase();

    @Extends(BaseOne.class)
    List<Class> getImplementsPlugin();

    @Extends(BaseTwo.class)
    @AnnotatedBy(TestAnnotation.class)
    List<Class> getAnnotatedExtendsBase();
}
