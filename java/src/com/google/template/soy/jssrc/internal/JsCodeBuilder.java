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

package com.google.template.soy.jssrc.internal;

import com.google.template.soy.jssrc.SoyJsSrcOptions.CodeStyle;
import com.google.template.soy.jssrc.restricted.JsExpr;
import com.google.template.soy.jssrc.restricted.JsExprUtils;
import com.google.template.soy.shared.internal.CodeBuilder;

import java.util.List;

/**
 * A JavaScript implementation of the CodeBuilder class.
 *
 * <p>Usage example that demonstrates most of the methods:
 * <pre>
 *   JsCodeBuilder jcb = new JsCodeBuilder(CodeStyle.STRINGBUILDER);
 *   jcb.appendLine("story.title = function(opt_data) {");
 *   jcb.increaseIndent();
 *   jcb.pushOutputVar("output");
 *   jcb.initOutputVarIfNecessary();
 *   jcb.pushOutputVar("temp");
 *   jcb.addToOutputVar(Lists.newArrayList(
 *       new JsExpr("'Snow White and the '", Integer.MAX_VALUE),
 *       new JsExpr("opt_data.numDwarfs", Integer.MAX_VALUE));
 *   jcb.popOutputVar();
 *   jcb.addToOutputVar(Lists.newArrayList(
 *       new JsExpr("temp", Integer.MAX_VALUE),
 *       new JsExpr("' Dwarfs'", Integer.MAX_VALUE));
 *   jcb.appendLineStart("return ").appendOutputVarName().appendLineEnd(".toString();");
 *   jcb.popOutputVar();
 *   jcb.decreaseIndent();
 *   String THE_END = "the end";
 *   jcb.appendLine("}  // ", THE_END);
 * </pre>
 *
 * <p>The above example builds the following JS code:
 * <pre>
 * story.title = function(opt_data) {
 *   var output = new soy.StringBuilder();
 *   var temp = new soy.StringBuilder('Snow White and the ', opt_data.numDwarfs);
 *   output.append(temp, ' Dwarfs');
 *   return output.toString();
 * }  // the end
 * </pre>
 *
 */
final class JsCodeBuilder extends CodeBuilder<JsExpr> {

  /** The {@code OutputCodeGenerator} to use. */
  private final CodeStyle codeStyle;


  /**
   * Constructs a new instance. At the start, the code is empty and the indent is 0 spaces.
   *
   * @param codeStyle The code style to use.
   */
  public JsCodeBuilder(CodeStyle codeStyle) {
    super();
    this.codeStyle = codeStyle;
  }

  /**
   * Appends a full line/statement for initializing the current output variable.
   */
  public void initOutputVarIfNecessary() {

    if (getOutputVarIsInited()) {
      // Nothing to do since it's already initialized.
      return;
    }

    if (codeStyle == CodeStyle.STRINGBUILDER) {
      // var output = new soy.StringBuilder();
      appendLine("var ", getOutputVarName(), " = new soy.StringBuilder();");
    } else {
      // var output = '';
      appendLine("var ", getOutputVarName(), " = '';");
    }
    setOutputVarInited();
  }

  /**
   * Appends a line/statement with the concatenation of the given JS expressions saved to the
   * current output variable.
   * @param jsExprs One or more JS expressions to compute output.
   */
  public void addToOutputVar(List<JsExpr> jsExprs) {

    if (codeStyle == CodeStyle.STRINGBUILDER) {
      StringBuilder commaSeparatedJsExprsSb = new StringBuilder();
      boolean isFirst = true;
      for (JsExpr jsExpr : jsExprs) {
        if (isFirst) {
          isFirst = false;
        } else {
          commaSeparatedJsExprsSb.append(", ");
        }
        commaSeparatedJsExprsSb.append(jsExpr.getText());
      }

      if (getOutputVarIsInited()) {
        // output.append(AAA, BBB);
        appendLine(getOutputVarName(), ".append(", commaSeparatedJsExprsSb.toString(), ");");
      } else {
        // var output = new soy.StringBuilder(AAA, BBB);
        appendLine("var ", getOutputVarName(), " = new soy.StringBuilder(",
                   commaSeparatedJsExprsSb.toString(), ");");
        setOutputVarInited();
      }

    } else {  // CodeStyle.CONCAT
      if (getOutputVarIsInited()) {
        // output += AAA + BBB + CCC;
        appendLine(getOutputVarName(), " += ", JsExprUtils.concatJsExprs(jsExprs).getText(), ";");
      } else {
        // var output = '' + AAA + BBB + CCC;
        // NOTE: We initialize with '' to enforce string concatenation. This ensures something like
        // {2}{2} becomes '22' instead of 4.
        // TODO: Optimize this away if we know the first or second expression is a string.
        String contents = JsExprUtils.concatJsExprsForceString(jsExprs).getText();
        appendLine("var ", getOutputVarName(), " = ", contents, ";");
        setOutputVarInited();
      }
    }
  }
}
