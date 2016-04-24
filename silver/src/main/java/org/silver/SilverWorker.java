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

import com.google.common.collect.ImmutableList;
import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JInvocation;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JVar;
import org.androidtransfuse.adapter.ASTAnnotation;
import org.androidtransfuse.adapter.ASTConstructor;
import org.androidtransfuse.adapter.ASTFactory;
import org.androidtransfuse.adapter.ASTField;
import org.androidtransfuse.adapter.ASTMethod;
import org.androidtransfuse.adapter.ASTParameter;
import org.androidtransfuse.adapter.ASTType;
import org.androidtransfuse.adapter.ASTWildcardType;
import org.androidtransfuse.adapter.LazyTypeParameterBuilder;
import org.androidtransfuse.adapter.classes.ASTClassFactory;
import org.androidtransfuse.adapter.element.ASTElementFactory;
import org.androidtransfuse.adapter.element.ElementVisitorAdaptor;
import org.androidtransfuse.gen.ClassGenerationUtil;
import org.androidtransfuse.gen.ClassNamer;
import org.androidtransfuse.gen.UniqueVariableNamer;
import org.androidtransfuse.transaction.AbstractCompletionTransactionWorker;
import org.androidtransfuse.util.matcher.Matcher;
import org.androidtransfuse.validation.Validator;

import javax.annotation.processing.RoundEnvironment;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author John Ericksen
 */
public class SilverWorker extends AbstractCompletionTransactionWorker<Provider<ASTType>, JDefinedClass> {

    private final Provider<RoundEnvironment> roundEnvironmentProvider;
    private final ASTElementFactory astElementFactory;
    private final ASTClassFactory astClassFactory;
    private final ASTFactory astFactory;
    private final ClassGenerationUtil generationUtil;
    private final UniqueVariableNamer namer;
    private final JCodeModel codeModel;
    private final Validator validator;

    @Inject
    public SilverWorker(Provider<RoundEnvironment> roundEnvironmentProvider,
                        ASTElementFactory astElementFactory,
                        ASTClassFactory astClassFactory,
                        ASTFactory astFactory,
                        ClassGenerationUtil generationUtil,
                        UniqueVariableNamer namer,
                        JCodeModel codeModel,
                        Validator validator) {
        this.roundEnvironmentProvider = roundEnvironmentProvider;
        this.astElementFactory = astElementFactory;
        this.astClassFactory = astClassFactory;
        this.astFactory = astFactory;
        this.generationUtil = generationUtil;
        this.namer = namer;
        this.codeModel = codeModel;
        this.validator = validator;
    }

