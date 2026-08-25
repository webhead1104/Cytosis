package net.cytonic.cytosis.json;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonParser;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.Transcoder;
import org.jetbrains.annotations.Unmodifiable;

public class JsonFileUtils {

    public static <T> T parseFileWithCodec(Codec<T> codec, String path) {
        try (InputStreamReader reader = ResourceUtils.getResourceFile(path)) {
            return codec.decode(Transcoder.JSON, JsonParser.parseReader(reader))
                .orElseThrow("Failed to parse file : " + path);
        } catch (IOException e) {
            throw new RuntimeException("Failed to close file : " + path, e);
        }
    }

    public static <T> List<T> parseFilesWithCodec(Codec<T> codec, String dir) {
        List<T> list = new ArrayList<>();
        for (InputStreamReader reader : ResourceUtils.getResourceFiles(dir)) {
            try (reader) {
                list.add(codec.decode(Transcoder.JSON, JsonParser.parseReader(reader)).orElseThrow());
            } catch (IOException e) {
                throw new RuntimeException("Failed to close file in : " + dir, e);
            }
        }
        return list;
    }

    public static <T> List<T> parseFilesWithListCodec(Codec<@Unmodifiable List<T>> codec, String dir) {
        List<T> list = new ArrayList<>();
        for (InputStreamReader reader : ResourceUtils.getResourceFiles(dir)) {
            try (reader) {
                list.addAll(codec.decode(Transcoder.JSON, JsonParser.parseReader(reader)).orElseThrow());
            } catch (IOException e) {
                throw new RuntimeException("Failed to close file in : " + dir, e);
            }
        }
        return list;
    }
}
