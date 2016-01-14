package org.vaadin.presentation.views;

import javax.inject.Qualifier;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/*
 * A qualifier for CDI events used to decouple PayeeForm from the PayeeView
 */
@Qualifier
@Retention(RUNTIME)
@Target({FIELD,PARAMETER})
public abstract @interface PayeeEvent {
    
    Type value();

    public enum Type {
        SAVE, DELETE, REFRESH
    }
}

