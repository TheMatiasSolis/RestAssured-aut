Feature: getPet
#https://petstore.swagger.io/
  Background:
    Given utilizo la url base del servicio PetStore con el endpoint "petFindByStatus"

  @PetStore @ObtenerPet
  Scenario: Servicio PetStore - Pet - Registrar mascota
    When complemento el parametro con el estado "available" con "header.json"
    And realizo un request al servicio PETSTORE con el endpoint "get-pet"
    And visualizo statuscode 200
    Then valido la estructura de respuesta es correcta con el json "EstructuraPetFindByStatus200.json"