package examples;


import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.rekognition.AmazonRekognition;
import com.amazonaws.services.rekognition.AmazonRekognitionClientBuilder;
import com.amazonaws.services.rekognition.model.*;
import com.amazonaws.util.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.List;


public class CompareFacesExample {
    static ByteBuffer sourceImageBytes = null;
    static ByteBuffer targetImageBytes = null;

    public static void main(String[] args) throws Exception {
        Float similarityThreshold = 70F;
        String sourceImage = "Data_with_blue_eyes.jpg";
        String targetImage = "insettngnew.jpg";
//        String targetImage = "storgcrew.jpg";



        AWSCredentials credentials;
        try {
            credentials = new ProfileCredentialsProvider("awstest").getCredentials();
        } catch (Exception e) {
            throw new AmazonClientException("Cannot load the credentials from the credential profiles file. "
                    + "Please make sure that your credentials file is at the correct "
                    + "location (/Users/userid/.aws/credentials), and is in valid format.", e);
        }


        AmazonRekognition rekognitionClient = AmazonRekognitionClientBuilder
                .standard()
                .withRegion(Regions.EU_WEST_1)
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .build();


        //Load LOCAL source and target images and create input parameters
        sourceImageBytes = getByteBufferFromImage(sourceImage);
        targetImageBytes = getByteBufferFromImage(targetImage);

        Image source = new Image()
                .withBytes(sourceImageBytes);
        Image target = new Image()
                .withBytes(targetImageBytes);



        CompareFacesRequest request = new CompareFacesRequest()
                .withSourceImage(source)
                .withTargetImage(target)
                .withSimilarityThreshold(similarityThreshold);

        // Call operation
        CompareFacesResult compareFacesResult = rekognitionClient.compareFaces(request);


        // Display results
        List<CompareFacesMatch> faceDetails = compareFacesResult.getFaceMatches();
        for (CompareFacesMatch match : faceDetails) {
            ComparedFace face = match.getFace();
            BoundingBox position = face.getBoundingBox();
            System.out.println("Face at " + position.getLeft().toString()
                    + " " + position.getTop()
                    + " matches with " + face.getConfidence().toString()
                    + "% confidence.");

        }
        List<ComparedFace> uncompared = compareFacesResult.getUnmatchedFaces();

        System.out.println("There were " + uncompared.size()
                + " that did not match");
        System.out.println("Source image rotation: " + compareFacesResult.getSourceImageOrientationCorrection());
        System.out.println("target image rotation: " + compareFacesResult.getTargetImageOrientationCorrection());
    }


    // --------------------- Helper
    private static ByteBuffer getByteBufferFromImage(String image) {
        ByteBuffer imageBytes = null;
        try (InputStream inputStream = new FileInputStream(new File("src/main/resources/"+image))) {
            imageBytes = ByteBuffer.wrap(IOUtils.toByteArray(inputStream));
        } catch (Exception e) {
            System.out.println("Failed to load image: " + image);
            System.exit(1);
        }
        return imageBytes;
    }
}


