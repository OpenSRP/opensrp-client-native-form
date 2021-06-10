package com.vijay.jsonwizard.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.interactors.JsonFormInteractor;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import timber.log.Timber;

import static com.vijay.jsonwizard.constants.JsonFormConstants.DYNAMIC_LABEL_IMAGE_SRC;
import static com.vijay.jsonwizard.constants.JsonFormConstants.DYNAMIC_LABEL_TEXT;
import static com.vijay.jsonwizard.constants.JsonFormConstants.DYNAMIC_LABEL_TITLE;
import static com.vijay.jsonwizard.constants.JsonFormConstants.KEY;
import static com.vijay.jsonwizard.constants.JsonFormConstants.MLS.PROPERTIES_FILE_NAME;
import static com.vijay.jsonwizard.constants.JsonFormConstants.STEP;
import static com.vijay.jsonwizard.constants.JsonFormConstants.TYPE;
import static com.vijay.jsonwizard.utils.NativeFormLangUtils.getEscapedValue;
import static com.vijay.jsonwizard.utils.Utils.getFileContentsAsString;

/**
 * Created by Vincent Karuri on 12/03/2020
 */
public class JsonFormMLSAssetGenerator {

    private static Map<String, String> placeholdersToTranslationsMap = new HashMap<>();
    private static String formName;

    private static JsonFormInteractor jsonFormInteractor;

    /**
     * Processes the {@param formToTranslate} outputting a placeholder-injected form
     * and its corresponding property file
     *
     * @param formToTranslate
     */
    public static void processForm(String formToTranslate) throws Exception {
        jsonFormInteractor = getJsonFormInteractor();
        String form = getFileContentsAsString(formToTranslate);

        printToSystemOut("\nForm before placeholder injection:\n\n" + form);

        String[] formPath = formToTranslate.split(File.separator);
        formName = formPath[formPath.length - 1].split("\\.")[0];

        JsonObject formJson = stringToJson(form);
        JsonObject placeholderInjectedForm = injectPlaceholders(formJson, formName, formJson.has(JsonFormConstants.CONTENT_FORM));
        placeholderInjectedForm.addProperty(PROPERTIES_FILE_NAME, formName);

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String placeholderInjectedFormStr = gson.toJson(placeholderInjectedForm, JsonObject.class);
        writeToFile(placeholderInjectedFormStr, File.separator + getMLSAssetsFolder() + File.separator + formName + ".json");

        printToSystemOut("\n\nPlaceholder-injected form: \n\n " + placeholderInjectedFormStr);

        createTranslationsPropertyFile();
    }

    /**
     * Returns the path to which generated assets should be stored
     *
     * @return
     */
    public static String getMLSAssetsFolder() {
        String mlsAssetsFolder = System.getenv("MLS_ASSETS_FOLDER");
        return mlsAssetsFolder == null ? "tmp" : mlsAssetsFolder;
    }

    /**
     * Converts a {@link String} representation of JSON into a {@link JsonObject}
     *
     * @param json
     * @return
     */
    private static JsonObject stringToJson(String json) {
        return new Gson().fromJson(json, JsonObject.class);
    }

    /**
     * Replaces {@link String} literals in the {@param jsonForm} with placeholders.
     * <p>
     * The {@link String} literals (fields) to be replaced have to be defined either globally or as part of a widget's definition.
     * <p>
     * The placeholders follow the scheme : {{form_name.step_name.widget_key.field_identifier}} for widget fields
     * <p>
     * And {{form_name.step_name.field_identifier}} for step-level fields
     *
     * @param jsonForm
     * @param formName
     * @return
     */
    private static JsonObject injectPlaceholders(JsonObject jsonForm, String formName, boolean isSubForm) {
        if (isSubForm) {
            jsonForm.addProperty(JsonFormConstants.COUNT, 1);
        }
        for (int i = 1; i <= getNumOfSteps(jsonForm); i++) {
            String stepName = STEP + i;
            String placeholderStringPrefix = formName + "." + stepName;
            replaceStepStringLiterals(placeholderStringPrefix, getStepJsonObject(jsonForm, stepName));
            replaceWidgetStringLiterals(placeholderStringPrefix, getWidgets(jsonForm, stepName));
        }
        return jsonForm;
    }

    /**
     * Replaces {@link String} literals at step level with the appropriate placeholders
     *
     * @param stepJsonObject
     * @param placeholderStrPrefix
     */
    private static void replaceStepStringLiterals(String placeholderStrPrefix, JsonObject stepJsonObject) {
        if (stepJsonObject == null) {
            return;
        }
        for (String stepField : jsonFormInteractor.getDefaultTranslatableStepFields()) {
            JsonElement strLiteralElement = stepJsonObject.get(stepField);
            if (strLiteralElement != null) {
                String propertyName = placeholderStrPrefix + "." + stepField;
                String placeholderStr = "{{" + propertyName + "}}";
                placeholdersToTranslationsMap.put(propertyName, strLiteralElement.getAsString());
                stepJsonObject.addProperty(stepField, placeholderStr);
            }
        }
    }

