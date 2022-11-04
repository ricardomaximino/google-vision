package es.brasatech.es.googlevision.controller;


import com.google.cloud.spring.vision.CloudVisionTemplate;
import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.FaceAnnotation;
import com.google.cloud.vision.v1.Feature;
import com.google.cloud.vision.v1.Vertex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

@RestController
public class AppController {

    @Autowired private ResourceLoader resourceLoader;

    @Autowired private CloudVisionTemplate cloudVisionTemplate;


    @GetMapping("/getLabelDetection")
    public String getLabelDetection(@RequestParam MultipartFile file) {
//        Resource imageResource = this.resourceLoader.getResource(file);
        Resource imageResource = file.getResource();
        AnnotateImageResponse response = this.cloudVisionTemplate.analyzeImage(
                imageResource, Feature.Type.LABEL_DETECTION);

        return response.getLabelAnnotationsList().toString();
    }

    @GetMapping("/getTextDetection")
    public String getTextDetection(@RequestParam MultipartFile file) {

        Resource imageResource = file.getResource();
        AnnotateImageResponse response = this.cloudVisionTemplate.analyzeImage(
                imageResource, Feature.Type.DOCUMENT_TEXT_DETECTION);

        return response.getTextAnnotationsList().toString();
    }

    @GetMapping("/getLandmarkDetection")
    public String getLandmarkDetection(@RequestParam MultipartFile file) {

        Resource imageResource = file.getResource();
        AnnotateImageResponse response = this.cloudVisionTemplate.analyzeImage(
                imageResource, Feature.Type.LANDMARK_DETECTION);

        return response.getLandmarkAnnotationsList().toString();
    }

    @GetMapping("/getFaceDetection")
    public String getFaceDetection(@RequestParam MultipartFile inputFile, @RequestParam String outputFile) throws IOException {

        Resource imageResource = inputFile.getResource();
        Resource outputImageResource = this.resourceLoader.getResource(outputFile);
        AnnotateImageResponse response = this.cloudVisionTemplate.analyzeImage(
                imageResource, Feature.Type.FACE_DETECTION);

        writeWithFaces(imageResource.getFile().toPath(), outputImageResource.getFile().toPath(), response.getFaceAnnotationsList());

        return response.getFaceAnnotationsList().toString();

    }

    private static void writeWithFaces(Path inputPath, Path outputPath, List<FaceAnnotation> faces)
            throws IOException {
        BufferedImage img = ImageIO.read(inputPath.toFile());
        annotateWithFaces(img, faces);
        ImageIO.write(img, "jpg", outputPath.toFile());
    }

    public static void annotateWithFaces(BufferedImage img, List<FaceAnnotation> faces) {
        for (FaceAnnotation face : faces) {
            annotateWithFace(img, face);
        }
    }

    private static void annotateWithFace(BufferedImage img, FaceAnnotation face) {
        Graphics2D gfx = img.createGraphics();
        Polygon poly = new Polygon();
        for (Vertex vertex : face.getFdBoundingPoly().getVerticesList()) {
            poly.addPoint(vertex.getX(), vertex.getY());
        }
        gfx.setStroke(new BasicStroke(5));
        gfx.setColor(new Color(0x00ff00));
        gfx.draw(poly);
    }
}