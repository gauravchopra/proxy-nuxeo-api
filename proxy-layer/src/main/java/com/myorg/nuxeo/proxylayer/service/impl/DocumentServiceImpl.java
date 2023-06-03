package com.myorg.nuxeo.proxylayer.service.impl;

import com.myorg.nuxeo.proxylayer.vo.GetResponse;
import com.myorg.nuxeo.proxylayer.service.DocumentService;
import com.myorg.nuxeo.proxylayer.vo.UploadResponse;
import org.jetbrains.annotations.NotNull;
import org.nuxeo.client.NuxeoClient;
import org.nuxeo.client.objects.Document;
import org.nuxeo.client.objects.Documents;
import org.nuxeo.client.objects.Repository;
import org.nuxeo.client.objects.blob.Blob;
import org.nuxeo.client.objects.blob.FileBlob;
import org.nuxeo.client.objects.upload.BatchUpload;
import org.nuxeo.client.objects.upload.BatchUploadManager;
import org.nuxeo.client.spi.NuxeoClientRemoteException;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@Service
public class DocumentServiceImpl implements DocumentService, ApplicationContextAware {


    private ApplicationContext applicationContext;

    private static final String basePath="/default-domain/workspaces";

    public NuxeoClient getNuxeoClientBean() {
        return applicationContext.getBean(NuxeoClient.class);
    }

    @Value("${nuxeo.file.download.url}")
    private String fileDownloadBaseUrl;


    @Override
    public void setApplicationContext(ApplicationContext applicationContext)
            throws BeansException {
        this.applicationContext = applicationContext;
    }

    private File createTempFile(MultipartFile multipartFile) throws IOException {
        Path tempFilePath = Files.createTempFile(multipartFile.getOriginalFilename(), "");
        Files.copy(multipartFile.getInputStream(), tempFilePath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        return tempFilePath.toFile();
    }

    private BatchUpload getBatchUpload(MultipartFile multipartFile, BatchUploadManager batchUploadManager) throws IOException {
        BatchUpload batchUpload = batchUploadManager.createBatch();
        File tempFile = createTempFile(multipartFile);
        FileBlob fileBlob = new FileBlob(tempFile);
        BatchUpload uploadedBlob = batchUpload.upload("0", fileBlob);
        tempFile.delete();
        return uploadedBlob;
    }

    private void setDocumentPropertiesFromMap(Map<String, String> properties, Document document) {
        Optional.ofNullable(properties)
                .orElse(Collections.emptyMap())
                .forEach(document::setPropertyValue);
    }

    @Override
    public ResponseEntity<byte[]> downloadDocumentGivenUid(String uid) throws Exception{
        Repository repository = getNuxeoClientBean().repository();
        try {
            Document document = repository.fetchDocumentById(uid);
            Blob blob = document.streamBlob();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(blob.getMimeType()));
            headers.setContentDisposition(ContentDisposition.attachment()
                    .filename(blob.getFilename()).build());

            byte[] data;
            try (InputStream in = blob.getStream()) {
                data = in.readAllBytes();
            }
            return ResponseEntity.status(HttpStatus.OK).headers(headers).body(data);
        } catch (NuxeoClientRemoteException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }


    public ResponseEntity<Map<String,List<String>>> getAllFilesInTheFolder(String folderPath){
        folderPath = appendBasePath(folderPath);
        List<String> files = new ArrayList<>();
        Repository repository = getNuxeoClientBean().repository();
        Document folder = repository.fetchDocumentByPath(folderPath);
        Documents documents = null;
        do {
            String pageIndex = documents == null ? "0" : String.valueOf(documents.getCurrentPageIndex() + 1);
            String query="SELECT * FROM Document where ecm:parentId=? AND ecm:isTrashed = 0";
            documents = repository.query(query, "50", pageIndex, null, "dc:title,dc:description", "ASC,DESC,",folder.getId());

            documents.streamEntries().forEach(d->{
                files.add(d.getTitle());
                if(d.getType().equals("Folder")){
                    getAllFilesInTheFolder(d.getPath());
                }
            });
        } while (documents.isNextPageAvailable());

        return ResponseEntity.status(HttpStatus.OK).body(Map.of("result", files));
    }

    @NotNull
    private static String appendBasePath(String folderPath) {
        if(!folderPath.contains(basePath)){
            folderPath =basePath+ folderPath;
        }
        return folderPath;
    }


    @Override
    public ResponseEntity<Map<String,List<GetResponse>>> getFileByFolderPath(String folderPath, String nameToSearch) {
        Repository repository = getNuxeoClientBean().repository();
        folderPath = appendBasePath(folderPath);
        List<GetResponse> responses = new ArrayList<>();
        try {
            Documents documents = null;
            List<Document> foundDocs = new ArrayList<>();

            Document folder = repository.fetchDocumentByPath(folderPath);

            do {
                nameToSearch=nameToSearch.toLowerCase();
                String pageIndex = documents == null ? "0" : String.valueOf(documents.getCurrentPageIndex() + 1);
                String query="SELECT * FROM Document where ecm:parentId=? AND ecm:isTrashed = 0";
                documents = repository.query(query, "50", pageIndex, null, "dc:title,dc:description", "ASC,DESC,",folder.getId());
                for (Document document : documents.getEntries()) {
                    if(document.getPropertyValue("dc:title").toString().toLowerCase().contains(nameToSearch))
                        foundDocs.add(document);
                }
            } while (documents.isNextPageAvailable());



            if(foundDocs.size()>0) {
                for (Document document : foundDocs) {
                    GetResponse getResponse = prepareResponseObject(document);
                    responses.add(getResponse);
                }
            }
            return ResponseEntity.status(HttpStatus.OK).body(Map.of("result", responses));
        } catch (Exception e) {
            GetResponse errorResponse = new GetResponse();
            errorResponse.setError("File not found with given search criteria");
            responses.add(errorResponse);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("result", responses));
        }
    }

