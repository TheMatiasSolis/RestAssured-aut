package automation.generic;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import io.cucumber.core.internal.com.fasterxml.jackson.databind.JsonNode;
import io.cucumber.core.internal.com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.core.internal.com.fasterxml.jackson.databind.SerializationFeature;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.everit.json.schema.Schema;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.*;
import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONException;


import static org.junit.Assert.*;

public class ValidacionGenericas {

    private static final Logger log = LogManager.getLogger(ValidacionGenericas.class);
    public static String json;
    private String lastXFlowId;
    Response response;

    public String getLastXFlowId() {
        return lastXFlowId;
    }


    public void setResponse(Response a)
    {
        response = a;
    }

    public Map<String, String> findWallet(String walletName, String servicio, String nombreJson) {
        try {
            String json = new String(Files.readAllBytes(Paths.get("src/test/resources/JsonHeader/" + servicio + "/" + nombreJson)));
            Gson gson = new Gson();
            Type type = new TypeToken<List<Map<String, String>>>(){}.getType();
            List<Map<String, String>> wallets = gson.fromJson(json, type);

            for (Map<String, String> wallet : wallets) {
                if (wallet.get("Billetera").equalsIgnoreCase(walletName)) {
                    Map<String, String> result = new HashMap<>(wallet);
                    result.remove("Billetera");

                    if(result.containsKey("x-flow-id")){
                        String xFlowId = result.get("x-flow-id");
                        if (xFlowId == null || xFlowId.isEmpty()){
                            xFlowId = lastXFlowId;
                            result.put("x-flow-id",xFlowId);
                        }
                    }

                    if(result.containsKey("x-client")){
                        String xClient = result.get("x-client");
                        if (xClient == null || xClient.isEmpty()){
                            xClient = lastXFlowId;
                            result.put("x-client",xClient);
                        }
                    }

                    log.info("Los datos del header son: {}", result);
                    return result;
                }
            }
        } catch (IOException e) {
            log.error("Error al leer el archivo JSON de wallets: {}", e.getMessage());
        }
        return null;
    }

    public void generarFlowId(){
        try {
            Random random = new Random();
            StringBuilder result = new StringBuilder();

            result.append(random.nextInt(9)+1);
            result.append(random.nextInt(10));

            String letras = "abcdefghijklmnopqrstuvwxyz";
            for (int i =0; i < 4; i++){
                result.append(letras.charAt(random.nextInt(letras.length())));
            }

            for (int i =0; i < 4; i++){
                result.append(random.nextInt(10));
            }
            String valorGenerado = result.toString();
            log.info("x-flow-id ó x-client utilizado es: {}", valorGenerado);
            lastXFlowId = valorGenerado;
        }catch (Exception e){
            log.info("Error al generar x-flow-id", e);
        }
    }


    public void modificarBody(String nombreJson, String campoModificar, String nuevoTexto, String servicio) {
        Path path = Paths.get("src/test/resources/jsonBody/" + servicio + "/" + nombreJson);
        try {
            json = new String(Files.readAllBytes(path));
            JSONObject jsonObject = new JSONObject(json);
            jsonObject.put(campoModificar, nuevoTexto);

            try (FileWriter fileWriter = new FileWriter(path.toFile())) {
                fileWriter.write(jsonObject.toString());
            }
        } catch (IOException e) {
            log.error("Error al modificar el campo: {}", e.getMessage());
            fail("El campo no se modificó correctamente: " + e.getMessage());
        } catch (JSONException e) {
            log.error("Error al procesar el JSON: {}", e.getMessage());
            fail("Error al procesar el JSON: " + e.getMessage());
        }
    }

