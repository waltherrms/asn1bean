/*
 * Copyright 2026 The ASN1bean Authors
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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class ExplicitExtensionMarkerTest {

  @TempDir Path outputDirectory;

  @Test
  public void appliesExtensionHandlingOnlyToMarkedSequences() throws Exception {
    Compiler.main(
        new String[] {
          "-o",
          outputDirectory.toString(),
          "-p",
          "com.beanit.asn1bean.compiler",
          "-f",
          "src/test/resources/explicit-extension-marker.asn",
          "-dv",
          "-e"
        });

    String extensibleSource = readGeneratedSource("ExtensibleSequence");
    String nonExtensibleSource = readGeneratedSource("NonExtensibleSequence");

    assertTrue(extensibleSource.contains("byte[] extensionBytes = null;"));
    assertTrue(extensibleSource.contains("DecodeUtil.decodeUnknownComponent(is, os)"));
    assertFalse(nonExtensibleSource.contains("extensionBytes"));
    assertFalse(nonExtensibleSource.contains("DecodeUtil.decodeUnknownComponent(is, os)"));

    assertContainsValueComponent("ExtensibleSet");
    assertContainsValueComponent("ExtensibleChoice");
  }

  private String readGeneratedSource(String className) throws Exception {
    Path generatedSource =
        outputDirectory.resolve(
            "com/beanit/asn1bean/compiler/explicit/extension/marker/" + className + ".java");
    return Files.readString(generatedSource);
  }

  private void assertContainsValueComponent(String className) throws Exception {
    assertTrue(readGeneratedSource(className).contains("BerInteger value = null;"));
  }
}