    @NotNull
    private GetResponse prepareResponseObject(Document document) {
        GetResponse getResponse = new GetResponse();
        getResponse.setPath(document.getPath());
        getResponse.setUid(document.getUid());
        getResponse.setType(document.getType());
        getResponse.setTitle(document.getTitle());
        getResponse.setLastModified(document.getLastModified());
        getResponse.setUrl(fileDownloadBaseUrl+ document.getUid());
        return getResponse;
    }

    @Override
    public ResponseEntity<Map<String, List<UploadResponse>>> uploadDocumentGivenPath(MultipartFile multipartFile, String parentPath, Map<String, String> properties) throws IOException {

        Repository repository = getNuxeoClientBean().repository();
        parentPath = appendBasePath(parentPath);
        BatchUploadManager batchUploadManager = getNuxeoClientBean().batchUploadManager();

        BatchUpload uploadedBlob = getBatchUpload(multipartFile, batchUploadManager);

        Document document = Document.createWithName(multipartFile.getOriginalFilename(), "File");
        document.setPropertyValue("file:content", uploadedBlob.getBatchBlob());
        setDocumentPropertiesFromMap(properties, document);

        UploadResponse uploadResponse = new UploadResponse();
        List<UploadResponse> responses = new ArrayList<>();

        try {
            document = repository.createDocumentByPath(parentPath, document);

            uploadResponse.setUid(document.getUid());
            uploadResponse.setUrl(fileDownloadBaseUrl+document.getUid());
            uploadResponse.setDocumentTitle(document.getTitle());
            uploadResponse.setPath(document.getPath());
            responses.add(uploadResponse);

            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("result", responses));
        } catch (NuxeoClientRemoteException e) {
            uploadResponse.setError("Document couldn't be uploaded");
            responses.add(uploadResponse);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("result", responses));
        }
    }



    @Override
    public ResponseEntity<Map<String, List<UploadResponse>>> updateDocumentGivenUid(MultipartFile multipartFile, String uid, Map<String, String> properties) throws Exception {
        Repository repository = getNuxeoClientBean().repository();
        UploadResponse uploadResponse = new UploadResponse();
        List<UploadResponse> responses = new ArrayList<>();
        Document document;
        try {
            document = repository.fetchDocumentById(uid);
        } catch (NuxeoClientRemoteException e) {
            uploadResponse.setError("Document couldn't be updated");
            responses.add(uploadResponse);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("result", responses));
        }

        if (multipartFile != null) {
            BatchUploadManager batchUploadManager = getNuxeoClientBean().batchUploadManager();
            BatchUpload uploadedBlob = getBatchUpload(multipartFile, batchUploadManager);
            document.setPropertyValue("file:content", uploadedBlob.getBatchBlob());
        }
        setDocumentPropertiesFromMap(properties, document);
        document = repository.updateDocument(document);

        uploadResponse.setPath(document.getPath());
        uploadResponse.setUid(document.getUid());
        uploadResponse.setUrl(fileDownloadBaseUrl+document.getUid());
        uploadResponse.setDocumentTitle(document.getTitle());
        responses.add(uploadResponse);
        return ResponseEntity.status(HttpStatus.OK).body(Map.of("result", responses));
    }

    @Override
    public ResponseEntity<Map<String, String>> deleteDocumentGivenUid(String uid) throws Exception {
        Repository repository = getNuxeoClientBean().repository();
        try {
            Document document = repository.fetchDocumentById(uid);
            repository.deleteDocument(document);
            return ResponseEntity.status(HttpStatus.OK).body(Map.of("Success", "Document deleted successfully"));
        } catch (NuxeoClientRemoteException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("Error", "Document couldn't be deleted"));
        }
    }
}
