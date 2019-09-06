
import java.awt.Color;
import java.awt.image.BufferedImage;
import static java.awt.image.BufferedImage.TYPE_INT_RGB;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import javax.imageio.ImageIO;

/* 
@author Jose M. Hernandez
Notes:A  
*/
public class Main {
    
    //Convert int to RGB values in Color object
    public static Color intToRGB(int i) {
        int a, r, g, b;
        //Gets the binary string representation of the integer with 24 bits
        String binary = Integer.toBinaryString(i);
        //System.out.println(i);
        //System.out.println(binary);
        //System.out.println(Integer.parseInt(binary, 2));
        while(binary.length() < 32) {
            binary = "0" + binary;
        }
        //Then split into seperate R G B binary strings
        //Bits 0-7 are Red, 8-15 Blue, 16-23 Green
        //Convert those substrings back to int
        a = Integer.parseInt(binary.substring(0, 8), 2);
        r = Integer.parseInt(binary.substring(8, 16), 2);
        g = Integer.parseInt(binary.substring(16, 24), 2);
        b = Integer.parseInt(binary.substring(24, 32), 2);
        return new Color(r, g, b, a);
    }
    
    //Convert RGB values to int
    public static int RGBtoInt(Color rgb) {
        if(rgb != null) {
            String a = Integer.toBinaryString(rgb.getAlpha());
            String r = Integer.toBinaryString(rgb.getRed());
            String g = Integer.toBinaryString(rgb.getGreen());
            String b = Integer.toBinaryString(rgb.getBlue());
            while(r.length() < 8 || g.length() < 8 || b.length() < 8 || a.length() < 8) {
                if(r.length() < 8) {
                    r = "0" + r;
                }
                if(g.length() < 8) {
                    g = "0" + g;
                }
                if(b.length() < 8) {
                    b = "0" + b;
                }
                if(a.length() < 8) {
                    a = "0" + a;
                }
            }
            //+ is normal - is reverse rgb;
            a = "+0000000";
            //a = "-0000000";
            return Integer.parseInt(a + r + g + b, 2);
        }
        return 0;
    }
    
    //Get RGB int values from image into Color object
    public static Color[][] getRGBs(BufferedImage in) {
        int width = in.getWidth();
        int height = in.getHeight();
        Color[][] rgb = new Color[width][height];
        for(int x = 0; x < width; x++) {
            for(int y = 0; y < height; y++) { 
                rgb[x][y] = intToRGB(in.getRGB(x, y));
            }
        }
        return rgb;
    }
    
    public static BufferedImage imageBlackNWhite(BufferedImage in) {
        BufferedImage out = new BufferedImage(in.getWidth(), in.getHeight(), TYPE_INT_RGB);
        Color[][] rgbBase = getRGBs(in);
        int rgbNew, r, g, b;
        for(int x = 0; x < in.getWidth(); x++) {
            for(int y = 0; y < in.getHeight(); y++) {
                r = rgbBase[x][y].getRed();
                g = rgbBase[x][y].getGreen();
                b = rgbBase[x][y].getBlue();
                rgbNew = (r + g + b) / 3;
                out.setRGB(x, y, RGBtoInt(new Color(rgbNew, rgbNew, rgbNew)));
            }
        }       
        return out;
    }
    
