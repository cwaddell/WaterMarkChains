package waterMarkVerifyControl;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.SecretKey;

import binaryAid.binaryHelper;
/**
 * The Class WatermarkVerificationAlgorithms.
 * @author Christopher Waddell
 */
public class WatermarkVerificationAlgorithms {
    
    /** The group in. */
    private int groupIn = 0;
    
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
    
    //SecretKey key;
    
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
	
	/** The W1 is the watermark that was originally generated. */
	private String W1="";
	
	/** The W2 is the watermark that ought to have been regenerated. */
	private String W2="";
	
	//A boolean to store the preliminary watermark verification results of the
	//	previous group.
	/** The p v0. */
	private boolean pV0;
	
	//A boolean to store the preliminary watermark verification results of the
	//	next group.
	/** The p v1. */
	private boolean pV1;
	
	//A boolean to store the final watermark verification results of the previous
	//	group.
	/** The V0. */
	private boolean V0;
	
	//A boolean to store the final watermark verification results of the next
	//	group.
	/** The V1. */
	private boolean V1;

	/**
	 * Instantiates a new watermark verification algorithms.
	 *
	 * @param streamingData the streaming data
	 * @param maxGroupSize the max group size
	 * @param synchronizationVar the synchronization var
	 * @param key the key
	 */
	public WatermarkVerificationAlgorithms(byte[] streamingData, int maxGroupSize, int synchronizationVar, SecretKey key){
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
        
        K = key;
        
		WatermarkVerification();
	}
	
	//ALGORITHM 6
	/**
	 * Watermark verification.
	 */
	public void WatermarkVerification() {
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
		pV0 = false;
		V0 = false;
		fillBuff(j0);
		Hj0 = getGroupHash(j0, K);
		
		while(streamDead!=true) {
            if(k[j0] == 0)
            {
                streamDead = true;
                break;
            }
			j1 = (j0 + 1)%2;
			fillBuff(j1);
			Hj1 = getGroupHash(j1, K);

			WatermarkVerify(j0, Hj0, Hj1, pV0, V0);
			
			//clear buff0:
			for (int i=0; i < k[j0]; i++) {
				buff[j0][i] = 0;
			}
			k[j0] = 0;
			
			j0 = j1;
			pV0 = pV1;
			V0 = V1;
		}
	}
	
	//ALGORITHM 7
	/**
	 * Watermark verify.
	 *
	 * @param buffNum the buff num
	 * @param groupHashj0 the group hashj0
	 * @param groupHashj1 the group hashj1
	 * @param prelimVer the prelim ver
	 * @param ver the ver
	 */
	public void WatermarkVerify(int buffNum, String groupHashj0, String groupHashj1, boolean prelimVer, boolean ver) {
		String combinedGroupHash = groupHashj0 + groupHashj1;
		String WatermarkHash = hash(K,combinedGroupHash);
		//create's the actual watermark
		W1 = extractBits(WatermarkHash, k[buffNum]);
		//resets the watermark
		W2 = "";
		byte c;
        String temp;
		for (int i=0; i < k[buffNum]; i++) {
		    c = buff[buffNum][i];
            temp = binaryHelper.toBinary(c,false);
            //Get the last bit of whats in our buffer
            W2 = W2 + temp.charAt(7);//temp.substring(7,8);//+ W2.charAt(i);
		}
		//Show the created watermark, and the one pulled from the buffer
		//System.out.println("System-Generated: " + W1 
		//              +  "\nBuffer-Built:     " + W2);
		if (!W1.equals(W2)) {
		    pV1 = false;
		    V1 = (V0 && pV0);
		}
		else{
			V1 = pV1 = true;			
		}
		if(pV0 == true && V0 == true && pV1 == true && V1 == true)
		{
		    System.out.println("Group " + groupIn + ": Valid");
		}
		else if(pV0 == false && V0 == false && pV1 == true && V1 == true)
		{
		    System.out.println("Group " + groupIn + ": Valid, Previous Group Detected Missing");
		}
		else if(pV0 == false && V0 == true && pV1 == true && V1 == true)
        {
		    System.out.println("Group " + groupIn + ": Valid, Previous Group Detected Missing");
        }
		else if(pV0 == true && V0 == true && pV1 == false && V1 == true)
        {
            System.out.println("Group " + groupIn + ": InValid");
        }
		else if(pV0 == false && V0 == true && pV1 == false && V1 == false)
        {
            System.out.println("Group " + groupIn + ": InValid, Previous Group Detected Missing, Potential False Positive");
        }
		else if(pV0 == false && V0 == false && pV1 == false && V1 == false)
        {
            System.out.println("Group " + groupIn + ": InValid, Previous Group Detected Missing, Potential False Positive");
        }
		groupIn++;
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
            if (binaryHelper.fromBinary(h[sIndex])%m == 0 && (k[buffNum] >= L)) {
                sIndexEnd = sIndex;
                sIndex++;
                return;
            }
            sIndex++;
        }
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
        for (int i=sIndexStart; i < sIndexEnd; i++) {
            hGroup.append(h[i]);
        }
        String groupHash = hash(key, hGroup.toString());
        return groupHash;
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