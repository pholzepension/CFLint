package com.cflint.config;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Collection;
import java.util.List;

import javax.xml.bind.Marshaller;

import com.cflint.BugInfo;
import com.cflint.api.CFLintAPI;
import com.cflint.api.CFLintResult;
import jdk.nashorn.internal.runtime.regexp.joni.Config;
import org.junit.Test;

import com.cflint.Levels;
import com.cflint.config.CFLintPluginInfo.PluginInfoRule;
import com.cflint.config.CFLintPluginInfo.PluginInfoRule.PluginMessage;
import com.cflint.config.CFLintPluginInfo.RuleGroup;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;

public class TestCFLintConfig {
    private CFLintAPI cfBugs;

    final String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" + "<CFLint-Plugin>\n"
            + "    <ruleImpl name=\"OPM\">\n" + "        <message code=\"code\">\n"
            + "            <messageText>messageText</messageText>\n" + "            <severity>WARNING</severity>\n"
            + "        </message>\n" + "    </ruleImpl>\n" + "</CFLint-Plugin>";

    @Test
    public void test() throws Exception {
        CFLintPluginInfo config = new CFLintPluginInfo();
        PluginInfoRule rule = new CFLintPluginInfo.PluginInfoRule();
        config.getRules().add(rule);
        rule.setName("OPM");
        PluginMessage message = new PluginMessage();
        rule.getMessages().add(message);
        message.setCode("code");
        message.setMessageText("messageText");
        message.setSeverity(Levels.WARNING);

        Marshaller jaxbMarshaller = ConfigUtils.createMarshaller();

        // output pretty printed
        jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

        StringWriter sw = new StringWriter();
        jaxbMarshaller.marshal(config, sw);
        assertEquals(expected, sw.toString().trim());
    }

    @Test
    /**
     * Test the round trip of the config json file including rule groups.
     * 
     * @throws JsonGenerationException
     * @throws JsonMappingException
     * @throws IOException
     */
    public void testRuleGroups() throws JsonGenerationException, JsonMappingException, IOException {
        CFLintPluginInfo config = new CFLintPluginInfo();
        PluginInfoRule rule = new CFLintPluginInfo.PluginInfoRule();
        config.getRules().add(rule);
        rule.setName("OPM");
        PluginMessage message = new PluginMessage();
        rule.getMessages().add(message);
        message.setCode("MyCode");
        message.setMessageText("messageText");
        message.setSeverity(Levels.WARNING);
        RuleGroup ruleGroup = new RuleGroup("r1");
        ruleGroup.setDefaultSeverity(Levels.INFO);
        ruleGroup.getMessages().add(message);
        config.getRuleGroups().add(ruleGroup);
        RuleGroup ruleGroup2 = new RuleGroup("r2");
        config.getRuleGroups().add(ruleGroup2);
        String jsonText = ConfigUtils.marshalJson(config);
        CFLintPluginInfo backConfig = ConfigUtils.unmarshalJson(jsonText, CFLintPluginInfo.class);
        assertEquals("MyCode", backConfig.getRules().get(0).getMessages().get(0).getCode());
        assertEquals("messageText", backConfig.getRules().get(0).getMessages().get(0).getMessageText());
        assertEquals("MyCode", backConfig.getRuleGroups().get(0).getMessages().get(0).getCode());
        assertEquals("messageText", backConfig.getRuleGroups().get(0).getMessages().get(0).getMessageText());
        assertEquals(Levels.INFO, backConfig.getRuleGroups().get(0).getDefaultSeverity());
    }

    @Test
    public void testConfigRC() throws Exception {
        final ConfigBuilder configBuilder = new ConfigBuilder().addCustomConfig("src/test/resources/com/cflint/tests/Config/.cflintrc");
        cfBugs = new CFLintAPI(configBuilder.build());
        final String scriptSrc =
            "component {\r\n" + "function test() {\r\n" +
            "	var thisIsNotTooLong = \"Fred\";\r\n" +
            "	var thisIsTooLongForTheDefault = \"Fred\";\r\n" +
            "	var thisIsTooLongEvenForTheCustomConfig = \"Fred\";\r\n" +
            "}\r\n" + "}";
        CFLintResult lintResult = cfBugs.scan(scriptSrc, "test");
        final List<BugInfo> result = lintResult.getIssues().values().iterator().next();
        assertEquals(1, result.size());
        assertEquals("VAR_TOO_LONG", result.get(0).getMessageCode());
        assertEquals(5, result.get(0).getLine());
    }
}
