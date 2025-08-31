package tracker.api;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.LocalDateTime;

public class LocalDateTimeTypeAdapter extends TypeAdapter<LocalDateTime> {
    public void write(JsonWriter writer, LocalDateTime dateTime) throws IOException {
        if (dateTime == null) {
            writer.nullValue();
            return;
        }
        writer.value(dateTime.toString());
    }

    public LocalDateTime read(JsonReader reader) throws IOException {
        return LocalDateTime.parse(reader.nextString());
    }
}
