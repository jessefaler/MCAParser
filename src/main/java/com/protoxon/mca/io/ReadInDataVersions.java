package com.protoxon.mca.io;

import com.protoxon.mca.Region_old;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

public class ReadInDataVersions {

    private static HashMap<Integer, String> dataVersions;

    /**
     * Retrieves the Minecraft version corresponding to the given data version.
     * This method initializes the dataVersion HashMap on the first call,
     * ensuring that it is only loaded when necessary.
     *
     * @param dataVersion The data version number.
     * @return A String specifying the Minecraft version.
     */
    public static String getMCVersionFromDataVersion(int dataVersion) {
        if(dataVersions == null) {
            readInDataVersions();
        }
        return dataVersions.get(dataVersion);
    }

    /**
     * Reads the data versions file and populates the Data_Versions HashMap.
     * The file is expected to be in the format "minecraftVersion, dataVersion" on each line.
     */
    private static void readInDataVersions() {
        dataVersions = new HashMap<>(556);
        try (InputStream inputStream = Region_old.class.getResourceAsStream("/Data_Versions");
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {

            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(", ");
                if (parts.length == 2) {
                    String minecraftVersion = parts[0];
                    int dataVersion = Integer.parseInt(parts[1]);
                    dataVersions.put(dataVersion, minecraftVersion);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