    @Override
    public JDefinedClass innerRun(Provider<ASTType> astTypeProvider) {
        ASTType implementation = astTypeProvider.get();

        try {
            if(!implementation.isInterface()){
                validator.error("Only interfaces may be annotated with @Silver.").element(implementation).build();
            }

            JDefinedClass silverImpl = generationUtil.defineClass(ClassNamer.className(implementation).append(SilverUtil.IMPL_EXT).build());

            silverImpl._implements(generationUtil.ref(implementation));

            JClass classWildcard = generationUtil.ref(Class.class).narrow(codeModel.wildcard());
            JClass setRef = generationUtil.ref(Set.class).narrow(classWildcard);
            JClass hashsetRef = generationUtil.ref(HashSet.class).narrow(classWildcard);


            // Builds the following static method:

            // private static Set<Class> buildSet(Class... input) {
            //     Set<java.lang.Class> set = new HashSet<Class>();
            //     Collections.addAll(set, input);
            //     return Collections.unmodifiableSet(set);
            // }
            JMethod buildSet = silverImpl.method(JMod.PRIVATE | JMod.STATIC, setRef, "buildSet");
            JVar inputVar = buildSet.varParam(classWildcard, namer.generateName(classWildcard));
            JBlock buildSetBody = buildSet.body();

            JVar setVar = buildSetBody.decl(setRef, namer.generateName(setRef), JExpr._new(hashsetRef));
            buildSetBody.staticInvoke(generationUtil.ref(Collections.class), "addAll").arg(setVar).arg(inputVar);
            buildSetBody._return(generationUtil.ref(Collections.class).staticInvoke("unmodifiableSet").arg(setVar));

            ASTType returnType = astFactory.buildGenericTypeWrapper(astClassFactory.getType(Set.class), new LazyTypeParameterBuilder() {
                @Override
                public ImmutableList<ASTType> buildGenericParameters() {
                    return ImmutableList.<ASTType>of(astFactory.buildGenericTypeWrapper(astClassFactory.getType(Class.class), new LazyTypeParameterBuilder() {
                        @Override
                        public ImmutableList<ASTType> buildGenericParameters() {
                            return ImmutableList.<ASTType>of(new ASTWildcardType(null, null));
                        }
                    }));
                }
            });

            for (ASTMethod astMethod : implementation.getMethods()) {

                if(!returnType.equals(astMethod.getReturnType())){
                    validator.error("Silver annotated methods must return Set<Class<?>>.").element(astMethod).build();
                }

                if(astMethod.getParameters().size() > 0){
                    validator.error("Silver annotated methods must have zero arguments.").element(astMethod).build();
                }

                JClass collectionType = generationUtil.narrowRef(returnType);

                JFieldVar collectionField = silverImpl.field(JMod.PRIVATE | JMod.FINAL | JMod.STATIC, collectionType, namer.generateName(collectionType), generateTypeCollection(astMethod));

                JMethod method = silverImpl.method(JMod.PUBLIC, collectionType, astMethod.getName());

                method.body()._return(collectionField);
            }

            return silverImpl;

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
                    final ArrayList<ASTAnnotation> annotations = new ArrayList<ASTAnnotation>();

                    for( ASTMethod astMethod : astType.getMethods()) {
                        annotations.addAll(astMethod.getAnnotations());

                        for( ASTParameter astParameter : astMethod.getParameters()) {
                            annotations.addAll(astParameter.getAnnotations());
                        }
                    }

                    for( ASTConstructor astConstructor : astType.getConstructors()) {
                        annotations.addAll(astConstructor.getAnnotations());

                        for( ASTParameter astParameter : astConstructor.getParameters()) {
                            annotations.addAll(astParameter.getAnnotations());
                        }
                    }

                    for( ASTField astField : astType.getFields()) {
                        annotations.addAll(astField.getAnnotations());
                    }

                    annotations.addAll(astType.getAnnotations());

                    for (ASTAnnotation astAnnotation : annotations) {
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
        if(method.isAnnotated(Package.class)){
            final String packageName = method.getAnnotation(Package.class).value();

            matcherConjunction.add(new Matcher<ASTType>(){

                @java.lang.Override
                public boolean matches(ASTType input) {
                    return input.getPackageClass().getPackage().startsWith(packageName);
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

        Set<ASTType> matched = match(roundEnvironmentProvider.get().getRootElements(), matcher);

        JInvocation buildSetInvocation = JExpr.invoke("buildSet");

        for (ASTType astType : matched) {
            buildSetInvocation.arg(generationUtil.ref(astType).staticRef("class"));
        }

        return buildSetInvocation;
    }

    private Set<ASTType> match(Collection<? extends Element> inputElements, final Matcher<ASTType> matcher){
        final Set<ASTType> matched = new HashSet<ASTType>();

        for (Element element : inputElements) {
            element.accept(new ElementVisitorAdaptor<Void, Void>(){
                @Override
                public Void visitType(TypeElement typeElement, Void input) {
                    ASTType elementType = astElementFactory.getType(typeElement);
                    if(matcher.matches(elementType)){
                        matched.add(elementType);
                    }

                    return null;
                }
            }, null);



            matched.addAll(match(element.getEnclosedElements(), matcher));
        }

        return matched;
    }
}