    public void validacionStatus(int status) {
        try {
            log.info("El estatus code es: {}", response.getStatusCode());
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonResponse = mapper.readTree(response.asString());
            String prettyJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonResponse);
            log.info("El body de respuesta es:\n{}", prettyJson);

            assertEquals(status, response.statusCode());
        } catch (Exception e) {
            log.error("Error statusCode no encontrado");
            fail("Error el statusCode no es el esperado " + e.getMessage());
        }
    }

    public void validacionDeEstructura(String servicio, String schemaJson) {
        try
        {

            String contenidoEsquema = new String(Files.readAllBytes(Paths.get("src/test/resources/jsonEstructure/" + servicio + "/" + schemaJson)));

            String Data = response.getBody().asString();

            boolean isValid = isJsonValid(Data,contenidoEsquema);

            assertTrue(isValid);
            log.info("La validacion de la estructura es valida");
            log.info("-----------------------------------------------------------------------------------------------");
        }
        catch (Exception e)
        {
            log.error("Error en la validacion de estructura");
            fail("Error en la validacion de estructura "+e.getMessage());
        }
    }

    public static boolean isJsonValid(String json, String JSON_SCHEMA)
    {
        try
        {
            JSONTokener jsonTokener = new JSONTokener(json);
            char primerCaracter = jsonTokener.next();
            char jsontypeArray= '[';

            JSONObject schemaJson = new JSONObject(new JSONTokener(JSON_SCHEMA));
            if (primerCaracter==jsontypeArray){
                JSONArray jsonToValidate = new JSONArray(new JSONTokener(json));

                Schema schema = SchemaLoader.load(schemaJson);
                schema.validate(jsonToValidate);

            }else{

                JSONObject jsonToValidate = new JSONObject(new JSONTokener(json));

                Schema schema = SchemaLoader.load(schemaJson);
                schema.validate(jsonToValidate);

            }
            return true;


        }
        catch (Exception e)
        {
            return false;
        }
    }


    public void modificarHeader(String parametroModificar, String nuevoValor, String billetera, String servicio) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            File inputFile = new File("src/test/resources/JsonHeader/" + servicio + "/header.json");
            File outputFile = new File("src/test/resources/JsonHeader/" + servicio + "/headerModificado.json");

            List<Map<String, Object>> jsonArray = mapper.readValue(inputFile, ArrayList.class);

            for (Map<String, Object> jsonObject : jsonArray) {
                if (jsonObject.containsKey("Billetera") && jsonObject.get("Billetera").equals(billetera)) {
                    if ("noEnviado".equals(nuevoValor)) {
                        jsonObject.remove(parametroModificar);
                    } else if ("vacio".equals(nuevoValor)) {
                        jsonObject.put(parametroModificar, "");
                    } else if ("alfanumerico".equals(nuevoValor)) {
                        jsonObject.put(parametroModificar, "456shhha7ohs54gtglakkksasa5");
                    } else if ("noAlfanumerico".equals(nuevoValor)) {
                        jsonObject.put(parametroModificar, "$///(##//");
                    } else{
                        jsonObject.put(parametroModificar, nuevoValor);
                    }
                }
            }
            mapper.writeValue(outputFile, jsonArray);
        } catch (IOException e) {
            log.error("Error al modificar el header ORTX: {}", e.getMessage());
        }
    }

    public void validacionTextoCampo(String campo, String texto) {
        try {
            JsonPath js = new JsonPath(response.asString());
            Object valor = buscarCampoJson(js.get("$"), campo);

            if (valor == null) {
                throw new NullPointerException("El campo " + campo + " no fue encontrado");
            }

            String textoCampo = valor.toString();

            assertEquals(textoCampo, texto);
            log.info("El campo {} contiene el texto: {}", campo, texto);
        } catch (Exception e) {
            log.error("El campo {} no contiene el texto: {}. Error: {}", campo, texto, e.getMessage());
            fail(e.toString());
        }
    }

    private Object buscarCampoJson(Object json, String campo) {
        if (json instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) json;
            if (map.containsKey(campo)) {
                return map.get(campo);
            }
            for (Object value : map.values()) {
                Object result = buscarCampoJson(value, campo);
                if (result != null) {
                    return result;
                }
            }
        } else if (json instanceof List) {
            for (Object item : (List<?>) json) {
                Object result = buscarCampoJson(item, campo);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

    public String modificarSimplecode(String operacion, String codigoQR) {
        switch (operacion){
            case "agregar":
                Random random = new Random();
                int num = random.nextInt(10);
                return codigoQR+num;
            case "eliminar":{
                if (!codigoQR.isEmpty()){
                    return codigoQR.substring(0,codigoQR.length()-1);
                }else {
                    return codigoQR;
                }
            }
            default:
                return codigoQR;
        }
    }

    public String vaciarCampo(String ignoredCampo){
        return "";
    }


    public String modificarIdTransaction(String operacion, String idTransaction) {
        if (idTransaction == null) {
            log.warn("La transacción ID proporcionada es null.");
            return null;
        }

        String resultado;
        switch (operacion) {
            case "agregar":
                resultado = idTransaction + "1";
                log.info("TransactionId modificado agregando caracter. Nuevo TransactionId es: {}", resultado);
                break;
            case "eliminar":
                if (!idTransaction.isEmpty()) {
                    resultado = idTransaction.substring(0, idTransaction.length() - 1);
                } else {
                    resultado = idTransaction;
                }
                log.info("TransactionId modificado eliminando caracter. Nuevo TransactionId es: {}", resultado);
                break;

            default:
                resultado = idTransaction;
                log.info("Operación desconocida: '{}' no modificó el ID de transacción.", operacion);
                break;
        }

        return resultado;
    }


    public Map<String, String> findChannel(String nombrecanal, String servicio, String nombreJson) {
        try {
            String json = new String(Files.readAllBytes(Paths.get("src/test/resources/JsonHeader/" + servicio + "/" + nombreJson)));
            Gson gson = new Gson();
            Type type = new TypeToken<List<Map<String, String>>>(){}.getType();
            List<Map<String, String>> canales = gson.fromJson(json, type);

            for (Map<String, String> canal : canales) {
                if (canal.get("Canal").equalsIgnoreCase(nombrecanal)) {
                    log.info("El header utilizado es: {}", canal);
                    return canal;
                }
            }
        } catch (IOException e) {
            log.error("Error al leer el archivo JSON desde Header: {}", e.getMessage());
        }
        return null;
    }

    public Map<String, String> findStatus(String nombreStatus, String servicio, String nombreJson) {
        try {
            String json = new String(Files.readAllBytes(Paths.get("src/test/resources/JsonHeader/" + servicio + "/" + nombreJson)));
            Gson gson = new Gson();
            Type type = new TypeToken<List<Map<String, String>>>(){}.getType();
            List<Map<String, String>> estados = gson.fromJson(json, type);

            for (Map<String, String> Status : estados) {
                if (Status.get("status").equalsIgnoreCase(nombreStatus)) {
                    log.info("El header utilizado es: {}", Status);
                    return Status;
                }
            }
        } catch (IOException e) {
            log.error("Error al leer el archivo JSON desde Header: {}", e.getMessage());
        }
        return null;
    }


    public void validacionLargoCampo(String nombreCampo, int largoCaracteres){
        try {
            JsonPath js = new JsonPath(response.asString());
            String campo = js.getString(nombreCampo);
            assertEquals(campo.length(),largoCaracteres);
            log.info("El largo del campo {} es de {} caracteres", nombreCampo, largoCaracteres);
        }
        catch (Exception e){
            fail(e.toString());
        }
    }

    public void validarFormatoCampoResponse(String campo)
    {
        try
        {
            switch (campo)
            {
                case "qrId":
                case "idQR":{
                    JsonPath js = new JsonPath(response.asString());
                    String valorCampo = js.getString(campo);
                    String expresionRegular = "^[0-9a-zA-Z]{8}-[0-9a-zA-Z]{4}-[0-9a-zA-Z]{4}-[0-9a-zA-Z]{4}-[0-9a-zA-Z]{12}$";
                    expresionRegular(expresionRegular,valorCampo);
                    log.info("El Formato del campo " + campo + " es valido");
                    break;
                }
                case "updated":{
                    JsonPath js = new JsonPath(response.asString());
                    String valorCampo = js.getString(campo);
                    String expresionRegular = "202[456][-](01|02|03|04|05|06|07|08|09|10|11|12)[-](01|02|03|04|05|06|07|08|09|10|11|12|13|14|15|16|17|18|19|20|21|22|23|24|25|26|27|28|29|30|31)[\\s](01|02|03|04|05|06|07|08|09|10|11|12)([:][0-5][0-9]){2}";
                    expresionRegular(expresionRegular,valorCampo);
                    log.info("El Formato del campo " + campo + " es valido");
                    break;
                }
                default: {
                    log.error("Formato del campo no valido");
                    fail("Formato del campo no valido");
                    break;
                }
            }
        }
        catch (Exception e)
        {
            log.error("Error el codigo no es el esperado");
            fail("Error el codigo no es el esperado "+e.getMessage());
        }

    }


    public void expresionRegular(String expression, String textValidate){
        try {
            Pattern p = Pattern.compile(expression);
            Matcher m = p.matcher(textValidate);
            assertTrue(textValidate,m.matches());
        }catch(AssertionError e){
            fail(e.toString());
        }
    }




    public void modificarCampoJson(String nombreJson, String campoModificar, String nuevoValor, String servicio  ) {
        try {
            String json = new String(Files.readAllBytes(Paths.get("src/test/resources/jsonBody/"  + servicio + "/" + nombreJson)));
            JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();
            modificarCampo(jsonObject, campoModificar, nuevoValor);
            guardarJsonModificado(jsonObject, servicio);
        } catch (IOException e) {
            log.error("Error al leer el archivo JSON");
            fail("Error al leer el archivo JSON");
        } catch (Exception e) {
            log.error("Error al modificar el campo '{}' en el JSON con el nuevo valor '{}'", campoModificar, nuevoValor);
            fail("Error al modificar el campo en el JSON");
        }
    }

    private void modificarCampo(JsonObject jsonObject, String campoModificar, String nuevoValor) {
        for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
            String key = entry.getKey();
            JsonElement value = entry.getValue();

            if (key.equals(campoModificar)) {
                jsonObject.addProperty(campoModificar, nuevoValor);
            } else if (value.isJsonObject()) {
                modificarCampo(value.getAsJsonObject(), campoModificar, nuevoValor);
            } else if (value.isJsonArray()) {
                JsonArray jsonArray = value.getAsJsonArray();
                for (JsonElement element : jsonArray) {
                    if (element.isJsonObject()) {
                        modificarCampo(element.getAsJsonObject(), campoModificar, nuevoValor);
                    }
                }
            }
        }
    }

    private void guardarJsonModificado(JsonObject jsonObject, String servicio) {
        String filePath = "src/test/resources/jsonBody/" + servicio + "/bodyModificado.json";
        try (FileWriter fileWriter = new FileWriter(filePath)) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(jsonObject, fileWriter);
            log.info("JSON guardado correctamente en: {}", filePath);
        } catch (IOException e) {
            log.error("Error al guardar el archivo JSON modificado: {}", e.getMessage());
            fail("Error al guardar el archivo JSON modificado: " + e.getMessage());
        }
    }

    public void eliminarCampoJson(String nombreJson, String campoEliminar, String servivio ) {
        try {
            String json = new String(Files.readAllBytes(Paths.get("src/test/resources/jsonBody/" + servivio +  "/" + nombreJson)));
            JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();

            log.info("Eliminando campo '{}' del siguiente JSON '{}'", campoEliminar, json);

            eliminarCampo(jsonObject, campoEliminar);
            guardarJsonModificado(jsonObject, servivio);
        } catch (IOException e) {
            log.error("Error al leer el archivo JSON");
            fail("Error al leer el archivo JSON");
        } catch (Exception e) {
            log.error("Error al eliminar el campo en el JSON");
            fail("Error al eliminar el campo en el JSON");
        }
    }

    private void eliminarCampo(JsonObject jsonObject, String campoEliminar) {
        boolean campoEncontrado = false;
        for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
            String key = entry.getKey();
            JsonElement value = entry.getValue();
            if (key.equals(campoEliminar)) {
                jsonObject.remove(key);
                log.info("Campo '{}' encontrado y eliminado correctamente", campoEliminar);
                campoEncontrado = true;
                break;
            } else if (value.isJsonObject()) {
                if (value.getAsJsonObject().size() == 1 && value.getAsJsonObject().has(campoEliminar)) {
                    jsonObject.remove(key);
                    log.info("Campo '{}' encontrado y eliminado correctamente", campoEliminar);
                    campoEncontrado = true;
                } else {
                    eliminarCampo(value.getAsJsonObject(), campoEliminar);
                }
            } else if (value.isJsonArray()) {
                JsonArray jsonArray = value.getAsJsonArray();
                for (JsonElement element : jsonArray) {
                    if (element.isJsonObject()) {
                        eliminarCampo(element.getAsJsonObject(), campoEliminar);
                    }
                }
            }
        }
    }

    public void validacionContengaTexto(String campo, String texto){
        try {
            if (texto == null || texto.isEmpty()){
                throw new IllegalArgumentException("El texto a buscar está vacío o es nulo.");
            }
            JsonPath js = new JsonPath(response.asString());
            String textoCampo = js.getString(campo);
            assertTrue(textoCampo.contains(texto));
            log.info("El campo " + campo + " contiene el texto: " + texto);

        }catch (Exception e){
            log.error("El campo " + campo + " no contiene el texto: " + texto);
            fail(e.toString());
        }
    }


    public String seleccionarCodigoCanal(String canal) {
        switch (canal) {
            case "CobroQR":
                return "001";
            case "webPay-TiendaNormal":
                return "002";
            case "webPay-TiendaMall":
                return "003";
            case "H2H":
                return "004";
            case "AndroidPost":
                return "007";
            default:
                throw new IllegalArgumentException("Canal no reconocido: " + canal);
        }
    }

    public void modificarCamposJson(String nombreJson, String campoModificar1, String nuevoTexto1, String campoModificar2, String nuevoTexto2, String servicio ) {
        Path path = Paths.get("src/test/resources/jsonBody/" + servicio + "/" + nombreJson);
        try {
            json = new String(Files.readAllBytes(path));
            JSONObject jsonObject = new JSONObject(json);

            jsonObject.put(campoModificar1, nuevoTexto1);

            jsonObject.put(campoModificar2, nuevoTexto2);

            try (FileWriter fileWriter = new FileWriter(path.toFile())) {
                fileWriter.write(jsonObject.toString());
            }
        } catch (IOException e) {
            log.error("Error al modificar los campos: {}", e.getMessage());
            fail("Los campos no se modificaron correctamente: " + e.getMessage());
        } catch (JSONException e) {
            log.error("Error al procesar el JSON: {}", e.getMessage());
            fail("Error al procesar el JSON: " + e.getMessage());
        }
    }


    public String obtenerCodigo(String[] codigosQR, String tipoCodigoQR, String canal) {
        String QR;
        try {
            if (codigosQR == null || codigosQR.length == 0) {
                throw new IllegalArgumentException("El array codigosQR está vacío o es nulo");
            }

            String contenido = codigosQR[0].replaceAll("^\\[|\\]$", "");
            String[] valoresSeparados = contenido.split(",", 2);

            switch (tipoCodigoQR) {
                case "simpleCode":
                    QR = valoresSeparados[0].trim();
                    break;
                case "codeKey":
                    QR = valoresSeparados.length > 1 ? valoresSeparados[1].trim() : "";
                    break;
                default:
                    throw new IllegalArgumentException("Tipo de código QR no reconocido: " + tipoCodigoQR);
            }

            log.info("El codigoQR obtenido desde {} es {}: {}", canal, tipoCodigoQR, QR);
            return QR;
        } catch (Exception e) {
            log.error("Error al obtener el código QR: {}", e.getMessage(), e);
            return null;
        }
    }


    public void validarFormatoCampo(String campo, String nombreJson , String servicio) {
        try {

            String jsonStr = new String(Files.readAllBytes(Paths.get("src/test/resources/jsonBody/" + servicio + "/" + nombreJson)));


            JsonPath json = JsonPath.from(jsonStr);


            Object valorCampo = buscarCampo(json.getMap(""), campo);
            if (valorCampo != null) {
                validarExpresionRegular(valorCampo.toString(), obtenerExpresionRegular(campo));
                log.info("El formato del campo " + campo + " es válido");
            } else {
                log.error("Campo no encontrado en el JSON: " + campo);
                fail("Campo no encontrado en el JSON: " + campo);
            }
        } catch (Exception e) {
            log.error("Error: " + e.getMessage());
            fail("Error: " + e.getMessage());
        }
    }

    private Object buscarCampo(Map<String, ?> json, String campo) {
        for (Map.Entry<String, ?> entry : json.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            if (key.equals(campo)) {
                return value;
            } else if (value instanceof Map) {
                Object nestedValue = buscarCampo((Map<String, ?>) value, campo);
                if (nestedValue != null) {
                    return nestedValue;
                }
            }
        }
        return null;
    }

    private void validarExpresionRegular(String valorCampo, String expresionRegular) {
        try {
            Pattern p = Pattern.compile(expresionRegular);
            Matcher m = p.matcher(valorCampo);
            assertTrue("Valor del campo " + valorCampo + " no cumple con el formato esperado: " + expresionRegular, valorCampo.matches(expresionRegular));
        } catch (Exception e) {
            fail("Error al validar expresión regular: " + e.getMessage());
        }
    }

    private String obtenerExpresionRegular(String campo) {
        switch (campo) {
            case "expirationDate":
                return "[0-9]{2}(?:0[1-9]|1[0-2])";
            case "cvv":
                return "^\\d{3,4}$";
            case "eci":
                return "^\\d{2}$";
            case "cavv":
                return "^\\w{2,40}$";
            case "vci":
                return "^\\w{1,4}$";
            default:
                throw new IllegalArgumentException("Expresión regular no encontrada para el campo: " + campo);
        }
    }


    public String modificarAllIdTransaction(String operacion, String idTransaction) {
        if (idTransaction == null) {
            log.warn("La transacción ID proporcionada es null.");
            return null;
        }

        String resultado;
        switch (operacion) {
            case "alfanumerico":
                resultado = "456shhha7ohs54gtglakkksasa5";
                log.info("TransactionId modificado agregando caracteres alfanumericos. Nuevo TransactionId es: {}", resultado);
                break;
            case "noAlfanumerico":
                try {
                    resultado = URLEncoder.encode("$///(##//", StandardCharsets.UTF_8);
                } catch (Exception e) {
                    log.error("Error al codificar el ID de transacción: ", e);
                    resultado = "$///(##//"; // Fallback si hay un error en la codificación
                }
                log.info("TransactionId modificado agregando caracteres no alfanumericos y codificado para URL. Nuevo TransactionId es: {}", resultado);
                break;
            case "canalInvalido":
                if (idTransaction.length() >= 3) {
                    resultado = "999" + idTransaction.substring(3);
                } else {
                    resultado = "999";
                }
                log.info("TransactionId modificado reemplazando los tres primeros dígitos. Nuevo TransactionId es: {}", resultado);
                break;
            default:
                resultado = idTransaction;
                log.info("Operación desconocida: '{}' no modificó el ID de transacción.", operacion);
                break;
        }

        return resultado;
    }


    public String seleccionarWallet(String wallet) {
        if (wallet == null) {
            log.warn("La billetera proporcionada es null.");
            return null;
        }

        String resultado;
        switch (wallet) {
            case "OnePay":
                resultado = "OnePay";
                log.info("La billetera seleccionada es: {}", resultado);
                break;
            case "BancoChile":
            case "Scotiabank":
            case "BancoItau":
            case "BCI":
            case "Match":
            case "LíderBci":
            case "Santander":
            case "EmisorEH":
                resultado = "wallet";
                log.info("La billetera seleccionada es: {}", wallet);
                break;
            default:
                resultado = wallet;
                log.info("Billetera '{}' no existe", wallet);
                break;
        }

        return resultado;
    }




    public void datosTarjeta(String newId, List<Map<String, List<String>>> datosComercio, String nomTarjeta, String tipoCuota, int cantCuotas, String tipoComercio){
        try {
            String tarjetasJson = new String(Files.readAllBytes(Paths.get("src/test/resources/jsonBody/QRWLTE/tarjetas.json")));
            JsonArray tarjetas = JsonParser.parseString(tarjetasJson).getAsJsonArray();

            JsonObject tarjetaEncontrada = null;
            boolean isSecure = false;
            String cardBrand = "";
            String cardType = "";

            for (JsonElement tarjeta : tarjetas) {
                JsonObject tarjetaObj = tarjeta.getAsJsonObject();
                if (tarjetaObj.get("nombreTarjeta").getAsString().equals(nomTarjeta)) {
                    tarjetaEncontrada = tarjetaObj;
                    isSecure = tarjetaObj.get("secure").getAsString().equals("true");
                    cardBrand = tarjetaObj.get("cardBrand").getAsString();
                    cardType = tarjetaObj.get("cardType").getAsString();
                    break;
                }
            }

            if (tarjetaEncontrada == null) {
                throw new IllegalArgumentException("No se encontró la tarjeta con el número proporcionado");
            }

            String jsonPath;
            if (isSecure && tipoComercio.equals("NORMAL")) {
                jsonPath = "src/test/resources/jsonBody/QRWLTE/bodyAuthorize3DS.json";
            } else if (isSecure && tipoComercio.equals("MALL")) {
                jsonPath = "src/test/resources/jsonBody/QRWLTE/bodyAuthorizeMall3DS.json";
            } else if (!isSecure && tipoComercio.equals("NORMAL")) {
                jsonPath = "src/test/resources/jsonBody/QRWLTE/bodyAuthorize.json";
            } else if (!isSecure && tipoComercio.equals("MALL")) {
                jsonPath = "src/test/resources/jsonBody/QRWLTE/bodyAuthorizeMall.json";
            } else {
                throw new IllegalArgumentException("Combinación no válida de isSecure y tipoComercio");
            }

            String json = new String(Files.readAllBytes(Paths.get(jsonPath)));
            JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();

            boolean condicionesCumplidas = true;

            for (Map<String, List<String>> comercio : datosComercio) {
                List<String> codes = comercio.get("codes");

                if (!codes.contains(tipoCuota)) {
                    log.info("Tipo de cuota de la tarjeta no coincide con tipo de cuota del comercio");
                    condicionesCumplidas = false;
                    break;
                }

                String paymentType = tipoCuota;

                switch (paymentType) {
                    case "VD":
                    case "VN":
                    case "VP":
                        if (cantCuotas != 1) {
                            log.info("Cuotas no coincide con el paymentType");
                            condicionesCumplidas = false;
                        }
                        break;
                    case "SI":
                        if (cantCuotas != 3) {
                            log.info("Cuotas no coincide con el paymentType");
                            condicionesCumplidas = false;
                        }
                        break;
                    case "S2":
                        if (cantCuotas != 2) {
                            log.info("Cuotas no coincide con el paymentType");
                            condicionesCumplidas = false;
                        }
                        break;
                    case "NC":
                        if (cantCuotas < 4 || cantCuotas > 24) {
                            log.info("Cuotas no coincide con el paymentType");
                            condicionesCumplidas = false;
                        }
                        break;
                    case "VC":
                        if (cantCuotas < 2 || cantCuotas > 48) {
                            log.info("Cuotas no coincide con el paymentType");
                            condicionesCumplidas = false;
                        }
                        break;
                    default:
                        log.info("Tipo de cuota no reconocido");
                        condicionesCumplidas = false;
                }

                if (!condicionesCumplidas) {
                    break;
                }
            }

            if (condicionesCumplidas) {
                Map<String, String> camposTarjeta = new HashMap<>();
                camposTarjeta.put("cardNumber", tarjetaEncontrada.get("cardNumber").getAsString());
                camposTarjeta.put("expirationDate", tarjetaEncontrada.get("expirationDate").getAsString());
                camposTarjeta.put("cvv", tarjetaEncontrada.get("cvv").getAsString());

                if (isSecure) {
                    camposTarjeta.put("cavv", tarjetaEncontrada.get("cavv").getAsString());
                    camposTarjeta.put("vci", tarjetaEncontrada.get("vci").getAsString());
                    camposTarjeta.put("eci", tarjetaEncontrada.get("eci").getAsString());
                }

                modificarCampo(jsonObject, "id", newId);
                modificarCampo(jsonObject, "cardType", cardType);
                modificarCampo(jsonObject, "cardBrand", cardBrand);

                for (Map.Entry<String, String> entry : camposTarjeta.entrySet()) {
                    modificarCampo(jsonObject, entry.getKey(), entry.getValue());
                }

                JsonArray userSelectionArray = jsonObject.getAsJsonArray("userSelection");
                userSelectionArray.remove(0);
                while (!userSelectionArray.isEmpty()) {
                    userSelectionArray.remove(0);
                }

                for (Map<String, List<String>> comercio : datosComercio) {
                    JsonObject userSelection = new JsonObject();
                    userSelection.addProperty("share", cantCuotas);
                    userSelection.addProperty("paymentType", tipoCuota);
                    userSelection.addProperty("childId", comercio.get("childId").get(0));
                    userSelectionArray.add(userSelection);
                }
            }

            guardarJsonModificado(jsonObject, "QRWLTE");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

/*    private void modificarCampoEncriptados(JsonObject jsonObject, String campoModificar, String nuevoValor) {
        for (String key : jsonObject.keySet()) {
            JsonElement value = jsonObject.get(key);
            if (key.equals(campoModificar)) {
                jsonObject.addProperty(campoModificar, nuevoValor);
            } else if (value.isJsonObject()) {
                modificarCampoEncriptados(value.getAsJsonObject(), campoModificar, nuevoValor);
            } else if (value.isJsonArray()) {
                JsonArray jsonArray = value.getAsJsonArray();
                for (JsonElement element : jsonArray) {
                    if (element.isJsonObject()) {
                        modificarCampoEncriptados(element.getAsJsonObject(), campoModificar, nuevoValor);
                    }
                }
            }
        }
    }

    private void guardarBodyAuthorize(JsonObject jsonObject) {
        try (FileWriter fileWriter = new FileWriter("src/test/resources/jsonBody/QRWLTE/bodyModificado.json")) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(jsonObject, fileWriter);
            System.out.println("JSON guardado correctamente...");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/

   /* private static final String KEY = "4df80cc06134bf8c8615f7d3bfe93b9a";
    private static final String IV = "1234567887654321";

    public void encriptarDatos(String newId) {
        try {
            String json = new String(Files.readAllBytes(Paths.get("src/test/resources/jsonBody/APIC/bodyAuthorize.json")));
            JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();
            Map<String, String> fieldsToEncrypt = new HashMap<>();
            fieldsToEncrypt.put("cardNumber", "36000000001006");
            fieldsToEncrypt.put("expirationDate", "2512");
            fieldsToEncrypt.put("cavv", "123F12A5113");
            fieldsToEncrypt.put("vci", "TSY");
            fieldsToEncrypt.put("eci", "05");
            fieldsToEncrypt.put("cvv", "123");

            modificarCampoEncriptados(jsonObject, "id", newId);

            for (Map.Entry<String, String> entry : fieldsToEncrypt.entrySet()) {
                String fieldName = entry.getKey();
                String fieldValue = entry.getValue();
                String encryptedValue = encrypt(fieldValue) + IV;
                modificarCampoEncriptados(jsonObject, fieldName, encryptedValue);
            }

            guardarJsonEncriptado(jsonObject);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/

    /*    private String encrypt(String data) throws Exception {
        byte[] keyBytes = hexStringToByteArray(KEY);
        byte[] ivBytes = IV.getBytes(StandardCharsets.UTF_8);
        SecretKeySpec secretKey = new SecretKeySpec(keyBytes, "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(ivBytes);

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);

        byte[] encrypted = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return bytesToHex(encrypted);
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }*/

/*    private void guardarJsonEncriptado(JsonObject jsonObject) {
        try (FileWriter fileWriter = new FileWriter("src/test/resources/jsonBody/APIC/bodyModificado.json")) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(jsonObject, fileWriter);
            System.out.println("JSON guardado correctamente...");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/

    public Map<String, String> headerORTXUpdate(String codigoCanal) {
        Map<String, String> result = new HashMap<>();
        try {
            result.put("x-flow-id", lastXFlowId);
            result.put("x-channel-code", codigoCanal);
            log.info("Los datos del header son: {}", result);
        }catch (Exception e){
            log.error("Error al crear el header updateTx de ORTX: {}", e.getMessage());
        }
        return result;
    }




}