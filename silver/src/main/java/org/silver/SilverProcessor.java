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

import com.sun.codemodel.JDefinedClass;
import org.androidtransfuse.adapter.ASTType;
import org.androidtransfuse.transaction.ScopedTransactionBuilder;
import org.androidtransfuse.transaction.TransactionProcessor;
import org.androidtransfuse.transaction.TransactionProcessorPool;
import org.androidtransfuse.util.Logger;

import javax.inject.Provider;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Set;

/**
 * @author John Ericksen
 */
public class SilverProcessor {

    private final TransactionProcessor processor;
    private final TransactionProcessorPool<Provider<ASTType>, JDefinedClass> silverProcessor;
    private final Provider<SilverWorker> silverTransactionFactory;
    private final ScopedTransactionBuilder scopedTransactionBuilder;
    private final Logger logger;

    public SilverProcessor(TransactionProcessor processor,
                           TransactionProcessorPool<Provider<ASTType>, JDefinedClass> silverProcessor,
                           Provider<SilverWorker> silverTransactionFactory,
                           ScopedTransactionBuilder scopedTransactionBuilder, Logger logger) {
        this.processor = processor;
        this.silverProcessor = silverProcessor;
        this.silverTransactionFactory = silverTransactionFactory;
        this.scopedTransactionBuilder = scopedTransactionBuilder;
        this.logger = logger;
    }

    public void submit(Class<? extends Annotation> componentAnnotation, Collection<Provider<ASTType>> astProviders) {
        for (Provider<ASTType> astProvider : astProviders) {
            if(componentAnnotation == Silver.class){
                silverProcessor.submit(scopedTransactionBuilder.build(astProvider, silverTransactionFactory));
            }
        }
    }

    public void execute() {
        processor.execute();
    }

    public void checkForErrors() {
        if (!processor.isComplete()) {
            for (Exception exception : (Set<Exception>) processor.getErrors()) {
                logger.error("Code generation did not complete successfully.", exception);
            }
        }
    }
}
