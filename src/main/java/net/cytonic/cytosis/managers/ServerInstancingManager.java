package net.cytonic.cytosis.managers;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.data.containers.servers.*;
import net.cytonic.cytosis.logging.Logger;
import net.cytonic.cytosis.messaging.Subjects;

public class ServerInstancingManager {

    public static final String CYTOSIS = "cytosis";
    public static final String CYNDER = "cynder";
    public static final String GILDED_GORGE_HUB = "gg_hub";
    public static final String GILDED_GORGE_INSTANCING = "gg_instancing";
    public static final String CYTONIC_LOBBY = "cytonic_lobby";
    public static final String BEDWARS_SOLOS = "bw_solos";
    public static final String BEDWARS_LOBBY = "bw_lobby";
    public static final String BEDWARS_DUOS = "bw_duos";
    public static final String BEDWARS_TRIOS = "bw_trios";
    public static final String BEDWARS_QUADROS = "bw_quadros";

    public static final String[] TYPES = {CYTOSIS, CYNDER, GILDED_GORGE_HUB, GILDED_GORGE_INSTANCING, CYTONIC_LOBBY, BEDWARS_SOLOS, BEDWARS_LOBBY, BEDWARS_DUOS, BEDWARS_TRIOS, BEDWARS_QUADROS};


    public static boolean isServerType(String type) {
        for (String t : TYPES) {
            if (t.equals(type)) {
                return true;
            }
        }
        return false;
    }

    public void createServerInstances(String type, int amount) {
        if (!isServerType(type)) return;
        CreateInstanceContainer container = new CreateInstanceContainer(type, amount);
        Cytosis.getNatsManager().request(Subjects.CREATE_SERVER, container.serialize(), (message, throwable) -> {
            if (throwable != null) {
                Logger.error("Failed to create server instance: " + type, throwable);
                return;
            }
            InstanceResponseContainer resp = InstanceResponseContainer.parse(message.getData());
            if (!resp.success()) {
                Logger.error("Failed to create server instance of type %s. (%s)", type, resp.message());
            }
        });
    }

    public void deleteAllServerInstances(String type) {
        if (!isServerType(type)) return;
        DeleteAllInstancesContainer container = new DeleteAllInstancesContainer(type);
        Cytosis.getNatsManager().request(Subjects.DELETE_ALL_SERVERS, container.serialize(), (message, throwable) -> {
            if (throwable != null) {
                Logger.error("Failed to delete all server instances: " + type, throwable);
                return;
            }
            InstanceResponseContainer resp = InstanceResponseContainer.parse(message.getData());
            if (!resp.success()) {
                Logger.error("Failed to delete all server instances of type %s. (%s)", type, resp.message());
            }
        });
    }

    public void deleteThisServerInstance() {
        if (!Cytosis.IS_NOMAD) return; // this isn't a nomad server to delete
        deleteServerInstance(System.getenv("NOMAD_JOB_NAME"), System.getenv("NOMAD_ALLOC_ID"));
    }

    public void deleteServerInstance(String type, String allocId) {
        if (!isServerType(type)) return;
        DeleteInstanceContainer container = new DeleteInstanceContainer(type, allocId);
        Cytosis.getNatsManager().request(Subjects.DELETE_SERVER, container.serialize(), (message, throwable) -> {
            if (throwable != null) {
                Logger.error("Failed to delete server instance: " + allocId, throwable);
                return;
            }
            InstanceResponseContainer resp = InstanceResponseContainer.parse(message.getData());
            if (!resp.success()) {
                Logger.error("Failed to delete server instance %s of type %s. (%s)", allocId, type, resp.message());
            }
        });
    }

    public void updateServers(String type) {
        if (!isServerType(type)) return;
        UpdateInstancesContainer container = new UpdateInstancesContainer(type);
        Cytosis.getNatsManager().request(Subjects.UPDATE_SERVER, container.serialize(), (message, throwable) -> {
            if (throwable != null) {
                Logger.error("Failed to update server instance type: " + type, throwable);
                return;
            }
            InstanceResponseContainer resp = InstanceResponseContainer.parse(message.getData());
            if (!resp.success()) {
                Logger.error("Failed to update server instances of type %s. (%s)", type, resp.message());
            }
        });
    }
}
