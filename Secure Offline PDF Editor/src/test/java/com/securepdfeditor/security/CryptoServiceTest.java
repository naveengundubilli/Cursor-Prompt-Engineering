package com.securepdfeditor.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.crypto.SecretKey;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;

import static org.junit.jupiter.api.Assertions.*;

class CryptoServiceTest {
	@TempDir
	Path tempDir;

	Path plain;
	Path enc;
	Path dec;

	@BeforeEach
	void setUp() throws Exception {
		plain = tempDir.resolve("plain.txt");
		enc = tempDir.resolve("enc.bin");
		dec = tempDir.resolve("dec.txt");
		Files.writeString(plain, "Hello Milestone 1!");
	}

	@Test
	void encryptDecryptFile() throws Exception {
		CryptoService.encryptFile(plain, enc, "pass123");
		assertTrue(Files.exists(enc));
		CryptoService.decryptFile(enc, dec, "pass123");
		assertEquals(Files.readString(plain), Files.readString(dec));
	}

	@Test
	void wrongPasswordFails() throws Exception {
		CryptoService.encryptFile(plain, enc, "pass123");
		assertThrows(GeneralSecurityException.class, () -> CryptoService.decryptFile(enc, dec, "nope"));
	}

	@Test
	void encryptDecryptBytes() throws Exception {
		byte[] data = "data".getBytes();
		byte[] salt = new byte[32];
		new java.security.SecureRandom().nextBytes(salt);
		SecretKey key = CryptoService.derive("pwd", salt);
		byte[] enc = CryptoService.encrypt(data, key);
		byte[] decBytes = CryptoService.decrypt(enc, key);
		assertArrayEquals(data, decBytes);
	}

	@Test
	void deriveKeyValidation() {
		byte[] salt = new byte[32];
		assertThrows(IllegalArgumentException.class, () -> CryptoService.derive("", salt));
		assertThrows(IllegalArgumentException.class, () -> CryptoService.derive("x", new byte[16]));
	}
}
