Feature: updatePet
#https://petstore.swagger.io/
  Background:
    Given utilizo la url base del servicio PetStore con el endpoint "pet"

  @PetStore @ActualizarPet
  Scenario: Servicio PetStore - Pet - Registrar mascota
    When utilizo el json "bodyUpdatePet.json" para el body
    And realizo un request al servicio PETSTORE con el endpoint "update-pet"
    And visualizo statuscode 200
    Then valido la estructura de respuesta es correcta con el json "EstructuraPetFindByStatus200.json"