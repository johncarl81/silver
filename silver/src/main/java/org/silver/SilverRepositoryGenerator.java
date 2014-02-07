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

import com.sun.codemodel.*;
import org.androidtransfuse.adapter.PackageClass;
import org.androidtransfuse.gen.AbstractRepositoryGenerator;
import org.androidtransfuse.gen.ClassGenerationUtil;
import org.androidtransfuse.gen.ClassNamer;
import org.androidtransfuse.gen.UniqueVariableNamer;

import javax.inject.Inject;

/**
 * @author John Ericksen
 */
public class SilverRepositoryGenerator extends AbstractRepositoryGenerator {

    private static final PackageClass SIlVER_REPOSITORY = new PackageClass(SilverUtil.SIlVER_PACKAGE, SilverUtil.SIlVER_REPOSITORY_NAME);

    private final ClassNamer classNamer;
    private final ClassGenerationUtil generationUtil;

    @Inject
    public SilverRepositoryGenerator(ClassGenerationUtil generationUtil, UniqueVariableNamer namer, ClassNamer classNamer) {
        super(SilverUtil.Repository.class, generationUtil, namer, SIlVER_REPOSITORY, SilverUtil.SilverImplementation.class);
        this.classNamer = classNamer;
        this.generationUtil = generationUtil;
    }

    @Override
    protected JExpression generateInstance(JDefinedClass repositoryClass, JClass inputClass, JClass outputClass) throws JClassAlreadyExistsException {
        String innerClassName = classNamer.numberedClassName(inputClass).append(SilverUtil.IMPL_EXT).namespaced().build().getClassName();

        JDefinedClass factoryInnerClass = repositoryClass._class(JMod.PRIVATE | JMod.STATIC | JMod.FINAL, innerClassName);

        factoryInnerClass._implements(generationUtil.ref(SilverUtil.SilverImplementation.class).narrow(inputClass));

        JMethod method = factoryInnerClass.method(JMod.PUBLIC, outputClass, SilverUtil.SilverImplementation.BUILD);
        method.annotate(Override.class);

        method.body()._return(JExpr._new(outputClass));

        return JExpr._new(factoryInnerClass);
    }
}
