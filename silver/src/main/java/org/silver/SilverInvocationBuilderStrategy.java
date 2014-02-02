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
