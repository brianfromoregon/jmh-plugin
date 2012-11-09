package com.brianfromoregon;

import com.google.caliper.Json;
import com.google.caliper.Result;
import com.google.common.base.Throwables;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;

public class ResultUploader {

    public String postResults(Result result, String postUrl, String apiKey, String proxyHostPort) {

        try {
            URL url = new URL(postUrl + apiKey + "/" + result.getRun().getBenchmarkName());
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection(getProxy(proxyHostPort));
            urlConnection.setDoOutput(true);
            String resultJson = Json.getGsonInstance().toJson(result);
            urlConnection.getOutputStream().write(resultJson.getBytes());
            if (urlConnection.getResponseCode() == 200) {
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(urlConnection.getInputStream()));
                try {
                    return in.readLine();
                } finally {
                    in.close();
                }
            }

            StringBuilder err = new StringBuilder();
            err.append("Posting to ").append(postUrl).append(" failed: ").append(urlConnection.getResponseMessage());
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(urlConnection.getInputStream()));
            try {
                String line;
                while ((line = reader.readLine()) != null) {
                    err.append("\n").append(line);
                }
            } finally {
                reader.close();
            }
            return err.toString();
        } catch (Exception e) {
            return "Posting to " + postUrl + " failed.\n" + Throwables.getStackTraceAsString(e);
        }
    }

    private Proxy getProxy(String proxyHostPort) {
        if (proxyHostPort == null || proxyHostPort.isEmpty()) {
            return Proxy.NO_PROXY;
        }

        String[] proxyHostAndPort = proxyHostPort.trim().split(":");
        return new Proxy(Proxy.Type.HTTP, new InetSocketAddress(
                proxyHostAndPort[0], Integer.parseInt(proxyHostAndPort[1])));
    }

}
