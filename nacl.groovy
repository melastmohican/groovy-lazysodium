#!/usr/bin/env groovy

@GrabResolver(name='bintray', root='https://dl.bintray.com/terl/lazysodium-maven')
@Grab('com.goterl.lazycode:lazysodium-java:2.5.0')
@Grab('info.picocli:picocli:2.0.3')
@picocli.groovy.PicocliScript

import com.goterl.lazycode.lazysodium.LazySodiumJava;
import com.goterl.lazycode.lazysodium.SodiumJava;
import com.goterl.lazycode.lazysodium.exceptions.SodiumException;
import com.goterl.lazycode.lazysodium.interfaces.SecretBox;

import javax.xml.bind.DatatypeConverter;
import java.nio.charset.StandardCharsets;

import groovy.transform.Field
import static picocli.CommandLine.*

@Parameters(index = "0",  arity = "1",  description = "Input")  @Field String input;
@Parameters(index = "1",  arity = "0..1",  description = "Nonce")  @Field String nonce="";

@Option(names = ["-e", "--encrypt"], description = "Encrypt text")
@Field boolean encryptOpt

@Option(names = ["-d", "--decrypt"], description = "Decrypt text")
@Field boolean decryptOpt

lazySodium = new LazySodiumJava(new SodiumJava(), StandardCharsets.UTF_8);
secretBoxLazy = (SecretBox.Lazy) lazySodium;

Properties properties = new Properties()
new File(System.getProperty("user.home") + "/.nacl/nacl.properties").withInputStream {
    properties.load(it)
}

key = properties["libsodium.key"] as String

def encrypt(String textToEncrypt) {
    byte[] nonceBytes = lazySodium.nonce(SecretBox.NONCEBYTES);
    String nonceHexString = DatatypeConverter.printHexBinary(nonceBytes);
    
    encryptedMessage = secretBoxLazy.cryptoSecretBoxEasy(textToEncrypt, nonceBytes, key);
    return [encryptedMessage, nonceHexString];
}

def decrypt(String encryptedMessage, String nonce) {
    byte[] nonceBytes = DatatypeConverter.parseHexBinary(nonce);
    decryptedMessage = secretBoxLazy.cryptoSecretBoxOpenEasy(encryptedMessage, nonceBytes, key);
    return decryptedMessage;
}

if(encryptOpt) {
    (encrypted,nonceStr) = encrypt(input)
    println encrypted
    println nonceStr
}
else if(decryptOpt) {
    String decrypted = decrypt(input,nonce)
    println decrypted
}