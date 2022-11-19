package de.plocki.util;

import de.plocki.util.files.FileBuilder;

public class Hooks {

    private final FileBuilder builder;

    public Hooks() {
        builder = new FileBuilder("data.yml");
    }

    public FileBuilder getFileBuilder() {
        return builder;
    }

    public Object fromFile(String key) {
        return builder.getYaml().get(key);
    }

    public void toFile(String key, Object val) {
        builder.getYaml().set(key, val);
        builder.save();
    }
}
