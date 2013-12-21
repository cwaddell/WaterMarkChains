package waterMarkEmbedControl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.SecretKey;

import binaryAid.binaryHelper;
/**
 * The Class WatermarkEmbeddingAlgorithms.
 * @author Christopher Waddell
 */
public class WatermarkEmbeddingAlgorithms {
    /** The s is a stream of data elements. */
    private byte[] S;

    /** The s is datum generated from the stream of data elements. */
    private byte[] s;

    /** The s index is what datum we have and haven't seen. */
    private int sIndex;
    
    /** The s index start. */
    private int sIndexStart;
    
    /** The s index end. */
    private int sIndexEnd;
    
    /** The l, a lower-bound on group size. */
    private int L;
    
    /** The W is our watermark embedded in a group (current and next) hash. */
    private String W;
    
    /** The k is our secret key for the hash function. */
    private SecretKey K;
    
    /** The h is the hash values of current data elements. */
    private String[] h;
    
    /** The j0 is our current group location. */
    private int j0;
    
    /** The j1 is our next group location. */
    private int j1;
    
    /** The Hj0 is the hash value of the current group. */
    private String Hj0;
   
    /** The Hj1 is the hash value of the next group. */
    private String Hj1;
    
    /** The buffer that holds our bytes from the first and second group. */
    private byte[][] buff;
    
    /** The k holds the number of dataElements, in our first and second groups. */
    private int[] k = new int[2];
    
    /** The m is our sync point. */
    private int m;
    
    /** The stream is still alive?. */
    private boolean streamDead = false;
    
    /** The fop that will be writing our bytes . */
    private FileOutputStream fop;
    
