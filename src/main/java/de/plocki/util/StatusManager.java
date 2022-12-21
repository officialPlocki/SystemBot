package de.plocki.util;

import de.plocki.util.files.FileBuilder;

import java.util.ArrayList;
import java.util.List;

public class StatusManager {

    public boolean checkLastStatus(long id) {
        return (boolean) new Hooks().fromFile("lastStatus." + id);
    }

    public void setLastStatus(long id, boolean status) {
        new Hooks().toFile("lastStatus." + id, status);
    }

    public void registerStatusEmbed(long messageID, String address, String name) {
        FileBuilder builder = new FileBuilder("data.yml");
        if(!builder.getYaml().isSet("statusEmbedID")) {
            builder.getYaml().set("statusEmbeds", new ArrayList<Long>());
            builder.save();
        }
        List<Long> list = builder.getYaml().getLongList("statusEmbeds");
        list.add(messageID);
        builder.getYaml().set("statusEmbeds", list);
        builder.getYaml().set("statusEmbedStatus." + messageID, Status.ORANGE.name());
        builder.save();
        new Hooks().toFile("statusEmbed." + messageID, address);
        new Hooks().toFile("statusEmbedName." + messageID, name);
    }

    public void setStatus(long messageID, Status status) {
        new Hooks().toFile("statusEmbedStatus." + messageID, status.name());
    }

    public Status getStatus(long messageID) {
        return Status.valueOf((String) new Hooks().fromFile("statusEmbedStatus." + messageID));
    }

    public String getAddress(long messageID) {
        return (String) new Hooks().fromFile("statusEmbed." + messageID);
    }

    public String getName(long messageID) {
        return (String) new Hooks().fromFile("statusEmbedName." + messageID);
    }

    public List<Long> getStatusEmbeds() {
        return new Hooks().fromFile("statusEmbeds") == null ? new ArrayList<>() : (List<Long>) new Hooks().fromFile("statusEmbeds");
    }

}
