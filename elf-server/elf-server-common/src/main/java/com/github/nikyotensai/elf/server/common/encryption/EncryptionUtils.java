/*
 * Copyright (C) 2019 Qunar, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.github.nikyotensai.elf.server.common.encryption;

import java.io.File;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

import org.springframework.core.io.ClassPathResource;

import com.github.nikyotensai.elf.common.FileUtil;
import com.google.common.base.Charsets;
import com.google.common.io.Files;

import cn.hutool.core.codec.Base64Decoder;
import cn.hutool.core.codec.Base64Encoder;

public class EncryptionUtils {

    public static PublicKey loadRSAPublicKey(String path) throws IOException, InvalidKeySpecException {
        ClassPathResource pathResource = new ClassPathResource(path);
        byte[] bb = FileUtil.readBytes(pathResource.getInputStream());
        X509EncodedKeySpec spec = new X509EncodedKeySpec(Base64Decoder.decode(bb));
        try {
            return KeyFactory.getInstance("RSA").generatePublic(spec);
        } catch (NoSuchAlgorithmException e) {
            // ignore;
            return null;
        }
    }

    public static PrivateKey loadRSAPrivateKey(String path) throws IOException, InvalidKeySpecException {
        ClassPathResource pathResource = new ClassPathResource(path);
        byte[] bb = FileUtil.readBytes(pathResource.getInputStream());
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(Base64Decoder.decode(bb));
        try {
            return KeyFactory.getInstance("RSA").generatePrivate(spec);
        } catch (NoSuchAlgorithmException e) {
            // ignore;
            return null;
        }
    }

    public static SecretKey loadDesKey(String path) throws InvalidKeySpecException, IOException, InvalidKeyException {
        String s = FileUtil.readString(new File(EncryptionUtils.class.getResource(path).getPath()), Charsets.UTF_8.name());
        DESKeySpec spec = new DESKeySpec(Base64Decoder.decode(s));
        try {
            return SecretKeyFactory.getInstance("DES").generateSecret(spec);
        } catch (NoSuchAlgorithmException e) {
            // ignore
            return null;
        }
    }

    public static KeyPair createKeyPair(String algorithm) throws NoSuchAlgorithmException, NoSuchProviderException {
        KeyPairGenerator generator = KeyPairGenerator.getInstance(algorithm);
        generator.initialize(1024);
        return generator.generateKeyPair();
    }

    public static SecretKey createKey(String algorithm) throws NoSuchAlgorithmException, NoSuchProviderException {
        KeyGenerator generator = KeyGenerator.getInstance(algorithm);
        return generator.generateKey();
    }

    public static void serializeKey(String dstFile, Key key) throws IOException {
        Files.write(Base64Encoder.encode(key.getEncoded()), new File(dstFile), Charsets.UTF_8);
    }

    public static String decryptDes(String data, String keyString) throws Encryption.DecryptException {
        try {
            Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
            DESKeySpec keySpec = new DESKeySpec(keyString.getBytes(Charsets.UTF_8));
            SecretKey key = SecretKeyFactory.getInstance("DES").generateSecret(keySpec);
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] bytes = Base64Decoder.decode(data);
            return new String(cipher.doFinal(bytes), Charsets.UTF_8);
        } catch (Exception e) {
            throw new Encryption.DecryptException(e);
        }
    }

    public static String encryptDes(String data, String keyString) throws Encryption.EncryptException {
        try {
            Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
            DESKeySpec keySpec = new DESKeySpec(keyString.getBytes(Charsets.UTF_8));
            SecretKey key = SecretKeyFactory.getInstance("DES").generateSecret(keySpec);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return Base64Encoder.encode(cipher.doFinal(data.getBytes(Charsets.UTF_8)));
        } catch (Exception e) {
            throw new Encryption.EncryptException(e);
        }

    }
}