    /**
     * Replaces {@link String} literals in widgets with the appropriate placeholders
     *
     * @param rootPlaceholderPrefix
     * @param stepWidgets
     */
    private static void replaceWidgetStringLiterals(String rootPlaceholderPrefix, JsonArray stepWidgets) {
        for (int i = 0; i < stepWidgets.size(); i++) {
            JsonObject widget = stepWidgets.get(i).getAsJsonObject();
            String widgetKey = widget.get(KEY).getAsString();

            Set<String> translatableWidgetFields = jsonFormInteractor
                    .map
                    .get(widget.get(TYPE).getAsString())
                    .getCustomTranslatableWidgetFields();

            translatableWidgetFields.addAll(jsonFormInteractor.getDefaultTranslatableWidgetFields());

            for (String fieldIdentifier : translatableWidgetFields) {
                // Split the widget field identifier into it's constituent keys
                // and traverse the widget json object to get to the
                // base parent element(s) (the element(s) with a field to be replaced)
                String[] fieldIdentifierKeys = fieldIdentifier.split("\\.");
                JsonObject parentJsonObj = widget;
                JsonElement parentElement = widget;
                StringBuilder fieldIdentifierPrefix = new StringBuilder();
                for (int j = 0; j < fieldIdentifierKeys.length - 1; j++) {
                    String constituentFieldIdentifierKey = fieldIdentifierKeys[j];
                    parentElement = parentJsonObj.get(constituentFieldIdentifierKey);
                    if (parentElement != null) {
                        fieldIdentifierPrefix.append(constituentFieldIdentifierKey);
                        if (parentElement instanceof JsonArray) {
                            break; // support only one level of json array placeholder injection
                        } else {
                            parentJsonObj = parentElement.getAsJsonObject();
                        }
                    }
                }

                // package parent element(s) into json array
                if (parentElement != null) {
                    JsonArray parentElementsArr = new JsonArray();
                    parentElementsArr.add(parentElement);
                    parentElementsArr = parentElement instanceof JsonArray ? parentElement.getAsJsonArray() : parentElementsArr;

                    // if parent element is the widget itself, don't modify placeholder prefix
                    String widgetPlaceholderPrefix;
                    if (fieldIdentifierPrefix.toString().isEmpty()) {
                        widgetPlaceholderPrefix = rootPlaceholderPrefix;
                    } else {
                        widgetPlaceholderPrefix = rootPlaceholderPrefix + "." + widgetKey + "." + fieldIdentifierPrefix;
                    }

                    performReplacements(parentElementsArr, widgetPlaceholderPrefix, fieldIdentifierKeys[fieldIdentifierKeys.length - 1]);
                }
            }
        }
    }


    /**
     * For each parent element in {@param parentElements}, replaces the {@param fieldToReplace}
     * with the appropriate placeholder derived from {@param placeholderPrefix}
     *
     * @param parentElements
     * @param placeholderPrefix
     * @param fieldToReplace
     */
    private static void performReplacements(JsonArray parentElements, String placeholderPrefix, String fieldToReplace) {
        if (parentElements == null) {
            return;
        }

        for (int i = 0; i < parentElements.size(); i++) {
            StringBuilder propertyName = new StringBuilder(placeholderPrefix);
            JsonObject parentElement = parentElements.get(i).getAsJsonObject();
            JsonElement parentElementKey = parentElement.get(KEY);

            // add unique key identifier if it exists
            if (parentElementKey != null) {
                propertyName.append(".").append(parentElementKey.getAsString());
            }
            propertyName = new StringBuilder(propertyName.toString().replaceAll("\\s", "_") + "." + fieldToReplace);

            JsonElement fieldValueToReplace = parentElement.get(fieldToReplace);

            JsonArray placeholderArray = new JsonArray();

            if (fieldValueToReplace instanceof JsonPrimitive) {

                String placeholderStr = "{{" + propertyName.toString() + "}}";
                placeholdersToTranslationsMap.put(propertyName.toString(), fieldValueToReplace.getAsString());
                parentElement.addProperty(fieldToReplace, placeholderStr);
            } else if (fieldValueToReplace instanceof JsonArray) {

                if (fieldToReplace.equals(JsonFormConstants.VALUES)) {
                    JsonArray elements = fieldValueToReplace.getAsJsonArray();
                    for (int j = 0; j < elements.size(); j++) {

                        StringBuilder propertyNameArray = new StringBuilder(propertyName);
                        propertyNameArray.append("[").append(j).append("]");
                        String placeholderStr = "{{" + propertyNameArray.toString() + "}}";
                        placeholdersToTranslationsMap.put(propertyNameArray.toString(), elements.get(j).getAsString());
                        placeholderArray.add(placeholderStr);
                        if (j == elements.size() - 1) {
                            parentElement.add(fieldToReplace, placeholderArray);
                            placeholderArray = new JsonArray();
                        }
                    }
                } else if (fieldToReplace.equals(JsonFormConstants.DYNAMIC_LABEL_INFO)) {

                    JsonArray labelsArray = parentElement.get(JsonFormConstants.DYNAMIC_LABEL_INFO).getAsJsonArray();
                    for (int j = 0; j < labelsArray.size(); j++) {
                        JsonObject jsonObject = labelsArray.get(j).getAsJsonObject();

                        StringBuilder propertyNameArray = new StringBuilder(propertyName);
                        propertyNameArray.append("[").append(j).append("]");

                        JsonObject placeHolderObject = new JsonObject();
                        placeHolderObject.addProperty(DYNAMIC_LABEL_TITLE,
                                "{{" + propertyNameArray.toString() + "." + DYNAMIC_LABEL_TITLE + "}}");

                        placeHolderObject.addProperty(DYNAMIC_LABEL_TEXT,
                                "{{" + propertyNameArray.toString() + "." + DYNAMIC_LABEL_TEXT + "}}");
                        placeHolderObject.addProperty(DYNAMIC_LABEL_IMAGE_SRC,
                                "{{" + propertyNameArray.toString() + "." + DYNAMIC_LABEL_IMAGE_SRC + "}}");

                        placeholdersToTranslationsMap.put(propertyNameArray.toString() + "." + DYNAMIC_LABEL_TITLE, jsonObject.get(DYNAMIC_LABEL_TITLE).getAsString());
                        placeholdersToTranslationsMap.put(propertyNameArray.toString() + "." + DYNAMIC_LABEL_TEXT, jsonObject.get(DYNAMIC_LABEL_TEXT).getAsString());
                        placeholdersToTranslationsMap.put(propertyNameArray.toString() + "." + DYNAMIC_LABEL_IMAGE_SRC, jsonObject.get(DYNAMIC_LABEL_IMAGE_SRC).getAsString());
                        placeholderArray.add(placeHolderObject);

                        if (j == labelsArray.size() - 1) {
                            parentElement.add(fieldToReplace, placeholderArray);
                            placeholderArray = new JsonArray();
                        }
                    }
                }
            }
        }
    }

