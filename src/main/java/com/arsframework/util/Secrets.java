package com.arsframework.util;

import java.security.*;
import javax.crypto.*;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;

import com.arsframework.annotation.Assert;

/**
 * @author yongqiang.wu
 * @description 数据加解密处理工具类
 * @date 2019-03-20 17:19
 */
public abstract class Secrets {
    /**
     * 构建DES密码处理器
     *
     * @param key  密钥
     * @param mode 处理类型（加密/解密）
     * @return 密码处理器
     * @throws GeneralSecurityException 密码处理异常
     */
    @Assert
    public static Cipher buildDESCipher(String key, int mode) throws GeneralSecurityException {
        DESKeySpec spec = new DESKeySpec(key.getBytes());
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
        SecretKey secretKey = keyFactory.generateSecret(spec);
        Cipher cipher = Cipher.getInstance("DES");
        cipher.init(mode, secretKey, new SecureRandom());
        return cipher;
    }

    /**
     * 构建AES密码处理器
     *
     * @param key  密钥
     * @param mode 处理类型（加密/解密）
     * @return 密码处理器
     * @throws GeneralSecurityException 密码处理异常
     */
    @Assert
    public static Cipher buildAESCipher(String key, int mode) throws GeneralSecurityException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(128, new SecureRandom(key.getBytes()));
        SecretKeySpec secretKeySpec = new SecretKeySpec(keyGenerator.generateKey().getEncoded(), "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(mode, secretKeySpec);
        return cipher;
    }

    /**
     * MD5加密
     *
     * @param source 明文
     * @return 密文
     * @throws GeneralSecurityException 加密异常
     */
    @Assert
    public static String md5(String source) throws GeneralSecurityException {
        MessageDigest digest = MessageDigest.getInstance("MD5");
        digest.update(source.getBytes());
        return Base64.encodeBase64String(digest.digest());
    }

    /**
     * SHA-1加密
     *
     * @param source 明文
     * @return 密文
     * @throws GeneralSecurityException 加密异常
     */
    @Assert
    public static String sha1(String source) throws GeneralSecurityException {
        MessageDigest digest = MessageDigest.getInstance("SHA-1");
        digest.update(source.getBytes());
        return Base64.encodeBase64String(digest.digest());
    }

    /**
     * DES加密
     *
     * @param source 明文
     * @param key    密钥
     * @return 密文
     * @throws GeneralSecurityException 加密异常
     */
    @Assert
    public static String des(String source, String key) throws GeneralSecurityException {
        Cipher cipher = buildDESCipher(key, Cipher.ENCRYPT_MODE);
        return Base64.encodeBase64String(cipher.doFinal(source.getBytes()));
    }

    /**
     * DES解密
     *
     * @param source 密文
     * @param key    秘钥
     * @return 明文
     * @throws GeneralSecurityException 解密异常
     */
    @Assert
    public static String undes(String source, String key) throws GeneralSecurityException {
        Cipher cipher = buildDESCipher(key, Cipher.DECRYPT_MODE);
        return new String(cipher.doFinal(Base64.decodeBase64(source)));
    }

    /**
     * AES加密
     *
     * @param source 数据源
     * @param key    秘钥
     * @return 密文（base64）
     * @throws GeneralSecurityException 加密异常
     */
    @Assert
    public static String aes(String source, String key) throws GeneralSecurityException {
        Cipher cipher = buildAESCipher(key, Cipher.ENCRYPT_MODE);
        return Base64.encodeBase64String(cipher.doFinal(source.getBytes()));
    }

    /**
     * AES解密
     *
     * @param source 数据源（base64）
     * @param key    秘钥
     * @return 明文
     * @throws GeneralSecurityException 解密异常
     */
    @Assert
    public static String unaes(String source, String key) throws GeneralSecurityException {
        Cipher cipher = buildAESCipher(key, Cipher.DECRYPT_MODE);
        return new String(cipher.doFinal(Base64.decodeBase64(source)));
    }

}
