package org.silver;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * @author John Ericksen
 */
@Retention(SOURCE)
@Target(METHOD)
public @interface Inherits {
    Class value();
}
