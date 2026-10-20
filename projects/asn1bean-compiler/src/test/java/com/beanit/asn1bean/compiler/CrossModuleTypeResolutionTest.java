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

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class CrossModuleTypeResolutionTest {

  @TempDir Path outputDirectory;

  @Test
  public void resolvesRetaggedTypesInTheirDefiningModules() throws Exception {
    Compiler.main(
        new String[] {
          "-o",
          outputDirectory.toString(),
          "-p",
          "com.beanit.asn1bean.compiler",
          "-f",
          "src/test/resources/cross-module-types.asn",
          "-dv"
        });

    assertTagStructure("ExplicitTopType", 0, "CONSTRUCTED");
    assertTagStructure("ImplicitTopType", 1, "PRIMITIVE");
    assertTagStructure("DefaultTopType", 2, "CONSTRUCTED");
  }

  private void assertTagStructure(String className, int tagNumber, String expectedStructure)
      throws Exception {
    Path generatedSource =
        outputDirectory.resolve(
            "com/beanit/asn1bean/compiler/cross/module/a/" + className + ".java");
    String source = Files.readString(generatedSource);

    assertTrue(
        source.contains(
            "public static final BerTag tag = new BerTag(BerTag.APPLICATION_CLASS, BerTag."
                + expectedStructure
                + ", "
                + tagNumber
                + ");"));
  }
}
