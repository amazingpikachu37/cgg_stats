package Threads;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class CountNumDeserializer extends StdDeserializer<ArrayList<CountValue>> {
        protected CountNumDeserializer() {
            super(ArrayList.class);
        }

        @Override
        public ArrayList<CountValue> deserialize(JsonParser parser, DeserializationContext ctx)
                throws IOException {
            CountValue[] counts = parser.getCodec().readValue(parser, CountValue[].class);
            return new ArrayList<>(Arrays.asList(counts));
        }
}
