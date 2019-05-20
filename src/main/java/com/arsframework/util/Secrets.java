package com.arsframework.util;

import java.util.Map;
import java.util.HashMap;
import java.security.*;
import javax.crypto.*;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.SecretKeySpec;

import com.arsframework.annotation.Nonnull;
import org.apache.commons.codec.binary.Base64;

/**
 * 数据加解密处理工具类
 *
 * @author yongqiang.wu
 */
public abstract class Secrets {
    /**
     * DES算法名称
     */
    public static final String DES = "DES";

    /**
     * AES算法名称
     */
    public static final String AES = "AES";

    /**
     * MD5算法名称
     */
    public static final String MD5 = "MD5";

    /**
     * SHA-1算法名称
     */
    public static final String SHA_1 = "SHA-1";

    /**
     * 算法名称/消息摘要映射
     */
    private static final ThreadLocal<Map<String, MessageDigest>> digests = ThreadLocal.withInitial(() -> new HashMap<>());

    /**
     * 算法名称/密钥/加解密模型/密码处理器映射
     */
    private static final ThreadLocal<Map<String, Map<String, Map<Integer, Cipher>>>> ciphers =
            ThreadLocal.withInitial(() -> new HashMap<>());

    /**
     * 从缓存中获取加解密模式/密码处理器映射表
     *
     * @param algorithm 算法名称
     * @param key       密钥
     * @return 加解密模式/密码处理器映射表
     */
    @Nonnull
    private static Map<Integer, Cipher> getCacheModeCiphers(String algorithm, String key) {
        Map<String, Map<String, Map<Integer, Cipher>>> ciphers = Secrets.ciphers.get();
        Map<String, Map<Integer, Cipher>> keyCiphers = ciphers.get(algorithm);
        if (keyCiphers == null) {
            keyCiphers = new HashMap<>();
            ciphers.put(algorithm, keyCiphers);
        }
        Map<Integer, Cipher> modeCiphers = keyCiphers.get(key);
        if (modeCiphers == null) {
            modeCiphers = new HashMap<>();
            keyCiphers.put(key, modeCiphers);
        }
        return modeCiphers;
    }

    /**
     * 构建算法摘要
     *
     * @param algorithm 算法名称
     * @return 摘要对象
     * @throws NoSuchAlgorithmException 算法不存在异常
     */
    @Nonnull
    public static MessageDigest buildDigest(String algorithm) throws NoSuchAlgorithmException {
        Map<String, MessageDigest> digests = Secrets.digests.get();
        MessageDigest digest = digests.get(algorithm);
        if (digest == null) {
            digest = MessageDigest.getInstance(algorithm);
            digests.put(algorithm, digest);
        }
        return digest;
    }

    /**
     * 构建DES密码处理器
     *
     * @param key  密钥
     * @param mode 处理类型（加密/解密）
     * @return 密码处理器
     * @throws GeneralSecurityException 密码处理异常
     */
    @Nonnull
    public static Cipher buildDESCipher(String key, int mode) throws GeneralSecurityException {
        Map<Integer, Cipher> ciphers = getCacheModeCiphers(DES, key);
        Cipher cipher = ciphers.get(mode);
        if (cipher == null) {
            DESKeySpec spec = new DESKeySpec(key.getBytes());
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(DES);
            SecretKey secretKey = keyFactory.generateSecret(spec);
            cipher = Cipher.getInstance(DES);
            cipher.init(mode, secretKey, new SecureRandom());
            ciphers.put(mode, cipher);
        }
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
    @Nonnull
    public static Cipher buildAESCipher(String key, int mode) throws GeneralSecurityException {
        Map<Integer, Cipher> ciphers = getCacheModeCiphers(AES, key);
        Cipher cipher = ciphers.get(mode);
        if (cipher == null) {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(AES);
            keyGenerator.init(128, new SecureRandom(key.getBytes()));
            SecretKeySpec secretKeySpec = new SecretKeySpec(keyGenerator.generateKey().getEncoded(), AES);
            cipher = Cipher.getInstance(AES);
            cipher.init(mode, secretKeySpec);
            ciphers.put(mode, cipher);
        }
        return cipher;
    }

    /**
     * MD5加密
     *
     * @param source 明文
     * @return 密文
     * @throws GeneralSecurityException 加密异常
     */
    @Nonnull
    public static String md5(String source) throws GeneralSecurityException {
        MessageDigest digest = buildDigest(MD5);
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
    @Nonnull
    public static String sha1(String source) throws GeneralSecurityException {
        MessageDigest digest = buildDigest(SHA_1);
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
    @Nonnull
    public static String des(String source, String key) throws GeneralSecurityException {
        return Base64.encodeBase64String(buildDESCipher(key, Cipher.ENCRYPT_MODE).doFinal(source.getBytes()));
    }

    /**
     * DES解密
     *
     * @param source 密文
     * @param key    秘钥
     * @return 明文
     * @throws GeneralSecurityException 解密异常
     */
    @Nonnull
    public static String undes(String source, String key) throws GeneralSecurityException {
        return new String(buildDESCipher(key, Cipher.DECRYPT_MODE).doFinal(Base64.decodeBase64(source)));
    }

    /**
     * AES加密
     *
     * @param source 数据源
     * @param key    秘钥
     * @return 密文（base64）
     * @throws GeneralSecurityException 加密异常
     */
    @Nonnull
    public static String aes(String source, String key) throws GeneralSecurityException {
        return Base64.encodeBase64String(buildAESCipher(key, Cipher.ENCRYPT_MODE).doFinal(source.getBytes()));
    }

    /**
     * AES解密
     *
     * @param source 数据源（base64）
     * @param key    秘钥
     * @return 明文
     * @throws GeneralSecurityException 解密异常
     */
    @Nonnull
    public static String unaes(String source, String key) throws GeneralSecurityException {
        return new String(buildAESCipher(key, Cipher.DECRYPT_MODE).doFinal(Base64.decodeBase64(source)));
    }

}
