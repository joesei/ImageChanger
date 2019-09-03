
import java.awt.Color;
import java.awt.image.BufferedImage;
import static java.awt.image.BufferedImage.TYPE_INT_RGB;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import javax.imageio.ImageIO;

/* 
@author Jose M. Hernandez
Notes:A
*/
public class Main {
    
    //Convert int to RGB values in Color object
    public static Color intToRGB(int i) {
        int r, g, b;
        //Gets the binary string representation of the integer with 24 bits
        String binary = Integer.toBinaryString(i);
        while(binary.length() < 24) {
            binary = "0" + binary;
        }
        //Then split into seperate R G B binary strings
        //Bits 0-7 are Red, 8-15 Blue, 16-23 Green
        //Convert those substrings back to int
        r = Integer.parseInt(binary.substring(0, 7), 2);
        g = Integer.parseInt(binary.substring(8, 15), 2);
        b = Integer.parseInt(binary.substring(16, 23), 2);
        return new Color(r, g, b);
    }
    
    //Convert RGB values to int
    public static int RGBtoInt(Color rgb) {
        String r = Integer.toBinaryString(rgb.getRed());
        String g = Integer.toBinaryString(rgb.getGreen());
        String b = Integer.toBinaryString(rgb.getBlue());
        while(r.length() < 8 || g.length() < 8 || b.length() < 8) {
            if(r.length() < 8) {
                r = "0" + r;
            }
            if(g.length() < 8) {
                g = "0" + g;
            }
            if(b.length() < 8) {
                b = "0" + b;
            }
        }
        return Integer.parseInt(r + g + b, 2);
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
    
    public static void main(String[] args) {
        
        System.out.println("Enter the name of the file: ");
        Scanner in = new Scanner(System.in);
        String entry = in.nextLine();
        File imageFile = new File(entry);
        BufferedImage image = null,imageOut = null;
        int width, height;
        try {
            image = ImageIO.read(imageFile);
        }
        catch(IOException e) {
            System.out.println(e.toString());
            System.out.println("Could not read image " + entry);
        }
        finally {
            if(image != null) {
                width = image.getWidth();
                height = image.getHeight();
                BufferedImage outBW = imageBlackNWhite(image);
                BufferedImage outEd = imageEdge(image);
                File outGray = new File("outBlackNWhite.png");
                File outEdge = new File("outEdge.png");
                try {
                    boolean write = ImageIO.write(outBW, "png", outGray);
                    boolean write1 = ImageIO.write(outEd, "png", outEdge);
                }
                catch(IOException e) {
                    System.out.println(e.toString());
                }
                finally {
                    System.out.println("Yeet");
                }
            }
        }
    }
}
