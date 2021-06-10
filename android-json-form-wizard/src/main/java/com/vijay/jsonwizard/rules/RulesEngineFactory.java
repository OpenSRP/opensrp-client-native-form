package com.vijay.jsonwizard.rules;

import android.content.Context;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.vijay.jsonwizard.activities.JsonFormBaseActivity;
import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.factory.FileSourceFactoryHelper;
import com.vijay.jsonwizard.utils.Utils;

import org.jeasy.rules.api.Facts;
import org.jeasy.rules.api.Rule;
import org.jeasy.rules.api.RuleListener;
import org.jeasy.rules.api.Rules;
import org.jeasy.rules.api.RulesEngine;
import org.jeasy.rules.core.DefaultRulesEngine;
import org.jeasy.rules.core.RulesEngineParameters;
import org.jeasy.rules.mvel.MVELRule;
import org.jeasy.rules.mvel.MVELRuleFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import org.smartregister.client.utils.contract.ClientFormContract;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import timber.log.Timber;

public class RulesEngineFactory implements RuleListener {
    public final String TAG = RulesEngineFactory.class.getCanonicalName();
    private Context context;
    private RulesEngine defaultRulesEngine;
    private Map<String, Rules> ruleMap;
    private String RULE_FOLDER_PATH = "rule/";
    private Rules rules;
    private String selectedRuleName;
    private Gson gson;
    private RulesEngineHelper rulesEngineHelper;
    private Facts globalFacts;

    public RulesEngineFactory(Context context, Map<String, String> globalValues) {
        this.context = context;
        RulesEngineParameters parameters = new RulesEngineParameters().skipOnFirstAppliedRule(true);
        this.defaultRulesEngine = new DefaultRulesEngine(parameters);
        ((DefaultRulesEngine) this.defaultRulesEngine).registerRuleListener(this);
        this.ruleMap = new HashMap<>();
        gson = new Gson();
        this.rulesEngineHelper = new RulesEngineHelper();

        if (globalValues != null) {
            globalFacts = new Facts();
            for (Map.Entry<String, String> entry : globalValues.entrySet()) {
                globalFacts.put(RuleConstant.PREFIX.GLOBAL + entry.getKey(), getValue(entry.getValue()));
            }
        }
    }

    public RulesEngineFactory() {
    }