    /**
     * Instantiates a new watermark embedding algorithms.
     *
     * @param streamingData the streaming data
     * @param maxGroupSize the max group size
     * @param synchronizationVar the synchronization var
     * @param key the key
     * @param stream the stream
     */
    public WatermarkEmbeddingAlgorithms(byte[] streamingData, int maxGroupSize, int synchronizationVar, SecretKey key, File stream){
        //Our streaming data
        S = streamingData;
        //An element
        s = new byte[S.length];
        //Write the stream into our internal storage
        for (int i=0; i < S.length; i++) {
            s[i] = S[i];
        }
        sIndex = 0;
        h = new String[s.length];
        buff = new byte[2][s.length];
        L = maxGroupSize;
        m = synchronizationVar;
        try {
            fop = new FileOutputStream(stream);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        //key = KeyGenerator.getInstance("AES").generateKey();
        K = key;
        //Start the embedding
        WatermarkEmbedding();
    }
    
    //ALGORITHM 1
    /**
     * Watermark embedding.
     *
     */
    public void WatermarkEmbedding(){
        //clear buff0:
        for (int i=0; i < buff[0].length; i++) {
            buff[0][i] = 0;
        }
        k[0] = 0;
        
        //clear buff1:
        for (int i=0; i < buff[1].length; i++) {
            buff[1][i] = 0;
        }
        k[1] = 0;
        
        j0 = 0;
        //fill our buffer
        fillBuff(j0);
        //hash the new value
        Hj0 = getGroupHash(j0, K);
        
        //if the stream has died, end the embedding
        while (streamDead!=true) {
            //if we have no data, close the stream and file
            if(k[j0] == 0)
            {
                try {
                    fop.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                streamDead = true;
                break;
            }
            
            j1 = (j0 + 1)%2;
            
            //fill the buffer
            fillBuff(j1);
            
            //hash the group
            Hj1 = getGroupHash(j1, K);
            //System.out.println("The first groupHash: " + Hj0);
            //System.out.println("The moving groupHash: " + Hj1);
            //embed the group
            WatermarkEmbed(j0, Hj0, Hj1);
            
            //write the buffer to a file
            for (int i=0; i < k[j0]; i++) {
                try {
                    //System.out.println(binaryHelper.toBinary(buff[j0][i]));
                    fop.write(buff[j0][i]);
                    fop.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //System.out.print((char)buff[j0][i]);
                buff[j0][i] = 0;
                
            }
            k[j0] = 0;
            //System.out.println(j0);
            j0 = j1;
            //System.out.println(j0);
            //Hj0=Hj1;//delete this if it doesn't work
        }
    }
    //ALGORITHM 2
    /**
     * Fill buff.
     *
     * @param buffNum the buff num
     */
    public void fillBuff(int buffNum) {
        k[buffNum] = 0;
        //while (*receive an incoming data s*) {
        //System.out.println(s.length);
        sIndexStart = sIndex;
        while ( sIndex < s.length) {
            String binaryStr = binaryHelper.toBinary(s[sIndex], false);
            binaryStr = binaryStr.substring(0,7);// + "0";
            h[sIndex] = hash(K, binaryStr);
            buff[buffNum][k[buffNum]++] = s[sIndex];
            //fix this line
            //int value = Integer.parseInt(h[sIndex]%m==0);
            if (binaryHelper.fromBinary(h[sIndex])%m == 0 && (k[buffNum] >= L)) {
                sIndexEnd = sIndex;
                sIndex++;
                return;
            }
            sIndex++;
        }
        //System.exit(0);
    }
    //ALGORITHM 3
    /**
     * Gets the group hash.
     *
     * @param buffNum the buff num
     * @param key the key
     * @return the group hash
     */
    public String getGroupHash(int buffNum, SecretKey key) {
        StringBuilder hGroup = new StringBuilder();
        //String hGroup = "";
        for (int i=sIndexStart; i < sIndexEnd; i++) {
            hGroup.append(h[i]);
        }
        String groupHash = hash(key, hGroup.toString());
        //System.out.println("GroupHash: " + groupHash);
        return groupHash;
    }
    
    //ALGORITHM 4
    /**
     * Watermark embed.
     *
     * @param buffNum the buff num
     * @param groupHashj0 the group hashj0
     * @param groupHashj1 the group hashj1
     */
    public void WatermarkEmbed(int buffNum, String groupHashj0, String groupHashj1) {
        //Concat the groups
        String combinedGroupHash = groupHashj0 + groupHashj1;
        //System.out.println("Combined Group: " + combinedGroupHash);
        //Hash the watermark
        String WatermarkHash = hash(K, combinedGroupHash);
        //k[buffNum]=buff[buffNum].length;
        //Extract bits from the watermark
        //System.out.println(WatermarkHash.length());
        //zSystem.out.println(k[buffNum]);
        //System.out.println("This is the WatermarkHash: " + WatermarkHash);
        W = extractBits(WatermarkHash, k[buffNum]);
        System.out.println("This is the watermark: " + W);// + " " + m);
        byte c;
        String temp;
        //set the last bit of buffer[i] = W[i];
        for (int i=0; i < k[buffNum]; i++) {
            c = buff[buffNum][i];
            temp = binaryHelper.toBinary(c,false);
          //System.out.print("Before Embed: " + temp + "\t");
            temp = temp.substring(0,7) + W.charAt(i);
          //System.out.print("After Embed: " + temp + "\n");
            buff[buffNum][i]= binaryHelper.fromBinaryB(temp);
        }
    }
    
    //ALGORITHM 5
    /**
     * Extract bits.
     *
     * @param WatermarkHash the watermark hash
     * @param numOfElementsInGroup the num of elements in group
     * @return the string
     */
    public String extractBits(String WatermarkHash, int numOfElementsInGroup) {
        String watermark="";
        //Determine a watermark
        if (WatermarkHash.length() >= numOfElementsInGroup) {
            //Watermark it if it's greater than the elements in the group
            watermark = WatermarkHash.substring(0, numOfElementsInGroup);
        }
        else {
            //Set the sync point
            m = numOfElementsInGroup - WatermarkHash.length();
            watermark = WatermarkHash.concat(extractBits(WatermarkHash, m));
        }
        //System.out.println("Watermark Binary: " + watermark);
        return watermark;
    }
    
    //Generate HMACSHA256 hash.
    /**
     * Hash.
     *
     * @param K the k
     * @param s the s
     * @return the string
     */
    public static String hash(SecretKey K, String s) {
        //System.out.println(s);
        try {
            Mac mac = Mac.getInstance(K.getAlgorithm());           
            try {
                mac.init(K);
            } catch (InvalidKeyException e) {
                e.printStackTrace();
            }
            byte[] b;
            byte[] hash = null;
            try {
                b = s.getBytes("UTF-8");
                hash = mac.doFinal(b);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            //Convert to binary
            String binary;
            binary = binaryHelper.toBinary(hash);
            return binary;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return "FAILURE";
        }
    }
}
