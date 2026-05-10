package automation.cucumber.steps;

import automation.generic.DataHeader;
import automation.generic.EnvironmentManager;
import automation.generic.ObtenerJson;
import automation.generic.ValidacionGenericas;
import automation.ProyectoPetStore.collections.petStoreService;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.util.Map;


public class petStoreSteps {
    ValidacionGenericas validacionGenericas = new ValidacionGenericas();
    EnvironmentManager environmentManager = new EnvironmentManager();
    DataHeader dataHeader = new DataHeader();
    petStoreService petStoreService = new petStoreService();
    private String servicio;
    private String estado;
    private final String ambiente = environmentManager.getCurrentEnvironment();
    ObtenerJson obtenerJson = new ObtenerJson();

    @Given("utilizo la url base del servicio PetStore con el endpoint {string}")
    public void utilizoLaUrlBaseDelServicioPetStoreConElEndpoint(String endpoint) {
        servicio = "PETSTORE";
        petStoreService.setRuta(servicio,endpoint,ambiente);
    }


    @And("realizo un request al servicio PETSTORE con el endpoint {string}")
    public void realizoUnRequestAlServicioPETSTOREConElEndpoint(String endpoint) {
        validacionGenericas.setResponse(petStoreService.requestPETSTORE(endpoint));
    }

    @When("utilizo el json {string} para el body")
    public void utilizoElJsonParaElBody(String nombreJson) {
        obtenerJson.setJson(nombreJson,servicio);
    }

    @And("visualizo statuscode {int}")
    public void visualizoStatuscode(int codigoRespuesta) {
        validacionGenericas.validacionStatus(codigoRespuesta);
    }

    @Then("valido la estructura de respuesta es correcta con el json {string}")
    public void validoLaEstructuraDeRespuestaEsCorrectaConElJson(String jsonEstructura) {
        validacionGenericas.validacionDeEstructura(servicio, jsonEstructura);
    }

    @When("complemento el parametro con el estado {string} con {string}")
    public void complementoElParametroConElEstadoCon(String estado,String nombreJson) {
        Map<String, String> status = validacionGenericas.findStatus(estado,servicio,nombreJson);
        dataHeader.setParametros(status);
    }
}
