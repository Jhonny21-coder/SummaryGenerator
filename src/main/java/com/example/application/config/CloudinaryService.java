package com.example.application.config;

import org.springframework.stereotype.Service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileOutputStream;

import java.util.Map;

@Service
public class CloudinaryService {
    private Cloudinary cloudinary;

    public CloudinaryService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    public String uploadImage(File file, String folder) throws IOException {
        Map<String, Object> uploadResult = cloudinary.uploader().upload(file, ObjectUtils.asMap(
        	"folder", folder
        ));
        return (String) uploadResult.get("secure_url");  // Use secure URL for HTTPS
    }

    public String uploadMedia(File file, String folder) throws IOException {
    	Map<String, Object> uploadParams = ObjectUtils.asMap(
        	"folder", folder,
        	"resource_type", "video"
    	);

    	Map<String, Object> uploadResult = cloudinary.uploader().upload(file, uploadParams);
    	return (String) uploadResult.get("secure_url");  // Return secure URL for HTTPS
    }

    public String updateImage(String currentImageUrl, InputStream uploadedStream, String folder) throws IOException {
        // Extract the public ID from the existing image URL
        String publicId = extractPublicIdFromUrl(currentImageUrl);

        // Create a temporary file to store the uploaded image
        File tempFile = File.createTempFile("tempImage", ".png");
        tempFile.deleteOnExit(); // Ensure the file is deleted on exit

        // Save the uploaded image to the temporary file
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            byte[] imageBytes = uploadedStream.readAllBytes();
            fos.write(imageBytes);
        }

        // Upload the new image to Cloudinary with the same public ID
        Map<String, Object> uploadResult = cloudinary.uploader().upload(tempFile, ObjectUtils.asMap(
        	"public_id", publicId,
        	"folder", folder
	));

        // Return the secure URL of the updated image
        return (String) uploadResult.get("secure_url");
    }

    // Method to delete an image from Cloudinary
    public void deleteImage(String imageUrl) {
        try {
            // Extract public ID from the URL
            String publicId = extractPublicIdFromUrl(imageUrl);

            // Call the Cloudinary destroy method
            Map<String, Object> result = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());

            if ("ok".equals(result.get("result"))) {
                System.out.println("Image deleted successfully");
            } else {
                System.out.println("Failed to delete image: " + result.get("result"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*public Map uploadGoogleImage(InputStream imageStream, String fileName) throws Exception {
        Map uploadResult = cloudinary.uploader().upload(imageStream, ObjectUtils.asMap(
            "public_id", fileName,  // Use the user's ID or any unique identifier as the file name
            "overwrite", true,
            "resource_type", "image"
        ));
        return uploadResult;
    }*/

    private String extractPublicIdFromUrl(String imageUrl) {
        String[] parts = imageUrl.split("/");
        String publicIdWithExtension = parts[parts.length - 1];
        return publicIdWithExtension.split("\\.")[0]; // Remove extension to get public ID
    }
}
