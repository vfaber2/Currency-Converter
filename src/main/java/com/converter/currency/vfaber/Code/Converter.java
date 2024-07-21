package com.converter.currency.vfaber.Code;

import com.converter.currency.vfaber.exceptions.CurrencyException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;


public class Converter {

    private final Map<String, String> currencyCodeToNameMap = new HashMap<>();
    private final Map<String, String> currencyNameToCodeMap = new HashMap<>();

    public Map<String, String> getCurrencyNameToCodeMap() {
        return currencyNameToCodeMap;
    }

    public Map<String, String> getSortedCurrencyCodeToNameMap() {
        return new TreeMap<>(currencyNameToCodeMap);
    }

    /**
     * Makes a request to get all the available currencies for conversion
     *
     * @return currency-names as string in JSON format
     */
    public String requestAvailableCurrencies() {
        try (HttpClient client = HttpClient.newHttpClient()) {
            String url = "https://cdn.jsdelivr.net/npm/@fawazahmed0/currency-api@latest/v1/currencies.min.json";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(url))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            return response.body();
        } catch (IOException | InterruptedException | URISyntaxException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Makes a request to get the conversion rates of a specific currency
     *
     * @param currencyCode code of the currency which will get converted
     * @return conversions of the given currency
     */
    private String requestConversionsForSpecificCurrency(String currencyCode) {

        String url = "https://cdn.jsdelivr.net/npm/@fawazahmed0/currency-api@latest/v1/currencies/" + currencyCode + ".min.json";

        try (HttpClient client = HttpClient.newHttpClient()) {

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(url))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            return response.body();
        } catch (IOException | InterruptedException | URISyntaxException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Map<String, Double> createCurrencyConversionMap(String currencyCode) {

        String input = requestConversionsForSpecificCurrency(currencyCode);

        assert input != null;
        JSONObject jsonObject = new JSONObject(input);

        JSONObject currencyObject = jsonObject.optJSONObject(currencyCode);

        Map<String, Double> currencyConversionMap = new HashMap<>();
        Iterator<String> keys = currencyObject.keys();

        while (keys.hasNext()) {
            String key = keys.next();
            double value = currencyObject.getDouble(key);
            currencyConversionMap.put(key, value);
        }

        return currencyConversionMap;
    }

    /**
     * Takes the JSON String of currencies and converts them into 2 maps:
     * One currency-name to -code;
     * One currency-code to -name
     *
     * @param currencies input string in JSON format
     */
    public void mappingCurrencyNames(String currencies) {
        currencies = currencies.replace("{", "").replace("}", "");

        String[] currenciesArray = currencies.split(",");

        Arrays.stream(currenciesArray).forEach(currency -> {

            String[] currencySplit = currency.split(":");
            String currencyCode = currencySplit[0].replace("\"", "");
            String currencyName = currencySplit[1].replace("\"", "");
            currencyCodeToNameMap.put(currencyCode, currencyName);
        });
        currencyCodeToNameMap.forEach((code, name) -> {
            if (!name.isEmpty()) currencyNameToCodeMap.put(name, code);
        });

    }

    /**
     * Converts currency-name to the matching currency-code
     *
     * @param currencyName name of the currency
     * @return currency-code
     */
    private String convertCurrencyNameToCode(String currencyName) {
        if (!currencyNameToCodeMap.containsKey(currencyName))
            throw new CurrencyException("Currency not found: " + currencyName);
        return currencyNameToCodeMap.get(currencyName);
    }

    /**
     * Searches the exchange rate from a specific currency to another
     *
     * @param fromCurrencyCode currency you have
     * @param toCurrencyCode   currency you want to convert to
     * @return exchange rate between the 2 currencies
     */
    public double getExchangeRate(String fromCurrencyCode, String toCurrencyCode) {
        var conversioonMap = createCurrencyConversionMap(fromCurrencyCode);
        return conversioonMap.get(toCurrencyCode);
    }

    /**
     * Calculates the conversion of the currency
     *
     * @param rate   conversion rate
     * @param amount amount of currency to convert
     * @return amount of converted currency
     */
    public double calculateConversion(double rate, double amount) {
        return amount * rate;
    }

    /**
     * Initializes the converter, makes the request oof the currencies and fills the maps
     */
    public void init() {
        currencyCodeToNameMap.clear();
        currencyNameToCodeMap.clear();

        mappingCurrencyNames(requestAvailableCurrencies());

    }

    /**
     * Actual converter function
     *
     * @param fromCurrencyName starting currency
     * @param toCurrencyName   resulting currency
     * @param amount           amount of currency
     * @return amount of converted currency
     */
    public double convert(String fromCurrencyName, String toCurrencyName, double amount) {

        if (currencyNameToCodeMap.isEmpty() || currencyCodeToNameMap.isEmpty()) init();

        String fromCurrencyCode = convertCurrencyNameToCode(fromCurrencyName);
        String toCurrencyCode = convertCurrencyNameToCode(toCurrencyName);

        double exchangeRate = getExchangeRate(fromCurrencyCode, toCurrencyCode);

        return calculateConversion(exchangeRate, amount);

    }
}