    public static BufferedImage imageEdge(BufferedImage in) {
        BufferedImage out = new BufferedImage(in.getWidth(), in.getHeight(), TYPE_INT_RGB);
        Color[][] rgbBase = getRGBs(in);
        int x, y, m, n, a, b, r, g, bb;
        int w = in.getWidth(); 
        int h = in.getHeight();
        int sumR = 0;   
        int sumG = 0;
        int sumB = 0;
        int[][] tmpR = new int[w][h];
        int[][] tmpG = new int[w][h];
        int[][] tmpB = new int[w][h];
        for(y = 0; y < h; y++){
            for(x = 0; x < w; x++) {
                tmpR[x][y] = rgbBase[x][y].getRed();
                tmpG[x][y] = rgbBase[x][y].getGreen();
                tmpB[x][y] = rgbBase[x][y].getBlue();
            }
        }
        for(y = 1; y < h - 1; y++){
            for(x = 1; x < w - 1; x++){
                for(n = -1; n <= 1; n++){
                    for(m = -1; m <= 1; m++) {
                        a = (x + m >= w) ? w - 1 : (x + m < 0) ? 0 : x + m;
                        b = (y + n >= h) ? h - 1 : (y + n < 0) ? 0 : y + n;
                        sumR += (tmpR[x][y] - tmpR[a][b]);
                        sumG += (tmpG[x][y] - tmpG[a][b]);
                        sumB += (tmpB[x][y] - tmpB[a][b]);
                    }
                }
                r = (sumR > 255) ? 255: (sumR < 0) ? 0: sumR;
                g = (sumG > 255) ? 255: (sumG < 0) ? 0: sumG;
                bb = (sumB > 255) ? 255: (sumB < 0) ? 0: sumB;               
                out.setRGB(x, y, RGBtoInt(new Color(r, g, bb)));
                sumR = sumG = sumB = 0;
            }
        }
        //Set the 4 edges of image black
        for (y = 0; y < h; y++) {
            out.setRGB(0, y, 0);
            out.setRGB(w - 1, y, 0);
        }
        for (x = 0; x < w; x++) {
            out.setRGB(x, 0, 0);
            out.setRGB(x, h - 1, 0);
        } 
        return out;
    }
    
    //Splits image into 16 squares and randomizes the placement
    public static BufferedImage imageRandom16(BufferedImage in) {
        int w = in.getWidth();
        int h = in.getHeight();
        int wdiv = w / 4;
        int hdiv = h / 4;
        int r1, r2, j, k, i;            
        BufferedImage out = new BufferedImage(w, h, TYPE_INT_RGB);
        Color[][] rgbBase = getRGBs(in);
        //Pair is the coords of a single square
        String pair;
        List<String> pairs = new ArrayList<>();
        //Get the 16 squares
        j = k = i = 0;
        while(pairs.size() < 16) {
            r1 = (int) (Math.random() * 4);
            r2 = (int) (Math.random() * 4);
            pair = Integer.toString(r1) + Integer.toString(r2);
            if(!pairs.contains(pair)) {
                pairs.add(pair);             
                for(int x = 0; x < wdiv; x++) {
                    for(int y = 0; y < hdiv; y++) {
                        out.setRGB(x + (j * wdiv), y + (k * hdiv), RGBtoInt(rgbBase[x + (r1 * wdiv)][y + (r2 * hdiv)]));
                    }
                }  
                i++;
                j = i % 4;
                if(i % 4 == 0) {
                    k++;
                }
            }
        }
        return out;
    }
    
    public static BufferedImage imageSquare(BufferedImage in) {
        int w = in.getWidth();
        int h = in.getHeight();
        int square = 10;
        int squaresW = in.getWidth() / square;
        int squaresH = in.getHeight() / square;
        int sumR, sumG, sumB, i, j, k; 
        int r, g, b;
        Color[][] rgbBase = getRGBs(in);
        BufferedImage out = new BufferedImage(w, h, TYPE_INT_RGB);
        i = j = k = 0;
        while(i < (squaresW * squaresH)) {
            sumR = sumG = sumB = 0;
            for(int x = 0; x < square; x++) {
                for(int y = 0; y < square; y++) {
                    sumR += rgbBase[x + (j * square)][y + (k * square)].getRed();
                    sumG += rgbBase[x + (j * square)][y + (k * square)].getGreen();
                    sumB += rgbBase[x + (j * square)][y + (k * square)].getBlue();
                }       
            }
            r = sumR / (square * square);
            g = sumG / (square * square);
            b = sumB / (square * square);
            for(int x = 0; x < square; x++) {
                for(int y = 0; y < square; y++) {
                    out.setRGB(x + (j * square), y + (k * square), RGBtoInt(new Color(r, g, b)));
                }       
            }         
            i++;
            j = i % squaresW;
            if(i % squaresW == 0) {
                k++;
            }
        }
        
        return out;
    }
    
