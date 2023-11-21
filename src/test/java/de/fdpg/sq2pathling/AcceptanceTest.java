package de.fdpg.sq2pathling;

import static org.junit.Assert.assertEquals;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.fdpg.sq2pathling.model.Mapping;
import de.fdpg.sq2pathling.model.MappingContext;
import de.fdpg.sq2pathling.model.TermCodeNode;
import de.fdpg.sq2pathling.model.pathling.Parameter;
import de.fdpg.sq2pathling.model.pathling.Parameters;
import de.fdpg.sq2pathling.model.structured_query.ContextualTermCode;
import de.fdpg.sq2pathling.model.structured_query.StructuredQuery;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.ZipFile;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.PullPolicy;
import org.testcontainers.junit.jupiter.Testcontainers;
import com.google.common.base.Functions;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
@TestInstance(Lifecycle.PER_CLASS)
public class AcceptanceTest {

  private static final String FHIR_SERVER_ENDPOINT = "http://localhost:8080/fhir/$import";

  private static final Logger logger = LoggerFactory.getLogger(AcceptanceTest.class);

  private final GenericContainer<?> pathling = new GenericContainer<>(
      DockerImageName.parse("aehrc/pathling:latest"))
      .withImagePullPolicy(PullPolicy.alwaysPull())
      .withEnv("LOG_LEVEL", "debug")
      .withExposedPorts(8080)
      .withFileSystemBind("C:/Users/Lorenz/Documents/Programmieren/Projekte/FDPG/sq2pathling/src",
          "/usr/share/staging", BindMode.READ_WRITE)
      .waitingFor(Wait.forHttp("/fhir/Observation").forStatusCode(200))
      .withLogConsumer(new Slf4jLogConsumer(logger));


  private Translator translator;

  private static Path resourcePath(String name) throws URISyntaxException {
    System.out.println(Files.exists(Path.of(name)));
    return Paths.get(Objects.requireNonNull(AcceptanceTest.class.getResource(name),
        "resource `%s` is missing".formatted(name)).toURI());
  }

  private static String slurp(String name) throws Exception {
    return Files.readString(resourcePath(name));
  }


  public static List<StructuredQuery> getTestQueriesReturningOnePatient()
      throws URISyntaxException, IOException {
    var exclusions = Set.of();
    try (var zipFile = new ZipFile(resourcePath("/returningOnePatient.zip").toString())) {
      return zipFile.stream()
          .map(entry -> {
            System.out.println(entry.toString());
            try {
              return new ObjectMapper().readValue(zipFile.getInputStream(entry),
                  StructuredQuery.class);
            } catch (IOException e) {
              throw new RuntimeException(e);
            }
          }).toList();
    }
  }


  private void upload_test_resource() {
    try {
      // Slurp the JSON content from GeneratedParameters.json located in src/test/resources/Parameters/
      String jsonString = slurp("/Parameters/GeneratedParameters.json");

      // Execute the cURL command within the container
      var response = pathling.execInContainer(
          "curl",
          "-X", "POST",
          "-H", "Content-Type: application/json",
          "-d", jsonString,
          FHIR_SERVER_ENDPOINT // Use the constant for the endpoint URL
      );

      // Retrieve the response from stdout and stderr
      var stdout = response.getStdout();
      var stderr = response.getStderr();

      // Log the response or handle it as needed
      logger.info("Response from server: {}", stdout);
      System.out.println("Response from server: " + stdout + " stderr " + stderr);
      if (!stderr.isEmpty()) {
        logger.error("Error response from server: {}", stderr);
      }

    } catch (Exception e) {
      // Handle the exception as needed
      logger.error("Exception occurred while uploading test resources: ", e);
    }
  }

  @BeforeAll
  public void setUp() throws Exception {
    translator = createTranslator();
    pathling.start();
    upload_test_resource();
  }

  @ParameterizedTest
  @MethodSource("de.fdpg.sq2pathling.AcceptanceTest#getTestQueriesReturningOnePatient")
  public void runTestCase(StructuredQuery structuredQuery) throws Exception {
    var requestParameters = translator.toPathling(structuredQuery);
    // Convert to JSON
    ObjectMapper mapper = new ObjectMapper();
    var jsonString = mapper.writeValueAsString(requestParameters);
    System.out.println(jsonString);
    var response = pathling.execInContainer("curl", "-X", "POST", "-H",
        "Content-Type: application/json", "-d", jsonString,
        "http://localhost:8080/fhir/Patient/$aggregate");
    var body = response.getStdout();
    System.out.println(body);

    JsonNode rootNode = mapper.readTree(body);

    // Attempt to retrieve the valueUnsignedInt from the JSON response
    JsonNode valueUnsignedIntNode = rootNode.path("parameter")
        .findPath("valueUnsignedInt");

    // Check if the valueUnsignedIntNode is not missing and is an integer
    if (!valueUnsignedIntNode.isMissingNode() && valueUnsignedIntNode.isInt()) {
      // Use assertEquals to compare the actual value to the expected value
      assertEquals("The valueUnsignedInt is not as expected", 1, valueUnsignedIntNode.intValue());
    } else {
      // If the node is missing or not an int, fail the test
      throw new AssertionError("The 'valueUnsignedInt' field is missing or is not an integer in the response JSON.");
    }


  }


  private static Map<ContextualTermCode, Mapping> readMappings(ZipFile zipFile) throws IOException {
    try (var in = zipFile.getInputStream(zipFile.getEntry("mapping/mapping_pathling.json"))) {
      var mapper = new ObjectMapper();
      return Arrays.stream(mapper.readValue(in, Mapping[].class))
          .collect(Collectors.toMap(Mapping::key, Functions.identity()));
    }
  }

  private static TermCodeNode readConceptTree(ZipFile zipFile) throws IOException {
    try (var in = zipFile.getInputStream(zipFile.getEntry("mapping/mapping_tree.json"))) {
      var mapper = new ObjectMapper();
      return mapper.readValue(in, TermCodeNode.class);
    }
  }


  static Translator createTranslator() throws Exception {
    try (ZipFile zipFile = new ZipFile(resourcePath("/mapping.zip").toString())) {
      var mappings = readMappings(zipFile);
      var conceptTree = readConceptTree(zipFile);
      var mappingContext = MappingContext.of(mappings, conceptTree);
      return Translator.of(mappingContext);
    }
  }
}
