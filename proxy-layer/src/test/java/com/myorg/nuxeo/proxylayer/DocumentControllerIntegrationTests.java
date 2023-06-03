package com.myorg.nuxeo.proxylayer;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
public class DocumentControllerIntegrationTests {
    @Autowired
    private WebTestClient webTestClient;

    private String uid;
    private final String path = "/default-domain/workspaces";

    @Test
    public void testUploadDocGivenPath_Returns201() {
        ClassPathResource file = new ClassPathResource("images.pdf");
        webTestClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/document/upload")
                        .queryParam("path", this.path)
                        .build())
                .accept(MediaType.ALL)
                .body(BodyInserters.fromMultipartData("file", file))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.result[0].uid").isNotEmpty()
                .jsonPath("$.result[0].documentTitle").isNotEmpty()
                .jsonPath("$.result[0].path").isNotEmpty()
                .jsonPath("$.result[0].url").isNotEmpty();

        Map<String, String> properties = new HashMap<>();
        properties.put("dc:title", "test dc:title");

        webTestClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/document/upload")
                        .queryParam("path", this.path)
                        .build())
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData("file", file)
                        .with("meta-data", properties))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.result[0].uid").isNotEmpty()
                .jsonPath("$.result[0].documentTitle").isEqualTo("test dc:title")
                .jsonPath("$.result[0].path").isNotEmpty()
                .jsonPath("$.result[0].url").isNotEmpty();
    }

    @Test
    public void testUploadDocGivenPath_Returns404() {
        ClassPathResource file = new ClassPathResource("images.pdf");
        webTestClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/document/upload")
                        .queryParam("path", "Incorrect path")
                        .build())
                .accept(MediaType.ALL)
                .body(BodyInserters.fromMultipartData("file", file))
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.result[0].error").isEqualTo("Document couldn't be uploaded");
    }

    @Test
    public void testUploadDocGivenPath_Returns400() {
        ClassPathResource file = new ClassPathResource("images.pdf");
        webTestClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/document/upload")
                        .build())
                .accept(MediaType.ALL)
                .body(BodyInserters.fromMultipartData("file", file))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.Error").isEqualTo("Please Contact SmartDoc Support");

        webTestClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/document/upload")
                        .queryParam("path", this.path)
                        .build())
                .accept(MediaType.ALL)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.Error").isEqualTo("Please Contact SmartDoc Support");
    }

    @Test
    public void testGetDocGivenUid_Returns200() throws Exception {
        ClassPathResource file = new ClassPathResource("images.pdf");
        byte[] originalFileBytes = Files.readAllBytes(file.getFile().toPath());
        webTestClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/document/upload")
                        .queryParam("path", this.path)
                        .build())
                .accept(MediaType.ALL)
                .body(BodyInserters.fromMultipartData("file", file))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.result[0].uid").isNotEmpty()
                .jsonPath("$.result[0].uid").value(value -> this.uid = value.toString());

        byte[] responseFileBytes = webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/document/get/uid/{uid}")
                        .build(this.uid))
                .accept(MediaType.parseMediaType("application/pdf"))
                .exchange()
                .expectStatus().isOk()
                .expectBody(byte[].class)
                .returnResult()
                .getResponseBody();

        assertNotNull(responseFileBytes);
        assertTrue(responseFileBytes.length > 0);
        assertArrayEquals(originalFileBytes, responseFileBytes);
    }


    @Test
    public void testGetDocGivenUid_Returns404() {
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/document/get/uid/{uid}")
                        .build("Incorrect uid"))
                .accept(MediaType.ALL)
                .exchange()
                .expectStatus().isNotFound();
    }


    @Test
    public void testUpdateDocGivenUid_Returns200() {
        ClassPathResource file = new ClassPathResource("images.pdf");
        webTestClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/document/upload")
                        .queryParam("path", this.path)
                        .build())
                .accept(MediaType.ALL)
                .body(BodyInserters.fromMultipartData("file", file))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.result[0].uid").isNotEmpty()
                .jsonPath("$.result[0].uid").value(value -> this.uid = value.toString());

        Map<String, String> properties = new HashMap<>();
        properties.put("dc:title", "test dc:title");

        webTestClient.put()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/document/update")
                        .queryParam("uid", this.uid)
                        .build())
                .accept(MediaType.ALL)
                .body(BodyInserters.fromMultipartData("file", file))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.result[0].uid").isNotEmpty()
                .jsonPath("$.result[0].documentTitle").isNotEmpty()
                .jsonPath("$.result[0].path").isNotEmpty()
                .jsonPath("$.result[0].url").isNotEmpty();

        webTestClient.put()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/document/update")
                        .queryParam("uid", this.uid)
                        .build())
                .accept(MediaType.ALL)
                .body(BodyInserters.fromMultipartData("meta-data", properties))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.result[0].uid").isNotEmpty()
                .jsonPath("$.result[0].documentTitle").isEqualTo("test dc:title")
                .jsonPath("$.result[0].path").isNotEmpty()
                .jsonPath("$.result[0].url").isNotEmpty();

        webTestClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/document/upload")
                        .queryParam("path", this.path)
                        .build())
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData("file", file)
                        .with("meta-data", properties))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.result[0].uid").isNotEmpty()
                .jsonPath("$.result[0].documentTitle").isEqualTo("test dc:title")
                .jsonPath("$.result[0].path").isNotEmpty()
                .jsonPath("$.result[0].url").isNotEmpty();
    }

    @Test
    public void testUpdateDocGivenUid_Returns404() {
        ClassPathResource file = new ClassPathResource("images.pdf");
        webTestClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/document/upload")
                        .queryParam("path", this.path)
                        .build())
                .accept(MediaType.ALL)
                .body(BodyInserters.fromMultipartData("file", file))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.result[0].uid").isNotEmpty()
                .jsonPath("$.result[0].uid").value(value -> this.uid = value.toString());

        webTestClient.put()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/document/update")
                        .queryParam("uid", "Incorrect uid")
                        .build())
                .accept(MediaType.ALL)
                .body(BodyInserters.fromMultipartData("file", file))
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.result[0].error").isEqualTo("Document couldn't be updated");
    }

    @Test
    public void testUpdateDocGivenUid_Returns400() {
        ClassPathResource file = new ClassPathResource("images.pdf");
        webTestClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/document/upload")
                        .queryParam("path", this.path)
                        .build())
                .accept(MediaType.ALL)
                .body(BodyInserters.fromMultipartData("file", file))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.result[0].uid").isNotEmpty()
                .jsonPath("$.result[0].uid").value(value -> this.uid = value.toString());

        webTestClient.put()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/document/update")
                        .queryParam("uid", this.uid)
                        .build())
                .accept(MediaType.ALL)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.result[0].error").isEqualTo("Please enter file or property(s) to update");
    }

    @Test
    public void testDeleteDocGivenUid_Returns200() {
        ClassPathResource file = new ClassPathResource("images.pdf");
        webTestClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/document/upload")
                        .queryParam("path", this.path)
                        .build())
                .accept(MediaType.ALL)
                .body(BodyInserters.fromMultipartData("file", file))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.result[0].uid").isNotEmpty()
                .jsonPath("$.result[0].uid").value(value -> this.uid = value.toString());

        webTestClient.delete()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/document/delete")
                        .queryParam("uid", this.uid)
                        .build())
                .accept(MediaType.ALL)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.Success").isEqualTo("Document deleted successfully");
    }

    @Test
    public void testDeleteDocGivenUid_Returns404() {
        webTestClient.delete()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/document/delete")
                        .queryParam("uid", "Incorrect uid")
                        .build())
                .accept(MediaType.ALL)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.Error").isEqualTo("Document couldn't be deleted");
    }

    @Test
    public void testDeleteDocGivenUid_Returns400() {
        webTestClient.delete()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/document/delete")
                        .build())
                .accept(MediaType.ALL)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.Error").isEqualTo("Please Contact SmartDoc Support");
    }


}
