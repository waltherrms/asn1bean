/*
 * Copyright 2012 The ASN1bean Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.beanit.asn1bean.compiler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.beanit.asn1bean.compiler.cli.CliParser;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Locale;
import org.junit.jupiter.api.Test;

public class StringCaseLocaleUsageTest {

  @Test
  public void testCliParserWithTurkishLocale() throws Exception {
    Locale defaultLocale = Locale.getDefault();
    try {
      Locale.setDefault(Locale.forLanguageTag("tr-TR"));

      CliParser cliParser = new CliParser("asn1bean-compiler", "Compiler for ASN.1");
      cliParser.addParameterGroup("compile", Collections.emptyList());
      cliParser.parseArguments(new String[] {"COMPILE"});

      assertEquals("compile", cliParser.getSelectedGroup());
    } finally {
      Locale.setDefault(defaultLocale);
    }
  }

  @Test
  public void testBerClassWriterWithTurkishLocale() throws Exception {
    Locale defaultLocale = Locale.getDefault();
    try {
      Locale.setDefault(Locale.forLanguageTag("tr-TR"));

      Path tempDir = Files.createTempDirectory("asn1bean-locale-test");
      String[] args =
          new String[] {
            "-o",
            tempDir.toString(),
            "-p",
            "com.beanit.asn1bean.compiler.test",
            "-f",
            "src/test/resources/iso-presentation-layer.asn",
            "-dv"
          };
      Compiler.main(args);

      File expectedDir =
          new File(tempDir.toFile(), "com/beanit/asn1bean/compiler/test/iso8823_presentation");
      assertTrue(
          expectedDir.exists() && expectedDir.isDirectory(),
          "Expected directory with ASCII 'iso8823_presentation' was not created: " + expectedDir);

      File generatedClass = new File(expectedDir, "CPType.java");
      assertTrue(generatedClass.exists(), "Expected generated class CPType.java to exist");

      String content = Files.readString(generatedClass.toPath());
      assertTrue(
          content.contains("package com.beanit.asn1bean.compiler.test.iso8823_presentation;"),
          "Package declaration should contain ASCII 'iso8823_presentation'");
    } finally {
      Locale.setDefault(defaultLocale);
    }
  }
}
