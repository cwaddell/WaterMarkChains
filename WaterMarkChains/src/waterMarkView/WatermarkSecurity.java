package waterMarkView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Random;
import java.util.Scanner;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import waterMarkEmbedControl.WatermarkEmbeddingAlgorithms;
import waterMarkVerifyControl.WatermarkVerificationAlgorithms;
/**
 * The Class WatermarkSecurity.
 * @author Christopher Waddell
 */
public class WatermarkSecurity {
    /** The secret key for the hash function, we must know it for both embedding and verifying. */
    private static SecretKey key;
    
    /** The file/stream we will be using. */
    private static File file = new File("doc.txt");
    
    /** The file/stream we will be using. */
    private static File fileWater = new File("stream.txt");
    
    /** The buffer that will read in the whole file. */
    private static byte[] buffer = new byte[(int)file.length()];
    
    /** The max preferred group size. */
    private static int maxGroupSize;
    
    /** The sync var. */
    private static int syncVar;
    
    /** The sc for reading input. */
    private static Scanner sc;
    
    /** The in stream for reading a file in. */
    private static FileInputStream in;
    
    /**
     * The main method.
     * 
     * This starts the project, enables us to Embed a Watermark.
     * It will also allow us to Verify a watermark.
     *
     * @param argv the arguments
     */
    public static void main(String argv[]) {
        
        init();
        menu();
        while(sc.hasNextLine()){ 
            int menuItem = sc.nextInt();
            switch(menuItem)
            {
                case 0: 
                        key();
                        break;
                case 1: 
                        readInOriginal();
                        embed();
                        break;
                case 2: 
                        readInEmbedded();
                        verify();
                        break;
                case 3: 
                        readInOriginal();
                        verify();
                        break;
                case 4: 
                        readInEmbedded();
                        tamper();
                        verify();
                        break;
                case 5: //does same thing as 2 needed for DEMO.
                        readInEmbedded();
                        tamper2();
                        verify();
                        break;
                case 6: 
                        System.exit(0);
            }
            menu();
            
        }
    }   
    
    /**
     * Menu view, for displaying a menu.
     */
    public static void menu(){
        System.out.println("\n\nWaterMark Chain System");
        System.out.println("----------------------");
        System.out.println("0: Change the Key     ");
        System.out.println("1: Embed the File     ");
        System.out.println("2: Verify the File    ");
        System.out.println("3: Check Original     ");
        System.out.println("4: Random Tamper Test ");
        System.out.println("5: Random 3 TamperTest");
        System.out.println("6: Exit the system    ");
    }
    
    /**
     * Inits the system.
     */
    public static void init(){
        sc = new Scanner(System.in);
        key();
        readInOriginal();
        //34
        maxGroupSize = (int) (file.length() / (file.length()/24)+1)+1;
        //18
        syncVar = (int)(file.length()/(file.length()/12)+1)+1;
        System.out.println("System Init Complete");
    }
    
    /**
     * Read in original data buffer.
     */
    public static void readInOriginal(){
        System.out.println("Reading in original data ");
        buffer = new byte[(int)file.length()];
        try {
            in = new FileInputStream(file);
            in.read(buffer);
            in.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Finished reading in original data ");
    }
    
    /**
     * Read in embedded data buffer.
     */
    public static void readInEmbedded(){
        System.out.println("Reading in embedded data ");
        buffer = new byte[(int)fileWater.length()];
        try {
            in = new FileInputStream(fileWater);
            in.read(buffer);
            in.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Finished reading in embedded data ");
    }
    
    /**
     * Key initializer, inits a new key for demonstration of how it changes the system.
     */
    public static void key(){
        System.out.println("Resetting the key ");
        try {
            key = KeyGenerator.getInstance("HmacSHA256").generateKey();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        System.out.println("Key Reset ");
    }
    
    /**
     * Embed a file with our watermark.
     */
    public static void embed(){
        System.out.println("Embedding the watermark started ");
        new WatermarkEmbeddingAlgorithms(buffer, maxGroupSize, syncVar, key, fileWater);
        System.out.println("Embedding the watermark finished");
    }
    
    /**
     * Verify that a file has/hasn't been tampered.
     */
    public static void verify(){
        System.out.println("Verifying the watermark started ");
        new WatermarkVerificationAlgorithms(buffer, maxGroupSize, syncVar, key);
        System.out.println("Embedding the watermark finished ");
    }
    
    /**
     * Tamper the system by shifting 1 byte backwards.
     */
    public static void tamper(){
        Random rand = new Random();
        System.out.println("Tampering Data started ");
        // nextInt is normally exclusive of the top value,
        // so add 1 to make it inclusive
        int randomByte = rand.nextInt((buffer.length - 1) + 1) + 1;
        buffer[randomByte] = buffer[randomByte-1];
        System.out.println("Tampering Data finished ");
    }
    /**
     * Tamper the system by shifting 1 byte backwards.
     */
    public static void tamper2(){
        Random rand = new Random();
        System.out.println("Tampering Data started ");
        // nextInt is normally exclusive of the top value,
        // so add 1 to make it inclusive
        int i = 0;
        while(i<3)
        {
        int randomByte = rand.nextInt((buffer.length - 1) + 1) + 1;
        buffer[randomByte] = buffer[randomByte-1];
        i++;
        }
        System.out.println("Tampering Data finished ");
    }
}
