// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.github.nikyotensai.decompiler.main;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.github.nikyotensai.decompiler.main.ClassesProcessor.ClassNode;
import com.github.nikyotensai.decompiler.main.extern.IBytecodeProvider;
import com.github.nikyotensai.decompiler.main.extern.IFernflowerLogger;
import com.github.nikyotensai.decompiler.main.extern.IFernflowerPreferences;
import com.github.nikyotensai.decompiler.main.extern.IIdentifierRenamer;
import com.github.nikyotensai.decompiler.main.extern.IResultSaver;
import com.github.nikyotensai.decompiler.modules.renamer.ConverterHelper;
import com.github.nikyotensai.decompiler.modules.renamer.IdentifierConverter;
import com.github.nikyotensai.decompiler.modules.renamer.PoolInterceptor;
import com.github.nikyotensai.decompiler.struct.IDecompiledData;
import com.github.nikyotensai.decompiler.struct.StructClass;
import com.github.nikyotensai.decompiler.struct.StructContext;
import com.github.nikyotensai.decompiler.struct.lazy.LazyLoader;
import com.github.nikyotensai.decompiler.util.TextBuffer;

public class Fernflower implements IDecompiledData {

    private final StructContext structContext;
    private final ClassesProcessor classProcessor;
    private final IIdentifierRenamer helper;
    private final IdentifierConverter converter;

    public Fernflower(IBytecodeProvider provider, IResultSaver saver, Map<String, Object> customProperties, IFernflowerLogger logger) {
        Map<String, Object> properties = new HashMap<>(IFernflowerPreferences.DEFAULTS);
        if (customProperties != null) {
            properties.putAll(customProperties);
        }

        String level = (String) properties.get(IFernflowerPreferences.LOG_LEVEL);
        if (level != null) {
            try {
                logger.setSeverity(IFernflowerLogger.Severity.valueOf(level.toUpperCase(Locale.ENGLISH)));
            } catch (IllegalArgumentException ignore) {
            }
        }

        structContext = new StructContext(saver, this, new LazyLoader(provider));
        classProcessor = new ClassesProcessor(structContext);

        PoolInterceptor interceptor = null;
        if ("1".equals(properties.get(IFernflowerPreferences.RENAME_ENTITIES))) {
            helper = loadHelper((String) properties.get(IFernflowerPreferences.USER_RENAMER_CLASS), logger);
            interceptor = new PoolInterceptor();
            converter = new IdentifierConverter(structContext, helper, interceptor);
        } else {
            helper = null;
            converter = null;
        }

        DecompilerContext context = new DecompilerContext(properties, logger, structContext, classProcessor, interceptor);
        DecompilerContext.setCurrentContext(context);
    }

    private static IIdentifierRenamer loadHelper(String className, IFernflowerLogger logger) {
        if (className != null) {
            try {
                Class<?> renamerClass = Fernflower.class.getClassLoader().loadClass(className);
                return (IIdentifierRenamer) renamerClass.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                logger.writeMessage("Cannot load renamer '" + className + "'", IFernflowerLogger.Severity.WARN, e);
            }
        }

        return new ConverterHelper();
    }

    public void addSource(File source) {
        structContext.addSpace(source, true);
    }

    /**
     * ???????????????????????????????????????
     *
     * @param in
     * @param filename
     * @param absolutePath
     */
    public void addStream(InputStream in, final String filename, final String absolutePath) {
        structContext.addStream(in, filename, absolutePath);
    }

    public void addLibrary(File library) {
        structContext.addSpace(library, false);
    }

    public void decompileContext() {
        if (converter != null) {
            converter.rename();
        }

        classProcessor.loadClasses(helper);

        structContext.saveContext();
    }

    public void clearContext() {
        DecompilerContext.setCurrentContext(null);
    }

    @Override
    public String getClassEntryName(StructClass cl, String entryName) {
        ClassNode node = classProcessor.getMapRootClasses().get(cl.qualifiedName);
        if (node.type != ClassNode.CLASS_ROOT) {
            return null;
        } else if (converter != null) {
            String simpleClassName = cl.qualifiedName.substring(cl.qualifiedName.lastIndexOf('/') + 1);
            return entryName.substring(0, entryName.lastIndexOf('/') + 1) + simpleClassName + ".java";
        } else {
            return entryName.substring(0, entryName.lastIndexOf(".class")) + ".java";
        }
    }

    @Override
    public String getClassContent(StructClass cl) {
        try {
            TextBuffer buffer = new TextBuffer(ClassesProcessor.AVERAGE_CLASS_SIZE);
            buffer.append(DecompilerContext.getProperty(IFernflowerPreferences.BANNER).toString());
            classProcessor.writeClass(cl, buffer);
            return buffer.toString();
        } catch (Throwable t) {
            DecompilerContext.getLogger().writeMessage("Class " + cl.qualifiedName + " couldn't be fully decompiled.", t);
            return null;
        }
    }
}