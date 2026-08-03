package com.example.spring.boardtoken.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.charset.MalformedInputException;
import java.util.UUID;

@Slf4j
@Service
public class FileService {

    @Value("${file.upload-dir}")
    private String uploadDir;

    public String storeFile(MultipartFile file) {

        if ( file == null || file.isEmpty() ) return null;

        try {
            // 절대 경로로 다뤄 실행 위치에 영향받지 않게 한다.
            File dir = new File(uploadDir).getAbsoluteFile();
            if ( !dir.exists() ) dir.mkdirs();

            String storedFileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            File dest = new File(dir, storedFileName);

            file.transferTo(dest);

            log.info("파일 저장 : originalFileName = {}, storedFileName = {}", file.getOriginalFilename(), storedFileName);

            return dest.getPath();
        } catch (Exception e) {
            throw new IllegalStateException("파일 저장에 실패 했습니다", e);
        }
    }

    public Resource downloadFile(String fileName) {

        try {
            File file = new File( new File(uploadDir).getAbsoluteFile(), fileName );

            Resource resource = new UrlResource(file.toURI());

            if ( !resource.exists() || !resource.isReadable() ) {
                throw new IOException("파일을 읽어오는데 실패 했습니다.");
            }

            return resource;
        } catch (MalformedInputException e) {
            throw new IllegalStateException("파일 경로가 잘못되었습니다 fileName : " + fileName, e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public void deleteFile(String filePath) {

        if ( filePath == null || filePath.isBlank() ) return;

        File file = new File(filePath);
        if ( !file.exists() ) return;

        boolean deleted = file.delete();
        if ( !deleted ) {
            log.warn("첨부파일 삭제 실패(디스에 남음) : filePath = {} ", filePath);
        }

    }

}