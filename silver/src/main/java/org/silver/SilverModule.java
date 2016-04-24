/**
 * Copyright 2014-2015 John Ericksen
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

import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import org.androidtransfuse.CodeGenerationScope;
import org.androidtransfuse.adapter.ASTFactory;
import org.androidtransfuse.adapter.ASTType;
import org.androidtransfuse.annotations.Bind;
import org.androidtransfuse.annotations.Bindings;
import org.androidtransfuse.annotations.DefineScope;
import org.androidtransfuse.annotations.DefineScopes;
import org.androidtransfuse.annotations.Install;
import org.androidtransfuse.annotations.Provides;
import org.androidtransfuse.bootstrap.BootstrapModule;
import org.androidtransfuse.bootstrap.Namespace;
import org.androidtransfuse.config.MapScope;
import org.androidtransfuse.config.OutOfScopeException;
import org.androidtransfuse.config.ThreadLocalScope;
import org.androidtransfuse.gen.ClassGenerationStrategy;
import org.androidtransfuse.gen.InjectionBuilderContextFactory;
import org.androidtransfuse.gen.invocationBuilder.InvocationBuilderStrategy;
import org.androidtransfuse.gen.variableDecorator.VariableExpressionBuilderFactory;
import org.androidtransfuse.transaction.ScopedTransactionBuilder;
import org.androidtransfuse.transaction.TransactionProcessorChannel;
import org.androidtransfuse.transaction.TransactionProcessorPool;
import org.androidtransfuse.util.Logger;
import org.androidtransfuse.util.MessagerLogger;
import org.androidtransfuse.validation.Validator;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.lang.model.util.Elements;
import java.util.Map;

@BootstrapModule
@DefineScopes({
        @DefineScope(annotation = CodeGenerationScope.class, scope = ThreadLocalScope.class),
        @DefineScope(annotation = ProcessingScope.class, scope = MapScope.class)
})
@Bindings({
    @Bind(type = InvocationBuilderStrategy.class, to = SilverInvocationBuilderStrategy.class)
})
@Install({
        ASTFactory.class,
        VariableExpressionBuilderFactory.class,
        InjectionBuilderContextFactory.class})
@Namespace("Silver")
public class SilverModule {

    @Provides
    public ClassGenerationStrategy getClassGenerationStrategy(){
        return new ClassGenerationStrategy(org.androidtransfuse.util.Generated.class, SilverAnnotationProcessor.class.getName());
    }

    @Provides
    @CodeGenerationScope
    public JCodeModel getJCodeModel(){
        return new JCodeModel();
    }

    @Provides
    @Singleton
    public Elements getElements(ProcessingEnvironment processingEnvironment){
        return processingEnvironment.getElementUtils();
    }

    @Provides
    @Singleton
    public Messager getMessenger(ProcessingEnvironment processingEnvironment){
        return processingEnvironment.getMessager();
    }

    @Provides
    @Singleton
    public Logger getLogger(ProcessingEnvironment processingEnvironment){
        return new MessagerLogger(getLogPreprend(), processingEnvironment.getMessager(), false);
    }

    @Provides
    @Named(Validator.LOG_PREPEND)
    public String getLogPreprend(){
        return "Silver: ";
    }

    @Provides
    @Singleton
    public Filer getFiler(ProcessingEnvironment processingEnvironment){
        return processingEnvironment.getFiler();
    }

    @Provides
    @Singleton
    public ProcessingEnvironment getProcessingEnvironment(){
        throw new OutOfScopeException("Expected seeded object, unable to construct directly.");
    }

    @Provides
    @ProcessingScope
    public RoundEnvironment getRoundEnvironment(){
        throw new OutOfScopeException("Expected seeded object, unable to construct directly.");
    }

    @Provides
    public SilverProcessor buildSilverProcessor(Provider<SilverWorker> silverTransactionFactory,
                                                Provider<SilverRepositoryGenerator> silverRepositoryGeneratorProvider,
                                                ScopedTransactionBuilder scopedTransactionBuilder,
                                                Logger logger){

        TransactionProcessorPool<Provider<ASTType>, JDefinedClass> workingPool = new TransactionProcessorPool<Provider<ASTType>, JDefinedClass>();

        TransactionProcessorChannel<Provider<ASTType>, JDefinedClass, JDefinedClass> channel =
                new TransactionProcessorChannel<Provider<ASTType>, JDefinedClass, JDefinedClass>(
                        workingPool,
                        new TransactionProcessorPool<Map<Provider<ASTType>, JDefinedClass>, JDefinedClass>(),
                        scopedTransactionBuilder.buildFactory(silverRepositoryGeneratorProvider)
                );

        return new SilverProcessor(channel, workingPool, silverTransactionFactory, scopedTransactionBuilder, logger);
    }
}