package com.converter.currency.vfaber.GUI;

import com.converter.currency.vfaber.Code.Converter;

import javax.swing.*;
import java.awt.*;

public class Gui {

    private Converter converter = new Converter();

    public void addItemsToSelector(JComboBox<String> sourceCurrency) {
        if (converter.getCurrencyNameToCodeMap().isEmpty()) converter.init();
        converter.getSortedCurrencyCodeToNameMap().keySet().forEach(sourceCurrency::addItem);
    }


    public static void main(String[] args) {
        Gui gui = new Gui();
        Converter converter = new Converter();
        converter.init();

        // Create the frame
        JFrame frame = new JFrame("Currency Converter");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setSize(600, 400);

        // Create a panel to hold components
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        // Panel for currency selection and amount input
        JPanel middlePanel = new JPanel();
        middlePanel.setLayout(new GridLayout(4, 2));

        // Source currency selector
        JLabel sourceLabel = new JLabel("Source Currency:");
        JComboBox<String> sourceCurrency = new JComboBox<>();
        middlePanel.add(sourceLabel);
        middlePanel.add(sourceCurrency);
        gui.addItemsToSelector(sourceCurrency);

        // Target currency selector
        JLabel targetLabel = new JLabel("Target Currency:");
        JComboBox<String> targetCurrency = new JComboBox<>();
        middlePanel.add(targetLabel);
        middlePanel.add(targetCurrency);
        gui.addItemsToSelector(targetCurrency);

        // Amount input
        JLabel amountLabel = new JLabel("Amount:");
        JTextField amountField = new JTextField();
        middlePanel.add(amountLabel);
        middlePanel.add(amountField);

        // Convert button
        JButton convertButton = new JButton("Convert");
        middlePanel.add(new JLabel()); // Empty label for spacing
        middlePanel.add(convertButton);

        // Text area for output result
        JTextArea outputTextArea = new JTextArea(5, 40);
        outputTextArea.setLineWrap(true);
        outputTextArea.setEditable(false);
        JScrollPane outputScrollPane = new JScrollPane(outputTextArea);
        panel.add(outputScrollPane, BorderLayout.SOUTH);

        convertButton.addActionListener(e -> {
            String sourceCurrencyName = (String) sourceCurrency.getSelectedItem();
            String targetCurrencyName = (String) targetCurrency.getSelectedItem();
            try {
                double amount = Double.parseDouble(amountField.getText());
                double result = converter.convert(sourceCurrencyName, targetCurrencyName, amount);
                outputTextArea.setText(String.format("Converted Amount: %.4f %s", result, targetCurrencyName));
            } catch (NumberFormatException ex) {
                outputTextArea.setText("Please enter a valid amount.");
            } catch (Exception ex) {
                outputTextArea.setText("Error during conversion: " + ex.getMessage());
            }
        });

        panel.add(middlePanel, BorderLayout.CENTER);

        // Add panel to frame
        frame.getContentPane().add(panel);

        // Set the frame to be visible
        frame.setVisible(true);
    }
}
