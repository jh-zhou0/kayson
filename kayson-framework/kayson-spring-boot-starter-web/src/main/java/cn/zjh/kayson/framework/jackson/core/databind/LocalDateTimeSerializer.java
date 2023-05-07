package cn.zjh.kayson.framework.jackson.core.databind;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * LocalDateTime序列化规则
 * 将LocalDateTime序列化为毫秒级时间戳
 * 
 * @author zjh - kayson
 */
public class LocalDateTimeSerializer extends JsonSerializer<LocalDateTime> {
    
    public static final LocalDateTimeSerializer INSTANCE = new LocalDateTimeSerializer();
    
    @Override
    public void serialize(LocalDateTime localDateTime, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeNumber(localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
    }
}
