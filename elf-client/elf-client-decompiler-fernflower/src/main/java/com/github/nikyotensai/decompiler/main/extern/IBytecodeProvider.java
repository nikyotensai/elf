// Copyright 2000-2017 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.github.nikyotensai.decompiler.main.extern;

import java.io.IOException;

public interface IBytecodeProvider {

    byte[] getBytecode(String externalPath, String internalPath) throws IOException;
}