    /**
     * Gets all the widget definitions in a particular {@param step} of the {@param jsonForm}
     * or in the {@code JsonFormConstants.CONTENT_FORM} portion of a sub-form
     *
     * @param jsonForm
     * @param step
     * @return
     */
    private static JsonArray getWidgets(JsonObject jsonForm, String step) {
        JsonArray formFields = jsonForm.has(JsonFormConstants.CONTENT_FORM) ? jsonForm.get(JsonFormConstants.CONTENT_FORM).getAsJsonArray() : null;
        return formFields == null ? getStepJsonObject(jsonForm, step).getAsJsonArray(JsonFormConstants.FIELDS) : formFields;
    }

    /**
     * Get the {@link JsonObject} representation of a {@param jsonForm} {@param step}
     *
     * @param jsonForm
     * @param step
     * @return
     */
    private static JsonObject getStepJsonObject(JsonObject jsonForm, String step) {
        return jsonForm.getAsJsonObject(step);
    }

    /**
     * Extracts the number of steps in a {@param jsonForm}
     *
     * @param jsonForm
     * @return
     */
    private static int getNumOfSteps(JsonObject jsonForm) {
        return jsonForm.has(JsonFormConstants.COUNT) ? jsonForm.get(JsonFormConstants.COUNT).getAsInt() : -1;
    }

    /**
     * Utility that prints to system out for debugging
     *
     * @param str
     */
    private static void printToSystemOut(String str) {
        System.out.println(str);
    }

    /**
     * Creates a property file and writes it to disk
     */
    private static void createTranslationsPropertyFile() {
        StringBuilder stringBuilder = new StringBuilder();
        for (Map.Entry<String, String> entry : placeholdersToTranslationsMap.entrySet()) {
            stringBuilder.append(entry.getKey() + " = " + getEscapedValue(entry.getValue()) + "\n");
        }
        writeToFile(stringBuilder.toString(), File.separator + getMLSAssetsFolder() + File.separator + formName + ".properties");
    }

    /**
     * Writes {@param data} to disk at the specified {@param path}
     *
     * @param data
     * @param path
     */
    private static void writeToFile(String data, String path) {
        try {
            Files.write(Paths.get(path), data.getBytes());
        } catch (IOException e) {
            Timber.e(e);
        }
    }

    private static JsonFormInteractor getJsonFormInteractor() throws Exception {
        String jsonFormInteractorName = System.getenv("JSON_FORM_INTERACTOR_NAME");
        jsonFormInteractorName = jsonFormInteractorName == null
                ? "com.vijay.jsonwizard.interactors.JsonFormInteractor" : jsonFormInteractorName;
        Class<?> clazz = Class.forName(jsonFormInteractorName);
        Method factoryMethod = clazz.getDeclaredMethod("getInstance");
        return (JsonFormInteractor) factoryMethod.invoke(null, (Object[]) null);
    }

    public static void main(String[] args) throws Exception {
        String formToTranslate = System.getenv("FORM_TO_TRANSLATE");
        printToSystemOut("Injecting placeholders in form at path: " + formToTranslate + " ...\n");
        processForm(formToTranslate);
    }
}