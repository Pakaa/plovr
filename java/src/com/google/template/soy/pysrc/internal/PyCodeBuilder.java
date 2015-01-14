/*
 * Copyright 2015 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.template.soy.pysrc.internal;

import com.google.template.soy.pysrc.restricted.PyExpr;
import com.google.template.soy.pysrc.restricted.PyExprUtils;
import com.google.template.soy.shared.internal.CodeBuilder;

import java.util.List;

/**
 * A Python implementation of the CodeBuilder class.
 *
 * <p>Usage example that demonstrates most of the methods:
 * <pre>
 *   PyCodeBuilder pcb = new PyCodeBuilder();
 *   pcb.appendLine("def title(opt_data):");
 *   pcb.increaseIndent();
 *   pcb.pushOutputVar("output");
 *   pcb.initOutputVarIfNecessary();
 *   pcb.pushOutputVar("temp");
 *   pcb.addToOutputVar(Lists.newArrayList(
 *       new PyExpr("'Snow White and the '", Integer.MAX_VALUE),
 *       new PyExpr("opt_data['numDwarfs']", Integer.MAX_VALUE));
 *   pcb.popOutputVar();
 *   pcb.addToOutputVar(Lists.newArrayList(
 *       new PyExpr("temp", Integer.MAX_VALUE),
 *       new PyExpr("' Dwarfs'", Integer.MAX_VALUE));
 *   pcb.appendLineStart("return ").appendOutputVarName().appendLineEnd();
 *   pcb.popOutputVar();
 *   pcb.decreaseIndent();
 *   String THE_END = "the end";
 *   pcb.appendLine("# ", THE_END);
 * </pre>
 *
 * The above example builds the following Python code:
 * <pre>
 * def title(opt_data):
 *   output = ''
 *   temp = ''.join(['Snow White and the ', str(opt_data['numDwarfs'])])
 *   output += ''.join([temp, ' Dwarfs'])
 *   return output
 * # the end
 * </pre>
 *
 */
final class PyCodeBuilder extends CodeBuilder<PyExpr> {

  @Override public void initOutputVarIfNecessary() {
    if (getOutputVarIsInited()) {
      // Nothing to do since it's already initialized.
      return;
    }

    // output = ''
    appendLine(getOutputVarName(), " = ''");

    setOutputVarInited();
  }

  @Override public void addToOutputVar(List<PyExpr> pyExprs) {
    PyExpr concatenatedOutput = PyExprUtils.concatPyExprs(pyExprs);
    if (getOutputVarIsInited()) {
      appendLine(getOutputVarName(), " += ", concatenatedOutput.getText());
    } else {
      appendLine(getOutputVarName(), " = ", concatenatedOutput.getText());
      setOutputVarInited();
    }
  }
}
