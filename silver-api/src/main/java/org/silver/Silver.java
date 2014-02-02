package org.silver;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * @author John Ericksen
 */
@Retention(SOURCE)
@Target(TYPE)
public @interface Silver {}
