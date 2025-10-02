package com.esclient.ratingservice.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.infisical.sdk.InfisicalSdk;
import com.infisical.sdk.models.Secret;
import com.infisical.sdk.resources.AuthClient;
import com.infisical.sdk.resources.SecretsClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InfisicalServiceTest {

  @Mock private AuthClient mockAuthClient;

  @Mock private SecretsClient mockSecretsClient;

  @Mock private Secret mockSecret;

  private InfisicalService createServiceWithToken() {
    try (MockedConstruction<InfisicalSdk> ignored =
        mockConstruction(
            InfisicalSdk.class,
            (mock, context) -> {
              when(mock.Auth()).thenReturn(mockAuthClient);
              when(mock.Secrets()).thenReturn(mockSecretsClient);
            })) {
      return new InfisicalService("https://app.infisical.com", "test-token", "", "");
    }
  }

  private InfisicalService createServiceWithUniversalAuth() {
    try (MockedConstruction<InfisicalSdk> ignored =
        mockConstruction(
            InfisicalSdk.class,
            (mock, context) -> {
              when(mock.Auth()).thenReturn(mockAuthClient);
              when(mock.Secrets()).thenReturn(mockSecretsClient);
            })) {
      return new InfisicalService(
          "https://app.infisical.com", "", "client-id-123", "client-secret-456");
    }
  }

  @Test
  void testGetSecretWithDefaultPath() {
    // Arrange
    String secretName = "DATABASE_PASSWORD";
    String projectId = "test-project-123";
    String environment = "dev";
    String expectedValue = "super_secret_password";

    try {
      when(mockSecret.getSecretValue()).thenReturn(expectedValue);
      when(mockSecretsClient.GetSecret(
              eq(secretName),
              eq(projectId),
              eq(environment),
              eq("/"),
              eq(false),
              eq(false),
              isNull()))
          .thenReturn(mockSecret);

      InfisicalService service = createServiceWithToken();

      // Act
      String result = service.getSecret(secretName, projectId, environment);

      // Assert
      assertEquals(expectedValue, result);
      verify(mockSecretsClient)
          .GetSecret(secretName, projectId, environment, "/", false, false, null);
    } catch (Exception e) {
      fail("Test failed with exception: " + e.getMessage());
    }
  }

  @Test
  void testGetSecretWithCustomPath() {
    // Arrange
    String secretName = "API_KEY";
    String projectId = "test-project-123";
    String environment = "production";
    String secretPath = "/custom/path";
    String expectedValue = "api-key-value";

    try {
      when(mockSecret.getSecretValue()).thenReturn(expectedValue);
      when(mockSecretsClient.GetSecret(
              eq(secretName),
              eq(projectId),
              eq(environment),
              eq(secretPath),
              eq(false),
              eq(false),
              isNull()))
          .thenReturn(mockSecret);

      InfisicalService service = createServiceWithToken();

      // Act
      String result = service.getSecret(secretName, projectId, environment, secretPath);

      // Assert
      assertEquals(expectedValue, result);
      verify(mockSecretsClient)
          .GetSecret(secretName, projectId, environment, secretPath, false, false, null);
    } catch (Exception e) {
      fail("Test failed with exception: " + e.getMessage());
    }
  }

  @Test
  void testGetSecretWithUniversalAuth() {
    // Arrange
    String clientId = "client-id-123";
    String clientSecret = "client-secret-456";
    String secretName = "DATABASE_PASSWORD";
    String projectId = "test-project";
    String environment = "dev";
    String expectedValue = "password123";

    try {
      when(mockSecret.getSecretValue()).thenReturn(expectedValue);
      when(mockSecretsClient.GetSecret(
              anyString(),
              anyString(),
              anyString(),
              anyString(),
              anyBoolean(),
              anyBoolean(),
              any()))
          .thenReturn(mockSecret);

      InfisicalService service = createServiceWithUniversalAuth();

      // Act
      String result = service.getSecret(secretName, projectId, environment);

      // Assert
      assertEquals(expectedValue, result);
      verify(mockAuthClient).UniversalAuthLogin(clientId, clientSecret);
    } catch (Exception e) {
      fail("Test failed with exception: " + e.getMessage());
    }
  }

  @Test
  void testGetSecretThrowsExceptionWhenSecretNotFound() {
    // Arrange
    try {
      when(mockSecretsClient.GetSecret(
              anyString(),
              anyString(),
              anyString(),
              anyString(),
              anyBoolean(),
              anyBoolean(),
              any()))
          .thenReturn(null);

      InfisicalService service = createServiceWithToken();

      // Act & Assert
      RuntimeException exception =
          assertThrows(
              RuntimeException.class, () -> service.getSecret("MISSING_SECRET", "project", "dev"));
      assertTrue(exception.getMessage().contains("not found or has null value"));
    } catch (Exception e) {
      fail("Test failed with exception: " + e.getMessage());
    }
  }

  @Test
  void testGetSecretThrowsExceptionWhenSecretValueIsNull() {
    // Arrange
    try {
      when(mockSecret.getSecretValue()).thenReturn(null);
      when(mockSecretsClient.GetSecret(
              anyString(),
              anyString(),
              anyString(),
              anyString(),
              anyBoolean(),
              anyBoolean(),
              any()))
          .thenReturn(mockSecret);

      InfisicalService service = createServiceWithToken();

      // Act & Assert
      RuntimeException exception =
          assertThrows(
              RuntimeException.class, () -> service.getSecret("NULL_SECRET", "project", "dev"));
      assertTrue(exception.getMessage().contains("not found or has null value"));
    } catch (Exception e) {
      fail("Test failed with exception: " + e.getMessage());
    }
  }

  @Test
  void testGetSecretThrowsExceptionForNullSecretName() {
    InfisicalService service = createServiceWithToken();

    // Act & Assert
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class, () -> service.getSecret(null, "project", "dev"));
    assertTrue(exception.getMessage().contains("Secret name cannot be null or empty"));
  }

  @Test
  void testGetSecretThrowsExceptionForEmptySecretName() {
    InfisicalService service = createServiceWithToken();

    // Act & Assert
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class, () -> service.getSecret("  ", "project", "dev"));
    assertTrue(exception.getMessage().contains("Secret name cannot be null or empty"));
  }

  @Test
  void testGetSecretThrowsExceptionForNullProjectId() {
    InfisicalService service = createServiceWithToken();

    // Act & Assert
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class, () -> service.getSecret("SECRET", null, "dev"));
    assertTrue(exception.getMessage().contains("Project ID cannot be null or empty"));
  }

  @Test
  void testGetSecretThrowsExceptionForEmptyProjectId() {
    InfisicalService service = createServiceWithToken();

    // Act & Assert
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class, () -> service.getSecret("SECRET", "   ", "dev"));
    assertTrue(exception.getMessage().contains("Project ID cannot be null or empty"));
  }

  @Test
  void testGetSecretThrowsExceptionForNullEnvironment() {
    InfisicalService service = createServiceWithToken();

    // Act & Assert
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class, () -> service.getSecret("SECRET", "project", null));
    assertTrue(exception.getMessage().contains("Environment cannot be null or empty"));
  }

  @Test
  void testGetSecretThrowsExceptionForEmptyEnvironment() {
    InfisicalService service = createServiceWithToken();

    // Act & Assert
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class, () -> service.getSecret("SECRET", "project", "  "));
    assertTrue(exception.getMessage().contains("Environment cannot be null or empty"));
  }

  @Test
  void testGetSecretWithEmptyPathDefaultsToRoot() {
    // Arrange
    String expectedValue = "secret-value";
    try {
      when(mockSecret.getSecretValue()).thenReturn(expectedValue);
      when(mockSecretsClient.GetSecret(
              anyString(), anyString(), anyString(), eq("/"), eq(false), eq(false), isNull()))
          .thenReturn(mockSecret);

      InfisicalService service = createServiceWithToken();

      // Act
      String result = service.getSecret("SECRET", "project", "dev", "  ");

      // Assert
      assertEquals(expectedValue, result);
      verify(mockSecretsClient).GetSecret("SECRET", "project", "dev", "/", false, false, null);
    } catch (Exception e) {
      fail("Test failed with exception: " + e.getMessage());
    }
  }

  @Test
  void testGetSecretWithNullPathDefaultsToRoot() {
    // Arrange
    String expectedValue = "secret-value";
    try {
      when(mockSecret.getSecretValue()).thenReturn(expectedValue);
      when(mockSecretsClient.GetSecret(
              anyString(), anyString(), anyString(), eq("/"), eq(false), eq(false), isNull()))
          .thenReturn(mockSecret);

      InfisicalService service = createServiceWithToken();

      // Act
      String result = service.getSecret("SECRET", "project", "dev", null);

      // Assert
      assertEquals(expectedValue, result);
      verify(mockSecretsClient).GetSecret("SECRET", "project", "dev", "/", false, false, null);
    } catch (Exception e) {
      fail("Test failed with exception: " + e.getMessage());
    }
  }

  @Test
  void testConstructorThrowsExceptionWhenNoCredentialsProvided() {
    // Act & Assert
    try (MockedConstruction<InfisicalSdk> ignored =
        mockConstruction(
            InfisicalSdk.class,
            (mock, context) -> {
              when(mock.Auth()).thenReturn(mockAuthClient);
            })) {

      IllegalArgumentException exception =
          assertThrows(
              IllegalArgumentException.class,
              () -> new InfisicalService("https://app.infisical.com", "", "", ""));
      assertTrue(
          exception
              .getMessage()
              .contains("Either INFISICAL_SERVER_TOKEN or both INFISICAL_CLIENT_ID"));
    }
  }

  @Test
  void testGetSecretHandlesSDKException() {
    // Arrange
    try {
      when(mockSecretsClient.GetSecret(
              anyString(),
              anyString(),
              anyString(),
              anyString(),
              anyBoolean(),
              anyBoolean(),
              any()))
          .thenThrow(new RuntimeException("SDK Error"));

      InfisicalService service = createServiceWithToken();

      // Act & Assert
      RuntimeException exception =
          assertThrows(RuntimeException.class, () -> service.getSecret("SECRET", "project", "dev"));

      // The exception is re-thrown, so check the original message or the cause
      assertTrue(
          exception.getMessage().contains("SDK Error")
              || (exception.getCause() != null
                  && exception.getCause().getMessage().contains("SDK Error")),
          "Expected exception message or cause to contain 'SDK Error' but was: "
              + exception.getMessage());
    } catch (Exception e) {
      fail("Test failed with exception: " + e.getMessage());
    }
  }

  @Test
  void testGetSecretHandlesGenericException() {
    // Arrange
    try {
      when(mockSecretsClient.GetSecret(
              anyString(),
              anyString(),
              anyString(),
              anyString(),
              anyBoolean(),
              anyBoolean(),
              any()))
          .thenThrow(new RuntimeException("Unexpected error"));

      InfisicalService service = createServiceWithToken();

      // Act & Assert
      RuntimeException exception =
          assertThrows(RuntimeException.class, () -> service.getSecret("SECRET", "project", "dev"));

      // The exception is re-thrown, so check the original message or the cause
      assertTrue(
          exception.getMessage().contains("Unexpected error")
              || (exception.getCause() != null
                  && exception.getCause().getMessage().contains("Unexpected error")),
          "Expected exception message or cause to contain 'Unexpected error' but was: "
              + exception.getMessage());
    } catch (Exception e) {
      fail("Test failed with exception: " + e.getMessage());
    }
  }

  @Test
  void testConstructorAuthenticatesWithAccessToken() {
    // Arrange & Act
    try (MockedConstruction<InfisicalSdk> ignored =
        mockConstruction(
            InfisicalSdk.class,
            (mock, context) -> {
              when(mock.Auth()).thenReturn(mockAuthClient);
              when(mock.Secrets()).thenReturn(mockSecretsClient);
            })) {

      new InfisicalService("https://app.infisical.com", "test-token", "", "");

      // Assert
      verify(mockAuthClient).SetAccessToken("test-token");
    }
  }

  @Test
  void testConstructorAuthenticatesWithUniversalAuth() {
    // Arrange & Act
    try (MockedConstruction<InfisicalSdk> ignored =
        mockConstruction(
            InfisicalSdk.class,
            (mock, context) -> {
              when(mock.Auth()).thenReturn(mockAuthClient);
              when(mock.Secrets()).thenReturn(mockSecretsClient);
            })) {

      new InfisicalService("https://app.infisical.com", "", "client-id", "client-secret");

      // Assert
      verify(mockAuthClient).UniversalAuthLogin("client-id", "client-secret");
    } catch (Exception e) {
      fail("Test failed with exception: " + e.getMessage());
    }
  }
}
