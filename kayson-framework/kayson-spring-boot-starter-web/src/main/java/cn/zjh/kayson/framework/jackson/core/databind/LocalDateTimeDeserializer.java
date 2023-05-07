package cn.zjh.kayson.framework.jackson.core.databind;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * LocalDateTime反序列化规则
 * 会将毫秒级时间戳反序列化为LocalDateTime
 * 
 * @author zjh - kayson
 */
public class LocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {
    
    public static final LocalDateTimeDeserializer INSTANCE = new LocalDateTimeDeserializer();
    
    @Override
    public LocalDateTime deserialize(JsonParser jsonParser, DeserializationContext context) throws IOException, JacksonException {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(jsonParser.getValueAsLong()), ZoneId.systemDefault());
    }
}
