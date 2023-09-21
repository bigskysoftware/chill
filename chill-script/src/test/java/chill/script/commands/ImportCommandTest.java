package chill.script.commands;

import chill.script.testutils.TestUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ImportCommandTest {
    @Test
    public void simpleImportTest() {
        var output = TestUtils.programOutput("""
                depend on com.google.code.gson:gson:2.10.1
                    and use com.google.gson.Gson
                end
                
                depend on com.sparkjava:spark-core:2.9.4
                    and import spark.Spark
                end
                
                let json be a new Gson()
                let value be "hello"
                
                let object be {
                    "hello": [1, "hi", value]
                }
                
                println json.toJson(object)
                """);

        assertEquals("""
                {"hello":[1,"hi","hello"]}
                """, output);
    }
}
