package automation.ProyectoPetStore.collections;

import automation.ProyectoPetStore.constants.constantPetStore;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;

import static automation.generic.DataHeader.getParametros;
import static automation.generic.ObtenerJson.getJson;

public class petStoreService {
    private static final ThreadLocal<Response> response = new ThreadLocal<>();
    constantPetStore constantPetStore = new constantPetStore();
    private String ruta;

    private static final Logger log = LogManager.getLogger(petStoreService.class);

    public void setRuta(String servicio, String endpoint, String ambientePruebas) {
        this.ruta = getPETSTORERuta(endpoint, ambientePruebas);

        log.info("URL del servicio {}, endpoint {}: {}", servicio, endpoint, ruta);
    }

    private String getPETSTORERuta(String endpoint, String ambientePruebas) {
        switch (endpoint) {
            case "pet":
                return constantPetStore.Pet;
            case "petFindByStatus":
                return constantPetStore.petFindByStatus;
            default:
                throw new IllegalArgumentException("Endpoint no válido: " + endpoint);
        }
    }

    public Response requestPETSTORE(String peticion) {
        RestAssured.baseURI = ruta;
        try {

            RestAssured.useRelaxedHTTPSValidation();
            RequestSpecification httpRequest = RestAssured.given();

            switch (peticion) {
                case "pet": {
                    response.set(httpRequest
                            .contentType(ContentType.JSON)
                            .headers(getParametros())
                            .body(getJson())
                            .post(ruta)
                            .then()
                            .extract()
                            .response());
                    break;
                }case "update-pet": {
                    response.set(httpRequest
                            .contentType(ContentType.JSON)
                            .headers(getParametros())
                            .body(getJson())
                            .put(ruta)
                            .then()
                            .extract()
                            .response());
                    break;
                }case "get-pet": {
                    response.set(httpRequest
                            .contentType(ContentType.JSON)
                            .queryParams(getParametros())
                            .get(ruta)
                            .then()
                            .extract()
                            .response());
                    break;
                }
                default: {
                    log.error("Opción no reconocida");
                    break;
                }
            }
        }
        catch (Exception e)
        {
            log.error("Error la petición no es la esperada");
            Assert.fail("Error la petición no es la esperadao "+e.getMessage());
        }
        System.out.println(">>> RESPONSE ES NULL? " + (response.get() == null));
        return response.get();
    }
}
