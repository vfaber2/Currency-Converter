package com.converter.currency.vfaber.Code;

import com.converter.currency.vfaber.exceptions.CurrencyException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


public class Converter {

    private final Map<String, String> currencyCodeToNameMap = new HashMap<>();
    private final Map<String, String> currencyNameToCodeMap = new HashMap<>();

    public Map<String, String> getCurrencyCodeToNameMap() {
        return currencyCodeToNameMap;
    }

    public Map<String, String> getCurrencyNameToCodeMap() {
        return currencyNameToCodeMap;
    }

    public void fillMaps() {
        var currencies = getAvailableCurrencies();
        mappingCurrencyNames(currencies);
    }

    public String getAvailableCurrencies() {
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


    private String getConversionsForSpecificCurrency(String currencyCode) {

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

        String input = getConversionsForSpecificCurrency(currencyCode);

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

    private String getCurrencyCode(String currencyName) {
        if (!currencyNameToCodeMap.containsKey(currencyName))
            throw new CurrencyException("Currency not found: " + currencyName);
        return currencyNameToCodeMap.get(currencyName);
    }

    public double getExchangeRate(String fromCurrencyCode, String toCurrencyCode) {

        var conversioonMap = createCurrencyConversionMap(fromCurrencyCode);
        return conversioonMap.get(toCurrencyCode);

    }

    public double calculateConversion(double rate, double amount) {
        return amount * rate;
    }

    public static void main(String[] args) {
        Converter converter = new Converter();

        converter.mappingCurrencyNames(converter.getAvailableCurrencies());

        String currencyCodeEuro = converter.getCurrencyCode("Euro");
        String currencyCodeSwiss = converter.getCurrencyCode("Swiss Franc");

        var exchangeRate = converter.getExchangeRate(currencyCodeEuro, currencyCodeSwiss);

        var money = converter.calculateConversion(exchangeRate, 100);

        System.out.println(money);

    }

}
