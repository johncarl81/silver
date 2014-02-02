package org.silver;

import com.sun.codemodel.*;
import org.androidtransfuse.adapter.ASTAnnotation;
import org.androidtransfuse.adapter.ASTMethod;
import org.androidtransfuse.adapter.ASTType;
import org.androidtransfuse.adapter.element.ASTElementFactory;
import org.androidtransfuse.gen.ClassGenerationUtil;
import org.androidtransfuse.gen.ClassNamer;
import org.androidtransfuse.gen.UniqueVariableNamer;
import org.androidtransfuse.transaction.AbstractCompletionTransactionWorker;
import org.androidtransfuse.util.matcher.Matcher;

import javax.annotation.processing.RoundEnvironment;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author John Ericksen
 */
public class SilverWorker extends AbstractCompletionTransactionWorker<Provider<ASTType>, JDefinedClass> {

    private final Provider<RoundEnvironment> roundEnvironmentProvider;
    private final ASTElementFactory astElementFactory;
    private final ClassGenerationUtil generationUtil;
    private final UniqueVariableNamer namer;

    @Inject
    public SilverWorker(Provider<RoundEnvironment> roundEnvironmentProvider, ASTElementFactory astElementFactory, ClassGenerationUtil generationUtil, UniqueVariableNamer namer) {
        this.roundEnvironmentProvider = roundEnvironmentProvider;
        this.astElementFactory = astElementFactory;
        this.generationUtil = generationUtil;
        this.namer = namer;
    }

    @Override
    public JDefinedClass innerRun(Provider<ASTType> astTypeProvider) {
        ASTType implementation = astTypeProvider.get();

        try {
            JDefinedClass silverimpl = generationUtil.defineClass(ClassNamer.className(implementation).append(SilverUtil.IMPL_EXT).build());

            silverimpl._implements(generationUtil.ref(implementation));

            for (ASTMethod astMethod : implementation.getMethods()) {

                JClass collectionType = generationUtil.narrowRef(astMethod.getReturnType());

                JFieldVar collectionField = silverimpl.field(JMod.PRIVATE | JMod.FINAL | JMod.STATIC, collectionType.erasure(), namer.generateName(collectionType), generateTypeCollection(astMethod));

                JMethod method = silverimpl.method(JMod.PUBLIC, collectionType, astMethod.getName());

                method.body()._return(collectionField);
            }

            return silverimpl;

        } catch (JClassAlreadyExistsException e) {
            throw new SilverRuntimeException("Class Already exists", e);
        }
    }

    private JExpression generateTypeCollection(ASTMethod method) {

        final List<Matcher<ASTType>> matcherConjunction = new ArrayList<Matcher<ASTType>>();

        if(method.isAnnotated(AnnotatedBy.class)){
            final ASTType annotatedBy = method.getASTAnnotation(AnnotatedBy.class).getProperty("value", ASTType.class);

            matcherConjunction.add(new Matcher<ASTType>() {
                @Override
                public boolean matches(ASTType astType) {
                    for (ASTAnnotation astAnnotation : astType.getAnnotations()) {
                        if(astAnnotation.getASTType().equals(annotatedBy)){
                            return true;
                        }
                    }
                    return false;
                }
            });
        }
        if(method.isAnnotated(Inherits.class)){
            final ASTType extendsType = method.getASTAnnotation(Inherits.class).getProperty("value", ASTType.class);

            matcherConjunction.add(new Matcher<ASTType>(){

                @Override
                public boolean matches(ASTType input) {
                    return input.inheritsFrom(extendsType) && !input.equals(extendsType);
                }
            });
        }


        Matcher<ASTType> matcher = new Matcher<ASTType>() {
            @Override
            public boolean matches(ASTType input) {
                if(matcherConjunction.size() == 0){
                    return false;
                }

                for (Matcher<ASTType> matcher : matcherConjunction) {
                    if(!matcher.matches(input)){
                        return false;
                    }
                }
                return true;
            }
        };

        List<ASTType> matched = new ArrayList<ASTType>();
        for (Element element : roundEnvironmentProvider.get().getRootElements()) {
            ASTType elementType = astElementFactory.getType((TypeElement) element);
            if(matcher.matches(elementType)){
                matched.add(elementType);
            }
        }

        JClass arraysRef = generationUtil.ref(Arrays.class);

        JInvocation collectionsInvocation = generationUtil.ref(Collections.class).staticInvoke("unmodifiableList");
        JInvocation asListInvocation = arraysRef.staticInvoke("asList");
        collectionsInvocation.arg(asListInvocation);

        for (ASTType astType : matched) {
            asListInvocation.arg(generationUtil.ref(astType).staticRef("class"));
        }

        return collectionsInvocation;
    }
}
