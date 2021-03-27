package com.github.cukespace.examples;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * @author Edward Mann <ed.mann@edmann.com>
 *         <p>
 *         Created: Mar 20, 2021
 */
public class AbstractIntegrationTest {

    public static String getUrlContents(final String theUrl) {
        final StringBuilder content = new StringBuilder();

        try {
            final URL               url           = new URL(theUrl);
            final HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestProperty("Accept", "*/*");
            final BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(urlConnection.getInputStream()));

            String               line;

            while ((line = bufferedReader.readLine()) != null) {
                content.append(line + "\n");
            }
            bufferedReader.close();
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }

        return content.toString();
    }
}