    public static BufferedImage imageReverseH(BufferedImage in) {
        int w = in.getWidth();
        int h = in.getHeight();
        BufferedImage out = new BufferedImage(w, h, TYPE_INT_RGB);
        for(int x = 0; x < w - 1; x++) {
            for(int y = 0; y < h; y++) {
                out.setRGB(x, y, in.getRGB(w - x - 1, y));
            }
        }
        return out;
    }
    
    public static BufferedImage imageTest(BufferedImage in) {
        int w = in.getWidth();
        int h = in.getHeight();
        BufferedImage out = new BufferedImage(w, h, TYPE_INT_RGB);
        Color[][] rgb = getRGBs(in);
        int r, g, b;
        for(int x = 0; x < w; x++) {
            for(int y = 0; y < h; y++) {
                r = rgb[x][y].getRed();
                g = rgb[x][y].getGreen();
                b = rgb[x][y].getBlue();
                if(r + g + b >= 415) {
                    r = (r - 150 < 0) ? 0 : r - 150;
                    g = (g - 100 < 0) ? 0 : g - 100;
                    b = (b - 75 < 0) ? 0 : b - 75;
                    out.setRGB(x, y, RGBtoInt(new Color(r, g, b)));
                }
                else {
                    out.setRGB(x, y, RGBtoInt(rgb[x][y]));
                }
            }
        }
        return out;
    }
    
    public static void main(String[] args) {
        boolean debug = true;
        
        System.out.println("Enter the name of the file: ");
        Scanner in = new Scanner(System.in);
        String entry = in.nextLine();
        File imageFile = new File(entry);
        BufferedImage imageIn = null, imageOut = null;
        try {
            imageIn = ImageIO.read(imageFile);
        }
        catch(IOException e) {
            System.out.println(e.toString());
            System.out.println("Could not read image " + entry);
        }

        if(debug) {
            BufferedImage outBW = imageBlackNWhite(imageIn);
            BufferedImage outEd = imageEdge(imageIn);
            BufferedImage outRa = imageRandom16(imageIn);
            BufferedImage outSq = imageSquare(imageIn);
            BufferedImage outRh = imageReverseH(imageIn);
            BufferedImage outTe = imageTest(imageIn);
            File outGray = new File("outGray.png");
            File outEdge = new File("outEdge.png");
            File outRand = new File("outRand.png");
            File outSqua = new File("outSqua.png");
            File outRevh = new File("outRevh.png");
            File outTest = new File("outTest.png");
            try {
                ImageIO.write(outBW, "png", outGray);
                ImageIO.write(outEd, "png", outEdge);
                ImageIO.write(outRa, "png", outRand);
                ImageIO.write(outSq, "png", outSqua);
                ImageIO.write(outRh, "png", outRevh);
                ImageIO.write(outTe, "png", outTest);
            }
            catch(IOException e) {
                System.out.println(e.toString());
            }
            finally {
                System.out.println("Yeet");
            }
        }
        else {
            System.out.println("Enter the number for the image change: ");
            System.out.println("1 : BlackNWhite");
            System.out.println("2 : Edge");
            System.out.println("3 : Random16");
            System.out.println("4 : Square");
            System.out.println("5 : ReverseH");
            entry = in.nextLine();
            switch(entry) {
                case "1":
                    imageOut = imageBlackNWhite(imageIn);
                    break;
                case "2":
                    imageOut = imageEdge(imageIn);
                    break;
                case "3":
                    imageOut = imageRandom16(imageIn);
                    break;
                case "4":
                    imageOut = imageSquare(imageIn);
                    break;
                case "5":
                    imageOut = imageReverseH(imageIn);
                    break;
                default:

                    break;        
            }
            System.out.println("Enter the name of the new file: ");
            entry = in.nextLine();
            File out = new File(entry);
            try {
                ImageIO.write(imageOut, "png", out);
            }
            catch(IOException e) {
                System.out.println(e.toString());
            }
            
        }
    }
}
