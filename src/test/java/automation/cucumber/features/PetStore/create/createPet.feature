Feature: createPet
#https://petstore.swagger.io/
  Background:
    Given utilizo la url base del servicio PetStore con el endpoint "pet"

  @PetStore @CrearPet
  Scenario: Servicio PetStore - Pet - Registrar mascota
    When utilizo el json "bodyPet.json" para el body
    And realizo un request al servicio PETSTORE con el endpoint "pet"
    And visualizo statuscode 200
    Then valido la estructura de respuesta es correcta con el json "EstructuraPet200.json"