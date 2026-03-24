package com.femtendo.realecon.client;

import java.util.HashMap;
import java.util.Map;

public class ClientMarketData {
    public static final Map<String, Double> PRICES = new HashMap<>();

    public static int tradesSinceUpdate = 0;
    public static int tradesUntilUpdate = 10;
}