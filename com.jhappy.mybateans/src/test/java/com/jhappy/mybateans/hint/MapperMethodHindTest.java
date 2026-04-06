/*
 * The MIT License
 *
 * Copyright 2026 th.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.jhappy.mybateans.hint;

import org.junit.Test;
import org.netbeans.modules.java.hints.test.api.HintTest;

/* TODO to make this test work:
   - to ensure that the newest Java language features supported by the IDE are available,
     regardless of which JDK you build the module with:
   -- for Ant-based modules, add "requires.nb.javac=true" into nbproject/project.properties
   -- for Maven-based modules, use dependency:copy in validate phase to create
      target/endorsed/org-netbeans-libs-javacapi-*.jar and add to endorseddirs
      in maven-compiler-plugin and maven-surefire-plugin configuration
      See: http://wiki.netbeans.org/JavaHintsTestMaven
 */
public class MapperMethodHindTest {

    @Test
    public void testWarningProduced() throws Exception {
        HintTest.create()
                .input("package test;\n"
                        + "public class Test {\n"
                        + "    public static void main(String[] args) {\n"
                        + "        assert args[0].equals(\"\");\n"
                        + "    }\n"
                        + "}\n")
                .run(MapperMethodHint.class)
                .assertWarnings("3:23-3:29:verifier:" + Bundle.ERR_MapperMethodHind());
    }

    @Test
    public void testFixWorking() throws Exception {
        HintTest.create()
                .input("package test;\n"
                        + "public class Test {\n"
                        + "    public static void main(String[] args) {\n"
                        + "        assert args[0].equals(\"\");\n"
                        + "    }\n"
                        + "}\n")
                .run(MapperMethodHint.class)
                .findWarning("3:23-3:29:verifier:" + Bundle.ERR_MapperMethodHind())
                .applyFix()
                .assertCompilable()
                //TODO: change to match expected output
                .assertOutput("package test;\n"
                        + "public class Test {\n"
                        + "    public static void main(String[] args) {\n"
                        + "        assert args[0].equals(\"\");\n"
                        + "    }\n"
                        + "}\n");
    }
}
