package com.mcp.rag.llm.module.model;

import lombok.Builder;
import lombok.Data;
import org.springframework.ai.content.Media;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.util.MimeTypeUtils;

import java.net.MalformedURLException;

@Data
@Builder
public class RagSource {

    private String content; // Text / Question or OCR results

    private String path; // Media Original path (e.g., gcs://... or s3://... or /data/...)

    private String documentId;
    private String sourceType;   // mimeType - pdf, audio, video, image

    private double score;


    // Helper method to convert this source into a Spring AI Media object
    public Media toMedia() {
        if (this.path == null || detectSourceType(path).equalsIgnoreCase("text")) return null;

        try {
            Resource resource;
            // 1. Check if it's a remote URL
            if (path.startsWith("http://") || path.startsWith("https://") || path.startsWith("gs://")) {
                resource = new UrlResource(this.path);
            }
            // 2. Handle as internal File System path
            else {
                // FileSystemResource does NOT throw MalformedURLException
                resource = new FileSystemResource(this.path);
            }

            String mimeType = resolveMimeType(this.path);
            return new Media(MimeTypeUtils.parseMimeType(mimeType), resource);

        } catch (MalformedURLException e) {
            // This only happens for the UrlResource branch
            //log.error("Invalid URL format: {}", this.path);
            return null;
        }
    }

    private String detectSourceType(String path) {
        String lower = path.toLowerCase();
        if (lower.endsWith(".txt") || lower.endsWith(".md") || lower.endsWith(".pdf") || lower.endsWith(".docx")) {
            return "text";
        }
        if (lower.endsWith(".mp3") || lower.endsWith(".wav")) {
            return "audio";
        }
        if (lower.endsWith(".mp4") || lower.endsWith(".mov") || lower.endsWith(".avi")) {
            return "video";
        }
        if (lower.endsWith(".png") || lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".bmp")) {
            return "image";
        }
        return "text";
    }

    public String resolveMimeType(String path) throws MalformedURLException{
        // This looks at the extension (e.g., "manual.pdf") and returns application/pdf
        String mimeType = MediaTypeFactory.getMediaType(path)
                .map(MediaType::toString)
                .orElse("application/octet-stream"); // Fallback for unknown types

        return mimeType;
    }

}

