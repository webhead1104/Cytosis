package net.cytonic.cytosis.data.containers.servers;

import com.google.gson.Gson;

import java.util.UUID;

public record PlayerChangeServerContainer(UUID player, String oldServer, String newServer) {
    /**
     * Deserializes this object from a string
     *
     * @param json the serialized data
     * @return the deserailized object
     */
    public static PlayerChangeServerContainer deserialize(String json) {
        return new Gson().fromJson(json, PlayerChangeServerContainer.class);
    }

    /**
     * Serializes the container into a string
     *
     * @return the serialized string
     */
    public byte[] serialize() {
        return toString().getBytes();
    }

    /**
     * Serializes the container into a string
     *
     * @return the serialized string
     */
    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
