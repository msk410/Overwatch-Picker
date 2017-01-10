/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package overwatchteampicker;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import org.opencv.calib3d.Calib3d;
import org.opencv.core.*;
import org.opencv.features2d.*;
import org.opencv.highgui.Highgui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;
import java.sql.*;
import java.util.Collections;



public class OverwatchTeamPicker {
    public static void main(String[] args) {

        
        Map<String, Integer> heroes = new HashMap<>();
        Map<String, Integer> counters;
        heroes.put("lucio", 0);
        heroes.put("roadhog", 0);
        heroes.put("soldier76", 0);
        heroes.put("torb", 0);
        heroes.put("zarya", 0);
        heroes.put("ana", 0);
        heroes.put("mccree", 0);
        heroes.put("reaper", 0);
        heroes.put("rein", 0);
        heroes.put("hanzo", 0);
        heroes.put("widow", 0);
        heroes.put("mei", 0);
        heroes.put("pharah", 0);
        heroes.put("tracer", 0);
        heroes.put("junkrat", 0);
        heroes.put("dva", 0);
        heroes.put("bastion", 0);
        heroes.put("symmetra", 0);
        heroes.put("zenyatta", 0);
        heroes.put("genji", 0);
        heroes.put("mercy", 0);
        heroes.put("winston", 0);
        counters = new HashMap<String, Integer>(heroes);
        
        String sourceImage = args[0];
        resizeImage(sourceImage);
        sourceImage = "small.jpg";
        //look for mid point
        ReturnValues retVal = null;
        Iterator it = heroes.entrySet().iterator();
        while(it.hasNext()) {
            Map.Entry<String, Integer> entry = (Map.Entry)it.next();
            String temp = entry.getKey();
            retVal = findImage(temp, sourceImage, 0);
            if(retVal != null) {
                break;
            }
        }
        
        //save image of just the top half of the picture
        BufferedImage image = null;
        try {
            image = ImageIO.read(new File(sourceImage));
            BufferedImage SubImgage = image.getSubimage(0, 0, image.getWidth(), retVal.yMid);
            File outputfile = new File("croppedImage.png");
            ImageIO.write(SubImgage, "png", outputfile);
        } catch(Exception e) {
            e.printStackTrace();
        }
        
        System.out.println("\n\nstarting to look\n\n");

       boolean flag = true;
       int foundHeroes = 0;
       int numLooks = 0;
     //  while(flag) {
       //    numLooks++;
           for(String s : heroes.keySet()) {
               retVal = findImage(s, "croppedImage.png", 1);
               if(retVal != null) {
                   blackOut(retVal);
                   int numOfOccur = heroes.get(s);
                   numOfOccur++;
                   heroes.put(s, numOfOccur);
                   foundHeroes++;
                   System.out.println(foundHeroes);
                   if(foundHeroes >=6) {
                       flag = false;
                   }
           //       break;
               }
           }
   //        if(numLooks >= 6) {
    //           flag = false;
   //        }
       //}
       heroes.values().removeAll(Collections.singleton(0)); //removes all heroes not found 
       counters(heroes, counters);
       
       Iterator i = counters.entrySet().iterator();
       while(i.hasNext()) {
           Map.Entry<String, Integer> e = (Map.Entry)i.next();
           System.out.println(e.getKey() + " " + e.getValue());
       }
        
        

    }
    public static ReturnValues findImage(String template, String source, int flag) {
        File lib = null;
        BufferedImage image = null;
        try {
            image = ImageIO.read(new File(source));
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        String os = System.getProperty("os.name");
        String bitness = System.getProperty("sun.arch.data.model");

        if (os.toUpperCase().contains("WINDOWS")) {
            if (bitness.endsWith("64")) {
                lib = new File("C:\\Users\\POWERUSER\\Downloads\\opencv\\build\\java\\x64\\" + System.mapLibraryName("opencv_java2413"));
            } else {
                lib = new File("libs//x86//" + System.mapLibraryName("opencv_java2413"));
            }
        }
        System.load(lib.getAbsolutePath());
        String tempObject = "images\\hero_templates\\" + template + ".png";
        String source_pic = source;
        Mat objectImage = Highgui.imread(tempObject, Highgui.CV_LOAD_IMAGE_GRAYSCALE);
        Mat sceneImage = Highgui.imread(source_pic, Highgui.CV_LOAD_IMAGE_GRAYSCALE);

        MatOfKeyPoint objectKeyPoints = new MatOfKeyPoint();
        FeatureDetector featureDetector = FeatureDetector.create(FeatureDetector.SURF);
        featureDetector.detect(objectImage, objectKeyPoints);
        KeyPoint[] keypoints = objectKeyPoints.toArray();
        MatOfKeyPoint objectDescriptors = new MatOfKeyPoint();
        DescriptorExtractor descriptorExtractor = DescriptorExtractor.create(DescriptorExtractor.SURF);
        descriptorExtractor.compute(objectImage, objectKeyPoints, objectDescriptors);

        // Create the matrix for output image.
        Mat outputImage = new Mat(objectImage.rows(), objectImage.cols(), Highgui.CV_LOAD_IMAGE_COLOR);
        Scalar newKeypointColor = new Scalar(255, 0, 0);
        Features2d.drawKeypoints(objectImage, objectKeyPoints, outputImage, newKeypointColor, 0);

        // Match object image with the scene image
        MatOfKeyPoint sceneKeyPoints = new MatOfKeyPoint();
        MatOfKeyPoint sceneDescriptors = new MatOfKeyPoint();
        featureDetector.detect(sceneImage, sceneKeyPoints);
        descriptorExtractor.compute(sceneImage, sceneKeyPoints, sceneDescriptors);

        Mat matchoutput = new Mat(sceneImage.rows() * 2, sceneImage.cols() * 2, Highgui.CV_LOAD_IMAGE_COLOR);
        Scalar matchestColor = new Scalar(0, 255, 25);

        List<MatOfDMatch> matches = new LinkedList<MatOfDMatch>();
        DescriptorMatcher descriptorMatcher = DescriptorMatcher.create(DescriptorMatcher.FLANNBASED);
        descriptorMatcher.knnMatch(objectDescriptors, sceneDescriptors, matches, 2);

        LinkedList<DMatch> goodMatchesList = new LinkedList<DMatch>();

        float nndrRatio = .78f;

        for (int i = 0; i < matches.size(); i++) {
            MatOfDMatch matofDMatch = matches.get(i);
            DMatch[] dmatcharray = matofDMatch.toArray();
            DMatch m1 = dmatcharray[0];
            DMatch m2 = dmatcharray[1];

            if (m1.distance <= m2.distance * nndrRatio) {
                goodMatchesList.addLast(m1);

            }
        }

        if (goodMatchesList.size() >= 4) {

            List<KeyPoint> objKeypointlist = objectKeyPoints.toList();
            List<KeyPoint> scnKeypointlist = sceneKeyPoints.toList();

            LinkedList<Point> objectPoints = new LinkedList<>();
            LinkedList<Point> scenePoints = new LinkedList<>();

            for (int i = 0; i < goodMatchesList.size(); i++) {
                objectPoints.addLast(objKeypointlist.get(goodMatchesList.get(i).queryIdx).pt);
                scenePoints.addLast(scnKeypointlist.get(goodMatchesList.get(i).trainIdx).pt);
            }

            MatOfPoint2f objMatOfPoint2f = new MatOfPoint2f();
            objMatOfPoint2f.fromList(objectPoints);
            MatOfPoint2f scnMatOfPoint2f = new MatOfPoint2f();
            scnMatOfPoint2f.fromList(scenePoints);

            Mat homography = Calib3d.findHomography(objMatOfPoint2f, scnMatOfPoint2f, Calib3d.RANSAC, 3);

            Mat obj_corners = new Mat(4, 1, CvType.CV_32FC2);
            Mat scene_corners = new Mat(4, 1, CvType.CV_32FC2);

            obj_corners.put(0, 0, new double[]{0, 0});
            obj_corners.put(1, 0, new double[]{objectImage.cols(), 0});
            obj_corners.put(2, 0, new double[]{objectImage.cols(), objectImage.rows()});
            obj_corners.put(3, 0, new double[]{0, objectImage.rows()});

            Core.perspectiveTransform(obj_corners, scene_corners, homography);

            Mat img = Highgui.imread(source_pic, Highgui.CV_LOAD_IMAGE_COLOR);
            
            
            
            Core.line(img, new Point(scene_corners.get(0, 0)), new Point(scene_corners.get(1, 0)), new Scalar(0, 255, 255), 4);
            Core.line(img, new Point(scene_corners.get(1, 0)), new Point(scene_corners.get(2, 0)), new Scalar(255, 255, 0), 4);
            Core.line(img, new Point(scene_corners.get(2, 0)), new Point(scene_corners.get(3, 0)), new Scalar(0, 255, 0), 4);
            Core.line(img, new Point(scene_corners.get(3, 0)), new Point(scene_corners.get(0, 0)), new Scalar(0, 255, 0), 4);

            MatOfDMatch goodMatches = new MatOfDMatch();
            goodMatches.fromList(goodMatchesList);

            Features2d.drawMatches(objectImage, objectKeyPoints, sceneImage, sceneKeyPoints, goodMatches, matchoutput, matchestColor, newKeypointColor, new MatOfByte(), 2);
            if(new Point(scene_corners.get(0, 0)).x < new Point(scene_corners.get(1, 0)).x &&
                    new Point(scene_corners.get(0, 0)).y < new Point(scene_corners.get(2, 0)).y) {
                System.out.println("found " + template);
                Highgui.imwrite("points.jpg", outputImage);
                Highgui.imwrite("matches.jpg", matchoutput);
                Highgui.imwrite("final.jpg", img);
                
                if(flag == 0) {
                    ReturnValues retVal = null;
                    int y = (int)new Point(scene_corners.get(3, 0)).y;
                    int yHeight = (int)new Point(scene_corners.get(3, 0)).y - (int)new Point(scene_corners.get(2, 0)).y;
                    if(y < image.getHeight() * .6) { //if found hero is in upper half of image then return point 3,0
                        retVal = new ReturnValues(y + (int)(image.getHeight() * .01), yHeight);
                    }
                    else { //if found hero is in lower half of image then return point 2,0
                        y = (int)new Point(scene_corners.get(2, 0)).y; 
                        retVal = new ReturnValues(y + (int)(image.getHeight() * .3), yHeight);
                    }
                    return retVal;
                } else if(flag == 1) {
                    int[] xPoints = new int[4];
                    int[] yPoints = new int[4];
                    
                    xPoints[0] = (int)(new Point(scene_corners.get(0, 0)).x);
                    xPoints[1] = (int)(new Point(scene_corners.get(1, 0)).x);
                    xPoints[2] = (int)(new Point(scene_corners.get(2, 0)).x);
                    xPoints[3] = (int)(new Point(scene_corners.get(3, 0)).x);
                    
                    yPoints[0] = (int)(new Point(scene_corners.get(0, 0)).y);
                    yPoints[1] = (int)(new Point(scene_corners.get(1, 0)).y);
                    yPoints[2] = (int)(new Point(scene_corners.get(2, 0)).y);
                    yPoints[3] = (int)(new Point(scene_corners.get(3, 0)).y);
                    
                    ReturnValues retVal = new ReturnValues(xPoints, yPoints);
                    return retVal;
                    
                    
                }
            }
        }
        return null;
    
    
    
    
    }

    private static void blackOut(ReturnValues retVal) {
        File imageFile = new File("croppedImage.png");
        BufferedImage img = null;
        try {
            img = ImageIO.read(imageFile);
            Graphics2D graph = img.createGraphics();
            graph.setColor(Color.black); //Then set the color to black
            graph.fillPolygon(retVal.xPoints, retVal.yPoints, 4);
            ImageIO.write(img, "png", new File("croppedImage.png"));
        } catch (Exception e) {
            e.printStackTrace();
        }
  }
    
    private static void resizeImage(String original) {
        File imageFile = new File(original);
        BufferedImage img = null;
        try {
            img = ImageIO.read(imageFile);
            
         
            BufferedImage bim=ImageIO.read(new FileInputStream(original));
            Image resizedImg=bim.getScaledInstance(1400,-1,Image.SCALE_FAST);
            int scaled_height=resizedImg.getHeight(null);
            BufferedImage rBimg=new BufferedImage(1400,scaled_height,bim.getType());
            // Create Graphics object
            Graphics2D g=rBimg.createGraphics();// Draw the resizedImg from 0,0 with no ImageObserver
            g.drawImage(resizedImg,0,0,null);
            // Dispose the Graphics object, we no longer need it
            g.dispose();

            ImageIO.write(rBimg,"jpg",new FileOutputStream("small.jpg"));
            
            
            
        }catch(Exception e) {
        }
    }

    private static void counters(Map<String, Integer> heroes, Map<String, Integer> counters) {
        Connection c = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:heroes.db");
            c.setAutoCommit(false);
            PreparedStatement ps = null;
            ResultSet rs = null;
            Iterator it = heroes.entrySet().iterator();
            while(it.hasNext()) {
                Map.Entry<String, Integer> entry = (Map.Entry)it.next();
                String hero = entry.getKey();
                int numHero = entry.getValue();
                int weight = 0;
                String col = "";
                int value = 0;
                String table = "";
                for(int i = 0; i < 2; i++) {
                    if(i == 0) table = "GOOD_AGAINST";
                    else if(i == 1) table = "BAD_AGAINST";
                    for(int j = 0; j < 3; j++) {
                        if(i == 0 && j == 0) {
                            col = "FIRST";
                            weight = 3;
                        } else if (i == 0 && j == 1) {
                            col = "SECOND";
                            weight = 2;
                        } else if(i == 0 && j == 2){
                            col = "THIRD";
                            weight = 1;
                        }else if (i == 1 && j == 0) {
                            col = "FIRST";
                            weight = -3;
                        } else if(i == 1 && j ==1){
                            col = "SECOND";
                            weight = -2;
                        }else if (i == 1 && j == 2) {
                            col = "THIRD";
                            weight = -1;
                        }
                      //  System.out.println(hero);
                        String statement = "SELECT * FROM " + table + " WHERE " + col + "= ?;";
                        ps = c.prepareStatement(statement);
                        ps.setString(1, hero.toUpperCase());
                        rs = ps.executeQuery();
                        while ( rs.next()) {
                            String counter = rs.getString(1).toLowerCase().trim();
                            value = counters.get(counter);
                            value += numHero * weight;
                            counters.put(counter, value);
                        }
                    }
                }
            }
            rs.close();
            c.close();
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
        System.out.println("Operation done successfully");
    }
}