package com.myorg.nuxeo.proxylayer;

import com.myorg.nuxeo.proxylayer.service.DocumentService;
import com.myorg.nuxeo.proxylayer.vo.GetResponse;
import com.myorg.nuxeo.proxylayer.vo.UploadResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
public class DocumentController {


    @Autowired
    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @GetMapping("/api/v1/document/get/path")
    public ResponseEntity<Map<String,List<GetResponse>>> getDocumentDetailGivenPathAndName(@RequestParam String folderPath,
                                                                                           @RequestParam String documentNameToSearch) throws MalformedURLException {
        ResponseEntity<Map<String,List<GetResponse>>> response=documentService.getFileByFolderPath(folderPath,documentNameToSearch);
        return response;
    }

    @GetMapping("/api/v1/document/get/uid/{uid}")
    public ResponseEntity<byte[]> downloadDocumentGivenUid(@PathVariable String uid) throws Exception {
        return documentService.downloadDocumentGivenUid(uid);
    }

    @PostMapping(value = "/api/v1/document/upload")
    public ResponseEntity<Map<String, List<UploadResponse>>> uploadDocumentGivenPath(
            @RequestParam(value = "path") String path,
            @RequestPart(value = "file") MultipartFile file,
            @RequestPart(value = "meta-data", required = false)  Map<String, String> properties
    ) throws Exception {
        return documentService.uploadDocumentGivenPath(file, path, properties);
    }

    @PutMapping("/api/v1/document/update")
    public ResponseEntity<Map<String, List<UploadResponse>>> updateDocumentGivenUid(
            @RequestParam(name = "uid") String uid,
            @RequestPart(name = "file", required = false) MultipartFile file,
            @RequestPart(value = "meta-data", required = false) Map<String, String> properties
    ) throws Exception {
        if (file == null && (properties == null || properties.isEmpty())) {
            UploadResponse uploadResponse = new UploadResponse();
            List<UploadResponse> responses = new ArrayList<>();
            uploadResponse.setError("Please enter file or property(s) to update");
            responses.add(uploadResponse);
            return ResponseEntity.badRequest().body(Map.of("result", responses));
        }
        return documentService.updateDocumentGivenUid(file, uid, properties);
    }

    @DeleteMapping("/api/v1/document/delete")
    public ResponseEntity<Map<String, String>> deleteDocumentGivenUid(
            @RequestParam(name = "uid") String uid
    ) throws Exception {
        return documentService.deleteDocumentGivenUid(uid);
    }

}
