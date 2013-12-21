package binaryAid;

import java.nio.ByteBuffer;
/**
 * The Class binaryHelper.
 * @author Christopher Waddell
 */
public class binaryHelper {
    /**
     * To binary.
     * This takes in an array of bytes and converts it to a binary string
     * @param bytes the bytes
     * @return the string
     */
    public static String toBinary( byte[] bytes )
    {
        StringBuilder sb = new StringBuilder(bytes.length * Byte.SIZE);
        for( int i = 0; i < Byte.SIZE * bytes.length; i++ )
            sb.append((bytes[i / Byte.SIZE] << i % Byte.SIZE & 0x80) == 0 ? '0' : '1');
        return sb.toString();
    }
    
    /**
     * To binary.
     * This takes in a byte, and converts it to a binary string. (8chars max)
     * @param bytes the bytes
     * @param verBose the ver bose
     * @return the string
     */
    public static String toBinary( byte bytes, boolean verBose )
    {
        StringBuilder sb = new StringBuilder(Byte.SIZE);
        for( int i = 0; i < Byte.SIZE; i++ )
            sb.append((bytes << i % Byte.SIZE & 0x80) == 0 ? '0' : '1');
        if(verBose){
            System.out.println("Bytes to Binary: " + sb.toString());
        }
        return sb.toString();
    }
    
    /**
     * From binary.
     * This takes in binary string and converts it to an integer with ByteBuffer Wrap,
     * It handles checking for a sync point.
     * @param s the s
     * @return the int
     */
    public static int fromBinary( String s )
    {
        int sLen = (s.length());
        byte[] toReturn = new byte[(sLen + Byte.SIZE - 1) / Byte.SIZE];
        char c;
        for( int i = 0; i < sLen; i++ )
            if( (c = s.charAt(i)) == '1' )
                toReturn[i / Byte.SIZE] = (byte) (toReturn[i / Byte.SIZE] | (0x80 >>> (i % Byte.SIZE)));
            else if ( c != '0' )
                throw new IllegalArgumentException();
        
        return ByteBuffer.wrap(toReturn).getInt();
    }
    
    /**
     * From binary a.
     * This deals with converting strings of size 8 or more into a byte array.
     * @param s the s
     * @return the int
     */
    public static byte[] fromBinaryA( String s )
    {
        int sLen = (s.length());
        byte[] toReturn = new byte[(sLen + Byte.SIZE - 1) / Byte.SIZE];
        char c;
        for( int i = 0; i < sLen; i++ )
            if( (c = s.charAt(i)) == '1' )
                toReturn[i / Byte.SIZE] = (byte) (toReturn[i / Byte.SIZE] | (0x80 >>> (i % Byte.SIZE)));
            else if ( c != '0' )
                throw new IllegalArgumentException();
        
        return toReturn;
    }
    
    /**
     * From binary b.
     * This converts a binary string of 8 into a byte.
     * @param s the s
     * @return the byte
     */
    public static byte fromBinaryB( String s )
    {
        int sLen = (s.length());
        byte[] toReturn = new byte[(sLen + Byte.SIZE - 1) / Byte.SIZE];
        char c;
        for( int i = 0; i < sLen; i++ )
            if( (c = s.charAt(i)) == '1' )
                toReturn[i / Byte.SIZE] = (byte) (toReturn[i / Byte.SIZE] | (0x80 >>> (i % Byte.SIZE)));
            else if ( c != '0' )
                throw new IllegalArgumentException();
        return toReturn[0];
    }
}
