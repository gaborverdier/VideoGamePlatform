package org.example.util;

import org.apache.avro.specific.SpecificRecordBase;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class AvroJacksonConfig {
    public static ObjectMapper avroObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.addMixIn(SpecificRecordBase.class, IgnoreAvroProperties.class);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        return mapper;
    }

    @JsonIgnoreProperties({ "schema", "specificData", "classSchema", "conversion" })
    public abstract static class IgnoreAvroProperties {}
}
