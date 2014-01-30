package org.silver;

import com.sun.codemodel.JDefinedClass;
import org.androidtransfuse.TransfuseAnalysisException;
import org.androidtransfuse.adapter.ASTType;
import org.androidtransfuse.transaction.ScopedTransactionBuilder;
import org.androidtransfuse.transaction.TransactionProcessor;
import org.androidtransfuse.transaction.TransactionProcessorPool;

import javax.inject.Provider;
import java.lang.annotation.Annotation;
import java.util.Collection;

/**
 * @author John Ericksen
 */
public class SilverProcessor {

    private final TransactionProcessor processor;
    private final TransactionProcessorPool<Provider<ASTType>, JDefinedClass> silverProcessor;
    private final Provider<SilverWorker> silverTransactionFactory;
    private final ScopedTransactionBuilder scopedTransactionBuilder;

    public SilverProcessor(TransactionProcessor processor,
                           TransactionProcessorPool<Provider<ASTType>, JDefinedClass> silverProcessor,
                           Provider<SilverWorker> silverTransactionFactory,
                           ScopedTransactionBuilder scopedTransactionBuilder) {
        this.processor = processor;
        this.silverProcessor = silverProcessor;
        this.silverTransactionFactory = silverTransactionFactory;
        this.scopedTransactionBuilder = scopedTransactionBuilder;
    }

    public void submit(Class<? extends Annotation> componentAnnotation, Collection<Provider<ASTType>> astProviders) {
        for (Provider<ASTType> astProvider : astProviders) {
            if(componentAnnotation == Silver.class){
                silverProcessor.submit(scopedTransactionBuilder.build(astProvider, silverTransactionFactory));
            }
        }
    }

    public void execute() {
        silverProcessor.execute();
    }

    public void checkForErrors() {
        if (!processor.isComplete()) {
            throw new TransfuseAnalysisException("@Silver code generation did not complete successfully.", processor.getErrors());
        }
    }
}
