package org.bricolages.streaming.log;
import org.bricolages.streaming.s3.S3ObjectLocation;
import lombok.*;

@AllArgsConstructor
public class FakeS3Event extends LogEvent {
    S3ObjectMetadata object;

    public FakeS3Event(S3ObjectMetadata obj) {
        this.object = obj;
    }

    public String messageBody() {
        val w = new JSONWriter();
        w.beginObject();
            w.beginArray("Records");
                w.beginObject();
                    w.pair("eventVersion", "2.0");
                    w.pair("eventSource", "bricolage:preprocessor");
                    w.pair("eventTime", log.createdTime());
                    w.pair("eventName", "ObjectCreated:Put");
                    w.beginObject("s3");
                        w.pair("s3SchemaVersion", "1.0");
                        w.beginObject("bucket");
                            w.pair("name", object.bucket());
                        w.endObject();
                        w.beginObject("object");
                            w.pair("key", object.key());
                            w.pair("size", object.size());
                            w.pair("eTag", object.eTag());
                        w.endObject();
                    w.endObject();
                w.endObject();
            w.endArray();
        w.endObject();
        return w.toString();
    }

    class JSONWriter {
        StringBuilder buf = new StringBuilder();
        boolean needSeparator = false;

        @Override
        String toString() {
            return buf.toString();
        }

        JSONWriter pair(String key, String value) {
            return key(key).value(value);
        }

        JSONWriter pair(String key, int value) {
            return key(key).value(value);
        }

        JSONWriter pair(String key, LocaDateTime value) {
            return key(key).value(value);
        }

        JSONWriter key(String key) {
            if (needSeparator) {
                buf.append(",");
                this.needSeparator = false;
            }
            buf.append("\"").append(key).append("\":");
            return this;
        }

        JSONWriter value(String value) {
            buf.append("\"").append(escapeString(value)).append("\":");
            this.needSeparator = true;
            return this;
        }

        String escapeString(String str) {
        }

        JSONWriter value(LocalDateTime t) {
            value(formatTime(t));
        }

        String formatTime(LocalDateTime t) {
        }

        JSONWriter value(long value) {
            buf.append("\"").append(value).append("\":");
            this.needSeparator = true;
            return this;
        }

        JSONWriter comma() {
            buf.append(",");
            return this;
        }

        JSONWriter beginObject(String key) {
            return key(key).beginObject();
        }

        JSONWriter beginObject() {
            buf.append("{");
            return this;
        }

        JSONWriter endObject() {
            buf.append("}");
            this.needSeparator = true;
            return this;
        }

        JSONWriter beginArray(String key) {
            return key(key).beginArray();
        }

        JSONWriter beginArray() {
            buf.append("[");
            return this;
        }

        JSONWriter endArray() {
            buf.append("]");
            return this;
        }
    }
}
