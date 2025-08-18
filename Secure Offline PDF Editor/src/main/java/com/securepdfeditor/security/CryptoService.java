package com.securepdfeditor.security;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.Security;
import java.security.spec.KeySpec;
import java.util.Arrays;

public final class CryptoService {
	private CryptoService() {}

	static {
		if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
			Security.addProvider(new BouncyCastleProvider());
		}
	}

	private static final String CIPHER = "AES/GCM/NoPadding";
	private static final String PBKDF2 = "PBKDF2WithHmacSHA256";
	private static final int KEY_BITS = 256;
	private static final int SALT_LEN = 32;
	private static final int IV_LEN = 12;
	private static final int TAG_BITS = 128;
	private static final int ITER = 65536;

	public static void encryptFile(Path in, Path out, String password) throws Exception {
		byte[] data = Files.readAllBytes(in);
		byte[] salt = random(SALT_LEN);
		SecretKey key = derive(password, salt);
		byte[] enc = encrypt(data, key);
		byte[] outBytes = new byte[salt.length + enc.length];
		System.arraycopy(salt, 0, outBytes, 0, salt.length);
		System.arraycopy(enc, 0, outBytes, salt.length, enc.length);
		Files.write(out, outBytes);
	}

	public static void decryptFile(Path in, Path out, String password) throws Exception {
		byte[] all = Files.readAllBytes(in);
		if (all.length < SALT_LEN + IV_LEN + TAG_BITS/8) throw new IllegalArgumentException("Invalid encrypted file");
		byte[] salt = Arrays.copyOfRange(all, 0, SALT_LEN);
		byte[] enc = Arrays.copyOfRange(all, SALT_LEN, all.length);
		SecretKey key = derive(password, salt);
		byte[] dec = decrypt(enc, key);
		Files.write(out, dec);
	}

	public static byte[] encrypt(byte[] data, SecretKey key) throws GeneralSecurityException {
		byte[] iv = random(IV_LEN);
		Cipher c = Cipher.getInstance(CIPHER);
		c.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(TAG_BITS, iv));
		byte[] ct = c.doFinal(data);
		byte[] out = new byte[iv.length + ct.length];
		System.arraycopy(iv, 0, out, 0, iv.length);
		System.arraycopy(ct, 0, out, iv.length, ct.length);
		return out;
	}

	public static byte[] decrypt(byte[] enc, SecretKey key) throws GeneralSecurityException {
		if (enc.length < IV_LEN + TAG_BITS/8) throw new IllegalArgumentException("Invalid encrypted data");
		byte[] iv = Arrays.copyOfRange(enc, 0, IV_LEN);
		byte[] ct = Arrays.copyOfRange(enc, IV_LEN, enc.length);
		Cipher c = Cipher.getInstance(CIPHER);
		c.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(TAG_BITS, iv));
		return c.doFinal(ct);
	}

	public static SecretKey derive(String password, byte[] salt) throws GeneralSecurityException {
		if (password == null || password.isBlank()) throw new IllegalArgumentException("Empty password");
		if (salt == null || salt.length != SALT_LEN) throw new IllegalArgumentException("Invalid salt");
		SecretKeyFactory f = SecretKeyFactory.getInstance(PBKDF2);
		KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITER, KEY_BITS);
		SecretKey tmp = f.generateSecret(spec);
		return new SecretKeySpec(tmp.getEncoded(), "AES");
	}

	private static byte[] random(int n) {
		byte[] b = new byte[n];
		new java.security.SecureRandom().nextBytes(b);
		return b;
	}
}
