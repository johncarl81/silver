package org.silver;

import org.androidtransfuse.adapter.ASTAccessModifier;
import org.androidtransfuse.gen.invocationBuilder.InvocationBuilderStrategy;
import org.androidtransfuse.gen.invocationBuilder.ModifierInjectionBuilder;
import org.androidtransfuse.gen.invocationBuilder.PublicInjectionBuilder;

import javax.inject.Inject;
import javax.inject.Provider;

/**
 * @author John Ericksen
 */
public class SilverInvocationBuilderStrategy implements InvocationBuilderStrategy {

    private final Provider<PublicInjectionBuilder> publicProvider;

    @Inject
    public SilverInvocationBuilderStrategy(Provider<PublicInjectionBuilder> publicProvider) {
        this.publicProvider = publicProvider;
    }

    @Override
    public ModifierInjectionBuilder getInjectionBuilder(ASTAccessModifier modifier) {
        return publicProvider.get();
    }
}
