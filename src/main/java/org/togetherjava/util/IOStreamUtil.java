package org.togetherjava.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.function.Supplier;

public class IOStreamUtil {

  /**
   * Reads an {@link InputStreamReader} to a {@link String}.
   *
   * @param inputStreamSupplier the supplier for the input stream. The stream will be properly
   * closed
   * @return the read string
   * @throws RuntimeException if an error occurs (unhelpful, I know)
   */
  public static String readToString(Supplier<InputStream> inputStreamSupplier) {
    StringBuilder stringBuilder = new StringBuilder();

    try (InputStream inputStream = inputStreamSupplier.get();
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        BufferedReader reader = new BufferedReader(inputStreamReader)) {

      String tmp;
      while ((tmp = reader.readLine()) != null) {
        stringBuilder.append(tmp)
            .append(System.lineSeparator());
      }

    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    return stringBuilder.toString();
  }

  public static Supplier<InputStream> resource(String name) {
    return () -> IOStreamUtil.class.getResourceAsStream(name);
  }
}
