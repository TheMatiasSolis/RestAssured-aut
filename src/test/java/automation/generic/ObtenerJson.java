package automation.generic;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;

import java.nio.file.Files;
import java.nio.file.Paths;

public class ObtenerJson {

    private static final Logger log = LogManager.getLogger(ValidacionGenericas.class);
    private static String json;

    public static String getJson()
    {
        return json;
    }

    public void setJson(String nombreJson, String servicio)
    {
        try {
            json = new String(Files.readAllBytes(Paths.get("src/test/resources/jsonBody/" + servicio + "/" + nombreJson)));

            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String prettyJson = gson.toJson(JsonParser.parseString(json));

            log.info("El body utiliza es siguiente JSON: {}", prettyJson);

        }
        catch (Exception e)
        {
            System.out.println("El Json no se ha leido correctamente");
            Assert.fail("El Json no se ha leido correctamente");
        }
    }
}
