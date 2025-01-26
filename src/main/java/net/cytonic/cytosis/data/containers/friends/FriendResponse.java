package net.cytonic.cytosis.data.containers.friends;

import com.google.gson.Gson;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public record FriendResponse(UUID request_id) {

    /**
     * Deserializes this object from a string
     *
     * @param json the serialized data
     * @return the deserailized object
     */
    public static FriendResponse deserialize(String json) {
        return new Gson().fromJson(json, FriendResponse.class);
    }

    public static byte[] create(UUID request_id) {
        return new FriendResponse(request_id).serialize().getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Serializes the container into a string
     *
     * @return the serialized string
     */
    public String serialize() {
        return new Gson().toJson(this);
    }

    /**
     * Serializes the container into a string
     *
     * @return the serialized string
     */
    @Override
    public String toString() {
        return serialize();
    }
}