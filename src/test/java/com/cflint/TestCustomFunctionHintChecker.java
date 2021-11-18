package com.cflint;

import com.cflint.api.CFLintAPI;
import com.cflint.api.CFLintResult;
import com.cflint.config.ConfigBuilder;
import com.cflint.exception.CFLintScanException;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class TestCustomFunctionHintChecker {

    private CFLintAPI cfBugs;

    @Before
    public void setUp() throws Exception {
        final ConfigBuilder configBuilder = new ConfigBuilder().include("CUSTOM_FUNCTION_HINT_MISSING");
        cfBugs = new CFLintAPI(configBuilder.build());
    }

    @Test
    public void testCustomFunctionTagHint() throws CFLintScanException {
        final String cfcSrc = "<cfcomponent>\r\n" + "<cffunction name=\"test\">\r\n"
            + "	<cfargument name=\"xyz\" default=\"123\">\r\n" + "</cffunction>\r\n" + "</cfcomponent>";
        CFLintResult lintresult = cfBugs.scan(cfcSrc, "test");
        final List<BugInfo> result = lintresult.getIssues().values().iterator().next();
        assertEquals(1, result.size());
        assertEquals("CUSTOM_FUNCTION_HINT_MISSING", result.get(0).getMessageCode());
        assertEquals(2, result.get(0).getLine());
    }

    @Test
    public void testCustomFunctionTagHintPrivate() throws CFLintScanException {
        final String cfcSrc = "<cfcomponent>\r\n" + "<cffunction access=\"private\" name=\"test\">\r\n"
            + "	<cfargument name=\"xyz\" default=\"123\">\r\n" + "</cffunction>\r\n" + "</cfcomponent>";
        CFLintResult lintresult = cfBugs.scan(cfcSrc, "test");
        final Map<String, List<BugInfo>> result = lintresult.getIssues();
        assertEquals(0, result.size());
    }

    @Test
    public void testCustomFunctionHint() throws CFLintScanException {
        final String scriptSrc =
            "component {\r\n" +
               "function test(required string testArgument) { \r\n" +
                " } \r\n" +
            " } ";

        CFLintResult lintresult = cfBugs.scan(scriptSrc, "test");
        final List<BugInfo> result = lintresult.getIssues().values().iterator().next();
        assertEquals(1, result.size());
        assertEquals("CUSTOM_FUNCTION_HINT_MISSING", result.get(0).getMessageCode());
        assertEquals(2, result.get(0).getLine());
    }

    @Test
    public void testCustomFunctionHintPrivate() throws CFLintScanException {
        final String scriptSrc =
            "component {\r\n" +
                "private function test(required string testArgument) { \r\n" +
                " } \r\n" +
            " } ";

        CFLintResult lintresult = cfBugs.scan(scriptSrc, "test");
        final Map<String, List<BugInfo>> result = lintresult.getIssues();
        assertEquals(0, result.size());
    }

}
