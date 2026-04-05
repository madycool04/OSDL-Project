package com.hotel.util;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Handles permanent file-based storage using Java Serialization.
 * Data is stored in the user's home directory under .hotelms/
 */
public class FileStorageUtil {

    private static final Logger LOGGER = Logger.getLogger(FileStorageUtil.class.getName());
    private static final String DATA_DIR = System.getProperty("user.home") + File.separator + ".hotelms";

    static {
        try {
            Files.createDirectories(Paths.get(DATA_DIR));
        } catch (IOException e) {
            LOGGER.severe("Could not create data directory: " + e.getMessage());
        }
    }

    /**
     * Save a list of objects to a file.
     */
    public static <T extends Serializable> void saveList(List<T> list, String filename) {
        String path = DATA_DIR + File.separator + filename;
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new BufferedOutputStream(new FileOutputStream(path)))) {
            oos.writeObject(new ArrayList<>(list));
            LOGGER.info("Saved " + list.size() + " items to " + filename);
        } catch (IOException e) {
            LOGGER.severe("Error saving to " + filename + ": " + e.getMessage());
        }
    }

    /**
     * Load a list of objects from a file.
     */
    @SuppressWarnings("unchecked")
    public static <T extends Serializable> List<T> loadList(String filename) {
        String path = DATA_DIR + File.separator + filename;
        File file = new File(path);
        if (!file.exists()) {
            LOGGER.info("No existing data found for " + filename);
            return new ArrayList<>();
        }
        try (ObjectInputStream ois = new ObjectInputStream(
                new BufferedInputStream(new FileInputStream(path)))) {
            List<T> list = (List<T>) ois.readObject();
            LOGGER.info("Loaded " + list.size() + " items from " + filename);
            return list;
        } catch (IOException | ClassNotFoundException e) {
            LOGGER.severe("Error loading from " + filename + ": " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Save a single object to a file.
     */
    public static <T extends Serializable> void saveObject(T obj, String filename) {
        String path = DATA_DIR + File.separator + filename;
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new BufferedOutputStream(new FileOutputStream(path)))) {
            oos.writeObject(obj);
        } catch (IOException e) {
            LOGGER.severe("Error saving object to " + filename + ": " + e.getMessage());
        }
    }

    /**
     * Load a single object from a file.
     */
    @SuppressWarnings("unchecked")
    public static <T extends Serializable> T loadObject(String filename, T defaultValue) {
        String path = DATA_DIR + File.separator + filename;
        File file = new File(path);
        if (!file.exists()) return defaultValue;
        try (ObjectInputStream ois = new ObjectInputStream(
                new BufferedInputStream(new FileInputStream(path)))) {
            return (T) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            LOGGER.severe("Error loading object from " + filename + ": " + e.getMessage());
            return defaultValue;
        }
    }

    public static String getDataDir() {
        return DATA_DIR;
    }
}