    private Rules getDynamicRulesFromJsonArray(@NonNull JSONArray jsonArray, @NonNull String type) {
        try {
            Rules rules = new Rules();
            JSONObject keyJsonObject = Utils.getJsonObjectFromJsonArray(JsonFormConstants.KEY, jsonArray);
            if (keyJsonObject != null) {
                String key = type + "/" + keyJsonObject.optString(JsonFormConstants.KEY);
                if (!ruleMap.containsKey(key)) {
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonRuleObject = jsonArray.optJSONObject(i);
                        if (jsonRuleObject != null && !jsonRuleObject.has(JsonFormConstants.KEY)) {
                            MVELRule rule = getDynamicRulesFromJsonObject(jsonRuleObject);
                            if (rule != null) {
                                rules.register(rule);
                            }
                        }
                    }
                    ruleMap.put(key, rules);
                }
                return ruleMap.get(key);
            }
            return null;
        } catch (Exception e) {
            Timber.e(e);
            return null;
        }
    }

    private MVELRule getDynamicRulesFromJsonObject(@NonNull JSONObject jsonObjectDynamicRule) {
        try {
            MVELRule dynamicMvelRule = new MVELRule();
            dynamicMvelRule.setDescription(jsonObjectDynamicRule.optString(RuleConstant.DESCRIPTION));
            dynamicMvelRule.setPriority(jsonObjectDynamicRule.optInt(RuleConstant.PRIORITY));
            dynamicMvelRule.when(jsonObjectDynamicRule.optString(RuleConstant.CONDITION));
            dynamicMvelRule.then(jsonObjectDynamicRule.optString(RuleConstant.ACTIONS));
            dynamicMvelRule.name(jsonObjectDynamicRule.optString(RuleConstant.NAME));
            return dynamicMvelRule;
        } catch (Exception e) {
            Timber.e(e);
            return null;
        }
    }


    public boolean getRelevance(@NonNull Facts relevanceFact, @NonNull String ruleFilename) {

        Facts facts = initializeFacts(relevanceFact);

        facts.put(RuleConstant.IS_RELEVANT, false);

        rules = getRulesFromAsset(RULE_FOLDER_PATH + ruleFilename);

        processDefaultRules(rules, facts);

        return facts.get(RuleConstant.IS_RELEVANT);
    }

    public boolean getDynamicRelevance(@NonNull Facts relevanceFact, @NonNull JSONArray rulesStrObject) {

        Facts facts = initializeFacts(relevanceFact);

        facts.put(RuleConstant.IS_RELEVANT, false);

        rules = getDynamicRulesFromJsonArray(rulesStrObject, JsonFormConstants.RELEVANCE);

        processDefaultRules(rules, facts);

        return facts.get(RuleConstant.IS_RELEVANT);
    }

    protected Facts initializeFacts(Facts facts) {
        if (globalFacts != null) {
            facts.asMap().putAll(globalFacts.asMap());
        }
        selectedRuleName = facts.get(RuleConstant.SELECTED_RULE);
        facts.put("helper", rulesEngineHelper);
        return facts;
    }


    public Rules getRulesFromAsset(String fileName) {

        try {
            if (!ruleMap.containsKey(fileName)) {

                if (context instanceof ClientFormContract.View) {
                    try {
                        BufferedReader bufferedReader = ((ClientFormContract.View) context).getRules(context, fileName);
                        ruleMap.put(fileName, MVELRuleFactory.createRulesFrom(bufferedReader));
                    } catch (Exception ex) {
                        ((ClientFormContract.View) context).handleFormError(true, fileName);
                        return null;
                    }
                } else {
                    ruleMap.put(fileName, FileSourceFactoryHelper.getFileSource(JsonFormBaseActivity.DATA_SOURCE).getRulesFromFile(context, fileName));
                }
            }
            return ruleMap.get(fileName);
        } catch (IOException e) {
            Timber.e(e, "%s getRulesFromAsset", this.getClass().getCanonicalName());
            return null;
        } catch (Exception e) {
            Timber.e(e);
            return null;
        }
    }

    protected void processDefaultRules(Rules rules, Facts facts) {
        defaultRulesEngine.fire(rules, facts);
    }

    protected Object getValue(String value) {
        String rawValue = value.trim();
        if (isList(rawValue)) {
            return gson.fromJson(rawValue, ArrayList.class);
        } else if ("true".equals(rawValue) || "false".equals(rawValue)) {
            return Boolean.valueOf(rawValue);
        } else {
            try {
                return Integer.valueOf(rawValue);
            } catch (NumberFormatException e) {
                try {
                    return Float.valueOf(rawValue);
                } catch (NumberFormatException e2) {
                    return rawValue;
                }
            }
        }
    }

    private boolean isList(String value) {
        return !value.isEmpty() && value.charAt(0) == '[';
    }

    public String getCalculation(Facts calculationFact, String ruleFilename) {
        //need to clean curValue map as constraint depend on valid values, empties wont do
        Facts facts = initializeFacts(calculationFact);
        facts.put(RuleConstant.CALCULATION, "");
        rules = getRulesFromAsset(RULE_FOLDER_PATH + ruleFilename);
        processDefaultRules(rules, facts);

        return formatCalculationReturnValue(facts.get(RuleConstant.CALCULATION));
    }

    public String getDynamicCalculation(@NonNull Facts calculationFact, @NonNull JSONArray rulesStrObject) {

        Facts facts = initializeFacts(calculationFact);

        facts.put(RuleConstant.CALCULATION, false);

        rules = getDynamicRulesFromJsonArray(rulesStrObject, JsonFormConstants.CALCULATION);

        processDefaultRules(rules, facts);

        return formatCalculationReturnValue(facts.get(RuleConstant.CALCULATION));
    }

    private String formatCalculationReturnValue(Object rawValue) {
        String value = String.valueOf(rawValue).trim();
        if (value.isEmpty()) {
            return "";
        } else if (rawValue instanceof Map) {
            return new JSONObject((Map<String, String>) rawValue).toString();
        } else if (value.contains(".")) {
            try {
                value = String.valueOf((float) Math.round(Float.valueOf(value) * 100) / 100);
            } catch (NumberFormatException e) {
                Timber.e(e, "%s formatCalculationReturnValue", this.getClass().getCanonicalName());
            }
        }
        return value;
    }

    public String getConstraint(Facts constraintFact, String ruleFilename) {
        Facts facts = initializeFacts(constraintFact);
        facts.put(RuleConstant.CONSTRAINT, "0");
        rules = getRulesFromAsset(RULE_FOLDER_PATH + ruleFilename);
        processDefaultRules(rules, facts);

        return formatCalculationReturnValue(facts.get(RuleConstant.CONSTRAINT));
    }

    @Override
    public boolean beforeEvaluate(Rule rule, Facts facts) {
        return selectedRuleName != null && selectedRuleName.equals(rule.getName());
    }

    @Override
    public void afterEvaluate(Rule rule, Facts facts, boolean evaluationResult) {
        //Overriden
    }

    @Override
    public void beforeExecute(Rule rule, Facts facts) {
        //Overriden
    }

    @Override
    public void onSuccess(Rule rule, Facts facts) {
        //Overriden
    }

    @Override
    public void onFailure(Rule rule, Facts facts, Exception exception) {
        //Overriden
    }

    public String getRulesFolderPath() {
        return RULE_FOLDER_PATH;
    }

    public void setRulesFolderPath(String path) {
        RULE_FOLDER_PATH = path;
    }

}
