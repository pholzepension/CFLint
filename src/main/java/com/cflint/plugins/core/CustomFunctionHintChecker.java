package com.cflint.plugins.core;

import cfml.parsing.cfscript.CFExpression;
import cfml.parsing.cfscript.script.CFFuncDeclStatement;
import cfml.parsing.cfscript.script.CFScriptStatement;
import cfml.parsing.cfscript.script.UserDefinedFunction;
import com.cflint.BugList;
import com.cflint.CF;
import com.cflint.plugins.Context;
import com.cflint.plugins.core.HintChecker;
import com.cflint.tools.CFTool;
import net.htmlparser.jericho.Attributes;
import net.htmlparser.jericho.Element;

/**
 * Check for missing function hint attributes.
 */
public class CustomFunctionHintChecker extends HintChecker {
    private static final String CUSTOM_FUNCTION_HINT_MISSING = "CUSTOM_FUNCTION_HINT_MISSING";

    /**
     * Parse a CF function tag declaration to see if it's missing a hint.
     */
    @Override
    public void element(final Element element, final Context context, final BugList bugs) {
        String name = element.getName();
        if (element.getName().equals(CF.CFFUNCTION)) {
            final String access = element.getAttributeValue("access");
            if (access != null && access.equals("private")) {
                return;
            }
            final String hint = element.getAttributeValue("hint");
            if (hint == null || hint.trim().isEmpty()) {
                context.addMessage(CUSTOM_FUNCTION_HINT_MISSING, context.getFunctionName());
            }
        }
    }

    /**
     * Parse a CF function deceleration to see if it's missing a hint.
     */
    @Override
    public void expression(final CFScriptStatement expression, final Context context, final BugList bugs) {
        if (expression instanceof CFFuncDeclStatement) {
            final CFFuncDeclStatement funcDeclStatement = (CFFuncDeclStatement) expression;
            if (funcDeclStatement.getAccess() == UserDefinedFunction.ACCESS_PRIVATE) {
                return;
            }
            final CFExpression hintAttribute = CFTool.convertMap(funcDeclStatement.getAttributes()).get("hint");
            if (hintAttribute == null) {
                checkHint(CUSTOM_FUNCTION_HINT_MISSING, context.getFunctionName(), expression, context);
            }
        }
    }
}
